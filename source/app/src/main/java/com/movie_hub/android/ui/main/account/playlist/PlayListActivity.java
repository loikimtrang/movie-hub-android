package com.movie_hub.android.ui.main.account.playlist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.playlist.CreatePlaylistRequest;
import com.movie_hub.android.data.model.api.request.playlist.GetListMoviePlayListRequest;
import com.movie_hub.android.data.model.api.request.playlist.RemoveItemPlaylistRequest;
import com.movie_hub.android.data.model.api.request.playlist.UpdatePlaylistRequest;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.playlist.PlayListResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityPlayListBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.playlist.adpater.MoviePlaylistAdapter;
import com.movie_hub.android.ui.main.account.playlist.adpater.PlaylistItemAdapter;
import com.movie_hub.android.ui.main.account.playlist.dialog.CreatePlaylistDialogFragment;
import com.movie_hub.android.ui.main.account.playlist.dialog.UpdatePlaylistDialogFragment;
import com.movie_hub.android.ui.main.account.playlist.shimmer.MoviePlaylistShimmerAdapter;
import com.movie_hub.android.ui.main.account.playlist.shimmer.PlaylistShimmerAdapter;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;

public class PlayListActivity extends BaseActivity <ActivityPlayListBinding, PlayListViewModel> implements SystemBarColorProvider,
        View.OnClickListener,
        CreatePlaylistDialogFragment.AddNewPlaylistDialogCallback,
        MoviePlaylistAdapter.OnMovieClickListener,
        PlaylistItemAdapter.OnPlaylistClickListener,
        UpdatePlaylistDialogFragment.UpdatePlaylistDialogCallback {

    private MoviePlaylistAdapter moviePlaylistAdapter;
    private PlaylistItemAdapter playlistItemAdapter;
    private MoviePlaylistShimmerAdapter moviePlaylistShimmerAdapter;
    private PlaylistShimmerAdapter playlistShimmerAdapter;
    int currentPage = 0;
    int pageSize = 10;
    boolean isLastPage = false;
    private boolean isLoading = false;
    boolean isCreateNewPlayList = false;
    boolean isUpdatePlaylist= false;
    boolean isDeletePlaylist= false;
    private int currentSizePlaylist;
    private boolean isLoaded = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        setUpAdapter();
        showShimmer();
        getListPlayList();

        viewBinding.rvMovie.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) return;

                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm == null || moviePlaylistAdapter == null) return;

                int totalItemCount = lm.getItemCount();
                int lastVisibleItemPosition = lm.findLastVisibleItemPosition();

                if (!isLoading && !isLastPage && lastVisibleItemPosition >= totalItemCount - 3) {
                    isLoading = true;

                    GetListMoviePlayListRequest request = new GetListMoviePlayListRequest();
                    request.setId(viewModel.playListSelect.getId());
                    request.setPage(currentPage);
                    request.setSize(pageSize);
                    getPlayListMovie(request);
                }
            }
        });
    }

    public void showShimmer() {
        viewBinding.shimmerManagePlaylist.setAlpha(Constants.SHIMMER_START_ALPHA);
        viewBinding.shimmerNamePlayList.setAlpha(Constants.SHIMMER_START_ALPHA);
        viewBinding.tvNamePlaylist.setVisibility(View.GONE);
        viewBinding.btnManagePlaylist.setVisibility(View.GONE);

        viewBinding.shimmerManagePlaylist.setVisibility(View.VISIBLE);
        viewBinding.shimmerNamePlayList.setVisibility(View.VISIBLE);

        viewBinding.rvMovie.setAdapter(moviePlaylistShimmerAdapter);
        viewBinding.rvPlaylist.setAdapter(playlistShimmerAdapter);
        showShimmerMovie();
    }

    public void showShimmerMovie() {
        viewBinding.rvMovie.setAdapter(moviePlaylistShimmerAdapter);
    }

    public void hideShimmerMovie() {
        viewBinding.rvMovie.setAdapter(moviePlaylistAdapter);
    }

    public void hideShimmer() {
        viewBinding.tvNamePlaylist.setVisibility(View.VISIBLE);
        viewBinding.btnManagePlaylist.setVisibility(View.VISIBLE);

        viewBinding.shimmerManagePlaylist.setVisibility(View.GONE);
        viewBinding.shimmerNamePlayList.setVisibility(View.GONE);

        hideShimmerMovie();
        viewBinding.rvPlaylist.setAdapter(playlistItemAdapter);
    }
    public void setUpAdapter() {
        // Movie Playlist
        moviePlaylistAdapter = new MoviePlaylistAdapter(this, this);
        moviePlaylistShimmerAdapter = new MoviePlaylistShimmerAdapter(6);

        viewBinding.rvMovie.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));

        // Playlist (horizontal)
        playlistItemAdapter = new PlaylistItemAdapter(this, this);
        playlistShimmerAdapter = new PlaylistShimmerAdapter(6, this);

        viewBinding.rvPlaylist.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));
    }

    public void getListPlayList() {
        showLoading();
        viewModel.getListPlaylist(new MainCallback<List<PlayListResponse>>() {

            @Override
            public void doSuccess(List<PlayListResponse> data) {
                hideLoading();
                if (data == null || data.isEmpty()) {
                    viewBinding.layoutEmptyMain.setVisibility(View.VISIBLE);
                    return;
                }
                viewBinding.layoutEmptyMain.setVisibility(View.GONE);

                currentSizePlaylist = data.size();

                if (viewModel.playList != null && !viewModel.playList.isEmpty()) {
                    viewModel.playList.clear();
                } else {
                    viewModel.playList = new ArrayList<>();
                }
                viewModel.playList.addAll(data);

                if (isUpdatePlaylist) {
                    isUpdatePlaylist = false;
                    viewModel.playListSelect = viewModel.getSelectPlayListUpdate();

                    viewModel.setSelectPlaylist(viewModel.playListSelect.getId());
                    playlistItemAdapter.setData(viewModel.playList);

                    viewBinding.tvNamePlaylist.setText(viewModel.playListSelect.getName());

                    return;
                }

                if (viewModel.playListSelect == null || viewModel.playListSelect.getId() == null) {
                    viewModel.playListSelect = data.get(0);
                    viewModel.setSelectPlaylist(viewModel.playListSelect.getId());
                    viewBinding.tvNamePlaylist.setText(viewModel.playListSelect.getName());

                    if (isCreateNewPlayList) {
                        if (viewModel.playList.size() == 1) {
                            hideShimmer();
                            viewBinding.layoutEmptyMovie.setVisibility(View.VISIBLE);
                        }
                        isCreateNewPlayList = false;
                        playlistItemAdapter.setData(viewModel.playList);
                        return;
                    }

                    GetListMoviePlayListRequest request = new GetListMoviePlayListRequest();
                    request.setId(viewModel.playListSelect.getId());
                    request.setPage(currentPage);
                    request.setSize(pageSize);
                    getPlayListMovie(request);
                } else {
                    if (viewModel.movieList.isEmpty()) {
                        if (isCreateNewPlayList) {
                            isCreateNewPlayList = false;
                            viewModel.setSelectPlaylist(viewModel.playListSelect.getId());
                            playlistItemAdapter.setData(viewModel.playList);
                            viewBinding.tvNamePlaylist.setText(viewModel.playListSelect.getName());

                            return;
                        }
                        viewModel.setSelectPlaylist(viewModel.playListSelect.getId());
                        viewBinding.tvNamePlaylist.setText(viewModel.playListSelect.getName());

                        GetListMoviePlayListRequest request = new GetListMoviePlayListRequest();
                        request.setId(viewModel.playListSelect.getId());
                        request.setPage(currentPage);
                        request.setSize(pageSize);
                        getPlayListMovie(request);
                    } else {
                        viewModel.setSelectPlaylist(viewModel.playListSelect.getId());
                        playlistItemAdapter.setData(viewModel.playList);
                        viewBinding.tvNamePlaylist.setText(viewModel.playListSelect.getName());

                        if (isCreateNewPlayList) {
                            isCreateNewPlayList = false;
                            return;
                        }
                    }
                }

                playlistItemAdapter.setData(viewModel.playList);
            }
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
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        });
    }

    public void getPlayListMovie(GetListMoviePlayListRequest request) {
        isLoading = true;
        showLoading();
        if (currentPage == 0 && isLoaded) {
            viewBinding.layoutEmptyMovie.setVisibility(View.GONE);
        }

        viewModel.getListPlaylistMovie(new MainCallback<ResponseListObj<MovieResponse>>() {
            @Override
            public void doSuccess(ResponseListObj<MovieResponse> data) {
                hideLoading();
                if (data.getContent() != null && !data.getContent().isEmpty()) {
                    viewBinding.layoutEmptyMovie.setVisibility(View.GONE);
                    if (!isLoaded) {
                        hideShimmer();
                        viewModel.movieList.clear();
                        viewModel.movieList.addAll(data.getContent());
                        moviePlaylistAdapter.setData(viewModel.movieList);
                    } else {
                        if (currentPage == 0) {
                            viewModel.movieList.clear();
                            viewModel.movieList.addAll(data.getContent());
                            moviePlaylistAdapter.setData(viewModel.movieList);
                        } else {
                            viewModel.movieList.addAll(data.getContent());
                            moviePlaylistAdapter.addData(data.getContent());
                        }
                    }

                    currentPage++;

                    if (currentPage == data.getTotalPages()) {
                        isLastPage = true;
                    }

                } else {
                    if (currentPage == 0 && !isLoaded) {
                        hideShimmer();
                        viewBinding.layoutEmptyMovie.setVisibility(View.VISIBLE);
                    }

                    if (currentPage == 0) {
                        viewBinding.layoutEmptyMovie.setVisibility(View.VISIBLE);
                    }
                    isLastPage = true;
                }
                isLoaded = true;
                isLoading = false;
            }
            @Override
            public void doError(Throwable error) {
                isLoading = false;
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {
                isLoading = false;
                hideLoading();
            }

            @Override
            public void doFail() {
                isLoading = false;
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, request);
    }

    public void createNewPlayList(CreatePlaylistRequest request) {
        showLoading();
        viewModel.createPlaylist(new MainCallback<PlayListResponse>() {
            @Override
            public void doSuccess(PlayListResponse responseWrapper) {
                hideLoading();
                isCreateNewPlayList = true;
                new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.create_playlist_succes)).showMessage(getApplicationContext());
                getListPlayList();
            }

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
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, request);
    }

    public void updatePlaylist(UpdatePlaylistRequest request) {
        showLoading();
        viewModel.updatePlaylist(new MainCallback<ResponseWrapper>() {
            @Override
            public void doSuccess(ResponseWrapper responseWrapper) {
                hideLoading();
                isUpdatePlaylist = true;
                new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.update_list_success)).showMessage(getApplicationContext());
                getListPlayList();
            }
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
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, request);
    }

    public void deletePlaylist(Long id) {
        showLoading();
        viewModel.deletePlaylist(new MainCallback<ResponseWrapper>() {
            @Override
            public void doSuccess(ResponseWrapper responseWrapper) {
                hideLoading();
                isDeletePlaylist = true;
                new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.delete_list_success)).showMessage(getApplicationContext());

                viewModel.playListSelect = viewModel.getPlaylistNextOrPrevious(viewModel.playListSelect.getId());

                currentPage = 0;
                isLastPage = false;
                viewModel.movieList = new ArrayList<>();

                getListPlayList();
            }
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
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, id);
    }

    public void removeItemFromPlaylist(RemoveItemPlaylistRequest request) {
        showLoading();
        viewModel.removeItemPlaylist(new MainCallback<ResponseWrapper>() {
            @Override
            public void doSuccess(ResponseWrapper responseWrapper) {
                hideLoading();
                getListPlayList();
            }
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
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, request);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_play_list;
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public int getStatusBarColor() {
        return R.color.header_app;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_app;
    }

    public void showBottomSheetCreateNew() {
        new CreatePlaylistDialogFragment(this)
                .show(getSupportFragmentManager(), "CreatePlaylistDialog");
    }

    public void showBottomSheetUpdatePlayList() {
        new UpdatePlaylistDialogFragment(this, viewModel.playListSelect)
                .show(getSupportFragmentManager(), "UpdatePlaylistDialog");
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add_new:
                if (currentSizePlaylist >= Constants.MaxPlaylist) {
                    new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.limit_list)).showMessage(this);
                    return;
                }
                showBottomSheetCreateNew();
                break;
            case R.id.btn_manage_playlist:
                showBottomSheetUpdatePlayList();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCreateClicked(CreatePlaylistRequest createPlaylistRequest) {
        createNewPlayList(createPlaylistRequest);
    }

    @Override
    public void onMovieClick(MovieResponse movie) {
        showLoading();
        getMovie(movie);
    }

    @Override
    public void onDeleteClick(MovieResponse movieResponse) {
        RemoveItemPlaylistRequest request = new RemoveItemPlaylistRequest();
        request.setMovieId(movieResponse.getId());
        request.setPlaylistId(viewModel.playListSelect.getId());
        viewModel.movieList.remove(movieResponse);
        if (viewModel.movieList.isEmpty()) {
            viewBinding.layoutEmptyMovie.setVisibility(View.VISIBLE);
        }
        removeItemFromPlaylist(request);
    }

    @Override
    public void onPlaylistClick(PlayListResponse playlist) {
        viewModel.playListSelect = playlist;
        viewModel.setSelectPlaylist(viewModel.playListSelect.getId());
        viewBinding.tvNamePlaylist.setText(viewModel.playListSelect.getName());

        currentPage = 0;
        isLastPage = false;
        isLoading = true;

        GetListMoviePlayListRequest request = new GetListMoviePlayListRequest();
        request.setId(viewModel.playListSelect.getId());
        request.setPage(currentPage);
        request.setSize(pageSize);
        getPlayListMovie(request);
    }

    @Override
    public void onUpdateClicked(UpdatePlaylistRequest request) {
        updatePlaylist(request);
    }

    @Override
    public void onDeleteClicked(Long idPlaylist) {
        deletePlaylist(idPlaylist);
    }

    public void getMovie(MovieResponse movie) {
        showLoading();

        viewModel.getMovie(new MainCallback<MovieResponse>() {
            @Override
            public void doError(Throwable throwable) {
                hideLoading();
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doSuccess(MovieResponse movieResponse) {
                if (viewModel.isLogin()) {
                    getListMovieTracking(movieResponse);
                } else {
                    Intent intent = new Intent(PlayListActivity.this, MovieDetailActivity.class);
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
        showLoading();
        viewModel.getListMovieTracking(new MainCallback<ListWatchHistoryResponse>() {
            @Override
            public void doError(Throwable throwable) {
                hideLoading();
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.fetch_data_failed));
            }

            @Override
            public void doSuccess(ListWatchHistoryResponse list) {
                Intent intent = new Intent(PlayListActivity.this, MovieDetailActivity.class);
                intent.putExtra("movie_details", GsonUtils.toJson(movie));
                intent.putExtra("movie_details_tracking", GsonUtils.toJson(list));
                startActivity(intent);
            }

            @Override
            public void doSuccess() {
            }
        }, movie.getId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideLoading();
        if (isLoaded) {
            getListPlayList();
        }
    }
}
