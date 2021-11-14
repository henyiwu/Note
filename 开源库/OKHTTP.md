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

### 拦截器分析

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