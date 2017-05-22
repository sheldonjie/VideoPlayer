package com.zt.player;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;

/**
 * Created by zhouteng on 2017/5/22.
 */

public class StandardIjkVideoView extends BaseIjkVideoView {


    public StandardIjkVideoView(@NonNull Context context) {
        super(context);
    }

    public StandardIjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StandardIjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StandardIjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.base_video_layout;
    }

    @Override
    protected int getSurfaceContainerId() {
        return R.id.surface_container;
    }
}
