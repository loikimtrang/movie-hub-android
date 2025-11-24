package com.movie_hub.android.ui.main.movie.detail;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.tabs.TabLayoutMediator;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;
import com.movie_hub.android.data.model.api.response.video.VideoResponse;
import com.movie_hub.android.databinding.ActivityMovieDetailBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.movie.detail.adapter.MovieDetailTabAdapter;
import com.movie_hub.android.ui.main.movie.detail.adapter.TagCategoryAdapter;
import com.movie_hub.android.ui.main.movie.detail.dialog.InformationMovieBottomSheetDialog;
import com.movie_hub.android.ui.main.movie.detail.fragment.CastFragment;
import com.movie_hub.android.ui.main.movie.detail.fragment.EpisodesFragment;
import com.movie_hub.android.ui.main.movie.detail.fragment.RecommendationFragment;
import com.movie_hub.android.ui.main.movie.watch.WatchMovieActivity;
import com.movie_hub.android.ui.main.search.topTrending.FlexSpacingItemDecoration;
import com.movie_hub.android.utils.ClickUtils;
import com.movie_hub.android.utils.DisplayUtils;
import com.movie_hub.android.utils.GsonUtils;
import com.movie_hub.android.utils.HtmlUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import eu.davidea.flexibleadapter.databinding.BR;

public class MovieDetailActivity extends BaseActivity<ActivityMovieDetailBinding, MovieDetailViewModel> implements SystemBarColorProvider, View.OnClickListener {
    private TagCategoryAdapter tagCategoryAdapter;
    private final List<Fragment> fragmentList = new ArrayList<>();
    private ExoPlayer player;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private Handler seekHandler = new Handler(Looper.getMainLooper());
    private Handler autoHideHandler = new Handler(Looper.getMainLooper());
    private boolean isUserSeeking = false;
    private Runnable updateSeekBarRunnable;
    private boolean isBuffering = false;
    private boolean isMuted = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        String json = getIntent().getStringExtra("movie_details");
        MovieResponse movie = GsonUtils.fromJson(json, MovieResponse.class);

        if (movie != null) {
            viewModel.movieDetails = movie;
            setUpView();
        }

    }


    @SuppressLint("SetTextI18n")
    private void setUpView() {
        if (viewModel.movieDetails == null) return;

        for (SeasonResponse s: viewModel.movieDetails.getSeasons()) {
            s.setSelect(false);
        }
        viewModel.movieDetails.getSeasons().get(viewModel.movieDetails.getSeasons().size() - 1).setSelect(true);

        Glide.with(this)
                .load(Constants.MEDIA_URL + viewModel.movieDetails.getThumbnailUrl())
                .placeholder(R.drawable.place_holder_16_9)
                .error(R.drawable.place_holder_16_9)
                .into(viewBinding.imgPoster);

        viewBinding.includeMovieHeader.nameMovie.setText(viewModel.movieDetails.getTitle());
        viewBinding.includeMovieHeader.nameMovieOriginal.setText(viewModel.movieDetails.getOriginalTitle());
        viewBinding.includeMovieHeader.description.setText(HtmlUtils.convertPtoStrong(viewModel.movieDetails.getDescription()));
        viewBinding.includeMovieHeader.ageRating.setText(DisplayUtils.displayAgeRating(viewModel.movieDetails.getAgeRating()));

        if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SINGLE) {
            viewBinding.includeMovieHeader.dateRelease.setText(DisplayUtils.getYearFromReleaseDate(viewModel.movieDetails.getReleaseDate()));
            viewBinding.includeMovieHeader.durationEpisodeSeason.setText(DisplayUtils.displayTimeFromSeconds(this, viewModel.movieDetails.getSeasons().get(0).getVideo().getDuration()));
        } if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES) {
            if (viewModel.movieDetails.getSeasons() != null && !viewModel.movieDetails.getSeasons().isEmpty()) {
                SeasonResponse lastSeason = viewModel.movieDetails.getSeasons().get(viewModel.movieDetails.getSeasons().size() - 1);
                viewBinding.includeMovieHeader.dateRelease.setText(DisplayUtils.getYearFromReleaseDate(lastSeason.getReleaseDate()));

                if (viewModel.movieDetails.getSeasons().size() == 1) {
                    viewBinding.includeMovieHeader.durationEpisodeSeason.setText(viewModel.movieDetails.getSeasons().get(0).getEpisodes().size()
                            + " " + getString(R.string.episode_non_up));
                } else {
                    viewBinding.includeMovieHeader.durationEpisodeSeason.setText(viewModel.movieDetails.getSeasons().size()
                            + " " + getString(R.string.season_non_up));
                }
            }
        }

        if (viewModel.movieDetails.getSeasons() != null && !viewModel.movieDetails.getSeasons().isEmpty()) {
            SeasonResponse lastSeason = viewModel.movieDetails.getSeasons().get(viewModel.movieDetails.getSeasons().size() - 1);

            if (lastSeason.getTrailer() != null) {
                VideoResponse video = lastSeason.getTrailer().getVideo();
                if (video != null && video.getContent() != null) {
                    setUpTrailer(video.getContent());
                }
            }
        }

        setUpTab(viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES);

        tagCategoryAdapter = new TagCategoryAdapter();
        FlexboxLayoutManager layout = new FlexboxLayoutManager(this);
        layout.setFlexDirection(FlexDirection.ROW);
        layout.setFlexWrap(FlexWrap.WRAP);
        layout.setJustifyContent(JustifyContent.FLEX_START);
        layout.setAlignItems(AlignItems.FLEX_START);

        viewBinding.includeMovieHeader.cateList.setLayoutManager(layout);
        int a = getResources().getDimensionPixelSize(R.dimen._6sdp);
        viewBinding.includeMovieHeader.cateList.addItemDecoration(new FlexSpacingItemDecoration(a));
        viewBinding.includeMovieHeader.cateList.setAdapter(tagCategoryAdapter);

        tagCategoryAdapter.setData(viewModel.movieDetails.getCategories());
    }

    public void setUpTab(boolean isSeries) {
        List<String> tabTitles = new ArrayList<>();
        fragmentList.clear();

        if (isSeries) {
            tabTitles.add(getString(R.string.episode));
            fragmentList.add(new EpisodesFragment());
        }

        tabTitles.add(getString(R.string.cast));
        fragmentList.add(CastFragment.newInstance(CastFragment.TYPE_MOVIE_DETAIL, null));

        tabTitles.add(getString(R.string.recommend));
        fragmentList.add(RecommendationFragment.newInstance(RecommendationFragment.TYPE_MOVIE_DETAIL, null, Objects.requireNonNull(viewModel.movieDetails.getId())));

        MovieDetailTabAdapter tabAdapter = new MovieDetailTabAdapter(this, fragmentList);
        viewBinding.subViewPager.setAdapter(tabAdapter);

        new TabLayoutMediator(viewBinding.subTabLayout, viewBinding.subViewPager,
                (tab, position) -> tab.setText(tabTitles.get(position))
        ).attach();

    }

    public void setUpTrailer(String uri) {
        if (uri == null || uri.isEmpty()) return;

        String token = viewModel.getTokenVideo();

        DefaultHttpDataSource.Factory httpFactory = new DefaultHttpDataSource.Factory()
                .setDefaultRequestProperties(
                        Collections.singletonMap("Authorization", token)
                );

        DefaultMediaSourceFactory mediaSourceFactory =
                new DefaultMediaSourceFactory(httpFactory);

        // tạo player có token
        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .build();

        viewBinding.trailer.setPlayer(player);

        viewModel.getIsPlaying().observe(this, this::updatePlayPauseIcons);

        if (!uri.contains("http")) uri = Constants.MEDIA_URL_VIDEO + uri;
        MediaItem mediaItem = MediaItem.fromUri(uri);

        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(true);
        player.setVolume(0f);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_BUFFERING) {
                    showLoadingVideo();
                } else if (state == Player.STATE_READY) {
                    hideLoadingVideo();
                    viewBinding.layoutPoster.setVisibility(View.GONE);
                    viewBinding.layoutReplay.setVisibility(View.GONE);
                    viewBinding.layoutSeekBar.setVisibility(View.VISIBLE);
                    viewBinding.btnMute.setVisibility(View.VISIBLE);

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

        viewBinding.seekBar.setThumb(null);
        viewBinding.btnMute.setOnClickListener(v -> toggleMute());
        updateMuteIcon();
        setupSeekBar();
        setupGestureDetector();
    }

    private void toggleMute() {
        isMuted = !isMuted;
        player.setVolume(isMuted ? 0f : 1f);
        updateMuteIcon();
    }

    private void updateMuteIcon() {
        if (isMuted) {
            viewBinding.icMute.setImageResource(R.drawable.ic_volume_mute);
        } else {
            viewBinding.icMute.setImageResource(R.drawable.ic_volume_high);
        }

    }
    public void setupSeekBar() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {

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
                autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
                viewBinding.controlVideo.setVisibility(View.VISIBLE);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupGestureDetector() {
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                toggleControls();
                return true;
            }
        });

        viewBinding.trailer.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    public void handleEndVideo() {
        updatePlayPauseIcons(false);
        viewModel.setPlaying(false);
        viewBinding.layoutPoster.setVisibility(View.VISIBLE);
        viewBinding.layoutReplay.setVisibility(View.VISIBLE);
        viewBinding.layoutSeekBar.setVisibility(View.GONE);
        viewBinding.btnMute.setVisibility(View.GONE);
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
    private void updatePlayPauseIcons(boolean isPlaying) {
        viewBinding.icPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private final Runnable hideControlsRunnable = () -> {
        viewBinding.controlVideo.setVisibility(View.GONE);
    };
    @SuppressLint("UseCompatLoadingForDrawables")
    private void toggleControls() {
        boolean show = viewBinding.controlVideo.getVisibility() != View.VISIBLE;
        viewBinding.controlVideo.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            autoHideHandler.removeCallbacks(hideControlsRunnable);
            autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
        }
    }

    private void showLoadingVideo() {
        if (!isBuffering && viewBinding.imgPoster.getVisibility() == View.GONE) {
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

    @Override
    public int getLayoutId() {
        return R.layout.activity_movie_detail;
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
    public int getStatusBarColor() {
        return R.color.account_header;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_app;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_close:
                this.finish();
                break;
            case R.id.watch_now:
                showLoading();
                Intent intent = new Intent(this, WatchMovieActivity.class);
                intent.putExtra("movie_details", GsonUtils.toJson(viewModel.movieDetails));
                if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES) {
                    intent.putExtra("episode", GsonUtils.toJson(viewModel.movieDetails.getSeasons().get(0).getEpisodes().get(0)));
                }
                startActivity(intent);
                break;
            case R.id.btn_replay:
                animateRotate(viewBinding.icReplay, false);
                break;
            case R.id.btn_play_pause:
                updatePlayPauseState(!player.isPlaying());
                break;
            case R.id.btn_movie_details:
                showMovieDetailsBottomSheet();
                break;
            default:
                break;
        }
    }

    public void showMovieDetailsBottomSheet() {
        ClickUtils.debounceClick(viewBinding.includeMovieHeader.btnMovieDetails);
        if (viewModel.movieDetails == null) return;

        InformationMovieBottomSheetDialog sheet =
                new InformationMovieBottomSheetDialog(
                        this,
                        viewModel.movieDetails
                );

        sheet.show();

        if (sheet.getWindow() != null) {
            sheet.getWindow().getDecorView().post(sheet::setupWindow);
        }
    }

    public void navigateToWatchMovieActivity(MovieItemResponse episode) {
        viewModel.showLoading();
        Intent intent = new Intent(this, WatchMovieActivity.class);
        intent.putExtra("movie_details", GsonUtils.toJson(viewModel.movieDetails));
        if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES) {
            intent.putExtra("episode", GsonUtils.toJson(episode));
        }
        startActivity(intent);
    }
    private void animateRotate(View view, boolean isForward) {
        float start = 0f;
        float end = isForward ? 360f : -360f;

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", start, end);
        animator.setDuration(400);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                player.seekTo(0);
                updatePlayPauseState(true);
            }
        });

        animator.start();
    }
}
