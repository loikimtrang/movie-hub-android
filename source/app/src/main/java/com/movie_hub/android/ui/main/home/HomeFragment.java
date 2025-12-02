package com.movie_hub.android.ui.main.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.FragmentHomeBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.home.adapter.MovieBannerAdapter;
import com.movie_hub.android.utils.DisplayUtils;
import com.movie_hub.android.utils.GsonUtils;
import com.movie_hub.android.utils.HtmlUtils;

import java.util.List;


public class HomeFragment extends BaseFragment<FragmentHomeBinding, HomeViewModel> implements SystemBarColorProvider {


    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    private MovieBannerAdapter movieBannerAdapter;
    private final Handler bannerHandler = new Handler();
    private Runnable bannerRunnable;
    private Long timeSwipeBanner = 5000L;
    private boolean isAutoSliding = false;
    private boolean isUserSwiping = false;
    public static final int NavigateToMovieDetails = 1;
    public static final int NavigateToWatchMovie = 2;

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            loadPlaylistData();
        });

        Bundle args = getArguments();
        if (args != null) {
            String bannerJson = args.getString("banner_json", null);
            if (bannerJson != null) {
                List<MovieResponse> bannerList = GsonUtils.fromJsonToList(bannerJson, MovieResponse.class);
                if (bannerList != null) {
                    viewModel.movieBannerList.postValue(bannerList);
                } else {
                    Log.e("HomeFragment", "Banner list is null after parsing!");
                }
            }
        }

        setUpBanner();
        observeMovieBanner();
        bindingClick();
    }
    public void reloadSwipeBanner() {
        stopAutoBannerSlide();
        startAutoBannerSlide();
    }
    public void bindingClick() {
        binding.btnBannerWatchNow.setOnClickListener(v -> {
            reloadSwipeBanner();
            getMovieDetail(viewModel.currentBannerMovie, NavigateToWatchMovie);
        });

        binding.btnBannerInf.setOnClickListener(v -> {
            reloadSwipeBanner();
            getMovieDetail(viewModel.currentBannerMovie, NavigateToMovieDetails);
        });
    }

    public void observeMovieBanner() {
        viewModel.movieBannerList.observe(getViewLifecycleOwner(), bannerList -> {
            if (bannerList == null || bannerList.isEmpty()) return;
            movieBannerAdapter.setData(bannerList);
            startAutoBannerSlide();
        });
    }

    public void setUpBanner() {
        movieBannerAdapter = new MovieBannerAdapter(getContext());
        binding.viewPagerBanner.setAdapter(movieBannerAdapter);
        binding.dotsIndicatorBanner.attachTo(binding.viewPagerBanner);

        binding.viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

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
                MovieResponse currentMovie = movieBannerAdapter.getItem(position);
                if (currentMovie != null) {
                    updateBanner(currentMovie);
                }
            }
        });

        binding.viewPagerBanner.setOffscreenPageLimit(3);

//        CompositePageTransformer transformer = new CompositePageTransformer();
//        transformer.addTransformer(new MarginPageTransformer(20)); // khoảng cách giữa các item
//        transformer.addTransformer((page, position) -> {
//            float absPos = Math.abs(position);
//            float scale = 1 - absPos * 0.25f;
//            page.setScaleX(scale);
//            page.setScaleY(scale);
//            page.setAlpha(1 - absPos * 0.3f);
//        });
//        binding.viewPagerBanner.setPageTransformer(transformer);

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

        binding.viewPagerBanner.setPageTransformer(transformer);
    }

    private void startAutoBannerSlide() {
        if (isAutoSliding || movieBannerAdapter == null || movieBannerAdapter.getItemCount() <= 1)
            return;

        bannerRunnable = () -> {
            int itemCount = movieBannerAdapter.getItemCount();
            int nextItem = (binding.viewPagerBanner.getCurrentItem() + 1) % itemCount;
            binding.viewPagerBanner.setCurrentItem(nextItem, true);
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


    public void updateBanner(MovieResponse item) {
        viewModel.currentBannerMovie = item;
        binding.tvName.setText(item.getTitle());
        binding.tvOtherName.setText(item.getOriginalTitle());
        binding.tvAgeRating.setText(DisplayUtils.displayAgeRating(item.getAgeRating()));
        binding.tvDateRelease.setText(DisplayUtils.getYearFromReleaseDate(item.getReleaseDate()));
        binding.tvDescription.setText(HtmlUtils.convertPtoStrong(item.getDescription()));

        Glide.with(getContext())
                .load(item.getPosterUrl())
                .transform(new jp.wasabeef.glide.transformations.BlurTransformation(10, 3)) // radius=25, sampling=3
                .into(binding.bgBlur);
    }

    private void loadPlaylistData() {
        // Ví dụ gọi lại adapter setData
       new ToastMessage(ToastMessage.TYPE_NORMAL, "ReLoad").showMessage(getContext());

        // Tắt refresh icon
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    public void getMovieDetail(MovieResponse movieResponse, int typeNavigate) {
        showLoading();
        viewModel.getMovie(new MainCallback<MovieResponse>() {

            @Override
            public void doSuccess(MovieResponse data) {
                if (viewModel.isLogin()) {
                    getListMovieTracking(data, typeNavigate);
                } else {
                    if (typeNavigate == NavigateToMovieDetails) {
                        navigateToMovieDetails(data, null);
                    } else if (typeNavigate == NavigateToWatchMovie) {
                        navigateToWatchMovie(data, null);
                    }
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

    public void getListMovieTracking(MovieResponse movieResponse, int typeNavigate) {
        showLoading();
        viewModel.getListMovieTracking(new MainCallback<ListWatchHistoryResponse>() {

            @Override
            public void doSuccess(ListWatchHistoryResponse data) {
                if (typeNavigate == NavigateToMovieDetails) {
                    navigateToMovieDetails(movieResponse, data);
                } else if (typeNavigate == NavigateToWatchMovie) {
                    navigateToWatchMovie(movieResponse, data);
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

    public void navigateToMovieDetails(MovieResponse movieResponse, ListWatchHistoryResponse listWatchHistoryResponse) {
        ((MainActivity) requireActivity()).navigateToMovieDetail(movieResponse, listWatchHistoryResponse);
    }

    public void navigateToWatchMovie(MovieResponse movieResponse, ListWatchHistoryResponse listWatchHistoryResponse) {
        ((MainActivity) requireActivity()).navigateToWatchMovie(movieResponse, listWatchHistoryResponse);
    }

    public void showLoading() {
        ((MainActivity) requireActivity()).showLoading();
    }

    public void hideLoading() {
        ((MainActivity) requireActivity()).hideLoading();
    }

    @Override
    public void onDestroyView() {
        bannerHandler.removeCallbacks(bannerRunnable);
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoBannerSlide();
    }

    @Override
    public void onResume() {
        super.onResume();
        startAutoBannerSlide();
        hideLoading();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAutoBannerSlide();
    }

    @Override
    public int getStatusBarColor() {
        return R.color.bg_tab_bar;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_app;
    }
}
