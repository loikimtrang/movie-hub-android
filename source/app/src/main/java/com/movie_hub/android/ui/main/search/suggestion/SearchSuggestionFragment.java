package com.movie_hub.android.ui.main.search.suggestion;

import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.request.person.PersonRequest;
import com.movie_hub.android.data.model.api.request.movie.MovieRequest;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.person.PersonResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.FragmentSearchSuggestionBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.custom.HorizontalSpacingItemDecoration;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.ui.main.person.PersonDetailActivity;
import com.movie_hub.android.ui.main.search.suggestion.adapter.ActorAdapter;
import com.movie_hub.android.ui.main.search.suggestion.adapter.MovieSuggestAdapter;
import com.movie_hub.android.ui.main.search.suggestion.shimmer.ActorShimmerAdapter;
import com.movie_hub.android.ui.main.search.suggestion.shimmer.MovieSuggestShimmerAdapter;
import com.movie_hub.android.utils.GsonUtils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import eu.davidea.flexibleadapter.databinding.BR;
import lombok.Setter;

@Setter
public class SearchSuggestionFragment extends BaseFragment<FragmentSearchSuggestionBinding, SearchSuggestionViewModel> implements MovieSuggestAdapter.OnMovieClickListener, ActorAdapter.OnActorClickListener {

    private String keyWord;
    private MovieSuggestAdapter movieAdapter;
    private ActorAdapter actorAdapter;
    private ActorShimmerAdapter actorShimmerAdapter;
    private MovieSuggestShimmerAdapter movieSuggestShimmerAdapter;

    boolean isMovieDone = false;
    boolean isActorDone = false;

    boolean hasMovie = false;
    boolean hasActor = false;

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
        setUpAdapter();
        showShimmerLoading();
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;

        if (keyWord == null || viewModel == null) return;

        if (!isAdded()) return;
        ((MainActivity) requireActivity()).showLoading();
        showShimmerLoading();
        binding.layoutEmpty.setVisibility(View.GONE);
        MovieRequest movieRequest = new MovieRequest();
        movieRequest.setKeyword(keyWord);
        getListMovie(movieRequest);

        PersonRequest personRequest = new PersonRequest();
        personRequest.setName(keyWord);
        personRequest.setKind(1);
        getListActor(personRequest);
    }

    public void showShimmerLoading() {
        binding.tvActor.setVisibility(View.GONE);
        binding.tvMovie.setVisibility(View.GONE);

        binding.rvMovie.setAdapter(movieSuggestShimmerAdapter);
        binding.rvActor.setAdapter(actorShimmerAdapter);
        binding.tvMovieShimmer.setAlpha(Constants.SHIMMER_START_ALPHA);
        binding.tvActorShimmer.setAlpha(Constants.SHIMMER_START_ALPHA);

        binding.tvActorShimmer.setVisibility(View.VISIBLE);
        binding.tvMovieShimmer.setVisibility(View.VISIBLE);
    }

    public void hideActorShimmer(List<PersonResponse> data) {
        binding.tvActorShimmer.setVisibility(View.GONE);
        binding.tvActor.setVisibility(View.VISIBLE);

        binding.rvActor.setAdapter(actorAdapter);
        actorAdapter.setData(data);
    }

    public void hideMovieShimmer(List<MovieResponse> data) {
        binding.tvMovieShimmer.setVisibility(View.GONE);
        binding.tvMovie.setVisibility(View.VISIBLE);

        binding.rvMovie.setAdapter(movieAdapter);
        movieAdapter.setData(data);
    }

    public void getListMovie(MovieRequest request) {
        hasMovie = true;
        updateLayoutVisibility();

        showShimmerLoading();
        viewModel.getListMovie(new MainCallback<List<MovieResponse>>() {
            @Override public void doSuccess(List<MovieResponse> data) {
                hasMovie = data != null && !data.isEmpty();
                updateLayoutVisibility();
                hideMovieShimmer(data);
            }

            @Override public void doError(Throwable t) { handleError(t); isMovieDone = true; updateLayoutVisibility(); }
            @Override public void doFail() { isMovieDone = true; updateLayoutVisibility(); }
            @Override public void doSuccess() {}
        }, request);
    }
    public void getListActor(PersonRequest request) {
        hasActor = true;
        updateLayoutVisibility();

        showShimmerLoading();
        viewModel.getListPerson(new MainCallback<List<PersonResponse>>() {
            @Override public void doSuccess(List<PersonResponse> data) {
                hasActor = data != null && !data.isEmpty();
                updateLayoutVisibility();
                hideActorShimmer(data);
            }

            @Override public void doError(Throwable t) { handleError(t); isActorDone = true; updateLayoutVisibility(); }
            @Override public void doFail() { isActorDone = true; updateLayoutVisibility(); }
            @Override public void doSuccess() {}
        }, request);
    }

    private void updateLayoutVisibility() {
        if (!isAdded()) return;
        ((MainActivity) requireActivity()).hideLoading();

        binding.lMovie.setVisibility(hasMovie ? View.VISIBLE : View.GONE);
        binding.lActor.setVisibility(hasActor ? View.VISIBLE : View.GONE);
        binding.layoutEmpty.setVisibility((!hasMovie && !hasActor) ? View.VISIBLE : View.GONE);
    }
    private void handleError(Throwable t) {
        if (getContext() == null) return;

        if (t instanceof UnknownHostException || t instanceof SocketTimeoutException) {
            showError(getString(R.string.network_error_please_check_your_internet_connection));
        } else if (t instanceof ConnectException) {
            showError(getString(R.string.cannot_connect_to_the_server_please_try_again));
        } else {
            showError(getString(R.string.fetch_data_failed));
        }
    }

    public void setUpAdapter() {
        movieAdapter = new MovieSuggestAdapter(this, requireContext());
        actorAdapter = new ActorAdapter(this, requireContext());
        movieSuggestShimmerAdapter = new MovieSuggestShimmerAdapter(6, requireContext());
        actorShimmerAdapter = new ActorShimmerAdapter(6, requireContext());

        binding.rvMovie.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvActor.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        int spacing = getResources().getDimensionPixelSize(R.dimen._8sdp);
        binding.rvMovie.addItemDecoration(new HorizontalSpacingItemDecoration(spacing));
        binding.rvActor.addItemDecoration(new HorizontalSpacingItemDecoration(spacing));
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search_suggestion;
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public void onActorClick(PersonResponse actor) {
        getPerson(actor.getId());
    }

    public void getPerson(Long id) {
        ((MainActivity) requireActivity()).showLoading();
        viewModel.getPerson(new MainCallback<PersonResponse>() {
            @Override
            public void doError(Throwable error) {
                ((MainActivity) requireActivity()).hideLoading();
                showError(getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {
                ((MainActivity) requireActivity()).hideLoading();
            }

            @Override
            public void doSuccess(PersonResponse data) {
                ((MainActivity) requireActivity()).hideLoading();
                navigateToPersonDetail(data);
            }

            @Override
            public void doFail() {
                showError(getString(R.string.an_error_occurred));
                ((MainActivity) requireActivity()).hideLoading();
            }
        }, id);
    }

    public void navigateToPersonDetail(PersonResponse data) {
        Intent intent = new Intent(getContext(), PersonDetailActivity.class);
        intent.putExtra("person", GsonUtils.toJson(data));
        startActivity(intent);
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
}
