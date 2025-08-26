package com.movie_hub.android.ui.main.movie.detail.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
            getListMovie();
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

    public void getListMovie() {
//        ((MovieDetailActivity) requireActivity()).showLoading();
        ((MovieDetailActivity) requireActivity()).showLoading();
        viewModel.getListMovie(new MainCallback<List<MovieResponse>>() {
            @Override
            public void doError(Throwable throwable) {
                ((MovieDetailActivity) requireActivity()).hideLoading();
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
                ((MovieDetailActivity) requireActivity()).hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doSuccess(List<MovieResponse> list) {
                ((MovieDetailActivity) requireActivity()).hideLoading();
                if (!isAdded()) return;
//                ((MovieDetailActivity) requireActivity()).hideLoading();
                movieAdapter.setData(list);
                isLoaded = true;
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
        return R.layout.fragment_recommendation;
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public void onMovieClick(MovieResponse movie) {
        Intent intent = new Intent(getContext(), MovieDetailActivity.class);
        intent.putExtra("movie_details", movie);
        startActivity(intent);
    }
}
