## 三Activity共享元素Demo

这是一个演示如何在三个Activity之间共享元素的Demo，理论上也支持更多的Activity。

### 能不能不用反射？

目前为止笔者没有找到反射以外的方法(2021.3)，下面解释为什么？

**为什么必须用反射?**

1. 假设现在有三个Activity，A => B => C，A在Activity栈底部。从Activity C中调用`supportFinishTransition()`，环境为API 29 (Android 10)。

2. 最终调用到`mActivityTransitionState.startExitBackTransition(this)`，代码里面会创建一个`ExitTransitionCoordinator`对象，其构造方法中**isReturning字段为true**，所以进到构造方法里面之后，`mIsBackgroundReady` 将为false。

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

3. Activity B的`performStart()`方法里面会调用`mActivityTransitionState.enterReady(this)`，尽管此时`sharedElementNames`能正常获取到，里面包含着从C返回B时需要过渡的transitionName，但是在`EnterTransitionCoordinator`的构造过程中，`sharedElementNames`只会通过构造函数赋值给`mAllSharedElementNames` ，而`mPendingExitNames` 只有等到接收到`MSG_ALLOW_RETURN_TRANSITION`消息且`mIsCancel`为false，才会从`mAllSharedElementNames`  里面拿值。

4. 在`ExitTransitionCoordinator`里面，只有一行代码会发`MSG_ALLOW_RETURN_TRANSITION`消息:

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

5. 但是很不幸，因为`mIsBackgroundReady`为false，所以 `isReadyToNotify()`不可能返回true,这行发消息的代码永远不会执行到。

6. 因此，从B返回A时，除非上反射，否则`mEnterTransitionCoordinator.getPendingExitSharedElementNames()`没有办法返回非null的结果，导致`startExitBackTransition()`返回false，不能正常的进行B返回A的共享元素过度动画。而`mActivityTransitionState` 正常情况下反射又是拿不到的，所以最终想在多个Activity之间共享元素只能像这个例子给出的代码一样先bypass反射限制，再反射强塞`mPendingExitNames`。

   > 这种解决方案最多只能用在个人写着玩的项目里，肯定没办法搬到生产环境的，正常的TL看到开发玩这种野路子，一定会骂死你。



**真的需要在多个'页面'共享元素?**

虽然多个Activity之间共享元素很难搞，但是如果不用Activity，而是用Fragment代替'页面'的概念的话，实现起来就非常容易，Fragment下这样的行为是天然支持的。这次更新笔者也随手写了一个三Fragment间共享元素的例子，补充到代码里面去了，读者可以参考借鉴，同时下面给出两篇谷歌的文档，里面描述了共享元素的最佳实践，也可以供参考。

https://developer.android.com/guide/fragments/animate#kotlin

https://android-developers.googleblog.com/2018/02/continuous-shared-element-transitions.html

祝你好运！