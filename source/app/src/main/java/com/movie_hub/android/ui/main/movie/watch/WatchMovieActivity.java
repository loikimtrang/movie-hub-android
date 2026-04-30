package com.movie_hub.android.ui.main.movie.watch;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.mediacodec.MediaCodecInfo;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.common.collect.ImmutableList;
import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.request.history.TrackingWatchHistoryRequest;
import com.movie_hub.android.data.model.api.request.setting.UserSettingsRequest;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.history.WatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.room.RoomResponse;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.api.response.video.VideoResponse;
import com.movie_hub.android.data.model.mqtt.ParticipantJoinModel;
import com.movie_hub.android.data.model.mqtt.RoomOptionModel;
import com.movie_hub.android.data.mqtt.Command;
import com.movie_hub.android.data.mqtt.Message;
import com.movie_hub.android.databinding.ActivityWatchMovieBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.movie.watch.Provider.SpriteThumbnailManager;
import com.movie_hub.android.ui.main.movie.watch.adapter.EpisodeItemListHoriAdapter;
import com.movie_hub.android.ui.main.movie.watch.adapter.SeasonItemAdapter;
import com.movie_hub.android.ui.main.movie.watch.dialog.MoreOptionBottomSheetDialog;
import com.movie_hub.android.ui.main.movie.watch.dialog.PlaySpeedBottomSheetDialog;
import com.movie_hub.android.ui.main.movie.watch.dialog.QualityBottomSheetDialog;
import com.movie_hub.android.ui.main.movie.watch.dialog.SettingBottomSheetDialog;
import com.movie_hub.android.ui.main.movie.watch.setting.SettingVideoModel;
import com.movie_hub.android.ui.main.movie.watch.setting.VideoQuality;
import com.movie_hub.android.utils.DeviceUtils;
import com.movie_hub.android.utils.GsonUtils;
import com.movie_hub.android.utils.LiveDataUtils;
import com.movie_hub.android.utils.NetworkUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import eu.davidea.flexibleadapter.databinding.BR;
import timber.log.Timber;

public class WatchMovieActivity extends BaseActivity<ActivityWatchMovieBinding, WatchMovieViewModel> implements View.OnClickListener,
        SettingBottomSheetDialog.SettingBottomSheetCallback,
        PlaySpeedBottomSheetDialog.PlaySpeedBottomSheetCallback,
        QualityBottomSheetDialog .QualityBottomSheetCallback,
        MoreOptionBottomSheetDialog.MoreOptionBottomSheetCallback,
        SeasonItemAdapter.OnSeasonClickListener,
        EpisodeItemListHoriAdapter.OnEpisodeClickListener {
    private ExoPlayer player;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int SEEK_DOUBLE_TAP_MILLIS = 10000;
    private Handler seekHandler = new Handler(Looper.getMainLooper());
    private Handler autoHideHandler = new Handler(Looper.getMainLooper());
    private Handler countResetHandler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarRunnable;
    private Runnable resetCountRunnable;
    private boolean isUserSeeking = false;
    private boolean isBuffering = false;
    private boolean isLockScreen = false;
    private boolean isVideoReadyWhenStartActivity = false;
    boolean isZoomed  = false;
    private ScaleGestureDetector scaleGestureDetector;
    private static final int DOUBLE_TAP_TIMEOUT = 800;
    private int forwardCount = 0;
    private int previousCount = 0;
    private AudioManager audioManager;
    private int maxVolume;
    private int currentVolume;
    private SeasonItemAdapter seasonItemAdapter;
    private EpisodeItemListHoriAdapter episodeItemListHoriAdapter;
    private SpriteThumbnailManager thumbnailManager;
    public boolean isStartContinueWatch = false;
    boolean isSeries = false;
    private Handler trackingHandler = new Handler(Looper.getMainLooper());
    private Runnable trackingRunnable;
    private static final long TRACKING_INTERVAL_MS = 5 * 60 * 1000L;
    private int currentSeasonIndex = 0;
    public static String LiveRoom = "isLiveRoom";
    public static String Host = "isHost";
    public static String ROOM = "room_details";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        hideSystemUI();
        viewModel.startTokenAutoRefresh();

        String json = getIntent().getStringExtra("movie_details");
        MovieResponse movie = GsonUtils.fromJson(json, MovieResponse.class);

        if (movie != null) {
            viewModel.movieDetails = movie;
            viewModel.setting = createDefaultSettings();
            thumbnailManager = new SpriteThumbnailManager(this, viewBinding.ivThumbnailPreview, viewBinding.thumbnailPreviewContainer);

            isSeries = viewModel.movieDetails != null &&
                    viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES;

            if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SINGLE) {
                viewModel.nowVideoPlay = viewModel.movieDetails.getSeasons().get(viewModel.movieDetails.getSeasons().size() - 1).getVideo();
                setUpViewForSingleMovie();
            } else if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES) {
                String jsonEpisode = getIntent().getStringExtra("episode");
                viewModel.nowEpisodePlay = GsonUtils.fromJson(jsonEpisode, MovieItemResponse.class);
                assert viewModel.nowEpisodePlay != null;
                viewModel.nowVideoPlay = viewModel.nowEpisodePlay.getVideo();
                setUpAdapterEpisode();
                setUpViewForSeriesMovie();
            }

            viewModel.isLiveRoom = getIntent().getBooleanExtra(LiveRoom, false);

            if (!viewModel.isLiveRoom) {
                if (viewModel.isLogin()) {
                    String jsonTracking = getIntent().getStringExtra("movie_details_tracking");
                    ListWatchHistoryResponse listTracking = GsonUtils.fromJson(jsonTracking, ListWatchHistoryResponse.class);
                    if (listTracking != null) {
                        viewModel.movieDetailsTracking.postValue(listTracking);
                    }

                    getUserProfile();
                } else {
                    initMovie();
                }
            } else {
                viewModel.isHost = getIntent().getBooleanExtra(Host, false);
                String roomJson = getIntent().getStringExtra(ROOM);
                viewModel.roomDetail = GsonUtils.fromJson(roomJson, RoomResponse.class);

                initMovie();
            }
        }
    }

    public void getUserProfile() {
        viewModel.getUserProfile(new MainCallback<UserResponse>() {
            @Override
            public void doError(Throwable error) {

            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doSuccess(UserResponse response) {
                String settingJson = response.getSettings();

                if (settingJson != null && !settingJson.isEmpty()) {
                    try {
                        viewModel.setting = GsonUtils.fromJson(settingJson, UserSettingsRequest.class);
                    } catch (Exception e) {
                        viewModel.setting = createDefaultSettings();
                    }
                } else {
                    viewModel.setting = createDefaultSettings();
                }

                initMovie();
            }

            @Override
            public void doFail() {

            }
        });
    }

    private UserSettingsRequest createDefaultSettings() {
        UserSettingsRequest defaultSetting = new UserSettingsRequest();
        defaultSetting.setPlaybackSpeed(1.0);
        defaultSetting.setAutoSkipIntro(false);
        defaultSetting.setAutoNextEpisode(false);
        defaultSetting.setResolution(0);

        if (audioManager == null) {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        int currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        defaultSetting.setAudio(currentVol);

        try {
            int systemBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            int brightnessPercent = Math.round((systemBrightness / 255f) * 100);
            defaultSetting.setBrightness(brightnessPercent);
        } catch (Settings.SettingNotFoundException e) {
            defaultSetting.setBrightness(50);
        }

        return defaultSetting;
    }

    //region === Init Movie ===

    public void loadVtt() {
        if (viewModel.nowVideoPlay != null) {
            String sprite = viewModel.nowVideoPlay.getSpriteUrl();
            String vtt = viewModel.nowVideoPlay.getVttUrl();
            if (sprite != null && vtt != null) {
                thumbnailManager.load(vtt, sprite);
            }
        }
    }
    public void initMovie() {
        reset();
        viewModel.nowUriPlay = getVideoUri(viewModel.nowVideoPlay);

        if (Boolean.TRUE.equals(viewModel.getTokenReady().getValue())) {
            setUpMovie(viewModel.nowUriPlay);

        } else {
            LiveDataUtils.observeOnce(viewModel.getTokenReady(), this, isReady -> {
                if (Boolean.TRUE.equals(isReady)) {
                    setUpMovie(viewModel.nowUriPlay);

                }
            });
        }
        viewModel.getIsPlaying().observe(this, this::updatePlayPauseIcons);
    }

    public void handleObserveTracking() {
        viewModel.movieDetailsTracking.observe(this, response -> {
            if (player == null || response == null || response.getWatchHistories() == null) return;

            viewModel.movieDetails.applyWatchHistory(response);
            if (episodeItemListHoriAdapter != null) {
                if (currentSeasonIndex >= 0 && currentSeasonIndex < viewModel.movieDetails.getSeasons().size()) {
                    List<MovieItemResponse> episodes = viewModel.movieDetails.getSeasons().get(currentSeasonIndex).getEpisodes();
                    episodeItemListHoriAdapter.setData(episodes, viewBinding.layoutListEpisodes.rvEpisode);
                } else {
                    Log.e("WatchMovie", "Invalid currentSeasonIndex = " + currentSeasonIndex);
                }

            }

            if (isStartContinueWatch) return;

            Long movieItemId = null;
            if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SINGLE) {
                movieItemId = viewModel.movieDetails.getSeasons().get(0).getId();
            } else if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES && viewModel.nowEpisodePlay != null) {
                movieItemId = viewModel.nowEpisodePlay.getId();
            }

            if (movieItemId == null) return;

            WatchHistoryResponse watchHistory = response.getWatchHistoryByMovieId(movieItemId);
            if (watchHistory == null) return;

            long seekTime = 0;

            if (!Boolean.TRUE.equals(watchHistory.isCompleted()) && watchHistory.getLastWatchSeconds() != null) {
                seekTime = watchHistory.getLastWatchSeconds() * 1000L;
            }

            // Nếu player đã sẵn sàng → seek liền, còn chưa thì chờ tới STATE_READY rồi mới seek
            if (player.getPlaybackState() == Player.STATE_READY) {
                player.seekTo(seekTime);
                isStartContinueWatch = true;
            } else {
                long finalSeekTime = seekTime;
                player.addListener(new Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int state) {
                        if (state == Player.STATE_READY) {
                            player.seekTo(finalSeekTime);
                            isStartContinueWatch = true;
                            player.removeListener(this);
                        }
                    }
                });
            }
        });
    }


    public void setUpViewForSingleMovie() {
        viewBinding.nameMovie.setText(Objects.requireNonNull(viewModel.movieDetails.getTitle()));
        viewBinding.nameMovieOriginal.setText(Objects.requireNonNull(viewModel.movieDetails.getOriginalTitle()));
        viewBinding.btnNextEpisode.setVisibility(View.GONE);
        viewBinding.btnEpisodes.setVisibility(View.GONE);
        loadVtt();
    }

    @SuppressLint("SetTextI18n")
    public void setUpViewForSeriesMovie() {
        currentSeasonIndex = viewModel.movieDetails.getIndexSeasonSelect();

        viewBinding.nameMovie.setText(getString(R.string.episode_index) + " " + viewModel.nowEpisodePlay.getLabel()
                + ". " + viewModel.nowEpisodePlay.getTitle());
        viewBinding.nameMovieOriginal.setText(viewModel.movieDetails.getTitle() + findLabelSeason());
        loadVtt();
        if (isLastEpisode()) {
            viewBinding.btnNextEpisode.setVisibility(View.GONE);
        } else {
            viewBinding.btnNextEpisode.setVisibility(View.VISIBLE);
            setUpNextEpisodeLayout();
        }
    }

    @SuppressLint("SetTextI18n")
    public void reset() {
        forwardCount = 0;
        previousCount = 0;
        isVideoReadyWhenStartActivity = false;
        viewBinding.seekBar.setProgress(0);
        viewBinding.tvCurrentTime.setText("00:00");
        viewBinding.tvTotalTime.setText("00:00");

        viewModel.lastPlaybackPosition = 0L;
    }
    public void setUpMovie(String uri) {
        if (!viewModel.setting.isSetupPlaybackSpeed()) {
            if (viewModel.setting.getPlaybackSpeed() != null) {
                float speed = viewModel.setting.getPlaybackSpeed().floatValue();
                viewModel.settingVideoModel.getPlaySpeed().setSpeed(speed);
            }

            viewModel.setting.setSetupPlaybackSpeed(true);
        }
        DefaultMediaSourceFactory mediaSourceFactory =
                new DefaultMediaSourceFactory(
                        new TokenRefreshingDataSourceFactoryWatchMovie(viewModel)
                );

        ExoPlayer.Builder playerBuilder = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory);

        // === RENDERERS FACTORY ===
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this)
                .setEnableDecoderFallback(true)
                .setMediaCodecSelector(getCustomCodecSelector());

        if (DeviceUtils.isLowEndDevice(this)) {
            renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
        }

        playerBuilder.setRenderersFactory(renderersFactory);

        // === TRACK SELECTOR (thay thế setForceLowestBitrate) ===
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
        DefaultTrackSelector.Parameters.Builder paramsBuilder = trackSelector.buildUponParameters();

        if (DeviceUtils.isLowEndDevice(this)) {
            paramsBuilder
                    .setMaxVideoBitrate(600_000)     // 600kbps
                    .setMaxVideoSize(854, 480)       // 480p
                    .setForceLowestBitrate(true);    // Ép chọn bitrate thấp nhất
        }

        trackSelector.setParameters(paramsBuilder.build());
        playerBuilder.setTrackSelector(trackSelector);

        // TẠO PLAYER
        player = playerBuilder.build();
        viewBinding.playerView.setPlayer(player);
        showLoadingVideo();

        MediaItem mediaItem = MediaItem.fromUri(uri);
        player.setMediaItem(mediaItem);
        player.prepare();
        long lastPosition = viewModel.lastPlaybackPosition;
        if (lastPosition > 0) {
            player.seekTo(lastPosition);
        }
        player.setPlayWhenReady(true);

        setupPlayerListener();
        setupSeekBar();
        setupGestureDetector();
        setupSeekBarBrightNess();
        setupSeekBarVolume();

        handleObserveTracking();
    }

    private String getVideoUri(VideoResponse videoResponse) {
        if (videoResponse != null) {
            if (!videoResponse.getContent().contains("http")) videoResponse.setContent(videoResponse.getHostname() + Constants.MEDIA_URL_VIDEO + videoResponse.getContent());
            return videoResponse.getContent();
        }
        return "";
    }

    //region === SetUp for Device ===

    @SuppressLint("VisibleForTests")
    private MediaCodecSelector getCustomCodecSelector() {
        return (mimeType, requiresSecureDecoder, requiresTunnelingDecoder) -> {
            if (MimeTypes.VIDEO_H264.equals(mimeType) && DeviceUtils.isQualcommOldChipset()) {
                try {
                    // Tìm decoder software-only cho H.264
                    List<MediaCodecInfo> decoders = MediaCodecUtil.getDecoderInfos(
                            mimeType, requiresSecureDecoder, requiresTunnelingDecoder
                    );

                    for (MediaCodecInfo decoder : decoders) {
                        if (decoder.softwareOnly && decoder.name.contains("google")) {
                            return ImmutableList.of(decoder);
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
            }

            // Mặc định: dùng hệ thống
            return MediaCodecSelector.DEFAULT.getDecoderInfos(mimeType, requiresSecureDecoder, requiresTunnelingDecoder);
        };
    }

    private void autoSelectQualityOnStart() {
        if (!viewModel.settingVideoModel.getQuality().isAuto()) return;

        DeviceUtils.DeviceTier tier = DeviceUtils.getDeviceTier(this);
        int maxHeight;

        if (tier == DeviceUtils.DeviceTier.LOW_END) {
            maxHeight = 480;
        } else if (tier == DeviceUtils.DeviceTier.MID_RANGE) {
            maxHeight = 720;
        } else {
            maxHeight = 1080;
        }

        VideoQuality best = null;
        for (VideoQuality q : viewModel.settingVideoModel.getAvailableQualities()) {
            if (q.height <= maxHeight && (best == null || q.height > best.height)) {
                best = q;
            }
        }

        if (best != null) {
            viewModel.settingVideoModel.getQuality().setResolution(best);
            applyQualityFromSetting();
        }
    }

    //region === Get Quality Video ===
    private void extractAvailableQualities(Tracks tracks) {
        viewModel.settingVideoModel.getAvailableQualities().clear();

        for (Tracks.Group group : tracks.getGroups()) {
            if (group.getType() != C.TRACK_TYPE_VIDEO) continue;

            TrackGroup mediaTrackGroup = group.getMediaTrackGroup();

            for (int i = 0; i < group.length; i++) {
                Format format = group.getTrackFormat(i);
                int height = format.height;
                int bitrate = format.bitrate;

                if (height > 0) {
                    VideoQuality quality = new VideoQuality(
                            height,
                            bitrate,
                            mediaTrackGroup.id,
                            i
                    );
                    viewModel.settingVideoModel.getAvailableQualities().add(quality);
                }
            }
        }

        Collections.sort(viewModel.settingVideoModel.getAvailableQualities(),
                (q1, q2) -> Integer.compare(q2.height, q1.height));

        boolean isCurrentAuto = viewModel.settingVideoModel.getQuality().isAuto();
        VideoQuality currentSelected = viewModel.settingVideoModel.getQuality().getResolution();

        VideoQuality auto = new VideoQuality();
        auto.label = getString(R.string.auto);
        auto.isCheck = isCurrentAuto; // Check nếu đang là auto

        for (VideoQuality quality : viewModel.settingVideoModel.getAvailableQualities()) {
            if (!isCurrentAuto && currentSelected != null) {
                if (quality.height == currentSelected.height) {
                    quality.isCheck = true;
                } else {
                    quality.isCheck = false;
                }
            } else {
                quality.isCheck = false;
            }
        }

        viewModel.settingVideoModel.getAvailableQualities().add(0, auto);
    }

    private VideoQuality findBestMatchingQuality(int settingResolutionId) {
        List<VideoQuality> available = viewModel.settingVideoModel.getAvailableQualities();
        if (available == null || available.isEmpty()) return null;

        int targetHeight;
        switch (settingResolutionId) {
            case 1: targetHeight = 720; break;
            case 2: targetHeight = 1080; break;
            case 3: targetHeight = 1440; break;
            case 4: targetHeight = 4320; break;
            default: return null;
        }

        VideoQuality bestMatch = null;
        for (VideoQuality q : available) {
            if (q.height == targetHeight) return q;

            if (bestMatch == null || q.height > bestMatch.height) {
                bestMatch = q;
            }
        }

        return bestMatch;
    }

    // region === Set Up Listener Video ===
    private boolean isPlayLiveRoom = false;
    private void setupPlayerListener() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_BUFFERING) {
                    viewBinding.btnReplay.setVisibility(View.GONE);
                    showLoadingVideo();
                } else if (state == Player.STATE_READY) {

                    if (!isPlayLiveRoom && viewModel.isLiveRoom && !viewModel.isHost) {
                        isPlayLiveRoom = true;

                        Message message = new Message();
                        message.setCmd(Command.CMD_PARTICIPANT_JOIN);
                        ParticipantJoinModel participantJoinModel = new ParticipantJoinModel();
                        participantJoinModel.setId(viewModel.getUserId().toString());

                        message.setData(participantJoinModel);
                        sendMqttMessage(message, Constants.TOPIC + viewModel.roomDetail.getId().toString());
                    }

                    if (isStartContinueWatch) {
                        updateVideoTracking();
                    }

                    isVideoReadyWhenStartActivity = true;
                    viewBinding.btnReplay.setVisibility(View.GONE);
                    hideLoadingVideo();
                    if (player.getDuration() > 0) {
                        viewBinding.tvTotalTime.setText(formatTime(player.getDuration()));
                    }
                    viewBinding.loadingProgress.setVisibility(View.GONE);

                } else if (state == Player.STATE_ENDED) {
                    handleEndVideo();
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                viewModel.setPlaying(isPlaying);
                updatePlayPauseIcons(isPlaying);

                if (isPlaying) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }

            @Override
            public void onTracksChanged(Tracks tracks) {
                extractAvailableQualities(tracks);

                if (!viewModel.setting.isSetupResolution()) {
                    applyInitialSettingResolution();
                    refreshCheckStatus();
                } else {
                    autoSelectQualityOnStart();
                }
            }

            @Override
            public void onVideoSizeChanged(VideoSize videoSize) {
                runOnUiThread(() -> {
                    VideoQuality current = new VideoQuality();
                    current.height = videoSize.height;
                    current.label = current.getQualityLabel(videoSize.height);
                    SettingBottomSheetDialog.videoQuality.postValue(current);
                    QualityBottomSheetDialog.videoQuality.postValue(current);
                });
            }
            @Override
            public void onPlayerError(PlaybackException error) {
                handlePlaybackError(error);
            }
        });
    }

    private void refreshCheckStatus() {
        List<VideoQuality> list = viewModel.settingVideoModel.getAvailableQualities();
        if (list == null || list.isEmpty()) return;

        boolean isAuto = viewModel.settingVideoModel.getQuality().isAuto();
        VideoQuality selected = viewModel.settingVideoModel.getQuality().getResolution();

        for (VideoQuality q : list) {
            if (q.label != null && q.label.equals(getString(R.string.auto))) {
                q.isCheck = isAuto;
            } else {
                q.isCheck = (!isAuto && selected != null && q.height == selected.height);
            }
        }
    }
    private void applyInitialSettingResolution() {
        Integer savedResId = viewModel.setting.getResolution();

        if (savedResId == null || savedResId == 0) {
            viewModel.setting.setSetupResolution(true);
            autoSelectQualityOnStart();
            return;
        }

        VideoQuality target = findBestMatchingQuality(savedResId);

        if (target != null) {
            viewModel.settingVideoModel.getQuality().setResolution(target);
            viewModel.settingVideoModel.getQuality().setAuto(false);

            applyQualityFromSetting();

            viewModel.setting.setSetupResolution(true);
        }
    }
    private void handlePlaybackError(PlaybackException error) {
        hideLoadingVideo();
        boolean isNetworkAvailable = NetworkUtils.isNetworkAvailable(getApplication());
        String mgs = getString(R.string.an_error_occurred);
        if (!isNetworkAvailable) {
            mgs = getString(R.string.network_error_please_check_your_internet_connection);
        }
        String finalMgs = mgs;

        viewModel.lastPlaybackPosition = player != null ? player.getCurrentPosition() : 0L;

        runOnUiThread(() -> {
            viewBinding.lDialogErrorVideo.setVisibility(View.VISIBLE);
            viewBinding.dialogErrorVideoMessage.setText(finalMgs);

            viewBinding.btnRetryErrorVideo.setOnClickListener(v -> {
                viewBinding.lDialogErrorVideo.setVisibility(View.GONE);
                if (viewModel.lastPlaybackPosition == 0L) {
                    reset();
                }
                if (Boolean.TRUE.equals(viewModel.getTokenReady().getValue())) {
                    setUpMovie(viewModel.nowUriPlay);

                } else {
                    LiveDataUtils.observeOnce(viewModel.getTokenReady(), this, isReady -> {
                        if (Boolean.TRUE.equals(isReady)) {
                            setUpMovie(viewModel.nowUriPlay);

                        }
                    });
                }
            });

            viewBinding.btnExitErrorVideo.setOnClickListener(v -> {
                viewBinding.lDialogErrorVideo.setVisibility(View.GONE);
                finish();
            });

        });
    }
    private void updatePlayPauseState(boolean isPlaying) {
        if (player == null) return;
        if (isPlaying) {
            player.play();
        } else {
            player.pause();
        }
        viewModel.setPlaying(isPlaying);
        updatePlayPauseIcons(isPlaying);
        autoHideHandler.removeCallbacks(hideControlsRunnable);
        autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
    }

    private int countdown = 5;
    private Handler nextEpisodeHandler = new Handler(Looper.getMainLooper());
    private Runnable nextEpisodeRunnable;

    public void handleEndVideo() {
        updateVideoTracking();

        hideLoadingVideo();
        updatePlayPauseIcons(false);
        viewModel.setPlaying(false);
        viewBinding.btnPlayPause.setVisibility(View.GONE);
        viewBinding.btnReplay.setVisibility(View.VISIBLE);

        if (!isLastEpisode() && isSeries) {
            toggleControls();
            viewBinding.layoutNextEpisode.layoutNextEpisode.setVisibility(View.VISIBLE);
            countdown = 5;
            updateNextEpisodeButtonText();
            startNextEpisodeCountdown();
        }
    }

    private void startNextEpisodeCountdown() {
        nextEpisodeRunnable = new Runnable() {
            @Override
            public void run() {
                countdown--;
                if (countdown > 0) {
                    updateNextEpisodeButtonText();
                    nextEpisodeHandler.postDelayed(this, 1000);
                } else {
                    viewBinding.layoutNextEpisode.layoutNextEpisode.setVisibility(View.GONE);
                    onNextEpisodeClick();
                    stopNextEpisodeCountdown();
                }
            }
        };
        nextEpisodeHandler.postDelayed(nextEpisodeRunnable, 1000);
    }

    private void stopNextEpisodeCountdown() {
        if (nextEpisodeHandler != null && nextEpisodeRunnable != null) {
            nextEpisodeHandler.removeCallbacks(nextEpisodeRunnable);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateNextEpisodeButtonText() {
        viewBinding.layoutNextEpisode.tvCountDown.setText(getString(R.string.next_episode) + " (" + countdown + ")");
    }


    // region === Set Up Seek Bar Video ===
    public void setupSeekBar() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY && player.getDuration() > 0) {
                    viewBinding.tvTotalTime.setText(formatTime(player.getDuration()));
                }
            }
        });

        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying() && !isUserSeeking) {
                    long current = player.getCurrentPosition();
                    long duration = player.getDuration();

                    if (duration > 0) {
                        int progress = (int) (1000 * current / duration);
                        viewBinding.seekBar.setProgress(progress);
                        viewBinding.tvCurrentTime.setText(formatTime(current));
                    }

                    if (viewModel.nowVideoPlay != null) {
                        Long introStart = viewModel.nowVideoPlay.getIntroStart();
                        Long introEnd = viewModel.nowVideoPlay.getIntroEnd();

                        if (introStart != null && introEnd != null) {
                            long introStartMs = introStart * 1000L;
                            long introEndMs = introEnd * 1000L;

                             if (current >= introStartMs && current < introEndMs && viewBinding.layoutSeekBar.getVisibility() != View.VISIBLE) {
                                if (viewBinding.btnSkipIntro.getVisibility() != View.VISIBLE) {

                                    viewBinding.btnSkipIntro.setVisibility(View.VISIBLE);
                                    if (viewModel.setting.getAutoSkipIntro()) {
                                        handleSkipIntro();
                                    }
                                }
                            } else {
                                if (viewBinding.btnSkipIntro.getVisibility() == View.VISIBLE) {
                                    viewBinding.btnSkipIntro.setVisibility(View.GONE);
                                }
                            }
                        }
                    }

                    if (viewModel.nowVideoPlay != null && isSeries && !isLastEpisode()) {
                        Long outroStart = viewModel.nowVideoPlay.getOutroStart();

                        if (outroStart != null && player != null) {
                            long outroStartMs = outroStart * 1000L;
                            long outroEndMs = player.getDuration();

                            if (current >= outroStartMs && current < outroEndMs && viewBinding.layoutSeekBar.getVisibility() != View.VISIBLE) {
                                if (viewBinding.btnSkipOutro.getVisibility() != View.VISIBLE) {
                                    viewBinding.btnSkipOutro.setVisibility(View.VISIBLE);
                                    if (viewModel.setting.getAutoNextEpisode()) {
                                        handleSkipOutro();
                                    }
                                }
                            } else {
                                if (viewBinding.btnSkipOutro.getVisibility() == View.VISIBLE) {
                                    viewBinding.btnSkipOutro.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                }


                seekHandler.postDelayed(this, 500);
            }
        };

        seekHandler.post(updateSeekBarRunnable);

        viewBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser || player == null) return;

                long duration = player.getDuration();
                if (duration <= 0) return;

                long newPosition = (duration * progress) / 1000;
                player.seekTo(newPosition);
                viewBinding.tvCurrentTime.setText(formatTime(newPosition));

                if (thumbnailManager != null) {
                    thumbnailManager.showPreviewAt(newPosition);
                    updateThumbnailPosition(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
                autoHideHandler.removeCallbacks(hideControlsRunnable);
                viewBinding.controlVideo.setVisibility(View.GONE);
                viewBinding.layoutBrightness.setVisibility(View.GONE);
                viewBinding.layoutVolume.setVisibility(View.GONE);
                viewBinding.loadingProgress.setVisibility(View.GONE);

                viewBinding.thumbnailPreviewContainer.setVisibility(View.VISIBLE);

                updateThumbnailPosition(seekBar.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                viewBinding.controlVideo.setVisibility(View.VISIBLE);
                viewBinding.layoutBrightness.setVisibility(View.VISIBLE);
                viewBinding.layoutVolume.setVisibility(View.VISIBLE);
                viewBinding.loadingProgress.setVisibility(View.VISIBLE);
                if (thumbnailManager != null) {
                    thumbnailManager.hidePreview();
                }
                viewBinding.thumbnailPreviewContainer.setVisibility(View.GONE);
                autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);

                updateVideoTracking();
            }
        });
    }

    private void updateThumbnailPosition(int progress) {
        viewBinding.seekBar.post(() -> {
            View container = viewBinding.thumbnailPreviewContainer;
            SeekBar seekBar = viewBinding.seekBar;

            int seekBarWidth = seekBar.getWidth();

            int max = seekBar.getMax(); // 1000
            float percent = progress / (float) max;

            int paddingLeft = seekBar.getPaddingLeft();
            int paddingRight = seekBar.getPaddingRight();

            int usableWidth = seekBarWidth - paddingLeft - paddingRight;

            float thumbCenterX = seekBar.getX() + paddingLeft + usableWidth * percent;

            int containerWidth = container.getWidth();
            float translationX = thumbCenterX - containerWidth / 2f;
            float layOutWidth = viewBinding.layoutSeekBar.getWidth() - containerWidth - viewBinding.layoutSeekBar.getPaddingRight() - viewBinding.layoutSeekBar.getPaddingLeft();

            // Clamp
            translationX = Math.max(0, Math.min(translationX, layOutWidth));

            container.setTranslationX(translationX);
        });
    }



    private String formatTime(long millis) {
        if (millis < 0) return "00:00";

        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    // region === Set Up Double Tap, One Tap, Zoom Fit ===
    @SuppressLint("ClickableViewAccessibility")
    public void setupGestureDetector() {
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                if (!isVideoReadyWhenStartActivity || isLockScreen) return true;
                viewBinding.btnSkipOutro.setVisibility(View.GONE);
                viewBinding.btnSkipIntro.setVisibility(View.GONE);
                viewBinding.layoutSeekBar.setVisibility(View.VISIBLE);
                viewBinding.controlVideo.setVisibility(View.GONE);
                viewBinding.layoutBrightness.setVisibility(View.GONE);
                viewBinding.layoutVolume.setVisibility(View.GONE);

                boolean isForward = e.getX() >= (float) viewBinding.playerView.getWidth() / 2;
                handleForwardAndPreviousVideo(isForward);

                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isVideoReadyWhenStartActivity) {
                    toggleControls();
                }
                return true;
            }

            @SuppressLint("DefaultLocale")
            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                if (player != null && player.isPlaying() && !isLockScreen) {
                    viewBinding.playerView.setHapticFeedbackEnabled(true);
                    viewBinding.playerView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                    player.setPlaybackParameters(new PlaybackParameters(viewModel.settingVideoModel.getPlaySpeedWhenPress().getSpeed()));
                    autoHideHandler.removeCallbacks(hideControlsRunnable);
                    autoHideHandler.postDelayed(hideControlsRunnable, 0);

                    runOnUiThread(() -> {
                        viewBinding.tvSpeed.setText(String.format(Locale.US,"%.2fx", viewModel.settingVideoModel.getPlaySpeedWhenPress().getSpeed()));
                        viewBinding.layoutSpeedPress.setVisibility(View.VISIBLE);
                    });
                }
            }
        });

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private float startSpan;

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                startSpan = detector.getCurrentSpan();
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (isLockScreen) return true;
                float scale = detector.getCurrentSpan() / startSpan;
                if (scale > 1.1f && !isZoomed) {
                    isZoomed = true;
                    viewBinding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                }

                else if (scale < 0.9f && isZoomed) {
                    isZoomed = false;
                    viewBinding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                }
                return true;
            }
        });

        viewBinding.playerView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            scaleGestureDetector.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (player != null && player.isPlaying()) {
                    player.setPlaybackParameters(new PlaybackParameters(viewModel.settingVideoModel.getPlaySpeed().getSpeed()));
                    runOnUiThread(() -> viewBinding.layoutSpeedPress.setVisibility(View.GONE));
                }
            }
            return true;
        });
    }
    public void handleForwardAndPreviousVideo(boolean isForward) {
        if (!isVideoReadyWhenStartActivity) return;

        long currentPosition = player.getCurrentPosition();
        long seekToPosition = currentPosition + (isForward ? SEEK_DOUBLE_TAP_MILLIS : -SEEK_DOUBLE_TAP_MILLIS);
        Log.d("SEE", String.valueOf(seekToPosition));
        seekToPosition = Math.max(0, Math.min(seekToPosition, player.getDuration()));
        player.seekTo(seekToPosition);
        showSeekPreview(seekToPosition, isForward);

        showSeekCount(isForward, SEEK_DOUBLE_TAP_MILLIS);
    }
    private void showSeekPreview(long position, boolean isForward) {
        long duration = player.getDuration();
        if (duration > 0) {
            int progress = (int) (1000 * position / duration);
            viewBinding.seekBar.setProgress(progress);
        }
        viewBinding.tvCurrentTime.setText(formatTime(position));

        if (autoHideHandler != null) {
            autoHideHandler.removeCallbacks(hideControlsRunnable);
            long delay = viewBinding.controlVideo.getVisibility() == View.VISIBLE
                    ? AUTO_HIDE_DELAY_MILLIS
                    : DOUBLE_TAP_TIMEOUT;
            autoHideHandler.postDelayed(hideControlsRunnable, delay);
        }
    }
    private void showSeekCount(boolean isForward, int seconds) {
        if (isForward) {
            forwardCount += seconds / 1000; // +10s
            previousCount = 0;
            viewBinding.layoutCountForward.setVisibility(View.VISIBLE);
            viewBinding.layoutCountPrevious.setVisibility(View.GONE);
            viewBinding.tvCountForward.setText(String.format("+ %d", forwardCount));
            animateSeekIcon(true);
            if (player.getCurrentPosition() + SEEK_DOUBLE_TAP_MILLIS >= player.getDuration()) {
                forwardCount = 0;
            }
        } else {
            previousCount += seconds / 1000; // -10s
            forwardCount = 0;
            viewBinding.layoutCountPrevious.setVisibility(View.VISIBLE);
            viewBinding.layoutCountForward.setVisibility(View.GONE);
            viewBinding.tvCountPrevious.setText(String.format("- %d", previousCount));
            animateSeekIcon(false);
            if (player.getCurrentPosition() == 0) {
                previousCount = 0;
            }
        }

        // Reset counter sau 800ms nếu không có tap mới
        countResetHandler.removeCallbacks(resetCountRunnable);
        resetCountRunnable = () -> {
            forwardCount = 0;
            previousCount = 0;
            viewBinding.layoutCountForward.setVisibility(View.GONE);
            viewBinding.layoutCountPrevious.setVisibility(View.GONE);
        };
        countResetHandler.postDelayed(resetCountRunnable, DOUBLE_TAP_TIMEOUT);
    }

    // region === Set Up Brightness and Volume SeekBar ===
    public void setupSeekBarBrightNess() {
        Window window = getWindow();

        if (!viewModel.setting.isSetupBrightness() && viewModel.setting.getBrightness() != null) {
            float brightnessValue = viewModel.setting.getBrightness() / 100f;

            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.screenBrightness = brightnessValue;
            window.setAttributes(layoutParams);
            viewBinding.seekBrightness.setProgress(viewModel.setting.getBrightness());
            updateBrightnessIcon(brightnessValue);
            viewModel.setting.setSetupBrightness(true);

        } else {
            float currentBrightness = window.getAttributes().screenBrightness;
            if (currentBrightness < 0) {
                currentBrightness = 0.5f;
            }
            viewBinding.seekBrightness.setProgress((int) (currentBrightness * 100));
        }
        viewBinding.seekBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float brightness = progress / 100f;

                    Window window = getWindow();
                    WindowManager.LayoutParams layoutParams = window.getAttributes();
                    layoutParams.screenBrightness = brightness;
                    window.setAttributes(layoutParams);

                   updateBrightnessIcon(brightness);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                autoHideHandler.removeCallbacks(hideControlsRunnable);
                viewBinding.controlVideo.setVisibility(View.GONE);
                viewBinding.layoutSeekBar.setVisibility(View.GONE);
                viewBinding.layoutVolume.setVisibility(View.GONE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                viewBinding.btnSkipOutro.setVisibility(View.GONE);
                viewBinding.btnSkipIntro.setVisibility(View.GONE);
                viewBinding.controlVideo.setVisibility(View.VISIBLE);
                viewBinding.layoutSeekBar.setVisibility(View.VISIBLE);
                viewBinding.layoutVolume.setVisibility(View.VISIBLE);
                autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
            }
        });
    }

    // Hàm phụ trợ để tái sử dụng logic đổi icon
    private void updateBrightnessIcon(float brightness) {
        if (brightness < 0.33f) {
            viewBinding.icBrightness.setImageResource(R.drawable.ic_brightness_low);
        } else if (brightness < 0.66f) {
            viewBinding.icBrightness.setImageResource(R.drawable.ic_brightness_medium);
        } else {
            viewBinding.icBrightness.setImageResource(R.drawable.ic_brightness_high);
        }
    }
    public void setupSeekBarVolume() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        int initialProgress;

        if (viewModel.setting != null && !viewModel.setting.isSetupAudio() && viewModel.setting.getAudio() != null) {
            initialProgress = viewModel.setting.getAudio();

            int systemVolume = (int) ((initialProgress / 100f) * maxVolume);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, systemVolume, 0);

            viewModel.setting.setSetupAudio(true);
        } else {
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            initialProgress = (int) ((currentVolume / (float) maxVolume) * 100);
        }

        viewBinding.seekVolume.setProgress(initialProgress);
        updateVolumeIcon(initialProgress);

        viewBinding.seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int newVolume = (int) ((progress / 100f) * maxVolume);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
                    updateVolumeIcon(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                autoHideHandler.removeCallbacks(hideControlsRunnable);
                viewBinding.controlVideo.setVisibility(View.GONE);
                viewBinding.layoutSeekBar.setVisibility(View.GONE);
                viewBinding.layoutBrightness.setVisibility(View.GONE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                viewBinding.btnSkipOutro.setVisibility(View.GONE);
                viewBinding.btnSkipIntro.setVisibility(View.GONE);
                viewBinding.controlVideo.setVisibility(View.VISIBLE);
                viewBinding.layoutSeekBar.setVisibility(View.VISIBLE);
                viewBinding.layoutBrightness.setVisibility(View.VISIBLE);
                autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
            }
        });

    }

    // region === Set Up Display Control ===
    private final Runnable hideControlsRunnable = () -> {
        viewBinding.controlVideo.setVisibility(View.GONE);
        viewBinding.layoutSeekBar.setVisibility(View.GONE);
        viewBinding.layoutBrightness.setVisibility(View.GONE);
        viewBinding.layoutVolume.setVisibility(View.GONE);
    };
    private void toggleControls() {
        boolean show = viewBinding.controlVideo.getVisibility() != View.VISIBLE;
        if (show) {
            viewBinding.btnSkipOutro.setVisibility(View.GONE);
            viewBinding.btnSkipIntro.setVisibility(View.GONE);
        }
        viewBinding.controlVideo.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!isLockScreen) viewBinding.layoutSeekBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!isLockScreen) viewBinding.layoutBrightness.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!isLockScreen) viewBinding.layoutVolume.setVisibility(show ? View.VISIBLE : View.GONE);

        if (show && autoHideHandler != null) {
            autoHideHandler.removeCallbacks(hideControlsRunnable);
            autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
        }
    }

    // region === Set Up View ===
    public void hideSystemUI() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            getWindow().getInsetsController().hide(WindowInsets.Type.systemBars());
            getWindow().getInsetsController().setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }
    }
    public void handleLockScreen() {
        if (isLockScreen) {
            isLockScreen = false;
            viewBinding.btnSkipOutro.setVisibility(View.GONE);
            viewBinding.btnSkipIntro.setVisibility(View.GONE);
            viewBinding.btnUnLockScreen.setVisibility(View.GONE);
            viewBinding.btnLockScreen.setVisibility(View.VISIBLE);
            viewBinding.buttonControlVideo.setVisibility(View.VISIBLE);
            viewBinding.layoutSeekBar.setVisibility(View.VISIBLE);
            viewBinding.layoutBrightness.setVisibility(View.VISIBLE);
            viewBinding.layoutVolume.setVisibility(View.VISIBLE);
        } else {
            isLockScreen = true;
            viewBinding.btnUnLockScreen.setVisibility(View.VISIBLE);
            viewBinding.btnLockScreen.setVisibility(View.GONE);
            viewBinding.buttonControlVideo.setVisibility(View.GONE);
            viewBinding.layoutSeekBar.setVisibility(View.GONE);
            viewBinding.layoutBrightness.setVisibility(View.GONE);
            viewBinding.layoutVolume.setVisibility(View.GONE);
        }
        autoHideHandler.removeCallbacks(hideControlsRunnable);
        autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
    }

    private void showLoadingVideo() {
        if (!isBuffering) {
            isBuffering = true;
            if (isUserSeeking) {
                viewBinding.loadingProgress.setVisibility(View.GONE);
            } else {
                viewBinding.loadingProgress.setVisibility(View.VISIBLE);

            }
            viewBinding.btnPlayPause.setVisibility(View.GONE);
        }
    }
    private void hideLoadingVideo() {
        if (isBuffering) {
            isBuffering = false;
            viewBinding.loadingProgress.setVisibility(View.GONE);
            viewBinding.btnPlayPause.setVisibility(View.VISIBLE);
        }
    }
    private void updateVolumeIcon(int progress) {
        if (progress == 0) {
            viewBinding.icVolume.setImageResource(R.drawable.ic_volume_mute);
        } else if (progress < 50) {
            viewBinding.icVolume.setImageResource(R.drawable.ic_volume_low);
        } else {
            viewBinding.icVolume.setImageResource(R.drawable.ic_volume_high);
        }
    }
    private void animateSeekIcon(boolean isForward) {
        ImageView icon = isForward
                ? viewBinding.icForward
                : viewBinding.icPrevious;

        View layout = isForward ? viewBinding.layoutCountForward : viewBinding.layoutCountPrevious;
        layout.setVisibility(View.VISIBLE);
        icon.setVisibility(View.VISIBLE);

        icon.setAlpha(1f);
        icon.setTranslationX(0f);
        icon.setRotation(0f);

        float moveDistance = isForward ? 50f : -50f;

        ObjectAnimator moveAnim = ObjectAnimator.ofFloat(icon, "translationX", 0f, moveDistance);
        moveAnim.setDuration(400);
        moveAnim.setInterpolator(new DecelerateInterpolator());

        moveAnim.start();
    }

    // region === Set Up Icon ===
    private void updatePlayPauseIcons(boolean isPlaying) {
        viewBinding.icPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }
    private void animateRotate(View view, boolean isForward) {
        float start = 0f;
        float end = isForward ? 360f : -360f;

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", start, end);
        animator.setDuration(400);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_watch_movie;
    }
    @Override
    public int getBindingVariable() {
        return BR.vm;
    }
    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    // region === Show Setting Video ===
    private void showSettingsBottomSheet() {
        toggleControls();
        SettingBottomSheetDialog sheet = new SettingBottomSheetDialog(this, viewModel.settingVideoModel, this);
        sheet.show();
        Objects.requireNonNull(sheet.getWindow()).getDecorView().post(sheet::setupTransparentWindow);
        sheet.setOnDismissListener(v -> {
            hideSystemUI();
        });
    }
    private void showSettingsPlaySpeedBottomSheet(int type) {
        PlaySpeedBottomSheetDialog sheet = new PlaySpeedBottomSheetDialog(this, viewModel.settingVideoModel, this, type);
        sheet.show();
        Objects.requireNonNull(sheet.getWindow()).getDecorView().post(sheet::setupTransparentWindow);
        sheet.setOnDismissListener(v -> {
            hideSystemUI();
        });
    }
    private void showSettingsQualityBottomSheet() {
        QualityBottomSheetDialog sheet = new QualityBottomSheetDialog(this, viewModel.settingVideoModel, this);
        sheet.show();
        Objects.requireNonNull(sheet.getWindow()).getDecorView().post(sheet::setupTransparentWindow);
        sheet.setOnDismissListener(v -> {
            hideSystemUI();
        });
    }
    private void showSettingsMoreOptionBottomSheet() {
        MoreOptionBottomSheetDialog sheet = new MoreOptionBottomSheetDialog(this, viewModel.settingVideoModel, this);
        sheet.show();
        Objects.requireNonNull(sheet.getWindow()).getDecorView().post(sheet::setupTransparentWindow);
        sheet.setOnDismissListener(v -> {
            hideSystemUI();
        });
    }

    // region === Show Runnable ===
    private void startSeekBarUpdate() {
        if (updateSeekBarRunnable != null) {
            seekHandler.post(updateSeekBarRunnable);
        }
    }
    private void stopSeekBarUpdate() {
        if (seekHandler != null && updateSeekBarRunnable != null) {
            seekHandler.removeCallbacks(updateSeekBarRunnable);
        }
    }
    private void startVolumeObserver() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        volumeObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int progress = (int) ((currentVolume / (float) maxVolume) * 100);
                viewBinding.seekVolume.setProgress(progress);
                updateVolumeIcon(progress);
            }
        };

        getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI,
                true,
                volumeObserver
        );
    }
    private void stopVolumeObserver() {
        if (volumeObserver != null) {
            getContentResolver().unregisterContentObserver(volumeObserver);
            volumeObserver = null;
        }
    }
    private ContentObserver volumeObserver;

    // region === Life Cycle===
    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        startSeekBarUpdate();
        startVolumeObserver();
        startTrackingLoop();
        viewModel.startTokenAutoRefresh();

    }
    @Override
    protected void onPause() {
        super.onPause();
        stopSeekBarUpdate();

        if (player != null) {
            updateVideoTracking();
            player.pause();
        }
        stopVolumeObserver();

        viewModel.stopTokenAutoRefresh();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (player != null) {
            updateVideoTracking();
            player.release();
            player = null;
        }

        // XÓA TẤT CẢ RUNNABLE TRƯỚC KHI HỦY
        if (seekHandler != null) {
            seekHandler.removeCallbacksAndMessages(null);
        }
        if (autoHideHandler != null) {
            autoHideHandler.removeCallbacksAndMessages(null);
        }
        if (countResetHandler != null) {
            countResetHandler.removeCallbacksAndMessages(null);
        }

        if (nextEpisodeHandler != null) {
            nextEpisodeHandler.removeCallbacksAndMessages(null);
        }

        stopSeekBarUpdate();
        stopVolumeObserver();
        stopTrackingLoop();
        viewModel.stopTokenAutoRefresh();

        ((MVVMApplication) application).destroyMqtt();
    }

    // region === Click ===

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_close:
                player.pause();
                toggleControls();
                viewBinding.lDialogExit.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_cancel:
            case R.id.l_dialog_exit:
                viewBinding.lDialogExit.setVisibility(View.GONE);
                updatePlayPauseState(Boolean.TRUE.equals(viewModel.getIsPlaying().getValue()));
                break;
            case R.id.btn_ok:
                finish();
                break;
            case R.id.btn_lock_screen:
            case R.id.btn_un_lock_screen:
                handleLockScreen();
                break;
            case R.id.btn_play_pause:
                updatePlayPauseState(!player.isPlaying());
                break;
            case R.id.btn_replay:
                viewBinding.btnPlayPause.setVisibility(View.VISIBLE);
                viewBinding.btnReplay.setVisibility(View.GONE);
                player.seekTo(0);
                updatePlayPauseState(true);
                break;
            case R.id.btn_forward:
                animateRotate(viewBinding.icForwardLooping, true);
                handleForwardAndPreviousVideo(true);
                break;

            case R.id.btn_previous:
                animateRotate(viewBinding.icPreviousLooping, false);
                handleForwardAndPreviousVideo(false);
                break;

            case R.id.btn_setting:
                showSettingsBottomSheet();
                break;

            case R.id.btn_next_episode:
//                https://files.vidstack.io/sprite-fight/hls/stream.m3u8
                onNextEpisodeClick();
                break;

            case R.id.btn_episodes:
                showListEpisode();
                break;

            case R.id.btn_close_episodes:
                viewBinding.layoutListEpisodes.listEpisode.setVisibility(View.GONE);
                toggleControls();
                player.play();
                break;
            case R.id.btn_skip_intro:
                handleSkipIntro();
                break;

            case R.id.btn_skip_outro:
                handleSkipOutro();
                break;

            case R.id.btn_close_next_episode:
                viewBinding.layoutNextEpisode.layoutNextEpisode.setVisibility(View.GONE);
                stopNextEpisodeCountdown();
                break;
            case R.id.btn_layout_next_episode:
                viewBinding.layoutNextEpisode.layoutNextEpisode.setVisibility(View.GONE);
                stopNextEpisodeCountdown();
                onNextEpisodeClick();
                break;
            default:
                break;
        }
    }
    private void handleSkipIntro() {
        if (viewModel.nowVideoPlay != null && viewModel.nowVideoPlay.getIntroEnd() != null) {
            long introEndMs = viewModel.nowVideoPlay.getIntroEnd() * 1000L;
            player.seekTo(introEndMs);
            viewBinding.btnSkipIntro.setVisibility(View.GONE);
        }
    }

    private void handleSkipOutro() {
        if (viewModel.nowVideoPlay == null) return;

        if (isSeries && !isLastEpisode()) {
            viewBinding.btnSkipOutro.setVisibility(View.GONE);
            onNextEpisodeClick();
        }
    }


    @Override
    public void onQualityClicked() {
        showSettingsQualityBottomSheet();
    }
    @Override
    public void onPlaybackSpeedPressClicked() {
        showSettingsPlaySpeedBottomSheet(PlaySpeedBottomSheetDialog.TYPE_SPEED_PRESS);
    }
    @Override
    public void onPlaybackSpeedClicked() {
        showSettingsPlaySpeedBottomSheet(PlaySpeedBottomSheetDialog.TYPE_SPEED);
    }
    @Override
    public void onSubtitleClicked() {

    }
    @Override
    public void onLockScreenClicked() {
        handleLockScreen();
    }
    @Override
    public void onMoreOptionsClicked() {
        showSettingsMoreOptionBottomSheet();
    }

    // region === Handle Setting ===
    @Override
    public void updatePlaySpeedVideo(float speed, int typeSpeedOption) {
        if (typeSpeedOption == PlaySpeedBottomSheetDialog.TYPE_SPEED) {
            viewModel.settingVideoModel.getPlaySpeed().setSpeed(speed);
            if (player != null) {
                PlaybackParameters params = new PlaybackParameters(viewModel.settingVideoModel.getPlaySpeed().getSpeed());
                player.setPlaybackParameters(params);
            }
        } else {
            viewModel.settingVideoModel.getPlaySpeedWhenPress().setSpeed(speed);
        }
    }
    @Override
    public void updateQualityVideo(SettingVideoModel settingVideoModel) {
        viewModel.settingVideoModel = settingVideoModel;
        applyQualityFromSetting();
    }
    private void applyQualityFromSetting() {
        if (player == null) return;

        SettingVideoModel.Quality qualitySetting =  viewModel.settingVideoModel.getQuality();
        TrackSelectionParameters.Builder builder = player.getTrackSelectionParameters().buildUpon();

        if (qualitySetting.isAuto()) {
            builder.clearOverridesOfType(C.TRACK_TYPE_VIDEO);
        } else {
            VideoQuality selected = qualitySetting.getResolution();
            if (selected == null) return;

            Tracks currentTracks = player.getCurrentTracks();

            for (Tracks.Group group : currentTracks.getGroups()) {
                if (group.getType() == C.TRACK_TYPE_VIDEO &&
                        group.getMediaTrackGroup().id.equals(selected.groupIndex)) {

                    TrackSelectionOverride override = new TrackSelectionOverride(
                            group.getMediaTrackGroup(),
                            ImmutableList.of(selected.trackIndex)
                    );

                    builder.clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                            .addOverride(override);
                    break;
                }
            }
        }
        player.setTrackSelectionParameters(builder.build());
    }

    //region === Change Video ===
    private void changeEpisode(String newUri) {
        if (player == null) return;

        viewModel.nowUriPlay = newUri;

        toggleControls();
        viewModel.updateSettingWhenChangeEpisode();
        showLoadingVideo();
        reset();

        TrackSelectionParameters.Builder builder = player.getTrackSelectionParameters().buildUpon();
        builder.clearOverridesOfType(C.TRACK_TYPE_VIDEO);
        player.setTrackSelectionParameters(builder.build());

        MediaItem newItem = MediaItem.fromUri(newUri);
        isStartContinueWatch = false;
        player.stop();
        player.clearMediaItems();
        player.setMediaItem(newItem);
        player.prepare();
        player.setPlayWhenReady(true);
    }
    public Boolean isLastEpisode() {
        if (viewModel.movieDetails == null || viewModel.nowEpisodePlay == null) {
            return true;
        }

        if (viewModel.movieDetails.getType() != Constants.TYPE_MOVIE_SERIES) {
            return true; // Phim lẻ
        }

        List<SeasonResponse> seasons = viewModel.movieDetails.getSeasons();
        if (seasons == null || seasons.isEmpty()) {
            return true;
        }

        // Tìm season hiện tại
        SeasonResponse currentSeason = findCurrentSeason();
        if (currentSeason == null || currentSeason.getEpisodes() == null || currentSeason.getEpisodes().isEmpty()) {
            return true;
        }

        // Lấy tập cuối của mùa hiện tại
        MovieItemResponse lastInCurrentSeason = currentSeason.getEpisodes()
                .get(currentSeason.getEpisodes().size() - 1);

        // Nếu tập hiện tại là tập cuối của mùa hiện tại → kiểm tra có mùa sau không
        if (viewModel.nowEpisodePlay.getId().equals(lastInCurrentSeason.getId())) {
            int currentSeasonIndex = seasons.indexOf(currentSeason);
            return currentSeasonIndex == seasons.size() - 1; // Không còn mùa sau
        }

        return false; // Còn tập trong mùa hiện tại
    }

    private SeasonResponse findCurrentSeason() {
        for (SeasonResponse season : viewModel.movieDetails.getSeasons()) {
            if (season.getEpisodes() != null) {
                for (MovieItemResponse episode : season.getEpisodes()) {
                    if (episode.getId().equals(viewModel.nowEpisodePlay.getId())) {
                        return season;
                    }
                }
            }
        }
        return null;
    }

    private String findLabelSeason() {
        SeasonResponse currentSeason = findCurrentSeason();
        if (currentSeason == null) {
            return "";
        }

        List<SeasonResponse> seasons = viewModel.movieDetails.getSeasons();
        if (seasons == null) return "";

        int seasonIndex = seasons.indexOf(currentSeason);
        if (seasonIndex == -1) return "";

        return " (" + getString(R.string.season) + " " + (seasonIndex + 1) + ")";
    }

    public MovieItemResponse getNextEpisode() {
        if (viewModel.movieDetails == null || viewModel.nowEpisodePlay == null) {
            return null;
        }

        List<SeasonResponse> seasons = viewModel.movieDetails.getSeasons();
        if (seasons == null || seasons.isEmpty()) {
            return null;
        }

        SeasonResponse currentSeason = findCurrentSeason();
        if (currentSeason == null || currentSeason.getEpisodes() == null || currentSeason.getEpisodes().isEmpty()) {
            return null;
        }

        List<MovieItemResponse> episodes = currentSeason.getEpisodes();
        int currentIndex = -1;

        // Tìm vị trí tập hiện tại
        for (int i = 0; i < episodes.size(); i++) {
            if (episodes.get(i).getId().equals(viewModel.nowEpisodePlay.getId())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1) return null;

        // 1. Còn tập trong mùa hiện tại → trả về tập kế
        if (currentIndex < episodes.size() - 1) {
            return episodes.get(currentIndex + 1);
        }

        // 2. Là tập cuối mùa → tìm mùa tiếp theo
        int currentSeasonIndex = seasons.indexOf(currentSeason);
        if (currentSeasonIndex == -1 || currentSeasonIndex == seasons.size() - 1) {
            return null; // Không có mùa sau
        }

        SeasonResponse nextSeason = seasons.get(currentSeasonIndex + 1);
        if (nextSeason.getEpisodes() == null || nextSeason.getEpisodes().isEmpty()) {
            return null;
        }

        return nextSeason.getEpisodes().get(0); // Tập đầu của mùa sau
    }

    public void onNextEpisodeClick() {
        updateVideoTracking();
        viewModel.nowEpisodePlay = getNextEpisode();
        viewModel.nowVideoPlay = viewModel.nowEpisodePlay.getVideo();
        setUpViewForSeriesMovie();
        changeEpisode(getVideoUri(viewModel.nowVideoPlay));
    }

    public void setUpAdapterEpisode() {
        seasonItemAdapter = new SeasonItemAdapter(this, this);
        viewBinding.layoutListEpisodes.rvSeason.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        viewBinding.layoutListEpisodes.rvSeason.setAdapter(seasonItemAdapter);

        episodeItemListHoriAdapter = new EpisodeItemListHoriAdapter(this, this);
        viewBinding.layoutListEpisodes.rvEpisode.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        viewBinding.layoutListEpisodes.rvEpisode.setAdapter(episodeItemListHoriAdapter);
    }
    @SuppressLint("NotifyDataSetChanged")
    public void showListEpisode() {
        toggleControls();
        player.pause();
        viewModel.movieDetails.setSeasonAndEpisodeSelectedAndPlaying(viewModel.nowEpisodePlay.getId());
        seasonItemAdapter.setData(viewModel.movieDetails.getSeasons());
        episodeItemListHoriAdapter.setData(viewModel.movieDetails.getSelectedSeasonEpisodes(), viewBinding.layoutListEpisodes.rvEpisode);
        currentSeasonIndex = viewModel.movieDetails.getIndexSeasonSelect();
        viewBinding.layoutListEpisodes.listEpisode.setVisibility(View.VISIBLE);
    }
    @Override
    public void onEpisodeClick(MovieItemResponse season) {
        updateVideoTracking();
        viewBinding.layoutListEpisodes.listEpisode.setVisibility(View.GONE);
        viewModel.nowEpisodePlay = season;
        viewModel.nowVideoPlay = viewModel.nowEpisodePlay.getVideo();
        setUpViewForSeriesMovie();
        changeEpisode(getVideoUri(viewModel.nowVideoPlay));
        toggleControls();
    }

    public void setUpNextEpisodeLayout() {
        if (getNextEpisode() == null) return;

        String img = "";

        if (getNextEpisode().getThumbnailUrl() != null) {
            img = getNextEpisode().getThumbnailUrl();
        } else {
            img = getNextEpisode().getVideo().getThumbnailUrl();
        }

        Glide.with(this)
                .load(img)
                .placeholder(R.drawable.place_holder_16_9)
                .error(R.drawable.place_holder_16_9)
                .into(viewBinding.layoutNextEpisode.image);
    }

    @Override
    public void onSeasonClick(SeasonResponse season) {
        viewModel.movieDetails.setSeasonSelect(season.getId());
        episodeItemListHoriAdapter.setData(viewModel.movieDetails.getSeasonEpisodesById(season.getId()), viewBinding.layoutListEpisodes.rvEpisode);

        currentSeasonIndex = viewModel.movieDetails.getIndexSeasonSelect();
    }

    public void updateVideoTracking() {
        if (!viewModel.isLogin()) return;
        TrackingWatchHistoryRequest request = new TrackingWatchHistoryRequest();

        request.setLastWatchSeconds(player.getCurrentPosition() /1000);

        if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SINGLE) {
            request.setMovieItemId(viewModel.movieDetails.getSeasons().get(0).getId());
        } else if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES) {
            request.setMovieItemId(viewModel.nowEpisodePlay.getId());
        }

        viewModel.updateTrackingMovie(request);
    }

    private void startTrackingLoop() {
        if (trackingRunnable == null) {
            trackingRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isStartContinueWatch) {
                        updateVideoTracking();
                    }
                    trackingHandler.postDelayed(this, TRACKING_INTERVAL_MS);
                }
            };
        }

        trackingHandler.postDelayed(trackingRunnable, TRACKING_INTERVAL_MS);
    }

    private void stopTrackingLoop() {
        if (trackingHandler != null && trackingRunnable != null) {
            trackingHandler.removeCallbacks(trackingRunnable);
        }
    }

    @Override
    public void onMessageReceived(Message message) {
        super.onMessageReceived(message);
        Log.d("MQTT_LOG", Objects.requireNonNull(GsonUtils.toJson(message)));
        if (!viewModel.isLiveRoom) return;
        switch (message.getCmd()) {
            case Command.CMD_PARTICIPANT_JOIN:
                participantJoin(message);
                break;
            case Command.CMD_ROOM_OPTION:
                roomOption(message);
                break;
            default:
                break;
        }
    }

    private void sendMqttMessage(Message message, String topic) {
        Timber.w("MQTT_LOG: MESSAGE SEND! Topic: %s | Payload: %s", topic, GsonUtils.toJson(message));
        ((MVVMApplication) application).sendMessageMqtt(message, topic);
    }
    public void participantJoin(Message message) {
        if (!viewModel.isHost) return;
        String json =  GsonUtils.toJson(message.getData());
        if (json != null) {
            ParticipantJoinModel participantJoinModel = GsonUtils.fromJson(json, ParticipantJoinModel.class);
            if (participantJoinModel != null) {
                String topic = Constants.TOPIC + viewModel.roomDetail.getId() + "/" + participantJoinModel.getId();
                Message msg = new Message();

                msg.setCmd(Command.CMD_ROOM_OPTION);
                if (player == null) return;
                RoomOptionModel roomOptionModel = new RoomOptionModel();
                roomOptionModel.setPlay(player.isPlaying());
                roomOptionModel.setCurrentPositionMovie(player.getCurrentPosition());

                msg.setData(roomOptionModel);
                sendMqttMessage(msg, topic);
            }
        }
    }

    public void roomOption(Message message) {
        if (viewModel.isHost) return;
        String json =  GsonUtils.toJson(message.getData());
        if (json != null) {
            RoomOptionModel roomOptionModel = GsonUtils.fromJson(json, RoomOptionModel.class);
            if (player != null && roomOptionModel != null) {

                player.seekTo(roomOptionModel.getCurrentPositionMovie());
                if (roomOptionModel.isPlay()) {
                    player.play();
                } else {
                    player.pause();
                }
            }
        }
    }
}

