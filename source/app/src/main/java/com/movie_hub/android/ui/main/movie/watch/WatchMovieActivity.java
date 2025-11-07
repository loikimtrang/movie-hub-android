package com.movie_hub.android.ui.main.movie.watch;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.movie_hub.android.R;
import com.movie_hub.android.custom.CustomDialog;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.ActivityWatchMovieBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.main.account.language.model.LanguageItemModel;

import java.util.Objects;

import eu.davidea.flexibleadapter.databinding.BR;

public class WatchMovieActivity extends BaseActivity<ActivityWatchMovieBinding, WatchMovieViewModel> implements View.OnClickListener {
    private ExoPlayer player;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int SEEK_DOUBLE_TAP_MILLIS = 10000;
    private Handler seekHandler = new Handler(Looper.getMainLooper());
    private final Handler autoHideHandler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarRunnable;
    private boolean isUserSeeking = false;
    private boolean isBuffering = false;
    private boolean isLockScreen = false;

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
        setUpMovie();
        viewModel.getIsPlaying().observe(this, playing -> {
            updatePlayPauseIcons(playing);
        });
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
    }

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
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
                autoHideHandler.removeCallbacks(hideControlsRunnable);
                viewBinding.controlVideo.setVisibility(View.GONE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                viewBinding.controlVideo.setVisibility(View.VISIBLE);
                autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
            }
        });
    }
    private final Handler singleTapHandler = new Handler(Looper.getMainLooper());
    private Runnable singleTapRunnable;
    private boolean isDoubleTap = false;

    @SuppressLint("ClickableViewAccessibility")
    public void setupGestureDetector() {
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                // Bắt đầu đếm single tap
                isDoubleTap = false;
                singleTapRunnable = () -> {
                    if (!isDoubleTap) {
                        if (!isBuffering) {
                            toggleControls();
                        }
                    }
                };
                singleTapHandler.postDelayed(singleTapRunnable, 300);
                return true;
            }

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                if (isLockScreen) return true;

                isDoubleTap = true;
                singleTapHandler.removeCallbacks(singleTapRunnable);

                viewBinding.layoutSeekBar.setVisibility(View.VISIBLE);
                viewBinding.controlVideo.setVisibility(View.GONE);

                boolean isForward = e.getX() >= (float) viewBinding.playerView.getWidth() / 2;
                handleForwardAndPreviousVideo(isForward);

                return true;
            }
        });

        viewBinding.playerView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    public void handleForwardAndPreviousVideo(boolean isForward) {
        long seekToPosition;
        long currentPosition = player.getCurrentPosition();

        seekToPosition = currentPosition + (isForward ? SEEK_DOUBLE_TAP_MILLIS : -SEEK_DOUBLE_TAP_MILLIS);
        seekToPosition = Math.max(0, Math.min(seekToPosition, player.getDuration()));

        player.seekTo(seekToPosition);
        showSeekPreview(seekToPosition, isForward);
    }

    private void showSeekPreview(long position, boolean isForward) {
        long duration = player.getDuration();
        if (duration > 0) {
            int progress = (int) (1000 * position / duration);
            viewBinding.seekBar.setProgress(progress);
        }
        viewBinding.tvCurrentTime.setText(formatTime(position));
        autoHideHandler.removeCallbacks(hideControlsRunnable);
        autoHideHandler.postDelayed(hideControlsRunnable, 2000);
    }
    private void setupPlayerListener() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_BUFFERING) {
                    viewBinding.btnReplay.setVisibility(View.GONE);
                    showLoadingVideo();
                } else if (state == Player.STATE_READY) {
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
        });
    }

    public void handleEndVideo() {
        updatePlayPauseIcons(false);
        viewModel.setPlaying(false);
        viewBinding.btnPlayPause.setVisibility(View.GONE);
        viewBinding.btnReplay.setVisibility(View.VISIBLE);
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

    private final Runnable hideControlsRunnable = () -> {
        viewBinding.controlVideo.setVisibility(View.GONE);
        viewBinding.layoutSeekBar.setVisibility(View.GONE);
    };
    private void toggleControls() {
        boolean show = viewBinding.controlVideo.getVisibility() != View.VISIBLE;
        viewBinding.controlVideo.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!isLockScreen) viewBinding.layoutSeekBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            autoHideHandler.removeCallbacks(hideControlsRunnable);
            autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
        }
    }
    private void updatePlayPauseIcons(boolean isPlaying) {
        viewBinding.icPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
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
    public void setUpView() {
        viewBinding.nameMovie.setText(Objects.requireNonNull(viewModel.getMovieDetails().getValue()).getTitle());
        viewBinding.nameMovieOriginal.setText(Objects.requireNonNull(viewModel.getMovieDetails().getValue()).getOriginalTitle());
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

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            getWindow().getInsetsController().hide(WindowInsets.Type.systemBars());
            getWindow().getInsetsController().setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        } else {
            // Android dưới R
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
                handleForwardAndPreviousVideo(true);
                break;
            case R.id.btn_previous:
                handleForwardAndPreviousVideo(false);
                break;
            default:
                break;
        }
    }

    public void handleLockScreen() {
        if (isLockScreen) {
            isLockScreen = false;
            viewBinding.lUnLockScreen.setVisibility(View.GONE);
            viewBinding.btnLockScreen.setVisibility(View.VISIBLE);
            viewBinding.buttonControlVideo.setVisibility(View.VISIBLE);
            viewBinding.layoutSeekBar.setVisibility(View.VISIBLE);
        } else {
            isLockScreen = true;
            viewBinding.lUnLockScreen.setVisibility(View.VISIBLE);
            viewBinding.btnLockScreen.setVisibility(View.GONE);
            viewBinding.buttonControlVideo.setVisibility(View.GONE);
            viewBinding.layoutSeekBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
        seekHandler.removeCallbacks(updateSeekBarRunnable);
        autoHideHandler.removeCallbacks(hideControlsRunnable);
    }
}
