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
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.meis.widget.photodrag.VideoDragRelativeLayout;
import com.meis.widget.radius.RadiusTextView;

/**
 * Created by wenshi on 2018/5/23.
 * Description
 */
public class MeiVideoDragActivity extends AppCompatActivity implements Player.EventListener {

    TextView mTvComment;
    TextView mTvAttention;
    RadiusTextView mTvName;
    ImageView mIvClose;
    SimpleExoPlayerView mVideoView;
    SimpleExoPlayer mVideoPlayer;

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
        mVideoView = findViewById(R.id.video_view);

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
                mTvComment.setVisibility(View.VISIBLE);
                mTvAttention.setVisibility(View.VISIBLE);
                mTvName.setVisibility(View.VISIBLE);
                mIvClose.setVisibility(View.VISIBLE);
                if (dismiss) {
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

        initPlayer();
        initVideo();
    }

    @Override
    public void finish() {
        mVideoPlayer.setPlayWhenReady(false);
        mVideoPlayer.seekTo(0);
        super.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoPlayer.setPlayWhenReady(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoPlayer.setPlayWhenReady(false);
    }

    /**
     * 初始化player
     */
    private void initPlayer() {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTackSelectionFactory);
        DefaultLoadControl loadControl = new DefaultLoadControl(new DefaultAllocator(true, 65536), 3000, 5000, 2500L, 5000L);
        //2.创建ExoPlayer
        mVideoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        mVideoView.setPlayer(mVideoPlayer);
        mVideoPlayer.addListener(this);
    }

    private void initVideo() {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "useExoPlayer"), null);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        MediaSource videoSource = new ExtractorMediaSource(Uri.parse("http://sv.dingdangyixia.cn/sv/2fced8a5922843c19c038330cb66f505"),
                dataSourceFactory, extractorsFactory, null, null);
        mVideoPlayer.prepare(videoSource);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }
}
