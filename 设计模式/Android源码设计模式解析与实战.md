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

  以上方式通过setImageCache()方法注入不同的缓存实现，这样不仅能够使ImageLoader更简单、健壮，也使得ImageLoader的可扩展性、灵活性更高。

  这样就符合开闭原则，使用抽象解决耦合问题。

### 1.3 构建扩展性更好的系统 —— 里氏替换原则

> 定义：所有引用基类的地方都能够透明地使用子类的对象。

面向对象语言的三大特性：封装、多态、继承，里氏替换原则就是依赖于继承、多态。

- android中的window

  ```java
  public class Window {
    	public void show(View child) {
        	child.draw();
      }
  }
  
  // 建立试图抽象，测量视图的宽高为公共代码，绘制实现交给具体子类
  public abstract class View {
    	public abstract void draw();
    	public void measure(int width, int height) {
        	// 测量视图大小
      }
  }
  
  // 按钮类具体实现
  public class Button extends View {
    	public void draw() {
        	// 绘制按钮
      }
  }
  
  // TextView实现
  public class TextView extends View {
    	public void draw() {
        	// 绘制文本
      }
  }
  ```

  window依赖于view，而view定义了一个视图抽象，measure是这个抽象类的动作，通过show方法可以代入各种各样的view，这样就体现了里氏替换原则。

- 继承的优缺点

  优点：

  

  1. 代码重用，减少创建类的成本，每个子类都有父类的方法和属性。
  2. 子类与父类基本相似，但又与父类有所区别。
  3. 提高代码的可扩展性。

  缺点：

  1. 继承是入侵性的，只要继承了就必须拥有父类的属性和方法。
  2. 可能造成子类代码冗余、灵活性降低，因为子类必须拥有父类的属性和方法。

### 1.4 让项目拥有变化的能力 —— 依赖倒置原则

> 定义：
>
> 1. 高层模块不应该依赖于低层模块，两者都应该依赖于抽象。
> 2. 抽象不应该依赖于细节。
> 3. 细节应该依赖抽象。

- 例如

  ```java
  public class ImageLoader {
    
    	// 这里不应该依赖于实现类，而是依赖于接口，这样当实现类扩展时，不用修改ImageLoader的代码
    	ImageCache imageCache = new MemoryCache();
    
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

### 1.5 系统有更高的灵活性 —— 接口隔离原则

> 定义：客户端不应该依赖于他不需要的接口

- 例如

  有一个接口，包含衣食住行四个action，就不如把衣食住行拆分成4个接口，这样更加灵活。

### 1.6 更好的可扩展性 —— 迪米特原则

> 定义：一个对象应该对其他对象有最少的了解。

- 例如

  故事背景：租客要租房

  有三个类：租客、中介、房东

  租客不应该依赖房东，而是依赖中介，中介依赖房东，让租客做到对房东这个对象的最少了解。

## 第二章 应用最广的模式 —— 单例模式

## 第三章 自由扩展你的项目 —— Builder模式

> 安卓中的应用：AlertDialog

## 第五章 应用最广泛的模式 —— 工厂方法模式

- demo

  ```java
  public class Main {
      public static void main(String[] args) {
          Factory factory = new ConcreteFactory();
          Product product = factory.createProduct();
          product.method();
      }
  }
  
  abstract class Product {
      public abstract void method();
  }
  
  class ConcreteProductA extends Product {
  
      @Override
      public void method() {
          System.out.println("具体产品a");
      }
  }
  
  class ConcreteProductB extends Product {
  
      @Override
      public void method() {
          System.out.println("具体产品b");
      }
  }
  
  abstract class Factory {
      abstract Product createProduct();
  }
  
  class ConcreteFactory extends Factory {
  
      @Override
      Product createProduct() {
          return new ConcreteProductA();
      }
  }
  ```

  1. 抽象工厂
  2. 具体工厂
  3. 抽象产品
  4. 具体产品

- 使用反射

  ```java
  abstract class Factory {
      public abstract <T extends Product> T createProduct(Class<T> clz);
  }
  
  class ConcreteFactory extends Factory {
  
      @Override
      public <T extends Product> T createProduct(Class<T> clz) {
          Product product = null;
          try {
              product = (Product) Class.forName(clz.getName()).newInstance();
          } catch (Exception e) {
              e.printStackTrace();
          }
          return (T) product;
      }
  }
  
  public class Main {
      public static void main(String[] args) {
          Factory factory = new ConcreteFactory();
          Product product = factory.createProduct(ConcreteProductA.class);
          product.method();
      }
  }
  ```

  这样比较优雅，缺点是反射比较消耗性能

## 第六章 创建型设计模式 —— 抽象工厂模式

> 为创建一组相关或者互相依赖的对象提供一个接口，而不需要指定它们的具体类

- 例子

  ```java
  abstract class AbstractProductA {
      abstract void method();
  }
  
  abstract class AbstractProductB {
      abstract void method();
  }
  
  class ConcreteProductA1 extends AbstractProductA {
  
      @Override
      void method() {
          System.out.println("具体产品a1的方法");
      }
  }
  
  class ConcreteProductA2 extends AbstractProductA {
  
      @Override
      void method() {
          System.out.println("具体产品a2的方法");
      }
  }
  
  class ConcreteProductB1 extends AbstractProductB {
  
      @Override
      void method() {
          System.out.println("具体产品b1的方法");
      }
  }
  
  class ConcreteProductB2 extends AbstractProductB {
  
      @Override
      void method() {
          System.out.println("具体产品b2的方法");
      }
  }
  
  abstract class AbstractFactory {
      abstract AbstractProductA createProductA();
      abstract AbstractProductB createProductB();
  }
  
  class ConcreteFactory1 extends AbstractFactory {
  
      @Override
      AbstractProductA createProductA() {
          return new ConcreteProductA1();
      }
  
      @Override
      AbstractProductB createProductB() {
          return new ConcreteProductB1();
      }
  }
  
  class ConcreteFactory2 extends AbstractFactory {
  
      @Override
      AbstractProductA createProductA() {
          return new ConcreteProductA2();
      }
  
      @Override
      AbstractProductB createProductB() {
          return new ConcreteProductB2();
      }
  }
  ```

## 第七章 时势造英雄 —— 策略模式

- 安卓中的应用

  ```java
  abstract public class BaseInterpolator implements Interpolator {
      private @Config int mChangingConfiguration;
  
      public @Config int getChangingConfiguration() {
          return mChangingConfiguration;
      }
  
      void setChangingConfiguration(@Config int changingConfiguration) {
          mChangingConfiguration = changingConfiguration;
      }
  }
  
  // 加速插值器
  public class AccelerateInterpolator extends BaseInterpolator implements NativeInterpolator {
      private final float mFactor;
      private final double mDoubleFactor;
  
      public AccelerateInterpolator() {
          mFactor = 1.0f;
          mDoubleFactor = 2.0;
      }
  
      public AccelerateInterpolator(float factor) {
          mFactor = factor;
          mDoubleFactor = 2 * mFactor;
      }
  
      public AccelerateInterpolator(Context context, AttributeSet attrs) {
          this(context.getResources(), context.getTheme(), attrs);
      }
  
      public AccelerateInterpolator(Resources res, Theme theme, AttributeSet attrs) {
          TypedArray a;
          if (theme != null) {
              a = theme.obtainStyledAttributes(attrs, R.styleable.AccelerateInterpolator, 0, 0);
          } else {
              a = res.obtainAttributes(attrs, R.styleable.AccelerateInterpolator);
          }
  
          mFactor = a.getFloat(R.styleable.AccelerateInterpolator_factor, 1.0f);
          mDoubleFactor = 2 * mFactor;
          setChangingConfiguration(a.getChangingConfigurations());
          a.recycle();
      }
  
      public float getInterpolation(float input) {
          if (mFactor == 1.0f) {
              return input * input;
          } else {
              return (float)Math.pow(input, mDoubleFactor);
          }
      }
  
      /** @hide */
      @Override
      public long createNativeInterpolator() {
          return NativeInterpolatorFactory.createAccelerateInterpolator(mFactor);
      }
  }
  
  // 线性插值器
  public class LinearInterpolator extends BaseInterpolator implements NativeInterpolator {
  
      public LinearInterpolator() {
      }
  
      public LinearInterpolator(Context context, AttributeSet attrs) {
      }
  
      public float getInterpolation(float input) {
          return input;
      }
  
      @Override
      public long createNativeInterpolator() {
          return NativeInterpolatorFactory.createLinearInterpolator();
      }
  }
  ```

  view动画插值器就是一种策略模式，不同的策略在getInterpolation()中体现

- 源代码

- View.java

  ````java
  public class View implements Drawable.Callback, KeyEvent.Callback,
          AccessibilityEventSource {
  						boolean draw(Canvas canvas, ViewGroup parent, long drawingTime) {        
          }
  
          final Animation a = getAnimation();
          if (a != null) {
              more = applyLegacyAnimation(parent, drawingTime, a, scalingRequired);
              concatMatrix = a.willChangeTransformationMatrix();
              if (concatMatrix) {
                  mPrivateFlags3 |= PFLAG3_VIEW_IS_ANIMATING_TRANSFORM;
              }
              transformToApply = parent.getChildTransformation();
          } else {
            ...
          }
            ....
  }
  
      private boolean applyLegacyAnimation(ViewGroup parent, long drawingTime,
              Animation a, boolean scalingRequired) {
          Transformation invalidationTransform;
          final Transformation t = parent.getChildTransformation();
          boolean more = a.getTransformation(drawingTime, t, 1f);
        	...
      }
  ````

- Animation.java

  ```java
  public abstract class Animation implements Cloneable {
      public boolean getTransformation(long currentTime, Transformation outTransformation,
              float scale) {
          mScaleFactor = scale;
          return getTransformation(currentTime, outTransformation);
      }
    
      public boolean getTransformation(long currentTime, Transformation outTransformation) {
          if ((normalizedTime >= 0.0f || mFillBefore) && (normalizedTime <= 1.0f || mFillAfter)) {
  						......	
            	// mInterpolator是我们通过Animation.setInterpolator()设置的策略
              final float interpolatedTime = mInterpolator.getInterpolation(normalizedTime);
              applyTransformation(interpolatedTime, outTransformation);
          }
      }
  }
  ```

## 第十四章 解决问题的"第三者" —— 迭代器模式

> 安卓中的应用：SqliteLite数据库使用游标查询数据

