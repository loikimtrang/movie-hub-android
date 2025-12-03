package com.movie_hub.android.ui.main.home.detail;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.ActivityHomeSideBarDetailBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.custom.GridSpacingItemDecoration;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.ui.main.search.topTrending.adapter.MovieVerticalAdapter;
import com.movie_hub.android.ui.main.search.topTrending.shimmer.MovieVerticalShimmerAdapter;
import com.movie_hub.android.utils.GridUtil;
import com.movie_hub.android.utils.GsonUtils;

import java.util.List;

import eu.davidea.flexibleadapter.databinding.BR;

public class HomeSideBarDetailActivity extends BaseActivity<ActivityHomeSideBarDetailBinding, HomeSideBarDetailViewModel>
        implements SystemBarColorProvider,
        MovieVerticalAdapter.OnMovieClickListener {

    @Override
    public int getStatusBarColor() {
        return R.color.header_app;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_app;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_home_side_bar_detail;
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    private MovieVerticalAdapter movieVerticalAdapter;
    private MovieVerticalShimmerAdapter movieVerticalShimmerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        setUpAdapter();
        showShimmer();

        String json = getIntent().getStringExtra("movie_list");
        List<MovieResponse> listMovie = GsonUtils.fromJsonToList(json, MovieResponse.class);

        if (listMovie != null && !listMovie.isEmpty()) {
            viewModel.listMovie = listMovie;
            movieVerticalAdapter.setData(viewModel.listMovie);
            hideShimmer();
        }
    }

    public void showShimmer() {
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
    protected void onResume() {
        super.onResume();
        hideLoading();
    }
}
