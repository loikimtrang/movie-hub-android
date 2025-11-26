package com.movie_hub.android.ui.main.movie.detail.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.movie_hub.android.R;
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
import com.movie_hub.android.utils.GridUtil;
import com.movie_hub.android.utils.GsonUtils;

import java.util.List;
import java.util.Objects;

import eu.davidea.flexibleadapter.databinding.BR;

public class RecommendationFragment extends BaseFragment<FragmentRecommendationBinding, RecommendationFragmentViewModel> implements MovieVerticalAdapter.OnMovieClickListener{
    private MovieVerticalAdapter movieAdapter;
    private boolean isLoaded = false;
    public static final int TYPE_SEARCH = 0;
    public static final int TYPE_MOVIE_DETAIL = 1;

    private static final String ARG_DISPLAY_FROM = "display_from";
    private static final String ARG_KEYWORD = "keyword";
    private static final String ARG_ID_MOVIE = "id_movie";

    private int displayFrom;
    private String keyword;
    private Long idMovie;

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);

        if (!isLoaded) {
            if (displayFrom == TYPE_MOVIE_DETAIL) {
                getListMovieTypeMovieDetail();
            } else {
                getListMovieTypeSearch();
            }
        }
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpAdapter();
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
        int spacing = requireContext().getResources().getDimensionPixelSize(R.dimen._8sdp);
        int spanCount = GridUtil.calculateSpanCount(requireContext(), 110);

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), spanCount);
        binding.rvRecommendation.setLayoutManager(layoutManager);
        binding.rvRecommendation.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing));
        binding.rvRecommendation.setAdapter(movieAdapter);
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
                list.removeIf(movie -> Objects.equals(movie.getId(), idMovie));
                movieAdapter.setData(list);
                isLoaded = true;
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
        request.setTitle(keyword);

        viewModel.getListMovie(new MainCallback<List<MovieResponse>>() {
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
            public void doSuccess(List<MovieResponse> list) {
                ((MainActivity) requireActivity()).hideLoading();
                if (!isAdded()) return;
                movieAdapter.setData(list);
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
        viewModel.getListMovieTracking(new MainCallback<List<ListWatchHistoryResponse>>() {
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
            public void doSuccess(List<ListWatchHistoryResponse> list) {
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
