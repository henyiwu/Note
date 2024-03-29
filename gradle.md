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

#### 4.9 任务规则

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

> ```java
> apply plugin : ExCustomPlugin
> 
> class ExCustomPlugin implements Plugin<Project> {
> 
>     @Override
>     void apply(Project project) {
>         project.task('ex53CustomTask') {
>             println "这是一个通过自定义插件创建的task"
>         }
>     }
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
>       // 引用gradleApi()，才能够检测到Plugin插件
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

### 7. Android Gradle 插件

#### 7.1 Android Gradle 插件简介

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
>   	buildTypes {
>           debug {
>                                 
>           }
>                     
>           release {
>                                 
>           }
>       }
>                     
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
>               // 1. getDefaultProguardFile('proguard-android.txt')，默认的文件，在android-sdk/tools/proguard/目录下，主要是基本组件的防混淆，例如四大组件。
>               // 2. proguard-rules.pro我们自己写的混淆文件
>               proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
>               signingConfig signingConfigs.release
>           }
>       }
>   ```

#### 7.5 Android Gradle 任务

> Android插件是基于Java的插件，所以Android插件基本上包含了所有Java插件的功能，包括继承的任务，比如assemble、check、build等。除此之外，Android在大类上还添加了connectedCheck、deviceCheck、lint、install、uninstall等任务，这些是属于Android特有的功能。

### 8. 自定义Android Gradle 工程

#### 8.1 defaultConfig 默认配置

> DefaultConfig是Android对象中的一个配置块，负责定义所有的默认配置，它是一个ProductFlavor，如果ProductFlavor没有被特殊定义配置的话，默认就会使用defaultConfig{}块指定的配置，比如报名、版本号、版本名等。
>
> - 一个基本的defaultConfig配置
>
>   ````
>   android {
>   		compileSdkVersion 23
>   		buildToolsVersion "23.0.1"
>   		                  
>   		defaultConfig {
>   				applicationId "xxx.xxx.xxx"
>   				minSdkVersion 19
>   				targetSdkVersion 23
>   				versionCode 1
>   				versionName "1.0"
>   		}
>   }
>   ````
>
>   以上配置可以满足一个app的基本开发

##### 8.1.1 applicationId

> applicationId是ProductFlavor的一个属性，用于指定生成app的包名，默认情况下是null，那么构建时，会从我们的androidManifest.xml文件中读取，manifest标签package属性值。

##### 8.1.2 minSdkVersion

> minSdkVersion是ProductFlavor的一个方法，它可以指定我们的app最低支持的Android操作系统，其对应的值是Android SDK的api level。

##### 8.1.3 targetSdkVersion

> 这个用于配制我们是基于哪个Android SDK开发，它的可选值与minSdkVersion一样，没有配置的时候也会从androidManifest.xml中读取。

##### 8.1.4 versionCode

> 也是ProductFlavor中的一个属性，用于配制app内部版本号，是一个常数值，没有配置的时候从androidManifest.xml读取。

##### 8.1.5 versionName

> 和versionCode相似，用于配制app的版本名称，其值是字符串，让用户知道当前的app版本。

##### 8.1.8 signingConfig

> 配制默认签名信息，对生成的app签名，也是ProductFlavor的一个属性。
>
> - signingConfig
>
>   ````groovy
>       signingConfigs {
>           Properties keyProperties = new Properties()
>           keyProperties.load(new FileInputStream(file("${project.rootDir}/buildcfg/xxx/keystore.properties")))
>   
>           debug {
>               storeFile file("${project.rootDir}" + keyProperties["debugStoreFile"])
>               keyAlias keyProperties["debugKeyAlias"]
>               storePassword keyProperties["debugStorePassword"]
>               keyPassword keyProperties["debugKeyPassword"]
>           }
>   
>           release {
>               storeFile file("${project.rootDir}" + keyProperties["releaseStoreFile"])
>               keyAlias keyProperties["releaseKeyAlias"]
>               storePassword keyProperties["releaseStorePassword"]
>               keyPassword keyProperties["releaseKeyPassword"]
>           }
>       }
>   ````
>
> - keystore.properties
>
>   ```properties
>   releaseStoreFile=/buildcfg/xxxx/xxxx.keystore
>   releaseStorePassword=xxxx
>   releaseKeyAlias=xxxx
>   releaseKeyPassword= xxxx
>   debugStoreFile=/buildcfg/xxxx/xxxx.keystore
>   debugStorePassword= xxxx
>   debugKeyAlias=xxxx
>   debugKeyPassword= xxxxx
>   ```

##### 8.1.9 proguardFile

> 用于配制app proguard混淆所使用的ProGuard配置文件，它是ProductFlavor的一个方法，接受一个文件作为参数。

##### 8.1.10 proguardFiles

> 同上，配制多个混淆文件。

#### 8.2 配制签名信息

> 一个app只有在签名后才能发布、安装、应用，签名是保护app的方式，标记该app的唯一性。如果app被恶意篡改，签名就不一样了，就无法安装升级，一定程度上也保护了我们的app。
>
> 要对app进行签名，需要有一个签名证书文件。
>
> 一般app有debug和release两种模式，我们可以针对两种模式采用不同的签名方式，一般debug的时候，android SDK已经提供了一个默认的debug签名证书，我们可以直接使用。但是发布的时候，release模式构建时，我们要配制使用自己的签名。

- 签名配制

  ```groovy
      signingConfigs {
          Properties keyProperties = new Properties()
          keyProperties.load(new FileInputStream(file("${project.rootDir}/buildcfg/xxx/keystore.properties")))
  
          debug {
              storeFile file("${project.rootDir}" + keyProperties["debugStoreFile"])
              keyAlias keyProperties["debugKeyAlias"]
              storePassword keyProperties["debugStorePassword"]
              keyPassword keyProperties["debugKeyPassword"]
          }
  
          release {
              storeFile file("${project.rootDir}" + keyProperties["releaseStoreFile"])
              keyAlias keyProperties["releaseKeyAlias"]
              storePassword keyProperties["releaseStorePassword"]
              keyPassword keyProperties["releaseKeyPassword"]
          }
      }
  ```

  SigningConfigs是Android的一个方法，它接受一个域对象作为参数，一个SigningConfig就是一个签名配制

  storeFile：签名证书文件

  storePassword：签名证书文件的密码

  storeType：签名证书的类型

  keyAlias：签名证书中密钥别名

  keyPassword：签名证书中该密钥的密码

  默认情况下，debug模式的签名已经被配置好了，使用的是android SDK自动生成的debug证书，它一般位于$HOME/.android/debug.keystore，其key和密码都是已知的，一般情况下我们不需要单独配置debug模式的签名信息。

- 使用签名

  ```groovy
      buildTypes {
          debug {
              signingConfig signingConfigs.debug
          }
  
          release {
              signingConfig signingConfigs.release
          }
      }
  ```

#### 8.3 构建的应用类型

> 在android gradle 工程中，android gradle 已经帮我们配置了debug和release两个构建类型，这两种模式的主要差别在于，能够在设备上调试以及签名不一样，其他代码资源都是一样的。
>
> - buildTypes
>
>   ```
>       buildTypes {
>           debug {
>                     
>           }
>                     
>           release {
>                                
>           }
>           // 自定义
>           vip {
>                             
>           }
>       }
>   ```
>
>   如果还想新增构建类型，在buildTypes{}代码块中继续添加元素就可以，buildTypes和signingConfigs一样，也是Android的一个方法，接受的参数是一个域对象NamedDomainObjectContainer。添加的每一个都是BuildType类型。

##### 8.3.1 applicationIdSuffix

> BuildType的一个属性，用于配制基于默认applicationId的后缀，比如默认defaultConfig中配制的applicationId为com.hello.world，我们在debug的BuildType中指定applicationIdSuffix为.debug，那么生成的debug apk包名为com.hello.world.debug。

##### 8.3.2 debuggable

> BuildType的一个属性，用于配制是否可以生成一个可供调试的apk。
>
> - buildType
>
>   ```
>       buildTypes {
>           debug {
>               debuggable true
>           }
>                     
>           release {
>               debuggable = false
>           }
>       }
>   ```

##### 8.3.3 jniDebuggable

> 类似于debuggable，是否生成可供jni调试的apk包。



##### 8.3.4 minifyEnabled

> BuildType的一个属性，用于配制该BuildType是否启用Proguard混淆。

##### 8.3.5 multiDexEnabled

> BuildType的一个属性，用于配制该BuildType是否启用自动拆分多个Dex的功能。一般用于程序中代码太多了，超过了65535个方法的时候。

##### 8.3.6 proguardFile/proguardFiles

> 配制混淆文件

##### 8.3.8 shrinkResources

> BuildType的一个属性，用于配制是否启动自动清理未使用的资源，默认为false

##### 8.3.9 signingConfig

> 配制签名文件

每一个BuildType都会生成一个sourceSet，默认位置为src//，一个sourceSet包含源代码，资源文件信息，在android中就包含了java源代码，res资源文件以及androidManifest文件。所以针对不同的BuildType，我们可以单独为其指定java源代码，res资源等。只要把他们放到src//下相应的位置即可，在构建的时候，Android Gradle 会优先使用它们代替main下相关文件。

另外需要注意，因为每个buildType都会生成一个SourceSet，所以新增的buildType名字一定要注意，不能是main和androidTest，因为它们已经被系统占用，同时每个buildType之间名称不能相同。

除了会生成对应的sourceSet外，每一个BuildType还会生成相应的assemble任务，比如常用的assmebleRelease和assembleDebug就是Android Gradle自动生成的两个task任务，它们对应release和debug这两个BuildType自动生成的，执行相应的assemeble任务，就能生成对应BuildType的所有apk。

#### 8.4 使用混淆

> 混淆是一个非常有用的功能，不仅能够优化我们的代码，让apk包变得更小，还可以混淆我们的代码，让反编译的人也不容易看懂业务逻辑，一般release版本需要混淆，debug版本不需要，混淆后就无法断电跟踪调试了。
>
> 要启用混淆，把BuildType属性的minifyEnabled值设置为true即可。
>
> 再配制混淆文件。
>
> ```groovy
>     buildTypes {
>         release {
>             minifyEnabled true
> 	          proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
>         }
>     }
> ```

#### 8.5 启用zipAlignEnabled优化

> zipAlignEnabled是android为我们提供的一个整理优化的apk文件工具，它能提高系统和应用的运行效率，更快地读写apk中的资源，降低内存的使用。所以对于要发布的app，在发布之前一定要使用zipalign优化
>
> Android Gradle提供了开启zipalign优化更简便的方式，我们只需要打开配制即可，剩下的操作，如调用sdk目录下的zipalign工具进行处理等，Android Gradle会帮我们搞定。

### 9. Android Gradle 高级自定义

#### 9.1 使用公共库

> android的包，比如android.app、android.content、android.view、android.widget等，是默认就包含在android sdk库里的，所有应用都可以直接使用它们，系统会帮我们自动链接它们，不会出现找不到的情况。还有一些库，比如com.google.android.maps，这些库是独立的，并不会被系统自动链接，所以需要单独进行生成使用。

#### 9.2 批量修改生成的apk文件名

> 既然要修改生成的apk文件名，就需要修改android gradle打包输出。android对象为我们提供了三个属性（不只是用来修改apk名）
>
> 1. ApplicationVariant
> 2. Library Vaniant
> 3. TestVaniant
>
> 这三个元素的名字直译是变体，通俗来讲是android构建的产物，比如applicationVaniant代表google渠道的release包，也可以代表dev开发用的debug包。
>
> 访问以上3个集合都会触发创建所有的任务，这意味着访问这些集合后无需重新配置就会产生，也就是说假如我们通过访问这些集合，修改apk输出的文件名，那么会自动触发创建所有任务，此时我们修改后的新的apk文件名就会起作用。
>
> - 修改apk名
>
>   ```groovy
>   android {
>   	applicationVariants.all { variant ->
>           variant.outputs.all {
>               // 定义apk的名字，拼接variant的版本号
>               def apkName = "${variant.versionName}"
>               // 判断是否为空
>               if (!variant.flavorName.isEmpty()) {
>                   apkName += "_${variant.flavorName}"
>               }
>               // 赋值属性
>               outputFileName = apkName + "_${getBuildTime()}" + ".apk"
>           }
>       }
>   }
>   ```
>
>   ApplicationVariants是一个DomainObjectCollection集合，我们可以通过all方法遍历，遍历的每一个variant都是一个生成的产物。例如默认的有debug和release两个产物。
>
>   ApplicationVariants中的variant都是ApplicationVariant，它有一个outputs作为它的输出。每个ApplicationVariants至少有一个输出，也可以有多个，所有这里的outputs是一个list集合，遍历它，如果它的名字是以.apk结尾的话，就是我们要修改的文件。
>   
> - variant
>
>   遍历的variant是buildType，比如我们创建一个vip的buildType，再遍历
>
>   ```groovy
>       applicationVariants.all { variant ->
>           variant.outputs.all {
>               println "遍历到一个variant flavorName $variant.name"
>               println "遍历到一个variant versionName $variant.versionName"
>           }
>       }
>   ```
>
>   遍历到一个variant flavorName debug
>   遍历到一个variant versionName 1.0
>   遍历到一个variant flavorName release
>   遍历到一个variant versionName 1.0
>   遍历到一个variant flavorName vip
>   遍历到一个variant versionName 1.0

#### 9.3 动态生成版本信息

> 一般版本号由3部分构成：major.minor.patch，第一个是主版本号，第二个是副版本号，第三个是补丁号。对应1.0.0

##### 9.3.1 最原始方式

> 最开始的时候我们都是配置在build文件下的，例如：
>
> ```groovy
> android {
> 	compileSdkVersion 23
> 	buildToolsVersion "23.0.1"
> 	
> 	defaultConfig {
> 		application "org.xxx.xxx"
> 		minSdkVersion 14
> 		targetSdkVersion 23
> 		versionCode 1
> 		versionName "1.0.0"
> 	}
> }
> ```
>
> 这种方式我们直接写在versionName后面，比较直观，但这个方式有个很大的问题，就是修改不方便，特别是当build文件中有很多代码时，不容易找到，而且修改容易出错。

##### 9.3.2 分模块的方式

> 新建一个version.gradle文件
>
> - ~/version.gradle
>
>   ```groovy
>   ext {
>       appVersionApp = 1
>       appVersionName = "1.0.0"
>   }
>   ```
>
>   ext表明要为当前project创建扩展属性，以供其他脚本引用。它就像我们的变量一样，创建好之后，在build.gradle中引用它。
>
> - app/build.gradle
>
>   ```groovy
>   // 引用上一级目录的version.gradle脚本
>   apply from : '../version.gradle'
>                   
>   android {
>       ...
>       defaultConfig {
>           versionCode appVersionCode
>           versionName appVersionNam
>       }
>   }
>   ```

##### 9.3.3 使用命令行

> gradle提供了执行shell命令非常简便的办法，就是exec。它是一个task任务，可以创建一个继承exec的任务来执行shell命令，但是比较麻烦。更方便的办法是直接在project里使用exec方法：
>
> - build.gradle
>
>   ```groovy
>       exec {
>           println "执行exec"
>           commandLine 'git', 'status'
>       }
>   ```
>   执行exec
>   On branch wangzhiping/dev-v1.0.00
>   Your branch is up to date with 'origin/wangzhiping/dev-v1.0.00'.

#### 9.5 动态配制AndroidManifest文件

> 动态配制AndroidManifest文件，顾名思义就是可以在构建的过程中，动态修改AndroidManifest文件中的一些内容。
>
> - build.gradle
>
>   ```groovy
>   android {
>       defaultConfig {
>           manifestPlaceholders = [
>                   "JPUSH_CHANNEL"  : jpush_channel,
>           ]
>       }
>   }
>   ```
>
> - AndroidManifest.xml
>
>   ````xaml
>   <application
>   		<meta-data
>           android:name="JPUSH_CHANNEL"
>           android:value="${JPUSH_CHANNEL}"/>
>   </application>
>   ````
>
>   如果我们的渠道非常多的话，可以这样操作
>
> - build.gradle
>
>   ```
>   // producFlavor可以理解为策略，例如不同的渠道包
>   // buildType是指开发周期，例如debug和release，对用户来说无感知
>   productFlavor {
>   		google {
>   		                
>   		}
>   		baidu {
>   		                
>   		}
>   		productFlavors.all { flavor ->
>           println "flavor $name"
>           manifestPlaceholders.put("UMENG_CHANNEL", name)
>       }
>   }
>   ```
>
>   Configure project :app
>   flavor develop
>   flavor publish

#### 9.6 自定义你的BuildConfig

> gradle提供了buildConfigField，我们可以自己添加常量到BuildConfig中。
>
> ```groovy
> buildConfigField "boolean", "IS_RELEASE_PACKAGE", "true"
> ```
>
> 它的函数原型是：
>
> ```java
> // 参数1:字段类型
> // 参数2:字段名称
> // 参数3:字段的值钱
> public void buildConfigField(
> 		@Nonable String type,
> 		@Nonable String name,
> 		@Nonable String value){
> 
> }
> ```

#### 9.7 动态添加自定义的资源

> 这里讲的自定义资源，是专门针对res/values类型资源的，它们不光可以在res/values文件夹里使用xml的方式定义，还可以在android gradle里定义，大大增加了构建的灵活性。
>
> - build.gradle
>
>   ```groovy
>   productFlavors {
>       publish {
>           resValue 'string','channel_tips', 'boxPublish'
>       }
>       develop {
>           buildConfigField "boolean", "IS_RELEASE_PACKAGE", "false"
>           resValue 'string', 'channel_tips', 'boxDevelop'
>       }
>   }
>   ```
>
> - build后，自动生成文件：build/generated/res/resValues/develop/debug/values/gradleResValues.xml
>
>   ```xml
>   <?xml version="1.0" encoding="utf-8"?>
>   <resources>
>                   
>       <!-- Automatically generated file. DO NOT MODIFY -->
>                   
>       <!-- Value from product flavor: develop -->
>       <string name="channel_tips" translatable="false">boxDevelop</string>
>                   
>   </resources>
>   ```
>
>   当前build的是develop，所以生成的string是boxDevelop

#### 9.8 编译选项

> 有时候我们需要对我们java源代码的编码、源文件使用的jdk版本进行调优修改。比如需要配制源文件的编码为utf-8的编码，以兼容更多的字符；还比如我们想配制编译java源代码的级别为1.8，这样就可以使用override接口方法的继承等特性。
>
> ```groovy
> android {
> 		// 字段位于gradle.properties
>     compileSdkVersion Integer.parseInt("${CompileSdkVersion}")
>     buildToolsVersion "${BuildToolsVersion}"
> }
> 
> compileOptions {
>   	encoding = 'utf-8'
>     sourceCompatibility JavaVersion.VERSION_1_8
>     targetCompatibility JavaVersion.VERSION_1_8
> 
> ```

#### 9.9 adb 操作选项配制

> ```
> adbOptions {
> 		timeOutInMs = 5 * 1000 //设置超时时间5秒，执行adb命令超时的时间
> 		// 有六个选项
> 		// 1. -l 锁定该应用程序
> 		// 2. -r 替换已经存在的应用程序，也就是强制安装
> 		// 3. -t 允许测试包
> 		// 4. -s 把应用安装到sd卡上
> 		// 5. -d 允许进行降级安装
> 		// 6. -g 为该应用授予所有运行时的权限
> 		installOptions '-r','-s'  
> }
> ```

#### 9.10 DEX选项选择

> android中的java源代码被编译成class字节码后，在打包成apk的时候又会被dx命令优化成android虚拟机可执行的dex文件。dex文件比较紧凑，android做这个dex格式目的是为了让程序在android上运行得更快。对于这些dex文件的生成，android gradle插件已经帮我们做好了，android gradle会调用sdk中的dx命令进行处理。但有时候可能遇到提示内存不足的情况，为什么会提示内存不足呢？其实dx只是一个脚本，它调用的还是java编写的dx.jar库。默认情况下分配给dx的内存是1024m。
>
> - dexOptions
>
>   ```groovy
>   public interface DexOptions {
>     	boolean getIncreamental()
>     	boolean getPreDexLibraries()
>     	boolean getJumboMode()
>     	String getJavaMaxHeapSize()
>     	Integer getThreadCount()
>   }
>   ```
>
>   dexOptions共有5个属性
>
>   1. getIncreamental
>
>      用来配制是否开启dex的增量模式，默认为false。增量模式虽然速度更快一些，但是可能会不工作，要慎用。
>
>   2. javaMaxHeapSize
>
>      执行dx命令分配的最大堆内存，例如可以设置为2g
>
>      ```groovy
>          dexOptions {
>              javaMaxHeapSize "2g"
>          }
>      ```
>
>   3. JumboMode
>
>      用来配制是否开启Jumbo模式，有时候我们的工程量比较大，方法超过了65535个，需要开启jumbo模式才能构建成功。
>
>   4. preDexLibraries
>
>      用来配制是否预执行dex Libraries库工程，开启后会大大提高增量构建的速度，不过可能会影响clean构建的速度。默认值为true。有时候我们需要使用dx的--multi-dex选项生成多个dex，这导致和库工程有冲突的时候，需要将该选项设置为false。
>
>   5. threadCount
>
>      用来配制android gradle运行dx命令时使用的线程数，适当的线程数量可以提高dx的效率。

#### 9.11 突破65535方法数限制

> 为什么有这个限制？
>
> java文件都被打包成一个dex文件，这个文件是被优化过的，dalvik虚拟机的可执行文件，dalvik虚拟机在执行dex文件的时候，使用了short这个类型来索引dex文件中的方法，这就意味着单个dex文件最多可以被定义的方法为65535个。

#### 9.12 自动清理未使用的资源

> gradle为我们提供了shrinkResources，它是一种在构建时，在打包apk之前，会检测所有的资源，看看是否被引用，如果没有，那么这些资源就不会被打包到apk中，因为在这个过程中（构建时），android gradle构建系统会拿到所有的资源，不管是自己的还是第三方的，都一视同仁处理。resource shrinking要结合minifyEnabled使用，目的是减缩代码。
>
> - build.gradle
>
>   ```groovy
>   release {
>       //混淆
>       minifyEnabled true
>       // 移除无用的resource文件
>       shrinkResources true
>   }
>   ```
>
>   Task :app:shrinkDevelopDebugRes
>
> 自动清理未使用的资源这个功能虽然好，但是也会有问题，比如用反射获取的资源文件，静态检测是检测不出来的，这时候就需要用android gradle提供的keep方法来配制哪些资源不被清理。
>
> - res/raw/keep.xml
>
>   ```xml
>   <?xml version="1.0" encoding="utf-8"?>
>   <resources xmlns:tools="http://schemas.android.com/tools"
>       tools:keep="@string/ik_eid_prefix, @string/ik_szlm_proxy" />
>   ```
>
>   keep方法的使用，我们需要新建一个xml文件来配制，文件目录是res/raw/keep.xml，然后通过tools:keep来设置，以逗号分隔多个属性。此外，对于这个keep.xml文件，android gradle构建系统最终打包的时候会清理它，不会把它打包到apk中，除非你在代码里通过R.raw.keep引用了它。
>
> 除了shrinkResource之外，android gradle还为我们提供了一个resConfigs，它属于ProductFlavor的一个方法，可让我们配置哪些资源才被打包进apk中，比如只有中文的资源，只有hdpi格式的图片等。这是非常重要的，比如support library和google play service这两个主要的大库，因为国际化的问题，都支持了几十种语言，但对于app来说，我们不需要那么多语言。
>
> - resConfigs
>
>   ```groovy
>   defaultConfig {
>     	// 只保留中文资源
>       resConfigs "zh"
>   }
>   ```
>
>   

### 10. Android Gradle 多项目构建

> Android的多项目和其他基于gradle构建的多项目差不多，比如java多项目，groovy多项目，它们本身都是gradle多项目构建，唯一的区别是项目本身属性，比如这个项目是java库，那个是android app项目等。

#### 10.1 Android 项目区别

> android项目一般分为库项目、应用项目、测试项目，android gradle根据这些项目分别对应有3种插件，com.android.library、com.android.application、com.android.test。
>
> 库项目一般和java库非常相似，它比java多的是一些android特有的资源配置。一般一些具有公共特性的类，可以抽象成一个库工程，这样它们就可以被其他项目使用。
>
> 应用项目，一般只有一个，可以打包成我们可以发布的apk包，如果工程太复杂，它会引用很多库项目，以便组成一个最终的app发布。

#### 10.2 Android 多项目设置

> 多个项目的设置和gradle的多项目是一样的，android也是基于gradle的，所以项目其实是gradle的概念，项目本身的特性才是每个领域的细分和定义，如android项目、java项目等。
>
> - setting.gradle
>
>   ```groovy
>   include ':app'
>   include ':base-lib'
>   ```
>
>   这样有三个项目，一个app、一个基础库base-lib，一个根project

#### 10.3 库项目引用和配置

> - app/build.gradle
>
>   ```groovy
>   dependencies { 
>   		implementation project(":lib-center")	
>   }
>   ```
>
>   android app也可以引用jar包，java lib打出来的包是jar包，android lib打出来的包是aar包。

#### 10.4 库项目单独发布

> 项目直接依赖一般适用于关联比较紧密，不可复用的项目，对于这类项目我们可以直接基于源代码项目的依赖。有时候一些项目，可以被其他项目复用，比如公共组件库，可以单独发布出去，被其他项目使用，**创建工程时选择Java Library**。
>
> 先在本地搭建nexus私服
>
> 1. wget http://sonatype-download.global.ssl.fastly.net/nexus/3/nexus-3.6.0-02-unix.tar.gz
>
> 2. nexus/nexus-3.6.0-02-unix/nexus-3.6.0-02/bin下
>
>    nexus的注释指向jdk路径，INSTALL4J_JAVA_HOME_OVERRIDE=/usr/local/java/jdk1.8.0_321
>
> 3. ./nexus start
>
> 4. 访问：http://localhost:8081/，通了即成功。
>
> 5. 创建Java Library工程
>
> 6. 项目目录为
>
>    ````
>    ├── custom-plugin
>    │   ├── build.gradle
>    │   ├── libs
>    │   └── src
>    │       └── main
>    │           ├── java
>    │           │   └── com
>    │           │       └── example
>    │           │           └── custom_plugin
>    │           │               └── MyPlugin.kt
>    │           └── resources
>    │               └── META-INF
>    │                   └── gradle-plugins
>    │                       └── my-plugin.properties
>    ````
>
> 7. build.gradle
>
>    ```groovy
>    dependencies {
>        // 应用gradleApi()，才能使用Plugin
>        implementation gradleApi()
>    }
>    ```
>
> 8. resources/META-INF/gradle-plugins/my-plugin.properties
>
>    ```properties
>    /*
>     * 对应com/example/custom_plugin/MyPlugin.kt
>     */
>    implementation-class=com.example.custom_plugin.MyPlugin
>    ```
>
>    其中文件名my-plugin即是发布插件的id
>
> 9. MyPlugin.kt
>
>    ```kotlin
>    package com.example.custom_plugin
>
>    import org.gradle.api.Plugin
>    import org.gradle.api.Project
>
>    class MyPlugin : Plugin<Project>{
>        override fun apply(project: Project) {
>            println("myPlugin被应用")
>            val customTask = project.tasks.create("customTask")
>            customTask.doLast {
>                println("Custom Task do Last")
>            }
>        }
>    }
>    ```
>
> 10. build.gradle
>
>     ```groovy
>     plugins {
>         id 'java-library'
>         id 'org.jetbrains.kotlin.jvm'
>         id 'maven-publish'
>     }
>                 
>     java {
>         sourceCompatibility = JavaVersion.VERSION_1_7
>         targetCompatibility = JavaVersion.VERSION_1_7
>     }
>                 
>     dependencies {
>         implementation gradleApi()
>     }
>                 
>     group 'com.example.plugin'
>     version '1.0.5'
>                 
>     publishing {
>         publications {
>             myPlugin(MavenPublication) {
>                 artifactId = "wzp"
>                 from components.java
>                 groupId = group
>                 version = version
>             }
>         }
>         repositories {
>             maven {
>                 allowInsecureProtocol true
>                 name = "nexus"
>                 url = "http://localhost:8081/repository/maven-releases/"
>                 credentials {
>                     username "admin"
>                     password "admin123"
>                 }
>             }
>         }
>     }
>     ```
>
>     sync成功后，运行publish任务，即可发布到私服。

- 引用刚发布到私库的插件

- 根目录build.gradle

  ```groovy
  buildscript {
      repositories {
          maven {
              allowInsecureProtocol = true
              url 'http://localhost:8081/repository/maven-releases/'
          }
      }
      dependencies {
          classpath 'com.example.plugin:wzp:1.0.5'
      }
  }
  ```

- app/build.gradle

  ```groovy
  apply plugin : 'my-plugin'
  ```

  > Configure project :app
  > myPlugin被应用
  >
  > 并且能够找到插件创建的customTask

### 11. Android Gradle多渠道构建

#### 11.1 多渠道构建的基本原理

> 在Android Gradle中，定义了一个叫Build Variant的概念，直译是构建变体 —— 构建apk的产物。一个Build Variant = BuildType + Product Flavor，BuilType是构建类型，比如release和debug；Product Flavor是构建的渠道，比如Baidu、Google等，它们加起来就是baiduRelease、baiduDebug、googleRelease、googleDebug，公有这几种组合构建产出。Product Flavor也是多渠道构建的基础。
>
> 再或者Product Flavor可以定义为develop、publish，因此就有developDebug、publishDebug、developRelease、publishRelease这几种组合构建产出。

- 新增一个Product Flavor

  ```groovy
      productFlavors {
          google {
  
          }
          baidu {
              
          }
      }
  ```

  android gradle 为我们提供了productFlavors方法来添加不同的渠道，它接受域对象类型的ProductFlavor闭包作为其参数。每一个都是一个ProductFlavor类型的渠道。在NamedDomainObjectContainer中的名字就是渠道名。

  以上的发布渠道配置之后，Android Gradle会生成很多task，基本上都属于BuildType + Product Flavor的方式生成的。比如assembleBaidu运行之后会生成Baidu渠道的release包和debug包，assembleRelease运行后会生成所有渠道的release包，而assembleBaiduRelease运行之后只生成Baidu的release包。除了生成task之外，每个ProductFlavor还可以有自己的SrouceSet，还可以有自己的Dependencis依赖，这意味着我们可以为每个渠道定义它们自己的资源、代码以及依赖的第三方库，这为我们自定义每个渠道提供了很大的灵活性。

#### 11.3 多渠道构建定制

> 多渠道的定制，其实就是对anroid gradle 插件的 ProductFlavor的配置，通过配置ProductFlavor达到灵活控制每一个渠道的目的。

##### 11.3.1 applicationId

> 它是ProductFlavor的属性，用于设置该渠道的包名。
>
> - build.gradle
>
>   ```groovy
>   productFlavors {
>       google {
>           flavorDimensions "1.0.1"
>           applicationId "org.example.google.test"
>       }
>       baidu {
>           flavorDimensions "1.0.0"
>           applicationId "org.example.baidu.test"
>       }
>   }
>   ```
>
>   这样就可以让不同渠道的apk包有不同的包名

##### 11.3.2 consumerProguardFiles

> 既是一个属性，也有一个同名的方法，它只对android库项目起作用，当我们发布库项目生成一个aar包的时候，使用这个属性配置的混淆文件列表也会被打包到aar里一起发布，这样当应用项目引用这个aar包，并启用混淆的时候，会自动使用aar包里的混淆文件对aar包里的代码进行混淆，这样我们就不用对该aar包进行混淆配置了，因为它自带了：
>
> - build.gradle
>
>   ```groovy
>   android {
>   	productFlavors {
>   		google {
>   			consumerProguardFiles 'proguard-rules.pro', 'proguard-android.txt'
>   		}
>   	}
>   }
>   ```
>
>   还有一种是属性设置，属性设置的方式每次都是新的混淆文件列表，以前的会被清空。

##### 11.3.13 dimension

> 有时候，我们想基于不同的标准来构建App，比如免费版还是收费版，x86版还是arm版等。在不考虑BuildType的情况下，这里有4个组合；x86免费、x86收费、arm免费、arm收费版。对于这种情况，我们有两种方式来构建，第一种通俗的用法，就是配置4个ProductFlavor，分别是x86free、x86paid、armfree、armpaid，然后针对这4个ProductFlavor配置，满足我们的需求即可。这种方式比较通俗易懂，但比较冗余，比如free是可以抽象出来的，可以通过多维度控制。
>
> dimension是ProductFlavor的一个属性，接受一个字符串，作为ProductFlavor的维度。可以理解为是对ProductFlavor的分组，比如free和paid可以认为是属于版本（version），而x86和arm属于架构，这样就分成了两组。
>
> - build.gradle
>
>   ```groovy
>   // 先定义dimensions
>   // 最后生成的variant会被如下几个ProductFlavor对象配置
>   // 1. android里的defaultConfig配置，也是一个ProductFlavor
>   // 2. abi维度的ProductFlavor，被dimension配置标记为abi的ProductFlavor
>   // 3. version维度的ProductFlavor，被dimension配置标记为version的ProductFlavor
>   // 维度的优先级非常重要，因为高优先级的flavor会替换掉低优先级的资源、代码、配置等。
>   // 这里优先级为abi > version > defaultConfig
>   flavorDimensions "abi", "version"
>   
>   productFlavors {
>       free {
>           dimension 'version'
>       }
>       paid {
>           dimension 'version'
>       }
>       x86 {
>           dimension 'abi'
>       }
>       arm {
>           dimension 'abi'
>       }
>   }
>   ```
>
>   这样gradle就会根据abi和version的组合，帮我们生成一下variant
>
>   1. ArmFreeDebug
>   2. ArmFreeRelease
>   3. ArmPaidDebug
>   4. ArmPaidRelease
>   5. x86FreeDebug
>   6. x86FreeRelease
>   7. x86PaidDebug
>   8. x86PaidRelease

#### 11.4 提高多渠道构建的效率

