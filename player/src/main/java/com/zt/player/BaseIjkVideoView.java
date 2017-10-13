package com.zt.player;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * Created by zhouteng on 2017/5/22.
 */

public abstract class BaseIjkVideoView extends IjkVideoView implements View.OnClickListener {

    public static final int FULLSCREEN_ID = 85597;

    public BaseIjkVideoView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public BaseIjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public BaseIjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BaseIjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    //region Custom LayoutId

    protected abstract void surfaceContainerClick(); //视频区域点击事件回调

    protected abstract int getPlayBtnId();   //布局中播放按钮ID

    protected abstract int getBackBtnId();   //返回按钮ID

    protected abstract int getFullScreenBtnId(); //

    protected void resetProgressAndTime() {
    } //重置进度条时间，当前时间，总时间

    protected void startProgressTimer() {
    } //开始进度条任务

    protected void cancelProgressTimer() {
    }  //取消进度条任务

    //endregion

    //region View Interaction

    private View playBtn;
    private View backBtn;
    private View fullScreenBtn;

    private boolean isFullScreen;
    private Dialog fullScreenVideoDialog;
    private ViewParent viewParent;
    private int originViewWidth;
    private int originViewHeight;

    private final void initView(Context context) {

        surfaceContainer.setOnClickListener(this);

        playBtn = findViewById(getPlayBtnId());
        playBtn.setOnClickListener(this);

        backBtn = findViewById(getBackBtnId());
        backBtn.setOnClickListener(this);

        fullScreenBtn = findViewById(R.id.fullscreen);
        fullScreenBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == getPlayBtnId()) {
            handleStartBtnClick();
        } else if (id == getBackBtnId()) {
            if (isFullScreen) {
                exitFullScreen();
            } else {
                exitCurrenActivity();
            }
        } else if (id == getSurfaceContainerId()) {
            surfaceContainerClick();
        } else if (id == getFullScreenBtnId()) {
            handleFullScreenBtnClick();
        }
    }

    private void handleStartBtnClick() {
        if (mCurrentState == STATE_IDLE) {
            prepareToPlay();
        } else if (isPlaying()) {
            pause();
        } else {
            start();
        }
    }

    protected void exitCurrenActivity() {
        release(true);
        CTUtils.exitActivity(getContext());
    }

    private void handleFullScreenBtnClick() {
        if (isFullScreen) {
//            exitFullScreen();
            exitWindowFullscreen();
        } else {
//            startFullScreen();
            startWindowFullscreen(getContext());
        }
    }

    protected void exitFullScreen() {
        isFullScreen = false;

        Activity activity = CTUtils.getActivity(getContext());
        toggledFullscreen(activity, false);

        if (fullScreenVideoDialog != null) {
            fullScreenVideoDialog.dismiss();
        }

        removePlayerFromParent();

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(originViewWidth, originViewHeight);
        setLayoutParams(layoutParams);

        if (viewParent != null) {
            ((ViewGroup) viewParent).addView(this);
        }
    }

    protected void startFullScreen() {

        isFullScreen = true;

        viewParent = getParent();
        originViewWidth = getWidth();
        originViewHeight = getHeight();

        Activity activity = CTUtils.getActivity(getContext());
        toggledFullscreen(activity, true);

        removePlayerFromParent();

        int screenWidth = CTUtils.getScreenWidth(getContext());
        int screenHeight = CTUtils.getScreenHeight(getContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(screenWidth, screenHeight);
        setLayoutParams(layoutParams);

        fullScreenVideoDialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        fullScreenVideoDialog.setContentView(this);
        fullScreenVideoDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    exitFullScreen();
                }
                return false;
            }
        });
        fullScreenVideoDialog.show();


    }

    private void toggledFullscreen(Activity mActivity, boolean fullscreen) {

        if (mActivity == null) {
            return;
        }

        if (fullscreen) {
            WindowManager.LayoutParams attrs = mActivity.getWindow().getAttributes();
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            mActivity.getWindow().setAttributes(attrs);
            if (android.os.Build.VERSION.SDK_INT >= 14) {
                //noinspection all
                mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            WindowManager.LayoutParams attrs = mActivity.getWindow().getAttributes();
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            mActivity.getWindow().setAttributes(attrs);
            if (android.os.Build.VERSION.SDK_INT >= 14) {
                //noinspection all
                mActivity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }

    private void removePlayerFromParent() {
        ViewParent parent = getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(this);
        }
    }

    /**
     * 获取com.android.internal.R.id.content
     * @return
     */
    private ViewGroup getRootViewGroup() {
        Activity activity = CTUtils.getActivity(getContext());
        if(activity != null) {
            return (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        }
        return null;
    }

    private void exitWindowFullscreen() {

        isFullScreen = false;

        ViewGroup vp = getRootViewGroup();
        vp.removeView((View) this.getParent());
        removePlayerFromParent();

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(originViewWidth, originViewHeight);
        setLayoutParams(layoutParams);

        if (viewParent != null) {
            ((ViewGroup) viewParent).addView(this);
        }
    }

    private void startWindowFullscreen(final Context context) {

        isFullScreen = true;

        originViewWidth = getWidth();
        originViewHeight = getHeight();

        CTUtils.hideSupportActionBar(context,true,true);

        viewParent = getParent();

        ViewGroup vp = getRootViewGroup();

        removePlayerFromParent();

        //处理暂停的逻辑
        pauseFullCoverLogic();

        final LayoutParams lpParent = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Color.BLACK);

        LayoutParams lp = new LayoutParams(getWidth(), getHeight());
        frameLayout.addView(this, lp);
        vp.addView(frameLayout, lpParent);
    }

    /**
     * 全屏的暂停的时候返回页面不黑色
     */
    private void pauseFullCoverLogic() {

    }


        /**
         * 隐藏导航栏
         * @param context
         */
    public static void hideNavKey(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //       设置屏幕始终在前面，不然点击鼠标，重新出现虚拟按键
            ((Activity) context).getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
                            // bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        } else {
            ((Activity) context).getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
            );
        }
    }

    //endregion

    protected void changeUIWithState(int currentState) {
        this.mCurrentState = currentState;
        switch (mCurrentState) {
            case STATE_IDLE:
                cancelProgressTimer();
                break;
            case STATE_PREPARING:
                resetProgressAndTime();
                break;
            case STATE_PLAYING:
            case STATE_PAUSED:
                startProgressTimer();
                break;
            case STATE_ERROR:
                cancelProgressTimer();
                break;
            case STATE_PLAYBACK_COMPLETED:
                cancelProgressTimer();
                break;
        }
    }
}
