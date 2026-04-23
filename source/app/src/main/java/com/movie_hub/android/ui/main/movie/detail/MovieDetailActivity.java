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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
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
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.favourite.CreateFavouriteRequest;
import com.movie_hub.android.data.model.api.request.playlist.CreatePlaylistRequest;
import com.movie_hub.android.data.model.api.request.playlist.UpdatePlayListItemRequest;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.history.WatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.playlist.PlayListResponse;
import com.movie_hub.android.data.model.api.response.review.ReviewResponse;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;
import com.movie_hub.android.data.model.api.response.video.VideoResponse;
import com.movie_hub.android.data.model.onesignal.MessageOneSignal;
import com.movie_hub.android.data.model.onesignal.OneSignalCommand;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityMovieDetailBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.login.LoginActivity;
import com.movie_hub.android.ui.main.movie.detail.adapter.MovieDetailTabAdapter;
import com.movie_hub.android.ui.main.movie.detail.adapter.TagCategoryAdapter;
import com.movie_hub.android.ui.main.movie.detail.comment.CommentActivity;
import com.movie_hub.android.ui.main.movie.detail.dialog.AddToPlaylistDialogFragment;
import com.movie_hub.android.ui.main.movie.detail.dialog.InformationMovieBottomSheetDialog;
import com.movie_hub.android.ui.main.movie.detail.fragment.CastFragment;
import com.movie_hub.android.ui.main.movie.detail.fragment.EpisodesFragment;
import com.movie_hub.android.ui.main.movie.detail.fragment.RecommendationFragment;
import com.movie_hub.android.ui.main.movie.detail.review.ReviewActivity;
import com.movie_hub.android.ui.main.movie.watch.WatchMovieActivity;
import com.movie_hub.android.ui.main.search.topTrending.FlexSpacingItemDecoration;
import com.movie_hub.android.utils.ClickUtils;
import com.movie_hub.android.utils.DialogUtils;
import com.movie_hub.android.utils.DisplayUtils;
import com.movie_hub.android.utils.GsonUtils;
import com.movie_hub.android.utils.LiveDataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.davidea.flexibleadapter.databinding.BR;

public class MovieDetailActivity extends BaseActivity<ActivityMovieDetailBinding, MovieDetailViewModel> implements SystemBarColorProvider,
        View.OnClickListener,
        AddToPlaylistDialogFragment.AddToPlaylistPlaylistDialogCallback {
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
    public static String DATA_MSG = "DATA_MSG";
    private ActivityResultLauncher<Intent> loginLauncher;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        viewModel.startTokenAutoRefresh();
        String json = getIntent().getStringExtra("movie_details");

        MovieResponse movie = GsonUtils.fromJson(json, MovieResponse.class);
        if (movie != null) {
            viewModel.movieDetails = movie;
            if (viewModel.isLogin()) {
                CreateFavouriteRequest request = new CreateFavouriteRequest();
                request.setTargetId(viewModel.movieDetails.getId());
                request.setType(Constants.FAVOURITE_TYPE_MOVIE);
                viewModel.getFavourite(request);
            }

            setUpView();

            if (viewModel.isLogin()) {
                String jsonTracking = getIntent().getStringExtra("movie_details_tracking");
                ListWatchHistoryResponse listTracking = GsonUtils.fromJson(jsonTracking, ListWatchHistoryResponse.class);
                if (listTracking != null) {
                    viewModel.movieDetailsTracking.setValue(listTracking);
                }
            }
        }

        viewModel.movieDetailsTracking.observe(this, response -> {
            if (response == null || response.getWatchHistories() == null) return;
            if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SINGLE) {
                WatchHistoryResponse watchHistoryResponse = response.getWatchHistoryNoComplete();

                if (watchHistoryResponse == null) {
                    viewBinding.includeMovieHeader.layoutRemaining.setVisibility(View.GONE);
                    viewBinding.includeMovieHeader.tvWatch.setText(getString(R.string.watch_now));
                    return;
                }

                viewBinding.includeMovieHeader.tvWatch.setText(getString(R.string.continue_watching));
                viewBinding.includeMovieHeader.tvTitleRemaining.setVisibility(View.GONE);
                viewBinding.includeMovieHeader.layoutRemaining.setVisibility(View.VISIBLE);

                Long currentTime = watchHistoryResponse.getLastWatchSeconds();
                Long totalTime = viewModel.movieDetails.getSeasons().get(0).getVideo().getDuration();

                viewBinding.includeMovieHeader.tvRemaining.setText(DisplayUtils.getRemainingTimeText(this,
                        currentTime,
                        totalTime));

                viewBinding.includeMovieHeader.seekBarRemaining.setMax(totalTime.intValue());

                viewBinding.includeMovieHeader.seekBarRemaining.setProgress(currentTime.intValue());

            } else if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES) {
                WatchHistoryResponse watchHistoryResponse = response.getFirstWatchHistory();
                if (watchHistoryResponse == null) {
                    viewBinding.includeMovieHeader.layoutRemaining.setVisibility(View.GONE);
                    viewBinding.includeMovieHeader.tvWatch.setText(getString(R.string.watch_now));
                    viewModel.remainingEpisode = null;
                    return;
                }

                MovieItemResponse remainingEpisode = new MovieItemResponse();
                Long currentTime = 0L;


                if (!watchHistoryResponse.isCompleted()) {
                    remainingEpisode = viewModel.movieDetails.getEpisodeById(watchHistoryResponse.getMovieItemId());
                    currentTime = watchHistoryResponse.getLastWatchSeconds();
                } else {
                    if (!viewModel.movieDetails.isLastEpisode(watchHistoryResponse.getMovieItemId())) {
                        MovieItemResponse nextEpisode = viewModel.movieDetails.getNextEpisode(watchHistoryResponse.getMovieItemId());

                        WatchHistoryResponse watchHistoryNoComplete = response.getWatchHistoryByMovieId(nextEpisode.getId());

                        if (watchHistoryNoComplete == null) {
                            remainingEpisode = viewModel.movieDetails.getEpisodeById(nextEpisode.getId());
                            currentTime = 0L;
                        } else {
                            remainingEpisode = viewModel.movieDetails.getEpisodeById(watchHistoryNoComplete.getMovieItemId());
                            currentTime = watchHistoryNoComplete.getLastWatchSeconds();
                        }

                    } else {

                        WatchHistoryResponse watchHistoryNoComplete = response.getWatchHistoryNoComplete();

                        if (watchHistoryNoComplete == null) {
                            viewBinding.includeMovieHeader.layoutRemaining.setVisibility(View.GONE);
                            viewBinding.includeMovieHeader.tvWatch.setText(getString(R.string.watch_now));
                            viewModel.remainingEpisode = null;
                            return;
                        } else {
                            remainingEpisode = viewModel.movieDetails.getEpisodeById(watchHistoryNoComplete.getMovieItemId());
                            currentTime = watchHistoryNoComplete.getLastWatchSeconds();
                        }
                    }
                }

                String label = "";


                if (viewModel.movieDetails.getSeasons().size() > 1) {
                    label = getString(R.string.season_char) + remainingEpisode.getParent().getLabel() +
                            ":" + getString(R.string.episode_char) + remainingEpisode.getLabel();
                } else {
                    label = getString(R.string.episode_char) + remainingEpisode.getLabel();
                }

                viewBinding.includeMovieHeader.tvWatch.setText(getString(R.string.watch_next) + " " + label);

                viewBinding.includeMovieHeader.tvTitleRemaining.setText(label + ": " + remainingEpisode.getTitle());

                Long totalTime = remainingEpisode.getVideo().getDuration();

                viewBinding.includeMovieHeader.tvRemaining.setText(DisplayUtils.getRemainingTimeText(this,
                        currentTime,
                        totalTime));

                viewBinding.includeMovieHeader.seekBarRemaining.setMax(totalTime.intValue());
                viewBinding.includeMovieHeader.seekBarRemaining.setProgress(currentTime.intValue());

                viewBinding.includeMovieHeader.tvTitleRemaining.setVisibility(View.VISIBLE);
                viewBinding.includeMovieHeader.layoutRemaining.setVisibility(View.VISIBLE);

                viewModel.remainingEpisode = remainingEpisode;
            }

        });

        viewModel.favouriteResponseFirst.observe(this, response -> {
            if (response != null) {
                updateIconFavourite(true);
                viewModel.isFavourite = true;
            } else {
                viewModel.isFavourite = false;
                updateIconFavourite(false);
            }
        });

        loginLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        boolean loginSuccess = data != null && data.getBooleanExtra("login_success", false);
                        if (loginSuccess) {
                            new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.login_successful)).showMessage(this);
                            CreateFavouriteRequest request = new CreateFavouriteRequest();
                            request.setTargetId(viewModel.movieDetails.getId());
                            request.setType(Constants.FAVOURITE_TYPE_MOVIE);
                            viewModel.getFavourite(request);
                        }
                    }
                }
        );


        String jsonMsg = getIntent().getStringExtra(DATA_MSG);
        if (jsonMsg != null) {
            getIntent().removeExtra(DATA_MSG);
            MessageOneSignal messageOneSignal = GsonUtils.fromJson(jsonMsg, MessageOneSignal.class);
            if (messageOneSignal != null && messageOneSignal.getCmd() != null) {
                if (Objects.equals(messageOneSignal.getCmd(), OneSignalCommand.CMD_REPLY_COMMENT)) {
                    showLoading();
                    ClickUtils.debounceClick(viewBinding.includeMovieHeader.btnCmt);
                    Intent it = new Intent(this, CommentActivity.class);
                    it.putExtra("movie_details", GsonUtils.toJson(viewModel.movieDetails));
                    it.putExtra(CommentActivity.MSG_CMT, messageOneSignal.getData());
                    startActivity(it);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel.isLogin()) {
            viewModel.getListMovieTracking(viewModel.movieDetails.getId());
        }

        viewModel.startTokenAutoRefresh();

        if (viewModel.movieDetails != null && viewModel.movieDetails.getId() != null) {
            getMovieDetail(viewModel.movieDetails);
        }
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    private void setUpView() {
        if (viewModel.movieDetails == null) return;

        for (SeasonResponse s: viewModel.movieDetails.getSeasons()) {
            s.setSelect(false);
        }

        viewModel.movieDetails.getSeasons().get(viewModel.movieDetails.getSeasons().size() - 1).setSelect(true);

        Glide.with(this)
                .load(viewModel.movieDetails.getThumbnailUrl())
                .placeholder(R.drawable.place_holder_16_9)
                .error(R.drawable.place_holder_16_9)
                .into(viewBinding.imgPoster);

        viewBinding.includeMovieHeader.nameMovie.setText(viewModel.movieDetails.getTitle());
        viewBinding.includeMovieHeader.nameMovieOriginal.setText(viewModel.movieDetails.getOriginalTitle());
        viewBinding.includeMovieHeader.description.setText(viewModel.movieDetails.getDescription());
        viewBinding.includeMovieHeader.ageRating.setText(DisplayUtils.displayAgeRating(viewModel.movieDetails.getAgeRating()));
        viewBinding.includeMovieHeader.seekBarRemaining.setOnTouchListener((v, event) -> true);

        if (viewModel.movieDetails.getReviewCount() != null && viewModel.movieDetails.getReviewCount() > 0L) {
            viewBinding.includeMovieHeader.tvAvgRv.setText(viewModel.movieDetails.getAverageRating().toString());
            viewBinding.includeMovieHeader.layoutReview.setVisibility(View.VISIBLE);
        } else {
            viewBinding.includeMovieHeader.layoutReview.setVisibility(View.GONE);
        }

        if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SINGLE) {
            viewBinding.includeMovieHeader.dateRelease.setText(DisplayUtils.getYearFromReleaseDate(viewModel.movieDetails.getReleaseDate()));
            viewBinding.includeMovieHeader.durationEpisodeSeason.setText(DisplayUtils.displayTimeFromSeconds(this, viewModel.movieDetails.getSeasons().get(0).getVideo().getDuration()));
        } else if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES) {
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
                    if (Boolean.TRUE.equals(viewModel.getTokenReady().getValue())) {
                        setUpTrailer(video);
                    } else {
                        LiveDataUtils.observeOnce(viewModel.getTokenReady(), this, isReady -> {
                            if (Boolean.TRUE.equals(isReady)) {
                                setUpTrailer(video);
                            }
                        });
                    }
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
        fragmentList.add(CastFragment.newInstance(CastFragment.TYPE_MOVIE_DETAIL, null, false));

        tabTitles.add(getString(R.string.director_person));
        fragmentList.add(CastFragment.newInstance(CastFragment.TYPE_MOVIE_DETAIL, null, true));

        tabTitles.add(getString(R.string.recommend));
        fragmentList.add(RecommendationFragment.newInstance(RecommendationFragment.TYPE_MOVIE_DETAIL, null, Objects.requireNonNull(viewModel.movieDetails.getId())));

        MovieDetailTabAdapter tabAdapter = new MovieDetailTabAdapter(this, fragmentList);
        viewBinding.subViewPager.setAdapter(tabAdapter);

        new TabLayoutMediator(viewBinding.subTabLayout, viewBinding.subViewPager,
                (tab, position) -> tab.setText(tabTitles.get(position))
        ).attach();

    }

    public void setUpTrailer(VideoResponse videoResponse) {
        String uri = videoResponse.getContent();
        if (uri == null || uri.isEmpty()) return;

        DefaultMediaSourceFactory mediaSourceFactory =
                new DefaultMediaSourceFactory(
                        new TokenRefreshingDataSourceFactoryMovieDetails(viewModel)
                );

        // tạo player có token
        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .build();

        viewBinding.trailer.setPlayer(player);

        viewModel.getIsPlaying().observe(this, this::updatePlayPauseIcons);

        if (!uri.contains("http")) uri = videoResponse.getHostname() + Constants.MEDIA_URL_VIDEO + uri;
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
        return R.color.header_app;
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
        viewModel.stopTokenAutoRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
        viewModel.stopTokenAutoRefresh();
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
                intent.putExtra("movie_details_tracking", GsonUtils.toJson(viewModel.movieDetailsTracking.getValue()));
                if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES) {

                    if (viewModel.remainingEpisode == null) {
                        intent.putExtra("episode", GsonUtils.toJson(viewModel.movieDetails.getSeasons().get(0).getEpisodes().get(0)));
                    } else {
                        intent.putExtra("episode", GsonUtils.toJson(viewModel.remainingEpisode));
                    }

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
            case R.id.btn_favourite:
                ClickUtils.debounceClick(viewBinding.includeMovieHeader.btnFavourite);
                if (viewModel.isLogin()) {
                    if (viewModel.isFavourite) {
                        viewModel.deleteFavorite(viewModel.favourite.getId());
                    } else {
                        addFavoriteMovie();
                    }
                    updateIconFavourite(!viewModel.isFavourite);
                } else {
                    showLoginRequiredDialog();
                }
                break;
            case R.id.btn_cmt:
                showLoading();
                ClickUtils.debounceClick(viewBinding.includeMovieHeader.btnCmt);
                Intent it = new Intent(this, CommentActivity.class);
                it.putExtra("movie_details", GsonUtils.toJson(viewModel.movieDetails));
                startActivity(it);
                break;

            case R.id.btn_rating:
                if (viewModel.isLogin()) {
                    checkReview();
                } else {
                    navigateToReview(false);
                }
                break;

            case R.id.btn_playlist:
                if (!viewModel.isLogin()) {
                    showLoginRequiredDialog();
                    return;
                }
                getListPlayList();
                break;
            default:
                break;
        }
    }
    public void checkReview() {
        viewModel.checkIsReview(new MainCallback<ReviewResponse>() {
            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
            @Override
            public void doSuccess(ReviewResponse data) {
                if (data != null) {
                    navigateToReview(true);
                } else {
                    navigateToReview(false);
                }
            }
            @Override
            public void doSuccess() {

            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, viewModel.movieDetails.getId());
    }
    public void navigateToReview(Boolean isReview) {
        ClickUtils.debounceClick(viewBinding.includeMovieHeader.btnRating);
        Intent it = new Intent(this, ReviewActivity.class);
        it.putExtra("movie_details", GsonUtils.toJson(viewModel.movieDetails));
        it.putExtra("is_review", isReview);
        startActivity(it);
    }
    public void showLoginRequiredDialog() {
        DialogUtils.dialogConfirm(
                this,
                getString(R.string.not_login),
                getString(R.string.login),
                (dialog, which) -> {
                    Intent it = new Intent(this, LoginActivity.class);
                    it.putExtra("login_from_other", "login_from_other");
                    loginLauncher.launch(it);
                },
                getString(R.string.cancel),
                null
        );
    }
    public void updateIconFavourite(Boolean isFavorite) {
        viewModel.isFavourite = isFavorite;
        if (isFavorite) {
            viewBinding.includeMovieHeader.icFavourite.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_heart_select)
            );
            viewBinding.includeMovieHeader.tvFavourite.setTextColor(
                    ContextCompat.getColor(this, R.color.bg_select_icon)
            );
        } else {
            viewBinding.includeMovieHeader.icFavourite.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_heart)
            );
            viewBinding.includeMovieHeader.tvFavourite.setTextColor(
                    ContextCompat.getColor(this, R.color.text)
            );
        }
    }

    public void addFavoriteMovie() {
        CreateFavouriteRequest request = new CreateFavouriteRequest();
        request.setType(Constants.FAVOURITE_TYPE_MOVIE);
        request.setTargetId(viewModel.movieDetails.getId());

        viewModel.createFavorite(request);
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
        intent.putExtra("movie_details_tracking", GsonUtils.toJson(viewModel.movieDetailsTracking.getValue()));
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

    public CreatePlayListCallback createPlayListCallback;

    public void showBottomSheetAddToPlaylist(List<PlayListResponse> playListResponseList) {
        AddToPlaylistDialogFragment dialog = new AddToPlaylistDialogFragment(
                this,
                playListResponseList,
                viewModel.movieDetails.getId()
        );
        this.createPlayListCallback = dialog.getCreatePlayListCallback();
        dialog.show(getSupportFragmentManager(), "AddToPlaylistPlaylistDialog");

    }
    public void createNewPlayList(CreatePlaylistRequest request) {
        showLoading();
        viewModel.createPlaylist(new MainCallback<PlayListResponse>() {
            @Override
            public void doSuccess(PlayListResponse data) {
                hideLoading();
                createPlayListCallback.onCreateSuccessCallBack(data);
            }

            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {
                hideLoading();

            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, request);
    }

    public void getListPlayList() {
        showLoading();
        viewModel.getListPlaylist(new MainCallback<List<PlayListResponse>>() {
            @Override
            public void doSuccess(List<PlayListResponse> data) {
                hideLoading();
                if (data == null || data.isEmpty()) {
                    showBottomSheetAddToPlaylist(new ArrayList<>());
                    return;
                }
                if (!viewModel.playListResponseList.isEmpty()) {
                    viewModel.playListResponseList.clear();
                }
                viewModel.playListResponseList.addAll(data);
                getPlayListMovie();
            }
            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {
                hideLoading();
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        });
    }

    public void getPlayListMovie() {
        showLoading();
        viewModel.getPlayListOfMovie(new MainCallback<List<Long>>() {
            @Override
            public void doSuccess(List<Long> data) {
                hideLoading();

                if (viewModel.playListResponseList != null && data != null) {
                    for (PlayListResponse item : viewModel.playListResponseList) {
                        item.setSelect(data.contains(item.getId()));
                    }
                }

                showBottomSheetAddToPlaylist(viewModel.playListResponseList);
            }

            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {
                hideLoading();
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, viewModel.movieDetails.getId());
    }

    public void updatePlayListItem(UpdatePlayListItemRequest request) {
        showLoading();
        viewModel.updateItemPlayList(new MainCallback<ResponseWrapper>() {
            @Override
            public void doSuccess(ResponseWrapper data) {
                hideLoading();
                new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.update_list_success)).showMessage(getApplicationContext());
            }
            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {
                hideLoading();
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, request);
    }

    @Override
    public void onCreateClicked(CreatePlaylistRequest request) {
        createNewPlayList(request);
    }

    @Override
    public void onChooseClicked(UpdatePlayListItemRequest request) {
        if (request.getActions().isEmpty() || request.getActions() == null) return;
        updatePlayListItem(request);
    }


    public void getMovieDetail(MovieResponse movieResponse) {
        viewModel.getMovie(new MainCallback<MovieResponse>() {

            @SuppressLint("SetTextI18n")
            @Override
            public void doSuccess(MovieResponse data) {
                viewModel.movieDetails = data;
                if (viewModel.movieDetails.getReviewCount() != null && viewModel.movieDetails.getReviewCount() > 0L) {
                    viewBinding.includeMovieHeader.tvAvgRv.setText(viewModel.movieDetails.getAverageRating().toString());
                    viewBinding.includeMovieHeader.layoutReview.setVisibility(View.VISIBLE);
                } else {
                    viewBinding.includeMovieHeader.layoutReview.setVisibility(View.GONE);
                }
            }

            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {
                hideLoading();
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, movieResponse.getId());
    }
}
