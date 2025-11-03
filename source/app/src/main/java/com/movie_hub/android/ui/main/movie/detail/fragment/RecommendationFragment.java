package com.movie_hub.android.ui.main.movie.detail.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.GridLayoutManager;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.request.movie.MovieRequest;
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

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import eu.davidea.flexibleadapter.databinding.BR;

public class RecommendationFragment extends BaseFragment<FragmentRecommendationBinding, RecommendationFragmentViewModel> implements MovieVerticalAdapter.OnMovieClickListener{
    private MovieVerticalAdapter movieAdapter;
    private boolean isLoaded = false;
    public static final int TYPE_SEARCH = 0;
    public static final int TYPE_MOVIE_DETAIL = 1;
    public static MutableLiveData<Integer> DISPLAY_FROM = new MutableLiveData<>();
    public static MutableLiveData<String> KEY_WORD = new MutableLiveData<>();

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
        setUpAdapter();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoaded) {
            if (DISPLAY_FROM.getValue() == TYPE_MOVIE_DETAIL) {
                getListMovieTypeMovieDetail();
            } else {
                getListMovieTypeSearch();
            }
        }
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
        ((MovieDetailActivity) requireActivity()).showLoading();
        viewModel.getListMovie(new MainCallback<List<MovieResponse>>() {
            @Override
            public void doError(Throwable throwable) {
                ((MovieDetailActivity) requireActivity()).hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doFail() {
                ((MovieDetailActivity) requireActivity()).hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doSuccess(List<MovieResponse> list) {
                ((MovieDetailActivity) requireActivity()).hideLoading();
                if (!isAdded()) return;
                movieAdapter.setData(list);
                isLoaded = true;
            }

            @Override
            public void doSuccess() {
            }
        }, new MovieRequest());
    }
    public void getListMovieTypeSearch() {
        ((MainActivity) requireActivity()).showLoading();
        MovieRequest request = new MovieRequest();
        if (KEY_WORD.getValue() != null) {
            request.setTitle(KEY_WORD.getValue());
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

                Intent intent = new Intent(getContext(), MovieDetailActivity.class);
                intent.putExtra("movie_details", movieResponse);
                startActivity(intent);
            }

            @Override
            public void doSuccess() {
            }
        }, movie.getId());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DISPLAY_FROM.setValue(null);
        KEY_WORD.setValue(null);
    }
}
