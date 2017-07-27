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
import android.widget.SeekBar;
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
    private SeekBar seekBar;

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
        totalTimeText = (TextView) findViewById(R.id.total);
        currentTimeText = (TextView) findViewById(R.id.current);
        bottomProgressbar = (ProgressBar) findViewById(R.id.bottom_progressbar);
        seekBar = (SeekBar) findViewById(R.id.progress);
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
    protected void setBufferProgress(int bufferProgress) {
        bottomProgressbar.setSecondaryProgress(bufferProgress);
    }

    @Override
    protected void resetProgressAndTime() {
        bottomProgressbar.setProgress(0);
        bottomProgressbar.setSecondaryProgress(0);
        currentTimeText.setText(CTUtils.stringForTime(0));
        totalTimeText.setText(CTUtils.stringForTime(0));
    }

    //region Progress Timer

    private Timer updateProgressTimer;
    private ProgressTimerTask mProgressTimerTask;
    private boolean mTouchingProgressBar = false;

    @Override
    protected void startProgressTimer() {
        cancelProgressTimer();
        updateProgressTimer = new Timer();
        mProgressTimerTask = new ProgressTimerTask();
        updateProgressTimer.schedule(mProgressTimerTask, 0, 300);
    }

    @Override
    protected void playCompleted() {
        cancelProgressTimer();
        seekBar.setProgress(100);
        currentTimeText.setText(totalTimeText.getText());
    }

    @Override
    protected void playError() {
        cancelProgressTimer();
    }

    @Override
    protected void cancelProgressTimer() {
        if (updateProgressTimer != null) {
            updateProgressTimer.cancel();
        }
        if (mProgressTimerTask != null) {
            mProgressTimerTask.cancel();
        }
    }

    private class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            if (isInPlaybackState()) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        setProgressAndText();
                    }
                });
            }
        }
    }

    protected void setProgressAndText() {
        int position = getCurrentPosition();
        int duration = getDuration();
        int progress = position * 100 / (duration == 0 ? 1 : duration);
        if (!mTouchingProgressBar) {
            if (progress != 0) {
                bottomProgressbar.setProgress(progress);
                seekBar.setProgress(progress);
            }
        }
        if (position != 0) currentTimeText.setText(CTUtils.stringForTime(position));
        totalTimeText.setText(CTUtils.stringForTime(duration));
    }

    //endregion

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
