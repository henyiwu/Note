[toc]

## Binder进程间通讯系统

> binder机制采用CS通信方式，其中提供服务的进程称为Server进程，访问服务的进程称为Client进程。
>
> **同一个Server进程可以同时运行多个组件来向Client进程提供服务，这些组件称为Service组件**。同时，同一个Client进程也可以同时向多个Service组件请求服务，每一个请求都有一个对应的Client组件（或者称为Service代理对象）。
>
> binder进程间通信机制的每一个Client和Server进程都维护了一个Binder线程池来处理进程间通信请求，因此Server进程和Client进程可以并发地提供和访问服务。
>
> Binder驱动程序向用户空间暴露了一个设备文件/dev/binder，使得应用程序进程可以间接地通过它来建立通信通道。

- Client、Service、Service Manager和Binder驱动四个角色关系图

  ![](https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fs3.51cto.com%2Fwyfs02%2FM02%2F25%2F5C%2FwKioL1NeZCviVepTAAGNJpGF3ls945.gif&refer=http%3A%2F%2Fs3.51cto.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1655199526&t=5ebb77ff452d3c3864db3ef4a98439bf)

  Client、Service和Service Manager运行在用户空间，Binder驱动程序运行在内核空间。Service Manager和Binder驱动程序由系统负责提供，而Client、Service组件由应用程序实现。

  Client、Service和Service Manager均通过系统调用open、mmap和ioctl来访问设备文件/dev/binder，从而实现与Binder驱动程序的交互。

### 基础数据结构

> 在binder驱动中有两种类型的数据结构，一种是在内部使用的，另一种是在内部和外部都会使用到的。
>
> 以下源代码基于内核2.6.39

#### binder_work

> /drivers/staging/android/binder.c#binder_work

- binder.c

  ```c
  struct binder_work {
  struct list_head entry;
  	enum {
  		BINDER_WORK_TRANSACTION = 1,
  		BINDER_WORK_TRANSACTION_COMPLETE,
  		BINDER_WORK_NODE,
  		BINDER_WORK_DEAD_BINDER,
  		BINDER_WORK_DEAD_BINDER_AND_CLEAR,
  		BINDER_WORK_CLEAR_DEATH_NOTIFICATION,
  	} type;
  };
  ```

  结构体binder_work用来描述待处理的工作项，这些工作项有可能属于一个进程，也有可能属于一个进程中的某个线程

  成员变量entry用来将该结构体嵌入到一个宿主结构中。

  成员变量type用来描述工作项的类型，根据成员变量type的取值，binder驱动就可以判断出一个binder_work结构体嵌入到什么类型的宿主结构中。

#### binder_node

>/drivers/staging/android/binder.c#binder_node

- binder.c

  ```c
  struct binder_node { // 一个binder实体
  	int debug_id; // 标识一个binder实体对象的身份，帮助调试binder驱动
  	struct binder_work work;
  	union {
          /**
           * 宿主进程中使用一个红黑树来维护它内部所有的binder对象
           * 而每一个binder实体对象的成员变量rb_node正好是这个红黑树中的一个节点
           */
  		struct rb_node rb_node;
          /**
           * 如果binder实体对象的宿主进程已经死亡，那么这个binder实体对象就会通过dead_node保存在一个全局的hash列表中
           */
  		struct hlist_node dead_node;
  	};
      /**
  	 * proc：在binder驱动程序中，这些宿主进程通过一个binder_proc结构体来描述。
  	 * 宿主进程使用一个红黑树来维护它内部所有的Binder实体对象
       */
  	struct binder_proc *proc; // -> 宿主进程
      /**
       * refs：一个hash列表，记录了引用这个binder实体对象的client
       */
  	struct hlist_head refs;
  	int internal_strong_refs; // 描述一个binder实体对象的强引用计数
  	int local_weak_refs; // 用来描述一个binder实体对象的弱引用计数
  	int local_strong_refs; // 描述一个binder实体对象的强引用计数
  	void __user *ptr; // -> 用户空间service组件内部的一个引用计数对象（类型为weakref_impl）的地址
  	void __user *cookie; // -> 用户空间service组件的地址 
  	unsigned has_strong_ref:1;
  	unsigned pending_strong_ref:1;
  	unsigned has_weak_ref:1;
  	unsigned pending_weak_ref:1;
  	unsigned has_async_transaction:1; // binder实体对象是否正在处理一个异步事务?1:0
  	unsigned accept_fds:1; // binder实体是否可以接收包含有文件描述符的进程间通信数据
      /**
       * min_priority：最小优先级，binder实体对象在处理一个来自client进程的请求时，它所要求的处理线程，即Server进程中	 * 的一个线程
       */
  	unsigned min_priority:8; 
  	struct list_head async_todo;
  };
  ```

  结构体binder_node用来描述一个Binder实体对象，每一个Service组件在Binder驱动程序中都对应有一个Binder实体对象，用来描述它在内核中的状态。Binder驱动通过强引用计数和弱引用计数技术来维护它们（Binder实体对象）的生命周期。

- 引用计数

  成员变量internal_strong_refs、local_strong_refs描述一个binder实体对象的强引用计数

  成员变量local_weak_refs描述一个binder实体对象的弱引用计数

  当一个binder实体对象请求一个service组件来执行某一个操作时，该service组件的强引用计数或者弱引用计数增加，相应的，binder实体的has_strong_ref和has_weak_ref的值设置为1。

  当一个service组件完成一个binder实体对象所请求的操作之后，binder实体对象会减少该service组件的强或弱引用计数。

  binder实体对象在请求一个Service组件增加或者减少强引用计数或者弱引用计数的过程中，会将其成员变量pending_strong_ref或pending_weak_ref的值设置为1，而该service组件增加或减少强引用计数或者弱引用计数之后，binder实体对象就会将这两个成员变量设置为0。

#### binder_ref_death

> /drivers/staging/android/binder.c#binder_ref_death

- binder.c

  ```c
  struct binder_ref_death {
      // 前面提到binder_work
      // 取值：
      // BINDER_WORK_TRANSACTION = 1,
  	// BINDER_WORK_TRANSACTION_COMPLETE,
  	// BINDER_WORK_NODE,
  	// BINDER_WORK_DEAD_BINDER,
  	// BINDER_WORK_DEAD_BINDER_AND_CLEAR,
  	// BINDER_WORK_CLEAR_DEATH_NOTIFICATION,
      // 标志一个具体的死亡通知类型
  	struct binder_work work;
  	void __user *cookie; // 保存负责接收死亡通知的对象地址
  };
  ```

  结构体binder_ref_death用来描述一个service组件的死亡通知，如果service组件意外崩溃，client进程能够在它所引用的service组件死亡时获得通知，以便做出处理。因此client进程需要将一个用来接收死亡通知的对象地址注册到binder驱动程序中。

  binder驱动程序决定要向一个client进程发送一个service组件死亡通知时，会将一个binder_ref_death结构体封装成一个工作项，并且根据实际情况来设置该结构体的成员变量work的值，最后将这个工作项添加到client进程的todo队列等待执行。

  - 以下两种情况，binder驱动会向一个client进程发送一个service组件的死亡通知
    1. 当binder驱动程序检测到一个service组件死亡时，它会找到该service组件对应的binder实体对象，然后通过binder实体对象的变量refs找到所有引用它的client进程，最后找到这些进程所注册的死亡接收通知，即一个binder_ref_death结构体，这时binder驱动会将该binder_ref_death结构体添加到client进程的todo队列去等待处理。这种情况下，binder驱动将死亡通知设置为BINDER_WORK_DEAD_BINDER
    2. 档client进程向binder程序注册一个死亡通知时，如果它所引用的service组件已经死亡，那么binder驱动程序会马上给client进程发送一个service死亡通知，这种情况下，binder驱动将死亡通知类型设置为BINDER_WORK_DEAD_BINDER

  - 当一个client进程向binder驱动注销一个死亡通知时，binder驱动程序会向该client进程todo队列发送一个类型为binder_ref_death的工作项，用来表示注销结果，分两种情况：

    1. 如果client进程在注销一个死亡通知时，相应的service组件还没有死亡，那么binder驱动会找到之前注册的binder_ref_death结构体，并将它的work修改为BINDER_WORK_CLIENT_DEATH_NOTIFICATION，然后再将该binder_ref_death结构体封装成一个工作项添加到该client进程的todo队列中等待处理。
    2. 如果client进程正在注销一个死亡接受通知时，相应的service组件已经死亡，那么binder驱动就会找到之前注册的binder_ref_death结构体，并将它的类型work修改为BINDER_WORK_CLEAR_DEATH_NOTIFICATION，然后再将该binder_ref_death结构体封装成一个工作项，添加到client进程的todo队列等待处理。

    client进程在处理这个工作项时，通过对应的binder_ref_death结构体成员变量work就可以判断出注销结果。

#### binder_ref

> /drivers/staging/android/binder.c#binder_ref

- binder.c

  ```c
  struct binder_ref {
  	/* Lookups needed: */
  	/*   node + proc => ref (transaction) */
  	/*   desc + proc => ref (transaction, inc/dec ref) */
  	/*   node => refs + procs (proc exit) */
  	int debug_id;
  	struct rb_node rb_node_desc; // 宿主进程红黑树1的节点
  	struct rb_node rb_node_node; // 宿主进程红黑树2的节点
  	struct hlist_node node_entry; // binder_node中hlist_head的节点
  	struct binder_proc *proc; // -> Binder引用对象的宿主进程
  	struct binder_node *node; // binder引用对象所引用的binder实体对象
  	uint32_t desc; // 句柄值
  	int strong; // 强引用计数，维护binder引用对象的生命周期
  	int weak; // 弱引用计数，维护binder引用对象的生命周期
  	struct binder_ref_death *death; // -> 注册后存放死亡接收通知
   };
  ```

  结构体binder_ref用来描述一个Binder引用对象。每一个Client组件在Binder驱动程序中都对应有一个Binder引用对象，用来描述它在内核的状态。Binder驱动程序通过强引用计数和弱引用计数来维护它们的生命周期。

  - 成员变量desc

    一个句柄值（handle），或者称为描述符，它是用来描述一个Binder引用对象的。

    在Client进程的用户空间中，一个Binder引用对象是使用一个句柄值来描述的，因此，当client进程的用户空间通过binder驱动程序来访问一个Service组件时，它只需要指定一个句柄值，Binder驱动程序就可以通过该句柄值找到对应的Binder引用对象，然后再根据该Binder引用对象的成员变量node找到对应的Binder实体对象，最后可以通过该Binder实体对象（宿主进程binder_proc *proc）找到要访问的service组件。

    一个Binder引用对象的句柄值在进程范围内是唯一的，因此在两个不同的进程中，同一个句柄值代表的是两个不同的目标Service组件。

  - 成员变量proc / rb_node_desc / rb_node_node

    指向一个Binder引用对象的宿主进程，一个宿主进程使用两个红黑树来保存它内部所有的Binder引用对象。

    1. 节点rb_node_desc：**key : 句柄值，value : binder引用对象**
    2. 节点rb_node_node：**key : binder实体对象地址，value : 对应的binder引用对象**


#### binder_buffer

> /drivers/staging/android/binder.c#binder_buffer

- binder.c

  ```c
  struct binder_buffer {
      // binder驱动程序为进程分配内核缓冲区列表，entry是列表的一个节点
  	struct list_head entry;
      /**
       * binder驱动进程用两棵红黑树表示正在使用的内核缓冲区以及空闲的内和缓冲区
       * 如果一个内核缓冲区是空闲的，即它的成员变量free的值等于1，
       * 且成员变量rb_node就是空闲缓冲区红黑树的一个节点，
       * 否则rb_node是正在使用内核缓冲区红黑树的一个节点
       */
  	struct rb_node rb_node;
  	unsigned free:1;
  	unsigned allow_user_free:1;
  	unsigned async_transaction:1; // 是否是异步任务
  	unsigned debug_id:29;
      
      // transaction和target_node内核缓冲区正在交给哪一个事务以及哪一个binder实体对象使用
      // 每一个事务都关联一个目标Binder实体对象
      // binder驱动程序将事务保存在一个内核缓冲区中，然后交给binder实体对象处理，
      // 然后目标实体对象再将该内存缓冲区中的内容交给相应的service组件处理，service组件处理完该事务之后，
      // 如果发现传递给它的内核缓冲区的成员变量allow_user_free的值为1，则service组件请求binder驱动释放该内核缓冲区
  	struct binder_transaction *transaction;
  	struct binder_node *target_node;
      
  	size_t data_size;
  	size_t offsets_size;
      // data：一块大小可变的数据缓冲区，保存真正的通信数据
  	uint8_t data[0];
  };
  ```

  结构体binder_buffer用来描述一个内核缓冲区，它是用来在进程间传递数据的，每一个使用binder进程通信机制的进程在binder驱动程序中都有一个内核缓冲区列表，用来表示binder驱动程序为它所分配的内核缓冲区，而成员变量entry正好是这个内核缓冲区列表的一个节点。

  - 数据缓冲区data保存的数据分为两类
    1. 普通数据
    2. binder对象

#### binder_proc

> /drivers/staging/android/binder.c#binder_proc

- binder.c

  ```c
  struct binder_proc {
      struct hlist_node proc_node; // 全局hash列表中的节点
      // 每个进行binder通信的进程，都有一个binder线程池，由binder驱动维护，threads是红黑树根节点
      // 它以线程id作为关键字组织一个进程的binder线程池。
      // 进程可以调用ioctl将线程注册到binder驱动中，同时，当进程没有足够多的空闲线程时，binder驱动也可以主动
      // 要求进程注册更多的线程到binder线程池中，最大数量为max_threads
      struct rb_root threads;
      struct rb_root nodes;
      struct rb_root refs_by_desc;
      struct rb_root refs_by_node;
      int pid; // 进程组id
      struct vm_area_struct *vma; // 用户空间地址
      struct task_struct *tsk; // 任务控制块
      struct files_struct *files; // 打开文件结构体数
      struct hlist_node deferred_work_node;
      int deferred_work;
      void *buffer; // 内核空间地址
      ptrdiff_t user_buffer_offset; // 用户空间与内核空间地址相差的一个固定的值
      struct list_head buffers;
      struct rb_root free_buffers; // 空闲小块内核缓冲区红黑树
      struct rb_root allocated_buffers; // 正在使用的小块内核缓冲区红黑树
      size_t free_async_space; // 当前可以用来保存异步事务数据的内核缓冲区大小
      // **pages 固定的物理页面，成员变量pages是类型为struct page*的一个数组，数组中的每一个元素都指向一个物理页
      struct page **pages; 
      size_t buffer_size; // 内存缓冲区大小
      uint32_t buffer_free;
      // 当进程接收到一个进程间通信请求时，binder驱动程序将请求封装成一个工作项，
      // 加入到进程待处理工作项队列中，即todo
      struct list_head todo;
      wait_queue_head_t wait; // binder线程池空闲线程队列
      struct binder_stats stats;
      struct list_head delivered_death;
      int max_threads;
      int requested_threads;
      int requested_threads_started;
      int ready_threads; // 当前进程空闲binder线程数
      long default_priority;
      struct dentry *debugfs_entry;
  };
  ```

  结构体binder_proc用来描述一个正在使用binder进程间通信的进程。

  当一个进程调用函数open来打开设备文件/dev/binder时，binder驱动就会为它创建一个binder_proc结构体，并且保存在一个全局的hash列表中，而成员变量proc_node就是hash列表的一个节点

  进程打开设备文件/dev/binder之后，还必须调用函数mmap将它映射到进程的地址空间来，实际上是请求binder驱动程序为它分配一块内核缓冲区，以便做进程间传递数据。binder驱动程序为进程分配的内和缓冲区大小保存在成员变量buffer_size中

- 运行中binder的通信进程，维护三棵红黑树

  1. nodes

     key：binder实体对象的成员变量ptr，value：binder实体对象

  2. refs_by_desc

     key：引用对象成员变量desc（句柄），value：引用对象

  3. refs_by_node

     key：应用对象成员变量node，value：引用对象

  当进程不再使用binder进程间通信时，调用close关闭设备/dev/binder，这时候binder驱动就会释放之前为它分配的资源，例如释放结构体binder_proc、binder实体对象结构体binder_node以及binder_ref等。

#### binder_thread

> /drivers/staging/android/binder.c#binder_thread

- binder.c

  ```c
  struct binder_thread {
      struct binder_proc *proc; // -> 宿主进程
      struct rb_node rb_node; // binder_proc#threads的一个节点
      int pid; // 线程id
      int looper; // 线程状态
      struct binder_transaction *transaction_stack;
      struct list_head todo;
      uint32_t return_error; /* Write failed, return error code in read buf */
      uint32_t return_error2; /* Write failed, return error code in read */
          /* buffer. Used when sending a reply to a dead process that */
          /* we are also waiting on */
      wait_queue_head_t wait;
      struct binder_stats stats;
  };
  ```

  结构体binder_thread用来描述binder线程池中的一个线程

  一个线程注册到binder驱动时，binder驱动会为它创建一个binder_thread结构体。

#### binder_transaction

> drivers/staging/android/binder.c#binder_transaction

- binder.c

  ```c
  struct binder_transaction {
      int debug_id;
      struct binder_work work;
      struct binder_thread *from; // 发起事务的线程
      struct binder_transaction *from_parent;
      struct binder_proc *to_proc; // 负责处理事务的进程
      struct binder_thread *to_thread; // 负责处理事务的线程
      struct binder_transaction *to_parent;
      unsigned need_reply:1; // 区分事务是同步还是异步，同步事件需要对方答复，异步不需要
      /* unsigned is_dead:1; */	/* not used at the moment */
      struct binder_buffer *buffer; // -> binder驱动为该事务分配的一块内核缓冲区，其中保存进程间通信数据
      unsigned int	code;
      unsigned int	flags;
      long	priority; // 源线程优先级
      long	saved_priority;
      uid_t	sender_euid; // 用户id
  };
  ```

  结构体binder_transaction描述进程间通信过程，这个过程又称为一个事务。

  当binder驱动为目标进程或目标线程创建一个事务时，就会将该事务的成员变量work设置为BINDER_WORK_TRANSACTION，并且将它添加到目标进程或者目标线程todo队列中等待处理。

  以上介绍的都是在binder驱动内部使用的结构体

  -----

### Binder进程间通信库(Framework)

> frameworks/base/include/binder/Binder.h
>
> Binder库中，Service组件和Client组件分别使用模板类BnInterface和BpInterface来描述，其中，前者称为Binder本地对象，后者称为Binder代理对象。Binder库中的Binder本地对象和Binder代理对象分别对应与Binder驱动程序中的Binder实体对象和Binder引用对象。

- IInterface.h

  ```c++
  template<typename INTERFACE>
  class BnInterface : public INTERFACE, public BBinder { // 本地对象
  public:
      virtual sp<IInterface>      queryLocalInterface(const String16& _descriptor);
      virtual const String16&     getInterfaceDescriptor() const;
  protected:
      virtual IBinder*            onAsBinder();
  };
  
  template<typename INTERFACE>
  class BpInterface : public INTERFACE, public BpRefBase { // 代理对象
  public:
                                  BpInterface(const sp<IBinder>& remote);
  protected:
      virtual IBinder*            onAsBinder();
  };
  ```

  开发service组件和client组件时，除了要定义service组件接口之外，还需要实现一个binder本地类对象和一个代理对象

- BBinder

  ```cpp
  class BBinder : public IBinder
  {
  public:
  	...
      virtual status_t    transact(   uint32_t code,
                                      const Parcel& data,
                                      Parcel* reply,
                                      uint32_t flags = 0);
     	...
  	virtual status_t    onTransact( uint32_t code,
     	        	                  	const Parcel& data,
            	 	                    Parcel* reply,
            	                        uint32_t flags = 0);
  };
  ```

  BBinder为binder本地对象提供了抽象的进程通信接口

  - transcat

    当一个Binder代理对象通过Binder驱动程序向一个Binder本地对象发出一个进程间通信的时候，Binder驱动程序会调用该Binder本地对象的成员函数transact来处理该请求。

  - onTransact

    onTransact是由BBinder的子类，即Binder本地对象来实现的，它负责分发与业务相关的进程间通信请求。实际上，与业务相关的进程间通信请求是由Binder本地对象的子类，即Service组件来实现的。

- BpRefBase

  BpInterface继承自BpRefBase，BpRefBase为代理对象提供了抽象的进程间通信接口

  ```cpp
  class BpRefBase : public virtual RefBase {
  protected:
                              BpRefBase(const sp<IBinder>& o);
      virtual                 ~BpRefBase();
      virtual void            onFirstRef();
      virtual void            onLastStrongRef(const void* id);
      virtual bool            onIncStrongAttempted(uint32_t flags, const void* id);
      inline  IBinder*        remote()                { return mRemote; }
      inline  IBinder*        remote() const          { return mRemote; }
  private:
                              BpRefBase(const BpRefBase& o);
      BpRefBase&              operator=(const BpRefBase& o);
      IBinder* const          mRemote; // -> BpBinder对象
      RefBase::weakref_type*  mRefs;
      volatile int32_t        mState;
  };
  ```

- BpBinder

  ```cpp
  class BpBinder : public IBinder {
  public:
                          BpBinder(int32_t handle);
      inline  int32_t     handle() const { return mHandle; }
      // 向运行在server进程中的service组件发送进程间通信请求
      // 把handle、通信数据发送给binder驱动程序
      // binder通过驱动程序找到对应的binder引用对象
      // 再找到binder实体对象，再找到service组件实体，最后将数据发送给service组件
      virtual status_t    transact(   uint32_t code,
                                      const Parcel& data,
                                      Parcel* reply,
                                      uint32_t flags = 0);
      ...
      const   int32_t             mHandle; // client组件的句柄值，与binder驱动程序的binder引用对象建立对应关系
  }; // namespace android
  ```

- IPCThreadState.h

  ```cpp
  class IPCThreadState
  {
  public:
      static  IPCThreadState*     self();
  	...
              status_t            transact(int32_t handle,
                                           uint32_t code, const Parcel& data,
                                           Parcel* reply, uint32_t flags);
  	...
              status_t            talkWithDriver(bool doReceive=true);
  	...
      const   sp<ProcessState>    mProcess; // -> ProcessState
  }; // namespace android
  ```

  每一个binder线程，内部都有一个IPCThreadState对象，调用它的transact和binder驱动交互，transact内部又调用了talkWithDriver实现，它一方面负责向binder驱动程序发送数据，又接收来自binder驱动程序的进程间通信请求。

- ProcessState.h

  ```cpp
  class IPCThreadState;
  
  class ProcessState : public virtual RefBase
  {
  public:
      // open打开设备/dev/binder
      // 接着通过mmap映射到进程的地址空间，即请求binder驱动为进程分配内核缓冲区
      static  sp<ProcessState>    self();
      ...
  private:
      friend class IPCThreadState;
      ...
              int                 mDriverFD;
              void*               mVMStart; // -> 内核缓冲区的用户地址
  }
  ```

  ProcessState负责初始化Binder设备，即打开设备文件/dev/binder，以及将/dev/binder映射到进程的地址空间，该对象在进程范围内唯一。
