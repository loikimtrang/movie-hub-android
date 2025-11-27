package com.movie_hub.android.ui.main.search.topTrending;

import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.request.movie.MovieRequest;
import com.movie_hub.android.data.model.api.response.favourite.FavouriteResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.data.model.room.SearchHistoryEntity;
import com.movie_hub.android.databinding.FragmentSearchTopTrendingBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.favourite.adapter.MovieFavouriteAdapter;
import com.movie_hub.android.ui.main.account.favourite.shimmer.MovieFavoriteShimmerAdapter;
import com.movie_hub.android.ui.main.custom.GridSpacingItemDecoration;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.ui.main.search.SearchFragment;
import com.movie_hub.android.ui.main.search.topTrending.adapter.MovieVerticalAdapter;
import com.movie_hub.android.ui.main.search.topTrending.adapter.SearchHistoryAdapter;
import com.movie_hub.android.ui.main.search.topTrending.shimmer.MovieVerticalShimmerAdapter;
import com.movie_hub.android.utils.GridUtil;
import com.movie_hub.android.utils.GsonUtils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import eu.davidea.flexibleadapter.databinding.BR;

public class SearchTopTrendingFragment extends BaseFragment<FragmentSearchTopTrendingBinding, SearchTopTrendingViewModel> implements MovieVerticalAdapter.OnMovieClickListener, SearchHistoryAdapter.OnItemClickListener{

    private MovieVerticalAdapter movieAdapter;
    private SearchHistoryAdapter historyAdapter;
    private MovieVerticalShimmerAdapter shimmerAdapter;
    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
        showShimmerLoading();
        setUpAdapter();
        getListMovie();
        viewModel.getHistory();
        setUpHistory();
    }

    public void setUpHistory() {
        viewModel.getHistoriesLiveData().observe(getViewLifecycleOwner(),  histories -> {
            if (histories != null && !histories.isEmpty()) {
                binding.searchHistory.setVisibility(View.VISIBLE);

                SearchHistoryEntity delete = new SearchHistoryEntity();
                delete.keyword = getString(R.string.delete);

                histories.add(delete);

                historyAdapter.setData(histories);
            } else {
                binding.searchHistory.setVisibility(View.GONE);
            }
        });
    }

    public void showShimmerLoading() {
        shimmerAdapter = new MovieVerticalShimmerAdapter(6);

        int spacing = requireContext().getResources().getDimensionPixelSize(R.dimen._8sdp);
        int spanCount = GridUtil.calculateSpanCount(requireContext(), 110);

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), spanCount);
        binding.rvMovie.setLayoutManager(layoutManager);
        binding.rvMovie.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing));
        binding.rvMovie.setAdapter(shimmerAdapter);
    }

    public void hideShimmerAndShowData(List<MovieResponse> data) {
        movieAdapter = new MovieVerticalAdapter(this);
        int spacing = requireContext().getResources().getDimensionPixelSize(R.dimen._8sdp);
        int spanCount = GridUtil.calculateSpanCount(requireContext(), 110);

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), spanCount);
        binding.rvMovie.setLayoutManager(layoutManager);
        binding.rvMovie.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing));
        binding.rvMovie.setAdapter(movieAdapter);
        movieAdapter.setData(data);
    }

    public void setUpAdapter() {

        historyAdapter = new SearchHistoryAdapter(this);

        FlexboxLayoutManager layout = new FlexboxLayoutManager(getContext());
        layout.setFlexDirection(FlexDirection.ROW);
        layout.setFlexWrap(FlexWrap.WRAP);
        layout.setJustifyContent(JustifyContent.FLEX_START);
        layout.setAlignItems(AlignItems.FLEX_START);

        binding.searchHistory.setLayoutManager(layout);

        int a = getResources().getDimensionPixelSize(R.dimen._6sdp);
        binding.searchHistory.addItemDecoration(new FlexSpacingItemDecoration(a));

        binding.searchHistory.setAdapter(historyAdapter);
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            viewModel.getHistory();
            setUpHistory();
        }
    }

    public void getListMovie() {
        if (!isAdded()) return; // Fragment đã bị detach

        ((MainActivity) requireActivity()).showLoading();

        viewModel.getListMovie(new MainCallback<List<MovieResponse>>() {
            @Override
            public void doError(Throwable throwable) {
                ((MainActivity) requireActivity()).hideLoading();
                if (!isAdded()) return;

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
                ((MainActivity) requireActivity()).hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doSuccess(List<MovieResponse> list) {
                if (!isAdded()) return;

                ((MainActivity) requireActivity()).hideLoading();
                hideShimmerAndShowData(list);
            }

            @Override
            public void doSuccess() {
                // không cần xử lý gì
            }
        }, new MovieRequest());
    }


    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search_top_trending;
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public void onMovieClick(MovieResponse movie) {
        if (movie != null) {
            getMovie(movie);
        }
    }

    public void getMovie(MovieResponse movie) {
        if (!isAdded()) return;
        ((MainActivity) requireActivity()).showLoading();

        viewModel.getMovie(new MainCallback<MovieResponse>() {
            @Override
            public void doError(Throwable throwable) {
                ((MainActivity) requireActivity()).hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doFail() {
                ((MainActivity) requireActivity()).hideLoading();
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
        ((MainActivity) requireActivity()).showLoading();

        viewModel.getListMovieTracking(new MainCallback<ListWatchHistoryResponse>() {
            @Override
            public void doError(Throwable throwable) {
                ((MainActivity) requireActivity()).hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doFail() {
                ((MainActivity) requireActivity()).hideLoading();
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
    public void onItemClick(SearchHistoryEntity item, int position) {
        if (getString(R.string.delete).equals(item.keyword)) {
            binding.searchHistory.setVisibility(View.GONE);
            viewModel.clearAllHistory();
        } else {
            if (getParentFragment() instanceof SearchFragment) {
                ((SearchFragment) getParentFragment()).onHistoryItemClicked(item.keyword);
            }
        }
    }

}
