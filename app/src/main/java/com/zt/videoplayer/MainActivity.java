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

        mVideoPath = "https://html5demos.com/assets/dizzy.mp4";

//        mVideoPath = "http://ivi.bupt.edu.cn/hls/chchd.m3u8";

//        mVideoPath = "http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8";

        if (mVideoPath != null) {
            mVideoView.setVideoPath(mVideoPath);
        }
    }
}
