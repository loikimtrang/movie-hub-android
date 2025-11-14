package com.movie_hub.android.ui.main.account.login;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.request.login.UserLoginRequest;
import com.movie_hub.android.data.model.api.request.user.UserLoginGoogleRequest;
import com.movie_hub.android.data.model.api.response.login.UserLoginResponse;
import com.movie_hub.android.data.model.mapper.UserMapper;
import com.movie_hub.android.data.model.room.UserEntity;
import com.movie_hub.android.ui.base.activity.BaseViewModel;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.utils.NetworkUtils;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;
import timber.log.Timber;

public class LoginViewModel extends BaseViewModel {
    public LoginViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
    public void userLogin(UserLoginRequest request, MainCallback<UserLoginResponse> callback) {
        showLoading();
        compositeDisposable.add(repository.getApiService().userLogin(request)
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
                            hideLoading();
                            if (response.getAccess_token() != null) {
                                repository.getSharedPreferences().setToken(response.getAccess_token());
                                repository.getSharedPreferences().saveAccessTokenObject(response);

                                compositeDisposable.add(repository.getApiService().getUserProfile()
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
                                                user -> {
                                                    if (user.isResult()) {
                                                        UserEntity entity = UserMapper.fromResponse(user.getData());

                                                        compositeDisposable.add(
                                                                repository.getRoomService().userDao().insert(entity)
                                                                        .subscribeOn(Schedulers.io())
                                                                        .subscribe(() -> {
                                                                            repository.getSharedPreferences().setUserId(response.getUser_id());
                                                                            callback.doSuccess(response);
                                                                        }, throwable -> {
                                                                        })
                                                        );
                                                    } else {
                                                        callback.doFail();
                                                    }
                                                }, throwable -> {
                                                    Timber.e(throwable);
                                                    callback.doError(throwable);
                                                }
                                        )
                                );
                            } else {
                                callback.doFail();
                            }
                        },
                        throwable -> {
                            hideLoading();
                            Timber.e(throwable);

                            if (throwable instanceof HttpException) {
                                HttpException httpException = (HttpException) throwable;
                                if (httpException.code() == 400) {
                                    callback.doFail();
                                } else {
                                    callback.doError(throwable);
                                }
                            } else {
                                callback.doError(throwable);
                            }
                        }
                )
        );
    }
    public void userLoginGoogle(MainCallback<UserLoginResponse> callback, UserLoginGoogleRequest request) {
        showLoading();
        compositeDisposable.add(repository.getApiService().userLoginGoogle(request)
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
                            hideLoading();
                            if (response.getAccess_token() != null) {
                                repository.getSharedPreferences().setToken(response.getAccess_token());
                                repository.getSharedPreferences().saveAccessTokenObject(response);
                                compositeDisposable.add(repository.getApiService().getUserProfile()
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
                                                user -> {
                                                    if (user.isResult()) {
                                                        UserEntity entity = UserMapper.fromResponse(user.getData());
                                                        compositeDisposable.add(
                                                                repository.getRoomService().userDao().insert(entity)
                                                                        .subscribeOn(Schedulers.io())
                                                                        .subscribe(() -> {
                                                                            repository.getSharedPreferences().setUserId(response.getUser_id());
                                                                            callback.doSuccess(response);
                                                                        }, throwable -> {
                                                                        })
                                                        );
                                                    } else {
                                                        callback.doFail();
                                                    }
                                                }, throwable -> {
                                                    Timber.e(throwable);
                                                    callback.doError(throwable);
                                                }
                                        )
                                );
                            } else {
                                callback.doFail();
                            }
                        },
                        throwable -> {
                            hideLoading();
                            Timber.e(throwable);

                            if (throwable instanceof HttpException) {
                                HttpException httpException = (HttpException) throwable;
                                if (httpException.code() == 400) {
                                    callback.doFail();
                                } else {
                                    callback.doError(throwable);
                                }
                            } else {
                                callback.doError(throwable);
                            }
                        }
                )
        );
    }
}
