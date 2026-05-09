package com.movie_hub.android.ui.main.live;


import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.collection.CollectionItemRequest;
import com.movie_hub.android.data.model.api.request.room.RoomRequest;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.room.RoomResponse;
import com.movie_hub.android.ui.base.fragment.BaseFragmentViewModel;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.utils.NetworkUtils;

import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class LiveViewModel extends BaseFragmentViewModel {

    public LiveViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void getListRoom(MainCallback<ResponseListObj<RoomResponse>> callback, RoomRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(
                repository.getApiService().getListRoom(query)
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

    public void getListMyRoom(MainCallback<ResponseListObj<RoomResponse>> callback, RoomRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(
                repository.getApiService().getListMyRoom(query)
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

    public void deleteRoom(MainCallback<Void> callback, Long roomId) {
        compositeDisposable.add(
                repository.getApiService().deleteRoom(roomId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retryWhen(throwable ->
                                throwable.flatMap((Function<Throwable, ObservableSource<?>>) throwable1 -> {
                                    if (NetworkUtils.checkNetworkError(throwable1)) {
                                        return application.showDialogNoInternetAccess();
                                    } else {
                                        return Observable.error(throwable1);
                                    }
                                })
                        )
                        .subscribe(
                                (ResponseWrapper response) -> {
                                    if (response.isResult()) {
                                        callback.doSuccess();
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
        compositeDisposable.add(
                repository.getApiService().joinRoom(roomId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retryWhen(throwable ->
                                throwable.flatMap((Function<Throwable, ObservableSource<?>>) throwable1 -> {
                                    if (NetworkUtils.checkNetworkError(throwable1)) {
                                        return application.showDialogNoInternetAccess();
                                    } else {
                                        return Observable.error(throwable1);
                                    }
                                })
                        )
                        .subscribe(
                                (response) -> {
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
                        }, throwable -> {
                            Timber.e(throwable);
                            callback.doError(throwable);
                        }
                )
        );
    }
}
