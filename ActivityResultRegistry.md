## ActivityResultRegistry

- ComponentActivity

  ```java
  public class ComponentActivity extends androidx.core.app.ComponentActivity implements ContextAware, LifecycleOwner, ViewModelStoreOwner, HasDefaultViewModelProviderFactory, SavedStateRegistryOwner, OnBackPressedDispatcherOwner, ActivityResultRegistryOwner, ActivityResultCaller, MenuHost {
  		@NonNull
      public final <I, O> ActivityResultLauncher<I> registerForActivityResult(@NonNull ActivityResultContract<I, O> contract, @NonNull ActivityResultRegistry registry, @NonNull ActivityResultCallback<O> callback) {
          return registry.register("activity_rq#" + this.mNextLocalRequestCode.getAndIncrement(), this, 		contract, callback);
      }
  }
  ```

- ActivityResultRegistry.ActivityResultLauncher()

  ```java
  public final <I, O> ActivityResultLauncher<I> register(
          @NonNull final String key,
          @NonNull final LifecycleOwner lifecycleOwner,
          @NonNull final ActivityResultContract<I, O> contract,
          @NonNull final ActivityResultCallback<O> callback) {
  
      Lifecycle lifecycle = lifecycleOwner.getLifecycle();
  
      if (lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
          throw new IllegalStateException("LifecycleOwner " + lifecycleOwner + " is "
                  + "attempting to register while current state is "
                  + lifecycle.getCurrentState() + ". LifecycleOwners must call register before "
                  + "they are STARTED.");
      }
  
      final int requestCode = registerKey(key);
      LifecycleContainer lifecycleContainer = mKeyToLifecycleContainers.get(key);
      if (lifecycleContainer == null) {
          lifecycleContainer = new LifecycleContainer(lifecycle);
      }
      LifecycleEventObserver observer = new LifecycleEventObserver() {
          @Override
          public void onStateChanged(
                  @NonNull LifecycleOwner lifecycleOwner,
                  @NonNull Lifecycle.Event event) {
              if (Lifecycle.Event.ON_START.equals(event)) {
                  mKeyToCallback.put(key, new CallbackAndContract<>(callback, contract));
                  if (mParsedPendingResults.containsKey(key)) {
                      @SuppressWarnings("unchecked")
                      final O parsedPendingResult = (O) mParsedPendingResults.get(key);
                      mParsedPendingResults.remove(key);
                      callback.onActivityResult(parsedPendingResult);
                  }
                  final ActivityResult pendingResult = mPendingResults.getParcelable(key);
                  if (pendingResult != null) {
                      mPendingResults.remove(key);
                      callback.onActivityResult(contract.parseResult(
                              pendingResult.getResultCode(),
                              pendingResult.getData()));
                  }
              } else if (Lifecycle.Event.ON_STOP.equals(event)) {
                  mKeyToCallback.remove(key);
              } else if (Lifecycle.Event.ON_DESTROY.equals(event)) {
                  unregister(key);
              }
          }
      };
      lifecycleContainer.addObserver(observer);
      mKeyToLifecycleContainers.put(key, lifecycleContainer);
  
      return new ActivityResultLauncher<I>() {
  				// c端掉用launch，即执行这个方法
        	@Override
          public void launch(I input, @Nullable ActivityOptionsCompat options) {
              mLaunchedKeys.add(key);
              Integer innerCode = mKeyToRc.get(key);
            	// onLaunch()在ComponentActivity中
              onLaunch((innerCode != null) ? innerCode : requestCode, contract, input, options);
          }
  
          @Override
          public void unregister() {
              ActivityResultRegistry.this.unregister(key);
          }
  
          @NonNull
          @Override
          public ActivityResultContract<I, ?> getContract() {
              return contract;
          }
      };
  }
  ```

- ComponentActivity

  ```java
      public ComponentActivity() {
          this.mActivityResultRegistry = new ActivityResultRegistry() {
              public <I, O> void onLaunch(final int requestCode, @NonNull ActivityResultContract<I, O> contract, I input, @Nullable ActivityOptionsCompat options) {
                  ComponentActivity activity = ComponentActivity.this;
                  final SynchronousResult<O> synchronousResult = contract.getSynchronousResult(activity, input);
                  if (synchronousResult != null) {
                      (new Handler(Looper.getMainLooper())).post(new Runnable() {
                          public void run() {
                              dispatchResult(requestCode, synchronousResult.getValue());
                          }
                      });
                  } else {
                      Intent intent = contract.createIntent(activity, input);
                      Bundle optionsBundle = null;
                      if (intent.getExtras() != null && intent.getExtras().getClassLoader() == null) {
                          intent.setExtrasClassLoader(activity.getClassLoader());
                      }
  
                      if (intent.hasExtra("androidx.activity.result.contract.extra.ACTIVITY_OPTIONS_BUNDLE")) {
                          optionsBundle = intent.getBundleExtra("androidx.activity.result.contract.extra.ACTIVITY_OPTIONS_BUNDLE");
                          intent.removeExtra("androidx.activity.result.contract.extra.ACTIVITY_OPTIONS_BUNDLE");
                      } else if (options != null) {
                          optionsBundle = options.toBundle();
                      }
  
                    // 请求权限
                      if ("androidx.activity.result.contract.action.REQUEST_PERMISSIONS".equals(intent.getAction())) {
                          String[] permissions = intent.getStringArrayExtra("androidx.activity.result.contract.extra.PERMISSIONS");
                          if (permissions == null) {
                              permissions = new String[0];
                          }
  
                          ActivityCompat.requestPermissions(activity, permissions, requestCode);
                      } else if ("androidx.activity.result.contract.action.INTENT_SENDER_REQUEST".equals(intent.getAction())) {
                          IntentSenderRequest request = (IntentSenderRequest)intent.getParcelableExtra("androidx.activity.result.contract.extra.INTENT_SENDER_REQUEST");
  
                          try {
                              ActivityCompat.startIntentSenderForResult(activity, request.getIntentSender(), requestCode, request.getFillInIntent(), request.getFlagsMask(), request.getFlagsValues(), 0, optionsBundle);
                          } catch (final SendIntentException var11) {
                              (new Handler(Looper.getMainLooper())).post(new Runnable() {
                                  public void run() {
                                      dispatchResult(requestCode, 0, (new Intent()).setAction("androidx.activity.result.contract.action.INTENT_SENDER_REQUEST").putExtra("androidx.activity.result.contract.extra.SEND_INTENT_EXCEPTION", var11));
                                  }
                              });
                          }
                      } else {
                          ActivityCompat.startActivityForResult(activity, intent, requestCode, optionsBundle);
                      }
  
                  }
              }
          };
      }
  
      @Deprecated
      @CallSuper
      public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
          if (!this.mActivityResultRegistry.dispatchResult(requestCode, -1, (new Intent()).putExtra("androidx.activity.result.contract.extra.PERMISSIONS", permissions).putExtra("androidx.activity.result.contract.extra.PERMISSION_GRANT_RESULTS", grantResults)) && VERSION.SDK_INT >= 23) {
              super.onRequestPermissionsResult(requestCode, permissions, grantResults);
          }
      }
  ```
## ActivityResultRegistry

- ComponentActivity

  ```java
  public class ComponentActivity extends androidx.core.app.ComponentActivity implements ContextAware, LifecycleOwner, ViewModelStoreOwner, HasDefaultViewModelProviderFactory, SavedStateRegistryOwner, OnBackPressedDispatcherOwner, ActivityResultRegistryOwner, ActivityResultCaller, MenuHost {
  		@NonNull
      public final <I, O> ActivityResultLauncher<I> registerForActivityResult(@NonNull ActivityResultContract<I, O> contract, @NonNull ActivityResultRegistry registry, @NonNull ActivityResultCallback<O> callback) {
          return registry.register("activity_rq#" + this.mNextLocalRequestCode.getAndIncrement(), this, 		contract, callback);
      }
  }
  ```

- ActivityResultRegistry.ActivityResultLauncher()

  ```java
  public final <I, O> ActivityResultLauncher<I> register(
          @NonNull final String key,
          @NonNull final LifecycleOwner lifecycleOwner,
          @NonNull final ActivityResultContract<I, O> contract,
          @NonNull final ActivityResultCallback<O> callback) {
  
      Lifecycle lifecycle = lifecycleOwner.getLifecycle();
  
      if (lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
          throw new IllegalStateException("LifecycleOwner " + lifecycleOwner + " is "
                  + "attempting to register while current state is "
                  + lifecycle.getCurrentState() + ". LifecycleOwners must call register before "
                  + "they are STARTED.");
      }
  
      final int requestCode = registerKey(key);
      LifecycleContainer lifecycleContainer = mKeyToLifecycleContainers.get(key);
      if (lifecycleContainer == null) {
          lifecycleContainer = new LifecycleContainer(lifecycle);
      }
      LifecycleEventObserver observer = new LifecycleEventObserver() {
          @Override
          public void onStateChanged(
                  @NonNull LifecycleOwner lifecycleOwner,
                  @NonNull Lifecycle.Event event) {
              if (Lifecycle.Event.ON_START.equals(event)) {
                  mKeyToCallback.put(key, new CallbackAndContract<>(callback, contract));
                  if (mParsedPendingResults.containsKey(key)) {
                      @SuppressWarnings("unchecked")
                      final O parsedPendingResult = (O) mParsedPendingResults.get(key);
                      mParsedPendingResults.remove(key);
                      callback.onActivityResult(parsedPendingResult);
                  }
                  final ActivityResult pendingResult = mPendingResults.getParcelable(key);
                  if (pendingResult != null) {
                      mPendingResults.remove(key);
                      callback.onActivityResult(contract.parseResult(
                              pendingResult.getResultCode(),
                              pendingResult.getData()));
                  }
              } else if (Lifecycle.Event.ON_STOP.equals(event)) {
                  mKeyToCallback.remove(key);
              } else if (Lifecycle.Event.ON_DESTROY.equals(event)) {
                  unregister(key);
              }
          }
      };
      lifecycleContainer.addObserver(observer);
      mKeyToLifecycleContainers.put(key, lifecycleContainer);
  
      return new ActivityResultLauncher<I>() {
  				// c端掉用launch，即执行这个方法
        	@Override
          public void launch(I input, @Nullable ActivityOptionsCompat options) {
              mLaunchedKeys.add(key);
              Integer innerCode = mKeyToRc.get(key);
            	// onLaunch()在ComponentActivity中
              onLaunch((innerCode != null) ? innerCode : requestCode, contract, input, options);
          }
  
          @Override
          public void unregister() {
              ActivityResultRegistry.this.unregister(key);
          }
  
          @NonNull
          @Override
          public ActivityResultContract<I, ?> getContract() {
              return contract;
          }
      };
  }
  ```

- ComponentActivity

  ```java
      public ComponentActivity() {
          this.mActivityResultRegistry = new ActivityResultRegistry() {
              public <I, O> void onLaunch(final int requestCode, @NonNull ActivityResultContract<I, O> contract, I input, @Nullable ActivityOptionsCompat options) {
                  ComponentActivity activity = ComponentActivity.this;
                  final SynchronousResult<O> synchronousResult = contract.getSynchronousResult(activity, input);
                  if (synchronousResult != null) {
                      (new Handler(Looper.getMainLooper())).post(new Runnable() {
                          public void run() {
                              dispatchResult(requestCode, synchronousResult.getValue());
                          }
                      });
                  } else {
                      Intent intent = contract.createIntent(activity, input);
                      Bundle optionsBundle = null;
                      if (intent.getExtras() != null && intent.getExtras().getClassLoader() == null) {
                          intent.setExtrasClassLoader(activity.getClassLoader());
                      }
  
                      if (intent.hasExtra("androidx.activity.result.contract.extra.ACTIVITY_OPTIONS_BUNDLE")) {
                          optionsBundle = intent.getBundleExtra("androidx.activity.result.contract.extra.ACTIVITY_OPTIONS_BUNDLE");
                          intent.removeExtra("androidx.activity.result.contract.extra.ACTIVITY_OPTIONS_BUNDLE");
                      } else if (options != null) {
                          optionsBundle = options.toBundle();
                      }
  
                    // 请求权限
                      if ("androidx.activity.result.contract.action.REQUEST_PERMISSIONS".equals(intent.getAction())) {
                          String[] permissions = intent.getStringArrayExtra("androidx.activity.result.contract.extra.PERMISSIONS");
                          if (permissions == null) {
                              permissions = new String[0];
                          }
  
                          ActivityCompat.requestPermissions(activity, permissions, requestCode);
                      } else if ("androidx.activity.result.contract.action.INTENT_SENDER_REQUEST".equals(intent.getAction())) {
                          IntentSenderRequest request = (IntentSenderRequest)intent.getParcelableExtra("androidx.activity.result.contract.extra.INTENT_SENDER_REQUEST");
  
                          try {
                              ActivityCompat.startIntentSenderForResult(activity, request.getIntentSender(), requestCode, request.getFillInIntent(), request.getFlagsMask(), request.getFlagsValues(), 0, optionsBundle);
                          } catch (final SendIntentException var11) {
                              (new Handler(Looper.getMainLooper())).post(new Runnable() {
                                  public void run() {
                                      dispatchResult(requestCode, 0, (new Intent()).setAction("androidx.activity.result.contract.action.INTENT_SENDER_REQUEST").putExtra("androidx.activity.result.contract.extra.SEND_INTENT_EXCEPTION", var11));
                                  }
                              });
                          }
                      } else {
                          ActivityCompat.startActivityForResult(activity, intent, requestCode, optionsBundle);
                      }
  
                  }
              }
          };
      }
  
      @Deprecated
      @CallSuper
      public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
          if (!this.mActivityResultRegistry.dispatchResult(requestCode, -1, (new Intent()).putExtra("androidx.activity.result.contract.extra.PERMISSIONS", permissions).putExtra("androidx.activity.result.contract.extra.PERMISSION_GRANT_RESULTS", grantResults)) && VERSION.SDK_INT >= 23) {
              super.onRequestPermissionsResult(requestCode, permissions, grantResults);
          }
      }
  ```

- 使用

  
