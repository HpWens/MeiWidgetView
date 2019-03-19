package com.demo.widget.meis;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.widget.R;
import com.demo.widget.event.ScrollToPositionEvent;
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
import com.meis.widget.photodrag.DragRelativeLayout;
import com.meis.widget.photodrag.OnDragListener;
import com.meis.widget.radius.RadiusTextView;

import org.greenrobot.eventbus.EventBus;

import me.yokeyword.fragmentation.SupportFragment;


/**
 * Created by wenshi on 2018/5/24.
 * Description
 */
public class MeiVideoDragFragment extends SupportFragment implements Player.EventListener {

    TextView mTvComment;
    TextView mTvAttention;
    RadiusTextView mTvName;
    ImageView mIvClose;
    ImageView mIvBg;
    SimpleExoPlayerView mVideoView;
    SimpleExoPlayer mVideoPlayer;

    String mVideoUrl = "";
    DragRelativeLayout mDragLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mei_video_drag_fragment, container, false);

        mTvComment = view.findViewById(R.id.tv_comment);
        mTvAttention = view.findViewById(R.id.tv_attention);
        mTvName = view.findViewById(R.id.tv_name);
        mIvClose = view.findViewById(R.id.iv_close);
        mDragLayout = view.findViewById(R.id.rl_drag);
        mVideoView = view.findViewById(R.id.video_view);
        mIvBg = view.findViewById(R.id.iv_bg);

        mIvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            int[] loc = bundle.getIntArray("region");
            int pos = bundle.getInt("position");
            if (loc != null) {
                mDragLayout.setTransitionsRegion(loc[0], loc[1], loc[2], loc[3], loc[4], loc[5]);
            }
            int index = bundle.getInt("index", 0);
            if (index == 0) {
                mDragLayout.startTransitions();
                EventBus.getDefault().post(new ScrollToPositionEvent(pos, mDragLayout.getDuration(), index == 0, new ScrollToPositionEvent.OnRegionListener() {
                    @Override
                    public void onRegion(int l, int t, int r, int b, int w, int h) {
                        mDragLayout.setTransitionsRegion(l, t, r, b, w, h);
                    }
                }));
            }
            mIvBg.setBackgroundResource(R.mipmap.ic_video_drag_bg);
            mVideoUrl = bundle.getString("video_url");
        }

        mDragLayout.setOnoDragListener(new OnDragListener() {
            @Override
            public void onStartDrag() {
                super.onStartDrag();
                mTvComment.setVisibility(View.GONE);
                mTvAttention.setVisibility(View.GONE);
                mTvName.setVisibility(View.GONE);
                mIvClose.setVisibility(View.GONE);
            }

            @Override
            public void onStartEnter(boolean outOfBound) {
                super.onStartEnter(outOfBound);
            }

            @Override
            public void onRelease(boolean isResume) {
                super.onRelease(isResume);
                if (!isResume) {
                    mIvBg.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onEndExit() {
                super.onEndExit();
                finish();
            }

            @Override
            public void onEndEnter() {
                super.onEndEnter();
            }

            @Override
            public void onStartExit(boolean outOfBound) {
                super.onStartExit(outOfBound);
            }

            @Override
            public void onEndResume() {
                super.onEndResume();
                mTvComment.setVisibility(View.VISIBLE);
                mTvAttention.setVisibility(View.VISIBLE);
                mTvName.setVisibility(View.VISIBLE);
                mIvClose.setVisibility(View.VISIBLE);
            }
        });

        initPlayer();
        initVideo(mVideoUrl);

        return view;
    }

    public void finish() {
        mVideoPlayer.setPlayWhenReady(false);
        mVideoPlayer.seekTo(0);
        mVideoPlayer.removeListener(this);
        mVideoPlayer.release();
        getActivity().finish();
    }

    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        mVideoPlayer.setPlayWhenReady(true);
        mIvBg.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIvBg.setVisibility(View.GONE);
            }
        }, mDragLayout.getDuration());
    }

    @Override
    public void onSupportInvisible() {
        super.onSupportInvisible();
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
        mVideoPlayer = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector, loadControl);
        mVideoView.setPlayer(mVideoPlayer);
        mVideoPlayer.addListener(this);
    }

    private void initVideo(String videoUrl) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getActivity(),
                Util.getUserAgent(getActivity(), "useExoPlayer"), null);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        //http://sv.dingdangyixia.cn/sv/2fced8a5922843c19c038330cb66f505
        //由于头条的视频地址 在一定的时间会变化  过期的地址无法访问 后期可以通过 python 抓起实时地址
        MediaSource videoSource = new ExtractorMediaSource(Uri.parse("https://vd3.bdstatic.com/" +
                "mda-idip3ibsutbfkae1/sc/mda-idip3ibsutbfkae1.mp4?auth_key=1528440598-0-0-f8eb95c6ad0f5c6b066feef02630cde0&amp;bcevod_channel=searchbox_feed"),
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

    @Override
    public boolean onBackPressedSupport() {
        mDragLayout.endTransitions();
        return true;
    }

}
