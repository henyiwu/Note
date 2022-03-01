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
  ```

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

#### 3.4 创建一个任务

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

#### 3.6 任务间通过API控制、交互

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

#### 3.7 自定义属性

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

#### 3.8 脚本即代码，代码也是脚本

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
  ```

### 4. Gradle任务

#### 4.1 Gradle多种方式创建任务

1. 直接以任务名字创建

   ```groovy
   def Task helloWorld = task(helloWorld)
   
   helloWorld.doLast {
       println "helloooo"
   }
   ```

   该方法完整的定义是：Task task(String name) throws InvalidUserDataExceptions

2. 任务+一个对该任务配置的map对象来创建

   ```groovy
   def Task helloWorld = task(helloWorld, group:BasePlugin.BUILD_GROUP)
   
   helloWorld.doLast {
       println "helloooo"
       println "任务分组${helloWorld.group}"
   }
   ```

   该函数的原型是：Task task(Map<String, ?> args, String name) throws InvalidUserDataException

   map可配置的参数如下：

   | 配置项      | 描述                                   | 默认值      |
   | ----------- | -------------------------------------- | ----------- |
   | type        | 基于一个存在的task来创建，和继承差不多 | DefaultTask |
   | overwrite   | 是否替换存在的task，和type配合使用     | false       |
   | dependsOn   | 用于配制任务的依赖                     | []          |
   | action      | 添加到任务中的一个action或者一个闭包   | null        |
   | description | 用于配制任务的描述                     | null        |
   | group       | 用于配制任务的分组                     | null        |

3. 任务名+闭包

   ```groovy
   task ex41CreateTask {
       description '演示'
       doLast {
           println "创建方法的原型为 : Task task(String name, Closure configureClosure)"
           println "任务描述, ${description}"
       }
   }
   ```

- task原型

  ```java
   public interface Project extends Comparable<Project>, ExtensionAware, PluginAware {
   
  	Task task(String var1) throws InvalidUserDataException;
  
      Task task(Map<String, ?> var1, String var2) throws InvalidUserDataException;
  
      Task task(Map<String, ?> var1, String var2, Closure var3);
  
      Task task(String var1, Closure var2);
  
      Task task(String var1, Action<? super Task> var2);
  	...    
  }
  ```

#### 4.2 多种方式访问任务

1. 首先，我们创建的任务都会作为项目的一个属性，属性名就是任务名，所以可以直接通过任务名访问和操纵该任务

   ```groovy
   task ex41CreateTask
   
   ex41CreateTask.doLast {
       println "hello world"
   }
   ```

2. 其次，任务都是通过taskContainer创建的，其实taskContainer就是我们创建任务的集合，在project中，可以通过tasks属性访问taskContainer，所以我们可以以访问集合的方式创建我们的任务

   ```groovy
   task ex41CreateTask
   
   tasks['ex41CreateTask'].doLast {
       println "ex41CreateTask doLast"
   }
   ```

   Task :app:ex41CreateTask
   ex41CreateTask doLast

   这里的[]指的不是map，而是a.getAt(b)，对应的例子tasks['ex41CreateTask']就是调用了tasks.getAt('ex41CreateTask')

3. 通过路径访问

   通过路径访问有两种方式

   1. get路径访问

      > get的时候如果找不到该任务，会抛出UnknownTaskException异常

      ```groovy
      task ex41CreateTask
      
      tasks['ex41CreateTask'].doLast {
          println tasks.getByPath(':app:ex41CreateTask')
      }
      ```

      > Task :app:ex41CreateTask
      > task ':app:ex41CreateTask'

   2. find路径访问

      >find的时候如果找不到任务，返回null

      ```groovy
      task ex41CreateTask
      
      tasks['ex41CreateTask'].doLast {
          println tasks.findByPath('ex41CreateTask')
      }
      ```

      > Task :app:ex41CreateTask
      > task ':app:ex41CreateTask'

      通过路径访问时，参数值可以是任务路径也可以是任务的名字。

      通过名字访问时，参数值只能是名字不能是路径。

#### 4.3 任务分组和描述

> 任务的分组就是对任务的分类，便于我们对任务进行归类整理。
>
> 任务的描述就是说明这个任务的作用。

- 添加分组和描述

  ```groovy
  def Task myTask = task ex43GroupTask
  myTask.group = BasePlugin.BUILD_GROUP
  myTask.description = '这是一个构建的引导任务'
  
  myTask.doLast {
      println "group ${group}, descrption:${description}"
  }
  ```

  ./gradlew tasks

  Build tasks

  assemble - Assemble main outputs for all the variants.
  assembleAndroidTest - Assembles all the Test applications.
  build - Assembles and tests this project.
  buildDependents - Assembles and tests this project and all projects that depend on it.
  buildNeeded - Assembles and tests this project and all projects it depends on.
  bundle - Assemble bundles for all the variants.
  clean - Deletes the build directory.
  cleanBuildCache - Deletes the build cache directory.
  compileDebugAndroidTestSources
  compileDebugSources
  compileDebugUnitTestSources
  compileReleaseSources
  compileReleaseUnitTestSources
  ex43GroupTask - 这是一个构建的引导任务

#### 4.5 任务的执行分析

> 当我们执行tasks的时候，其实就是执行其拥有的actions列表，这个列表保存在task对象实例中actions成员变量中，其类型是一个list
>
> private List<ContextAwareTaskAction> actions = new ArrayList<ContextAwareTaskAction>();

- 现在我们把task之前执行、task本身执行以及task之后执行分别称为doFirst、doSelf以及doLast，举个例子

  ```groovy
  // 创建了task，所以task里的actions有了元素
  def Task myTask = task ex45CustomTask(type : CustomTask)
  
  // 把doFirst这个action放在actions开头
  myTask.doFirst {
      println "task执行之前执行do first"
  }
  
  // 把doLast这个action放在actions末尾
  myTask.doLast {
      println "task执行之后执行do last"
  }
  
  class CustomTask extends DefaultTask {
      @TaskAction
      def doSelf() {
          println "task 自己本身在执行in doSelf"
      }
  }
  ```

  > Task :app:ex45CustomTask
  > task执行之前执行do first
  > task 自己本身在执行in doSelf
  > task执行之后执行do last

- AbstractTask

  ````java
  public abstract class AbstractTask implements TaskInternal, DynamicObjectAware {
    // 执行task时，就是执行task中该actions集合
    private List<InputChangesAwareTaskAction> actions;	 
    
    public Task doFirst(final String actionName, final Action<? super Task> action) {
          this.hasCustomActions = true;
          if (action == null) {
              throw new InvalidUserDataException("Action must not be null!");
          } else {
              this.taskMutator.mutate("Task.doFirst(Action)", new Runnable() {
                  public void run() {
                      // 在list最前面添加
                      AbstractTask.this.getTaskActions().add(0, AbstractTask.this.wrap(action, actionName));
                  }
              });
              return this;
          }
      }
    
        public Task doLast(final String actionName, final Action<? super Task> action) {
          this.hasCustomActions = true;
          if (action == null) {
              throw new InvalidUserDataException("Action must not be null!");
          } else {
              this.taskMutator.mutate("Task.doLast(Action)", new Runnable() {
                  public void run() {
                      // 在list最后面添加
                      AbstractTask.this.getTaskActions().add(AbstractTask.this.wrap(action, actionName));
                  }
              });
              return this;
          }
      }
  }
  ````
  
  当我们使用Task方法创建ex45CustomTask这个任务时，Gradle会解析所有其带有TaskAction标注的方法作为其Task执行的Action，然后通过Task的prependParallelSafeAction方法把该Action添加到actions List里。
  
  ```groovy
      public void prependParallelSafeAction(Action<? super Task> action) {
          if (action == null) {
              throw new InvalidUserDataException("Action must not be null!");
          } else {
              this.getTaskActions().add(0, this.wrap(action));
          }
      }
  ```

#### 4.6 任务排序

- mustRunAfter

  ```groovy
  task order1 {
      println "order1"
  
      doFirst {
          println "order1 doFirst"
      }
  }
  
  task order2 {
      println "order2"
  
      doFirst {
          println "order2 doFirst"
      }
  }
  
  order1.mustRunAfter order2
  ```

  > > Configure project :
  > > order1
  > > order2
  >
  > > Task :order2  // order2先执行
  > > order2 doFirst
  >
  > > Task :order1
  > > order1 doFirst // order1后执行
  >
  > BUILD SUCCESSFUL in 393ms
  > 2 actionable tasks: 2 executed

- 去掉order1.mustRunAfter order2

  ```groovy
  task order1 {
      println "order1"
  
      doFirst {
          println "order1 doFirst"
      }
  }
  
  task order2 {
      println "order2"
  
      doFirst {
          println "order2 doFirst"
      }
  }
  ```

  > Configure project :
  > order1
  > order2

  > Task :order1
  > order1 doFirst

  > Task :order2
  > order2 doFirst

  order1比order2先执行

  或者使用shouldRunAfter，但有可能还是按原顺序执行

- shouldRunAfter

  ```groovy
  task order1 {
      println "order1"
  
      doFirst {
          println "order1 doFirst"
      }
  }
  
  task order2 {
      println "order2"
  
      doFirst {
          println "order2 doFirst"
      }
  }
  
  order1.shouldRunAfter order2
  ```

  > Configure project :
  > order1
  > order2

  > Task :order2
  > order2 doFirst

  > Task :order1
  > order1 doFirst

#### 4.7 任务的启用和禁用

- task.enabled

  ```groovy
  order1.enabled = false
  ```

  开启enabled = false，没有执行task1

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle order1

  > Configure project :
  > order1
  > order2

  关闭enabled，成功执行了task1

  BUILD SUCCESSFUL in 394ms
  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle order1

  > Configure project :
  > order1
  > order2

  > Task :order1
  > order1 doFirst

#### 4.8 任务的OnlyIf断言

> 断言就是一个条件表达式，Task有一个oflyIf方法，它接受一个闭包作为参数，如果该闭包返回true则该任务执行，否则跳过。

- 渠道打包举例

  ```groovy
  final String BUILD_APPS_ALL = "all"
  final String BUILD_APPS_SHOUFA = "shoufa"
  final String BUILD_APPS_EXCLUDE_SHOUFA = "exclude_shoufa"
  
  task ex48QQRelease {
      println "打应用宝的包"
      doFirst {
          println "打应用宝的包 doFirst"
      }
  }
  
  task ex48BaiduRelease {
      println "打百度的包"
      doFirst {
          println "打百度的包 doFirst"
      }
  }
  
  task ex48HuaweiRelease {
      println "打华为的包"
      doFirst {
          println "打华为的包 doFirst"
      }
  }
  
  task ex48MiuiRelease {
      println "打小米的包"
      doFirst {
          println "打小米的包 doFirst"
      }
  }
  
  task build {
      group BasePlugin.BUILD_GROUP
      description "打渠道包"
      doFirst {
          println "打渠道包 doFirst"
      }
  }
  
  build.dependsOn ex48BaiduRelease,ex48HuaweiRelease,ex48MiuiRelease,ex48QQRelease
  
  ex48BaiduRelease.onlyIf {
      def execute = false
      if (project.hasProperty("build_apps")) {
          Object buildApps = project.property("build_apps")
          if (BUILD_APPS_SHOUFA == buildApps || BUILD_APPS_ALL == buildApps) {
              execute = true
          } else {
              execute = false
          }
      } else {
          execute = true
      }
      execute
  }
  
  ex48QQRelease.onlyIf {
      def execute = false
      if (project.hasProperty("build_apps")) {
          Object buildApps = project.property("build_apps")
          if (BUILD_APPS_SHOUFA == buildApps || BUILD_APPS_ALL == buildApps) {
              execute = true
          } else {
              execute = false
          }
      } else {
          execute = true
      }
      execute
  }
  
  ex48HuaweiRelease.onlyIf {
      def execute = false
      if (project.hasProperty("build_apps")) {
          Object buildApps = project.property("build_apps")
          if (BUILD_APPS_EXCLUDE_SHOUFA == buildApps || BUILD_APPS_ALL == buildApps) {
              execute = true
          } else {
              execute = false
          }
      } else {
          execute = true
      }
      execute
  }
  
  ex48MiuiRelease.onlyIf {
      def execute = false
      if (project.hasProperty("build_apps")) {
          Object buildApps = project.property("build_apps")
          if (BUILD_APPS_EXCLUDE_SHOUFA == buildApps || BUILD_APPS_ALL == buildApps) {
              execute = true
          } else {
              execute = false
          }
      } else {
          execute = true
      }
      execute
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle -Pbuild_apps=all build

  > Configure project :
  > 打应用宝的包
  > 打百度的包
  > 打华为的包
  > 打小米的包

  > Task :ex48BaiduRelease
  > 打百度的包 doFirst

  > Task :ex48HuaweiRelease
  > 打华为的包 doFirst

  > Task :ex48MiuiRelease
  > 打小米的包 doFirst

  > Task :ex48QQRelease
  > 打应用宝的包 doFirst

  > Task :build
  > 打渠道包 doFirst

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle -Pbuild_apps=shoufa build

  > Configure project :
  > 打应用宝的包
  > 打百度的包
  > 打华为的包
  > 打小米的包

  > Task :ex48BaiduRelease
  > 打百度的包 doFirst

  > Task :ex48QQRelease
  > 打应用宝的包 doFirst

  > Task :build
  > 打渠道包 doFirst

  打包命令为gradle -Pbuild_apps=shoufa build时，比gradle -Pbuild_apps=all build，少执行了小米和华为两个打包任务，应为它们的onlyIf表达式返回false

#### 4.9任务规则

> 我们创建的任务都在TaskContainer里，由其进行管理。所以当我们访问任务的时候都是通过TaskContainer进行访问，二TaskContainer又是一个NamedDomainObjectCollection，所以说我们的任务规则是NamedDomainObjectCollection的规则。
>
> NamedDomainObjectCollection是一个具有唯一名字的域对象的集合，它里面所有的元素都有一个唯一不变的名字，该名字是String类型，所以我们可以通过名字获取该元素，比如我们通过任务名获取该任务
>
> 我们提供的任务名在NamedDomainObjectCollection中可能并不存在，这时候就会调用我们添加的规则来处理这种异常情况。如理：	

- addRule

  ```groovy
  tasks.addRule("对该规则的一个描述,便于调试") { String taskName ->
      println "addRule 开始执行"
      task(taskName) {
          println "该${taskName}不存在，请查证后再执行"
      }
  }
  
  task ex49RuleTask {
      println "ex49RuleTask 开始执行"
      dependsOn missTask
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle ex49RuleTask

  > Configure project :
  > ex49RuleTask 开始执行
  > addRule 开始执行
  > 该missTask不存在，请查证后再执行

- 如果不在addRule中创建task，则程序崩溃

  ```groovy
  tasks.addRule("对该规则的一个描述,便于调试") { String taskName ->
      println "addRule 开始执行"
  }
  
  task ex49RuleTask {
      println "ex49RuleTask 开始执行"
      dependsOn missTask
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle ex49RuleTask

  > Configure project :
  > ex49RuleTask 开始执行
  > addRule 开始执行

  FAILURE: Build failed with an exception.

  * Where:
  Build file '/home/wangzhiping/GradleProject/build.gradle' line: 7

  * What went wrong:
  A problem occurred evaluating root project 'GradleProject'.
  > Could not get unknown property 'missTask' for task ':ex49RuleTask' of type org.gradle.api.DefaultTask.

  * Try:
  > Run with --stacktrace option to get the stack trace.
  > Run with --info or --debug option to get more log output.
  > Run with --scan to get full insights.

  * Get more help at https://help.gradle.org

  BUILD FAILED in 401ms

- 如果依赖的task存在，addRule回调不会执行

  ```groovy
  tasks.addRule("对该规则的一个描述,便于调试") { String taskName ->
      println "addRule 开始执行"
  }
  
  task missTask {
      println "missTask 开始执行"
  }
  
  task ex49RuleTask {
      println "ex49RuleTask 开始执行"
      dependsOn missTask
  }
  ```

  wangzhiping@wangzhiping-PC:~/GradleProject$ gradle ex49RuleTask

  > Configure project :
  > missTask 开始执行
  > ex49RuleTask 开始执行

- 函数原型

  ````java
  public class DefaultNamedDomainObjectCollection<T> extends DefaultDomainObjectCollection<T> implements NamedDomainObjectCollection<T>, MethodMixIn, PropertyMixIn {
  
      public Rule addRule(Rule rule) {
          this.rules.add(rule);
          return rule;
      }
      ...
  }
  ````

- findByName

  ```java
  public T findByName(String name) {
      T value = this.findByNameWithoutRules(name);
      if (value != null) {
          return value;
      } else {
          ProviderInternal<? extends T> provider = this.index.getPending(name);
          if (provider != null) {
              provider.getOrNull();
              return this.index.get(name);
          } else {
              // 要执行的task不存在时，执行applyRules()
              return !this.applyRules(name) ? null : this.findByNameWithoutRules(name);
          }
      }
  }
  ```

  从findByName中可以看出，如果依赖的任务存在，findByName会直接返回，不存在会执行rules

### 5. Gradle插件

> Gradle本身提供一些基本的概念和整体核心框架，其他用于描述真实使用场景逻辑的都以插件扩展，这样的设计可以抽象的方式提供一个核心框架，其他具体的功能和业务都通过插件的扩展的方式来实现，比如构建java应用，就是通过java插件来实现的。

#### 5.1 插件的作用

> 1. 添加任务到项目中，帮助完成一些事情，比如测试、编译、打包。
> 2. 可以添加依赖配置到项目中，我们可以通过它们配置我们项目在构建构成中需要的依赖，比如我们去编译的时候依赖的第三方库等。
> 3. 可以向项目中现有的对象类型添加新的扩展属性、方法等，让你可以使用它们帮助我们配置、优化构建，比如android{}这个配置块就是Android Gradle插件为Project对象添加的一个扩展。
> 4. 可以对项目进行一些约定，比如应用java插件之后，约定src/main/java目录下是我们源代码存放的位置，在编译的时候也是编译这个目录下的java文件。

#### 5.2 如何引用一个插件

- 应用二进制插件

  > 什么是二进制插件？二进制插件就是实现了org.gradle.api.Plugin接口的插件，它们可以有plugin id。
  >
  > ```groovy
  > apply plugin:'java'
  > ```
  >
  > 这句代码就把java插件应用到我们项目中了，其中'java'是Java插件的plugin id，它是唯一的。对于Gradle自带的核心插件都有一个容易记的短名，称其为plugin id，比如这里的java，其实它对应的类型是org.gradle.api.plugins.JavaPlugin，所以通过该类型我们也可以应用这个插件：
  >
  > ```groovy
  > apply plugin:org.gradle.api.plugins.JavaPlugin
  > ```
  >
  > 又因为包org.gradle.api.plugins是默认导入的，所以我们可以去掉包名直接改为
  >
  > ```groovy
  > apply plugin:JavaPlugin
  > ```
  >
  > 以上三种写法是等价的，不过第一种用的最多，因为它容易记住，第二种写法一般适用于我们在build文件中自定义的插件，也就是脚本插件。
  >
  > 二进制插件一般被打包在一个jar里面独立发布，比如我们自定义的插件，在发布的时候我们也可以指定其plugin id，这个plugin id最好是一个全限定名称，就像包名一样，这样发布的插件plugin id就不会重复，比如org.flysnow.tools.plugin.xxx

- 应用脚本插件

  >version.gradle
  >
  >```groovy
  >ext {
  >    versionName = '1.0.0'
  >    versionCode = 1
  >}
  >```
  >
  >build.gradle
  >
  >```groovy
  >apply from : 'version.gradle'
  >
  >task ex52PrintTask {
  >    println "app version is ${versionName}"
  >    println "app version code is ${versionCode}"
  >}
  >```
  >
  >其实这不能算是一个插件，只能算是一个脚本。应用脚本插件，其实就是把这个脚本加载进来，和二进制插件不同的是它用的是from关键字，后面紧跟着一个脚本文件，可以是本地的，也可以是网络存在的，如果是网络上的话要使用html url。
  >
  >虽然它不是一个正真的插件，但是不能忽视它的作用，它是脚本文件模块化的基础，我们可以把庞大的脚本文件，进行分块，分段整理，拆分成一个个公用、职责分明的文件，然后使用apply from来引用它们，比如我们可以把常用的函数放在一个个utils.gradle文件里，供其他脚本文件引用。示例中我们把app各个版本名称和版本号单独放在一个脚本文件里，清晰、简单。我们也可以使用自动化对该文件自动处理，生成版本。

- apply方法的其他用法

  > Project.apply()方法有3种方式，它们是以接受参数的不同区分的。我们上面用的是接受一个Map类型参数的方式。此外还有两种。
  >
  > ```kotlin
  > public abstract class ProjectDelegate public constructor() : org.gradle.api.Project {
  > 
  > 	public open fun apply(closure: groovy.lang.Closure<*>): kotlin.Unit { /* compiled code */ }
  > 
  > 	public open fun apply(options: kotlin.collections.Map<kotlin.String, *>): kotlin.Unit { /* 		compiled code */ }
  > 
  > 	public open fun apply(action: org.gradle.api.Action<in 				org.gradle.api.plugins.ObjectConfigurationAction>): kotlin.Unit { /* compiled code */ }
  > }
  > ```
  >
  > 闭包的方式如下：
  >
  > ```groovy
  > apply {
  > 	plugin 'java'
  > }
  > ```
  >
  > 该闭包被用来配置一个ObjectConfigurationAction对象，所以可以在闭包里使用ObjectConfigurationAction对象的方法、属性等进行配置。
  >
  > - ObjectConfigurationAction
  >
  > ```java
  > public interface ObjectConfigurationAction {
  >     ObjectConfigurationAction to(Object... var1);
  > 
  >     ObjectConfigurationAction from(Object var1);
  > 
  >     ObjectConfigurationAction plugin(Class<? extends Plugin> var1);
  > 
  >     ObjectConfigurationAction type(Class<?> var1);
  > 
  >     ObjectConfigurationAction plugin(String var1);
  > }
  > ```
  >
  > plugin 'java'对应ObjectConfigurationAction plugin(Class<? extends Plugin> var1);
  >
  > Action的方式：
  >
  > ```groovy
  > apply(new Action<ObjectConfigurationAction>() {
  >     @Override
  >     void execute(ObjectConfigurationAction objectConfigurationAction) {
  >         objectConfigurationAction.plugin('java')
  >     }
  > })
  > ```
  >
  > 对应public open fun apply(action: org.gradle.api.Action<in 				org.gradle.api.plugins.ObjectConfigurationAction>): kotlin.Unit { /* compiled code */ }

- 应用第三方发布的插件

  > 第三方发布的作为jar的二进制插件，引用时候需要在buildscript{}里配置其classpath才能使用，这个不像Gradle为我们提供的内置插件。比如我们在Android Gradle插件，就属于Android发布的第三方插件，如果要使用它们我们要先进行配置。
  >
  > - 根目录build.gradle
  >
  >   ```groovy
  >   // Top-level build file where you can add configuration options common to all sub-projects/modules.
  >   
  >   buildscript {
  >       ext.kotlin_version = '1.3.72'
  >       ext.gradle_version = '4.0.0'
  >       repositories {
  >           google()
  >           jcenter()
  >       }
  >       dependencies {
  >           classpath "com.android.tools.build:gradle:$gradle_version"
  >           classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
  >   
  >           // NOTE: Do not place your application dependencies here; they belong
  >           // in the individual module build.gradle files
  >       }
  >   }
  >   
  >   allprojects {
  >       repositories {
  >           google()
  >           jcenter()
  >       }
  >   }
  >   
  >   task clean(type: Delete) {
  >       delete rootProject.buildDir
  >   }
  >   ```
  >
  >   buildscript{}块是一个在构建项目之前，为项目进行前期准备和初始化相关配置依赖的地方，配置好所需的依赖，就可以应用插件了：
  >
  > - app/build.gradle
  >
  >   ```groovy
  >   apply plugin: 'com.android.application'
  >   ```
  >
  >   如果没有提前在buildscript里配置依赖的classpath，会提示找不到这个插件。

- 使用plugin DSL应用插件

  >plugins DSL是一种新的插件应用方式，gradle2.1以上版本才能使用。
  >
  >```groovy
  >plugins {
  >    id 'com.android.application'
  >    id 'com.android.library'
  >    id 'org.jetbrains.kotlin.android'
  >}
  >```
  >
  >使用plugins应用第三方插件时，如果该插件已经被托管在https://plugins.gradle.org/上，我们就可以不用在buildscript里配置classpath依赖了，直接使用plugins就可以应用插件。
  >
  >```groovy
  >plugins {
  >    id 'com.android.application' version '7.1.0'
  >    id 'com.android.library' version '7.1.0'
  >    id 'org.jetbrains.kotlin.android' version '1.5.30'
  >}
  >```

#### 5.3 自定义插件

> ```groovy
> apply plugin : ExCustomPlugin
> 
> class ExCustomPlugin implements Plugin<Project> {
> 
>  @Override
>  void apply(Project project) {
>      project.task('ex53CustomTask') {
>          println "这是一个通过自定义插件创建的task"
>      }
>  }
> }
> ```
>
> wangzhiping@wangzhiping-PC:~/GradleProject$ gradle ex53CustomTask
> Starting a Gradle Daemon (subsequent builds will be faster)
>
> Configure project :	
> 这是一个通过自定义插件创建的task
>
> 自定义插件必须要实现Plugin接口，这个接口只有一个apply方法，该方法在插件被应用时调用，所以我们可以实现这个方法，做我们想做的事情，比如这里创建一个名称为ex53CustomTask的任务。	
>
> 这个插件定义在build脚本里，只能是自己的项目用，如果我们想开发一个独立的插件给所有人使用，应该怎么做呢？这就需要单独创建一个Groovy工程作为开发自定义插件的工程了。
>
> - groovy模块目录层级
>
>   ```
>   ── buildSrc
>   │   ├── build
>   │   │   ├── classes
>   │   │   │   └── groovy
>   │   │   │       └── main
>   │   │   │           └── com
>   │   │   │               └── henyiwu
>   │   │   │                   └── gradle
>   │   │   │                       ├── Ex53CustomPlugin$_apply_closure1$_closure2.class
>   │   │   │                       ├── Ex53CustomPlugin$_apply_closure1.class
>   │   │   │                       └── Ex53CustomPlugin.class
>   │   │   ├── generated
>   │   │   │   └── sources
>   │   │   │       └── annotationProcessor
>   │   │   │           └── groovy
>   │   │   │               └── main
>   │   │   ├── libs
>   │   │   │   └── buildSrc.jar
>   │   │   ├── resources
>   │   │   │   └── main
>   │   │   │       └── META-INF
>   │   │   │           └── gradle-plugins
>   │   │   │               └── com.henyiwu.gradle.Ex53CustomPlugin.properties
>   │   │   ├── source-roots
>   │   │   │   └── buildSrc
>   │   │   │       └── source-roots.txt
>   │   │   └── tmp
>   │   │       ├── compileGroovy
>   │   │       │   └── groovy-java-stubs
>   │   │       └── jar
>   │   │           └── MANIFEST.MF
>   │   ├── build.gradle
>   │   └── src
>   │       └── main
>   │           ├── groovy
>   │           │   └── com
>   │           │       └── henyiwu
>   │           │           └── gradle
>   │           │               └── Ex53CustomPlugin.groovy
>   │           └── resources
>   │               └── META-INF
>   │                   └── gradle-plugins
>   │                       └── com.henyiwu.gradle.Ex53CustomPlugin.properties
>   ├── gradle
>   │   └── wrapper
>   │       ├── gradle-wrapper.jar
>   │       └── gradle-wrapper.properties
>   ├── gradle.properties
>   ├── gradlew
>   ├── gradlew.bat
>   ├── local.properties
>   └── settings.gradle
>   ```
>
> - Ex53CustomPlugin.groovy
>
>   ```groovy
>   class Ex53CustomPlugin implements Plugin<Project>{
>
>       @Override
>       void apply(Project project) {
>           project.task('ex53CustomTask') {
>               println "这是一个通过自定义插件创建的task"
>               doLast {
>                   println "ex53CustomTask do Last"
>               }
>           }
>       }
>   }
>   ```
>
> - resources/META-INF/gradle-plugins/{pluginId}.properties
>
>   这里文件名对应com.henyiwu.gradle.Ex53CustomPlugin.properties
>
>   ```properties
>   implementation-class=com.henyiwu.gradle.Ex53CustomPlugin
>   ```
>
> - buildSrc/build.gradle
>
>   ```groovy
>   apply plugin: 'groovy'
>
>   dependencies {
>       implementation gradleApi()
>       implementation localGroovy()
>   }
>   ```
>
> - 运行结果
>
>   ```
>   wangzhiping@wangzhiping-PC:~/AndroidStudioProjects/GradleTEst$ gradle ex53CustomTask
>   
>   > Configure project :app
>   这是一个通过自定义插件创建的task
>   
>   > Task :app:ex53CustomTask
>   ex53CustomTask do Last
>   
>   BUILD SUCCESSFUL in 984ms
>   ```

### 6. Java/Kotlin Gradle插件

> Java/Kotlin gradle插件，指的是org.jetbrains.kotlin.android或者java插件，它们帮助我们编译源文件，进行单元测试，打包发布等。

#### 6.1 引入插件

- app/build.gradle

  ```groovy
  plugins {
      // 这是一个app模块
      id 'com.android.application'
      // 引入kotlin插件
      id 'org.jetbrains.kotlin.android'
  }
  ```

#### 6.2 Java/Kotlin插件约定的项目结构

> 只有我们遵循这些项目结构约定，插件才能找到我们的Java类，找到我们的资源进行编译。
>
> ```
> ├── app
> │   ├── build.gradle
> │   └── src
> │       ├── test
> │       ├── main
> │       │   ├── AndroidManifest.xml
> │       │   ├── java
> │       │   │   └── com
> │       │   │       └── example
> │       │   │           └── myapplication
> │       │   │               └── MainActivity.kt
> │       │   └── res
> ├── build.gradle
> ```
>
> 默认情况下，src/main/Java为我们的项目源代码存放位置：src/main/res为要打包的文件存放目录，比如一些Properties配置文件和图片等。
>
> main和test是插件为我们内置的两个源代码集合，我们可以添加新的源代码集合，例如添加一个vip文件夹，并且依葫芦画瓢照着main文件夹新建java、res文件夹，然后在app/build.gradle中添加：
>
> ```
> android {
>     sourceSets {
>         vip {
> 
>         }
>     	}
> }
> ```
>
> 新建源代码集合，默认的路径是src/sourceName/java、src/sourceName/res

#### 6.3 如何配置第三方依赖

> 想要使用这些第三方依赖，你要告诉Gradle如何找到这些依赖，也就是我们要讲的依赖配置。一般情况下都是我们都是从仓库找我们要的jar包，在gradle中要配置一个仓库的jar依赖，首先我们得告诉gradle我们要使用什么类型的仓库，这些仓库的位置在哪里，这样gradle知道从哪里去搜寻我们的jar：
>
> - build.gradle
>
>   ```groovy
>   buildscript {
>       repositories {
>           mavenCenter()
>       }
>   }
>   allprojects {
>       repositories {
>           mavenCenter()
>       }
>   }
>   ```
>
>   以上脚本配置了一个Maven中心库，告诉gradle可以在Maven中心搜寻我们依赖的jar包。
>
>   有了仓库，就需要告诉Gradle我们需要依赖什么。
>
>   ```groovy
>   dependencies {
>   	implementation 'com.squareup.retrofit2:retrofit:2.9.0'
>   }
>   ```
>
>   它完整的写法应该是
>
>   ```groovy
>   dependencies {
>   	implementation group:'com.squareup.retrofit2',name:'retrofit',version:'2.9.0'
>   }
>   ```
>
> - api和implementation的区别
>
>   ```
>   implementation：
>   Gradle 会将依赖项添加到编译类路径，并将依赖项打包到构建输出。不过，当您的模块配置 implementation 依赖项时，会让 Gradle 了解您不希望该模块在编译时将该依赖项泄露给其他模块。也就是说，其他模块只有在运行时才能使用该依赖项。
>   使用此依赖项配置代替 api 或 compile（已弃用）可以显著缩短构建时间，因为这样可以减少构建系统需要重新编译的模块数。例如，如果 implementation 依赖项更改了其 API，Gradle 只会重新编译该依赖项以及直接依赖于它的模块。大多数应用和测试模块都应使用此配置。
>   
>   api：
>   Gradle 会将依赖项添加到编译类路径和构建输出。当一个模块包含 api 依赖项时，会让 Gradle 了解该模块要以传递方式将该依赖项导出到其他模块，以便这些模块在运行时和编译时都可以使用该依赖项。
>   此配置的行为类似于 compile（现已弃用），但使用它时应格外小心，只能对您需要以传递方式导出到其他上游消费者的依赖项使用它。 这是因为，如果 api 依赖项更改了其外部 API，Gradle 会在编译时重新编译所有有权访问该依赖项的模块。 因此，拥有大量的 api 依赖项会显著增加构建时间。除非要将依赖项的 API 公开给单独的模块，否则库模块应改用 implementation 依赖项。
>   ```
>
> - 引用一个本地module，例如我们新建一个mylibrary
>
> - setting.gradle
>
>   ```groovy
>   include ':mylibrary'
>   ```
>
> - app/build.gradle
>
>   ```groovy
>   dependencies {
>       implementation project(':mylibrary')
>   }
>   ```
>
>   然后我们就可以在app模块使用mylibrary的类了
>
> - 引入本地jar包、arr包
>
>   ```groovy
>   dependencies {
>   	api fileTree(dir: 'libs', include:['*.jar'])
>   	api fileTree(dir: 'libs', include:['*.aar']
>   }
>   ```
>
>   这样配置后，libs文件下的扩展名为jar的都会被依赖，这里用到的是Project的fileTree()方法

#### 6.4 如何构建一个java项目

> 在gradle中，执行任何操作都是任务驱动的，构建java项目也不例外。java插件为我们提供了很多任务，通过运行它们来达到我们构建java项目的目的。最常用的是build任务，运行它会构建你的整个项目，我们可以通过gradle build来运行，然后gradle就会开始编译源码文件，处理资源文件，打成jar包，然后编译测试用例代码，处理测试资源，最后运行单元测试。
>
> 运行build后大致的task执行流程
> compileJava -> processResource -> classes -> jar -> assemble -> compileTestJava -> processTestResource -> testClasses 
> -> test -> check -> build
>
> 最后在build/libs生成jar包
>
> - clean
>
>   这个是删除build目录以及其他构建生成的文件。如果编译中有问题，可以先clean再重新编译。
>
> - Assemble
>
>   该任务不会执行单元测试，只会编译和打包。这个任务在android里也有，执行它可以打apk包，所以它不止会打jar包，其实它也算是一个引导类任务，根据不同的项目打不同的包。
>
> - check
>
>   只会执行单元测试，有时候还会做一些质量检查，不会打jar包。

#### 6.5 源码集合SourceSet的概念

> SourceSet-源代码合集-源集，是java插件用来描述和管理源代码及其资源的一个抽象概念，是一个java源代码文件和资源文件的集合。通过源集，我们可以非常方便地访问源代码目录，设置源集的属性，更改源集的java目录或者资源文件等。
>
> 有了源集，我们就能针对不同的业务和应用对我们源代码进行分组，比如用于主要业务产品的main以及用于单元测试的test，责任分明、清晰。它们两个也是java插件默认内置的两个标准集。
>
> java插件在Project下为我们提供了一个sourceSets属性以及一个sourceSets{}闭包来访问和配置源集。sourceSets是一个SourceSetContainer。
>
> - 遍历源代码集合
>
>   ```groovy
>   sourceSets.all {
>       println "源代码集：name: ${name}"
>   }
>   ```
>
> - 输出源代码集路径
>
>   ```groovy
>   sourceSets.all {
>       println "源代码集：srcDirs: ${java.srcDirs}"
>   }
>   ```
>
>   源代码集：srcDirs: [/Users/xx/AndroidStudioProjects/GradleApplication/app/src/androidTest/java]
>   源代码集：srcDirs: [/Users/xx/AndroidStudioProjects/GradleApplication/app/src/androidTestDebug/java]
>   源代码集：srcDirs: [/Users/xx/AndroidStudioProjects/GradleApplication/app/src/debug/java]
>   源代码集：srcDirs: [/Users/xx/AndroidStudioProjects/GradleApplication/app/src/main/java]
>   源代码集：srcDirs: [/Users/xx/AndroidStudioProjects/GradleApplication/app/src/release/java]
>   源代码集：srcDirs: [/Users/xx/AndroidStudioProjects/GradleApplication/app/src/test/java]
>   源代码集：srcDirs: [/Users/xx/AndroidStudioProjects/GradleApplication/app/src/testDebug/java]
>   源代码集：srcDirs: [/Users/xx/AndroidStudioProjects/GradleApplication/app/src/testRelease/java]
>
> - 源代码集合的资源文件
>
>   ```groovy
>   sourceSets.all {
>       println "源代码集：srcDirs: ${resources}"
>   }
>   ```
>
>    source=[src/androidTest/resources]
>    source=[src/androidTestDebug/resour
>    source=[src/debug/resources]
>    source=[src/main/resources]
>    source=[src/release/resources]
>    source=[src/test/resources]
>    source=[src/testDebug/resources]
>    source=[src/testRelease/resources]
>
> - 更改源代码集路径
>
>   ```groovy
>   def modules = src_root.list().toList().stream()
>           .filter(
>                   new Predicate<String>() {
>                       @Override
>                       boolean test(String name) {
>                           return name == 'main' || (name.startsWith('xx_') && new File(src_root, 		name).isDirectory())
>                       }
>                   }).collect(Collectors.toList())
>   
>   def moduleSrc = modules.stream()
>           .map(
>                   new Function() {
>                       @Override
>                       Object apply(Object moduleName) {
>                           return ['src', moduleName, 'java'].join('/')
>                       }
>                   })
>           .collect(Collectors.toList())
>   
>       def moduleRes = p_modules.stream()
>           .map(
>               new Function() {
>                   @Override
>                   Object apply(Object moduleName) {
>                       return ['src', moduleName, 'res'].join('/')
>                   }
>               }).collect(Collectors.toList())
>   
>   sourceSets {
>       main {
>         	// srcDirs是一个集合，以上代码表示指定java源代码集合为src下，main文件夹和xx_开头的文件夹
>           java.srcDirs = moduleSrc
>         	// 同理，资源文件集
>        		res.srcDirs = moduleRes
>       }
>   }
>   ```

#### 6.6 java插件添加的任务

> java插件主要包含以下task
>
> - java插件添加的通用任务
>
>   ```
>   任务名称               类型             描述
>   compileJava           JavaCompile     使用 javac 编译 Java 源文件
>   processResources      Copy            把资源文件 copy 到生产的资源文件目录中
>   classes               Task            组装产生的类和资源文件目录
>   compileTestJava       JavaComplie     使用 javac 编译测试的 Java 源文件
>   ProcessTestResource   Copy            把测试的资源文件 copy 到生产的资源文件目录中
>   testClass             Task            组装产生的测试类和资源文件目录
>   jar                   Jar             组装 Jar 文件
>   javadoc               Javadoc         使用 javadoc 生成 Java API  文档
>   test                  Test            使用 Junit 或 TestNG 进行单元测试
>   uploadArchives        Upload          上传包含 Jar 的构建，用 archives{} 闭包进行配置
>   clean                 Delete          清理构建生成的目录文件
>   cleanTaskName         Delete          删除指定任务生成的文件，比如 cleanJar 会删除 Jar 任务生成的文件
>   ```
>
> - 源集任务
>
>   ```
>   任务名称                    类型              描述
>   compileSourceaSetJava      JavaCompile      使用 javac 编译指定源集的 Java 源代码
>   processSourceSetResources  Copy             把指定源集的资源文件复制到生产文件的资源目录中
>   sourceSetClasses           Task             组装给定源集类和资源文件目录
>   ```
>
>   运行任务的时候，列表中的任务名称中的sourceSet要换成源集的名称，比如main源集的名称是compileMainJava

#### 6.7 Java插件添加的属性

> ```dart
> 属性名称                   类型                  描述
> sourceSets           SourceSetContainer       Java 项目的源集，可以访问和配置源集
> sourceCompatiblity   JavaVersion              编译 Java 源文件使用的版本
> targeCompatiblity    JavaVersion              编译生成的类的 Java 版本
> archivesBaseName     String                   打包 Java 或者 Zip 文件的名字
> manifest             Manifest                 用于访问或者配置 manifest 清单文件
> libsDir              File                     存放生成的类库目录
> distsDir             File                     存放生成发布的文件目录
> ```

#### 6.8 多项目构建

> 多项目构建，其实就是多个gradle项目一起构建，它们一起通过Settings.gradle配置管理。每个项目都有一个build文件对该项目进行配置，然后采用项目依赖，就可以实现多项目协作，这对于大项目开发，进行模块化非常有用。
>
> - 目录结构
>
>   ```
>   ├── app
>   │   ├── build.gradle
>   │   ├── libs
>   │   ├── proguard-rules.pro
>   │   └── src
>   │       ├── androidTest
>   │       ├── main
>   │       └── test
>   ├── build.gradle
>   ├── mylibrary
>   │   ├── build.gradle
>   │   └── src
>   │       ├── androidTest
>   │       ├── main
>   │       └── test
>   └── settings.gradle
>   ```
>
> - settings.gradle
>
>   ```
>   rootProject.name = "GradleApplication"
>   include ':app'
>   include ':mylibrary'
>   ```
>
> - app/build.gradle
>
>   ```kotlin
>   dependencies {
>   	implementation project(':mylibrary')
>   }
>   ```
>
> - subprojects
>
>   在根目录中使用，遍历所有子项目
>
>   ```groovy
>   subprojects {
>       apply plugin : 'kotlin-android'
>   }
>   ```
>
>   例如，这样就让所有子项目都依赖了kotlin-android插件
>
> - buildscript、allprojects、subprojects的区别
>
>   ```
>   buildScript块的repositories主要是为了Gradle脚本自身的执行，获取脚本依赖插件。也就是说，buildScript是用来加载Gradle脚本自身需要使用的资源，可以声明的资源包括依赖项、第三方插件、maven仓库地址等。
>   
>   allprojects块的repositories用于多项目构建，为所有项目提供共同的所需依赖包。而子项目可以配置自己的repositories以获取自己独需的依赖包。
>   
>   subprojects块的repositories用于配置这个项目的子项目。使用多模块项目时，不同模块之间有相同的配置，导致重复配置，可以将相同的部分抽取出来，使用配置注入的技术完成子项目的配置。根项目就像一个容器, subprojects 方法遍历这个容器的所有元素并且注入指定的配置。allprojects是对所有project的配置，包括Root Project。而subprojects是对所有Child Project的配置。
>   ```

#### 6.9 如何发布构件

> 基于gradle6.7.1
>
> ```groovy
> apply plugin: 'groovy'
> apply plugin: 'kotlin'
> apply plugin: 'maven-publish'
> apply plugin: 'maven'
> 
> dependencies {
>     implementation gradleApi()
>     implementation localGroovy()
> 		....
> }
> 
> repositories {
>     mavenCentral()
>     google()
> }
> 
> buildscript {
>     ext.kotlin_version = '1.4.10'
>     repositories {
>         mavenCentral()
>     }
>     dependencies {
>         classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
>     }
> }
> compileKotlin {
>     kotlinOptions {
>         jvmTarget = "1.8"
>     }
> }
> compileTestKotlin {
>     kotlinOptions {
>         jvmTarget = "1.8"
>     }
> }
> 
> def MAVEN_PATH = "$MAVEN_PATH"
> def ARTIFACT_ID = 'xxxxxx'
> def VERSION_NAME = '1.0.0'
> def GROUP_ID = "xxxxxx"
> uploadArchives {
>     repositories {
>         mavenDeployer {
>             repository(url: MAVEN_PATH) {
>                 authentication(userName: "userName", password: "password")
>             }
>             pom.project {
>                 groupId GROUP_ID
>                 artifactId ARTIFACT_ID
>                 version VERSION_NAME
>                 packaging 'aar'
>             }
>         }
>     }
> }
> ```
>
> 同步后，运行publish任务即可，该任务属于maven-publish插件

### 7 Android Gradle 插件

### 7.1 Android Gradle 插件简介

> 从Gradle的角度来看，Android插件其实就是Gradle的一个第三方插件，它是由Google的Android团队开发的

#### 7.2 Android Gradle 插件分类

> 1. App插件id：com.android.application
> 2. Library插件id：com.android.library
> 3. Test插件id：com.android.test
>
> 通过应用以上三种不同的插件，可以配置项目为一个Android App工程，或者Library工程，或者是一个Android Test测试工程。

#### 7.3 应用Android Gradle 插件

> 应用一个插件，必须知道它的id，并且配置它们的依赖classpath。
>
> - 根目录下build.gradle
>
>   ```groovy
>   buildscript {
>       repositories {
>         	// jcenter()仓库，目前已过期 
>       		jcenter()
>       }
>       dependencies {
>           classpath "com.android.tools.build:gradle:4.0.2"
>       }
>   }
>   ```
>
> - app/build.gradle
>
>   ```groovy
>   apply plugin : 'com.android.application'
>   
>   android {
>     
>   }
>   ```

#### 7.4 Android Gradle 工程示例

> Android Gradle 插件继承于Java插件，具有Java插件的特性，它也需要在Setting文件里通过include配置包含的子工程，也需要应用Android插件等。
>
> - app目录结构
>
>   ```
>   ├── app
>   │   ├── build.gradle
>   │   ├── libs
>   │   ├── proguard-rules.pro
>   │   └── src
>   │       ├── androidTest
>   │       │   └── java
>   │       ├── main
>   │       │   ├── AndroidManifest.xml
>   │       │   ├── java
>   │       │   └── res
>   │       └── test
>   │           └── java
>   ├── build.gradle
>   ```
>
>   main文件夹与Java文件结构相比，多了AndroidManifest.xml和res这两个属于Android特有的文件目录。
>
> - app/build.gradle
>
>   ```groovy
>   apply plugin: 'com.android.application'
>   
>   android {
>       defaultConfig {
>           applicationId application_id
>           versionCode version_code
>           versionName version_name
>       }
>   
>       signingConfigs {
>       		// 签名相关配置
>       }
>   
>   		buildTypes {
>           debug {
>               
>           }
>   
>           release {
>               
>           }
>       }
>   
>       flavorDimensions 'pandora'
>       productFlavors {
>           publish {
>           }
>           develop {
>               buildConfigField "boolean", "IS_RELEASE_PACKAGE", "false"
>           }
>       }
>   
>       aaptOptions {
>           cruncherEnabled = false
>           useNewCruncher = false
>       }
>   
>       compileOptions {
>           sourceCompatibility JavaVersion.VERSION_1_8
>           targetCompatibility JavaVersion.VERSION_1_8
>       }
>   
>       kotlinOptions {
>           jvmTarget = '1.8'
>       }
>   
>       buildFeatures {
>           viewBinding true
>       }
>   }
>   
>   dependencies {
>       implementation fileTree(dir: 'libs', include: ['*.jar'])
>       implementation fileTree(dir: 'libs', include: ['*.aar'])
>       implementation project(":lib-center")
>       ...
>   }
>   
>   project.afterEvaluate { project ->
>       project.android.buildTypes.all { buildType ->
>           println(">>>>>> 当前编译的签名信息 = ${buildType.signingConfig.toString()}")
>       }
>   }
>   
>   println(">>>>>>当前使用的gradle版本=" + project.gradle.gradleVersion)
>   ```
>
>   Android Gradle 工程的配置，都是在android{}中，这是唯一的入口，通过它，可以对Android Gradle工程进行自定义的配置，具体的实现是com.android.build.gradle.AppExtension

##### 7.4.1 compileSdkVersion

> 编译Android工程的SDK版本，原型是一个compileSdkVersion方法
>
> ```java
> public void compileSdkVersion(String version) {
> 		checkWritability()
> 		this.target = version;
> }
> ```

- build.gradle

  ```groovy
  android {
      compileSdkVersion Integer.parseInt("${CompileSdkVersion}")
  }
  ```

- 此外，还有一个setCompileSdkVersion

  ```java
  public void setCompileSdkVersion(int apiLevel) {
  		compileSdkVersion(apiLevel)
  }
  ```

- 使用方法

  ```groovy
  android.compileSdkVersion = 23
  android.compileSdkVersion = 'android-23'
  ```

##### 7.4.2 buildToolsVersion

> buildToolsVersion "23.0.1"表示我们使用的android构件工具的版本，我们可以在Android SDK目录里看到，它是一个工具包，包括aapt、dex等工具，它的原型也是一个方法：
>
> ```java
> public void buildToolsVersion(String version) {
> 		checkWritability();
> 		buildToolsVersion = FullRevision.parseRevision(version);
> }
> 
> @Override
> public String getBuildToolsVersion() {
> 		return buildToolsRevision.toString();
> }
> ```
>
> 从源代码可以看出，我们可以通过buildToolsVersion方法赋值，也可以通过android.buildToolsVersion属性读写它的值。

##### 7.4.3 defaultConfig

> defaultConfig是默认的配置，它是一个ProductFlavor。ProductFlavor允许我们根据不同的情况生成多个不同的APK包，比如多渠道打包。如果不针对我们自定义的ProductFlavor单独配置的话，会为这个ProductFlavor使用默认的defaultConfig的配置。
>
> - build.gradle
>
>   ```groovy
>   defaultConfig {
>     	// 包名
>       applicationId application_id
>       // 版本号
>     	versionCode version_code
>       // 版本名
>     	versionName version_name
>     	// 最低支持的android系统api
>     	minSdkVersion Integer.parseInt("${MinSdkVersion}")
>       // 基于哪个android版本开发的
>     	targetSdkVersion Integer.parseInt("${TargetSdkVersion}")
>   }
>   ```
>
>   以上所有类都对应ProductFlavor类里的方法或属性。

##### 7.4.4 buildTypes

> BuildTypes是一个NamedDomainObjectContainer类型，是一个域对象，和sourceSet一样。buildTypes里有release、debug等。我们可以在buildTypes{}里增加任意多个我们需要构建的类型，Gradle会帮我们自动创建一个对应的buildType，名字就是我们定义的名字。
>
> - build.gradle
>
>   ```
>       buildTypes {
>           debug {
>              ...
>           }
>   
>           release {
>               debuggable = false
>               jniDebuggable false
>               renderscriptDebuggable false
>               renderscriptOptimLevel 3
>               //混淆
>               minifyEnabled true
>               //Zipalign优化
>               zipAlignEnabled true
>               // 移除无用的resource文件
>               shrinkResources true
>               // 两个混淆文件
>               // 1. getDefaultProguardFile('proguard-android.txt')，默认的文件，在android-sdk/tools/proguard/目录下
>               // 2. proguard-rules.pro我们自己写的混淆文件
>               proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
>               signingConfig signingConfigs.release
>           }
>       }
>   ```

#### 7.5 Android Gradle 任务

