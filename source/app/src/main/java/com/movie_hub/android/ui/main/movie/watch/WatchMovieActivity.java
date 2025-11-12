package com.movie_hub.android.ui.main.movie.watch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
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
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
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
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;

import com.google.common.collect.ImmutableList;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.ActivityWatchMovieBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.main.movie.watch.dialog.MoreOptionBottomSheetDialog;
import com.movie_hub.android.ui.main.movie.watch.dialog.PlaySpeedBottomSheetDialog;
import com.movie_hub.android.ui.main.movie.watch.dialog.QualityBottomSheetDialog;
import com.movie_hub.android.ui.main.movie.watch.dialog.SettingBottomSheetDialog;
import com.movie_hub.android.ui.main.movie.watch.setting.SettingVideoModel;
import com.movie_hub.android.ui.main.movie.watch.setting.VideoQuality;

import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

import eu.davidea.flexibleadapter.databinding.BR;

public class WatchMovieActivity extends BaseActivity<ActivityWatchMovieBinding, WatchMovieViewModel> implements View.OnClickListener,
        SettingBottomSheetDialog.SettingBottomSheetCallback,
        PlaySpeedBottomSheetDialog.PlaySpeedBottomSheetCallback,
        QualityBottomSheetDialog .QualityBottomSheetCallback,
        MoreOptionBottomSheetDialog.MoreOptionBottomSheetCallback{
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        hideSystemUI();

        MovieResponse movie = getIntent().getParcelableExtra("movie_details");
        if (movie != null) {
            viewModel.setMovieDetails(movie);
            setUpView();
//            setUpMovie();
        }

        initMovie();
    }

    //region === Init Movie ===
    public void initMovie() {
        reset();
        setUpMovie();
        viewModel.getIsPlaying().observe(this, playing -> {
            updatePlayPauseIcons(playing);
        });
    }
    public void reset() {
        forwardCount = 0;
        previousCount = 0;
        isVideoReadyWhenStartActivity = false;
        viewBinding.seekBar.setProgress(0);
    }
    @SuppressLint("ClickableViewAccessibility")
    public void setUpMovie() {
        player = new ExoPlayer.Builder(this).build();
        viewBinding.playerView.setPlayer(player);
        showLoadingVideo();

        MediaItem mediaItem = MediaItem.fromUri("https://files.vidstack.io/sprite-fight/hls/stream.m3u8");

        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(true);

        setupPlayerListener();
        setupSeekBar();
        setupGestureDetector();
        setupSeekBarBrightNess();
        setupSeekBarVolume();
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

        VideoQuality auto = new VideoQuality();
        auto.label = getString(R.string.auto);

        if (viewModel.settingVideoModel.getQuality().isAuto()) {
            auto.isCheck = true;
        } else {
            for (VideoQuality quality : viewModel.settingVideoModel.getAvailableQualities()) {
                if (Objects.equals(quality.label, viewModel.settingVideoModel.getQuality().getResolution().label)) {
                    quality.isCheck = true;
                    break;
                }
            }
        }
        viewModel.settingVideoModel.getAvailableQualities().add(0, auto);
    }

    // region === Set Up Listener Video ===
    private void setupPlayerListener() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_BUFFERING) {
                    viewBinding.btnReplay.setVisibility(View.GONE);
                    showLoadingVideo();
                } else if (state == Player.STATE_READY) {
                    isVideoReadyWhenStartActivity = true;
                    viewBinding.btnReplay.setVisibility(View.GONE);
                    hideLoadingVideo();
                    if (player.getDuration() > 0) {
                        viewBinding.tvTotalTime.setText(formatTime(player.getDuration()));
                    }
                } else if (state == Player.STATE_ENDED) {
                    handleEndVideo();
                }
            }
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                viewModel.setPlaying(isPlaying);
                updatePlayPauseIcons(isPlaying);
            }

            @Override
            public void onTracksChanged(Tracks tracks) {
                extractAvailableQualities(tracks);
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
    }
    public void handleEndVideo() {
        updatePlayPauseIcons(false);
        viewModel.setPlaying(false);
        viewBinding.btnPlayPause.setVisibility(View.GONE);
        viewBinding.btnReplay.setVisibility(View.VISIBLE);
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
                        viewBinding.tvCurrentTimeSecond.setText(formatTime(current));
                    }
                }
                seekHandler.postDelayed(this, 500);
            }
        };

        seekHandler.post(updateSeekBarRunnable);

        viewBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null) {
                    long duration = player.getDuration();
                    long newPosition = (duration * progress) / 1000;
                    player.seekTo(newPosition);
                    viewBinding.tvCurrentTime.setText(formatTime(newPosition));
                    viewBinding.tvCurrentTimeSecond.setText(formatTime(newPosition));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
                autoHideHandler.removeCallbacks(hideControlsRunnable);
                viewBinding.controlVideo.setVisibility(View.GONE);
                viewBinding.layoutBrightness.setVisibility(View.GONE);
                viewBinding.layoutVolume.setVisibility(View.GONE);
                viewBinding.tvCurrentTimeSecond.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                viewBinding.controlVideo.setVisibility(View.VISIBLE);
                viewBinding.layoutBrightness.setVisibility(View.VISIBLE);
                viewBinding.layoutVolume.setVisibility(View.VISIBLE);
                viewBinding.tvCurrentTimeSecond.setVisibility(View.GONE);
                autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
            }
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
        viewBinding.tvCurrentTimeSecond.setText(formatTime(position));
        autoHideHandler.removeCallbacks(hideControlsRunnable);

        if (viewBinding.controlVideo.getVisibility() == View.VISIBLE) {
            autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
        } else {
            autoHideHandler.postDelayed(hideControlsRunnable, DOUBLE_TAP_TIMEOUT);
        }

    }
    private void showSeekCount(boolean isForward, int seconds) {
        viewBinding.tvCurrentTimeSecond.setVisibility(View.VISIBLE);
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
            viewBinding.tvCurrentTimeSecond.setVisibility(View.GONE);
        };
        countResetHandler.postDelayed(resetCountRunnable, DOUBLE_TAP_TIMEOUT);
    }

    // region === Set Up Brightness and Volume SeekBar ===
    public void setupSeekBarBrightNess() {
        Window window = getWindow();
        float currentBrightness = window.getAttributes().screenBrightness;
        if (currentBrightness < 0) {
            currentBrightness = 0.5f;
        }
        viewBinding.seekBrightness.setProgress((int) (currentBrightness * 100));

        viewBinding.seekBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float brightness = progress / 100f;

                    Window window = getWindow();
                    WindowManager.LayoutParams layoutParams = window.getAttributes();
                    layoutParams.screenBrightness = brightness; // 0.0f = tối, 1.0f = sáng nhất
                    window.setAttributes(layoutParams);

                    if (brightness < 0.33f) {
                        viewBinding.icBrightness.setImageResource(R.drawable.ic_brightness_low);
                    } else if (brightness < 0.66f) {
                        viewBinding.icBrightness.setImageResource(R.drawable.ic_brightness_medium);
                    } else {
                        viewBinding.icBrightness.setImageResource(R.drawable.ic_brightness_high);
                    }
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
                viewBinding.controlVideo.setVisibility(View.VISIBLE);
                viewBinding.layoutSeekBar.setVisibility(View.VISIBLE);
                viewBinding.layoutVolume.setVisibility(View.VISIBLE);
                autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
            }
        });
    }
    public void setupSeekBarVolume() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        int progress = (int) ((currentVolume / (float) maxVolume) * 100);
        viewBinding.seekVolume.setProgress(progress);

        updateVolumeIcon(progress);

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
        viewBinding.controlVideo.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!isLockScreen) viewBinding.layoutSeekBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!isLockScreen) viewBinding.layoutBrightness.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!isLockScreen) viewBinding.layoutVolume.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            autoHideHandler.removeCallbacks(hideControlsRunnable);
            autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
        }
    }

    // region === Set Up View ===
    public void setUpView() {
        viewBinding.nameMovie.setText(Objects.requireNonNull(viewModel.getMovieDetails().getValue()).getTitle());
        viewBinding.nameMovieOriginal.setText(Objects.requireNonNull(viewModel.getMovieDetails().getValue()).getOriginalTitle());
    }
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
    }
    private void showLoadingVideo() {
        if (!isBuffering) {
            isBuffering = true;
            viewBinding.loadingProgress.setVisibility(View.VISIBLE);
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
                android.provider.Settings.System.CONTENT_URI,
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
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopSeekBarUpdate();

        if (player != null) {
            player.pause();
        }
        stopVolumeObserver();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (player != null) {
            player.release();
            player = null;
        }

        if (seekHandler != null && updateSeekBarRunnable != null) {
            seekHandler.removeCallbacks(updateSeekBarRunnable);
        }

        if (autoHideHandler != null) {
            autoHideHandler.removeCallbacks(hideControlsRunnable);
        }

        if (countResetHandler != null && resetCountRunnable != null) {
            countResetHandler.removeCallbacks(resetCountRunnable);
        }

        seekHandler = null;
        autoHideHandler = null;
        countResetHandler = null;
        stopVolumeObserver();
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
                changeEpisode("https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8");
                break;

            case R.id.btn_episodes:
                showListEpisode();
                break;

            case R.id.btn_close_episodes:
                viewBinding.layoutListEpisodes.listEpisode.setVisibility(View.GONE);
                break;
            default:
                break;
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

    public void showListEpisode() {
        toggleControls();
        viewBinding.layoutListEpisodes.listEpisode.setVisibility(View.VISIBLE);
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

        toggleControls();
        viewModel.updateSettingWhenChangeEpisode();
        showLoadingVideo();
        reset();

        TrackSelectionParameters.Builder builder = player.getTrackSelectionParameters().buildUpon();
        builder.clearOverridesOfType(C.TRACK_TYPE_VIDEO);
        player.setTrackSelectionParameters(builder.build());

        MediaItem newItem = MediaItem.fromUri(newUri);
        player.stop();
        player.clearMediaItems();
        player.setMediaItem(newItem);
        player.prepare();
        player.setPlayWhenReady(true);
    }
}

