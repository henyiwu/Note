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
    
    1. 发送http请求，首先构造一个Request对象，Request对象对应一个http请求，参数最少有一个url，此外还可以通过Reuqest.Builder添加参数，例如addHeader()、method("PUT", body)
      2. 通过request的对象去构造一个Call对象，类似于将请求封装成任务，既然是任务，就会有execute、cancel等方法
      3. 构建完任务，通过同步或者异步的方式执行请求，等待任务完成，得到结果，不管是同步还是异步，最终都会通过责任链完成请求
    
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

  - Dispatcher#enqueue()

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

#### RetryAndFollowUpInterceptor

> 重试和重定向连接器

- intercept

  ```kotlin
  @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
      val realChain = chain as RealInterceptorChain
      var request = chain.request
      val call = realChain.call
      var followUpCount = 0
      var priorResponse: Response? = null
      var newExchangeFinder = true
      var recoveredFailures = listOf<IOException>()
      while (true) {
        // 创建ExchangeFilder对象，从连接池获取可复用的链接
        call.enterNetworkInterceptorExchange(request, newExchangeFinder)
  
        var response: Response
        var closeActiveExchange = true
        try {
          if (call.isCanceled()) {
            throw IOException("Canceled")
          }
  
          try {
            // 执行下一个责任链
            response = realChain.proceed(request)
            newExchangeFinder = true
          } catch (e: RouteException) {
            // 与服务器建立连接失败，如果是可恢复的（根据recover方法判断）
            // 则尝试重新建立连接
            if (!recover(e.lastConnectException, call, request, requestSendStarted = false)) {
              throw e.firstConnectException.withSuppressed(recoveredFailures)
            } else {
              recoveredFailures += e.firstConnectException
            }
            newExchangeFinder = false
            continue
          } catch (e: IOException) {
            if (!recover(e, call, request, requestSendStarted = e !is ConnectionShutdownException)) {
              throw e.withSuppressed(recoveredFailures)
            } else {
              recoveredFailures += e
            }
            newExchangeFinder = false
            continue
          }
  
          // Attach the prior response if it exists. Such responses never have a body.
          if (priorResponse != null) {
            response = response.newBuilder()
                .priorResponse(priorResponse.newBuilder()
                    .body(null)
                    .build())
                .build()
          }
  
          val exchange = call.interceptorScopedExchange
          // 根据返回的状态码，确定是否需要重定向，不需要返回null
          val followUp = followUpRequest(response, exchange)
  
          if (followUp == null) {
            if (exchange != null && exchange.isDuplex) {
              call.timeoutEarlyExit()
            }
            closeActiveExchange = false
            return response
          }
  
          val followUpBody = followUp.body
          if (followUpBody != null && followUpBody.isOneShot()) {
            closeActiveExchange = false
            return response
          }
  
          response.body?.closeQuietly()
  
          // 重定向次数超过最大限制抛异常
          if (++followUpCount > MAX_FOLLOW_UPS) {
            throw ProtocolException("Too many follow-up requests: $followUpCount")
          }
  
          request = followUp
          priorResponse = response
        } finally {
          // ExchangeFinder置空和退出连接
          call.exitNetworkInterceptorExchange(closeActiveExchange)
        }
      }
    }
  ```

#### BridgeInterceptor

> 在request阶段：配置请求头信息，cookie信息
>
> 在response阶段：处理并保存返回的cookie信息，判断是否需要gzip压缩等

- BridgeInterceptor

  ```kotlin
  @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
      val userRequest = chain.request()
      val requestBuilder = userRequest.newBuilder()
  
      // 配置请求头信息
      val body = userRequest.body
      if (body != null) {
        val contentType = body.contentType()
        if (contentType != null) {
          requestBuilder.header("Content-Type", contentType.toString())
        }
  
        val contentLength = body.contentLength()
        if (contentLength != -1L) {
          requestBuilder.header("Content-Length", contentLength.toString())
          requestBuilder.removeHeader("Transfer-Encoding")
        } else {
          requestBuilder.header("Transfer-Encoding", "chunked")
          requestBuilder.removeHeader("Content-Length")
        }
      }
  
      if (userRequest.header("Host") == null) {
        requestBuilder.header("Host", userRequest.url.toHostHeader())
      }
  
      if (userRequest.header("Connection") == null) {
        requestBuilder.header("Connection", "Keep-Alive")
      }
  
      // If we add an "Accept-Encoding: gzip" header field we're responsible for also decompressing
      // the transfer stream.
      var transparentGzip = false
      if (userRequest.header("Accept-Encoding") == null && userRequest.header("Range") == null) {
        transparentGzip = true
        requestBuilder.header("Accept-Encoding", "gzip")
      }
  
      // 配置cookie信息
      val cookies = cookieJar.loadForRequest(userRequest.url)
      if (cookies.isNotEmpty()) {
        requestBuilder.header("Cookie", cookieHeader(cookies))
      }
  
      if (userRequest.header("User-Agent") == null) {
        requestBuilder.header("User-Agent", userAgent)
      }
  
      val networkResponse = chain.proceed(requestBuilder.build())
  
      // 保存cookie信息
      cookieJar.receiveHeaders(userRequest.url, networkResponse.headers)
  
      val responseBuilder = networkResponse.newBuilder()
          .request(userRequest)
  
      // 解压处理
      if (transparentGzip &&
          "gzip".equals(networkResponse.header("Content-Encoding"), ignoreCase = true) &&
          networkResponse.promisesBody()) {
        val responseBody = networkResponse.body
        if (responseBody != null) {
          val gzipSource = GzipSource(responseBody.source())
          val strippedHeaders = networkResponse.headers.newBuilder()
              .removeAll("Content-Encoding")
              .removeAll("Content-Length")
              .build()
          responseBuilder.headers(strippedHeaders)
          val contentType = networkResponse.header("Content-Type")
          responseBuilder.body(RealResponseBody(contentType, -1L, gzipSource.buffer()))
        }
      }
  
      return responseBuilder.build()
    }
  ```

#### CacheInterceptor

> 缓存拦截器

- CacheInterceptor

  ```java
  @Override public Response intercept(Chain chain) throws IOException {
    // 如果配置了缓存，则从缓存中取一次，不一定能取到
      Response cacheCandidate = cache != null
          ? cache.get(chain.request())
          : null;
  
      long now = System.currentTimeMillis();
  
    // 工厂模式，返回缓存策略
      CacheStrategy strategy = new CacheStrategy.Factory(now, chain.request(), cacheCandidate).get();
      Request networkRequest = strategy.networkRequest;
      Response cacheResponse = strategy.cacheResponse;
  
    // 缓存检测
      if (cache != null) {
        cache.trackResponse(strategy);
      }
  
      if (cacheCandidate != null && cacheResponse == null) {
        closeQuietly(cacheCandidate.body()); // The cache candidate wasn't applicable. Close it.
      }
  
      // 禁止使用网络且缓存无效，直接返回，504:网关错误
      if (networkRequest == null && cacheResponse == null) {
        return new Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(504)
            .message("Unsatisfiable Request (only-if-cached)")
            .body(Util.EMPTY_RESPONSE)
            .sentRequestAtMillis(-1L)
            .receivedResponseAtMillis(System.currentTimeMillis())
            .build();
      }
  
      // 缓存有效，不使用网络，返回缓存数据
      if (networkRequest == null) {
        return cacheResponse.newBuilder()
            .cacheResponse(stripBody(cacheResponse))
            .build();
      }
  
    // 缓存无效，执行下一个拦截器
      Response networkResponse = null;
      try {
        networkResponse = chain.proceed(networkRequest);
      } finally {
        // If we're crashing on I/O or otherwise, don't leak the cache body.
        if (networkResponse == null && cacheCandidate != null) {
          closeQuietly(cacheCandidate.body());
        }
      }
  
      // 本地有缓存，根据条件选择哪个响应
      if (cacheResponse != null) {
        if (networkResponse.code() == HTTP_NOT_MODIFIED) {
          Response response = cacheResponse.newBuilder()
              .headers(combine(cacheResponse.headers(), networkResponse.headers()))
              .sentRequestAtMillis(networkResponse.sentRequestAtMillis())
              .receivedResponseAtMillis(networkResponse.receivedResponseAtMillis())
              .cacheResponse(stripBody(cacheResponse))
              .networkResponse(stripBody(networkResponse))
              .build();
          networkResponse.body().close();
  
          // Update the cache after combining headers but before stripping the
          // Content-Encoding header (as performed by initContentStream()).
          cache.trackConditionalCacheHit();
          cache.update(cacheResponse, response);
          return response;
        } else {
          closeQuietly(cacheResponse.body());
        }
      }
  
    // 使用网络响应
      Response response = networkResponse.newBuilder()
          .cacheResponse(stripBody(cacheResponse))
          .networkResponse(stripBody(networkResponse))
          .build();
  
    // 缓存到本地
      if (cache != null) {
        if (HttpHeaders.hasBody(response) && CacheStrategy.isCacheable(response, networkRequest)) {
          // Offer this request to the cache.
          CacheRequest cacheRequest = cache.put(response);
          return cacheWritingResponse(cacheRequest, response);
        }
  
        if (HttpMethod.invalidatesCache(networkRequest.method())) {
          try {
            cache.remove(networkRequest);
          } catch (IOException ignored) {
            // The cache cannot be written.
          }
        }
      }
  
      return response;
    }
  ```

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

### Dns

> OkHttp支持配置自己的Dns解析服务器
>
> 构建OkHttpClient时，调用接口获取host和对应的ip，在自定义的Dns（接口）根据host获取对应的ip，如果没有获取到则调用系统默认的dns实现方式
>
> 如果一个host对应多个ip，则ping各个id，选择最快的ip访问

- Dns

  ```java
  public interface Dns {
    /**
     * A DNS that uses {@link InetAddress#getAllByName} to ask the underlying operating system to
     * lookup IP addresses. Most custom {@link Dns} implementations should delegate to this instance.
     */
    Dns SYSTEM = new Dns() {
      @Override public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        if (hostname == null) throw new UnknownHostException("hostname == null");
        try {
          return Arrays.asList(InetAddress.getAllByName(hostname));
        } catch (NullPointerException e) {
          UnknownHostException unknownHostException =
              new UnknownHostException("Broken system behaviour for dns lookup of " + hostname);
          unknownHostException.initCause(e);
          throw unknownHostException;
        }
      }
    };
  
    /**
     * Returns the IP addresses of {@code hostname}, in the order they will be attempted by OkHttp. If
     * a connection to an address fails, OkHttp will retry the connection with the next address until
     * either a connection is made, the set of IP addresses is exhausted, or a limit is exceeded.
     */
    List<InetAddress> lookup(String hostname) throws UnknownHostException;
  }
  ```

- 使用

  ```java
  clientBuilder.dns(new MyDns()).build();
  ```

- MyDns

  ```java
  public class MyDns implements Dns {
  
      @Override
      public List<InetAddress> lookup(String hostname) throws UnknownHostException {
          Map<String, String> hostIpMap = AppPingHelper.getInstance().getHostIpMap();
          DnsConfigHelper configHelper = DnsConfigHelper.getInstance();
          if (!configHelper.enableFromServer() || !configHelper.isEnableDNS()) {
              hostIpMap.clear();
              return Dns.SYSTEM.lookup(hostname);
          }
        	// 获取自定义的dns服务器ip
          String ip = hostIpMap.get(hostname);
          if (!TextUtils.isEmpty(ip)) {
            	// 如果获取到了，则返回作为dns服务器地址
              return Collections.singletonList(InetAddress.getByName(ip));
          }
        	// 如果没有获取到，调用系统默认的dns服务
          return Dns.SYSTEM.lookup(hostname);
      }
  }
  ```

- RouteSelector#resetNextInetSocketAddress

  ```java
  private void resetNextInetSocketAddress(Proxy proxy) throws IOException {
    if (proxy.type() == Proxy.Type.SOCKS) {
      inetSocketAddresses.add(InetSocketAddress.createUnresolved(socketHost, socketPort));
    } else {
      eventListener.dnsStart(call, socketHost);
  
      // Try each address for best behavior in mixed IPv4/IPv6 environments.
      List<InetAddress> addresses = address.dns().lookup(socketHost);
      if (addresses.isEmpty()) {
        throw new UnknownHostException(address.dns() + " returned no addresses for " + socketHost);
      }
  
      eventListener.dnsEnd(call, socketHost, addresses);
  
      for (int i = 0, size = addresses.size(); i < size; i++) {
        InetAddress inetAddress = addresses.get(i);
        inetSocketAddresses.add(new InetSocketAddress(inetAddress, socketPort));
      }
    }
  }
  ```

  

