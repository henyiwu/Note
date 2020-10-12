[toc]

## 关于覆盖equals()

- 覆盖equals()的时机

> 如果类有自己“逻辑相等”的判断，我们认为两个“逻辑相等”的类是相等的，他们相互调用equals()应该返回true。例如自定义Person类，两个Person实例的身份证号码是相等的，我们认为这两个Person实例相等，他们是同一个人。

- 区别于对象相等

> equals()用于判断对象的“逻辑相等”
>
> == 用于表示两个对象的地址相等（物理相等）

### 覆盖equals()时必须遵守的通用规定

#### 1. 自反性

> 定义：对于任何非null的引用值x，x.equals(x)，必须返回true。基本上不会违背这条规定，如果违背了，list.add(x)，list.contains(x)返回false
>
> ```java
> x.equals(x) // true
> ```

#### 2. 对称性

> 对于任何非null的引用值x和y，e.equals(y)返回true，y.equals(x)也返回true
>
> ````java
> x.equals(y)
> y.equals(x)
> // 结果应相同
> ````

- 反例

  ```java
  final class CaseInsensitiveString {
      private final String s;
      public CaseInsensitiveString(String s) {
          this.s = Objects.requireNonNull(s);
      }
  
      @Override
      public boolean equals(Object o) {
          if (o instanceof CaseInsensitiveString) {
              return s.equalsIgnoreCase(((CaseInsensitiveString) o).s);
          }
          //cis.equals(s)返回true的原因
          if (o instanceof String) {
              return s.equalsIgnoreCase((String) o);
          }
          return false;
      }
  }
  
  val cis = CaseInsensitiveString("Polish")
  val s = "polish"
  println("result : " + s.equals(cis))//false
  println("result : " + cis.equals(s))//true
  ```

- String.equals(Object anObject)

  ```java
      public boolean equals(Object anObject) {
          if (this == anObject) {
              return true;
          }
          //s.equals(cis)在这里cis判断不是String的实例，故返回false
          if (anObject instanceof String) {
              String anotherString = (String)anObject;
              int n = length();
              if (n == anotherString.length()) {
                  int i = 0;
                  while (n-- != 0) {
                      if (charAt(i) != anotherString.charAt(i))
                              return false;
                      i++;
                  }
                  return true;
              }
          }
          return false;
      }
  ```

  运行结果违反对称性，原因在代码注释中。

  为了解决这个问题，把对String的判断去掉。

  ```java
  // 原
  @Override
      public boolean equals(Object o) {
          if (o instanceof CaseInsensitiveString) {
              return s.equalsIgnoreCase(((CaseInsensitiveString) o).s);
          }
          //cis.equals(s)返回true的原因
          if (o instanceof String) {
              return s.equalsIgnoreCase((String) o);
          }
          return false;
      }
  
  // 现
  @Override
      public boolean equals(Object o) {
          return o instanceof CaseInsensitiveString && 
                  ((CaseInsensitiveString) o).s.equalsIgnoreCase(s);
      }
  ```

