package com.movie_hub.android.ui.main.movie.detail.fragment;

import android.view.View;

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
                getListMoviePersonTypeMovieDetail();
            } else {
                getListMoviePersonTypeSearch();
            }
        }
    }

    public void setUpAdapter() {
        if (DISPLAY_FROM.getValue() == TYPE_MOVIE_DETAIL) {
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
        ((MainActivity) requireActivity()).showLoading();
        PersonRequest personRequest = new PersonRequest();
        if (KEY_WORD.getValue() != null) {
            personRequest.setName(KEY_WORD.getValue());
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
        DISPLAY_FROM.setValue(null);
        KEY_WORD.setValue(null);
    }

    @Override
    public void onPersonClick(PersonResponse actor) {

    }
}

