[toc]

## EventBus

> 基于EventBus 3.0

### Register

- Eventbus#register()

  ```java
  private final SubscriberRegistry subscribers = new SubscriberRegistry(this);
      public void register(Object subscriber) {
          Class<?> subscriberClass = subscriber.getClass();
          // 所有带注解的函数集合
          List<SubscriberMethod> subscriberMethods = subscriberMethodFinder
            .findSubscriberMethods(subscriberClass);
          synchronized (this) {
              for (SubscriberMethod subscriberMethod : subscriberMethods) {
                  subscribe(subscriber, subscriberMethod);
              }
          }
      }
  ```

- SubscriberRegistery#register(Object subscriber, SubscriberMethod subscriberMethod)

  ```java
      private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
          Class<?> eventType = subscriberMethod.eventType;
          Subscription newSubscription = new Subscription(subscriber, subscriberMethod);
          CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
          if (subscriptions == null) {
              subscriptions = new CopyOnWriteArrayList<>();
              // 填充subscriptionsByEventType，该数据结构可以根据时间类型，获取所有订阅方法信息的集合
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
  
          List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
          if (subscribedEvents == null) {
              subscribedEvents = new ArrayList<>();
              // 填充typesBySubscriber，该数据结构可以根据注册类获得这个类上的所有订阅方法
              typesBySubscriber.put(subscriber, subscribedEvents);
          }
          subscribedEvents.add(eventType);
  
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