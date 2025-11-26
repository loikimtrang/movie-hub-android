package com.movie_hub.android.ui.main.movie.watch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.favourite.CreateFavouriteRequest;
import com.movie_hub.android.data.model.api.request.history.ListWatchHistoryRequest;
import com.movie_hub.android.data.model.api.request.history.TrackingWatchHistoryRequest;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.video.VideoResponse;
import com.movie_hub.android.ui.base.activity.BaseViewModel;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.movie.watch.setting.SettingVideoModel;
import com.movie_hub.android.ui.main.movie.watch.setting.VideoQuality;
import com.movie_hub.android.utils.NetworkUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.exceptions.Exceptions;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class WatchMovieViewModel extends BaseViewModel {
    public MovieResponse movieDetails;
    public String nowUriPlay;
    public MovieItemResponse nowEpisodePlay;
    public VideoResponse nowVideoPlay; // Single movie

    private MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(true);
    public MutableLiveData<List<ListWatchHistoryResponse>> movieDetailsTracking = new MutableLiveData<>();

    SettingVideoModel settingVideoModel = new SettingVideoModel();
    public WatchMovieViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
        settingVideoModel.initSetting();
    }

    public LiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying.setValue(playing);
    }

    public void updateSettingWhenChangeEpisode() {
        settingVideoModel.getQuality().setAuto(true);
        settingVideoModel.getQuality().setResolution(new VideoQuality());
        settingVideoModel.getAvailableQualities().clear();
    }
    public String getTokenVideo() {
        return "Bearer " + repository.getSharedPreferences().getToken();
    }

    public void updateTrackingMovie(TrackingWatchHistoryRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(repository.getApiService().updateHistory(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(errors ->
                        errors
                                .zipWith(Observable.range(1, 3), (Throwable error, Integer retryCount) -> {
                                    if (NetworkUtils.checkNetworkError(error)) {
                                        return retryCount;
                                    } else {
                                        throw Exceptions.propagate(error);
                                    }
                                })
                                .flatMap(retryCount -> {
                                    return Observable.timer(3, TimeUnit.SECONDS);
                                })
                )

                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                getListMovieTracking(movieDetails.getId());
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
                .retryWhen(errors ->
                        errors
                                .zipWith(Observable.range(1, 3), (Throwable error, Integer retryCount) -> {
                                    if (NetworkUtils.checkNetworkError(error)) {
                                        return retryCount;
                                    } else {
                                        throw Exceptions.propagate(error);
                                    }
                                })
                                .flatMap(retryCount -> {
                                    return Observable.timer(3, TimeUnit.SECONDS);
                                })
                )
                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                movieDetailsTracking.postValue(response.getData().getContent());
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                        }
                )
        );
    }

}
