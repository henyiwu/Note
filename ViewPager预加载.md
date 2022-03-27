[toc]

## ViewPager预加载

### 为什么不使用ViewPager+Activity，而是ViewPager+View

> ViewPager内有适配器，适配的是一个个的View，Fragment的本质是一个View
>
> - 为什么说Fragment本质是个View
>
>   Fragment对View的封装，是使用组合

- ViewPager.populate()

  ```java
  void populate() {    populate(mCurItem);}
  
      void populate(int newCurrentItem) {
          ItemInfo oldCurInfo = null;
          if (mCurItem != newCurrentItem) {
              oldCurInfo = infoForPosition(mCurItem);
              mCurItem = newCurrentItem;
          }
  
          if (mAdapter == null) {
              sortChildDrawingOrder();
              return;
          }
  
          // Bail now if we are waiting to populate.  This is to hold off
          // on creating views from the time the user releases their finger to
          // fling to a new position until we have finished the scroll to
          // that position, avoiding glitches from happening at that point.
          if (mPopulatePending) {
              if (DEBUG) Log.i(TAG, "populate is pending, skipping for now...");
              sortChildDrawingOrder();
              return;
          }
  
          // Also, don't populate until we are attached to a window.  This is to
          // avoid trying to populate before we have restored our view hierarchy
          // state and conflicting with what is restored.
          if (getWindowToken() == null) {
              return;
          }
  
          mAdapter.startUpdate(this);
  
          final int pageLimit = mOffscreenPageLimit;
          final int startPos = Math.max(0, mCurItem - pageLimit);
          final int N = mAdapter.getCount();
          final int endPos = Math.min(N - 1, mCurItem + pageLimit);
  
          if (N != mExpectedAdapterCount) {
              String resName;
              try {
                  resName = getResources().getResourceName(getId());
              } catch (Resources.NotFoundException e) {
                  resName = Integer.toHexString(getId());
              }
              throw new IllegalStateException("The application's PagerAdapter changed the adapter's"
                      + " contents without calling PagerAdapter#notifyDataSetChanged!"
                      + " Expected adapter item count: " + mExpectedAdapterCount + ", found: " + N
                      + " Pager id: " + resName
                      + " Pager class: " + getClass()
                      + " Problematic adapter: " + mAdapter.getClass());
          }
  
          // Locate the currently focused item or add it if needed.
          int curIndex = -1;
          ItemInfo curItem = null;
          for (curIndex = 0; curIndex < mItems.size(); curIndex++) {
              final ItemInfo ii = mItems.get(curIndex);
              if (ii.position >= mCurItem) {
                  if (ii.position == mCurItem) curItem = ii;
                  break;
              }
          }
  
          // 第一次进来，什么数据都没有
          if (curItem == null && N > 0) {
              curItem = addNewItem(mCurItem, curIndex);
          }
  
          // Fill 3x the available width or up to the number of offscreen
          // pages requested to either side, whichever is larger.
          // If we have no current item we have no work to do.
          if (curItem != null) {
              float extraWidthLeft = 0.f;
              int itemIndex = curIndex - 1;
              ItemInfo ii = itemIndex >= 0 ? mItems.get(itemIndex) : null;
              final int clientWidth = getClientWidth();
              final float leftWidthNeeded = clientWidth <= 0 ? 0 :
                      2.f - curItem.widthFactor + (float) getPaddingLeft() / (float) clientWidth;
              // 当前页左边的缓存和去缓存处理
              for (int pos = mCurItem - 1; pos >= 0; pos--) {
                  if (extraWidthLeft >= leftWidthNeeded && pos < startPos) {
                      if (ii == null) {
                          break;
                      }
                      if (pos == ii.position && !ii.scrolling) {
                          // 把左边要销毁的View移除缓存
                          mItems.remove(itemIndex);
                          mAdapter.destroyItem(this, pos, ii.object);
                          if (DEBUG) {
                              Log.i(TAG, "populate() - destroyItem() with pos: " + pos
                                      + " view: " + ((View) ii.object));
                          }
                          itemIndex--;
                          curIndex--;
                          ii = itemIndex >= 0 ? mItems.get(itemIndex) : null;
                      }
                  } else if (ii != null && pos == ii.position) {
                      extraWidthLeft += ii.widthFactor;
                      itemIndex--;
                      ii = itemIndex >= 0 ? mItems.get(itemIndex) : null;
                  } else {
                      ii = addNewItem(pos, itemIndex + 1);
                      extraWidthLeft += ii.widthFactor;
                      curIndex++;
                      ii = itemIndex >= 0 ? mItems.get(itemIndex) : null;
                  }
              }
  
              float extraWidthRight = curItem.widthFactor;
              itemIndex = curIndex + 1;
              if (extraWidthRight < 2.f) {
                  ii = itemIndex < mItems.size() ? mItems.get(itemIndex) : null;
                  final float rightWidthNeeded = clientWidth <= 0 ? 0 :
                          (float) getPaddingRight() / (float) clientWidth + 2.f;
                  // 当前页右边的缓存和去缓存处理
                  for (int pos = mCurItem + 1; pos < N; pos++) {
                      if (extraWidthRight >= rightWidthNeeded && pos > endPos) {
                          if (ii == null) {
                              break;
                          }
                          if (pos == ii.position && !ii.scrolling) {
                              mItems.remove(itemIndex);
                              mAdapter.destroyItem(this, pos, ii.object);
                              if (DEBUG) {
                                  Log.i(TAG, "populate() - destroyItem() with pos: " + pos
                                          + " view: " + ((View) ii.object));
                              }
                              ii = itemIndex < mItems.size() ? mItems.get(itemIndex) : null;
                          }
                      } else if (ii != null && pos == ii.position) {
                          extraWidthRight += ii.widthFactor;
                          itemIndex++;
                          ii = itemIndex < mItems.size() ? mItems.get(itemIndex) : null;
                      } else {
                          ii = addNewItem(pos, itemIndex);
                          itemIndex++;
                          extraWidthRight += ii.widthFactor;
                          ii = itemIndex < mItems.size() ? mItems.get(itemIndex) : null;
                      }
                  }
              }
  
              calculatePageOffsets(curItem, curIndex, oldCurInfo);
  
              mAdapter.setPrimaryItem(this, mCurItem, curItem.object);
          }
  
          if (DEBUG) {
              Log.i(TAG, "Current page list:");
              for (int i = 0; i < mItems.size(); i++) {
                  Log.i(TAG, "#" + i + ": page " + mItems.get(i).position);
              }
          }
  
          mAdapter.finishUpdate(this);
  
          // Check width measurement of current pages and drawing sort order.
          // Update LayoutParams as needed.
          final int childCount = getChildCount();
          for (int i = 0; i < childCount; i++) {
              final View child = getChildAt(i);
              final LayoutParams lp = (LayoutParams) child.getLayoutParams();
              lp.childIndex = i;
              if (!lp.isDecor && lp.widthFactor == 0.f) {
                  // 0 means requery the adapter for this, it doesn't have a valid width.
                  final ItemInfo ii = infoForChild(child);
                  if (ii != null) {
                      lp.widthFactor = ii.widthFactor;
                      lp.position = ii.position;
                  }
              }
          }
          sortChildDrawingOrder();
  
          if (hasFocus()) {
              View currentFocused = findFocus();
              ItemInfo ii = currentFocused != null ? infoForAnyChild(currentFocused) : null;
              if (ii == null || ii.position != mCurItem) {
                  for (int i = 0; i < getChildCount(); i++) {
                      View child = getChildAt(i);
                      ii = infoForChild(child);
                      if (ii != null && ii.position == mCurItem) {
                          if (child.requestFocus(View.FOCUS_FORWARD)) {
                              break;
                          }
                      }
                  }
              }
          }
      }
  ```

- addNewItem()

  ```java
      ItemInfo addNewItem(int position, int index) {
          ItemInfo ii = new ItemInfo();
          ii.position = position;
          // 根据不同类型的adapter返回不同的类型
          // 比如FragmentPagerAdapter返回的是fragment
          ii.object = mAdapter.instantiateItem(this, position);
          ii.widthFactor = mAdapter.getPageWidth(position);
          if (index < 0 || index >= mItems.size()) {
              mItems.add(ii);
          } else {
              mItems.add(index, ii);
          }
          return ii;
      }
  ```

- FragmentViewPager.instantiateItem()

  ```java
      @NonNull
      public Object instantiateItem(@NonNull ViewGroup container, int position) {
          if (this.mCurTransaction == null) {
              this.mCurTransaction = this.mFragmentManager.beginTransaction();
          }
  
          long itemId = this.getItemId(position);
          String name = makeFragmentName(container.getId(), itemId);
          Fragment fragment = this.mFragmentManager.findFragmentByTag(name);
          if (fragment != null) {
              // 如果fragment没有被销毁，直接attach
              this.mCurTransaction.attach(fragment);
          } else {
              fragment = this.getItem(position);
              this.mCurTransaction.add(container.getId(), fragment, makeFragmentName(container.getId(), itemId));
          }
  
          if (fragment != this.mCurrentPrimaryItem) {
              fragment.setMenuVisibility(false);
              if (this.mBehavior == 1) {
                  this.mCurTransaction.setMaxLifecycle(fragment, State.STARTED);
              } else {
                  fragment.setUserVisibleHint(false);
              }
          }
  
          return fragment;
      }
  ```

- FragmentPagerAdapter.setPrimaryItem()

  ```java
      public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
          Fragment fragment = (Fragment)object;
          if (fragment != this.mCurrentPrimaryItem) {
              if (this.mCurrentPrimaryItem != null) {
                  // 旧的当前页设置为不展示
                  this.mCurrentPrimaryItem.setMenuVisibility(false);
                  if (this.mBehavior == 1) {
                      if (this.mCurTransaction == null) {
                          this.mCurTransaction = this.mFragmentManager.beginTransaction();
                      }
  
                      this.mCurTransaction.setMaxLifecycle(this.mCurrentPrimaryItem, State.STARTED);
                  } else {
                      this.mCurrentPrimaryItem.setUserVisibleHint(false);
                  }
              }
  
              // 当前页设置为展示
              fragment.setMenuVisibility(true);
              if (this.mBehavior == 1) {
                  if (this.mCurTransaction == null) {
                      this.mCurTransaction = this.mFragmentManager.beginTransaction();
                  }
  
                  this.mCurTransaction.setMaxLifecycle(fragment, State.RESUMED);
              } else {
                  fragment.setUserVisibleHint(true);
              }
  
              this.mCurrentPrimaryItem = fragment;
          }
      }
  ```

- FragmentPagerAdapter.finishUpdate()

  ```java
      public void finishUpdate(@NonNull ViewGroup container) {
          if (this.mCurTransaction != null) {
              if (!this.mExecutingFinishUpdate) {
                  try {
                      this.mExecutingFinishUpdate = true;
                      this.mCurTransaction.commitNowAllowingStateLoss();
                  } finally {
                      this.mExecutingFinishUpdate = false;
                  }
              }
              this.mCurTransaction = null;
          }
      }
  ```

  finishUpdate是抽象方法，不同适配器有不同的finish方式

### ViewPager预加载在什么时候

> ViewPager三步：缓存页面、设置当前页面、销毁页面
>
> 预加载在缓存页面设置