[toc]

## EventBus

> 基于EventBus 3.0

### Register

> register主要步骤：
>
> 1. 构建两个map，subscriptionsByEventType和typesBySubscriber，subscriptionsByEventType是以参数类型为key，subscriber为value的map，typesBySubscriber是以要注册类的对象为key，类中订阅事件的方法参数类型为集合为value的键值对。（EventType是被Subscribe修饰的函数唯一的入参的类名）
> 2. 目标类的条件：有subscribe注解、只有一个参数、public修饰、非static修饰

- Eventbus#register()

  ```java
  // key : eventType（订阅方法唯一的参数，是个类）
  // value ： 订阅该事件的方法集合
  private final Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType;
  // key : 类
  // value : 该订阅类的参数类型
  private final Map<Object, List<Class<?>>> typesBySubscriber;
  
  private final SubscriberRegistry subscribers = new SubscriberRegistry(this);
      public void register(Object subscriber) {
          Class<?> subscriberClass = subscriber.getClass();
          // 所有带注解的函数集合
          // 条件1.带Subscribe注解 2.public修饰 3.方法单参数
          List<SubscriberMethod> subscriberMethods = subscriberMethodFinder
            .findSubscriberMethods(subscriberClass);
          synchronized (this) {
              for (SubscriberMethod subscriberMethod : subscriberMethods) {
                  subscribe(subscriber, subscriberMethod);
              }
          }
      }
  ```

- SubscriberMethodFinder#findSubscriberMethods(Class<?> subscriberClass)

  ```java
      List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
          /**
           * METHOD_CACHE: 是一个ConcurrentHashMap，key是要注册类的字节码文件，value是这个字节码文件里的所有订阅方		  * 法信息的集合，集合的元素是SubscriberMethod，它实际上就是订阅方法的信息类，包含Method对象、线程模式、事件			 * 类型、优先级、是否是粘性事等。
           */
          List<SubscriberMethod> subscriberMethods = METHOD_CACHE.get(subscriberClass);
          if (subscriberMethods != null) {
              return subscriberMethods;
          }
  		/**
  		 * EventBus是支持EventBusBuilder的，如果我们自定义了EventBusBuilder，则ignoreGeneratedIndex为true， 			* 否则为false，我们没自定义，所有看false
  		 */
          if (ignoreGeneratedIndex) {
              subscriberMethods = findUsingReflection(subscriberClass);
          } else {
              subscriberMethods = findUsingInfo(subscriberClass);
          }
          // 如果这个类没有订阅方法，抛出异常
          if (subscriberMethods.isEmpty()) {
              throw new EventBusException("Subscriber " + subscriberClass
                      + " and its super classes have no public methods with the @Subscribe annotation");
          } else {
              // 将该注册类的类型为key，将这个类所有注册方法的封装集合为value存入map集合	
              METHOD_CACHE.put(subscriberClass, subscriberMethods);
              return subscriberMethods;
          }
      }
  ```

- SubscriberMethodFinder#findUsingInfo(Class<?> subscriberClass)

  ```java
  /**
   * 分析2：findUsingInfo()
   * 作用：如果findState缓存了，订阅方法信息，则使用findState里的缓存，否则调用findUsingReflectionInSingleClass方 	* 法，反射获取订阅方法信息。
   */
  private List<SubscriberMethod> findUsingInfo(Class<?> subscriberClass) {
    
      // FindState辅助我们查找订阅方法的类
      FindState findState = prepareFindState();
      findState.initForSubscriber(subscriberClass);
  
      // findState.clazz就是我们的注册类subscriberClass
      while (findState.clazz != null) {
  
          findState.subscriberInfo = getSubscriberInfo(findState);
  
          // 该类第一次注册时，findState.subscriberInfo为null, 我们走false
          if (findState.subscriberInfo != null) {
                  SubscriberMethod[] array = findState.subscriberInfo.getSubscriberMethods();
                  for (SubscriberMethod subscriberMethod : array) {
                      if (findState.checkAdd(subscriberMethod.method, subscriberMethod.eventType)) {
                          findState.subscriberMethods.add(subscriberMethod);
                      }
                  }
          } else {
              // ->> 分析3
              findUsingReflectionInSingleClass(findState);
          }
  
          // 修改findState.clazz为subscriberClass的父类Class，即需要遍历父类
          findState.moveToSuperclass();
      }
  
       // 将查找到的方法保存在了FindState实例的subscriberMethods集合中。然后使用subscriberMethods构建一个新的List<SubscriberMethod>并返回，最后释放掉findState
      return getMethodsAndRelease(findState);
  
      // ->> 返回到findSubscriberMethods() 方法中
  }
  ```

- SubscriberMethodFinder#findUsingReflectionSingleClass(FindState findState)

  ```java
  // 方法不能是abstract、static
  private static final int MODIFIERS_IGNORE = Modifier.ABSTRACT | Modifier.STATIC | BRIDGE | SYNTHETIC;
  
  private void findUsingReflectionInSingleClass(FindState findState) {
      Method[] methods;
      try {
          // This is faster than getMethods, especially when subscribers are fat classes like Activities
          methods = findState.clazz.getDeclaredMethods();
      } catch (Throwable th) {
          // Workaround for java.lang.NoClassDefFoundError, see https://github.com/greenrobot/EventBus/issues/149
          methods = findState.clazz.getMethods();
          findState.skipSuperClasses = true;
      }
      for (Method method : methods) {
          // 获取方法修饰符
          int modifiers = method.getModifiers();
          // 方法由public修饰
          if ((modifiers & Modifier.PUBLIC) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
              Class<?>[] parameterTypes = method.getParameterTypes();
              // 方法只有一个参数
              if (parameterTypes.length == 1) {
                  Subscribe subscribeAnnotation = method.getAnnotation(Subscribe.class);
                  // 如果该方法的Subscribe存在
                  if (subscribeAnnotation != null) {
                      // 获取该方法第一个参数类型，也就是订阅的事件类型，例如：boolean、class android.os.Bundle
                      Class<?> eventType = parameterTypes[0];
                     // checkAdd()方法用来判断FindState中是否已经添加过将该事件类型为key的键值对，没添加过则返回					   // true
                     if (findState.checkAdd(method, eventType)) {
                          ThreadMode threadMode = subscribeAnnotation.threadMode();
                          findState.subscriberMethods.add(
                              new SubscriberMethod(method, eventType, threadMode,
                          subscribeAnnotation.priority(), subscribeAnnotation.sticky()));
                     }
                  }
              } else if (strictMethodVerification && method.isAnnotationPresent(Subscribe.class)) {
                  String methodName = method.getDeclaringClass().getName() + "." + method.getName();
                  throw new EventBusException("@Subscribe method " + methodName +
                          "must have exactly 1 parameter but has " + parameterTypes.length);
              }
          } else if (strictMethodVerification && method.isAnnotationPresent(Subscribe.class)) {
              String methodName = method.getDeclaringClass().getName() + "." + method.getName();
              throw new EventBusException(methodName +
                      " is a illegal @Subscribe method: must be public, non-static, and non-abstract");
          }
      }
  }
  ```

#### Eventbus#subscribe

- Eventbus#subscribe(Object subscriber, SubscriberMethod subscriberMethod)

  ```java
  private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
      // 获取订阅方法的事件类型
      Class<?> eventType = subscriberMethod.eventType;
      // 将订阅方法的封装类、再进行封装
      Subscription newSubscription = new Subscription(subscriber, subscriberMethod);
      // subscriptionsByEventType是hashmap, 以事件类型为key, Subscription集合为value
      // 先查找subscriptionsByEventType是否存在以当前事件类型为key的值
      CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
      if (subscriptions == null) {
          subscriptions = new CopyOnWriteArrayList<>();
          // 填充subscriptionsByEventType，该数据结构可以根据事件类型，获取所有订阅方法信息的集合
          subscriptionsByEventType.put(eventType, subscriptions);
      } else {
          if (subscriptions.contains(newSubscription)) {
              throw new EventBusException("Subscriber " + subscriber.getClass() + " already registered to event "
                      + eventType);
          }
      }
  
      int size = subscriptions.size();
      for (int i = 0; i <= size; i++) {
          if (i == size || subscriberMethod.priority > subscriptions.get(i).subscriberMethod.priority) {
              subscriptions.add(i, newSubscription);
              break;
          }
      }
  
      // typesBySubscriber也是一个HashMap，保存了以当前要注册类的对象为key，注册类中订阅事件的方法的参数类型的集合为value的键值对
      // 和上面一样，根据key先判断，是否已经存储过了，如果已经存储过了，直接取出订注册类中订阅事件的方法的参数类型的集合
      List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
      if (subscribedEvents == null) {
          subscribedEvents = new ArrayList<>();
          // 填充typesBySubscriber，该数据结构可以根据注册类获得这个类上的所有订阅方法
          typesBySubscriber.put(subscriber, subscribedEvents);
      }
      subscribedEvents.add(eventType);
  
    	// 是否支持粘性事件
      if (subscriberMethod.sticky) {
          if (eventInheritance) {
              // Existing sticky events of all subclasses of eventType have to be considered.
              // Note: Iterating over all events may be inefficient with lots of sticky events,
              // thus data structure should be changed to allow a more efficient lookup
              // (e.g. an additional map storing sub classes of super classes: Class -> List<Class>).
              Set<Map.Entry<Class<?>, Object>> entries = stickyEvents.entrySet();
              for (Map.Entry<Class<?>, Object> entry : entries) {
                  Class<?> candidateEventType = entry.getKey();
                  if (eventType.isAssignableFrom(candidateEventType)) {
                      Object stickyEvent = entry.getValue();
                      checkPostStickyEventToSubscription(newSubscription, stickyEvent);
                  }
              }
          } else {
              Object stickyEvent = stickyEvents.get(eventType);
              checkPostStickyEventToSubscription(newSubscription, stickyEvent);
          }
      }
  }
  ```

### unregister

> 使用：EventBus.getDefault().unregister(this);

- unregister

  ```java
  public synchronized void unregister(Object subscriber) {
      // 通过类，取出该订阅类的eventType集合
      List<Class<?>> subscribedTypes = typesBySubscriber.get(subscriber);
      if (subscribedTypes != null) {
          for (Class<?> eventType : subscribedTypes) {
              unsubscribeByEventType(subscriber, eventType);
          }
          // hashmap.remove(key)
          // 删掉这个key
          typesBySubscriber.remove(subscriber);
      } else {
          Log.w(TAG, "Subscriber to unregister was not registered before: " + subscriber.getClass());
      }
  }
  ```

- unsubscribeByEventType(Object subscribe, Class<?> eventType)

  ```java
  private void unsubscribeByEventType(Object subscriber, Class<?> eventType) {
    	// 拿到这个eventType的所有订阅方法，并遍历
    	// 注意这里拿到的订阅方法有可能是其他类的，不一定是subscriber这个类里的
    	// 所以下方删除时需要判断这个订阅方法是不是当前类的
      List<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
      if (subscriptions != null) {
          int size = subscriptions.size();
          for (int i = 0; i < size; i++) {
            	// subscription中保存了订阅类的信息
              Subscription subscription = subscriptions.get(i);
            	// 判断订阅方法是不是要unregister的class里的，是的话删除
              if (subscription.subscriber == subscriber) {
                  subscription.active = false;
                  subscriptions.remove(i);
                  i--;
                  size--;
              }
          }
      }
  }
  ```

### post

- post

  ```java
  private final ThreadLocal<PostingThreadState> currentPostingThreadState = new ThreadLocal<PostingThreadState>() {
          @Override
          protected PostingThreadState initialValue() {
              return new PostingThreadState();
          }
      };
  
  public void post(Object event) {
    	// postingState是ThreadLocal
    	// 把发送的事件保存在postingState里
      PostingThreadState postingState = currentPostingThreadState.get();
      List<Object> eventQueue = postingState.eventQueue;
      eventQueue.add(event);
  
    	// 判断该事件是否正在发送中
      if (!postingState.isPosting) {
          postingState.isMainThread = Looper.getMainLooper() == Looper.myLooper();
          postingState.isPosting = true;
          if (postingState.canceled) {
              throw new EventBusException("Internal error. Abort state was not reset");
          }
          try {
            	// 遍历postingState，直到事件发送完成
              while (!eventQueue.isEmpty()) {
                  postSingleEvent(eventQueue.remove(0), postingState);
              }
          } finally {
              postingState.isPosting = false;
              postingState.isMainThread = false;
          }
      }
  }
  ```

- postSingleEvent(Object event, PostingThreadState postingState) throws Error 

  ```java
  private void postSingleEvent(Object event, PostingThreadState postingState) throws Error {
      Class<?> eventClass = event.getClass();
      boolean subscriptionFound = false;
    	// 判断event是否有弗雷
      if (eventInheritance) {
        	// lookupAllEventTypes() 拿到该事件的所有父类事件类型
          List<Class<?>> eventTypes = lookupAllEventTypes(eventClass);
          int countTypes = eventTypes.size();
        	// 遍历事件类型
          for (int h = 0; h < countTypes; h++) {
              Class<?> clazz = eventTypes.get(h);
              subscriptionFound |= postSingleEventForEventType(event, postingState, clazz);
          }
      } else {
          subscriptionFound = postSingleEventForEventType(event, postingState, eventClass);
      }
      if (!subscriptionFound) {
          if (logNoSubscriberMessages) {
              Log.d(TAG, "No subscribers registered for event " + eventClass);
          }
          if (sendNoSubscriberEvent && eventClass != NoSubscriberEvent.class &&
                  eventClass != SubscriberExceptionEvent.class) {
            	// 如果没有订阅事件，发送NoSubscriberEvent
              post(new NoSubscriberEvent(this, event));
          }
      }
  }
  ```

- postSingleEventForEventType(Object event, PostingThreadState postingState, Class<?> eventClass) 

  ```java
  private boolean postSingleEventForEventType(Object event, PostingThreadState postingState, Class<?> eventClass) {
      CopyOnWriteArrayList<Subscription> subscriptions;
      synchronized (this) {
        	// 根据eventType拿到订阅方法的集合
          subscriptions = subscriptionsByEventType.get(eventClass);
      }
      if (subscriptions != null && !subscriptions.isEmpty()) {
          for (Subscription subscription : subscriptions) {
              postingState.event = event;
              postingState.subscription = subscription;
              boolean aborted = false;
              try {
                	// 把事件发送给这个方法
                  postToSubscription(subscription, event, postingState.isMainThread);
                  aborted = postingState.canceled;
              } finally {
                  postingState.event = null;
                  postingState.subscription = null;
                  postingState.canceled = false;
              }
              if (aborted) {
                  break;
              }
          }
          return true;
      }
      return false;
  }
  ```

- postToSubscription(Subscription subscription, Object event, boolean isMainThread)

  ```java
  private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
      switch (subscription.subscriberMethod.threadMode) {
          // 在哪个线程发送事件，就在哪个线程接收事件
          case POSTING:
              invokeSubscriber(subscription, event);
              break;
          // 在主线程接收事件
          case MAIN:
          		// 如果当前是主线程，直接发送，否则通过主线程的handler把事件转到主线程发送
              if (isMainThread) {
                  invokeSubscriber(subscription, event);
              } else {
                  mainThreadPoster.enqueue(subscription, event);
              }
              break;
          // 在io线程接收事件
          case BACKGROUND:
          		// 如果当前是主线程，通过eventbus的线程池发送事件
              if (isMainThread) {
                  backgroundPoster.enqueue(subscription, event);
              } else {
                	// 如果当前不是主线程，直接发送事件 
                  invokeSubscriber(subscription, event);
              }
              break;
          // 无论在哪个线程，都用线程池执行
          case ASYNC:
              asyncPoster.enqueue(subscription, event);
              break;
          default:
              throw new IllegalStateException("Unknown thread mode: " + subscription.subscriberMethod.threadMode);
      }
  }
  ```

- invokeSubscriber(Subscription subscription, Object event) 

  ```java
  // 不论是ThreadMode BACKGROUND、MAIN、POSTING、ASYNC
  // 最终都通过这个方法调用
  void invokeSubscriber(Subscription subscription, Object event) {
      try {
        	// 反射调用方法
          subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
      } catch (InvocationTargetException e) {
          handleSubscriberException(subscription, event, e.getCause());
      } catch (IllegalAccessException e) {
          throw new IllegalStateException("Unexpected exception", e);
      }
  }
  ```

### Sticky

> 原理：调用postSticky时，把事件存储起来（map<类，eventType>），

- 使用

  ```java
  // 发布事件
  EventBus.getDefault().postSticky(new Object());
  
  // 订阅事件
  @Subscribe(sticky = true)
  public void testEventBus(Object obj){
  
      ...
  }
  ```

- postSticky

  ```java
  private final Map<Class<?>, Object> stickyEvents;
  
  public void postSticky(Object event) {
      synchronized (stickyEvents) {
        	// 把事件存储在stickyEvents
        	// 例:
        	// key : class com.gzik.pandora.logic.event.MineLiveEvent
        	// value : MineLiveEvent(infoChange=true)
          stickyEvents.put(event.getClass(), event);
      }
      // 普通的post，流程上面分析过
      post(event);
  }
  ```

- subscribe(Object subscriber, SubscriberMethod subscriberMethod)

  ```java
      private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
  			...
        // 如果事件支持粘性事件
        if (subscriberMethod.sticky) {
              if (eventInheritance) {
                	// 遍历上面提到的stickyEvents，取出粘性事件
                  Set<Map.Entry<Class<?>, Object>> entries = stickyEvents.entrySet();
                  for (Map.Entry<Class<?>, Object> entry : entries) {
                      Class<?> candidateEventType = entry.getKey();
                    	// 订阅方法的eventType和粘性事件的eventType匹配
                      if (eventType.isAssignableFrom(candidateEventType)) {
                          Object stickyEvent = entry.getValue();
                          checkPostStickyEventToSubscription(newSubscription, stickyEvent);
                      }
                  }
              } else {
                  Object stickyEvent = stickyEvents.get(eventType);
                  checkPostStickyEventToSubscription(newSubscription, stickyEvent);
              }
          }
      }
  ```
  
- checkPostStickyEventToSubscription

  ```java
  private void checkPostStickyEventToSubscription(Subscription newSubscription, Object stickyEvent) {
      if (stickyEvent != null) {
          postToSubscription(newSubscription, stickyEvent, Looper.getMainLooper() == Looper.myLooper());
      }
  }
  ```

- postToSubscription(Subscription subscription, Object event, boolean isMainThread)

  ```java
  private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
      switch (subscription.subscriberMethod.threadMode) {
          case POSTING:
              invokeSubscriber(subscription, event);
              break;
          case MAIN:
              if (isMainThread) {
                  invokeSubscriber(subscription, event);
              } else {
                  mainThreadPoster.enqueue(subscription, event);
              }
              break;
          case BACKGROUND:
              if (isMainThread) {
                  backgroundPoster.enqueue(subscription, event);
              } else {
                  invokeSubscriber(subscription, event);
              }
              break;
          case ASYNC:
              asyncPoster.enqueue(subscription, event);
              break;
          default:
              throw new IllegalStateException("Unknown thread mode: " + subscription.subscriberMethod.threadMode);
      }
  }
  ```

  