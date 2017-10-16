package com.zt.player;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.FrameLayout;

/**
 * Created by zhouteng on 2017/5/22.
 */

public abstract class BaseIjkVideoView extends IjkVideoView implements View.OnClickListener {

    private int mSystemUiVisibility;

    //是否需要在利用window实现全屏幕的时候隐藏actionbar
    protected boolean mActionBar = false;

    //是否需要在利用window实现全屏幕的时候隐藏statusbar
    protected boolean mStatusBar = false;

    //region 构造函数

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

    //endregion

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
                exitWindowFullscreen();
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
            exitWindowFullscreen();
        } else {
            startWindowFullscreen(true,true);
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

        CTUtils.showSupportActionBar(getContext(),mActionBar,mStatusBar);

        CTUtils.toggledFullscreen(getContext(),false);

        CTUtils.showNavKey(getContext(),mSystemUiVisibility);
    }

    private void startWindowFullscreen(boolean mActionBar,boolean mStatusBar) {

        isFullScreen = true;

        mSystemUiVisibility = ((Activity) getContext()).getWindow().getDecorView().getSystemUiVisibility();

        originViewWidth = getWidth();
        originViewHeight = getHeight();

        this.mActionBar = mActionBar;
        this.mStatusBar = mStatusBar;

        CTUtils.hideSupportActionBar(getContext(),mActionBar,mStatusBar);

        CTUtils.toggledFullscreen(getContext(),true);

        viewParent = getParent();

        ViewGroup vp = getRootViewGroup();

        removePlayerFromParent();

        //处理暂停的逻辑
        pauseFullCoverLogic();

        final LayoutParams lpParent = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final FrameLayout frameLayout = new FrameLayout(getContext());
        frameLayout.setBackgroundColor(Color.BLACK);

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        frameLayout.addView(this, lp);
        vp.addView(frameLayout, lpParent);

        CTUtils.hideNavKey(getContext());
    }

    /**
     * 全屏的暂停的时候返回页面不黑色
     */
    private void pauseFullCoverLogic() {

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
