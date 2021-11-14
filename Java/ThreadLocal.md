## ThreadLocal

- 这句话的背后发生了什么？

  ```java
  threadLocal.set(new Person())
  ```

- 源码

  ```java
  public void set(T value) {
          Thread t = Thread.currentThread();
          ThreadLocalMap map = getMap(t);
          if (map != null)
              map.set(this, value);
          else
              createMap(t, value);
      }
  
  ThreadLocalMap getMap(Thread t) {
          return t.threadLocals;
      }
  ```

  1. 拿到当前线程中的一个Map
  2. 以threadLocal为key，Person对象为value，存入map

- 常见误区

  threadLocal.set(new Person())

  这句话容易理解成，以线程为key，往threadLocal放入Person对象，这是错误的

- Map是什么

  ```java
  static class ThreadLocalMap {
          static class Entry extends WeakReference<ThreadLocal<?>> {
              /** The value associated with this ThreadLocal. */
              Object value;
  
              Entry(ThreadLocal<?> k, Object v) {
                  // new出一个WeakReference，并指向k
                  super(k);
                  value = v;
              }
          }
      ...
  ```

  Map是每个线程独有的一个数据结构，存储的是Entry，key是threadLocal

- 为什么Entry要用弱引用？

  如果Entry是强引用，只要Thread不退出，线程会一直持有ThreadLocalMap，ThreadLocalMap中Entry会强应用ThreadLocal对象，即使threadLocal = null，也不能被回收。

  ![](https://pics3.baidu.com/feed/242dd42a2834349b79fe653d508c16c837d3be7e.jpeg?token=4b047410f2c7c94baf88bc42b6f6bca8&s=4B62B85293E44D0B0AC11F6E03009074)

### ThreadLocal Q&A

1. Java会应用在什么地方

   Android：通过ThreadLocal为每个线程存放一个Looper对象

2. ThreadLocal会产生内存泄漏你了解吗？

   ThreadLocal存放在ThreadLocalMap（静态变量，生命周期同线程），ThreadLocalMap持有Entry，Entry如果强引用ThreadLocal，即使引用ThreadLocal对象的变量置空，ThreadLocal也会被ThreadLocalMap间接持有，导致泄露