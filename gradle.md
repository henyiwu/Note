[toc]

## Gradle

### 1. Gradle入门

#### 1.2 gradle版helloWorld

- build.gradle

```groovy
// 这个构建脚本定义了一个（任务）Task，任务名hello
// doLast意味着在Task执行完毕之后，要回调doLast里的闭包
task hello {
    doLast {
       	// groovy中，单引号和双引号都代表字符串
        println 'hello world'
    }
}
```

执行：gradle hello

Configure project :
hello world

build.gradle是Gradle默认的构建脚本文件，执行Gradle命令时，会默认加载当前目录下的build.gradle文件

#### 1.3 Gradle Wrapper

> Wrapper顾名思义，是对Gradle的一层包装，便于在团队开发时统一Gradle构建的版本。

- 生成wrapper

  ```groovy
  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle wrapper
  
  BUILD SUCCESSFUL in 2s
  1 actionable task: 1 executed
  ```

- 目录结构

  wangzhiping@wangzhiping-PC:~/GradleProject/gradle$ tree
  .
  └── wrapper
      ├── gradle-wrapper.jar
      └── gradle-wrapper.properties

#### 1.4 Gradle 日志

| 级别      | 用途     |
| --------- | -------- |
| error     | 错误信息 |
| quiet     | 重要信息 |
| warning   | 警告信息 |
| lifecycle | 进度信息 |
| info      | 信息     |
| debug     | 调试信息 |

| 开关选项        | 输出的日志级别                          |
| --------------- | --------------------------------------- |
| 无选项          | lifecycle及其更高级别                   |
| -q 或者 --quiet | quiet及其更高级别                       |
| -i 或者 --info  | info及其更高级别                        |
| -d 或者 --debug | debug及其更高级别，这一般会输出所有日志 |

##### 1.4.2 输出错误信息

默认情况下，堆栈信息的输出是关闭的，需要通过命令打开。

| 命令行选项                | 用于                 |
| ------------------------- | -------------------- |
| 无选项                    | 没有堆栈输出         |
| -s 或者--stacktrace       | 输出关键性的堆栈信息 |
| -S 或者 --full-stacktrace | 输出全部堆栈信息     |

一般推荐使用-s，-S过于冗余

#### 1.5 Gradle 命令行

- 1.5.1 使用帮助

  gradle -h

- 1.5.2 查看所有可执行的tasks

  gradle tasks

- 1.5.5 多任务执行

  gradle clean hello

  多个task用空格隔开

- 1.5.6 通过任务名缩写执行

  例如名为helloWorld的task可以执行为：

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle hW

  > Task :helloWorld
  > hello world

### 2. Groovy基础

> Groovy是基于jvm的一种动态语言，完全兼容java，在此基础上又增加了很多动态类型和灵活的特性，比如支持闭包，支持dsl，可以说是一门非常灵活的脚本语言。

#### 2.1 字符串

在groovy中，单引号双引号都可以定义一个字符串常量（java里单引号定义一个字符），不同的是单引号标记的是纯粹的字符串常量，而不是对字符串里的表达式做运算，但是双引号可以

- build.gradle

  ```groovy
  task helloWorld {
      def str1 = 'str1'
      def str2 = "str2"
      println "单引号定义的字符类型：" + str1.getClass().name
      println "双引号定义的字符类型：" + str2.getClass().name
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle hW

  > Configure project :
  > 单引号定义的字符类型：java.lang.String
  > 双引号定义的字符类型：java.lang.String

  BUILD SUCCESSFUL in 438ms
  <-------------> 0% WAITING

- build.gradle

  ```groovy
  task helloWorld {
      def str1 = 'str1'
      def str2 = "str2"
      println '单引号定义的字符类型:${str1}'
      println "双引号定义的字符类型:${str2}"
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle hW

  > Configure project :
  > 单引号定义的字符类型:${str1}
  > 双引号定义的字符类型:str2

  可见，在单引号中不能对字符串里的表达式做运算

#### 2.2 集合

- 2.1 List

  ```groovy
  task helloWorld {
      def numList = [1,2,3,4,5]
      println numList.getClass().name
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle hW

  > Configure project :
  > java.util.ArrayList

  可见，numList是一个ArrayList

- 2.2 打印list

  ```groovy
  task helloWorld {
      def numList = [1,2,3,4,5]
      println numList.get(0) // 访问第一个元素
      println numList[0] // 访问第一个元素
      println numList[-2] // 访问倒数第二个元素
      println numList[1..3] // 访问第2-4个元素
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle hW

  > Configure project :
  > 1
  > 1
  > 4
  > [2, 3, 4]

- 制造异常

  ```groovy
  task helloWorld {
      def numList = [1,2,3,4,5]
      println numList[-20] // 抛出异常
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle -stacktrace  hW

  Caused by: java.lang.ArrayIndexOutOfBoundsException: Negative array index [-20] too large for array size 5
          at build_atjutgku0sx4akor6tqhpkg7l$_run_closure1.doCall(/home/wangzhiping/GradleProject/build.gradle:5)
          at org.gradle.util.internal.ClosureBackedAction.execute(ClosureBackedAction.java:72)
          at org.gradle.util.internal.ConfigureUtil.configureTarget(ConfigureUtil.java:155)
          at org.gradle.util.internal.ConfigureUtil.configureSelf(ConfigureUtil.java:131)
          at org.gradle.api.internal.AbstractTask.configure(AbstractTask.java:669)
          at org.gradle.api.DefaultTask.configure(DefaultTask.java:309)
          at org.gradle.api.internal.project.DefaultProject.task(DefaultProject.java:1275)
          at org.gradle.internal.metaobject.BeanDynamicObject$MetaClassAdapter.invokeMethod(BeanDynamicObject.java:484)
          at org.gradle.internal.metaobject.BeanDynamicObject.tryInvokeMethod(BeanDynamicObject.java:196)
          at org.gradle.internal.metaobject.CompositeDynamicObject.tryInvokeMethod(CompositeDynamicObject.java:98)
          at org.gradle.internal.extensibility.MixInClosurePropertiesAsMethodsDynamicObject.tryInvokeMethod(MixInClosurePropertiesAsMethodsDynamicObject.java:34)
          at org.gradle.groovy.scripts.BasicScript$ScriptDynamicObject.tryInvokeMethod(BasicScript.java:135)
          at org.gradle.internal.metaobject.AbstractDynamicObject.invokeMethod(AbstractDynamicObject.java:163)
          at org.gradle.groovy.scripts.BasicScript.invokeMethod(BasicScript.java:84)
          at build_atjutgku0sx4akor6tqhpkg7l.run(/home/wangzhiping/GradleProject/build.gradle:1)
          at org.gradle.groovy.scripts.internal.DefaultScriptRunnerFactory$ScriptRunnerImpl.run(DefaultScriptRunnerFactory.java:91)
          ... 152 more

  增加-stacktrace打印堆栈信息，可以看到是index溢出异常

- 遍历list

  ```groovy
  task helloWorld {
      def numList = [1,2,3,4,5]
      numList.each {
          println it
      }
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle -stacktrace  hW

  > Configure project :
  > 1
  > 2
  > 3
  > 4
  > 5

- 2.2.2 map

  ```groovy
  task helloWorld {
      def map = ['width':1024, 'height':768]
      println map.getClass().name
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle -stacktrace  hW

  > Configure project :
  > java.util.LinkedHashMap

- 取map的key或者value

  ```groovy
  task helloWorld {
      def map = ['width':1024, 'height':768]
      println map.getClass().name
  
      println map['width']
      println map.height
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle -stacktrace  hW

  > Configure project :
  > java.util.LinkedHashMap
  > 1024
  > 768

- 遍历map

  ```groovy
  task helloWorld {
      def map = ['width':1024, 'height':768]
  
      map.each{
          println it.key
          println it.value
      }
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle -stacktrace  hW

  > Configure project :
  > width
  > 1024
  > height
  > 768

#### 2.3 方法

- 2.3.1 括号是可以省略的

  ```groovy
  task helloWorld {
      method1(1,2)
      method1 1,2
  }
  
  def method1(int a, int b) {
      println a+b
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle hW

  > Configure project :
  > 3
  > 3

- 2.3.2 return是可以不写的

  ```groovy
  task helloWorld {
      println method1(1,2)
  }
  
  static def method1(int a, int b) {
      if (a > b) {
          a
      } else {
          b
      }
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle hW

  > Configure project :
  > 2

  groovy取最后一行作为返回值，可以不写return

- 代码块可以作为参数传递

  类似于kotlin的闭包优化规则

  ```groovy
  task helloWorld {
      def numList1 = [1,2,3,4,5]
      numList1.each({println it})
  
      // 只有一个参数时，可以把闭包提出来
      def numList2 = [1,2,3,4,5]
      numList2.each(){println it}
  
      // 括号内没有参数时，可以把括号去掉
      def numList3 = [1,2,3,4,5]
      numList3.each{
          println it
      }
  }
  ```

#### 2.4  javaBea

- demo

  ```groovy
  task helloWorld {
      Person p = new Person()
  
      println "名字是: ${p.name}"
      p.name = "张三"
      println "名字是: ${p.name}"
  }
  
  class Person {
      private String name
  }

​		wangzhiping@wangzhiping-PC:~/GradleProject$ gradle hW

> Configure project :
> 名字是: null
> 名字是: 张三

#### 2.5 闭包

> 闭包是groovy一个非常重要的特性，可以说是dsl的基础。

- 2.5.1 初识闭包

  ```groovy
  task helloWorld {
      customEach {
          println it
      }
  }
  
  // 参数名closure是自定义的，可以随便起
  def customEach(closure) {
      for (int i in 1..10) {
          closure(i)
      }
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle hW

  > Configure project :
  > 1
  > 2
  > 3
  > 4
  > 5
  > 6
  > 7
  > 8
  > 9
  > 10

- 2.5.2 向闭包传递参数

  ```groovy
  task helloWorld {
      customEach { k,v ->
          println "key:" + k + ",value:" + v
      }
  }
  
  def customEach(closure) {
      def map = ["name":"张三", "age":18]
      map.each {
          closure(it.key, it.value)
      }
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle hW

  > Configure project :
  > key:name,value:张三
  > key:age,value:18

- 2.5.3闭包委托

  ```groovy
  task helloWorld {
      new Delegate().test {
          println "thisObject: ${thisObject.getClass()}"
          println "owner: ${owner.getClass()}"
          println "delegate: ${delegate.getClass()}"
          method1()
          it.method1()
      }
  }
  
  def method1() {
      println "context this : ${this.getClass()} in root"
      println "method1 in root"
  }
  
  class Delegate {
      def method1() {
          println "context this : ${this.getClass()} in delegate"
          println "method1 in delegate"
      }
  
      def test(Closure<Delegate> closure) {
          closure(this)
      }
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle -stacktrace  hW

  > Configure project :
  > thisObject: class build_atjutgku0sx4akor6tqhpkg7l
  > owner: class build_atjutgku0sx4akor6tqhpkg7l$_run_closure1
  > delegate: class build_atjutgku0sx4akor6tqhpkg7l$_run_closure1
  > context this : class build_atjutgku0sx4akor6tqhpkg7l in root
  > method1 in root
  > context this : class Delegate in delegate
  > method1 in delegate

  thisObject的优先级最高，默认情况下使用thisObject来处理闭包中调用的方法，如果有则执行。thisObject其实就是这个构建脚本的上下文，它和脚本中的this对象是相等的。从例子中也证明了delegate和owner是相等的（owner: class build_atjutgku0sx4akor6tqhpkg7l$_run_closure1
  delegate: class build_atjutgku0sx4akor6tqhpkg7l$_run_closure1）。它们两个的优先级是：owner > delegate高，所以闭包内的方法处理顺序是thisObject > owner > delegate

- 在dsl中，我们一般指定delegate为当前的it，这样就可以在闭包内对该it进行配置，或者调用其方法：

  ```groovy
  task helloWorld {
      person {
          // 可以操作person对象属性，访问person对象的方法
          personName = "张三"
          personAge = 20
          dumpPerson()
      }
  }
  
  class Person {
      String personName
      int personAge
  
      def dumpPerson() {
          println "name is ${personName}, age is ${personAge}"
      }
  }
  
  def person(Closure<Person> closure) {
      Person p = new Person()
      closure.delegate = p
      closure.setResolveStrategy(Closure.DELEGATE_FIRST)
      closure(p)
  }
  ```

  拿一段安卓项目的build.gradle文件对比

- build.gradle

  ```groovy
  defaultConfig {
      applicationId "com.example.myapplication"
      minSdk 21
      targetSdk 32
      versionCode 1
      versionName "1.0"
  
      testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
  }
  
  // demo
  person {
      personName = "张三"
      personAge = 20
      dumpPerson()
  }
  ```

#### 2.6 dsl

> dsl(Domain Specific Language)，专门关注某一领域的语言，对比java这种通用全面的语言。
>
> gradle就是一门dsl，它基于groovy，专门解决自动化构建的dsl。

### 3. Gradle 构建脚本基础

#### 3.1 Setting文件

> 在Gradle中，定义了一个设置文件，用于初始化以及工程树的配置，设置文件的默认名字是setting.gradle，放在根目录工程下。
>
> 设置文件大多数的作用是为了配置子工程，在gradle中多工程是通过工程树表示的，相当于在android studio中看到的project和module一样，根工程相当于android studio中的project，一个根工程可以有很多子工程，也就是很多的module。
>
> 一个子工程只有在setting里设置了gradle才会去识别，才会在构建的时候被包含进去。

- setting.gradle

  ```groovy
  rootProject.name = "My Application"
  include ':app'
  ```

  对应于目录结构：

  MyApplication/
  ├── app

#### 3.2 Build文件

> 每个project都会有一个Build文件，该文件是该project构建的入口，可以在这里针对project进行配置，比如配置版本，需要哪些插件，依赖哪些库等。
>
> 既然每个project都会有一个build文件，那么root project也不例外。root project可以获得所有child project，所以可以在root project的build文件里对child project统一配置，比如应用的插件，依赖的maven中心库等。

- build.gradle

  ```groovy
  subprojects {
      repositories {
          jcenter()
      }
  }
  ```

  对所有子project配置maven仓库，制定为jcenter

#### 3.3 projects及tasks

> 多个project组成整个gradle的构建，一个project又包含多个task，task是一个原子操作，比如打个jar包，复制一份文件，编译一次java代码。

#### 3.4创建一个任务

> task其实是Project对象的一个函数，原型为create(String name, Closure configureClosure)
> 参数1：任务的名字，可以自定义
> 参数2：一个闭包，也就是花括号内的代码块

- 写法1

  ```groovy
  task helloWorld {
      doFirst {
          println 'customTask:doFirst'
      }
      doLast {
          println 'customTask:doLast'
      }
  }
  ```

- 写法2

  ```groovy
  tasks.create("customTask") {
      doFirst {
          println 'customTask:doFirst'
      }
      doLast {
          println 'customTask:doLast'
      }
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle customTask

  > Task :customTask
  > customTask:doFirst
  > customTask:doLast

  两种写法的效果是一样的

#### 3.5 任务依赖

> 使用dependsOn:可以指定多个依赖任务作为参数，dependsOn是Task类的一个方法。
>
> task之间是有依赖关系的，这样我们就可以控制哪些任务优先于哪些任务执行。比如执行jar任务之前，compile任务一定要先执行过，android的install任务一定要依赖package任务打包生成apk。

- ex35Hello

  ```groovy
  task ex35Hello {
      println 'hello'
  }
  
  task ex35Main(dependsOn: ex35Hello) {
      doLast {
          println 'Main'
      }
  }
  ```

  通过dependsOn:指定依赖的任务ex35Hello，运行结果：

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle ex35Main

  > Configure project :
  > hello

  > Task :ex35Main
  > Main

- 指定多个依赖task

  ```groovy
  task ex35Hello {
      println "hello"
  }
  
  task ex35World {
      println "World"
  }
  
  task ex35MultiTask {
      dependsOn ex35Hello,ex35World
      doLast {
          println "multiTask"
      }
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle ex35MultiTask

  > Configure project :
  > hello
  > World

  > Task :ex35MultiTask
  > multiTask

#### 3.6任务间通过API控制、交互

> 创建一个任务和定义一个变量是一样的，变量名就是任务名，类型是Task，所以我们可以通过任务名，使用Task的API访问它的方法、属性或者重新配置等。

- ex35Hello

  ```groovy
  task ex35Hello {
      println "hello"
  }
  
  ex35Hello.doFirst {
      println "doFirst"
  }
  
  ex35Hello.doLast {
      println "doLast"
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle ex35Hello

  > Configure project :
  > hello

  > Task :ex35Hello
  > doFirst
  > doLast

- 判断是否有ex35Hello这个变量

  ```groovy
  task ex35Hello {
      println "hello"
  }
  
  ex35Hello.doFirst {
      println "doFirst"
  }
  
  ex35Hello.doLast {
      println "has property ${project.hasProperty('ex35Hello')}"
      println "doLast"
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle ex35Hello

  > Configure project :
  > hello

  > Task :ex35Hello
  > doFirst
  > has property true
  > doLast

  project.hasProperty('ex35Hello')运行结果是true，说明每个task都是project的一个属性

#### 3.7自定义属性

> project和task都允许用户添加额外的自定义属性，要添加额外的属性，通过应用所属对应的ext属性即可实现。添加之后可以通过ext属性对自定义属性读取和设置，如果要同时添加多个自定义属性，可以通过ext代码块。
>
> ext一般用来自定义版本号名称，把版本号和版本名单独放在一个gradle文件中，便于管理。

- ex37CustomProperty

  ```groovy
  ext.age = 18
  
  ext {
      phone = 122222
      address = "xxxddd"
  }
  
  task ex37CustomProperty {
      println "年龄是 ${age}"
      println "电话是 ${phone}"
      println "地址是 ${address}"
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle ex37CustomProperty

  > Configure project :
  > 年龄是 18
  > 电话是 122222
  > 地址是 xxxddd

#### 3.8脚本即代码，代码也是脚本

> 虽然我们在gradle文件中写脚本，但是我们写的都是代码，这一点要记住，这样才能时刻使用groovy，java以及gradle的任何语法和api帮你完成想做的事情。是脚本吗？是，但并不是简单的脚本，这脚本上可以定义class、内部类、导入包、定义方法等。

- 给打包的apk定义生成的文件名

  ```groovy
  android {
      android.applicationVariants.all { variant ->
          variant.outputs.all {
              outputFileName = "my_${buildTime()}.apk"
          }
      }
  }
  
  def buildTime() {
      def date = new Date()
      def formattedDate = date.format('yyyyMMdd')
      return formattedDate
  }