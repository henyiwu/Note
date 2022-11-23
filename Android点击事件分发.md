[toc]

## Android点击事件分发

> 为什么需要事件分发？
>
> 手机屏幕上的每个视图都是矩形的，它们按照一定的次序堆叠放置，可能造成视图的重叠，系统怎么知道我们想要操作的是哪一个布局呢？这时候就需要事件分发机制来处理。所以事件分发机制也可以理解为，系统精准地把事件分发给我们想要操作的视图。

### 1. MotionEvent

> 手指接触屏幕后产生的一系列事件中，典型的事件有：
>
> 1. ACTION_DOWN —— 手指刚接触屏幕
> 2. ACTION_MOVE —— 手指在屏幕上移动
> 3. ACTION_UP —— 手指从屏幕上松开的瞬间
> 4. ACTION_CANCEL ——  被上层拦截时触发（手指点击了子View，但事件被父ViewGroup拦截）
>
> 正常情况下触摸事件有如下情况：
>
> 1. 点击屏幕后松开，事件序列为ACTION_DOWN -> ACTION_UP
> 2. 点击屏幕后滑动一会再松开ACTION_DOWN -> ACTION_MOVE..........（多个MOVE） -> ACTION_UP
>
> 通过MotionEvent我们还可以得到点击事件发生的x和y坐标。

### 2. 事件分发流程

> 事件分发也就是对MotionEvent的分发过程，点击事件分发由dispatchTouchEvent、onInterceptTouchEvent、onTouchEvent方法共同完成。

- dispatchTouchEvent(MotionEvent ev)

> 用于进行事件分发，如果事件能够传递给当前View，该方法一定被调用，返回结果受当前View的onTouchEvent和下级View的dispatchTouchEvent影响，表示是否消耗事件。

- onInterceptTouchEvent(MotionEvent ev)

> 在dispatchTouchEvent()内部调用，判断是否拦截某个事件，如果当前View拦截了某个事件，则在同一个事件序列中，此方法不会再被调用，返回值表示是否拦截事件。
>
> **View没有onInterceptTouchEvent(MotionEvent ev)方法**

- onTouchEvent(MotionEvent ev)

> 在dispatchTouchEvent()方法中调用，用于处理点击事件，返回值表示是否消耗当前事件，如果不消耗，则在同一个事件序列中，当前View无法再次接收到事件。

- 伪代码表示

  ```java
  public boolean dispatchTouchEvent(MotionEvent ev) {
  	boolean consume = false;
      if(onInterceptTouchEvent(ev)) {
          consume = onTouchEvent(ev);
      } else {
          consume = child.dispatchTouchEvent(ev);
      }
      return consume;
  }
  ```

- 《Android开发艺术探索》中总结的事件分发结论

> **其中6.0以上版本增加OnContextClickListener，指外接设备的点击状态，书中源代码版本无此监听器，故认为点击状态只有clickable和longClickable**
>
> 1. 同一个事件序列指从手指接触屏幕的一刻开始，到手指离开屏幕的一刻结束，这个过程中所产生的一系列事件，这个事件序列以down事件开始，中间含有数量不定的move事件，最终以up事件结束。
> 2. 正常情况下，一个事件序列只能被一个View拦截且消耗，因为一旦一个元素拦截了某事件，那么同一事件序列内的所有事件都会直接交给它处理，因此同一个事件序列不能分别由两个View同时处理，但也可以通过特殊手段做到，比如一个View通过onTouchEvent将原本自己处理的事件强行传递给其他View。
> 3. 某一个View一旦决定拦截，那么这一个事件序列都只能由它来处理，并且它的onInterceptTouchEvent()不会被再次调用。
> 4. 某个View一旦开始处理事件，如果它不消耗ACTION_DOWN事件(onTouchEvent()返回false)，那么同一事件中的其他事件都不会再交给它处理，并且事件将重新交由它的父元素处理，即父元素的onTouchEvent()会被调用。
> 5. 如果View不消耗除ACTION_DOWN以为的其他事件，那么这个点击事件会消失，此时父元素的onTouchEvent()不会再调用，并且当前View可以持续收到后续的事件，最终这些消失的事件会传递给Activity处理。
> 6. ViewGroup默认不拦截任何事件，Android源码中ViewGroup的onInterceptTouchEvent方法默认返回false
> 7. View没有onInterceptTouchEvent方法，一旦有事件传递给它，那么它的onTouchEvent方法就会被调用
> 8. View的onTouchEvent默认都会消耗事件（返回true），除非它是不可点击的（clickable和longClickable同时为false）。View的longClickable属性默认都为false，clickable属性分情况，可点击的控件例如Button的clickable属性默认为true，不可点击的控件例如TextView的clickable属性默认为false。
> 9. View的enable属性不影响onTouchEvent的默认返回值，哪怕一个View是disable状态的，只要它的clickable或者longClickable有一个为true，那么它的onTouchEvent就返回true
> 10. onClick会发生的前提是当前View是可点击的，并且收到了down事件和up事件。
> 11. 事件传递总是先传递给父布局，然后再由父布局分发给子View，通过requestDisallowInterceptTouchEvent()方法可以在子View中干预父元素的分发过程，但是ACTION_DOWN，该方法常用来解决两个滑动方向相同的滑动布局嵌套造成的滑动冲突。

### 3. 源码解析

#### 3.1 Activity对事件的分发过程

> 点击事件被封装为MotionEvent，当点击事件发生时，首先传递给当前的Activity，从Activity的dispatchTouchEvent(MotionEvent ev)开始分发事件，具体工作由Activity内的Window完成，Window将事件传递给DecorView，也就是ContentView的父容器。

- Activity.dispatchTouchEvent()

  ```java
  // 触摸屏事件调用，内核通过jni接口触发的
  // MotionEvent是linux通过反射产生的
  public boolean dispatchTouchEvent(MotionEvent ev) {
      if (ev.getAction() == MotionEvent.ACTION_DOWN) {
          onUserInteraction();
      }
    	// getWindow()得到PhoneWindow
      if (getWindow().superDispatchTouchEvent(ev)) {
          return true;
      }
      return onTouchEvent(ev);
  }
  ```

  如果return true执行，表示有子View处理了事件，否则Activity的onTouchEvent方法会执行。

- PhoneWindow.superDispatchTouchEvent()

  ```java
  @Override
  public boolean superDispatchTouchEvent(MotionEvent event) {
      return mDecor.superDispatchTouchEvent(event);
  }
  ```

  PhoneWindow中直接把点击事件传递给DecorView，到这里点击事件已经传递到视图的根View。

#### 3.2 根View对事件的分发过程

- ViewGroup

  ```java
  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
      ...
      final boolean intercepted;
      if (actionMasked == MotionEvent.ACTION_DOWN || mFirstTouchTarget != null) {
          final boolean disallowIntercept = (mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0;
          if (!disallowIntercept) {
              intercepted = onInterceptTouchEvent(ev);
              ev.setAction(action); // restore action in case it was changed
          } else {
              intercepted = false;
          }
      } else {
          // There are no touch targets and this action is not an initial down
          // so this view group continues to intercept touches.
          intercepted = true;
      }
  }
  ```

  DecorView继承自FrameLayout，所以也是ViewGroup。这段代码是ViewGroup.dispatchTouchEvent()中的一小段代码，描述的是ViewGroup是否拦截点击事件。

  ViewGroup判断是否拦截事件的两种情况：1.ACTION_DOWN事件、2.mFirstTouchTarget != null，mFirstTouchTarget的意思是，**当ViewGroup不拦截事件并且交给子View处理时，mFirstTouchTarget不为null，如果事件被ViewGroup拦截mFirstTouchTarget为null。**如果事件被ViewGroup拦截，当ACTION_MOVE和ACTION_UP事件到来时，if (actionMasked == MotionEvent.ACTION_DOWN || mFirstTouchTarget != null)这个判断为false，则ViewGroup的onInterceptTouchEvent不再执行，并且同一序列中的其他事件都默认交给它处理。

- ViewGroup不拦截事件时，事件会向下分发并交给子View处理

  ```java
   @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
      ...
          final View [] children = mChildren;
          for (int i = childrenCount - 1; i >= 0; i--) {
              final int childIndex = getAndVerifyPreorderedIndex(
                  childrenCount, i, customOrder
              );
              final View child = getAndVerifyPreorderedView(
                  preorderedList, children, childIndex
              );
  
              // If there is a view that has accessibility focus we want it
              // to get the event first and if not handled we will perform a
              // normal dispatch. We may do a double iteration but this is
              // safer given the timeframe.
              if (childWithAccessibilityFocus != null) {
                  if (childWithAccessibilityFocus != child) {
                      continue;
                  }
                  childWithAccessibilityFocus = null;
                  i = childrenCount - 1;
              }
  
              // 判断子View没有在播放动画，子View不在点击范围里，执行continue
              if (!canViewReceivePointerEvents(child)
                  || !isTransformedTouchPointInView(x, y, child, null)) {
                  ev.setTargetAccessibilityFocus(false);
                  continue;
              }
  
              newTouchTarget = getTouchTarget(child);
              if (newTouchTarget != null) {
                  // Child is already receiving touch within its bounds.
                  // Give it the new pointer in addition to the ones it is handling.
                  newTouchTarget.pointerIdBits | = idBitsToAssign;
                  break;
              }
  
              resetCancelNextUpFlag(child);
              // dispatchTransformedTouchEvent()调用子元素的dispatchTouchEvent()
              // 如果子View返回true，if判断为true
              if (dispatchTransformedTouchEvent(ev, false, child, idBitsToAssign)) {
                  // Child wants to receive touch within its bounds.
                  mLastTouchDownTime = ev.getDownTime();
                  if (preorderedList != null) {
                      // childIndex points into presorted list, find original index
                      for (int j = 0; j < childrenCount; j++) {
                          if (children[childIndex] == mChildren[j]) {
                              mLastTouchDownIndex = j;
                              break;
                          }
                      }
                  } else {
                      mLastTouchDownIndex = childIndex;
                  }
                  mLastTouchDownX = ev.getX();
                  mLastTouchDownY = ev.getY();
                  newTouchTarget = addTouchTarget(child, idBitsToAssign);
                  alreadyDispatchedToNewTouchTarget = true;
                  break;
              }
  
              // The accessibility focus didn't handle the event, so clear
              // the flag and do a normal dispatch to all children.
              ev.setTargetAccessibilityFocus(false);
          }
  }
  ```

  先注意到dispatchTransformedTouchEvent()方法，这个方法中将事件交给子View分发，其中部分代码如下。

- dispatchTransformedTouchEvent()

  ```java
  	if (child == null) {
          handled = super.dispatchTouchEvent(transformedEvent);
      } else {
          final float offsetX = mScrollX - child.mLeft;
          final float offsetY = mScrollY - child.mTop;
          transformedEvent.offsetLocation(offsetX, offsetY);
          if (! child.hasIdentityMatrix()) {
              transformedEvent.transform(child.getInverseMatrix());
          }
          handled = child.dispatchTouchEvent(transformedEvent);
      }
  ```

  传递过来的child不为null，则调用子View的dispatchTouchEvent()方法，完成一轮事件的传递。如果子View返回true，则if(dispatchTransformedTouchEvent(ev, false, child, idBitsToAssign))为true，mFirstTouchTarget被赋值，代码如下：

- addTouchTarget()

  ```java
  private TouchTarget addTouchTarget(@NonNull View child, int pointerIdBits) {
          final TouchTarget target = TouchTarget.obtain(child, pointerIdBits);
          target.next = mFirstTouchTarget;
          mFirstTouchTarget = target;
          return target;
      }
  ```

  再次说明一下mFirstTouchTarget的含义：**ViewGroup不拦截事件，交给子View处理，则mFirstTouchTarget被赋值为子View。**

  如果遍历所有子View后事件没有被合适地处理，有两种情况：1.ViewGroup没有子元素，2.子元素处理了事件但是dispatchTouchEvent()返回false，所以mFirstTouchTarget依然为null，这时候ViewGroup会自己处理点击事件：

- ViewGroup自己处理点击事件

  ```java
  if (mFirstTouchTarget == null) {
          // No touch targets so treat this as an ordinary view.
          handled = dispatchTransformedTouchEvent(
              ev, canceled, null,
              TouchTarget.ALL_POINTER_IDS
          );
      }
  ```

  如果ViewGroup自己处理事件，第三个参数child为null，结合上述dispatchTransformedTouchEvent()方法分析，最终会走：

- dispatchTransformedTouchEvent()

  ```java
  if (child == null) {
      handled = super.dispatchTouchEvent(transformedEvent);
  }
  ```

  点击事件转到View的dispatchTouchEvent()方法

#### 3.3 View对点击事件的处理过程

- dispatchTouchEvent()

  ```java
      public boolean dispatchTouchEvent(MotionEvent event) {
          boolean result = false;
          ...
          if (onFilterTouchEventForSecurity(event)) {
              if ((mViewFlags & ENABLED_MASK) == ENABLED && handleScrollBarDragging(event)) {
                  result = true;
              }
              //noinspection SimplifiableIfStatement
              ListenerInfo li = mListenerInfo;
              // 判断有没有设置OnTouchListener（li != null，设置了OnTouchListener或者OnClickListener就不会空）
              // 如果li.mOnTouchListener.onTouch(this, event)返回true，则result=true执行
              if (li != null && li.mOnTouchListener != null
                      && (mViewFlags & ENABLED_MASK) == ENABLED
                      && li.mOnTouchListener.onTouch(this, event)) {
                  result = true;
              }
  
              // 如果onTouch(this, event)返回true，则onTouchEvent()不执行
              if (!result && onTouchEvent(event)) {
                  result = true;
              }
          }
          ...
          return result;
      }
  ```

  View（不含ViewGroup）没有子元素，不需要考虑事件的分发，只能自己处理事件。从上面代码可以看出onTouch()优先级高于onTouchEvent()，并且onTouch()返回true时，onTouchEvent()不执行。

- View.OnTouchEvent()

  ```java
          if ((viewFlags & ENABLED_MASK) == DISABLED) {
              if (action == MotionEvent.ACTION_UP && (mPrivateFlags & PFLAG_PRESSED) != 0) {
                  setPressed(false);
              }
              mPrivateFlags3 &= ~PFLAG3_FINGER_DOWN;
              // A disabled view that is clickable still consumes the touch
              // events, it just doesn't respond to them.
              return clickable;
          }
  ```

  当View处于不可用状态时，也会消耗点击事件。

- View.OnTouchEvent()

  ```java
  /*
  OnContextClickListener是在Android6.0(API 23)才添加的一个选项，是用于检测外部设备上的按钮是否按下的，例如蓝牙触控笔上的按钮，一般情况下，忽略即可，影响CONTEXT_CLICKABLE标志位.
  */
  final boolean clickable = ((viewFlags & CLICKABLE) == CLICKABLE
                  || (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE)
                  || (viewFlags & CONTEXT_CLICKABLE) == CONTEXT_CLICKABLE;
  
  // 只要CLICKABLE、LONG_CLICKABLE、CONTEXT_CLICKABLE中有一个为true，则View会消耗这个事件，即onTouchEvent()放回true，不管是不是DISABLE状态。
  if (clickable || (viewFlags & TOOLTIP) == TOOLTIP) {
          switch (action) {
              case MotionEvent.ACTION_UP:
                  boolean prepressed = (mPrivateFlags & PFLAG_PREPRESSED) != 0;
                  if ((mPrivateFlags & PFLAG_PRESSED) != 0 || prepressed) {
                      if (!mHasPerformedLongPress && !mIgnoreNextUpEvent) {
                          // This is a tap, so remove the longpress check
                          removeLongPressCallback();
  
                          // Only perform take click actions if we were in the pressed state
                          if (!focusTaken) {
                              // Use a Runnable and post this rather than calling
                              // performClick directly. This lets other visual state
                              // of the view update before click actions start.
                              if (mPerformClick == null) {
                                  mPerformClick = new PerformClick();
                              }
                              if (!post(mPerformClick)) {
                                  // 调用performClick()
                                  performClickInternal();
                              }
                          }
                      }
                      ...
                  }
             		break;
              case MotionEvent.ACTION_DOWN:
                  // 是否在滚动的容器内
                  boolean isInScrollingContainer = isInScrollingContainer();
                  if (isInScrollingContainer) {
                          mPrivateFlags |= PFLAG_PREPRESSED;
                          if (mPendingCheckForTap == null) {
                              mPendingCheckForTap = new CheckForTap();
                          }
                          mPendingCheckForTap.x = event.getX();
                          mPendingCheckForTap.y = event.getY();
                          postDelayed(mPendingCheckForTap, ViewConfiguration.getTapTimeout());
                      } else {
                          // Not inside a scrolling container, so show the feedback right away
                          setPressed(true, x, y);
                      	// 触发onLongClick()事件
                          checkForLongClick(
                                  ViewConfiguration.getLongPressTimeout(),
                                  x,
                                  y,
                                  TOUCH_GESTURE_CLASSIFIED__CLASSIFICATION__LONG_PRESS);
                      }
                      break;
          }
      	...
          return true;
      }
  ```

  onTouchEvent()方法中，只要CLICKABLE、LONG_CLICKABLE、CONTEXT_CLICKABLE中有一个为true，最终会返回true，消费事件。当ACTION_UP事件发生时，会触发performClick()

- performClick()

  ```java
  // NOTE: other methods on View should not call this method directly, but performClickInternal()
      // instead, to guarantee that the autofill manager is notified when necessary (as subclasses
      // could extend this method without calling super.performClick()).
      public boolean performClick() {
          // We still need to call this method to handle the cases where performClick() was called
          // externally, instead of through performClickInternal()
          notifyAutofillManagerOnClick();
          final boolean result;
          final ListenerInfo li = mListenerInfo;
          if (li != null && li.mOnClickListener != null) {
              playSoundEffect(SoundEffectConstants.CLICK);
              // 点击事件回调
              li.mOnClickListener.onClick(this);
              result = true;
          } else {
              result = false;
          }
          sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
          notifyEnterOrExitForAutoFillIfNeeded(true);
          return result;
      }
  ```

  如果View设置了OnClickListener，则调用其onClick()，所以这里能够得出performClick()的作用，**如果View设置了OnClickListener，则调用其onClick方法。**结合上述onTouch()、onTouchEvent()的执行顺序，可以得出：

  > 优先级
  >
  > 1. onTouch() -> **（View.dispatchTouchEvent中）**
  > 2. onTouchEvent() -> **（View.dispatchTouchEvent中，且onTouch返回false）**
  > 3. onLongClick() -> **（View.onTouchEvent中，ACTION_DOWN时）**
  > 4.  onClick() -> **（View.onTouchEvent中，ACTION_UP时）**

- CLICKABLE、LONG_CLICKABLE等标志位的默认值

  ```java
  public void setOnClickListener(@Nullable OnClickListener l) {
          if (!isClickable()) {
              setClickable(true);
          }
          getListenerInfo().mOnClickListener = l;
      }
      
      public void setOnLongClickListener(@Nullable OnLongClickListener l) {
          if (!isLongClickable()) {
              setLongClickable(true);
          }
          getListenerInfo().mOnLongClickListener = l;
      }
  ```

  View的LONG_CLICKABLE属性默认为false，而CLICKABLE属性与具体的View有关，可点击的View例如Button的CLICKABLE标志位默认为true，不可点击的View例如TextView的CLICKABLE标志位默认为false，通过setClickable()和setLongClickable()可以改变View的标志位，此外，setOnClickListener会自动将View的CLICKABLE设置为true，setOnLongClickListener()同理:


### 4 解决滑动冲突

> 滑动冲突指，在两个或两个以上的可滑动的View的嵌套下，例如ListView、RecyclerView、ScrollView等相互嵌套时，可能产生的滑动冲突（系统选择的事件分发与我们预期的不符合），这时我们需要手动处理。

#### 4.1 滑动冲突场景

##### 4.1.1 内外滑动方向不同

- 冲突场景1

  ![](D:\Note\PicBed\scollCrash1.png)

<center>图3-1 滑动冲突场景1</center>
- 可能的组合

  1. 外部：ViewPager

     内部：Fragment

  2. 外部：ScrollView

     内部：ListView

  造成冲突场景1的原因：**内外布局的滑动方向相反，系统没有将事件正确地分发给对应布局。**例如外部是横向HorizontalScrollView，内部是ListView，外部HorizontalScrollView把纵向滑动事件也拦截，内部ListView就无法收到。

##### 4.1.2 内外滑动方向相同

- 冲突场景2

  ![](D:\Note\PicBed\scollCrash2.jpg)

<center>图3-2 滑动冲突场景2</center>
- 可能的组合

  1. 外部ScrollView

     内部ListView

  2. 外部ScrollView

     内部GridView

  造成冲突场景2的原因：**内外布局滑动方向相同，当我们想滑动内布局时，触摸事件会被外布局拦截。**

#### 4.2 解决滑动冲突

##### 4.2.1 外部拦截法

> 外部拦截法指由外布局决定事件自己消费或者分发给内布局，用于解决冲突场景1

- ViewPager解决滑动冲突

> 如果在ViewPager内部嵌入一个ListView，则事件的分发由ViewPager决定，具体在ViewPager的onInterceptTouchEvent()中控制

- ViewPager.onInterceptTouchEvent

  ```java
  @Override
      public boolean onInterceptTouchEvent(MotionEvent ev) {
          //一些ViewPager拖拽的标志位要设置，必调super，否则看不到效果
          super.onInterceptTouchEvent(ev);
          boolean isIntercepted = false;
  
          switch (ev.getAction()) {
              case MotionEvent.ACTION_DOWN:
                  Log.d(TAG, "onInterceptTouchEvent: down");
                  break;
              case MotionEvent.ACTION_MOVE:
                  if (needEvent(ev)) {
                      isIntercepted = true;
                  }
                  Log.d(TAG, "onInterceptTouchEvent: move " + isIntercepted);
                  break;
              case MotionEvent.ACTION_UP:
                  // 拦截了UP事件，子View点击事件失效
                  Log.d(TAG, "onInterceptTouchEvent: up222");
                  //isIntercepted = true;
                  break;
              default:
          }
          mLastX = (int) ev.getX();
          mLastY = (int) ev.getY();
          return isIntercepted;
      }
  
      private boolean needEvent(MotionEvent ev) {
          //水平滚动距离大于垂直滚动距离则将事件交由ViewPager处理
          return Math.abs(ev.getX() - mLastX) > Math.abs(ev.getY() - mLastY);
      }
  }
  ```

  主要思路是判断当前滑动的方向，如果水平滑动距离大于垂直滑动距离则ViewPager自己处理，否则不拦截事件，事件会传递给ListView

##### 4.2.2 内部拦截法

> 内部拦截法指**内布局对事件触摸做出判断，并决定由自己处理或者让父布局处理。**

- ScrollView与ListView嵌套解决滑动冲突

> 外布局ScrollView、内布局ListView，手指滑动ListView时，事件依然被ScrollView处理，采用内部拦截法解决冲突，**由内布局ListView干预父布局对事件的拦截。**

- MyListView

  ```java
  public class MyListView extends ListView implements AbsListView.OnScrollListener {
  
      private boolean isScrollToTop;
      private boolean isScrollToBottom;
      private int mLastX;
      private int mLastY;
      ...
  
      public boolean onInterceptTouchEvent(MotionEvent ev) {
          return super.onInterceptTouchEvent(ev);
      }
  
      @Override
      public boolean onTouchEvent(MotionEvent ev) {
          return super.onTouchEvent(ev);
      }
  
      @Override
      public boolean dispatchTouchEvent(MotionEvent ev) {
          switch (ev.getAction()) {
              case MotionEvent.ACTION_DOWN:
                  getParent().requestDisallowInterceptTouchEvent(true);
                  mLastX = (int) ev.getX();
                  mLastY = (int) ev.getY();
                  break;
              case MotionEvent.ACTION_MOVE:
                  if (superDispatchMoveEvent(ev)) {
                      getParent().requestDisallowInterceptTouchEvent(false);
                  }
                  break;
              case MotionEvent.ACTION_UP:
                  LogUtil.d("Action_up");
                  break;
              default:
                  break;
          }
          return super.dispatchTouchEvent(ev);
      }
  
      private boolean superDispatchMoveEvent(MotionEvent ev) {
          boolean canScrollBottom = isScrollToTop && (ev.getY() - mLastY) > 0;
          boolean canScrollTop = isScrollToBottom && (ev.getY() - mLastY) < 0;
          return canScrollBottom || canScrollTop;
      }
  
      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {
  
      }
  
      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
          isScrollToBottom = false;
          isScrollToTop = false;
          if (firstVisibleItem == 0) {
              View topView = getChildAt(0);
              if (topView!=null && topView.getTop()==0) {
                  isScrollToTop = true;
                  LogUtil.d("滚动到顶部");
              }
          }
  
          if ((firstVisibleItem+visibleItemCount) == totalItemCount) {
              View lastView = getChildAt(getChildCount()-1);
              if (lastView!=null && lastView.getBottom()==getHeight()) {
                  isScrollToBottom = true;
              }
          }
      }
  }
  ```

- 关键代码

  ```java
  @Override
      public boolean dispatchTouchEvent(MotionEvent ev) {
          switch (ev.getAction()) {
              case MotionEvent.ACTION_DOWN:
                  getParent().requestDisallowInterceptTouchEvent(true);
                  mLastX = (int) ev.getX();
                  mLastY = (int) ev.getY();
                  break;
              case MotionEvent.ACTION_MOVE:
                  if (superDispatchMoveEvent(ev)) {
                      getParent().requestDisallowInterceptTouchEvent(false);
                  }
                  break;
              case MotionEvent.ACTION_UP:
                  LogUtil.d("Action_up");
                  break;
              default:
                  break;
          }
          return super.dispatchTouchEvent(ev);
      }
  ```

  DOWN事件中getParent().requestDisallowInterceptTouchEvent(true)干预了父元素（外布局）的事件分发过程，后续的MOVE事件会传递到该ListView，superDispatchMoveEvent()用于判断ListView是否滑动到顶/底部，是则getParent().requestDisallowInterceptTouchEvent(false)，父布局能够正常接收事件。

### 5.   Q&A

#### 5.1 为什么手指按下按钮，然后移动到按钮区域外，点击时间就不触发了？

- View#onTouchEvent(MotionEvent event)

  ```java
      public boolean onTouchEvent(MotionEvent event) {
          if (clickable || (viewFlags & TOOLTIP) == TOOLTIP) {
              switch (action) {
                  case MotionEvent.ACTION_UP:
                      boolean prepressed = (mPrivateFlags & PFLAG_PREPRESSED) != 0;
                  		// press状态为false，这里进不去
                      if ((mPrivateFlags & PFLAG_PRESSED) != 0 || prepressed) {
                          if (!mHasPerformedLongPress && !mIgnoreNextUpEvent) {
                              removeLongPressCallback();
                              if (!focusTaken) {
                                	// 触发点击事件
                                  if (mPerformClick == null) {
                                      mPerformClick = new PerformClick();
                                  }
                                  if (!post(mPerformClick)) {
                                      performClickInternal();
                                  }
                              }
                          }
                      break;
                  case MotionEvent.ACTION_MOVE:
                  		// 触摸事件不在view区域内，设置press状态为false
                      if (!pointInView(x, y, touchSlop)) {
                          removeTapCallback();
                          removeLongPressCallback();
                          if ((mPrivateFlags & PFLAG_PRESSED) != 0) {
                              setPressed(false);
                          }
                          mPrivateFlags3 &= ~PFLAG3_FINGER_DOWN;
                      }
                      break;
              }
              return true;
          }
          return false;
      }
  ```

