package com.zt.videoplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TableLayout;

import com.zt.player.ijk.widget.media.AndroidMediaController;
import com.zt.player.ijk.widget.media.IjkVideoView;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MainActivity extends AppCompatActivity {

    private IjkVideoView mVideoView;
    private AndroidMediaController mMediaController;
    private TableLayout mHudView;
    private String mVideoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVideoView();
    }

    private void initVideoView() {
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        mMediaController = new AndroidMediaController(this, false);
        mHudView = (TableLayout) findViewById(R.id.hud_view);

        mVideoView = (IjkVideoView)findViewById(R.id.video_view);
        mVideoView.setMediaController(mMediaController);
//        mVideoView.setHudView(mHudView);

        mVideoPath = "http://video.tt.cmstop.cn/2017/0417/4c0d70cde07ff671d0d675aecf1709f6sd/index.mp4";

        if (mVideoPath != null) {
            mVideoView.setVideoPath(mVideoPath);
        }

        mVideoView.start();
    }
}
