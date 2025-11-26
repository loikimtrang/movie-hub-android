package com.movie_hub.android.ui.main.movie.detail.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.favourite.CreateFavouriteRequest;
import com.movie_hub.android.data.model.api.request.favourite.FavouriteListRequest;
import com.movie_hub.android.data.model.api.request.moviePerson.MoviePersonRequest;
import com.movie_hub.android.data.model.api.request.person.PersonRequest;
import com.movie_hub.android.data.model.api.response.favourite.FavouriteResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.moviePerson.MoviePersonResponse;
import com.movie_hub.android.data.model.api.response.person.PersonResponse;
import com.movie_hub.android.databinding.FragmentCastBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.favourite.FavouriteActivity;
import com.movie_hub.android.ui.main.account.favourite.adapter.PersonFavouriteAdapter;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailViewModel;
import com.movie_hub.android.ui.main.movie.detail.adapter.MoviePersonAdapter;
import com.movie_hub.android.ui.main.movie.detail.adapter.PersonAdapter;
import com.movie_hub.android.ui.main.person.PersonDetailActivity;
import com.movie_hub.android.utils.GsonUtils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.databinding.BR;

public class CastFragment extends BaseFragment<FragmentCastBinding, CastFragmentViewModel> implements MoviePersonAdapter.OnMoviePersonClickListener,
        PersonAdapter.OnPersonClickListener,
        PersonFavouriteAdapter.onPersonFavouriteClickListener {
    private MoviePersonAdapter moviePersonAdapter;
    private PersonAdapter personAdapter;
    private PersonFavouriteAdapter personFavouriteAdapter;
    private boolean isLoaded = false;
    private MovieDetailViewModel sharedViewModel;
    public static final int TYPE_SEARCH = 0;
    public static final int TYPE_MOVIE_DETAIL = 1;
    public static final int TYPE_FAVORITE = 2;

    private static final String ARG_DISPLAY_FROM = "display_from";
    private static final String ARG_KEYWORD = "keyword";

    private int displayFrom;
    private String keyword;

    int currentPage = 0;
    int pageSize = 8;
    boolean isLastPage = false;

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);

        if (!isLoaded) {
            if (displayFrom == TYPE_MOVIE_DETAIL) {
                getListMoviePersonTypeMovieDetail();
            } else if (displayFrom == TYPE_SEARCH) {
                getListMoviePersonTypeSearch();
            } else {
                getListFavoritePersonTypeFavourite();
                binding.rvCast.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                        if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == personFavouriteAdapter.getItemCount() - 1) {
                            if (!isLastPage) {
                                getListFavoritePersonTypeFavourite();
                            }
                        }
                    }
                });
            }
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
        } else if (displayFrom == TYPE_SEARCH) {
            personAdapter = new PersonAdapter(this);
            binding.rvCast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            binding.rvCast.setAdapter(personAdapter);
        } else {
            personFavouriteAdapter = new PersonFavouriteAdapter(this);
            binding.rvCast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            binding.rvCast.setAdapter(personFavouriteAdapter);
        }
    }

    public void getListMoviePersonTypeMovieDetail() {
        sharedViewModel = new ViewModelProvider(requireActivity()).get(MovieDetailViewModel.class);
        MovieResponse movie = sharedViewModel.movieDetails;
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
                    isLoaded = true;
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

        showLoading();
        PersonRequest personRequest = new PersonRequest();
        personRequest.setName(keyword);
        personRequest.setKind(1);

        viewModel.getListPerson(new MainCallback<List<PersonResponse>>() {
            @Override public void doSuccess(List<PersonResponse> data) {
                hideLoading();
                if (data != null && !data.isEmpty()) {
                    personAdapter.setData(data);
                    binding.layoutEmpty.setVisibility(View.GONE);
                    isLoaded = true;
                } else {
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override public void doError(Throwable throwable) {
                hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));

            }
            @Override public void doFail() {
                showError(getString(R.string.fetch_data_failed));
                hideLoading();
            }
            @Override public void doSuccess() {}
        }, personRequest);
    }
    public void getListFavoritePersonTypeFavourite() {
        showLoading();
        FavouriteListRequest request = new FavouriteListRequest();
        request.setPage(currentPage);
        request.setSize(pageSize);
        request.setPaged(true);
        request.setType(CreateFavouriteRequest.FAVOURITE_TYPE_PERSON);

        viewModel.getFavoritePersonList(new MainCallback<ResponseListObj<FavouriteResponse>>() {
            @Override public void doSuccess(ResponseListObj<FavouriteResponse> data) {
                hideLoading();
                if (data.getContent() != null && !data.getContent().isEmpty()) {
                    if (currentPage == 0) {
                        personFavouriteAdapter.setData(data.getContent());
                    } else {
                        personFavouriteAdapter.addData(data.getContent());
                    }

                    binding.layoutEmpty.setVisibility(View.GONE);
                    viewModel.favouriteResponses.addAll(data.getContent());
                    isLoaded = true;
                    currentPage++;

                    if (currentPage == data.getTotalPages()) {
                        isLastPage = true;
                    }
                } else {
                    if (currentPage == 0) binding.layoutEmpty.setVisibility(View.VISIBLE);
                    isLastPage = true;
                }
            }

            @Override public void doError(Throwable throwable) {
                hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));

            }
            @Override public void doFail() {
                hideLoading();
                showError(getString(R.string.fetch_data_failed));
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
        if (actor.getPerson() != null) {
            getPerson(actor.getPerson().getId());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPersonClick(PersonResponse actor) {
        getPerson(actor.getId());
    }

    public void getPerson(Long id) {
        showLoading();
        viewModel.getPerson(new MainCallback<PersonResponse>() {
            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {
                hideLoading();
            }

            @Override
            public void doSuccess(PersonResponse data) {
                hideLoading();
                navigateToPersonDetail(data);
            }

            @Override
            public void doFail() {
                showError(getString(R.string.an_error_occurred));
                hideLoading();
            }
        }, id);
    }

    public void navigateToPersonDetail(PersonResponse data) {
        Intent intent = new Intent(getContext(), PersonDetailActivity.class);
        intent.putExtra("person", GsonUtils.toJson(data));
        startActivity(intent);
    }

    private void showLoading() {
        if (displayFrom == TYPE_SEARCH && requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).showLoading();
        } else if (requireActivity() instanceof MovieDetailActivity) {
            ((MovieDetailActivity) requireActivity()).showLoading();
        } else if (requireActivity() instanceof FavouriteActivity) {
            ((FavouriteActivity) requireActivity()).showLoading();
        }
    }

    private void hideLoading() {
        if (displayFrom == TYPE_SEARCH && requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).hideLoading();
        } else if (requireActivity() instanceof MovieDetailActivity) {
            ((MovieDetailActivity) requireActivity()).hideLoading();
        } else if (requireActivity() instanceof FavouriteActivity) {
            ((FavouriteActivity) requireActivity()).hideLoading();
        }
    }

    @Override
    public void onPersonFavouriteClick(PersonResponse actor) {
        if (actor != null) {
            getPerson(actor.getId());
        }
    }

    @Override
    public void onUnFavouriteClick(FavouriteResponse favouriteResponse) {
        viewModel.deleteFavorite(favouriteResponse.getId());
        viewModel.favouriteResponses.remove(favouriteResponse);

        if (viewModel.favouriteResponses.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        }
    }
}

