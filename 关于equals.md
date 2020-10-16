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

#### 3. 传递性

> 对于任何非null的引用值x和y，当且仅当y.equals(x)返回值为true时，y.equals(x)范围值也为true

- 反例

  ```java
  public class MyClass {
      public static void main(String[] args) {
          ColorPoint p1 = new ColorPoint(1, 2, Color.RED);
          Point p2 = new Point(1, 2);
          ColorPoint p3 = new ColorPoint(1, 2, Color.BLUE);
          System.out.println(p1.equals(p2));//true
          System.out.println(p2.equals(p3));//true
          System.out.println(p1.equals(p3));//false
      }
  }
  
  class Point {
      private final int x;
      private final int y;
  
      public Point(int x, int y) {
          this.x = x;
          this.y = y;
      }
  
      @Override
      public boolean equals(Object o) {
          if (!(o instanceof Point)) {
              return false;
          }
          Point point = (Point) o;
          return x == point.x && y == point.y;
      }
  }
  
  class ColorPoint extends Point {
      private final Color color;
  
      public ColorPoint(int x, int y, Color color) {
          super(x, y);
          this.color = color;
      }
  
      @Override
      public boolean equals(Object o) {
          if (!(o instanceof Point)) {
              return false;
          }
          if (!(o instanceof ColorPoint)) {
              return o.equals(this);
          }
          return super.equals(o) && ((ColorPoint) o).color == this.color;
      }
  
      @Override
      public int hashCode() {
          return Objects.hash(color);
      }
  }
  ```

  前两种比较忽略了颜色，最后一种没有

- 改进1

  ```java
      @Override
      public boolean equals(Object o) {
          if (o == null || o.getClass() != getClass()) {
              return false;
          }
          Point p = (Point) o;
          return p.x == x && p.y == y;
      }
  ```

  采用getClass代替instanceof，保证两个类型相同时才可能相同，但新的问题是，ColorPoint继承于Point，也是一个Point，所以这种代替存在逻辑问题。

- 改进2

  ```java
  class ColorPoint {
      protected final Color color;
      protected final Point point;
  
      public ColorPoint(int x, int y, Color color) {
          this.point = new Point(x, y);
          this.color = color;
      }
  
      public Point asPoint() {
          return point;
      }
      
      @Override
      public boolean equals(Object o) {
          if (!(o instanceof ColorPoint)) {
              return false;
          }
          ColorPoint colorPoint = (ColorPoint) o;
          return colorPoint.point.equals(point) && colorPoint.color.equals(color);
      }
  }
  
  public static void main(String[] args) {
          ColorPoint p1 = new ColorPoint(1, 2, Color.BLUE);
          Point p2 = new Point(1, 2);
          System.out.println(p1.equals(p2));//比较两个点，包括颜色，false
          System.out.println(p1.asPoint().equals(p2));//只比较坐标，true
      }
  ```

  把ColorPoint改为聚合Point的方式，通过asPoint()方法转换成普通点，忽略颜色，这样既可以比较颜色又可以比较坐标，缺点是创建一个ColorPoint相当于创建了一个ColorPoint和一个Point。

  回顾一下，采用ColorPoint继承Point可能造成的问题：

  1. instanceff，(colorPoint instanceof Point)结果为true，有色点和无色点的比较忽略了颜色
  2. 采用getClass()的方式，断绝了Point和ColorPoint相等的可能性

#### 4. 一致性

> 如果两个对象相等，它们就必须始终保持相等，除非当中有一个（或者两个对象都）被修改了。

#### 5. 非空性

> 所有对象都不能等于null

- 例

  ```java
  o,equals(null)//返回true的情况比较少见，但我们见过抛出NullPointerException
  ```

- 例

  ```java
  @Override
      public boolean equals(Object o) {
          if (o == null) {
              return false;
          }
          return super.equals(o);
      }
  ```

  这种判断是没有必要存在的

- 例

  ```java
  @Override
      public boolean equals(Object o) {
          if (!(o instanceof MyClass)) {
              return false;
          }
          return super.equals(o);
      }
  ```

  如果漏掉这一步检查，并且传递给equals的方法是错误类型，会抛出ClassCastException。

  如果instanceof的第一个参数类型是null，结果一定返回false，所以不需要判定空。

### 提高equals方法的诀窍

1. 使用==操作符检查“参数是否为这个对象的引用”，即判断地址是否相同，是则直接返回true，如果equals操作比较复杂，则有必要增加这两行代码，提高性能。

2. 使用instanceof检查“参数是否为正确类型”。如果不是，返回false。

   正确的类型：指所在类的类型，也就是完全一样的Class。但某些情况下指的是某个接口，如果类实现了接口并且允许实现类之间采用接口的equals判断“逻辑相等”，那么就使用接口（参考Set、List、Map等）

3. 把参数转换成正确的类型，有instanceof检查则确保成功。

4. 检查该类中每一个（关键）"significant"变量，查看参数中的变量是否与该对象中对应的变量相匹配。

总而言之，不要轻易覆盖equals方法，除非迫不得已，通常情况直接用Object继承的方法即可，如果覆盖equals，一定要比较这个类的所有变量，并且查看是否符合文中提到的五个性质。

### 覆盖equals时必须覆盖hashCode

> 相等的对象必须具有相等的散列码

- 例

  ```java
  class Person {
      String name;
  
      public Person(String name) {
          this.name = name;
      }
  
      @Override
      public boolean equals(Object o) {
          if (this == o) return true;
          if (!(o instanceof Person)) return false;
          Person person = (Person) o;
          return this.name.equals(person.name);
      }
  }
  
  Person p1 = new Person("hello");
  Person p2 = new Person("world");
  Person p3 = new Person("hello");
  System.out.println(p1.equals(p2));//false
  System.out.println(p2.equals(p3));//flase
  System.out.println(p1.equals(p3));//true
  
  System.out.println(p1.hashCode());//705927765
  System.out.println(p2.hashCode());//366712642
  System.out.println(p3.hashCode());//1829164700
  ```

  Person类没有重写hashCode，p1与p3对象相等，散列码却不相同，不符合规定。

- 没有重写hashCode的危害

  ```java
      public static void main(String[] args) {
          Person p1 = new Person("hello");
          Person p2 = new Person("hello");
          Map map = new HashMap();
          map.put(p1, "hello");
          System.out.println(map.get(p2));//null
      }
  ```

  p1与p2是相等的对象，但以p1为键把"hello"放入Map中，再以p2为键读取，结果为null，因为p1与p2的散列码不同。

- 重写hashCode

  ```java
  @Override
      public int hashCode() {
          return Objects.hash(name);
      }
  
  public static int hash(Object... var0) {
          return Arrays.hashCode(var0);
      }
  ```

  Objects中的hash方法允许我们传入多个变量，获得哈希码，不过由于参数数量可变，内部会创建数组，速度相对更慢。

- 重写hashCode

  ```java
  @Override
      public int hashCode() {
          int result = name.hashCode();
          result = 31 * result + Integer.valueOf(age).hashCode();
          return result;
      }
  ```

  使用31是因为 31 * i == (i << 5) - i，现代虚拟机可以自动完成优化，提高运算速度。



