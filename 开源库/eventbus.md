[toc]

## EventBus

> 基于EventBus 3.0 

### Register

- Eventbus#register()

  ```java
  private final SubscriberRegistry subscribers = new SubscriberRegistry(this);
  
  public void register(Object object) {
    subscribers.register(object);
  }
  ```

- SubscriberRegistery#register(Object listener)

  ```java
  void register(Object listener) {
    // 获得register的class所有带Subscribe.class注解的方法，封装成一个列表
    Multimap<Class<?>, Subscriber> listenerMethods = findAllSubscribers(listener);
  
    for (Entry<Class<?>, Collection<Subscriber>> entry : listenerMethods.asMap().entrySet()) {
      Class<?> eventType = entry.getKey();
      Collection<Subscriber> eventMethodsInListener = entry.getValue();
  
      CopyOnWriteArraySet<Subscriber> eventSubscribers = subscribers.get(eventType);
  
      if (eventSubscribers == null) {
        CopyOnWriteArraySet<Subscriber> newSet = new CopyOnWriteArraySet<>();
        eventSubscribers =
            MoreObjects.firstNonNull(subscribers.putIfAbsent(eventType, newSet), newSet);
      }
  
      eventSubscribers.addAll(eventMethodsInListener);
    }
  }
  ```