[toc]

## Bitmap

> Bitmap在安卓中可以理解为图片，由像素点组成，每个像素点有它的颜色信息和编码大小，以不同方式加载Bitmap占用的内存大小不同，尤其注意**一张图片在属性中显示的大小和加载到内存中的大小不是一回事。**

### 1. 如何加载一个Bitmap

> 通过系统提供的解码方法，可以选择加载一张png、jpg或者其他常见格式的图片，解码方法有以下四种：
>
> 1. decodeFile：从文件系统加载出一个Bitmap对象
> 2. decodeResource：从资源加载一个Bitmap对象
> 3. decodeStream：从输入流加载一个Bitmap对象
> 4. decodeByteArray：从字节数组中加载一个Bitmap对象
>
> 这四个方法最终在Android底层实现，对应BitmapFactory类的几个native方法

### 2. Bitmap的颜色配置信息与压缩方式

![](D:\Note\PicBed\Bitmap_config.jpg)

<center>图 1-1</center>
#### 2.1 Bitmap.Config

> Config类是Bitmap中的枚举类，用于定义每个Bitmap实例的配置信息，主要包括
>
> 1. ARGB_8888
>
>    >Android默认使用标准，占用内存最高的存储方案，图片质量最佳。A,R,G,B四个通道各占用8位，（每个像素点）共32位即4个字节。
>
> 2. ARGB_4444
>
>    > **比较不推荐的方案**，A,R,G,B四个通道各占用4位，（每个像素点）共16位即2个字节。
>
> 3. RGB_565
>
>    > R通道占用5字节，G通道占用6字节，B通道占用5字节，一共16位即2字节，**如果图片不要求透明度，建议采用这种存储方式。**
>
> 4. ALPHA_8
>
>    > ALPHA通道占用8位即1字节。
>>
> > 每种模式图片占用内存大小 = 像素长 * 像素宽 * 占用字节
> > 例如ARGB_8888的1080*1080的图片加载到内存中占用：1080 * 1080 * 4bit = 4M
>>
> > **值得注意的是，在指定Config后Bitmap的编码方式也不一定生效，系统可能全部采用ARGB_8888编码**

#### 2.2 压缩方式

> 通过改变BitmapFactory.Options中inSampleSize的值来控制图片的缩放系数，**注意inSampleSize的值只能是2的n次方**
>
> 以图片1024*1024，Config为ARGB_8888为例，改变其inSampleSize值：
>
> | inSampleSize | 占用内存                  |
> | ------------ | ------------------------- |
> | 1            | 1024 * 1024 * 4 bit = 4M  |
> | 2            | 512 * 512 * 4 bit = 1M    |
> | 4            | 256 * 256 * 4 bit = 256KB |
>
> 如果inSampleSize取值不为2的n次方，则向下取整至2的n次方数

- 控制inSampleSize

  ```kotlin
  // 计算出合适的inSampleSize值
      private fun calculateInSampleSize(options: BitmapFactory.Options, reqW: Int, reqH: Int): Int{
          val halfHeight = options.outHeight / 2
          val halfWidth = options.outWidth / 2
          var inSampleSize = 1
          while ((halfHeight / inSampleSize) >= reqH && (halfWidth / inSampleSize) >= reqW) {
              inSampleSize *= 2
          }
          return inSampleSize
      }
  ```
  
  加入ImageView的尺寸为200 * 200，有一张图片为500 * 500，如果inSampleSize=2，图片变为250*250，如果为4，图片为125 * 125，如果把125图片放在ImageView上会导致图片被拉伸，所以在开发中建议把图片压缩至尺寸比控件大的最小尺寸，当然这不是硬性规定，只是一个建议。
  
- 获取采样率(inSampleSize)

  ```kotlin
  val options = BitmapFactory.Options()
  // 调用decode方法只解析图片的宽高信息，不分配内存加载图片
  options.inJustDecodeBounds = true
  BitmapFactory.decodeResource(res, id, options)
  // inSampleSize:缩放系数，只能取2的n次方
  options.inSampleSize = calculateInSampleSize(options, reqW, reqH)
  if (!hasAlpha) {
      // 没有alpha值，用RGB565，每个像素只占用2个字节
      options.inPreferredConfig = Bitmap.Config.RGB_565
  }
  options.inJustDecodeBounds = false
  ```

- 完整代码

  ```kotlin
  object ImageResize {
  
      fun decodeSampleBitmapFromResource(res: Resources, id: Int, reqW: Int, reqH: Int, hasAlpha: Boolean): Bitmap? {
          val options = BitmapFactory.Options()
          // 只读参数，不加载图片
          options.inJustDecodeBounds = true
          BitmapFactory.decodeResource(res, id, options)
          // inSampleSize:缩放系数，只能取2的n次方
          options.inSampleSize = calculateInSampleSize(options, reqW, reqH)
          if (!hasAlpha) {
              // 没有alpha值，用RGB565，每个像素只占用2个字节
              options.inPreferredConfig = Bitmap.Config.RGB_565
          }
          options.inJustDecodeBounds = false
          return BitmapFactory.decodeResource(res, id, options)
      }
  
      private fun calculateInSampleSize(options: BitmapFactory.Options, reqW: Int, reqH: Int): Int{
          val halfHeight = options.outHeight / 2
          val halfWidth = options.outWidth / 2
          var inSampleSize = 1
          while ((halfHeight / inSampleSize) >= reqH && (halfWidth / inSampleSize) >= reqW) {
              inSampleSize *= 2
          }
          return inSampleSize
      }
  }
  ```

### 3. 缓存策略

> 读取缓存的顺序：
>
> 1. 内存：速度最快、但是断电后缓存消失
> 2. 磁盘：存放于手机缓存目录或SD卡，速度仅次于内存
> 3. 网络：速度最慢，需要连接网络，无WIFI情况下消耗流量，仅在图片第一次下载时使用，第二次加载图片时选择内存或者硬盘缓存策略
>
> 做图片缓存的好处：节省流量、速度快。

#### 3.1 LruCache

> LRU(Least Recently Used)即最近最少使用算法，它的核心思想是当缓存满时，会优先淘汰近期最少使用的缓存对象，LRU缓存有两种：LruCache和DiskLruCache，分别用于作内存缓存和磁盘缓存。数据结构采用LinkedHashMap

##### 3.1.1 LruCache(内存缓存)

> Google官方提供了一个LruCache，内部采用LinkedHashMap以强引用的方式存储外界的缓存对象，当缓存满时，LruCache会移除较早使用的缓存对象，然后再添加新的缓存对象。

- LruCache的初始化

  ```java
  int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
  int cacheSize = maxMemory / 8;
  mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
      @Override
      protected int sizeOf(String key, Bitmap bitmap) {
          return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
      }
  };
  ```

  maxMemory()方法获取的是该进程可用的最大内存，单位byte，这里转化为KB。

  sizeOf()方法用于计算缓存对象的大小，单位于总容量的单位一致。

- entryRemoved

  ```java
  private final Set<WeakReference<Bitmap>> reusePool;
  reusePool = Collections.synchronizedSet(new HashSet<WeakReference<Bitmap>>());
  
  mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
  	@Override
  	protected int sizeOf(String key, Bitmap bitmap) {
  		return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
  	}            
  
  	@Override
  	protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
  		// oldValue是从LRU中拿出的bitmap
          if (oldValue.isMutable()) {
              //能复用扔进复用池
              //eusePool.add(new WeakReference<>(oldValue);
              // getReferenceQueue()返回一个引用队列，被删除的oldValue进入队列，getReferenceQueue中有线程不断读取队列数据，如果有数据则手动bitmap.recycler()，相当于加快了bitmap回收过程，否则等待GC回收。
  			reusePool.add(new WeakReference<>(oldValue, getReferenceQueue()));
  		} else {
              //recycle比GC回收快
  			oldValue.recycle();
  		}
  	}
  };
  ```

  其中entryRemoved方法在LruCache移除旧缓存时调用，

- referenceQueue

  ```java
  private ReferenceQueue referenceQueue;
  private Thread clearReferenceQueue;
  private boolean shutDown;
  
  private ReferenceQueue<Bitmap> getReferenceQueue() {
          if (null == referenceQueue) {
              referenceQueue = new ReferenceQueue<Bitmap>();
              clearReferenceQueue = new Thread(new Runnable() {
                  @Override
                  public void run() {
                      while (!shutDown) {
                          try {
                              // remove是阻塞方法
                              Reference<Bitmap> reference = referenceQueue.remove();
                              Bitmap bitmap = reference.get();
                              if (null != bitmap && !bitmap.isRecycled()) {
                                  bitmap.recycle();
                              }
                          } catch (InterruptedException e) {
                              e.printStackTrace();
                          }
                      }
                  }
              });
              clearReferenceQueue.start();
          }
          return referenceQueue;
      }
  ```

##### 3.1.2 DiskLruCache

> DiskLruCache即磁盘缓存，通过将对象写入文件系统从而实现缓存的效果。DiskLruCache得到了Android官方文档的推荐，但是并没有集成在Android源码中。
>
>  https://android.googlesource.com/platform/libcore/+/android-4.1.1_r1/luni/src/main/java/libcore/io/DiskLruCache.java 

- DiskLruCache的创建

  ```java
  private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;
  File diskCacheDir = getDiskCacheDir(mContext, "bitmap");
  
  public File getDiskCacheDir(Context context, String uniqueName) {
          boolean externalStorageAvailable = Environment
                  .getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
          final String cachePath;
          if (externalStorageAvailable) {
              cachePath = context.getExternalCacheDir().getPath();
          } else {
              cachePath = context.getCacheDir().getPath();
          }
  
          return new File(cachePath + File.separator + uniqueName);
      }
  
  // 参数1：磁盘缓存在文件系统中的存储路径
  // 参数2：版本号，版本号改变时DiskLruCache会清空所有缓存文件
  // 参数3：单个节点对应的数据个数，一般设为1
  // 参数4：总缓存大小，当缓存超出这个设定值后，DiskLruCache会清除最少使用的缓存
  mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1,DISK_CACHE_SIZE);
  ```

- DiskLruCache的缓存添加

  ```java
  // 参数1：要写入的文件地址
  // 参数2：文件输出流，用于将缓存写入磁盘
  public boolean downloadUrlToStream(String urlString,
              OutputStream outputStream) {
          HttpURLConnection urlConnection = null;
          BufferedOutputStream out = null;
          BufferedInputStream in = null;
  
          try {
              final URL url = new URL(urlString);
              urlConnection = (HttpURLConnection) url.openConnection();
              in = new BufferedInputStream(urlConnection.getInputStream(),
                      IO_BUFFER_SIZE);
              out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
  
              int b;
              while ((b = in.read()) != -1) {
                  out.write(b);
              }
              return true;
          } catch (IOException e) {
              Log.e(TAG, "downloadBitmap failed." + e);
          } finally {
              if (urlConnection != null) {
                  urlConnection.disconnect();
              }
              MyUtils.close(out);
              MyUtils.close(in);
          }
          return false;
      }
  ```

- 文件输出流的获取

  ```java
  String key = hashKeyFormUrl(url);
          DiskLruCache.Editor editor = mDiskLruCache.edit(key);
          if (editor != null) {
              OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
              if (downloadUrlToStream(url, outputStream)) {
                  editor.commit();
              } else {
                  editor.abort();
              }
              mDiskLruCache.flush();
          }
  ```

  通过editer.commit()，文件才算真正写入了文件系统，如果下载过程中出现异常，可以使用editor.commit()回退操作。

- 获取url的MD5值

  ```java
  private String hashKeyFormUrl(String url) {
          String cacheKey;
          try {
              final MessageDigest mDigest = MessageDigest.getInstance("MD5");
              mDigest.update(url.getBytes());
              cacheKey = bytesToHexString(mDigest.digest());
          } catch (NoSuchAlgorithmException e) {
              cacheKey = String.valueOf(url.hashCode());
          }
          return cacheKey;
      }
  
      private String bytesToHexString(byte[] bytes) {
          StringBuilder sb = new StringBuilder();
          for (byte aByte : bytes) {
              String hex = Integer.toHexString(0xFF & aByte);
              if (hex.length() == 1) {
                  sb.append('0');
              }
              sb.append(hex);
          }
          return sb.toString();
      }
  ```

  DiskLruCache中的Key值用的是MD5值，因为图片的url值可能有特殊字符，会影响url在Android中的使用。

- DiskLruCache的缓存查找

  ```java
  Bitmap bitmap = null;
          String key = hashKeyFormUrl(url);
          DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
          if (snapShot != null) {
              FileInputStream fileInputStream = (FileInputStream)snapShot
                  .getInputStream(DISK_CACHE_INDEX);
              FileDescriptor fileDescriptor = fileInputStream.getFD();
              bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor,
                      reqWidth, reqHeight);
              if (bitmap != null) {
                  addBitmapToMemoryCache(key, bitmap);
              }
          }
  ```

  获取磁盘缓存同样需要将url值转换为MD5值

### 4. ImageLoader

> 一个ImageLoader最基本应该具备以下几个功能：
>
> 1. 图片的同步加载
> 2. 图片的异步加载
> 3. 线程池用于执行任务
> 4. 图片压缩，见2.2
> 5. 内存缓存，见3.1.1
> 6. 磁盘缓存，见3.1.2
> 7. 网络拉取
> 8. 防图片错位

#### 4.1 图片的同步加载

> 同步加载接口需要在外部线程调用，内部不包含线程切换操作。该方法不能再主线程中调用，因为IO操作耗时。

- loadBitmap(String uri, int reqWidth, int reqHeight)

  ```java
      public Bitmap loadBitmap(String uri, int reqWidth, int reqHeight) {
          Bitmap bitmap = loadBitmapFromMemCache(uri);
          if (bitmap != null) {
              Log.d(TAG, "loadBitmapFromMemCache,url:" + uri);
              return bitmap;
          }
  
          try {
              bitmap = loadBitmapFromDiskCache(uri, reqWidth, reqHeight);
              if (bitmap != null) {
                  Log.d(TAG, "loadBitmapFromDisk,url:" + uri);
                  return bitmap;
              }
              bitmap = loadBitmapFromHttp(uri, reqWidth, reqHeight);
              Log.d(TAG, "loadBitmapFromHttp,url:" + uri);
          } catch (IOException e) {
              e.printStackTrace();
          }
  
          if (bitmap == null && !mIsDiskLruCacheCreated) {
              Log.w(TAG, "encounter error, DiskLruCache is not created.");
              bitmap = downloadBitmapFromUrl(uri);
          }
  
          return bitmap;
      }
  ```

#### 4.2 图片的异步加载

> 函数内部会将Bitmap的加载通过Handler转为主线程，函数也可在IO线程加载。

- bindBitmap(final String uri, final ImageView ImageView, final int reqWidth, final int reqHeight)

  ```java
  public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
              CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
              KEEP_ALIVE, TimeUnit.SECONDS,
              new LinkedBlockingQueue<Runnable>(), sThreadFactory);
  
  public void bindBitmap(final String uri, final ImageView imageView,
              final int reqWidth, final int reqHeight) {
          imageView.setTag(TAG_KEY_URI, uri);
          Bitmap bitmap = loadBitmapFromMemCache(uri);
          if (bitmap != null) {
              imageView.setImageBitmap(bitmap);
              return;
          }
          Runnable loadBitmapTask = new Runnable() {
              @Override
              public void run() {
                  Bitmap bitmap = loadBitmap(uri, reqWidth, reqHeight);
                  if (bitmap != null) {
                      LoaderResult result = new LoaderResult(imageView, uri, bitmap);
                      // 把结果发送到主线程
                      mMainHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
                  }
              }
          };
          THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
      }
  ```

- mainHandler

  ````java
      private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
          @Override
          public void handleMessage(Message msg) {
              LoaderResult result = (LoaderResult) msg.obj;
              ImageView imageView = result.imageView;
              String uri = (String) imageView.getTag(TAG_KEY_URI);
              if (uri.equals(result.uri)) {
                  imageView.setImageBitmap(result.bitmap);
              } else {	
                  Log.w(TAG, "set image bitmap,but url has changed, ignored!");
              }
          }
      };
  ````

  从4.1和4.2可以看出，同步加载和异步加载的区别在于，**同步加载函数内部不涉及线程切换，而异步加载函数内部需要传入ImageView，加载完成Bitmap后切换到主线程把Bitmap放入ImageView。**

#### 4.3 网络拉取

> 网络拉取数据需要把url转化为MD5值从而获得文件输出流，再使用url获得文件输入流，最终读取网络文件，通过输出流写入磁盘，再通过LruCache写入内存缓存。

- loadBitmapFromHttp()

  ```java
      private Bitmap loadBitmapFromHttp(String url, int reqWidth, int reqHeight)
              throws IOException {
          if (Looper.myLooper() == Looper.getMainLooper()) {
              throw new RuntimeException("can not visit network from UI Thread.");
          }
          if (mDiskLruCache == null) {
              return null;
          }
          
          String key = hashKeyFormUrl(url);
          DiskLruCache.Editor editor = mDiskLruCache.edit(key);
          if (editor != null) {
              // 磁盘输出流
              OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
              if (downloadUrlToStream(url, outputStream)) {// 写入磁盘
                  editor.commit();
              } else {
                  editor.abort();
              }
              mDiskLruCache.flush();
          }
          // 写入内存
          return loadBitmapFromDiskCache(url, reqWidth, reqHeight);
      }
  ```

- downloadUrlToStream()

  ```java
  public boolean downloadUrlToStream(String urlString,
              OutputStream outputStream) {
          HttpURLConnection urlConnection = null;
      	// 文件输出流，写入数据
          BufferedOutputStream out = null;
  		// 文件输入流，读取网络数据
  	    BufferedInputStream in = null;
  
          try {
              final URL url = new URL(urlString);
              urlConnection = (HttpURLConnection) url.openConnection();
              in = new BufferedInputStream(urlConnection.getInputStream(),
                      IO_BUFFER_SIZE);
              out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
  
              int b;
              while ((b = in.read()) != -1) {
                  out.write(b);
              }
              return true;
          } catch (IOException e) {
              Log.e(TAG, "downloadBitmap failed." + e);
          } finally {
              if (urlConnection != null) {
                  urlConnection.disconnect();
              }
              MyUtils.close(out);
              MyUtils.close(in);
          }
          return false;
      }
  ```

#### 4.4 读取缓存的函数loadBitmap()

> 外部通过url试图从内存或者磁盘获取Bitmap，如果没有则从网络读取，并写入内存和磁盘缓存。

- loadBitmap()

  ```java
      public Bitmap loadBitmap(String uri, int reqWidth, int reqHeight) {
          Bitmap bitmap = loadBitmapFromMemCache(uri);
          if (bitmap != null) {
              Log.d(TAG, "loadBitmapFromMemCache,url:" + uri);
              return bitmap;
          }
  
          try {
              bitmap = loadBitmapFromDiskCache(uri, reqWidth, reqHeight);
              if (bitmap != null) {
                  Log.d(TAG, "loadBitmapFromDisk,url:" + uri);
                  return bitmap;
              }
              // 写入磁盘，写入内存
              bitmap = loadBitmapFromHttp(uri, reqWidth, reqHeight);
              Log.d(TAG, "loadBitmapFromHttp,url:" + uri);
          } catch (IOException e) {
              e.printStackTrace();
          }
  
          if (bitmap == null && !mIsDiskLruCacheCreated) {
              Log.w(TAG, "encounter error, DiskLruCache is not created.");
              bitmap = downloadBitmapFromUrl(uri);
          }
  
          return bitmap;
      }
  ```

#### 4.5 图片列表中防图片错位

> 在ListView或者GridView中加载图片，给每个ImageView绑定tag，

- set/getTag()

  ```java
  private static final int TAG_KEY_URI = R.id.imageloader_uri;
  
  public void bindBitmap(final String uri, final ImageView imageView,
              final int reqWidth, final int reqHeight) {
          imageView.setTag(TAG_KEY_URI, uri);
      	...
          THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
      }
  
  private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
          @Override
          public void handleMessage(Message msg) {
              LoaderResult result = (LoaderResult) msg.obj;
              ImageView imageView = result.imageView;
              // 获取Tag
              String uri = (String) imageView.getTag(TAG_KEY_URI);
              // 判断这个资源是否放在这个ImageView
              if (uri.equals(result.uri)) {
                  imageView.setImageBitmap(result.bitmap);
              } else {
                  Log.w(TAG, "set image bitmap,but url has changed, ignored!");
              }
          }
      };
  
  Runnable loadBitmapTask = new Runnable() {
              @Override
              public void run() {
                  Bitmap bitmap = loadBitmap(uri, reqWidth, reqHeight);
                  if (bitmap != null) {
                      LoaderResult result = new LoaderResult(imageView, uri, bitmap);
                      mMainHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
                  }
              }
          };
  ```

- src/main/res/values/ids.xml

  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <resources>
  
      <item name="imageloader_uri" type="id"/>
  
  </resources>
  ```

#### 4.5 线程池

> 一张图片的加载使用一个线程，线程使用线程池维护。

- THREAD_POOL_EXECUTOR

  ```java
  private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
  private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
  private static final long KEEP_ALIVE = 10L;
  
  public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
              CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
              KEEP_ALIVE, TimeUnit.SECONDS,
              new LinkedBlockingQueue<Runnable>(), sThreadFactory);
  
      public void bindBitmap(final String uri, final ImageView imageView,
              final int reqWidth, final int reqHeight) {
          ...
          Runnable loadBitmapTask = new Runnable() {
              @Override
              public void run() {
                  Bitmap bitmap = loadBitmap(uri, reqWidth, reqHeight);
                  if (bitmap != null) {
                      LoaderResult result = new LoaderResult(imageView, uri, bitmap);
                      mMainHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
                  }
              }
          };
          THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
      }
  ```

