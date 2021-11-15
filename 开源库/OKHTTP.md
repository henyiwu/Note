[toc]

## OKHTTP

### GET/POST流程分析

- get请求#同步

  ```kotlin
     GlobalScope.launch(Dispatchers.IO) {
               val client = OkHttpClient()
               val request = Request.Builder()
                       .url("https://www.baidu.com/?tn=44004473_1_oem_dg")
                       .build()
               val response = client.newCall(request).execute()
               val string = response.body()?.string()
               Log.d("MainActivity", "response $string")
           }
  ```

  - 同步请求源码分析

  - RealCall#execute()

    ```java
    @Override public Response execute() throws IOException {
        synchronized (this) {
          if (executed) throw new IllegalStateException("Already Executed");
          executed = true;
        }
        transmitter.timeoutEnter();
        transmitter.callStart();
        try {
          // 把当前任务加入dispatcer
          client.dispatcher().executed(this);
          // 拦截器开始工作
          return getResponseWithInterceptorChain();
        } finally {
          client.dispatcher().finished(this);
        }
      }
    ```

  - RealCall#getResponseWithInterceptChain()

    ````java
      Response getResponseWithInterceptorChain() throws IOException {
        // Build a full stack of interceptors.
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.addAll(client.interceptors());
        interceptors.add(new RetryAndFollowUpInterceptor(client));
        interceptors.add(new BridgeInterceptor(client.cookieJar()));
        interceptors.add(new CacheInterceptor(client.internalCache()));
        interceptors.add(new ConnectInterceptor(client));
        if (!forWebSocket) {
          interceptors.addAll(client.networkInterceptors());
        }
          // 拦截器链的最后一个拦截器，负责像服务器发送请求，并获取返回数据
        interceptors.add(new CallServerInterceptor(forWebSocket));
        Interceptor.Chain chain = new RealInterceptorChain(interceptors, transmitter, null, 0,
            originalRequest, this, client.connectTimeoutMillis(),
            client.readTimeoutMillis(), client.writeTimeoutMillis());
    
        boolean calledNoMoreExchanges = false;
        try {
          Response response = chain.proceed(originalRequest);
          if (transmitter.isCanceled()) {
            closeQuietly(response);
            throw new IOException("Canceled");
          }
          return response;
        } catch (IOException e) {
          calledNoMoreExchanges = true;
          throw transmitter.noMoreExchanges(e);
        } finally {
          if (!calledNoMoreExchanges) {
            transmitter.noMoreExchanges(null);
          }
        }
      }
    ````

  不管是同步还是异步，最终都会通过责任链完成请求

- get请求异步

  ```kotlin
   val client = OkHttpClient()
               val request = Request.Builder()
                       .url("https://www.baidu.com/?tn=44004473_1_oem_dg")
                       .build()
               client.newCall(request).enqueue(object : Callback {
                   override fun onFailure(call: Call, e: IOException) {
                       Log.d(tag,"onFailure ${e.message}")
                   }
  
                   override fun onResponse(call: Call, response: Response) {
                       // io线程
                       Log.d(tag, "response ${response.body()?.string()}")
                   }
               })
  ```
```
  
  1. 发送http请求，首先构造一个Request对象，Request对象对应一个http请求，参数最少有一个url，此外还可以通过Reuqest.Builder添加参数，例如addHeader()、method("PUT", body)
  2. 通过request的对象去构造一个Call对象，类似于将请求封装成任务，既然是任务，就会有execute、cancel等方法
  3. 构建完任务，通过同步或者异步的方式执行请求，等待任务完成，得到结果
  
  - 异步请求源码分析
  
    RealCall#enqueue()
  
    ```java
    @Override public void enqueue(Callback responseCallback) {
        // 如果这个task已经执行，抛出异常
        synchronized (this) {
          if (executed) throw new IllegalStateException("Already Executed");
          executed = true;
        }
        transmitter.callStart();
        client.dispatcher().enqueue(new AsyncCall(responseCallback));
      }
```

~~~kotlin
Dispatcher#enqueue()
  
```java
void enqueue(AsyncCall call) {
    synchronized (this) {
      readyAsyncCalls.add(call);
      if (!call.get().forWebSocket) {
          // AsyncCall是RealCall的内部类，本质是一个Runnable
        AsyncCall existingCall = findExistingCallWithHost(call.host());
        if (existingCall != null) call.reuseCallsPerHostFrom(existingCall);
      }
    }
    promoteAndExecute();
  }

@Nullable private AsyncCall findExistingCallWithHost(String host) {
    for (AsyncCall existingCall : runningAsyncCalls) {
      if (existingCall.host().equals(host)) return existingCall;
    }
    for (AsyncCall existingCall : readyAsyncCalls) {
      if (existingCall.host().equals(host)) return existingCall;
    }
    return null;
  }

// 判断请求是否超过阈值，有没有可复用的请求
  private boolean promoteAndExecute() {
    assert (!Thread.holdsLock(this));

    List<AsyncCall> executableCalls = new ArrayList<>();
    boolean isRunning;
    synchronized (this) {
      for (Iterator<AsyncCall> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
        AsyncCall asyncCall = i.next();

        if (runningAsyncCalls.size() >= maxRequests) break;
          // Max capacity 64.
        if (asyncCall.callsPerHost().get() >= maxRequestsPerHost) continue;
          // Host max capacity 5.

        i.remove();
          // callsPerHost()可以复用相同的请求
        asyncCall.callsPerHost().incrementAndGet();
        executableCalls.add(asyncCall);
          // 把call从就绪队列加入运行队列
        runningAsyncCalls.add(asyncCall);
      }
      isRunning = runningCallsCount() > 0;
    }

    for (int i = 0, size = executableCalls.size(); i < size; i++) {
      AsyncCall asyncCall = executableCalls.get(i);
      asyncCall.executeOn(executorService());
    }

    return isRunning;
  }
```
  
AsyncCall#executeOn(ExecutorService executorService)
  
```java
// 将call放入线程池执行
 void executeOn(ExecutorService executorService) {
      assert (!Thread.holdsLock(client.dispatcher()));
      boolean success = false;
      try {
        executorService.execute(this);
        success = true;
      } catch (RejectedExecutionException e) {
        InterruptedIOException ioException = new InterruptedIOException("executor rejected");
        ioException.initCause(e);
        transmitter.noMoreExchanges(ioException);
        responseCallback.onFailure(RealCall.this, ioException);
      } finally {
        if (!success) {
          client.dispatcher().finished(this); // This call is no longer running!
        }
      }
    }
```
~~~

- post请求

  ```kotlin
           GlobalScope.launch(Dispatchers.IO) {
               val client = OkHttpClient()
               val body = FormBody.Builder()
                       .add("name", "wzp")
                       .build()
               val request = Request.Builder()
                       .url("https://www.baidu.com/?tn=44004473_1_oem_dg")
                       .post(body)
                       .build()
               client.newCall(request).enqueue(object : Callback {
                   override fun onFailure(call: Call, e: IOException) {
                       Log.d(tag,"onFailure ${e.message}")
                   }
  
                   override fun onResponse(call: Call, response: Response) {
                       Log.d(tag, "response ${response.body()?.byteStream()}")
                   }
               })
           }
  ```

- 支持文件上传

  ```java
      private Request buildMultipartFormRequest(String url, File[] files,
                                                String[] fileKeys, Param[] params){
          params = validateParam(params);
          MultipartBuilder builder = new MultipartBuilder()
                  .type(MultipartBuilder.FORM);
          for (Param param : params){
              builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + param.key + "\""),
              RequestBody.create(null, param.value));
          }
          if (files != null){
              RequestBody fileBody = null;
              for (int i = 0; i < files.length; i++){
                  File file = files[i];
                  String fileName = file.getName();
                  fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
                  //TODO 根据文件名设置contentType
                  builder.addPart(Headers.of("Content-Disposition",
                          "form-data; name=\"" + fileKeys[i] + "\"; filename=\"" + fileName + "\""),
                          fileBody);
              }
          }
  
          RequestBody requestBody = builder.build();
          return new Request.Builder()
                  .url(url)
                  .post(requestBody)
                  .build();
      }
  ```

### 重要的类

1. Dispatcher：调度器，有两个重要的方法，enqueue()和execute()，分别标识异步任务和同步任务，把当前任务送入运行队列。

2. RealCall和AsyncCall，真正执行请求的位置（最终都开始执行拦截器）

   ```java
   // 异步：AsyncCall本质是一个Runnable，真正执行的位置在Dispatcher中promoteAndExecute()
   asyncCall.executeOn(executorService());
   
   @Override protected void execute() {
         boolean signalledCallback = false;
         transmitter.timeoutEnter();
         try {
             // 拦截器开始工作
           Response response = getResponseWithInterceptorChain();
           signalledCallback = true;
           responseCallback.onResponse(RealCall.this, response);
         } catch (IOException e) {
           if (signalledCallback) {
             // Do not signal the callback twice!
             Platform.get().log(INFO, "Callback failure for " + toLoggableString(), e);
           } else {
             responseCallback.onFailure(RealCall.this, e);
           }
         } catch (Throwable t) {
           cancel();
           if (!signalledCallback) {
             IOException canceledException = new IOException("canceled due to " + t);
             canceledException.addSuppressed(t);
             responseCallback.onFailure(RealCall.this, canceledException);
           }
           throw t;
         } finally {
           client.dispatcher().finished(this);
         }
       }
     }
   
   //同步：RealCall#execute()
   @Override public Response execute() throws IOException {
       synchronized (this) {
         if (executed) throw new IllegalStateException("Already Executed");
         executed = true;
       }
       transmitter.timeoutEnter();
       transmitter.callStart();
       try {
         client.dispatcher().executed(this);
         // 拦截器开始工作
         return getResponseWithInterceptorChain();
       } finally {
         client.dispatcher().finished(this);
       }
     }
   ```

3. 拦截器

   拦截器按添加的顺序执行

   ```java
   interceptors.addAll(client.interceptors()); // 用户自定义的拦截器，最早执行
   interceptors.add(new RetryAndFollowUpInterceptor(client));
   interceptors.add(new BridgeInterceptor(client.cookieJar()));
   interceptors.add(new CacheInterceptor(client.internalCache()));
   interceptors.add(new ConnectInterceptor(client));
   if (!forWebSocket) {
     interceptors.addAll(client.networkInterceptors());
   }
   interceptors.add(new CallServerInterceptor(forWebSocket));
   ```

4. OkHttpClient

   OkHttp使用入口，封装了Dispatcher使用户无感知，Call任务的工厂，官方建议把OkHttpClient设计为单例模式，这样可以减少连接、重复请求，即复用了OkHttpClient内部的连接池和线程池

### 拦截器分析

> 拦截器是OkHttp的精华，是网络请求真正发起的地方

- RealCall#getResponseWithInterceptorChain()

  ```java
    Response getResponseWithInterceptorChain() throws IOException {
      // Build a full stack of interceptors.
      List<Interceptor> interceptors = new ArrayList<>();
      interceptors.addAll(client.interceptors());
      interceptors.add(new RetryAndFollowUpInterceptor(client));
      interceptors.add(new BridgeInterceptor(client.cookieJar()));
      interceptors.add(new CacheInterceptor(client.internalCache()));
      interceptors.add(new ConnectInterceptor(client));
      if (!forWebSocket) {
        interceptors.addAll(client.networkInterceptors());
      }
      interceptors.add(new CallServerInterceptor(forWebSocket));
  
      Interceptor.Chain chain = new RealInterceptorChain(interceptors, transmitter, null, 0,
          originalRequest, this, client.connectTimeoutMillis(),
          client.readTimeoutMillis(), client.writeTimeoutMillis());
  
      boolean calledNoMoreExchanges = false;
      try {
        Response response = chain.proceed(originalRequest);
        if (transmitter.isCanceled()) {
          closeQuietly(response);
          throw new IOException("Canceled");
        }
        return response;
      } catch (IOException e) {
        calledNoMoreExchanges = true;
        throw transmitter.noMoreExchanges(e);
      } finally {
        if (!calledNoMoreExchanges) {
          transmitter.noMoreExchanges(null);
        }
      }
    }
  ```

  异步、同步请求，最终都会调用getResponseWithInterceptorChain()

  ```java
  // chain是RealInterceptorChain
  Response response = chain.proceed(originalRequest);
  ```

  执行完这句话，最终得到结果，说明这段代码走完了完整的责任链

- chain是RealInterceptorChain#process()

  ```java
  public Response proceed(Request request, Transmitter transmitter, @Nullable Exchange exchange)
        throws IOException {
      if (index &gt;= interceptors.size()) throw new AssertionError();
  
      calls++;
  
      // If we already have a stream, confirm that the incoming request will use it.
      if (this.exchange != null &amp;&amp; !this.exchange.connection().supportsUrl(request.url())) {
        throw new IllegalStateException(&quot;network interceptor &quot; + interceptors.get(index - 1)
            + &quot; must retain the same host and port&quot;);
      }
  
      // If we already have a stream, confirm that this is the only call to chain.proceed().
      if (this.exchange != null &amp;&amp; calls &gt; 1) {
        throw new IllegalStateException(&quot;network interceptor &quot; + interceptors.get(index - 1)
            + &quot; must call proceed() exactly once&quot;);
      }
  
      // 执行下一个块
      // Call the next interceptor in the chain.
      RealInterceptorChain next = new RealInterceptorChain(interceptors, transmitter, exchange,
          index + 1, request, call, connectTimeout, readTimeout, writeTimeout);
      Interceptor interceptor = interceptors.get(index);
      Response response = interceptor.intercept(next);
  
      // Confirm that the next interceptor made its required call to chain.proceed().
      if (exchange != null &amp;&amp; index + 1 &lt; interceptors.size() &amp;&amp; next.calls != 1) {
        throw new IllegalStateException(&quot;network interceptor &quot; + interceptor
            + &quot; must call proceed() exactly once&quot;);
      }
  
      // Confirm that the intercepted response isn&#39;t null.
      if (response == null) {
        throw new NullPointerException(&quot;interceptor &quot; + interceptor + &quot; returned null&quot;);
      }
  
      if (response.body() == null) {
        throw new IllegalStateException(
            &quot;interceptor &quot; + interceptor + &quot; returned a response with no body&quot;);
      }
  
      return response;
    }
  ```

  其中，这块代码执行了责任链的下一个块

- interceptor.intercept(next)

  ```java
  RealInterceptorChain next = new RealInterceptorChain(interceptors, transmitter, exchange,
          index + 1, request, call, connectTimeout, readTimeout, writeTimeout);
      Interceptor interceptor = interceptors.get(index);
      Response response = interceptor.intercept(next);
  ```

  这里假设下一个块是BridgeInterceptor

- BridgeInterceptor#interceptor()

  ```java
  @Override public Response intercept(Chain chain) throws IOException {
      Request userRequest = chain.request();
      Request.Builder requestBuilder = userRequest.newBuilder();
  
      RequestBody body = userRequest.body();
      if (body != null) {
        MediaType contentType = body.contentType();
        if (contentType != null) {
          requestBuilder.header("Content-Type", contentType.toString());
        }
  
        long contentLength = body.contentLength();
        if (contentLength != -1) {
          requestBuilder.header("Content-Length", Long.toString(contentLength));
          requestBuilder.removeHeader("Transfer-Encoding");
        } else {
          requestBuilder.header("Transfer-Encoding", "chunked");
          requestBuilder.removeHeader("Content-Length");
        }
      }
  
      if (userRequest.header("Host") == null) {
        requestBuilder.header("Host", hostHeader(userRequest.url(), false));
      }
  
      if (userRequest.header("Connection") == null) {
        requestBuilder.header("Connection", "Keep-Alive");
      }
  
      // If we add an "Accept-Encoding: gzip" header field we're responsible for also decompressing
      // the transfer stream.
      boolean transparentGzip = false;
      if (userRequest.header("Accept-Encoding") == null && userRequest.header("Range") == null) {
        transparentGzip = true;
        requestBuilder.header("Accept-Encoding", "gzip");
      }
  
      List<Cookie> cookies = cookieJar.loadForRequest(userRequest.url());
      if (!cookies.isEmpty()) {
        requestBuilder.header("Cookie", cookieHeader(cookies));
      }
  
      if (userRequest.header("User-Agent") == null) {
        requestBuilder.header("User-Agent", Version.userAgent());
      }
  
      Response networkResponse = chain.proceed(requestBuilder.build());
  
      HttpHeaders.receiveHeaders(cookieJar, userRequest.url(), networkResponse.headers());
  
      Response.Builder responseBuilder = networkResponse.newBuilder()
          .request(userRequest);
  
      if (transparentGzip
          && "gzip".equalsIgnoreCase(networkResponse.header("Content-Encoding"))
          && HttpHeaders.hasBody(networkResponse)) {
        GzipSource responseBody = new GzipSource(networkResponse.body().source());
        Headers strippedHeaders = networkResponse.headers().newBuilder()
            .removeAll("Content-Encoding")
            .removeAll("Content-Length")
            .build();
        responseBuilder.headers(strippedHeaders);
        String contentType = networkResponse.header("Content-Type");
        responseBuilder.body(new RealResponseBody(contentType, -1L, Okio.buffer(responseBody)));
      }
  
      return responseBuilder.build();
    }
  ```

  这个拦截器拼接了一些请求头参数，其中有一句chain.proceed(requestBuilder.build())，又回到了RealInterceptorChain的proceed()，并且request已经是处理过之后的request，此时在RealInterceptorChain#proceed()中，index + 1，即会继续处理下一个责任块。

  总结，责任链的处理顺序：RealInterceptorChain#process()->块A->RealInterceptorChain#process()->块B......直到责任链最后一块CallServerInterceptor，没有继续往下传递事件，获得服务器返回的结果。

#### 自定义拦截器

- 添加自定义拦截器

  ```kotlin
  val client = OkHttpClient.Builder()
                   .addInterceptor(MyInterceptor())
                   .build()
  ```

  自定义的拦截器，执行顺序在所有默认拦截器之前，从源码可以看出：

- 拦截器添加顺序

  ```java
  Response getResponseWithInterceptorChain() throws IOException {
      List<Interceptor> interceptors = new ArrayList<>();
      // 获取自定义拦截器
      interceptors.addAll(client.interceptors());
      interceptors.add(new RetryAndFollowUpInterceptor(client));
      interceptors.add(new BridgeInterceptor(client.cookieJar()));
      interceptors.add(new CacheInterceptor(client.internalCache()));
      interceptors.add(new ConnectInterceptor(client));
      if (!forWebSocket) {
        interceptors.addAll(client.networkInterceptors());
      }
      interceptors.add(new CallServerInterceptor(forWebSocket));
  
      Interceptor.Chain chain = new RealInterceptorChain(interceptors, transmitter, null, 0,
          originalRequest, this, client.connectTimeoutMillis(),
          client.readTimeoutMillis(), client.writeTimeoutMillis());
          ......
  }
  ```
  
- 自定义一个日志拦截器

  ```kotlin
  OkHttpClient.builder.addInterceptor(HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                      override fun log(message: String) {
                          ComponentCenter.doLog("okHttp: $message")
                      }
                  })
  ```