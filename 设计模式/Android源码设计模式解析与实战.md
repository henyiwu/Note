[toc]

# 《Android源码设计模式解析与实战》

## 第一章 走向灵活软件之路——面向对象六大原则

### 1.1 优化代码第一步 —— 单一职责原则

> 定义：就一个类而言，应该仅有一个引起它变化的原因。简单来说，一个类中应该是一组相关性很高的函数、数据的封装。

单一职责的划分界限并不是那么清晰，很多时候需要靠个人经验来界定。

- 以图片加载框架举例

  ```kotlin
  class ImageLoader {
      
      constructor() {
          initImageCache()
      }
      
      var mImageCache: LruCache<String, Bitmap>? = null
      private fun initImageCache() {
          val cacheSize = (Runtime.getRuntime().maxMemory() / 1024).toInt()
          mImageCache = object : LruCache<String, Bitmap>(cacheSize) {
              override fun sizeOf(key: String?, value: Bitmap?): Int {
                  return (value?.rowBytes?.times(value.height) ?: 0) / 1024
              }
          }
      }
  
      public fun displayImage(url: String, imageView: ImageView) {
  
      }
  
      public fun downloadImage(imageUrl: String): Bitmap {
          return BitmapFactory.decodeStream(...)
      }
  }
  ```

  以上代码能够完成一个图片加载类应有的结构，但是耦合过于严重，即缓存和图片加载可以拆分开，让类满足单一职责原则。

- 把缓存和加载拆分成两个类

  ```kotlin
  class ImageLoader {
  
      constructor() {
          initImageCache()
      }
  
      public fun displayImage(url: String, imageView: ImageView) {
  
      }
  
      public fun downloadImage(imageUrl: String): Bitmap {
          return BitmapFactory.decodeStream(...)
      }
  }
  
  class ImageCache {
      var mImageCache: LruCache<String, Bitmap>? = null
  
      constructor() {
          initImageCache()
      }
  
      private fun initImageCache() {
          val cacheSize = (Runtime.getRuntime().maxMemory() / 1024).toInt()
          mImageCache = object : LruCache<String, Bitmap>(cacheSize) {
              override fun sizeOf(key: String?, value: Bitmap?): Int {
                  return (value?.rowBytes?.times(value.height) ?: 0) / 1024
              }
          }
      }
  
      public fun put(url: String, bitmap: Bitmap) {
          mImageCache?.put(url, bitmap)
      }
  
      public fun get(url: String): Bitmap? {
          return mImageCache?.get(url)
      }
  }
  ```

  拆分后ImageLoader只负责图片加载，ImageCache只负责缓存逻辑，这样ImageLoader代码量减少了，职责也清晰了。当缓存相关的逻辑修改时，不需要修改ImageLoader类的代码，反之同理。**改进后的ImageLoader结构更加清晰了，但扩展性还有待提高。**

### 1.2 让程序更稳定、更灵活 —— 开闭原则

> 定义：软件中的对象（类、模块、函数等）应该对于扩展是开放的，但是对于修改是封闭的。

在软件的生命周期内，因为变化，升级和维护等原因需要对原有代码进行修改时，可能会将错误引入原本已经测试过的旧代码中，破坏原有的系统。因此，当软件需求变化时，我们应该尽量通过扩展的方式来实现变化，避免修改原来的代码。

- ImageLoader举例

  ```kotlin
  class ImageLoader {
      val mImageCache = ImageCache()
      val mDiskCache = DiskCache()
      var isUseDiskCache = false
      val mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
      
      fun displayImage(url: String, imageView: ImageView) {
          val bitmap = if (isUseDiskCache) {
              return mDiskCache.get(url)
          } else {
              mImageCache.get(url)
          }
          // 没有缓存，提交给线程池下载
      }
      
      fun useDiskCache(useDiskCache: Boolean) {
          isUseDiskCache = useDiskCache
      }
  }
  
  fun main() {
      val imageLoader = ImageLoader()
      imageLoader.useDiskCache(true)
      imageLoader.useDiskCache(false)
  }
  ```

  上述代码能够实现图片缓存功能，但是如果使用内存缓存就不能使用磁盘缓存，使用磁盘缓存就不能使用内存缓存。

  应该优化为：先从内存缓存中取，如果没有再从磁盘缓存中取，如果没有再从网络下载。

- DoubleCache

  ```kotlin
  class DoubleCache {
      val mMemoryCache = ImageCache()
      val mDiskCache = DiskCache()
      
      public fun get(url: String) : Bitmap {
          var bitmap = mMemoryCache.get(url)
          if (bitmap == null) {
              bitmap = mDiskCache.get(url)
          }
          return bitmap
      }
      
      public fun put(url: String, bitmap: Bitmap) {
          mMemoryCache.put(url, bitmap)
          mDiskCache.put(url, bitmap)
      }
  }
  ```

- ImageLoader

  ```kotlin
  class ImageLoader {
      val mImageCache = ImageCache()
      val mDiskCache = DiskCache()
      val doubleCache = DoubleCache()
      var isUseDiskCache = false
      var isUseDoubleCache = false
      val mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
  
  	  // 每次增加新的缓存，都要修改ImageLoader这个位置
      fun displayImage(url: String, imageView: ImageView) {
          var bitmap: Bitmap? = null
          if (isUseDoubleCache) {
              bitmap = doubleCache.get(url)
          } else if (isUseDiskCache) {
              bitmap = mDiskCache.get(url)
          } else {
              bitmap = mImageCache.get(url)
          }
          if (bitmap != null) {
              imageView.setImageBitmap(bitmap)
          }
          // 没有缓存，提交给线程池异步下载图片
      }
  
      fun useDiskCache(useDiskCache: Boolean) {
          isUseDiskCache = useDiskCache
      }
  
      fun useDoubleCache(useDoubleCache: Boolean) {
          isUseDoubleCache = useDoubleCache
      }
  }
  ```

  这样写是可实现双缓存 -> 单缓存 -> 网络下载的缓存优先级，但是每次增加新的缓存方法都要修改原来的代码，而且用户不能实现自定义缓存。

- 通过接口解耦

- ImageCache

  ```kotlin
  interface ImageCache {
      fun get(url: String) : Bitmap
      fun put(url: String, bitmap: Bitmap)
  }
  ```

- 实现接口

- MemoryCache

  ```kotlin
  class MemoryCache : com.gzik.pandora.index.ImageCache {
  
      val lruCache: LruCache<String, Bitmap>
  
      constructor() {
          // lruCache初始化
      }
  
      override fun get(url: String): Bitmap {
          return lruCache.get(url)
      }
  
      override fun put(url: String, bitmap: Bitmap) {
          lruCache.put(url, bitmap)
      }
  }
  ```

- DiskCache

  ```kotlin
  class DiskCache : com.gzik.pandora.index.ImageCache {
      override fun get(url: String): Bitmap {
  
      }
  
      override fun put(url: String, bitmap: Bitmap) {
  
      }
  }
  ```

- DoubleCache

  ```kotlin
  class DoubleCache : com.gzik.pandora.index.ImageCache {
      val mMemoryCache = com.gzik.pandora.index.MemoryCache()
      val diskCache = DiskCache()
      override fun get(url: String): Bitmap {
          var bitmap = mMemoryCache.get(url)
          if (bitmap == null) {
              bitmap = diskCache.get(url)
          }
          return bitmap
      }
  
      override fun put(url: String, bitmap: Bitmap) {
          mMemoryCache.put(url, bitmap)
          diskCache.put(url, bitmap)
      }
  }
  ```

- 使用

  ```java
  public class ImageLoader {
    
    	ImageCache imageCache = new MemoryCache();
    
   		//用户可实现自己的缓存逻辑
   		public void setImageCache(ImageCache cache) {
        	mImageCache = cache;
      }
    
  		public void displayImage(String imageUrl, ImageView imageView) {
        	Bitmap bitmap = mImageCache.get(imageUrl)
          if(bitmap != null) {
            	imageView.setImageView(bitmap)
              return ;
          }
        // 没图片缓存，去网络下载
      }
  }
  ```