[toc]

## Retrofit

### 在有okhttp的情况下，为什么还要开发retrofit？

> 使用okhttp网络请求到数据展示流程：
>
> 1. 构建OkhttpClient实例
> 2. 构建Request
> 3. 构建请求Call
> 4. 手动添加适配数据
> 5. 手动实现线程切换
> 6. 展示
>
> okhttp使用过程中的缺陷：
>
> 1. 用户网络请求的接口配置繁琐，尤其是需要配置复杂请求body，请求头，参数的时候
> 2. 数据解析过程需要用户手动拿到responsebody进行解析，不能复用
> 3. 无法适配自动进行线程切换
> 4. 容易陷入回调陷阱

### retrofit是什么

> Retrofit是一个RESTful的HTTP网络请求框架的封装
> 原因：网络请求的工作本质是OkHttp完成，而Retrofit仅负责网络请求接口的封装
>
> app应用程序通过Retrofit请求网络，实际上是使用Retrofit接口层封装的参数、Header、Url等信息，之后由OkHttp完成后续的请求操作
>
> 在服务端返回数据之后，OkHttp将原始的结果交给Retrofit，Retrofit根据用户的需求对结果进行解析

### retrofit的作用

> 1. 更好地创建okhttp请求
> 2. 数据返回后更好地解析

### retrofit封装的点

> 1. 构建蓝色的Request的方案，retrofit是通过注解来进行适配
> 2. 适配Call的过程中，retrofit是利用Adapter适配的OkHttp的Call，为call的适配提供了多样性
> 3. 相对okhttp，retrofit会对responseBody进行自动的Gson解析，提供了可复用，易拓展的数据解析方案
> 4. 相对okhttp，retrofit会自动完成线程的切换

### 架构思路

#### 建造者模式

> 构建过程做的工作：
>
> 建立一个Retrofit对象的标准：配置好Retrofit类里成员变量
>
> baseUrl：网络请求的地址
>
> callFactroy：网络请求的工厂
>
> callbackExecutor：回调方法执行器
>
> adapterFactories：网络请求适配器工厂的集合
>
> converterFactories：数据转换工厂的集合

- build()

  ```kotlin
  private val retrofit = Retrofit.Builder()
          .baseUrl(BASE_URL)
          .addConverterFactory(GsonConverterFactory.create())
          .build()
  ```

- build()源代码

  ```java
      public Retrofit build() {
        if (baseUrl == null) {
          throw new IllegalStateException("Base URL required.");
        }
  
        okhttp3.Call.Factory callFactory = this.callFactory;
        // 只支持okhttp请求，不支持httpclient、httpUrlConnection等
        if (callFactory == null) {
          callFactory = new OkHttpClient();
        }
  
        // 添加一个线程管理Executor，callbackExecutor是handler，用于线程切换
        Executor callbackExecutor = this.callbackExecutor;
        if (callbackExecutor == null) {
          callbackExecutor = platform.defaultCallbackExecutor();
        }
  
        // Make a defensive copy of the adapters and add the default Call adapter.
        List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>(this.callAdapterFactories);
        callAdapterFactories.addAll(platform.defaultCallAdapterFactories(callbackExecutor));
  
        // Make a defensive copy of the converters.
        List<Converter.Factory> converterFactories =
            new ArrayList<>(
                1 + this.converterFactories.size() + platform.defaultConverterFactoriesSize());
  
        // Add the built-in converter factory first. This prevents overriding its behavior but also
        // ensures correct behavior when using converters that consume all types.
        converterFactories.add(new BuiltInConverters());
        converterFactories.addAll(this.converterFactories);
        converterFactories.addAll(platform.defaultConverterFactories());
  
        return new Retrofit(
            callFactory,
            baseUrl,
            unmodifiableList(converterFactories),
            unmodifiableList(callAdapterFactories),
            callbackExecutor,
            validateEagerly);
      }
  ```