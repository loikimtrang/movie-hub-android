package com.movie_hub.android.ui.main.home.filter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.request.movie.MovieRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.AgeRatingRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.CountryRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.LanguageRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.TypeMovieRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.YearReleaseRequest;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.ActivityHomeFilterBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.custom.GridSpacingItemDecoration;
import com.movie_hub.android.ui.main.home.filter.adapter.FilterAgeRatingAdapter;
import com.movie_hub.android.ui.main.home.filter.adapter.FilterCategoryItemAdapter;
import com.movie_hub.android.ui.main.home.filter.adapter.FilterCountryItemAdapter;
import com.movie_hub.android.ui.main.home.filter.adapter.FilterHomeAdapter;
import com.movie_hub.android.ui.main.home.filter.adapter.FilterLanguageItemAdapter;
import com.movie_hub.android.ui.main.home.filter.adapter.FilterTypeItemAdapter;
import com.movie_hub.android.ui.main.home.filter.adapter.FilterYearReleaseAdapter;
import com.movie_hub.android.ui.main.home.filter.fragment.FilterFragmentDialog;
import com.movie_hub.android.ui.main.home.filter.model.FilterItemModel;
import com.movie_hub.android.ui.main.home.filter.model.FilterTypeModel;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.ui.main.search.topTrending.FlexSpacingItemDecoration;
import com.movie_hub.android.ui.main.search.topTrending.adapter.MovieVerticalAdapter;
import com.movie_hub.android.ui.main.search.topTrending.shimmer.MovieVerticalShimmerAdapter;
import com.movie_hub.android.utils.ClickUtils;
import com.movie_hub.android.utils.FileUtils;
import com.movie_hub.android.utils.GridUtil;
import com.movie_hub.android.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends BaseActivity<ActivityHomeFilterBinding, FilterViewModel> implements
        SystemBarColorProvider,
        MovieVerticalAdapter.OnMovieClickListener,
        FilterFragmentDialog.FilterDialogCallback,
        FilterHomeAdapter.OnDeleteFilterClick {

    @Override
    public int getLayoutId() {
        return R.layout.activity_home_filter;
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
    boolean isLastPage = false;
    private boolean isLoading = false;
    private MovieVerticalAdapter movieVerticalAdapter;
    private MovieVerticalShimmerAdapter movieVerticalShimmerAdapter;
    FilterHomeAdapter filterHomeAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        setUpAdapter();
        showShimmer();

        String json = getIntent().getStringExtra("filter_type");
        FilterTypeModel filterTypeModel = GsonUtils.fromJson(json, FilterTypeModel.class);

        String jsonCate = getIntent().getStringExtra("cate_list");
        List<CategoryResponse> categoryResponses = GsonUtils.fromJsonToList(jsonCate, CategoryResponse.class);

        String jsonMovieRequest = getIntent().getStringExtra("movie_request");
        MovieRequest request = GsonUtils.fromJson(jsonMovieRequest, MovieRequest.class);

        if (filterTypeModel != null && categoryResponses != null && !categoryResponses.isEmpty()) {
            viewModel.filterType = filterTypeModel;
            viewModel.categoryResponseList = categoryResponses;

            if (viewModel.filterType.getType() == Constants.TYPE_MOVIE_SINGLE) {
                viewBinding.tvTitle.setText(getString(R.string.label_movie));
                viewModel.movieRequest.setType(Constants.TYPE_MOVIE_SINGLE);
                getListMovie(viewModel.movieRequest);
            } else if (viewModel.filterType.getType() == Constants.TYPE_MOVIE_SERIES) {
                viewBinding.tvTitle.setText(getString(R.string.label_series));
                viewModel.movieRequest.setType(Constants.TYPE_MOVIE_SERIES);
                getListMovie(viewModel.movieRequest);
            } else {
                if (request != null && request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
                    viewModel.movieRequest = request;
                }
                viewBinding.tvTitle.setText(getString(R.string.filter));
                getListMovie(viewModel.movieRequest);
            }

            generateData();
            handleDataFilter();
        }

        viewBinding.rvMovie.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) return;

                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm == null || movieVerticalAdapter == null) return;

                int totalItemCount = lm.getItemCount();
                int lastVisibleItemPosition = lm.findLastVisibleItemPosition();

                if (!isLoading && !isLastPage && lastVisibleItemPosition >= totalItemCount - 5) {
                    getListMovie(viewModel.movieRequest);
                }
            }
        });

        viewBinding.btnFilter.setOnClickListener(v -> {
            ClickUtils.debounceClick(viewBinding.btnFilter);
            showFilterDialog();
        });

        viewModel.listFilter.observe(this, filters -> {
            if (filters == null || filters.isEmpty() || filters.size() == 1) {
                viewBinding.rvFilter.setVisibility(View.GONE);
                return;
            }
            filterHomeAdapter.setData(filters);
            viewBinding.rvFilter.setVisibility(View.VISIBLE);
        });

    }

    public void resetForFilter() {
        viewModel.currentPage = 0;
        isLastPage = false;
        isLoading = false;
        viewModel.listMovieResponse.setValue(new ArrayList<>());
        showShimmer();
    }

    public void generateData() {
        viewModel.typeMovieRequestList = TypeMovieRequest.getAll(this);
        viewModel.ageRatingRequestList = AgeRatingRequest.getAll();
        viewModel.yearReleaseRequestList = YearReleaseRequest.generateYearReleaseList();

        viewModel.countryRequestList = GsonUtils.fromJsonToList(
                FileUtils.readRawResource(this, R.raw.country_options), CountryRequest.class);

        viewModel.languageRequestList = GsonUtils.fromJsonToList(
                FileUtils.readRawResource(this, R.raw.language_options), LanguageRequest.class);
    }
    public void handleDataFilter() {
        MovieRequest movieRequest = viewModel.movieRequest;

        // === Category ===
        List<Long> selectedCategoryIds = movieRequest.getCategoryIds() != null ? movieRequest.getCategoryIds() : new ArrayList<>();
        for (CategoryResponse cate : viewModel.categoryResponseList) {
            cate.setSelect(selectedCategoryIds.contains(cate.getId()));
        }

        // === Country ===
        CountryRequest selectedCountry = movieRequest.getCountryRequest();
        for (CountryRequest country : viewModel.countryRequestList) {
            country.setSelect(selectedCountry != null && selectedCountry.getValue().equals(country.getValue()));
        }

        // === Type Movie ===
        TypeMovieRequest selectedType = movieRequest.getTypeMovieRequest();
        for (TypeMovieRequest type : viewModel.typeMovieRequestList) {
            type.setSelect(selectedType != null && selectedType.getType() == type.getType());
        }

        // === Language ===
        LanguageRequest selectedLanguage = movieRequest.getLanguageRequest();
        for (LanguageRequest lang : viewModel.languageRequestList) {
            lang.setSelect(selectedLanguage != null && selectedLanguage.getValue().equals(lang.getValue()));
        }

        // === Age Rating ===
        AgeRatingRequest selectedAge = movieRequest.getAgeRatingRequest();
        for (AgeRatingRequest age : viewModel.ageRatingRequestList) {
            age.setSelect(selectedAge != null && selectedAge.getType() == age.getType());
        }

        // === Year Release ===
        Integer selectedYear = movieRequest.getReleaseYear();
        for (YearReleaseRequest year : viewModel.yearReleaseRequestList) {
            year.setSelect(selectedYear != null && selectedYear.equals(year.getReleaseYear()));
        }
    }

    public void showShimmer() {
        viewBinding.layoutEmpty.setVisibility(View.GONE);
        viewBinding.rvMovie.setAdapter(movieVerticalShimmerAdapter);
    }

    public void hideShimmer() {
        viewBinding.rvMovie.setAdapter(movieVerticalAdapter);
    }

    public void setUpAdapter() {
        movieVerticalShimmerAdapter = new MovieVerticalShimmerAdapter(6);
        movieVerticalAdapter = new MovieVerticalAdapter(this);

        int spacing = this.getResources().getDimensionPixelSize(R.dimen._8sdp);
        int spanCount = GridUtil.calculateSpanCount(this, 110);

        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        viewBinding.rvMovie.setLayoutManager(layoutManager);
        viewBinding.rvMovie.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing));

        int a = getResources().getDimensionPixelSize(R.dimen._6sdp);
        filterHomeAdapter = new FilterHomeAdapter(this, this);
        FlexboxLayoutManager layout = new FlexboxLayoutManager(this);
        layout.setFlexDirection(FlexDirection.ROW);
        layout.setFlexWrap(FlexWrap.WRAP);
        layout.setJustifyContent(JustifyContent.FLEX_START);
        layout.setAlignItems(AlignItems.FLEX_START);
        viewBinding.rvFilter.setLayoutManager(layout);
        viewBinding.rvFilter.addItemDecoration(new FlexSpacingItemDecoration(a));
        viewBinding.rvFilter.setAdapter(filterHomeAdapter);
    }

    public void showFilterDialog() {
        FilterFragmentDialog dialog = new FilterFragmentDialog(
                this,
                viewModel.movieRequest,
                GsonUtils.toJson(viewModel.categoryResponseList),
                GsonUtils.toJson(viewModel.countryRequestList),
                GsonUtils.toJson(viewModel.languageRequestList),
                GsonUtils.toJson(viewModel.typeMovieRequestList),
                GsonUtils.toJson(viewModel.ageRatingRequestList),
                GsonUtils.toJson(viewModel.yearReleaseRequestList),
                viewModel.filterType
        );
        dialog.show(getSupportFragmentManager(), "FilterFragmentDialog");
    }


    @Override
    public void onFilterClick(MovieRequest request) {
        resetForFilter();
        handleDataFilter();
        viewModel.movieRequest = request;
        getListMovie(viewModel.movieRequest);
    }

    public void handleListFilter() {
        List<FilterItemModel> newFilterList = new ArrayList<>();
        MovieRequest movieRequest = viewModel.movieRequest;

        if (movieRequest.getCountryRequest() != null) {
            newFilterList.add(new FilterItemModel(movieRequest.getCountryRequest().getLabel()));
        }

        if (viewModel.filterType.getType() == Constants.TYPE_GENRE) {
            if (movieRequest.getTypeMovieRequest() != null) {
                newFilterList.add(new FilterItemModel(movieRequest.getTypeMovieRequest().getLabel()));
            }
        }

        if (movieRequest.getCategoryRequest() != null && !movieRequest.getCategoryRequest().isEmpty()
                && movieRequest.getCategoryIds() != null && !movieRequest.getCategoryIds().isEmpty()) {
            for (CategoryResponse categoryResponse: movieRequest.getCategoryRequest()) {
                newFilterList.add(new FilterItemModel(categoryResponse.getName()));
            }
        }

        if (movieRequest.getYearReleaseRequest() != null) {
            newFilterList.add(new FilterItemModel(movieRequest.getYearReleaseRequest().getReleaseYear().toString()));
        }

        if (movieRequest.getAgeRatingRequest() != null) {
            newFilterList.add(new FilterItemModel(movieRequest.getAgeRatingRequest().getLabel()));
        }

        if (movieRequest.getLanguageRequest() != null) {
            newFilterList.add(new FilterItemModel(movieRequest.getLanguageRequest().getLabel()));
        }

        newFilterList.add(new FilterItemModel(getString(R.string.delete)));

        filterHomeAdapter.setData(new ArrayList<>());
        viewModel.listFilter.postValue(newFilterList);
    }

    public void getListMovie(MovieRequest movieRequest) {
        isLoading = true;

        if (viewModel.filterType.getType() != Constants.TYPE_GENRE) {
            movieRequest.setType(viewModel.filterType.getType());
        }
        MovieRequest request = new MovieRequest();
        if (movieRequest.getCategoryIds() != null && !movieRequest.getCategoryIds().isEmpty()) {
            request.setCategoryIds(movieRequest.getCategoryIds());
        }
        if (movieRequest.getType() != null) {
            request.setType(movieRequest.getType());
        }
        if (movieRequest.getAgeRating() != null) {
            request.setAgeRating(movieRequest.getAgeRating());
        }
        if (movieRequest.getCountry() != null) {
            request.setCountry(movieRequest.getCountry());
        }
        if (movieRequest.getLanguage() != null) {
            request.setLanguage(movieRequest.getLanguage());
        }
        if (movieRequest.getReleaseYear() != null) {
            request.setReleaseYear(movieRequest.getReleaseYear());
        }
        
        handleListFilter();

        viewModel.getListMovie(new MainCallback<ResponseListObj<MovieResponse>>() {
            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
                isLoading = false;
            }

            @Override
            public void doSuccess() {
                hideLoading();
                isLoading = false;
            }

            @Override
            public void doSuccess(ResponseListObj<MovieResponse> data) {
                hideLoading();
                if (data.getContent() != null && !data.getContent().isEmpty()) {
                    if (viewModel.currentPage == 0) {
                        viewBinding.layoutEmpty.setVisibility(View.GONE);
                        hideShimmer();
                        viewModel.listMovieResponse.setValue(new ArrayList<>(data.getContent()));

                        movieVerticalAdapter.setData(data.getContent());
                    } else {
                        List<MovieResponse> currentList = viewModel.listMovieResponse.getValue();
                        if (currentList == null) currentList = new ArrayList<>();

                        currentList.addAll(data.getContent());
                        viewModel.listMovieResponse.postValue(currentList);

                        movieVerticalAdapter.addData(data.getContent());
                    }

                    viewModel.currentPage = viewModel.currentPage + 1;

                    if (viewModel.currentPage >= data.getTotalPages()) {
                        isLastPage = true;
                    }
                } else {
                    if (viewModel.currentPage == 0) {
                        hideShimmer();
                        movieVerticalAdapter.setData(new ArrayList<>());
                        viewBinding.layoutEmpty.setVisibility(View.VISIBLE);
                    }
                    isLastPage = true;
                }
                isLoading = false;
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
                isLoading = false;
            }
        }, request);
    }

    @Override
    public void onMovieClick(MovieResponse movie) {
        getMovieDetail(movie);
    }

    public void getMovieDetail(MovieResponse movieResponse) {
        showLoading();
        viewModel.getMovie(new MainCallback<MovieResponse>() {

            @Override
            public void doSuccess(MovieResponse data) {
                if (viewModel.isLogin()) {
                    getListMovieTracking(data);
                } else {
                    navigateToMovieDetail(data, null);
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

    public void getListMovieTracking(MovieResponse movieResponse) {
        showLoading();
        viewModel.getListMovieTracking(new MainCallback<ListWatchHistoryResponse>() {

            @Override
            public void doSuccess(ListWatchHistoryResponse data) {
                navigateToMovieDetail(movieResponse, data);
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
    public void navigateToMovieDetail(MovieResponse movieResponse, ListWatchHistoryResponse listWatchHistoryResponse) {
        Intent it = new Intent(this, MovieDetailActivity.class);
        if (viewModel.isLogin()) {
            it.putExtra("movie_details", GsonUtils.toJson(movieResponse));
            it.putExtra("movie_details_tracking", GsonUtils.toJson(listWatchHistoryResponse));

        } else {
            it.putExtra("movie_details", GsonUtils.toJson(movieResponse));
        }
        startActivity(it);
    }

    @Override
    public void onDeleteClick() {
        viewModel.listFilter.postValue(new ArrayList<>());
        viewModel.movieRequest = new MovieRequest();
        if (viewModel.filterType.getType() != Constants.TYPE_GENRE) {
            viewModel.movieRequest.setType(viewModel.filterType.getType());
        }
        handleDataFilter();
    }
}
