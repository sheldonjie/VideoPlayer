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

//        mVideoPath = "http://video.tt.cmstop.cn/2017/0612/33a410b66b32d2d23f17a6ba8678425esd/index.mp4";

        mVideoPath = "http://hnxc.chinashadt.com:1936/live/stream:xcsh.stream/playlist.m3u8";

//        mVideoPath = "http://ivi.bupt.edu.cn/hls/chchd.m3u8";

//        mVideoPath = "http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8";

        if (mVideoPath != null) {
            mVideoView.setVideoPath(mVideoPath);
        }
    }

    @Override
    public void onBackPressed() {
        if(mVideoView != null) {
            mVideoView.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mVideoView != null) {
            mVideoView.onDestroy();
        }
    }
}
