package com.movie_hub.android.ui.main.search.topTrending;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Movie;
import android.util.TypedValue;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.request.movie.MovieRequest;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.FragmentSearchTopTrendingBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.custom.GridSpacingItemDecoration;
import com.movie_hub.android.ui.main.search.topTrending.adapter.MovieVerticalAdapter;
import com.movie_hub.android.utils.GridUtil;

import java.util.Arrays;
import java.util.List;

import eu.davidea.flexibleadapter.databinding.BR;

public class SearchTopTrendingFragment extends BaseFragment<FragmentSearchTopTrendingBinding, SearchTopTrendingViewModel> implements MovieVerticalAdapter.OnMovieClickListener{

    private MovieVerticalAdapter movieAdapter;
    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
        setUpAdapter();
        getListMovie();
    }

    public void setUpAdapter() {

        movieAdapter = new MovieVerticalAdapter(this);
        int spacing = requireContext().getResources().getDimensionPixelSize(R.dimen._8sdp);
        int spanCount = GridUtil.calculateSpanCount(requireContext(), 110);

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), spanCount);
        binding.rvMovie.setLayoutManager(layoutManager);
        binding.rvMovie.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing));
        binding.rvMovie.setAdapter(movieAdapter);
    }


    public void getListMovie() {
        ((MainActivity) requireActivity()).showLoading();
        viewModel.getListMovie(new MainCallback<List<MovieResponse>>() {
            @Override
            public void doError(Throwable error) {

            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doFail() {

            }

            @Override
            public void doSuccess(List<MovieResponse> list) {
                ((MainActivity) requireActivity()).hideLoading();
                movieAdapter.setData(list);
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

    }
}
