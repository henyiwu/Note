[toc]

## Java中的引用类型：强软弱虚

### 强引用

即使内存不足，JVM也不会回收强引用，而是抛出OOM，如果对象不需要使用了，可以置为null

```
Object o = new Object()

普通的赋值，是强引用
只有"="这个引用不存在了，"new Object()"才能成为"垃圾"，GC才会把它回收
```

```java
public class M {

    public static void main(String[] args) throws IOException {
        M m = new M();
        m = null;
        System.gc();

        // 阻塞main线程，给垃圾回收线程时间执行
        System.in.read();
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println("finalize");
    }
}

结果：
finalize
    
一个对象即将被回收时，会调用finalize方法
```

### 软引用

内存不足时，可以回收软引用对象，软应用不会被gc

```*java
SoftReference<byte[]> softReference = new SoftReference<>(new byte[1024*1024*10]);

m -> SR -> byte[1024*1024*10]
m强引用SR，SR软应用字节数组
```

- 软应用非常适合做缓存

  例如图片缓存，空间不足的时候，会回收不需要的图片

### 弱引用

> 会被gc回收的引用，可防止内存泄漏

### 虚引用

> 虚引用唯一的作用，管理直接内存：
>
> 虚拟机运行于操作系统中，如果需要访问网络资源，可以直接使用操作系统的资源，故用虚应用指向外存，如果虚引用被回收，外存对象入队，JVM可感知并清理外存

- 例

  ```java
      public static void main(String[] args) {
          ReferenceQueue<JavaTest> queue = new ReferenceQueue<JavaTest>();
          PhantomReference<JavaTest> reference = new PhantomReference<>(new JavaTest(), queue);
          ArrayList<byte[]> list = new ArrayList<>();
  
          new Thread(() -> {
              while (true) {
                  list.add(new byte[1024 * 1024]);
                  try {
                      Thread.sleep(1000);
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                      Thread.currentThread().interrupt();
                  }
                  System.out.println(reference.get());
              }
          }).start();
  
          new Thread(new Runnable() {
              @Override
              public void run() {
                  Reference<? extends JavaTest> poll = queue.poll();
                  if (poll != null) {
                      System.out.println("----虚引用对象被回收了---" + poll);
                  }
              }
          }).start();
      }
  
  ```

  虚应用被回收时，会进入QUEUE中