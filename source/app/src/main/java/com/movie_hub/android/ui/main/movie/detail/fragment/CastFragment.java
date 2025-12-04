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
import com.movie_hub.android.constant.Constants;
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
import com.movie_hub.android.ui.main.account.favourite.shimmer.PersonFavoriteShimmerAdapter;
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
    int pageSize = 20;
    boolean isLastPage = false;
    private boolean isLoading = false;

    private PersonFavoriteShimmerAdapter shimmerAdapter;

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);

        if (!isLoaded) {
            if (displayFrom == TYPE_MOVIE_DETAIL) {
                getListMoviePersonTypeMovieDetail();

                binding.rvCast.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (dy <= 0) return;

                        LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                        if (lm == null || moviePersonAdapter == null) return;

                        int totalItemCount = lm.getItemCount();
                        int lastVisibleItemPosition = lm.findLastVisibleItemPosition();

                        if (!isLoading && !isLastPage && lastVisibleItemPosition >= totalItemCount - 5) {
                            getListMoviePersonTypeMovieDetail();
                        }
                    }
                });
            } else if (displayFrom == TYPE_SEARCH) {
                getListMoviePersonTypeSearch();

                binding.rvCast.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (dy <= 0) return;

                        LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                        if (lm == null || personAdapter == null) return;

                        int totalItemCount = lm.getItemCount();
                        int lastVisibleItemPosition = lm.findLastVisibleItemPosition();

                        if (!isLoading && !isLastPage && lastVisibleItemPosition >= totalItemCount - 5) {
                            getListMoviePersonTypeSearch();
                        }
                    }
                });
            } else {
                getListFavoritePersonTypeFavourite();
                binding.rvCast.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (dy <= 0) return;

                        LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                        if (lm == null || personFavouriteAdapter == null) return;

                        int totalItemCount = lm.getItemCount();
                        int lastVisibleItemPosition = lm.findLastVisibleItemPosition();

                        if (!isLoading && !isLastPage && lastVisibleItemPosition >= totalItemCount - 5) {
                            getListFavoritePersonTypeFavourite();
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
        showShimmer();
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
        binding.rvCast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        if (displayFrom == TYPE_MOVIE_DETAIL) {
            shimmerAdapter = new PersonFavoriteShimmerAdapter(6, false);
            moviePersonAdapter = new MoviePersonAdapter(this);
            binding.rvCast.setAdapter(moviePersonAdapter);
        } else if (displayFrom == TYPE_SEARCH) {
            shimmerAdapter = new PersonFavoriteShimmerAdapter(6, false);
            personAdapter = new PersonAdapter(this);
            binding.rvCast.setAdapter(personAdapter);
        } else {
            shimmerAdapter = new PersonFavoriteShimmerAdapter(6, true);
            personFavouriteAdapter = new PersonFavouriteAdapter(this);
            binding.rvCast.setAdapter(personFavouriteAdapter);
        }
    }

    public void showShimmer() {
        binding.rvCast.setAdapter(shimmerAdapter);
    }

    public void hideShimmer() {
        if (displayFrom == TYPE_MOVIE_DETAIL) {
            binding.rvCast.setAdapter(moviePersonAdapter);
        } else if (displayFrom == TYPE_SEARCH) {
            binding.rvCast.setAdapter(personAdapter);
        } else {
            binding.rvCast.setAdapter(personFavouriteAdapter);
        }
    }

    public void getListMoviePersonTypeMovieDetail() {
        isLoading = true;
        sharedViewModel = new ViewModelProvider(requireActivity()).get(MovieDetailViewModel.class);
        MovieResponse movie = sharedViewModel.movieDetails;
        if (movie == null || movie.getId() == 0) return;

        MoviePersonRequest request = new MoviePersonRequest();
        request.setPage(currentPage);
        request.setSize(pageSize);
        request.setMovieId(String.valueOf(movie.getId()));
        request.setKind(1);
        
        viewModel.getListMoviePerson(new MainCallback<ResponseListObj<MoviePersonResponse>>() {
            @Override public void doSuccess(ResponseListObj<MoviePersonResponse> data) {
                hideLoading();
                if (data.getContent() != null && !data.getContent().isEmpty()) {
                    if (currentPage == 0) {
                        binding.layoutEmpty.setVisibility(View.GONE);
                        hideShimmer();
                        moviePersonAdapter.setData(data.getContent());
                    } else {
                        moviePersonAdapter.addData(data.getContent());
                    }

                    binding.layoutEmpty.setVisibility(View.GONE);
                    isLoaded = true;
                    currentPage++;

                    if (currentPage == data.getTotalPages()) {
                        isLastPage = true;
                    }

                } else {
                    if (currentPage == 0) binding.layoutEmpty.setVisibility(View.VISIBLE);
                    isLastPage = true;
                }

                isLoading = false;
            }

            @Override public void doError(Throwable throwable) {
                hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));
                isLoading = false;

            }
            @Override public void doFail() {
                showError(getString(R.string.fetch_data_failed));
                hideLoading();

                isLoading = false;
            }
            @Override public void doSuccess() {}
        }, request);
    }
    public void getListMoviePersonTypeSearch() {
        if (keyword == null || keyword.isEmpty()) return;
        isLoading = true;
        PersonRequest personRequest = new PersonRequest();
        personRequest.setPage(currentPage);
        personRequest.setSize(pageSize);
        personRequest.setName(keyword);
        personRequest.setKind(1);

        viewModel.getListPerson(new MainCallback<ResponseListObj<PersonResponse>>() {
            @Override public void doSuccess(ResponseListObj<PersonResponse> data) {
                hideLoading();
                if (data.getContent() != null && !data.getContent().isEmpty()) {
                    if (currentPage == 0) {
                        binding.layoutEmpty.setVisibility(View.GONE);
                        hideShimmer();
                        personAdapter.setData(data.getContent());
                    } else {
                        personAdapter.addData(data.getContent());
                    }

                    binding.layoutEmpty.setVisibility(View.GONE);
                    isLoaded = true;
                    currentPage++;

                    if (currentPage == data.getTotalPages()) {
                        isLastPage = true;
                    }

                } else {
                    if (currentPage == 0) binding.layoutEmpty.setVisibility(View.VISIBLE);
                    isLastPage = true;
                }

                isLoading = false;
            }

            @Override public void doError(Throwable throwable) {
                hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));
                isLoading = false;
            }
            @Override public void doFail() {
                showError(getString(R.string.fetch_data_failed));
                hideLoading();
                isLoading = false;
            }
            @Override public void doSuccess() {}
        }, personRequest);
    }
    public void getListFavoritePersonTypeFavourite() {
        isLoading = true;
        FavouriteListRequest request = new FavouriteListRequest();
        request.setPage(currentPage);
        request.setSize(pageSize);
        request.setType(Constants.FAVOURITE_TYPE_PERSON);

        viewModel.getFavoritePersonList(new MainCallback<ResponseListObj<FavouriteResponse>>() {
            @Override public void doSuccess(ResponseListObj<FavouriteResponse> data) {
                hideLoading();
                if (data.getContent() != null && !data.getContent().isEmpty()) {
                    if (currentPage == 0) {
                        hideShimmer();
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

                isLoading = false;
            }

            @Override public void doError(Throwable throwable) {
                hideLoading();
                if (!isAdded()) return;
                showError(getString(R.string.fetch_data_failed));
                isLoading = false;

            }
            @Override public void doFail() {
                hideLoading();
                showError(getString(R.string.fetch_data_failed));
                isLoading = false;
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

