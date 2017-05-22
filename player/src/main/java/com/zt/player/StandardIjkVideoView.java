package com.zt.player;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhouteng on 2017/5/22.
 */

public class StandardIjkVideoView extends BaseIjkVideoView {

    private ImageView startButton;
    private ViewGroup bottomContainer;
    private ViewGroup topContainer;
    private TextView totalTimeText;
    private TextView currentTimeText;
    private ProgressBar bottomProgressbar;

    public StandardIjkVideoView(@NonNull Context context) {
        super(context);
        initView();
    }

    public StandardIjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public StandardIjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public StandardIjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        startButton = (ImageView) findViewById(R.id.start);
        bottomContainer = (ViewGroup) findViewById(R.id.layout_bottom);
        topContainer = (ViewGroup) findViewById(R.id.layout_top);
        totalTimeText = (TextView)findViewById(R.id.total);
        currentTimeText = (TextView)findViewById(R.id.current);
        bottomProgressbar = (ProgressBar)findViewById(R.id.bottom_progressbar);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.standard_video_layout;
    }

    @Override
    protected int getSurfaceContainerId() {
        return R.id.surface_container;
    }

    @Override
    protected int getPlayBtnId() {
        return R.id.start;
    }

    @Override
    protected int getBackBtnId() {
        return R.id.back;
    }

    @Override
    protected void setCurrentProgress() {
    }

    @Override
    protected void setTotalProgress() {
        totalTimeText.setText(CTUtils.stringForTime(getDuration()));
    }

    @Override
    protected void setBufferProgress(int bufferProgress) {
        bottomProgressbar.setSecondaryProgress(bufferProgress);
    }

    @Override
    protected void playBtnClick() {
        startButton.setImageResource(R.drawable.ct_click_pause_selector);
        startButton.setVisibility(View.INVISIBLE);
        topContainer.setVisibility(View.INVISIBLE);
        bottomContainer.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void pauseBtnClick() {
        startButton.setImageResource(R.drawable.ct_click_play_selector);
    }

    @Override
    protected void surfaceContainerClick() {
        toggleControlView();
        startControlViewTimer();
    }

    //region  dismiss view control timer

    private Timer mControlViewTimer;
    private ControlViewTimerTask mControlViewTimerTask;


    private void startControlViewTimer() {
        cancelDismissControlViewTimer();
        mControlViewTimer = new Timer();
        mControlViewTimerTask = new ControlViewTimerTask();
        mControlViewTimer.schedule(mControlViewTimerTask, 2500);
    }

    private void cancelDismissControlViewTimer() {
        if (mControlViewTimer != null) {
            mControlViewTimer.cancel();
        }
        if (mControlViewTimerTask != null) {
            mControlViewTimerTask.cancel();
        }
    }

    private class ControlViewTimerTask extends TimerTask {

        @Override
        public void run() {
            if (isInPlaybackState()) {
                if (getContext() != null && getContext() instanceof Activity) {
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bottomContainer.setVisibility(View.INVISIBLE);
                            topContainer.setVisibility(View.INVISIBLE);
                            startButton.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        }
    }

    private void toggleControlView() {
        if (mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE) {
            bottomContainer.setVisibility(bottomContainer.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
            topContainer.setVisibility(topContainer.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
            startButton.setVisibility(startButton.getVisibility() == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
        }
    }


    //endregion

}
