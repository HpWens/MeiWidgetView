package com.demo.widget.meis;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.widget.R;
import com.demo.widget.utils.Eyes;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.meis.widget.photodrag.VideoDragRelativeLayout;
import com.meis.widget.radius.RadiusTextView;

/**
 * Created by wenshi on 2018/5/23.
 * Description
 */
public class MeiVideoDragActivity extends AppCompatActivity {

    TextView mTvComment;
    TextView mTvAttention;
    RadiusTextView mTvName;
    ImageView mIvClose;
    SimpleExoPlayerView mPlayerView;

    VideoDragRelativeLayout mDragLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mei_video_drag_activity);
        Eyes.translucentStatusBar(this, true);

        mTvComment = findViewById(R.id.tv_comment);
        mTvAttention = findViewById(R.id.tv_attention);
        mTvName = findViewById(R.id.tv_name);
        mIvClose = findViewById(R.id.iv_close);
        mDragLayout = findViewById(R.id.rl_drag);
        mPlayerView = findViewById(R.id.play_view);

        //视频源
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory trackFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(trackFactory);
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        mPlayerView.setPlayer(player);

        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this
                , Util.getUserAgent(this, "MyApplication")
                , defaultBandwidthMeter);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        Uri uri = Uri.parse("http://2449.vod.myqcloud.com/2449_22ca37a6ea9011e5acaaf51d105342e3.f20.mp4");
        MediaSource mediaSource = new ExtractorMediaSource(uri
                , dataSourceFactory, extractorsFactory, null, null);
        player.prepare(mediaSource);

        mDragLayout.setOnVideoDragListener(new VideoDragRelativeLayout.OnVideoDragListener() {
            @Override
            public void onStartDrag() {
                mTvComment.setVisibility(View.GONE);
                mTvAttention.setVisibility(View.GONE);
                mTvName.setVisibility(View.GONE);
                mIvClose.setVisibility(View.GONE);
            }

            @Override
            public void onRelease(boolean dismiss) {
                if (!dismiss) {
                    mTvComment.setVisibility(View.VISIBLE);
                    mTvAttention.setVisibility(View.VISIBLE);
                    mTvName.setVisibility(View.VISIBLE);
                    mIvClose.setVisibility(View.VISIBLE);
                } else {
                    //根据转场来执行相应的动画
                    finish();
                }
            }
        });

        mIvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
