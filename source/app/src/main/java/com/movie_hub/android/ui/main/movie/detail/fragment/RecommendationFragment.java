package com.movie_hub.android.ui.main.movie.detail.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.request.movie.MovieRequest;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.FragmentRecommendationBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.custom.GridSpacingItemDecoration;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.ui.main.search.topTrending.adapter.MovieVerticalAdapter;
import com.movie_hub.android.ui.main.search.topTrending.shimmer.MovieVerticalShimmerAdapter;
import com.movie_hub.android.utils.GridUtil;
import com.movie_hub.android.utils.GsonUtils;

import java.util.List;
import java.util.Objects;

import eu.davidea.flexibleadapter.databinding.BR;

public class RecommendationFragment extends BaseFragment<FragmentRecommendationBinding, RecommendationFragmentViewModel> implements MovieVerticalAdapter.OnMovieClickListener{
    private MovieVerticalAdapter movieAdapter;
    private MovieVerticalShimmerAdapter shimmerAdapter;
    private boolean isLoaded = false;
    private boolean isLoading = false;

    public static final int TYPE_SEARCH = 0;
    public static final int TYPE_MOVIE_DETAIL = 1;

    private static final String ARG_DISPLAY_FROM = "display_from";
    private static final String ARG_KEYWORD = "keyword";
    private static final String ARG_ID_MOVIE = "id_movie";

    private int displayFrom;
    private String keyword;
    private Long idMovie;

    int currentPage = 0;
    int pageSize = 12;
    boolean isLastPage = false;


    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);

        setUpAdapter();
        showShimmerAdapter();

        if (!isLoaded && currentPage == 0 && !isLoading) {
            isLoading = true;
            if (displayFrom == TYPE_MOVIE_DETAIL) {
                getListMovieTypeMovieDetail();
            } else {
                getListMovieTypeSearch();
            }
        }

        binding.rvRecommendation.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (displayFrom == TYPE_MOVIE_DETAIL) return;
                if (dy <= 0) return; // chỉ load khi kéo xuống

                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm == null || movieAdapter == null) return;

                int totalItemCount = lm.getItemCount();
                int lastVisibleItemPosition = lm.findLastVisibleItemPosition();

                if (!isLoading && !isLastPage && lastVisibleItemPosition >= totalItemCount - 5) {
                    isLoading = true;
                    getListMovieTypeSearch();
                }
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            displayFrom = getArguments().getInt(ARG_DISPLAY_FROM, TYPE_MOVIE_DETAIL);
            keyword = getArguments().getString(ARG_KEYWORD);
            idMovie = getArguments().getLong(ARG_ID_MOVIE, 0L);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public static RecommendationFragment newInstance(int displayFrom, String keyword, Long idMovie) {
        RecommendationFragment fragment = new RecommendationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DISPLAY_FROM, displayFrom);
        args.putString(ARG_KEYWORD, keyword);
        args.putLong(ARG_ID_MOVIE, idMovie);
        fragment.setArguments(args);
        return fragment;
    }

    public void setUpAdapter() {
        movieAdapter = new MovieVerticalAdapter(this);
        shimmerAdapter = new MovieVerticalShimmerAdapter(6);

        int spacing = requireContext().getResources().getDimensionPixelSize(R.dimen._8sdp);
        int spanCount = GridUtil.calculateSpanCount(requireContext(), 110);

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), spanCount);
        binding.rvRecommendation.setLayoutManager(layoutManager);
        binding.rvRecommendation.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing));
    }

    public void showShimmerAdapter() {
        binding.rvRecommendation.setAdapter(shimmerAdapter);
    }

    public void hideShimmer(List<MovieResponse> data) {
        binding.rvRecommendation.setAdapter(movieAdapter);
        movieAdapter.setData(data);
    }
    public void getListMovieTypeMovieDetail() {
        showLoading();
        viewModel.getListMovieRecommendations(new MainCallback<List<MovieResponse>>() {
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

            @SuppressLint("NewApi")
            @Override
            public void doSuccess(List<MovieResponse> list) {
                hideLoading();
                if (!isAdded()) return;
                if (list != null && !list.isEmpty()) {
                    binding.layoutEmpty.setVisibility(View.GONE);
                    list.removeIf(movie -> Objects.equals(movie.getId(), idMovie));
                    hideShimmer(list);
                    isLoaded = true;
                } else {
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void doSuccess() {
            }
        }, idMovie);
    }
    public void getListMovieTypeSearch() {
        if (keyword == null || keyword.isEmpty()) return;

        ((MainActivity) requireActivity()).showLoading();
        MovieRequest request = new MovieRequest();
        request.setPage(currentPage);
        request.setSize(pageSize);
        request.setPaged(true);
        viewModel.getListMovie(new MainCallback<ResponseListObj<MovieResponse>>() {
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
            public void doSuccess(ResponseListObj<MovieResponse> data) {
                ((MainActivity) requireActivity()).hideLoading();
                if (!isAdded()) return;

                if (data.getContent() != null && !data.getContent().isEmpty()) {
                    if (currentPage == 0) {
                        hideShimmer(data.getContent());
                    } else {
                        movieAdapter.addData(data.getContent());
                    }

                    binding.layoutEmpty.setVisibility(View.GONE);
                    currentPage++;

                    if (currentPage >= data.getTotalPages()) {  // >= để an toàn
                        isLastPage = true;
                    }
                } else {
                    if (currentPage == 0) binding.layoutEmpty.setVisibility(View.VISIBLE);
                    isLastPage = true;
                }

                isLoading = false;
                isLoaded = true;
            }

            @Override
            public void doSuccess() {
            }
        }, request);
    }


    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_recommendation;
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
    private void showLoading() {
        if (displayFrom == TYPE_SEARCH && requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).showLoading();
        } else if (requireActivity() instanceof MovieDetailActivity) {
            ((MovieDetailActivity) requireActivity()).showLoading();
        }
    }

    private void hideLoading() {
        if (displayFrom == TYPE_SEARCH && requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).hideLoading();
        } else if (requireActivity() instanceof MovieDetailActivity) {
            ((MovieDetailActivity) requireActivity()).hideLoading();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
