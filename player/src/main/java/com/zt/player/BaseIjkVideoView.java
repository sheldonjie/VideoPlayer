package com.zt.player;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by zhouteng on 2017/5/22.
 */

public abstract class BaseIjkVideoView extends IjkVideoView implements View.OnClickListener {


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

    protected abstract void playBtnClick();    //播放按钮按下时，UI回调

    protected abstract void pauseBtnClick();   //暂停按钮按下时,UI回调

    protected abstract int getBackBtnId();   //返回按钮ID

    protected abstract void setBufferProgress(int bufferProgress); //显示缓冲百分比

    protected void resetProgressAndTime() {} //重置进度条时间，当前时间，总时间

    protected void startProgressTimer() {} //开始进度条任务

    protected void cancelProgressTimer(){}  //取消进度条任务

    //endregion

    //region View Interaction

    private View playBtn;
    private View backBtn;

    private boolean isFullScreen;

    private final void initView(Context context) {

        surfaceContainer.setOnClickListener(this);

        playBtn = findViewById(getPlayBtnId());
        playBtn.setOnClickListener(this);

        backBtn = findViewById(getBackBtnId());
        backBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == getPlayBtnId()) {
            if (isPlaying()) {
                pause();
                pauseBtnClick();
            } else {
                start();
                playBtnClick();
            }
        } else if (id == getBackBtnId()) {
            if (isFullScreen) {

            } else {
                exitCurrenActivity();
            }
        } else if (id == getSurfaceContainerId()) {
            surfaceContainerClick();
        }
    }

    protected void exitCurrenActivity() {
        release(true);
        CTUtils.exitActivity(getContext());
    }

    protected void exitFullScreen() {

    }

    protected void startFullScreen() {

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
