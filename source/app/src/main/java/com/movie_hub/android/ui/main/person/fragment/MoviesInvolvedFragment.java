package com.movie_hub.android.ui.main.person.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.request.moviePerson.MoviePersonRequest;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.moviePerson.MoviePersonResponse;
import com.movie_hub.android.databinding.FragmentMoviesInvolvedBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.custom.GridSpacingItemDecoration;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.ui.main.person.PersonDetailActivity;
import com.movie_hub.android.ui.main.search.topTrending.adapter.MovieVerticalAdapter;
import com.movie_hub.android.utils.GridUtil;
import com.movie_hub.android.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;

public class MoviesInvolvedFragment extends BaseFragment<FragmentMoviesInvolvedBinding, MoviesInvolvedFragmentViewModel> implements MovieVerticalAdapter.OnMovieClickListener{

    private MovieVerticalAdapter movieAdapter;
    private boolean isLoaded = false;
    private static final String ARG_PERSON_ID = "person_id";
    private Long personId;
    private List<MovieResponse> movieList = new ArrayList<>();

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
        if (getArguments() != null) {
            personId = getArguments().getLong(ARG_PERSON_ID, -1L);
        }

        if (!isLoaded) {
            setUpAdapter();
            getListMovieOfPerson();
        }
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
    public static MoviesInvolvedFragment newInstance(Long personId) {
        MoviesInvolvedFragment fragment = new MoviesInvolvedFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PERSON_ID, personId);
        fragment.setArguments(args);
        return fragment;
    }

    public void getListMovieOfPerson() {
        MoviePersonRequest request = new MoviePersonRequest();
        request.setPersonId(String.valueOf(personId));
        request.setKind(1);
        ((PersonDetailActivity) requireActivity()).showLoading();

        viewModel.getListMoviePerson(new MainCallback<List<MoviePersonResponse>>() {

            @Override public void doSuccess(List<MoviePersonResponse> data) {
                if (!isAdded()) return;
                ((PersonDetailActivity) requireActivity()).hideLoading();
                if (data != null && !data.isEmpty()) {
                    binding.layoutEmpty.setVisibility(View.GONE);
                    movieList.clear();
                    for (MoviePersonResponse item : data) {
                        if (item.getMovie() != null) {
                            movieList.add(item.getMovie());
                        }
                    }

                    if (movieList != null) {
                        movieAdapter.setData(movieList);
                        isLoaded = true;
                    } else {
                        binding.layoutEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override public void doError(Throwable throwable) {
                ((PersonDetailActivity) requireActivity()).hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));

            }
            @Override public void doFail() {
                showError(getString(R.string.fetch_data_failed));
                ((PersonDetailActivity) requireActivity()).hideLoading();
            }
            @Override public void doSuccess() {}
        }, request);
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_movies_involved;
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
        ((PersonDetailActivity) requireActivity()).showLoading();
    }

    private void hideLoading() {
        ((PersonDetailActivity) requireActivity()).hideLoading();
    }
}
