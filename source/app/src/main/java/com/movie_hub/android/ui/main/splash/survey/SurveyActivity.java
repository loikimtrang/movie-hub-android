package com.movie_hub.android.ui.main.splash.survey;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.movie.MovieRequest;
import com.movie_hub.android.data.model.api.request.side_bar.SideBarRequest;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.side_bar.SidebarResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivitySurveyBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.custom.GridSpacingItemDecoration;
import com.movie_hub.android.ui.main.home.OnMovieClickCallback;
import com.movie_hub.android.ui.main.home.adapter.MovieBannerAdapter;
import com.movie_hub.android.ui.main.search.topTrending.FlexSpacingItemDecoration;
import com.movie_hub.android.ui.main.search.topTrending.adapter.MovieVerticalAdapter;
import com.movie_hub.android.ui.main.search.topTrending.adapter.SearchHistoryAdapter;
import com.movie_hub.android.ui.main.search.topTrending.shimmer.MovieVerticalShimmerAdapter;
import com.movie_hub.android.ui.main.splash.SplashActivity;
import com.movie_hub.android.ui.main.splash.survey.adapter.MovieSurveyAdapter;
import com.movie_hub.android.utils.DisplayUtils;
import com.movie_hub.android.utils.GridUtil;
import com.movie_hub.android.utils.GsonUtils;
import com.movie_hub.android.utils.HtmlUtils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

public class SurveyActivity extends BaseActivity<ActivitySurveyBinding, SurveyViewModel> implements SystemBarColorProvider, MovieSurveyAdapter.OnMovieClickListener, OnMovieClickCallback {
    @Override
    public int getLayoutId() {
        return R.layout.activity_survey;
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    private MovieSurveyAdapter movieAdapter;
    private MovieVerticalShimmerAdapter shimmerAdapter;

    private MovieBannerAdapter movieBannerAdapter;
    private final Handler bannerHandler = new Handler();
    private Runnable bannerRunnable;
    private Long timeSwipeBanner = 5000L;
    private boolean isAutoSliding = false;
    private boolean isUserSwiping = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        viewBinding.btnDone.setEnabled(false);
        startWelcomeAnimation();
        viewBinding.btnContinue.setOnClickListener(v -> onClickContinue());

        if (getIntent() != null && getIntent().hasExtra("home_banner")) {
            String json = getIntent().getStringExtra("home_banner");

            observeMovieBanner();
            setUpBanner();
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<SidebarResponse>>(){}.getType();
            viewModel.movieBannerList.postValue(GsonUtils.getGson().fromJson(json, type));
        }

        viewBinding.btnDone.setOnClickListener(v -> {
            handleFinishSurvey();
        });

        viewBinding.btnLogOut.setOnClickListener(v -> {
            userSignOut();
        });
    }
    public void userSignOut() {
        viewModel.showLoading();
        viewModel.userSignOut(new MainCallback<Void>() {
            @Override
            public void doSuccess(Void unused) {
                startActivity(new Intent(getApplicationContext(), SplashActivity.class));
                new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.sign_out_success)).showMessage(getApplicationContext());
                finish();
            }

            @Override
            public void doError(Throwable throwable) {
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred)).showMessage(getApplicationContext());
            }

            @Override
            public void doFail() {
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred)).showMessage(getApplicationContext());
            }

            @Override
            public void doSuccess() {
                startActivity(new Intent(getApplicationContext(), SplashActivity.class));
                finish();
            }
        });
    }
    public void showShimmerLoading() {
        shimmerAdapter = new MovieVerticalShimmerAdapter(6);

        int spacing = this.getResources().getDimensionPixelSize(R.dimen._8sdp);
        int spanCount = GridUtil.calculateSpanCount(this, 110);

        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        viewBinding.rvSurvey.setLayoutManager(layoutManager);
        viewBinding.rvSurvey.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing));
        viewBinding.rvSurvey.setAdapter(shimmerAdapter);
    }

    public void hideShimmerAndShowData(List<MovieResponse> data) {
        movieAdapter = new MovieSurveyAdapter(this);
        int spacing = this.getResources().getDimensionPixelSize(R.dimen._8sdp);
        int spanCount = GridUtil.calculateSpanCount(this, 110);

        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        viewBinding.rvSurvey.setLayoutManager(layoutManager);
        viewBinding.rvSurvey.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing));
        viewBinding.rvSurvey.setAdapter(movieAdapter);
        movieAdapter.setData(data);
    }
    private void startWelcomeAnimation() {
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        viewBinding.layoutLogo.startAnimation(slideUp);

        String welcomeMsg = getString(R.string.welcome);
        viewBinding.tvWelcome.setText("");

        final long delayBetweenChars = 50;

        for (int i = 0; i <= welcomeMsg.length(); i++) {
            final int finalI = i;
            viewBinding.tvWelcome.postDelayed(() -> {
                String textToShow = welcomeMsg.substring(0, finalI);
                viewBinding.tvWelcome.setText(textToShow);

                viewBinding.tvWelcome.setAlpha(0.7f);
                viewBinding.tvWelcome.animate().alpha(1.0f).setDuration(delayBetweenChars).start();

            }, i * delayBetweenChars);
        }
    }

    public void onClickContinue() {
        Animation slideOutTop = AnimationUtils.loadAnimation(this, R.anim.slide_out_top);
        Animation slideUpIn = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        slideOutTop.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                viewBinding.layoutWelcome.setVisibility(android.view.View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        viewBinding.layoutWelcome.startAnimation(slideOutTop);

        viewBinding.layoutSurvey.setVisibility(android.view.View.VISIBLE);
        viewBinding.layoutSurvey.startAnimation(slideUpIn);

        showShimmerLoading();
        getListMovieSurvey();
    }

    public void getListMovieSurvey() {
        viewModel.getListMovieSurvey(new MainCallback<List<MovieResponse>>() {
            @Override
            public void doError(Throwable throwable) {
                if (throwable instanceof UnknownHostException || throwable instanceof SocketTimeoutException) {
                    showError(getString(R.string.network_error_please_check_your_internet_connection));
                } else if (throwable instanceof ConnectException) {
                    showError(getString(R.string.cannot_connect_to_the_server_please_try_again));
                } else {
                    showError(getString(R.string.fetch_data_failed));
                }
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doSuccess(List<MovieResponse> list) {
                hideLoading();
                hideShimmerAndShowData(list);
            }

            @Override
            public void doSuccess() {
            }
        });
    }

    @Override
    public void onMovieClick(MovieResponse movie) {
        List<Long> selectedIds = viewModel.makeSurveyRequest.getMovieIds();

        if (movie.isSelect()) {
            if (!selectedIds.contains(movie.getId())) {
                selectedIds.add(movie.getId());
            }
        } else {
            selectedIds.remove(movie.getId());
        }
        viewBinding.btnDone.setEnabled(selectedIds.size() >= 3);
        viewBinding.count.setText(String.valueOf(selectedIds.size()));
    }

    @Override
    public void onWatchMovieClick(MovieResponse movieResponse) {
        
    }

    @Override
    public void onMovieLongClick(MovieResponse movieResponse) {

    }

    private void handleFinishSurvey() {
        if (viewModel.makeSurveyRequest.getMovieIds().isEmpty()) {
            showError(getString(R.string.error_select_at_least_3));
            return;
        }

        viewModel.makeSurvey(new MainCallback<ResponseWrapper>() {
            @Override
            public void doSuccess(ResponseWrapper responseWrapper) {
                navigateToMainActivity();
            }

            @Override
            public void doSuccess() {
            }

            @Override
            public void doError(Throwable throwable) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));

            }
        }, viewModel.makeSurveyRequest);
    }

    public void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
    public void observeMovieBanner() {
        viewModel.movieBannerList.observe(this, bannerList -> {
            if (bannerList == null || bannerList.isEmpty()) return;
            movieBannerAdapter.setData(bannerList);
            reloadSwipeBanner();
            viewBinding.viewPagerBanner.setCurrentItem(0);
        });
    }

    public void reloadSwipeBanner() {
        stopAutoBannerSlide();
        startAutoBannerSlide();
    }

    private void startAutoBannerSlide() {
        if (isAutoSliding || movieBannerAdapter == null || movieBannerAdapter.getItemCount() <= 1)
            return;

        bannerRunnable = () -> {
            int itemCount = movieBannerAdapter.getItemCount();
            int nextItem = (viewBinding.viewPagerBanner.getCurrentItem() + 1) % itemCount;
            viewBinding.viewPagerBanner.setCurrentItem(nextItem, true);
            bannerHandler.postDelayed(bannerRunnable, timeSwipeBanner);
        };

        bannerHandler.postDelayed(bannerRunnable, timeSwipeBanner);
        isAutoSliding = true;
    }
    private void stopAutoBannerSlide() {
        if (isAutoSliding) {
            bannerHandler.removeCallbacks(bannerRunnable);
            isAutoSliding = false;
        }
    }
    public void setUpBanner() {
        movieBannerAdapter = new MovieBannerAdapter(this, this);
        viewBinding.viewPagerBanner.setAdapter(movieBannerAdapter);
        viewBinding.dotsIndicatorBanner.attachTo(viewBinding.viewPagerBanner);

        viewBinding.viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);

                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    isUserSwiping = true;
                    stopAutoBannerSlide();
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    if (isUserSwiping) {
                        isUserSwiping = false;
                        startAutoBannerSlide();
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
                SidebarResponse currentMovie = movieBannerAdapter.getItem(position);
                if (currentMovie != null) {
                    updateBanner(currentMovie);
                }
            }
        });

        viewBinding.viewPagerBanner.setOffscreenPageLimit(3);
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));

        transformer.addTransformer((page, position) -> {
            float absPos = Math.abs(position);
            float scale = 1 - absPos * 0.25f;
            page.setScaleX(scale);
            page.setScaleY(scale);
            page.setAlpha(1 - absPos * 0.3f);
            page.setRotationY(position * -25f);
            page.setTranslationX(position * -page.getWidth() * 0.15f);
            page.setTranslationZ(-absPos);
        });

        viewBinding.viewPagerBanner.setPageTransformer(transformer);
    }

    public void updateBanner(SidebarResponse sidebarResponse) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        MovieResponse item = sidebarResponse.getMovie();
        viewBinding.tvName.setText(item.getTitle());

        Glide.with(this)
                .load(sidebarResponse.getMobileThumbnailUrl())
                .transform(new jp.wasabeef.glide.transformations.BlurTransformation(10, 3)) // radius=25, sampling=3
                .into(viewBinding.bgBlur);
    }

    @Override
    public int getStatusBarColor() {
        return R.color.bg_app;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_app;
    }

    @Override
    protected void onDestroy() {
        stopAutoBannerSlide();
        bannerHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
