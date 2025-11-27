package com.movie_hub.android.ui.main.movie.detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.favourite.CreateFavouriteRequest;
import com.movie_hub.android.data.model.api.request.history.ListWatchHistoryRequest;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.favourite.FavouriteResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.ui.base.activity.BaseViewModel;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.utils.NetworkUtils;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.Getter;
import lombok.Setter;
import timber.log.Timber;

public class MovieDetailViewModel extends BaseViewModel {
    public MovieResponse movieDetails;
    public MovieItemResponse remainingEpisode;
    public MutableLiveData<ListWatchHistoryResponse> movieDetailsTracking = new MutableLiveData<>();
    private MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(true);
    public Boolean isFavourite = false;
    public MutableLiveData<FavouriteResponse> favouriteResponseFirst = new MutableLiveData<>();

    public FavouriteResponse favourite = new FavouriteResponse();

    public MovieDetailViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public LiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying.setValue(playing);
    }
    public String getTokenVideo() {
        return "Bearer " + repository.getSharedPreferences().getToken();
    }

    public void applyWatchHistory(ListWatchHistoryResponse history) {
        if (movieDetails != null) {
            movieDetails.applyWatchHistory(history);
        }
    }


    public void createFavorite(CreateFavouriteRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(repository.getApiService().createFavourite(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable ->
                        throwable.flatMap((Function<Throwable, ObservableSource<?>>) throwable1 -> {
                            if (NetworkUtils.checkNetworkError(throwable1)) {
                                return application.showDialogNoInternetAccess();
                            } else{
                                return Observable.error(throwable1);
                            }
                        })
                )
                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                favourite.setId(response.getData());
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                        }
                )
        );
    }
    public void deleteFavorite(Long id) {
        compositeDisposable.add(repository.getApiService().deleteFavourite(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable ->
                        throwable.flatMap((Function<Throwable, ObservableSource<?>>) throwable1 -> {
                            if (NetworkUtils.checkNetworkError(throwable1)) {
                                return application.showDialogNoInternetAccess();
                            }else{
                                return Observable.error(throwable1);
                            }
                        })
                )
                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                favourite.setId(null);
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                        }
                )
        );
    }

    public void getFavourite(CreateFavouriteRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(repository.getApiService().getFavourite(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable ->
                        throwable.flatMap((Function<Throwable, ObservableSource<?>>) throwable1 -> {
                            if (NetworkUtils.checkNetworkError(throwable1)) {
                                return application.showDialogNoInternetAccess();
                            } else{
                                return Observable.error(throwable1);
                            }
                        })
                )
                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                favouriteResponseFirst.postValue(response.getData());
                                favourite.setId(response.getData().getId());
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                        }
                )
        );
    }

    public void getListMovieTracking(long movieId) {
        ListWatchHistoryRequest request = new ListWatchHistoryRequest();
        request.setMovieId(movieId);

        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(repository.getApiService().getListWatchHistory(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable ->
                        throwable.flatMap((Function<Throwable, ObservableSource<?>>) throwable1 -> {
                            if (NetworkUtils.checkNetworkError(throwable1)) {
                                hideLoading();
                                return application.showDialogNoInternetAccess();
                            } else {
                                return Observable.error(throwable1);
                            }
                        })
                )
                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                movieDetailsTracking.postValue(response.getData());
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                        }
                )
        );
    }
}
