package com.movie_hub.android.ui.main;

import android.annotation.SuppressLint;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.category.CategoryRequest;
import com.movie_hub.android.data.model.api.request.history.ListWatchHistoryRequest;
import com.movie_hub.android.data.model.api.request.login.UserLoginRequest;
import com.movie_hub.android.data.model.api.request.login.UserRegisterRequest;
import com.movie_hub.android.data.model.api.request.user.UserLoginGoogleRequest;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.login.UserLoginResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.room.RoomResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.mapper.UserMapper;
import com.movie_hub.android.data.model.onesignal.MessageOneSignal;
import com.movie_hub.android.data.model.room.UserEntity;
import com.movie_hub.android.ui.base.activity.BaseViewModel;
import com.movie_hub.android.utils.NetworkUtils;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;
import timber.log.Timber;

public class MainViewModel extends BaseViewModel {

    MessageOneSignal messageOneSignal = new MessageOneSignal();
    String msgCommentData;
    public MainViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
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

    public void getListCategory(MainCallback<List<CategoryResponse>> callback, CategoryRequest request) {
        request.setSize(1000);
        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(repository.getApiService().getListCategory(query)
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
                                callback.doSuccess(response.getData().getContent());
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

    public void getRoomByCode(MainCallback<RoomResponse> callback, String code) {
        compositeDisposable.add(repository.getApiService().getRoomByCode(code)
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
                                callback.doSuccess(response.getData());
                            } else {
                                callback.doFail();
                            }
                        },
                        throwable -> {
                            Timber.e(throwable);
                            callback.doError(throwable);
                        }
                )
        );
    }

    public void getRoom(MainCallback<RoomResponse> callback, Long roomId) {
        compositeDisposable.add(repository.getApiService().getRoom(roomId)
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
                                callback.doSuccess(response.getData());
                            } else {
                                callback.doFail();
                            }
                        },
                        throwable -> {
                            Timber.e(throwable);
                            callback.doError(throwable);
                        }
                )
        );
    }

    public void joinRoom(MainCallback<RoomResponse> callback, Long roomId) {
        compositeDisposable.add(repository.getApiService().joinRoom(roomId)
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
                            hideLoading();
                            if (response.isResult()) {
                                callback.doSuccess(response.getData());
                            } else {
                                callback.doFail();
                            }
                        },
                        throwable -> {
                            hideLoading();
                            Timber.e(throwable);
                            callback.doError(throwable);
                        }
                )
        );
    }

    public void startRoom(MainCallback<RoomResponse> callback, Long id) {
        compositeDisposable.add(repository.getApiService().startRoom(id)
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
                                callback.doSuccess(response.getData());
                            } else {
                                callback.doFail();
                            }
                        },
                        throwable -> {
                            Timber.e(throwable);
                            callback.doError(throwable);
                        }
                )
        );
    }
}
