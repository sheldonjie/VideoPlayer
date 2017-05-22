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

        mVideoPath = "http://video.jiecao.fm/8/16/%E4%BF%AF%E5%8D%A7%E6%92%91.mp4";

        if (mVideoPath != null) {
            mVideoView.setVideoPath(mVideoPath);
        }
    }
}
