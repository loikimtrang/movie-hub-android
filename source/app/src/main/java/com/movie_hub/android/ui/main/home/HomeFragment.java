package com.movie_hub.android.ui.main.home;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.request.category.CategoryRequest;
import com.movie_hub.android.data.model.api.request.collection.CollectionRequest;
import com.movie_hub.android.data.model.api.request.movie.MovieRequest;
import com.movie_hub.android.data.model.api.request.side_bar.SideBarRequest;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.data.model.api.response.collection.CollectionResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.history.MovieHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.side_bar.SidebarResponse;
import com.movie_hub.android.databinding.FragmentHomeBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.home.adapter.CollectionAdapter;
import com.movie_hub.android.ui.main.home.adapter.CollectionType_Topic_Adapter;
import com.movie_hub.android.ui.main.home.adapter.MovieBannerAdapter;
import com.movie_hub.android.ui.main.home.adapter.MovieHistoryHomeAdapter;
import com.movie_hub.android.ui.main.home.dialog.MovieDetailDialogFragment;
import com.movie_hub.android.ui.main.home.filter.adapter.HomeTopFilterItemAdapter;
import com.movie_hub.android.ui.main.home.filter.fragment.FilterCategoryFragmentDialog;
import com.movie_hub.android.ui.main.home.filter.fragment.FilterFragmentDialog;
import com.movie_hub.android.ui.main.home.filter.model.FilterTypeModel;
import com.movie_hub.android.utils.DisplayUtils;
import com.movie_hub.android.utils.GsonUtils;
import com.movie_hub.android.utils.HtmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class HomeFragment extends BaseFragment<FragmentHomeBinding, HomeViewModel>
        implements SystemBarColorProvider,
        OnMovieClickCallback,
        CollectionAdapter.OnCollectionClickListener,
        CollectionType_Topic_Adapter.OnTopicClickCallback,
        HomeTopFilterItemAdapter.OnFilterTopClickListener,
        FilterCategoryFragmentDialog.FilterCategoryDialogCallback {

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
    @Override
    public int getStatusBarColor() {
        return R.color.header_app;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_tab_bar;
    }
    private MovieBannerAdapter movieBannerAdapter;
    private final Handler bannerHandler = new Handler();
    private Runnable bannerRunnable;
    private Long timeSwipeBanner = 5000L;
    private boolean isAutoSliding = false;
    private boolean isUserSwiping = false;
    public static final int NavigateToMovieDetails = 1;
    public static final int NavigateToWatchMovie = 2;

    // history
    private MovieHistoryHomeAdapter movieHistoryHomeAdapter;
    private HomeTopFilterItemAdapter homeTopFilterItemAdapter;
    // collection
    private CollectionAdapter collectionAdapter;
    private CollectionType_Topic_Adapter collectionTypeTopicAdapter;

    int currentPage = 0;
    int pageSize = 4;
    boolean isLastPage = false;
    private boolean isLoading = false;
    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);

        setUpView();

        observeMovieBanner();
        observeHistory();
        observeCollection();
        observeTopic();
        bindingClick();
    }

    public void setUpView() {
        showLayout();
        setUpAdapter();
        showShimmer();
        getListSideBar();

        if (viewModel.isLogin()) {
            getListMovieHistory();
        } else {
            binding.layoutHistory.setVisibility(View.GONE);
        }

        setUpBanner();
    }
    public void setUpAdapter() {
        movieHistoryHomeAdapter = new MovieHistoryHomeAdapter(this, getContext());
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvHistory.setAdapter(movieHistoryHomeAdapter);

        collectionAdapter = new CollectionAdapter(this, this, getContext());
        binding.rvCollection.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.rvCollection.setAdapter(collectionAdapter);

        collectionTypeTopicAdapter = new CollectionType_Topic_Adapter(this, getContext());
        binding.rvTopic.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvTopic.setAdapter(collectionTypeTopicAdapter);

        homeTopFilterItemAdapter = new HomeTopFilterItemAdapter(this, getContext());
        binding.rvFilter.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvFilter.setAdapter(homeTopFilterItemAdapter);
        homeTopFilterItemAdapter.setData(getFilterType());
    }

    public List<FilterTypeModel> getFilterType() {
        List<FilterTypeModel> filterTypeModels = new ArrayList<>();
        FilterTypeModel filterTypeModel1 = new FilterTypeModel(getString(R.string.recommend), -1, true);
        FilterTypeModel filterTypeModel2 = new FilterTypeModel(getString(R.string.label_movie), Constants.TYPE_MOVIE_SINGLE, false);
        FilterTypeModel filterTypeModel3 = new FilterTypeModel(getString(R.string.label_series), Constants.TYPE_MOVIE_SERIES, false);
        FilterTypeModel filterTypeModel4 = new FilterTypeModel(getString(R.string.label_genre), Constants.TYPE_GENRE, false);

        filterTypeModels.add(filterTypeModel1);
        filterTypeModels.add(filterTypeModel2);
        filterTypeModels.add(filterTypeModel3);
        filterTypeModels.add(filterTypeModel4);

        return filterTypeModels;
    }
    public void bindingClick() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::reloadDataHome);

        binding.btnBannerWatchNow.setOnClickListener(v -> {
            reloadSwipeBanner();
            getMovieDetail(viewModel.currentBannerMovie, NavigateToWatchMovie);
        });

        binding.btnBannerInf.setOnClickListener(v -> {
            reloadSwipeBanner();
            getMovieDetail(viewModel.currentBannerMovie, NavigateToMovieDetails);
        });

        binding.btnMoreHistory.setOnClickListener(( v-> {
            if (viewModel.isLogin()) {
                showLoading();
                ((MainActivity) requireActivity()).navigateToHistory();
            }
        }));

        binding.btnMoreTopic.setOnClickListener(v -> {
            showLoading();
            ((MainActivity) requireActivity()).navigateToMoreTopic();
        });

        binding.scrollMain.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            View child = binding.scrollMain.getChildAt(0);
            if (child != null) {
                int threshold = 1000;
                int distanceToBottom = child.getBottom() - (binding.scrollMain.getHeight() + scrollY);

                if (distanceToBottom < threshold && !isLoading && !isLastPage) {
                    getListCollection();
                }

                if (scrollY > oldScrollY + 5) {
                    hideRvFilter();
                }

                if (scrollY < 100) {
                    showRvFilter();
                }
            }
        });

        binding.btnNotification.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).navigateToNotification();
        });
    }

    private boolean isRvFilterVisible = true;

    private void hideRvFilter() {
        if (isRvFilterVisible) {
            isRvFilterVisible = false;

            binding.rvFilter.animate()
                    .translationY(-binding.rvFilter.getHeight())
                    .alpha(0f)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> binding.rvFilter.setVisibility(View.GONE))
                    .start();
        }
    }

    private void showRvFilter() {
        if (!isRvFilterVisible) {
            isRvFilterVisible = true;

            binding.rvFilter.setVisibility(View.VISIBLE);
            binding.rvFilter.setAlpha(0f);
            binding.rvFilter.setTranslationY(-binding.rvFilter.getHeight());

            binding.rvFilter.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }


    private void reloadDataHome() {
        showShimmer();
        // collection
        currentPage = 0;
        pageSize = 4;
        isLastPage = false;
        isLoading = false;
        viewModel.collectionList.setValue(new ArrayList<>());

        viewModel.currentBannerMovie = new MovieResponse();
        viewModel.movieHistory.setValue(new ArrayList<>());
        viewModel.movieBannerList.setValue(new ArrayList<>());
        viewModel.topicList.setValue(new ArrayList<>());

        getListSideBar();
        binding.swipeRefreshLayout.setRefreshing(false);

        if (viewModel.isLogin()) {
            getListMovieHistory();
        } else {
            viewModel.movieHistory.postValue(new ArrayList<>());
            binding.layoutHistory.setVisibility(View.GONE);
        }
    }

    public void showLayout() {
        binding.layoutBanner.setVisibility(View.VISIBLE);
        binding.layoutTopic.setVisibility(View.VISIBLE);
        binding.layoutCollection.setVisibility(View.VISIBLE);

        binding.tvTopic.setSelected(true);
        binding.rvFilter.setVisibility(View.VISIBLE);
    }
    public void showShimmer() {
        binding.shimmerBanner.shimmerLayout.setVisibility(View.VISIBLE);
    }

    public void hideShimmer() {
        binding.shimmerBanner.shimmerLayout.setVisibility(View.GONE);
        hideLoading();
    }

    public void observeMovieBanner() {
        viewModel.movieBannerList.observe(getViewLifecycleOwner(), bannerList -> {
            if (bannerList == null || bannerList.isEmpty()) return;
            movieBannerAdapter.setData(bannerList);
            reloadSwipeBanner();
            binding.viewPagerBanner.setCurrentItem(0);
            hideShimmer();
        });
    }

    public void observeHistory() {
        viewModel.movieHistory.observe(getViewLifecycleOwner(), histories -> {
            if (histories == null || histories.isEmpty()) return;

            List<MovieHistoryResponse> top5 = histories.size() > 5
                    ? histories.subList(0, 5)
                    : histories;

            movieHistoryHomeAdapter.setData(top5);
            binding.layoutHistory.setVisibility(View.VISIBLE);
        });
    }

    public void observeCollection() {
        viewModel.collectionList.observe(getViewLifecycleOwner(), collections -> {
            if (collections == null || collections.isEmpty()) return;
            collectionAdapter.setData(collections);
        });
    }

    public void observeTopic() {
        viewModel.topicList.observe(getViewLifecycleOwner(), collections -> {
            if (collections == null || collections.isEmpty()) return;
            collectionTypeTopicAdapter.setData(collections);
        });
    }

    public void reloadSwipeBanner() {
        stopAutoBannerSlide();
        startAutoBannerSlide();
    }

    public void setUpBanner() {
        movieBannerAdapter = new MovieBannerAdapter(getContext(), this);
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
                SidebarResponse currentMovie = movieBannerAdapter.getItem(position);
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
    public void updateBanner(SidebarResponse sidebarResponse) {
        MovieResponse item = sidebarResponse.getMovie();
        viewModel.currentBannerMovie = item;
        binding.tvName.setText(item.getTitle());
        binding.tvOtherName.setText(item.getOriginalTitle());
        binding.tvAgeRating.setText(DisplayUtils.displayAgeRating(item.getAgeRating()));
        binding.tvDateRelease.setText(DisplayUtils.getYearFromReleaseDate(item.getReleaseDate()));
        binding.tvDescription.setText(item.getDescription());

        Glide.with(getContext())
                .load(sidebarResponse.getMobileThumbnailUrl())
                .transform(new jp.wasabeef.glide.transformations.BlurTransformation(10, 3)) // radius=25, sampling=3
                .into(binding.bgBlur);
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

    public void getListSideBar() {
        showLoading();
        viewModel.getListSideBar(new MainCallback<ResponseListObj<SidebarResponse>>() {
            @Override
            public void doSuccess(ResponseListObj<SidebarResponse> data) {
                viewModel.movieBannerList.postValue(data.getContent());
                getListCollection();
                getListTopic();
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
        }, new SideBarRequest());
    }

    public void getListMovieHistory() {
        viewModel.getListMovieHistory(new MainCallback<List<MovieHistoryResponse>>() {
            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doSuccess(List<MovieHistoryResponse> data) {
                if (data != null && !data.isEmpty()) {
                    viewModel.movieHistory.postValue(data);
                } else {
                    binding.layoutHistory.setVisibility(View.GONE);
                }
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        });
    }

    public void getListCollection() {
        isLoading = true;
        CollectionRequest request = new CollectionRequest();
        request.setSize(pageSize);
        request.setPage(currentPage);

        viewModel.getListCollection(new MainCallback<ResponseListObj<CollectionResponse>>() {
            @Override
            public void doError(Throwable error) {
                hideLoading();
                isLoading = false;
                showError(getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {
                isLoading = false;
                hideLoading();

            }

            @Override
            public void doSuccess(ResponseListObj<CollectionResponse> data) {
                if (data.getContent() != null && !data.getContent().isEmpty()) {
                    List<CollectionResponse> filteredContent = new ArrayList<>();
                    for (CollectionResponse item : data.getContent()) {
                        if (item.getMovies() != null && !item.getMovies().isEmpty()) {
                            filteredContent.add(item);
                        }
                    }

                    List<CollectionResponse> newList = new ArrayList<>();
                    if (currentPage != 0) {
                        List<CollectionResponse> currentList = viewModel.collectionList.getValue();
                        if (currentList != null) newList.addAll(currentList);
                    }

                    newList.addAll(filteredContent);
                    viewModel.collectionList.setValue(newList);

                    currentPage++;
                    isLastPage = currentPage >= data.getTotalPages();
                } else {
                    isLastPage = true;
                }
                isLoading = false;
            }


            @Override
            public void doFail() {
                isLoading = false;
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, request);
    }

    public void getListTopic() {
        CollectionRequest request = new CollectionRequest();
        request.setSize(10);
        request.setPage(0);

        viewModel.getListTopic(new MainCallback<ResponseListObj<CollectionResponse>>() {
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
            public void doSuccess(ResponseListObj<CollectionResponse> data) {
                if (data.getContent() != null && !data.getContent().isEmpty()) {
                    viewModel.topicList.setValue(new ArrayList<>(data.getContent()));
                } else {
                    binding.layoutTopic.setVisibility(View.GONE);
                }
            }


            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, request);
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

        if (viewModel.isLogin()) {
            getListMovieHistory();
        } else {
            viewModel.movieHistory.postValue(new ArrayList<>());
            binding.layoutHistory.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAutoBannerSlide();
    }

    @Override
    public void onMovieClick(MovieResponse movieResponse) {
        if (movieResponse != null) {
            getMovieDetail(movieResponse, NavigateToMovieDetails);
        }
    }

    @Override
    public void onWatchMovieClick(MovieResponse movieResponse) {
        if (movieResponse != null) {
            getMovieDetail(movieResponse, NavigateToWatchMovie);
        }
    }

    @Override
    public void onMovieLongClick(MovieResponse movieResponse) {
        showDialogMovieDetail(movieResponse);
    }

    @Override
    public void onMoreClick(CollectionResponse collectionResponse) {
        ((MainActivity) requireActivity()).navigateToHomeSideBarDetail(collectionResponse);
    }

    @Override
    public void onTopicClick(CollectionResponse collectionResponse) {
        showLoading();
        ((MainActivity) requireActivity()).navigateToTopicDetail(collectionResponse);
    }

    public void getListCategory(FilterTypeModel filterTypeModel) {
        showLoading();
        viewModel.getListCategory(new MainCallback<List<CategoryResponse>>() {
            @Override
            public void doError(Throwable error) {
                showError(getString(R.string.an_error_occurred));
                hideLoading();
            }
            @Override
            public void doSuccess(List<CategoryResponse> data) {
                viewModel.categoryResponses = data;
                if (filterTypeModel.getType() == Constants.TYPE_GENRE) {
                    showFilterCategoryDialog(viewModel.categoryResponses);
                } else {
                    ((MainActivity) requireActivity()).navigateToFilter(filterTypeModel, data, new MovieRequest());
                }
            }
            @Override
            public void doSuccess() {
                hideLoading();
            }

            @Override
            public void doFail() {
                showError(getString(R.string.an_error_occurred));
                hideLoading();
            }
        }, new CategoryRequest());
    }
    @Override
    public void onFilterClick(FilterTypeModel filterTypeModel) {
        showLoading();
        viewModel.filterTypeModel = filterTypeModel;
        getListCategory(viewModel.filterTypeModel);
    }

    public void showFilterCategoryDialog(List<CategoryResponse> categoryResponseList) {
        FilterCategoryFragmentDialog dialog = new FilterCategoryFragmentDialog(
                this,
                categoryResponseList

        );
        dialog.show(getChildFragmentManager(), "FilterFragmentDialog");
    }

    @Override
    public void onFilterCategoryClick(MovieRequest request) {
        ((MainActivity) requireActivity()).navigateToFilter(viewModel.filterTypeModel, viewModel.categoryResponses, request);
    }

    @Override
    public void onDialogDismiss() {
        hideLoading();
    }

    public void showDialogMovieDetail(MovieResponse movieResponse) {
        ((MainActivity) requireActivity()).showMovieDialogDetail(movieResponse);
    }
}
