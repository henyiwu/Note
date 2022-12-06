## ClassLoader

- 自定义classloader

- main

  ```java
      public static void main(String[] args) {
          try {
              MyClassLoader myClassLoader = new MyClassLoader(
                      "/Users/wangzhiping/designpattern/src/", "random");
              Class c = myClassLoader.findClass("Hello");
              System.out.println("ClassLoader:" + c.getClassLoader());
              Object instance = c.newInstance();
          } catch (Exception e) {
              e.printStackTrace();
          }
      }
  ```

- MyClassLoader

  ```java
  public class MyClassLoader extends ClassLoader {
      private String path;
      private String classLoaderName;
  
      public MyClassLoader(String path, String classLoaderName) {
          this.path = path;
          this.classLoaderName = classLoaderName;
      }
  
      @Override
      protected Class<?> findClass(String name) throws ClassNotFoundException {
          try {
  
              byte[] b = loadClassData(name);
              return defineClass(name, b, 0, b.length);
          } catch (IOException e) {
              e.printStackTrace();
          }
          return null;
      }
  
      private byte[] loadClassData(String name) throws IOException {
          name = path + name + ".class";
          InputStream is = null;
          ByteArrayOutputStream outputStream = null;
          try {
              is = new FileInputStream(new File(name));
              outputStream = new ByteArrayOutputStream();
              int i = 0;
              while ((i = is.read()) != -1) {
                  outputStream.write(i);
              }
          } catch (Exception e) {
              e.printStackTrace();
          } finally {
              if (outputStream != null) {
                  outputStream.close();
              }
              if (is != null) {
                  is.close();
              }
          }
  
          return outputStream.toByteArray();
      }
    
      @Override
      protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
          System.out.println("loadClass name : " + name);
          return super.loadClass(name, resolve);
      }
  }
  ```

- Hello.java

  ```java
  //编译
  //javac src/Hello.java
  // 生成Hello.class，由自定义classLoader加载
  public class Hello {
      static {
          System.out.println("hello");
      }
  }
  ```