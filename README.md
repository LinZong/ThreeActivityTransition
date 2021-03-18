## ThreeActivityTransition Demo

This repo will show you how to support shared elements animations among multi activities (>= 3) **using reflection**.

### Can we do this without reflection?

Still no idea (2021.3) and here is my opinion.

> If you happen to speak Chinese, you can also refer to [中文版](./README_CN.md).

**Why?**

1. Assuming there are 3 activities, A => B => C, (A in the bottom), calling from`supportFinishAfterTransition()` in C, on API 29 platform.

2. Results in calling `mActivityTransitionState.startExitBackTransition(this)`, it will construct an `ExitTransitionCoordinator` which **isReturning is TRUE** , then field `mIsBackgroundReady` will be false.

   ```java
   mReturnExitCoordinator = new ExitTransitionCoordinator(activity,
                           activity.getWindow(), activity.mEnterTransitionListener, pendingExitNames,
                           null, null, true);
   
   
   public ExitTransitionCoordinator(Activity activity, Window window,
           SharedElementCallback listener, ArrayList<String> names,
           ArrayList<String> accepted, ArrayList<View> mapped, boolean isReturning) {
       super(window, names, listener, isReturning);
       viewsReady(mapSharedElements(accepted, mapped));
       stripOffscreenViews();
       mIsBackgroundReady = !isReturning; // here
       mActivity = activity;
   }
   ```

3. In B's `performStart()` will call `mActivityTransitionState.enterReady(this)`, at that time `sharedElementNames` is still containing the shared elements' name. But `sharedElementNames` will just assign to `mAllSharedElementNames` in `EnterTransitionCoordinator`, `mPendingExitNames` won't get the value from `mAllSharedElementNames`  without receiving message `MSG_ALLOW_RETURN_TRANSITION`.

4. In `ExitTransitionCoordinator`, only one piece will send such message:

   ```java
   protected void notifyComplete() {
       if (isReadyToNotify()) {
           if (!mSharedElementNotified) {
               mSharedElementNotified = true;
               delayCancel();
   
               if (!mActivity.isTopOfTask()) {
                   // here
                   mResultReceiver.send(MSG_ALLOW_RETURN_TRANSITION, null);
               }
   
               if (mListener == null) {
                   mResultReceiver.send(MSG_TAKE_SHARED_ELEMENTS, mSharedElementBundle);
                   notifyExitComplete();
               } else {
                   final ResultReceiver resultReceiver = mResultReceiver;
                   final Bundle sharedElementBundle = mSharedElementBundle;
                   mListener.onSharedElementsArrived(mSharedElementNames, mSharedElements,
                           new OnSharedElementsReadyListener() {
                               @Override
                               public void onSharedElementsReady() {
                                   resultReceiver.send(MSG_TAKE_SHARED_ELEMENTS,
                                           sharedElementBundle);
                                   notifyExitComplete();
                               }
                           });
               }
           } else {
               notifyExitComplete();
           }
       }
   }
   ```

5. But `isReadyToNotify()` won't return `true` since `mIsBackgroundReady` is false while `isReturning` is true.

   ```java
   protected boolean isReadyToNotify() {
       return mSharedElementBundle != null && mResultReceiver != null && mIsBackgroundReady;
   }
   ```

6. Therefore, no way to let B's `mEnterTransitionCoordinator.getPendingExitSharedElementNames()` return a non-null value without reflection. Since `mActivityTransitionState` is hidden in reflection we also need to bypass it, finally lead to such 'ugly' solution provided in repo.

**Really need to share elements among multi 'pages'?**

Through sharing elements among activities seems to be tricky, but everything turns good if we use Fragment instead of Activity. I had added another sample for fragment scenario. Here are also some docs and posts written by Google.

https://developer.android.com/guide/fragments/animate#kotlin

https://android-developers.googleblog.com/2018/02/continuous-shared-element-transitions.html

Good luck!
