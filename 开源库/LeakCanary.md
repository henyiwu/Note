[toc]

## LeakCanary

### 初始化

> Leakcanary从2.0版本后不需要手动初始化，而是通过ContentProvider来初始化
>
> 基本原理：对Activity、Fragment、ViewModel、RootView、Service的销毁进行监控，拿到即将销毁的对象通过WeakReference+ReferenceQueue进行内存泄漏的初步判断，最后Dump HeapProfile进行具体分析

- AndroidManifest.xml

  ```xml
  <application>
          <provider
              android:name="leakcanary.internal.AppWatcherInstaller$MainProcess"
              android:authorities="${applicationId}.leakcanary-installer"
              android:enabled="@bool/leak_canary_watcher_auto_install"
              android:exported="false" />
      </application>
  ```

- AppWatcherInstaller

  ```kotlin
  internal sealed class AppWatcherInstaller : ContentProvider() {
  
    /**
     * [MainProcess] automatically sets up the LeakCanary code that runs in the main app process.
     */
    internal class MainProcess : AppWatcherInstaller()
  
    /**
     * When using the `leakcanary-android-process` artifact instead of `leakcanary-android`,
     * [LeakCanaryProcess] automatically sets up the LeakCanary code
     */
    internal class LeakCanaryProcess : AppWatcherInstaller()
  
    override fun onCreate(): Boolean {
      val application = context!!.applicationContext as Application
      AppWatcher.manualInstall(application)
      return true
    }
  }
  ```

  onCreate()中进行具体初始化

- AppWatcher.manualInstall()

  ```kotlin
  @JvmOverloads
    fun manualInstall(
      application: Application,
      retainedDelayMillis: Long = TimeUnit.SECONDS.toMillis(5),
      watchersToInstall: List<InstallableWatcher> = appDefaultWatchers(application)
    ) {
      checkMainThread()
  		......
      // 遍历watchersToInstall并注册
      // watchersToInstall是个列表，从appDefaultWatchers()中拿
      watchersToInstall.forEach {
        it.install()
      }
    }
  ```

- appDefaultWatchers()

  ```kotlin
  fun appDefaultWatchers(
      application: Application,
      reachabilityWatcher: ReachabilityWatcher = objectWatcher
    ): List<InstallableWatcher> {
      return listOf(
        ActivityWatcher(application, reachabilityWatcher),
        FragmentAndViewModelWatcher(application, reachabilityWatcher),
        RootViewWatcher(reachabilityWatcher),
        ServiceWatcher(reachabilityWatcher)
      )
    }
  ```

  返回一个list，由Activity，Fragment，ViewModel，RootView，Service对应的Watchers组成，分别对它们的销毁进行监控。

### 具体watcher如何监控对象销毁

> 具体的对象监控，关键在于获取监控时机，也就是组建销毁的时机

#### ActivityWatcher

> 监控Activity对象的销毁

- ActivityWatcher

  ```kotlin
  class ActivityWatcher(
    private val application: Application,
    private val reachabilityWatcher: ReachabilityWatcher
  ) : InstallableWatcher {
  
    private val lifecycleCallbacks =
      object : Application.ActivityLifecycleCallbacks by noOpDelegate() {
        override fun onActivityDestroyed(activity: Activity) {
          reachabilityWatcher.expectWeaklyReachable(
            activity, "${activity::class.java.name} received Activity#onDestroy() callback"
          )
        }
      }
  
    override fun install() {
      application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }
  
    override fun uninstall() {
      application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
    }
  }
  ```

  通过application注册每个activity生命周期回调，来监控每个activity的销毁，activity销毁时通过reachabilityWatcher将当前activity加入监控队列，进行具体分析

#### FragmentAndViewModelWatcher

> 监控fragment和viewmodel对象销毁
>
> fragment有三种：framework自带、supprot v4包，androidx下

- FragmentAndViewModelWatcher

  ```kotlin
  class FragmentAndViewModelWatcher(
    private val application: Application,
    private val reachabilityWatcher: ReachabilityWatcher
  ) : InstallableWatcher {
  
    private val lifecycleCallbacks =
      object : Application.ActivityLifecycleCallbacks by noOpDelegate() {
        // activity启动时，调用所有fragmentDestroyWatcher的invoke()方法
        // fragmentDestroyWatchers从哪来？看下面
        override fun onActivityCreated(
          activity: Activity,
          savedInstanceState: Bundle?
        ) {
          for (watcher in fragmentDestroyWatchers) {
            watcher(activity)
          }
        }
      }
    
    override fun install() {
      // 注册activity生命周期回调
      application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }
  
    override fun uninstall() {
      application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
    }
  }
  ```

- fragmentDestroyWatchers

  ```kotlin
    private val fragmentDestroyWatchers: List<(Activity) -> Unit> = run {
      val fragmentDestroyWatchers = mutableListOf<(Activity) -> Unit>()
  
      if (SDK_INT >= O) {
        fragmentDestroyWatchers.add(
          // framework自带的fragment
          AndroidOFragmentDestroyWatcher(reachabilityWatcher)
        )
      }
  
      // 如果存在androidx.fragment.app.Fragment
      // 反射创建leakcanary.internal.AndroidXFragmentDestroyWatcher
      getWatcherIfAvailable(
        ANDROIDX_FRAGMENT_CLASS_NAME,
        ANDROIDX_FRAGMENT_DESTROY_WATCHER_CLASS_NAME,
        reachabilityWatcher
      )?.let {
        fragmentDestroyWatchers.add(it)
      }
  
      // 如果存在android.support.v4.app.Fragment
      // 反射创建leakcanary.internal.AndroidSupportFragmentDestroyWatcher
      getWatcherIfAvailable(
        ANDROID_SUPPORT_FRAGMENT_CLASS_NAME,
        ANDROID_SUPPORT_FRAGMENT_DESTROY_WATCHER_CLASS_NAME,
        reachabilityWatcher
      )?.let {
        fragmentDestroyWatchers.add(it)
      }
      fragmentDestroyWatchers
    }
  ```

  fragmentDestroyWatchers是一个list

  判断系统是否支持fragment和support.fragment，反射创建对应的watchers对象，AndroidSupportFragmentDestroyWatcher逻辑与FragmentAndViewModelWatcher基本相同。

  使用反射是因为这两个watchers对象是另外的aar引入的，可实现解耦

##### framework自带的fragment监控

- AndroidOFragmentDestroyWatcher

  ```kotlin
  @SuppressLint("NewApi")
  internal class AndroidOFragmentDestroyWatcher(
    private val reachabilityWatcher: ReachabilityWatcher
  ) : (Activity) -> Unit {
    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
  
      override fun onFragmentViewDestroyed(
        fm: FragmentManager,
        fragment: Fragment
      ) {
        val view = fragment.view
        if (view != null) {
          reachabilityWatcher.expectWeaklyReachable(
            view, "${fragment::class.java.name} received Fragment#onDestroyView() callback " +
            "(references to its views should be cleared to prevent leaks)"
          )
        }
      }
  
      override fun onFragmentDestroyed(
        fm: FragmentManager,
        fragment: Fragment
      ) {
        // 熟悉的提示
        reachabilityWatcher.expectWeaklyReachable(
          fragment, "${fragment::class.java.name} received Fragment#onDestroy() callback"
        )
      }
    }
  
    override fun invoke(activity: Activity) {
      val fragmentManager = activity.fragmentManager
      fragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true)
    }
  }
  ```

  通过activity监控其中fragment的生命周期，在fragment销毁或者其view销毁时通过reachabilityWatcher加入监控队列，进行具体分析

##### AndroidSupportFragmentDestroyWatcher 安卓support包下的fragment监控

- AndroidSupportFragmentDestroyWatcher

  ````kotlin
  internal class AndroidSupportFragmentDestroyWatcher(
    private val reachabilityWatcher: ReachabilityWatcher
  ) : (Activity) -> Unit {
  
    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
  
      override fun onFragmentViewDestroyed(
        fm: FragmentManager,
        fragment: Fragment
      ) {
        val view = fragment.view
        if (view != null) {
          reachabilityWatcher.expectWeaklyReachable(
            view, "${fragment::class.java.name} received Fragment#onDestroyView() callback " +
            "(references to its views should be cleared to prevent leaks)"
          )
        }
      }
  
      override fun onFragmentDestroyed(
        fm: FragmentManager,
        fragment: Fragment
      ) {
        reachabilityWatcher.expectWeaklyReachable(
          fragment, "${fragment::class.java.name} received Fragment#onDestroy() callback"
        )
      }
    }
  
    override fun invoke(activity: Activity) {
      if (activity is FragmentActivity) {
        val supportFragmentManager = activity.supportFragmentManager
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true)
      }
    }
  }
  ````

##### AndroidXFragmentDestroyWatcher fragment和viewmodel监控

- AndroidXFragmentDestroyWatcher

  ````kotlin
  internal class AndroidXFragmentDestroyWatcher(
    private val reachabilityWatcher: ReachabilityWatcher
  ) : (Activity) -> Unit {
  
    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
  
      override fun onFragmentCreated(
        fm: FragmentManager,
        fragment: Fragment,
        savedInstanceState: Bundle?
      ) {
        ViewModelClearedWatcher.install(fragment, reachabilityWatcher)
      }
  
      override fun onFragmentViewDestroyed(
        fm: FragmentManager,
        fragment: Fragment
      ) {
        val view = fragment.view
        if (view != null) {
          reachabilityWatcher.expectWeaklyReachable(
            view, "${fragment::class.java.name} received Fragment#onDestroyView() callback " +
            "(references to its views should be cleared to prevent leaks)"
          )
        }
      }
  
      override fun onFragmentDestroyed(
        fm: FragmentManager,
        fragment: Fragment
      ) {
        reachabilityWatcher.expectWeaklyReachable(
          fragment, "${fragment::class.java.name} received Fragment#onDestroy() callback"
        )
      }
    }
  
    override fun invoke(activity: Activity) {
      if (activity is FragmentActivity) {
        val supportFragmentManager = activity.supportFragmentManager
        // 注册fragment生命周期回调
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true)
        // 注册ViewModelClearedWatcher，用于监控viewmodel销毁
        ViewModelClearedWatcher.install(activity, reachabilityWatcher)
      }
    }
  }
  ````

- ViewModelClearedWatcher

  ````kotlin
  internal class ViewModelClearedWatcher(
    storeOwner: ViewModelStoreOwner,
    private val reachabilityWatcher: ReachabilityWatcher
  ) : ViewModel() {
  
    private val viewModelMap: Map<String, ViewModel>?
  
    init {
      // 反射获取当前ViewModelStoreOwner存放ViewModel的集合mMap
      viewModelMap = try {
        val mMapField = ViewModelStore::class.java.getDeclaredField("mMap")
        mMapField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        mMapField[storeOwner.viewModelStore] as Map<String, ViewModel>
      } catch (ignored: Exception) {
        null
      }
    }
  
    override fun onCleared() {
      // ViewModel回收时调用，监控回收情况
      viewModelMap?.values?.forEach { viewModel ->
        reachabilityWatcher.expectWeaklyReachable(
          viewModel, "${viewModel::class.java.name} received ViewModel#onCleared() callback"
        )
      }
    }
  
    companion object {
      fun install(
        storeOwner: ViewModelStoreOwner,
        reachabilityWatcher: ReachabilityWatcher
      ) {
        // 创建ViewModelClearedWatcher，本身就是一个ViewModel
        // storeOwner为activity或fragment
        val provider = ViewModelProvider(storeOwner, object : Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            ViewModelClearedWatcher(storeOwner, reachabilityWatcher) as T
        })
        provider.get(ViewModelClearedWatcher::class.java)
      }
    }
  }
  ````

