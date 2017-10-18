package com.zt.player;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;

/**
 * Created by zhouteng on 2017/10/13.
 */

public class LiveVideoPlayerView extends BaseIjkVideoView {

    public LiveVideoPlayerView(@NonNull Context context) {
        super(context);
    }

    public LiveVideoPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LiveVideoPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LiveVideoPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void surfaceContainerClick() {

    }

    @Override
    protected int getPlayBtnId() {
        return 0;
    }

    @Override
    protected int getBackBtnId() {
        return 0;
    }

    @Override
    protected int getFullScreenBtnId() {
        return 0;
    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected int getSurfaceContainerId() {
        return 0;
    }

    @Override
    protected int getAllowPlayState() {
        return 0;
    }

    @Override
    protected void showDisallowDialog() {

    }

    @Override
    protected void setBufferProgress(int progress) {

    }
}
