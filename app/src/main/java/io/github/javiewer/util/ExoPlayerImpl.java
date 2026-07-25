package io.github.javiewer.util;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoSize;

import cn.jzvd.JZMediaInterface;
import cn.jzvd.JZMediaManager;
import cn.jzvd.JZVideoPlayerManager;
import io.github.javiewer.R;

public class ExoPlayerImpl extends JZMediaInterface implements Player.Listener {
    private SimpleExoPlayer simpleExoPlayer;
    private Handler mainHandler;
    private Runnable callback;
    private String TAG = "JZExoPlayer";
    private MediaSource videoSource;
    private long previousSeek = 0;

    @Override
    public void start() {
        simpleExoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void prepare() {
        Log.e(TAG, "prepare");
        mainHandler = new Handler();
        Context context = JZVideoPlayerManager.getCurrentJzvd().getContext();

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(context).build();
        TrackSelector trackSelector =
                new DefaultTrackSelector(context);

        LoadControl loadControl = new DefaultLoadControl.Builder()
                .setAllocator(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE))
                .setBufferDurationsMs(360000, 600000, 1000, 5000)
                .setTargetBufferBytes(C.LENGTH_UNSET)
                .setPrioritizeTimeOverSizeThresholds(false)
                .build();

        RenderersFactory renderersFactory = new DefaultRenderersFactory(context);
        simpleExoPlayer = new SimpleExoPlayer.Builder(context, renderersFactory)
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl)
                .build();

        String userAgent = Util.getUserAgent(context, context.getResources().getString(R.string.app_name));
        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context,
                new DefaultHttpDataSource.Factory()
                        .setUserAgent(userAgent)
                        .setConnectTimeoutMs(DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS)
                        .setReadTimeoutMs(DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS)
                        .setAllowCrossProtocolRedirects(true)
        );

        String currUrl = currentDataSource.toString();
        Log.i("CURR URL", currUrl);
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(currUrl));
        if (currUrl.contains(".m3u8") || currUrl.contains("api.rekonquer.com/psvs")) {
            videoSource = new HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem);
        } else {
            videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem);
        }
        simpleExoPlayer.addListener(this);

        Log.e(TAG, "URL Link = " + currUrl);

        simpleExoPlayer.setMediaSource(videoSource);
        simpleExoPlayer.prepare();
        simpleExoPlayer.setPlayWhenReady(true);
        callback = new onBufferingUpdate();
    }

    @Override
    public void onVideoSizeChanged(VideoSize videoSize) {
        JZMediaManager.instance().currentVideoWidth = videoSize.width;
        JZMediaManager.instance().currentVideoHeight = videoSize.height;
        JZMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (JZVideoPlayerManager.getCurrentJzvd() != null) {
                    JZVideoPlayerManager.getCurrentJzvd().onVideoSizeChanged();
                }
            }
        });
    }

    @Override
    public void onRenderedFirstFrame() {
        Log.e(TAG, "onRenderedFirstFrame");
    }

    @Override
    public void pause() {
        simpleExoPlayer.setPlayWhenReady(false);
    }

    @Override
    public boolean isPlaying() {
        return simpleExoPlayer.getPlayWhenReady();
    }

    @Override
    public void seekTo(long time) {
        if (time != previousSeek) {
            simpleExoPlayer.seekTo(time);
            previousSeek = time;
            JZVideoPlayerManager.getCurrentJzvd().seekToInAdvance = time;
        }
    }

    @Override
    public void release() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.release();
        }
        if (mainHandler != null)
            mainHandler.removeCallbacks(callback);
    }

    @Override
    public long getCurrentPosition() {
        if (simpleExoPlayer != null)
            return simpleExoPlayer.getCurrentPosition();
        else return 0;
    }

    @Override
    public long getDuration() {
        if (simpleExoPlayer != null)
            return simpleExoPlayer.getDuration();
        else return 0;
    }

    @Override
    public void setSurface(Surface surface) {
        simpleExoPlayer.setVideoSurface(surface);
        Log.e(TAG, "setSurface");
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        simpleExoPlayer.setVolume(leftVolume);
        simpleExoPlayer.setVolume(rightVolume);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, int reason) {
        Log.e(TAG, "onTimelineChanged");
    }

    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    }

    public void onLoadingChanged(boolean isLoading) {
        Log.e(TAG, "onLoadingChanged");
    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        Log.e(TAG, "onPlayerStateChanged" + playbackState);
        JZMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (JZVideoPlayerManager.getCurrentJzvd() != null) {
                    switch (playbackState) {
                        case Player.STATE_IDLE:
                            break;
                        case Player.STATE_BUFFERING:
                            mainHandler.post(callback);
                            break;
                        case Player.STATE_READY:
                            JZVideoPlayerManager.getCurrentJzvd().onPrepared();
                            break;
                        case Player.STATE_ENDED:
                            JZVideoPlayerManager.getCurrentJzvd().onAutoCompletion();
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
    }

    @Override
    public void onPlayerError(PlaybackException error) {
        Log.e(TAG, "onPlayerError" + error.toString());
        JZMediaManager.instance().mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (JZVideoPlayerManager.getCurrentJzvd() != null) {
                    JZVideoPlayerManager.getCurrentJzvd().onError(1000, 1000);
                }
            }
        });
    }

    @Override
    public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
    }

    private class onBufferingUpdate implements Runnable {
        @Override
        public void run() {
            final int percent = simpleExoPlayer.getBufferedPercentage();
            JZMediaManager.instance().mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (JZVideoPlayerManager.getCurrentJzvd() != null) {
                        JZVideoPlayerManager.getCurrentJzvd().setBufferProgress(percent);
                    }
                }
            });
            if (percent < 100) {
                mainHandler.postDelayed(callback, 300);
            } else {
                mainHandler.removeCallbacks(callback);
            }
        }
    }
}