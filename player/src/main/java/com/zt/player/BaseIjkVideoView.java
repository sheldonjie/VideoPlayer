package com.zt.player;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.zt.player.ijk.widget.media.TextureRenderView;

/**
 * Created by zhouteng on 2017/5/22.
 */

public abstract class BaseIjkVideoView extends IjkVideoView implements View.OnClickListener, View.OnTouchListener {

    private int mSystemUiVisibility;

    //是否需要在利用window实现全屏幕的时候隐藏actionbar
    protected boolean mActionBar = false;

    //是否需要在利用window实现全屏幕的时候隐藏statusbar
    protected boolean mStatusBar = false;

    //是否改变音量
    protected boolean mChangeVolume = false;

    private OrientationHelper orientationHelper;

    //触摸的是否进度条
    protected boolean mTouchingProgressBar = false;

    //触摸的X
    protected float mDownX;

    //触摸的Y
    protected float mDownY;

    //移动的Y
    protected float mMoveY;

    //是否改变播放进度
    protected boolean mChangePosition = false;

    //触摸显示虚拟按键
    protected boolean mShowVKey = false;

    //是否改变亮度
    protected boolean mBrightness = false;

    //是否首次触摸
    protected boolean mFirstTouch = false;

    //是否支持全屏滑动触摸有效
    protected boolean mIsTouchWigetFull = true;

    //是否支持非全屏滑动触摸有效
    protected boolean mIsTouchWiget = true;

    //手势偏差值
    protected int mThreshold = 80;

    //手动滑动的起始偏移位置
    protected int mSeekEndOffset;

    //手指放下的位置
    protected int mDownPosition;

    //手势调节音量的大小
    protected int mGestureDownVolume;

    //屏幕宽度
    protected int mScreenWidth;

    //屏幕高度
    protected int mScreenHeight;

    //手动改变滑动的位置
    protected int mSeekTimePosition;

    //触摸滑动进度的比例系数
    protected float mSeekRatio = 1;

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

    public abstract View getFullScreenBtn(); //获取全屏切换按钮控件

    public abstract View getTinyWindowSwitchBtn(); //获取小窗口切换按钮控件

    public abstract View getShareBtn();   //获取分享按钮控件

    public abstract View getTitleView(); //获取标题栏控件

    public abstract View getBackBtn(); //获取返回箭头控件

    protected void resetProgressAndTime() {
    } //重置进度条时间，当前时间，总时间

    protected void startProgressTimer() {
    } //开始进度条任务

    protected void cancelProgressTimer() {
    }  //取消进度条任务

    protected void setBottomProgress(int progress) {

    }  //设置底部进度条数值

    //endregion

    //region View Interaction

    private View playBtn;
    private View backBtn;
    private View fullScreenBtn;

    protected boolean isFullScreen;

    private ViewParent viewParent;

    protected int originViewWidth;
    protected int originViewHeight;

    private final void initView(Context context) {

        surfaceContainer.setOnClickListener(this);

        playBtn = findViewById(getPlayBtnId());
        playBtn.setOnClickListener(this);

        backBtn = findViewById(getBackBtnId());
        backBtn.setOnClickListener(this);

        fullScreenBtn = findViewById(R.id.fullscreen);
        fullScreenBtn.setOnClickListener(this);

        orientationHelper = new OrientationHelper(CTUtils.getActivity(context), this);

        mSeekEndOffset = CTUtils.dip2px(getContext(), 50);

        mAudioManager = (AudioManager) getContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        mScreenHeight = CTUtils.getScreenHeight(getContext());
        mScreenWidth = CTUtils.getScreenWidth(getContext());

        surfaceContainer.setOnTouchListener(this);

        initVolumeAndBrightnessViews();
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
            startWindowFullscreen(true, true);
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
     *
     * @return
     */
    private ViewGroup getRootViewGroup() {
        Activity activity = CTUtils.getActivity(getContext());
        if (activity != null) {
            return (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        }
        return null;
    }

    public void exitWindowFullscreen() {

        isFullScreen = false;

//        pauseFullCoverLogic();

//        CTUtils.toggledFullscreen(getContext(),false);

        orientationHelper.resolveByClick();

        CTUtils.showSupportActionBar(getContext(), mActionBar, mStatusBar);

        CTUtils.showNavKey(getContext(), mSystemUiVisibility);

        changeToNormalScreen();
    }

    protected void changeToNormalScreen() {
        ViewGroup vp = getRootViewGroup();
        vp.removeView((View) this.getParent());
        removePlayerFromParent();

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(originViewWidth, originViewHeight);
        setLayoutParams(layoutParams);

        if (viewParent != null) {
            ((ViewGroup) viewParent).addView(this);
        }
    }

    public void startWindowFullscreen(boolean mActionBar, boolean mStatusBar) {

        isFullScreen = true;

        mSystemUiVisibility = ((Activity) getContext()).getWindow().getDecorView().getSystemUiVisibility();

        this.mActionBar = mActionBar;
        this.mStatusBar = mStatusBar;

        originViewWidth = getWidth();
        originViewHeight = getHeight();

//        pauseFullCoverLogic();

        CTUtils.hideSupportActionBar(getContext(), mActionBar, mStatusBar);

        orientationHelper.resolveByClick();

        CTUtils.hideNavKey(getContext());

        changeToFullScreen();

    }

    protected void changeToFullScreen() {
        viewParent = getParent();

        ViewGroup vp = getRootViewGroup();

        removePlayerFromParent();

        final LayoutParams lpParent = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final FrameLayout frameLayout = new FrameLayout(getContext());
        frameLayout.setBackgroundColor(Color.BLACK);

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        frameLayout.addView(this, lp);
        vp.addView(frameLayout, lpParent);
    }

    /**
     * 全屏的暂停的时候返回页面不黑色
     */
    private void pauseFullCoverLogic() {
        if (mCurrentState == STATE_PAUSED) {
            try {
                mFullPauseBitmap = initCover();
            } catch (Exception e) {
                e.printStackTrace();
                mFullPauseBitmap = null;
            }
        }
    }

    protected Bitmap initCover() {
        if (mRenderView != null && mRenderView instanceof TextureRenderView) {
            TextureRenderView textureRenderView = (TextureRenderView) mRenderView;
            Bitmap bitmap = Bitmap.createBitmap(
                    textureRenderView.getSizeW(), textureRenderView.getSizeH(), Bitmap.Config.RGB_565);
            return textureRenderView.getBitmap(bitmap);
        }
        return null;
    }

    //endregion

    /**
     * 返回键调用
     */
    public void onBackPressed() {
        backBtn.performClick();
    }

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
                pauseFullCoverLogic();
                break;
            case STATE_ERROR:
                cancelProgressTimer();
                break;
            case STATE_PLAYBACK_COMPLETED:
                cancelProgressTimer();
                break;
        }
    }

    //region 音量和亮度调节界面

    private float mBrightnessData = -1;

    private View volumeLayout;
    private View brightnessLayout;

    protected ProgressBar volumeProgressBar;
    protected ProgressBar brightnessProgressBar;

    private void initVolumeAndBrightnessViews() {
        volumeLayout = findViewById(R.id.volume_layout);
        volumeProgressBar = (ProgressBar) findViewById(R.id.volume_progressbar);
        brightnessLayout = findViewById(R.id.brightness_layout);
        brightnessProgressBar = (ProgressBar) findViewById(R.id.brightness_progressbar);
    }

    public View getVolumeIncreaseIcon() {
        return volumeLayout.findViewById(R.id.volume_increase_icon);
    }

    public View getVolumeDecreaseIcon() {
        return volumeLayout.findViewById(R.id.volume_decrease_icon);
    }

    public View getBrightnessIncreaseIcon() {
        return brightnessLayout.findViewById(R.id.brightness_increase_icon);
    }

    public View getBrightnessDecreaseIcon() {
        return brightnessLayout.findViewById(R.id.brightness_decrease_icon);
    }

    protected void showVolume(int volumePercent) {
        volumeLayout.setVisibility(View.VISIBLE);
        volumeProgressBar.setProgress(volumePercent);
    }

    protected void hideVolume() {
        volumeLayout.setVisibility(View.GONE);
    }

    protected void showBrightness(float brightnessPercent) {
        brightnessLayout.setVisibility(View.VISIBLE);
        mBrightnessData = ((Activity) (getContext())).getWindow().getAttributes().screenBrightness;
        if (mBrightnessData <= 0.00f) {
            mBrightnessData = 0.50f;
        } else if (mBrightnessData < 0.01f) {
            mBrightnessData = 0.01f;
        }
        WindowManager.LayoutParams lpa = ((Activity) (getContext())).getWindow().getAttributes();
        lpa.screenBrightness = mBrightnessData + brightnessPercent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        ((Activity) (getContext())).getWindow().setAttributes(lpa);
        brightnessProgressBar.setProgress((int) (lpa.screenBrightness * 100));
    }

    protected void hideBrightness() {
        brightnessLayout.setVisibility(View.GONE);
    }

    //endregion

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        float x = event.getX();
        float y = event.getY();

        if (id == getSurfaceContainerId()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchSurfaceDown(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float deltaX = x - mDownX;
                    float deltaY = y - mDownY;
                    float absDeltaX = Math.abs(deltaX);
                    float absDeltaY = Math.abs(deltaY);

                    if ((isFullScreen && mIsTouchWigetFull)
                            || (mIsTouchWiget && !isFullScreen)) {
                        if (!mChangePosition && !mChangeVolume && !mBrightness) {
                            touchSurfaceMoveFullLogic(absDeltaX, absDeltaY);
                        }
                    }
                    touchSurfaceMove(deltaX, deltaY, y);
                    break;
                case MotionEvent.ACTION_UP:
                    touchSurfaceUp();
                    break;
            }
        }
        return false;
    }

    protected void touchSurfaceUp() {
        if (mChangePosition) {
            int duration = getDuration();
            int progress = mSeekTimePosition * 100 / (duration == 0 ? 1 : duration);
            setBottomProgress(progress);
        }
        if (!mChangePosition && !mChangeVolume && !mBrightness) {
//            onClickUiToggle();
        }
        mTouchingProgressBar = false;
//        dismissProgressDialog();
        hideVolume();
        hideBrightness();
    }

    protected void touchSurfaceMove(float deltaX, float deltaY, float y) {

        int curWidth = CTUtils.getCurrentScreenLand(getContext()) ? mScreenHeight : mScreenWidth;
        int curHeight = CTUtils.getCurrentScreenLand(getContext()) ? mScreenWidth : mScreenHeight;

        if (mChangePosition) {
            int totalTimeDuration = getDuration();
            mSeekTimePosition = (int) (mDownPosition + (deltaX * totalTimeDuration / curWidth) / mSeekRatio);
            if (mSeekTimePosition > totalTimeDuration)
                mSeekTimePosition = totalTimeDuration;
            String seekTime = CTUtils.stringForTime(mSeekTimePosition);
            String totalTime = CTUtils.stringForTime(totalTimeDuration);
//            showProgressDialog(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration);
        } else if (mChangeVolume) {
            deltaY = -deltaY;
            int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int deltaV = (int) (max * deltaY * 3 / curHeight);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mGestureDownVolume + deltaV, 0);
            int volumePercent = (int) (mGestureDownVolume * 100 / max + deltaY * 2 * 100 / curHeight);
            showVolume(volumePercent);
        } else if (!mChangePosition && mBrightness) {
            if (Math.abs(deltaY) > mThreshold) {
                float percent = (-deltaY * 2 / curHeight);
                showBrightness(percent);
                mDownY = y;
            }
        }
    }

    protected void touchSurfaceMoveFullLogic(float absDeltaX, float absDeltaY) {


        int curWidth = CTUtils.getCurrentScreenLand(getContext()) ? mScreenHeight : mScreenWidth;

        if (absDeltaX > mThreshold || absDeltaY > mThreshold) {
            cancelProgressTimer();
            if (absDeltaX >= mThreshold) {
                //防止全屏虚拟按键
                int screenWidth = CTUtils.getScreenWidth(getContext());
                if (Math.abs(screenWidth - mDownX) > mSeekEndOffset) {
                    mChangePosition = true;
                    mDownPosition = getCurrentPosition();
                } else {
                    mShowVKey = true;
                }
            } else {
                int screenHeight = CTUtils.getScreenHeight(getContext());
                boolean noEnd = Math.abs(screenHeight - mDownY) > mSeekEndOffset;
                if (mFirstTouch) {
                    mBrightness = (mDownX < curWidth * 0.5f) && noEnd;
                    mFirstTouch = false;
                }
                if (!mBrightness) {
                    mChangeVolume = noEnd;
                    mGestureDownVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                }
                mShowVKey = !noEnd;
            }
        }
    }

    protected void touchSurfaceDown(float x, float y) {
        mTouchingProgressBar = true;
        mDownX = x;
        mDownY = y;
        mMoveY = 0;
        mChangeVolume = false;
        mChangePosition = false;
        mShowVKey = false;
        mBrightness = false;
        mFirstTouch = true;
    }

    /**
     * 调整触摸滑动快进的比例
     *
     * @param seekRatio 滑动快进的比例，默认1。数值越大，滑动的产生的seek越小
     */
    public void setSeekRatio(float seekRatio) {
        if (seekRatio < 0) {
            return;
        }
        this.mSeekRatio = seekRatio;
    }

    /**
     * 是否可以全屏滑动界面改变进度，声音等
     * 默认 true
     */
    public void setIsTouchWigetFull(boolean isTouchWigetFull) {
        this.mIsTouchWigetFull = isTouchWigetFull;
    }

    /**
     * 是否可以滑动界面改变进度，声音等
     * 默认true
     */
    public void setIsTouchWiget(boolean isTouchWiget) {
        this.mIsTouchWiget = isTouchWiget;
    }

    public void onDestroy() {
        release(true);
    }

}
