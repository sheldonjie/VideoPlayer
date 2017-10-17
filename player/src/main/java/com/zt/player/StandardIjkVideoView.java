package com.zt.player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zt.player.ijk.widget.media.TextureRenderView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhouteng on 2017/5/22.
 */

public class StandardIjkVideoView extends BaseIjkVideoView implements SeekBar.OnSeekBarChangeListener {

    private ImageView startButton;
    private ViewGroup bottomContainer;
    private ViewGroup topContainer;
    private TextView totalTimeText;
    private TextView currentTimeText;
    private ProgressBar bottomProgressbar, loadingProgressBar;
    private SeekBar seekBar;
    private ImageView thumbView;

    private int allowPlayState = IjkVideoView.PLAY_UI_DISALLOW;

    private boolean isWifiTipDialogShowed;

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
        seekBar.setOnSeekBarChangeListener(this);
        thumbView = (ImageView) findViewById(R.id.thumb);
        loadingProgressBar = (ProgressBar) findViewById(R.id.loading);
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
    protected int getAllowPlayState() {
        if (CTUtils.isWifiConnected(getContext())) {
            allowPlayState = IjkVideoView.PLAY_WIFI_ALLOW;
        } else if (allowPlayState != IjkVideoView.PLAY_DATA_ALLOW) {
            allowPlayState = IjkVideoView.PLAY_UI_DISALLOW;
        }
        return allowPlayState;
    }

    @Override
    protected void showDisallowDialog() {

        if (isWifiTipDialogShowed) {
            return;
        }

        isWifiTipDialogShowed = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.tips_not_wifi));
        builder.setPositiveButton(getResources().getString(R.string.tips_not_wifi_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isWifiTipDialogShowed = false;
                allowPlayState = IjkVideoView.PLAY_DATA_ALLOW;
                start();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.tips_not_wifi_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isWifiTipDialogShowed = false;
                allowPlayState = IjkVideoView.PLAY_DISALLOW;
            }
        });
        builder.create().show();
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
    protected int getFullScreenBtnId() {
        return R.id.fullscreen;
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
    protected void cancelProgressTimer() {
        if (updateProgressTimer != null) {
            updateProgressTimer.cancel();
        }
        if (mProgressTimerTask != null) {
            mProgressTimerTask.cancel();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        cancelProgressTimer();
        ViewParent vpdown = getParent();
        while (vpdown != null) {
            vpdown.requestDisallowInterceptTouchEvent(true);
            vpdown = vpdown.getParent();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        startProgressTimer();
        ViewParent vpup = getParent();
        while (vpup != null) {
            vpup.requestDisallowInterceptTouchEvent(false);
            vpup = vpup.getParent();
        }
        if (mCurrentState != STATE_PLAYING && mCurrentState != STATE_PAUSED) {
            return;
        }
        int time = seekBar.getProgress() * getDuration() / 100;
        seekTo(time);
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
    protected void surfaceContainerClick() {
        toggleControlView();
        startControlViewTimer();
    }

    //region  dismiss view control timer

    private Timer mControlViewTimer;
    private ControlViewTimerTask mControlViewTimerTask;


    private void startControlViewTimer() {
        cancelControlViewTimer();
        mControlViewTimer = new Timer();
        mControlViewTimerTask = new ControlViewTimerTask();
        mControlViewTimer.schedule(mControlViewTimerTask, 2500);
    }

    private void cancelControlViewTimer() {
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
                            bottomContainer.setVisibility(View.GONE);
                            topContainer.setVisibility(View.GONE);
                            startButton.setVisibility(View.GONE);
                            bottomProgressbar.setVisibility(View.VISIBLE);
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


    @Override
    protected void changeUIWithState(int currentState) {
        super.changeUIWithState(currentState);
        switch (currentState) {
            case STATE_PREPARED:
            case STATE_IDLE:
                changeUIWithIdle();
                break;
            case STATE_PREPARING:
                changeUIWithPreparing();
                startControlViewTimer();
                break;
            case STATE_PLAYING:
                changeUIWithPlaying();
                startControlViewTimer();
                break;
            case STATE_PAUSED:
                changeUIWithPause();
                cancelControlViewTimer();
                break;
            case STATE_ERROR:
                changeUIWithError();
                break;
            case STATE_PLAYBACK_COMPLETED:
                changeUIWithComplete();
                cancelControlViewTimer();
                bottomProgressbar.setProgress(100);
                seekBar.setProgress(100);
                currentTimeText.setText(totalTimeText.getText());
                break;
        }

    }

    //region 各种状态下UI的显示风格

    private void changeUIWithIdle() {
        setViewsVisible(View.VISIBLE, View.GONE, View.VISIBLE, View.GONE, View.VISIBLE, View.GONE);
        updateStartImage();
    }

    private void changeUIWithPreparing() {
        setViewsVisible(View.VISIBLE, View.VISIBLE, View.GONE, View.VISIBLE, View.GONE, View.GONE);
    }

    private void changeUIWithPlaying() {
        setViewsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE);
        updateStartImage();
    }

    private void changeUIWithPause() {
        setViewsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE);
        updateStartImage();
    }

    private void changeUIWithError() {
        setViewsVisible(View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE);
    }

    private void changeUIWithComplete() {
        setViewsVisible(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.GONE, View.VISIBLE, View.GONE);
    }

    private void updateStartImage() {
        if (mCurrentState == STATE_PLAYING) {
            startButton.setImageResource(R.drawable.ct_click_pause_selector);
        } else if (mCurrentState == STATE_ERROR) {

        } else {
            startButton.setImageResource(R.drawable.ct_click_play_selector);
        }
    }


    private void setViewsVisible(int topConVisi, int bottomConVisi, int startBtnVisi, int loadingProVisi, int thumbVisi, int bottomProVisi) {
        topContainer.setVisibility(topConVisi);
        bottomContainer.setVisibility(bottomConVisi);
        startButton.setVisibility(startBtnVisi);
        loadingProgressBar.setVisibility(loadingProVisi);
        thumbView.setVisibility(thumbVisi);
        bottomProgressbar.setVisibility(bottomProVisi);
    }

    @Override
    protected void startWindowFullscreen(boolean mActionBar, boolean mStatusBar) {
        super.startWindowFullscreen(mActionBar, mStatusBar);
        if(mCurrentState == STATE_PAUSED && mFullPauseBitmap != null && !mFullPauseBitmap.isRecycled()) {
//            thumbView.setVisibility(View.VISIBLE);
//            thumbView.setImageBitmap(mFullPauseBitmap);
        }
    }

    @Override
    protected void exitWindowFullscreen() {
        super.exitWindowFullscreen();
        if(mCurrentState == STATE_PAUSED && mFullPauseBitmap != null && !mFullPauseBitmap.isRecycled()) {
//            thumbView.setVisibility(View.VISIBLE);
//            thumbView.setImageBitmap(mFullPauseBitmap);
        }
    }

    //endregion

}
