package com.movie_hub.android.ui.main.account.history;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.favourite.FavouriteResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.history.MovieHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.ActivityHistoryBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.favourite.adapter.MovieFavouriteAdapter;
import com.movie_hub.android.ui.main.account.favourite.shimmer.MovieFavoriteShimmerAdapter;
import com.movie_hub.android.ui.main.account.history.adapter.MovieHistoryAdapter;
import com.movie_hub.android.ui.main.account.history.shimmer.MovieHistoryShimmerAdapter;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.utils.GsonUtils;

import java.util.List;

public class HistoryActivity extends BaseActivity<ActivityHistoryBinding, HistoryViewModel> implements SystemBarColorProvider, MovieHistoryAdapter.OnMovieClickListener {

    private MovieHistoryAdapter movieHistoryAdapter;
    private MovieHistoryShimmerAdapter shimmerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        showShimmerLoading();
        getListMovieHistory();

        viewModel.movieHistory.observe(this, response -> {
            if (response != null) {
                viewBinding.layoutEmpty.setVisibility(View.GONE);
                hideShimmerAndShowData(response);
            }
        });
    }
    public void showShimmerLoading() {
        shimmerAdapter = new MovieHistoryShimmerAdapter(6);
        viewBinding.rvHistory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        viewBinding.rvHistory.setAdapter(shimmerAdapter);
    }

    public void hideShimmerAndShowData(List<MovieHistoryResponse> data) {
        movieHistoryAdapter = new MovieHistoryAdapter(this, this);
        viewBinding.rvHistory.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        viewBinding.rvHistory.setAdapter(movieHistoryAdapter);
        movieHistoryAdapter.setData(data);
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
                    viewBinding.layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_history;
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
    public void onMovieClick(MovieHistoryResponse movieHistoryResponse) {
        getMovie(movieHistoryResponse.getMovie());
    }

    public void getMovie(MovieResponse movie) {
        showLoading();

        viewModel.getMovie(new MainCallback<MovieResponse>() {
            @Override
            public void doError(Throwable throwable) {
                hideLoading();
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doSuccess(MovieResponse movieResponse) {
                hideLoading();
                if (viewModel.isLogin()) {
                    getListMovieTracking(movieResponse);
                } else {
                    navigateToMovieDetail(movieResponse, null);
                }
            }

            @Override
            public void doSuccess() {
            }
        }, movie.getId());
    }

    public void getListMovieTracking(MovieResponse movie) {
        showLoading();
        viewModel.getListMovieTracking(new MainCallback<ListWatchHistoryResponse>() {
            @Override
            public void doError(Throwable throwable) {
                hideLoading();
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doSuccess(ListWatchHistoryResponse list) {
                hideLoading();
                navigateToMovieDetail(movie, list);
            }

            @Override
            public void doSuccess() {
                hideLoading();
            }
        }, movie.getId());
    }

    public void navigateToMovieDetail(MovieResponse movieResponse, ListWatchHistoryResponse listWatchHistoryResponses) {
        if (!viewModel.isLogin()) {
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra("movie_details", GsonUtils.toJson(movieResponse));
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra("movie_details", GsonUtils.toJson(movieResponse));
            intent.putExtra("movie_details_tracking", GsonUtils.toJson(listWatchHistoryResponses));
            startActivity(intent);
        }
    }
}
