package com.zt.videoplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.zt.player.StandardIjkVideoView;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MainActivity extends AppCompatActivity {

    private StandardIjkVideoView mVideoView;
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

        mVideoView = (StandardIjkVideoView)findViewById(R.id.video_view);

        mVideoPath = "http://video.tt.cmstop.cn/2017/0417/4c0d70cde07ff671d0d675aecf1709f6sd/index.mp4";

        if (mVideoPath != null) {
            mVideoView.setVideoPath(mVideoPath);
        }

        mVideoView.start();
    }
}
