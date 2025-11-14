package com.movie_hub.android.ui.main.movie.detail.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.request.moviePerson.MoviePersonRequest;
import com.movie_hub.android.data.model.api.request.person.PersonRequest;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.moviePerson.MoviePersonResponse;
import com.movie_hub.android.data.model.api.response.person.PersonResponse;
import com.movie_hub.android.databinding.FragmentCastBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailViewModel;
import com.movie_hub.android.ui.main.movie.detail.adapter.MoviePersonAdapter;
import com.movie_hub.android.ui.main.movie.detail.adapter.PersonAdapter;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import eu.davidea.flexibleadapter.databinding.BR;

public class CastFragment extends BaseFragment<FragmentCastBinding, CastFragmentViewModel> implements MoviePersonAdapter.OnMoviePersonClickListener, PersonAdapter.OnPersonClickListener{

    private MoviePersonAdapter moviePersonAdapter;
    private PersonAdapter personAdapter;
    private boolean isLoaded = false;
    private MovieDetailViewModel sharedViewModel;
    public static final int TYPE_SEARCH = 0;
    public static final int TYPE_MOVIE_DETAIL = 1;
    private static final String ARG_DISPLAY_FROM = "display_from";
    private static final String ARG_KEYWORD = "keyword";

    private int displayFrom;
    private String keyword;

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);

        if (!isLoaded) {
            if (displayFrom == TYPE_MOVIE_DETAIL) {
                getListMoviePersonTypeMovieDetail();
            } else {
                getListMoviePersonTypeSearch();
            }
            isLoaded = true;
        }
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            displayFrom = getArguments().getInt(ARG_DISPLAY_FROM, TYPE_MOVIE_DETAIL);
            keyword = getArguments().getString(ARG_KEYWORD);
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpAdapter();
    }

    public static CastFragment newInstance(int displayFrom, String keyword) {
        CastFragment fragment = new CastFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DISPLAY_FROM, displayFrom);
        args.putString(ARG_KEYWORD, keyword);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setUpAdapter() {
        if (displayFrom == TYPE_MOVIE_DETAIL) {
            moviePersonAdapter = new MoviePersonAdapter(this);
            binding.rvCast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            binding.rvCast.setAdapter(moviePersonAdapter);
        } else {
            personAdapter = new PersonAdapter(this);
            binding.rvCast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            binding.rvCast.setAdapter(personAdapter);
        }
    }

    public void getListMoviePersonTypeMovieDetail() {
        sharedViewModel = new ViewModelProvider(requireActivity()).get(MovieDetailViewModel.class);
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
                showError(getString(R.string.fetch_data_failed));

            }
            @Override public void doFail() {
                showError(getString(R.string.fetch_data_failed));
                ((MovieDetailActivity) requireActivity()).hideLoading();
            }
            @Override public void doSuccess() {}
        }, request);
    }
    public void getListMoviePersonTypeSearch() {
        if (keyword == null || keyword.isEmpty()) return;

        ((MainActivity) requireActivity()).showLoading();
        PersonRequest personRequest = new PersonRequest();
        personRequest.setName(keyword);

        viewModel.getListPerson(new MainCallback<List<PersonResponse>>() {
            @Override public void doSuccess(List<PersonResponse> data) {
                ((MainActivity) requireActivity()).hideLoading();
                if (data != null && !data.isEmpty()) {
                    personAdapter.setData(data);
                    binding.layoutEmpty.setVisibility(View.GONE);
                } else {
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override public void doError(Throwable throwable) {
                ((MainActivity) requireActivity()).hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));

            }
            @Override public void doFail() {
                showError(getString(R.string.fetch_data_failed));
                ((MainActivity) requireActivity()).hideLoading();
            }
            @Override public void doSuccess() {}
        }, personRequest);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPersonClick(PersonResponse actor) {

    }
}

