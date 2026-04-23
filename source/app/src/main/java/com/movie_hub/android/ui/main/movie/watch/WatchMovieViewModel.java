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
import com.movie_hub.android.data.model.api.request.setting.UserSettingsRequest;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
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
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.Getter;
import timber.log.Timber;

public class WatchMovieViewModel extends BaseViewModel {
    public MovieResponse movieDetails;
    public String nowUriPlay;
    public MovieItemResponse nowEpisodePlay;
    public VideoResponse nowVideoPlay; // Single movie

    public UserSettingsRequest setting = new UserSettingsRequest();

    private MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(true);
    public MutableLiveData<ListWatchHistoryResponse> movieDetailsTracking = new MutableLiveData<>();
    public long lastPlaybackPosition = 0L;

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
    @Getter
    private String tokenVideo;

    public void setTokenVideo(String token) {
        this.tokenVideo = "Bearer " + token;
    }

    private Disposable tokenRefreshDisposable;

    public void startTokenAutoRefresh() {
        if (isLogin()) {
            setTokenVideo(repository.getToken());
            tokenReady.postValue(true);
            Timber.d("👤 User đã đăng nhập → dùng token login, không auto refresh.");
            return;
        }

        // Nếu chưa login → dùng anonymous token và auto refresh mỗi 10 phút
        getAnonymousToken();

        if (tokenRefreshDisposable != null && !tokenRefreshDisposable.isDisposed()) return;

        tokenRefreshDisposable = Observable.interval(10, 10, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    Timber.d("🔄 Refreshing anonymous token...");
                    getAnonymousToken();
                });
    }


    public void stopTokenAutoRefresh() {
        if (tokenRefreshDisposable != null && !tokenRefreshDisposable.isDisposed()) {
            tokenRefreshDisposable.dispose();
        }
    }

    private final MutableLiveData<Boolean> tokenReady = new MutableLiveData<>();

    public LiveData<Boolean> getTokenReady() {
        return tokenReady;
    }

    public void getAnonymousToken() {
        if (isLogin()) {
            setTokenVideo(repository.getToken());
            tokenReady.postValue(true);
            return;
        }

        compositeDisposable.add(repository.getMasterApiService().getAnonymousToken()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable ->
                        throwable.flatMap(throwable1 -> {
                            if (NetworkUtils.checkNetworkError(throwable1)) {
                                return application.showDialogNoInternetAccess();
                            } else {
                                return Observable.error(throwable1);
                            }
                        })
                )
                .subscribe(
                        response -> {
                            String token = response.getAccessToken();
                            setTokenVideo(token);
                            Timber.d("✅ Token đã sẵn sàng: %s", token);
                            tokenReady.postValue(true);
                        },
                        throwable -> {
                            Timber.e(throwable, "❌ Lỗi khi gọi anonymous token");
                            tokenReady.postValue(false); // hoặc xử lý retry
                        }
                ));
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
                                movieDetailsTracking.postValue(response.getData());
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                        }
                )
        );
    }


    public void getUserProfile(MainCallback<UserResponse> callback) {
        compositeDisposable.add(repository.getMasterApiService().getUserProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable ->
                        throwable.flatMap((Function<Throwable, ObservableSource<?>>) throwable1 -> {
                            if (NetworkUtils.checkNetworkError(throwable1)) {
                                hideLoading();
                                return application.showDialogNoInternetAccess();
                            }else{
                                return Observable.error(throwable1);
                            }
                        })
                )
                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                callback.doSuccess(response.getData());
                            } else {
                                callback.doFail();
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                            callback.doError(throwable);
                        }
                )
        );
    }
}
