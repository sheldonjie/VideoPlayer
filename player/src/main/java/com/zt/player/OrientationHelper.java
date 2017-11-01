package com.zt.player;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.provider.Settings;
import android.view.OrientationEventListener;

/**
 * Created by zhouteng on 2017/10/26.
 */

public class OrientationHelper {

    private Activity activity;
    private BaseIjkVideoView ijkVideoView;
    private OrientationEventListener orientationEventListener;

    private int screenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    private boolean mRotateWithSystem = true; //是否跟随系统

    private boolean mEnable = true;

    private int mIsLand;

    private boolean mClick, mClickLand, mClickPort;

    public OrientationHelper(Activity activity, BaseIjkVideoView ijkVideoView) {
        this.activity = activity;
        this.ijkVideoView = ijkVideoView;
        init();
    }

    private void init() {
        orientationEventListener = new OrientationEventListener(activity) {
            @Override
            public void onOrientationChanged(int rotation) {
                boolean autoRotateOn = (Settings.System.getInt(activity.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
                if (!autoRotateOn && mRotateWithSystem) {
                    return;
                }
                //设置竖屏
                if (((rotation >= 0) && (rotation <= 30)) || (rotation >= 330)) {
                    if (mClick) {
                        if (mIsLand > 0 && !mClickLand) {
                            return;
                        } else {
                            mClickPort = true;
                            mClick = false;
                            mIsLand = 0;
                        }
                    } else {
                        if (mIsLand > 0) {
                            screenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
//                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                            ijkVideoView.changeToNormalScreen();
                            ijkVideoView.exitWindowFullscreen();
                            mIsLand = 0;
                            mClick = false;
                        }
                    }
                }
                // 设置横屏
                else if (((rotation >= 230) && (rotation <= 310))) {
                    if (mClick) {
                        if (!(mIsLand == 1) && !mClickPort) {
                            return;
                        } else {
                            mClickLand = true;
                            mClick = false;
                            mIsLand = 1;
                        }
                    } else {
                        if (!(mIsLand == 1)) {
                            screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
//                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                            ijkVideoView.changeToFullScreen();
                            ijkVideoView.startWindowFullscreen(true, true);
                            mIsLand = 1;
                            mClick = false;
                        }
                    }
                }
                // 设置反向横屏
                else if (rotation > 30 && rotation < 95) {
                    if (mClick) {
                        if (!(mIsLand == 2) && !mClickPort) {
                            return;
                        } else {
                            mClickLand = true;
                            mClick = false;
                            mIsLand = 2;
                        }
                    } else if (!(mIsLand == 2)) {
                        screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
//                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
//                        ijkVideoView.changeToFullScreen();
                        ijkVideoView.startWindowFullscreen(true, true);
                        mIsLand = 2;
                        mClick = false;
                    }
                }

            }
        };
        orientationEventListener.enable();
    }

    /**
     * 点击切换的逻辑，比如竖屏的时候点击了就是切换到横屏不会受屏幕的影响
     */
    public void resolveByClick() {
        mClick = true;
        if (mIsLand == 0) {
            screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            CTUtils.toggledFullscreen(activity, true);
            mIsLand = 1;
            mClickLand = false;
        } else {
            screenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            CTUtils.toggledFullscreen(activity, false);
            mIsLand = 0;
            mClickPort = false;
        }
    }

    public void setEnable(boolean enable) {
        this.mEnable = enable;
        if (mEnable) {
            orientationEventListener.enable();
        } else {
            orientationEventListener.disable();
        }
    }

}
