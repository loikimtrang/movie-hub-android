package com.movie_hub.android.ui.main.movie.detail.fragment;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.request.moviePerson.MoviePersonRequest;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.moviePerson.MoviePersonResponse;
import com.movie_hub.android.databinding.FragmentCastBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailViewModel;
import com.movie_hub.android.ui.main.movie.detail.adapter.MoviePersonAdapter;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import eu.davidea.flexibleadapter.databinding.BR;

public class CastFragment extends BaseFragment<FragmentCastBinding, CastFragmentViewModel> implements MoviePersonAdapter.OnMoviePersonClickListener {

    private MoviePersonAdapter moviePersonAdapter;
    private boolean isLoaded = false;
    private MovieDetailViewModel sharedViewModel;

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(MovieDetailViewModel.class);
        setUpAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoaded) {
            getListMoviePerson();
        }
    }

    public void setUpAdapter() {
        moviePersonAdapter = new MoviePersonAdapter(this);
        binding.rvCast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.rvCast.setAdapter(moviePersonAdapter);
    }

    public void getListMoviePerson() {
        MovieResponse movie = sharedViewModel.getMovieDetails().getValue();
        if (movie == null || movie.getId() == 0) return;

        MoviePersonRequest request = new MoviePersonRequest();
        request.setMovieId(String.valueOf(movie.getId()));
        request.setKind(1);
        ((MovieDetailActivity) requireActivity()).showLoading();
        viewModel.getListMoviePerson(new MainCallback<List<MoviePersonResponse>>() {
            @Override public void doSuccess(List<MoviePersonResponse> data) {
                ((MovieDetailActivity) requireActivity()).hideLoading();
                if (data != null && !data.isEmpty()) {
                    moviePersonAdapter.setData(data);
                    binding.layoutEmpty.setVisibility(View.GONE);
                } else {
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override public void doError(Throwable throwable) {
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
            @Override public void doFail() {
                showError(getString(R.string.fetch_data_failed));
                ((MovieDetailActivity) requireActivity()).hideLoading();
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
        return R.layout.fragment_cast;
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public void onActorClick(MoviePersonResponse actor) {

    }
}

