## volatile

- 例1

  ```java
  public class Main {
      boolean running = true;
      public static void main(String[] args) {
          Main main = new Main();
          new Thread(main::m).start();
          try {
              TimeUnit.SECONDS.sleep(1);
          } catch (Exception e) {
              e.printStackTrace();
          }
          main.running = true;
      }
  	
      void m() {
          System.out.println("m start");
          while (running) {
  //            System.out.println("hello");
          }
          System.out.println("m end");
      }
  }
  ```

  运行结果：

  m start

  程序没有结束

  原因：running变量，在主存，值为true。线程去读running，为true，读到线程本地。所以对线程来说，running一直是true，即使主存的running已经被改成false。

- 例2

  ```java
  volatile boolean running = true;
  ```

  将running改为volatile，其他代码不变，运行结果：

  m start
  m end

  程序结束。

  volatile的作用：主线程对running的操作，其他线程马上感知。

- 例3

  ```java
  public class Main {
      boolean running = true;
      public static void main(String[] args) {
          Main main = new Main();
          new Thread(main::m).start();
          try {
              TimeUnit.SECONDS.sleep(1);
          } catch (Exception e) {
              e.printStackTrace();
          }
          main.running = true;
      }
  	
      void m() {
          System.out.println("m start");
          while (running) {
          		System.out.println("hello"); // 比例1增加一条输出语句
          }
          System.out.println("m end");
      }
  }
  ```

  输出结果：

  m start

  hello

  ....

  hello

  m end

  解析：running没有加volatile，程序还是结束了。

  因为：System.out.println("hello")这个语句会将内存与本地缓存做同步

### dcl单例要不要加volatile

- 创建对象的过程

  ```java
  class T {
  	int m = 8;
  }
  T t = new T();
  ```

- 对应的字节码

  ```
  new #2 <T>  // 1 申请一块内存空间，用来装对象，此时m的值是0（默认值），引用类型是null
  dup
  invokespecial #3 <T.<init>> // 2 调用构造方法，m变成8
  astore_1 // 3 t指向对象
  return 
  ```

- double check lock

  ```java
  // 需要加入volatile
  private static volatile Main sInstance;
  
  		public static Main getInstance() {
          if (sInstance == null) {
              synchronized (Main.class) {
                	// 因为指令重排序的原因，有可能这个指向非null，但对象还没new出
                  if (sInstance == null) {
                      try {
                          Thread.sleep(1);
                      } catch (Exception e) {
                          e.printStackTrace();
                      }
                      sInstance = new Main();
                  }
              }
          }
          return sInstance;
      }
  ```

- volatile怎么阻止乱序执行？

  