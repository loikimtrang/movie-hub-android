package com.movie_hub.android.ui.main.account.favourite.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.request.favourite.CreateFavouriteRequest;
import com.movie_hub.android.data.model.api.request.favourite.FavouriteListRequest;
import com.movie_hub.android.data.model.api.response.favourite.FavouriteResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.FragmentMovieFavoriteBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.favourite.FavouriteActivity;
import com.movie_hub.android.ui.main.account.favourite.adapter.MovieFavouriteAdapter;
import com.movie_hub.android.ui.main.account.favourite.shimmer.MovieFavoriteShimmerAdapter;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.utils.GsonUtils;

import java.util.List;

public class MovieFavoriteFragment extends BaseFragment<FragmentMovieFavoriteBinding, MovieFavoriteViewModel> implements MovieFavouriteAdapter.OnMovieClickListener {
    private boolean isLoaded = false;
    int currentPage = 0;
    int pageSize = 8;
    boolean isLastPage = false;
    private MovieFavouriteAdapter movieFavouriteAdapter;
    private MovieFavoriteShimmerAdapter shimmerAdapter;
    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
        if (!isLoaded && currentPage == 0) {
            showShimmerLoading();
            getListFavoriteMovie();
        }


        binding.rvMovie.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (layoutManager != null
                        && movieFavouriteAdapter != null
                        && layoutManager.findLastCompletelyVisibleItemPosition() == movieFavouriteAdapter.getItemCount() - 1) {
                    if (!isLastPage) {
                        getListFavoriteMovie();
                    }
                }

            }
        });

    }
    public void showShimmerLoading() {
        shimmerAdapter = new MovieFavoriteShimmerAdapter(6);
        binding.rvMovie.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.rvMovie.setAdapter(shimmerAdapter);
    }

    public void hideShimmerAndShowData(List<FavouriteResponse> data) {
        movieFavouriteAdapter = new MovieFavouriteAdapter(this, getContext());
        binding.rvMovie.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.rvMovie.setAdapter(movieFavouriteAdapter);
        movieFavouriteAdapter.setData(data);
    }


    public static MovieFavoriteFragment newInstance() {
        MovieFavoriteFragment fragment = new MovieFavoriteFragment();
        return fragment;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_movie_favorite;
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    public void getListFavoriteMovie() {
        showLoading();

        FavouriteListRequest request = new FavouriteListRequest();
        request.setType(Constants.FAVOURITE_TYPE_MOVIE);
        request.setPage(currentPage);
        request.setSize(pageSize);
        request.setPaged(true);

        viewModel.getFavoriteMovieList(new MainCallback<ResponseListObj<FavouriteResponse>>() {
            @Override
            public void doSuccess(ResponseListObj<FavouriteResponse> data) {
                hideLoading();
                if (data.getContent() != null && !data.getContent().isEmpty()) {
                    if (currentPage == 0) {
                        hideShimmerAndShowData(data.getContent());
                    } else {
                        movieFavouriteAdapter.addData(data.getContent());
                    }

                    binding.layoutEmpty.setVisibility(View.GONE);
                    viewModel.favouriteList.addAll(data.getContent());
                    isLoaded = true;
                    currentPage++;

                    if (currentPage == data.getTotalPages()) {
                        isLastPage = true;
                    }
                } else {
                    if (currentPage == 0) binding.layoutEmpty.setVisibility(View.VISIBLE);
                    isLastPage = true;
                }
            }

            @Override public void doError(Throwable throwable) {
                hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));
            }

            @Override public void doFail() {
                hideLoading();
                showError(getString(R.string.fetch_data_failed));
            }

            @Override public void doSuccess() {}
        }, request);
    }


    private void showLoading() {
        if (requireActivity() instanceof FavouriteActivity) {
            ((FavouriteActivity) requireActivity()).showLoading();
        }
    }

    private void hideLoading() {
         if (requireActivity() instanceof FavouriteActivity) {
            ((FavouriteActivity) requireActivity()).hideLoading();
        }
    }

    @Override
    public void onMovieClick(MovieResponse movie) {
        getMovie(movie);
    }

    public void getMovie(MovieResponse movie) {
        if (!isAdded()) return;
        showLoading();

        viewModel.getMovie(new MainCallback<MovieResponse>() {
            @Override
            public void doError(Throwable throwable) {
                hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doFail() {
                hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doSuccess(MovieResponse movieResponse) {
                if (!isAdded()) return;
                if (viewModel.isLogin()) {
                    getListMovieTracking(movieResponse);
                } else {
                    Intent intent = new Intent(getContext(), MovieDetailActivity.class);
                    intent.putExtra("movie_details", GsonUtils.toJson(movieResponse));
                    startActivity(intent);
                }
            }

            @Override
            public void doSuccess() {
            }
        }, movie.getId());
    }

    public void getListMovieTracking(MovieResponse movie) {
        if (!isAdded()) return;
        showLoading();
        viewModel.getListMovieTracking(new MainCallback<ListWatchHistoryResponse>() {
            @Override
            public void doError(Throwable throwable) {
                hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doFail() {
                hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doSuccess(ListWatchHistoryResponse list) {
                if (!isAdded()) return;

                Intent intent = new Intent(getContext(), MovieDetailActivity.class);
                intent.putExtra("movie_details", GsonUtils.toJson(movie));
                intent.putExtra("movie_details_tracking", GsonUtils.toJson(list));
                startActivity(intent);
            }

            @Override
            public void doSuccess() {
            }
        }, movie.getId());
    }

    @Override
    public void onUnFavouriteClick(FavouriteResponse favourite) {
        viewModel.deleteFavorite(favourite.getId());
        viewModel.favouriteList.remove(favourite);

        if (viewModel.favouriteList.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        }
    }
}
