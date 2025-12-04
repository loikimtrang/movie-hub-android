package com.movie_hub.android.ui.main.home.topic.topic_detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.request.collection.CollectionItemRequest;
import com.movie_hub.android.data.model.api.response.collection.CollectionResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.ActivityHomeTopicDetailBinding;
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

import java.util.ArrayList;
import java.util.List;

public class HomeTopicDetailActivity extends BaseActivity<ActivityHomeTopicDetailBinding, HomeTopicDetailViewModel>
        implements SystemBarColorProvider, MovieVerticalAdapter.OnMovieClickListener{
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
        return R.layout.activity_home_topic_detail;
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }
    int currentPage = 0;
    int pageSize = 20;
    boolean isLastPage = false;
    private boolean isLoading = false;
    private MovieVerticalAdapter movieVerticalAdapter;
    private MovieVerticalShimmerAdapter movieVerticalShimmerAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        setUpAdapter();
        showShimmer();

        String json = getIntent().getStringExtra("collection");
        CollectionResponse collectionResponse = GsonUtils.fromJson(json, CollectionResponse.class);

        if (collectionResponse != null) {
            viewBinding.tvTitle.setSelected(true);
            viewBinding.tvTitle.setText(collectionResponse.getName());

            viewModel.collectionResponse = collectionResponse;
            getListMovie();
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
                    getListMovie();
                }
            }
        });
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

    public void getListMovie() {
        showLoading();
        isLoading = true;
        CollectionItemRequest request = new CollectionItemRequest();
        request.setCollectionId(viewModel.collectionResponse.getId());
        request.setSize(pageSize);
        request.setPage(currentPage);

        viewModel.getListCollectionItem(new MainCallback<ResponseListObj<MovieResponse>>() {
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
                    if (currentPage == 0) {
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

                    currentPage++;

                    if (currentPage >= data.getTotalPages()) {
                        isLastPage = true;
                    }
                } else {
                    if (currentPage == 0) {
                        hideShimmer();
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
    protected void onResume() {
        super.onResume();
        hideLoading();
    }
}
