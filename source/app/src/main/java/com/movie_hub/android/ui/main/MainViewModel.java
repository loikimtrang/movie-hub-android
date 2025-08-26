package com.movie_hub.android.ui.main;

import android.annotation.SuppressLint;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.login.UserLoginRequest;
import com.movie_hub.android.data.model.api.request.login.UserRegisterRequest;
import com.movie_hub.android.data.model.api.request.user.UserLoginGoogleRequest;
import com.movie_hub.android.data.model.api.response.login.UserLoginResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.mapper.UserMapper;
import com.movie_hub.android.data.model.room.UserEntity;
import com.movie_hub.android.ui.base.activity.BaseViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;
import timber.log.Timber;

public class MainViewModel extends BaseViewModel {

    public MainViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void userLogin(MainCallback<UserLoginResponse> callback, UserLoginRequest request) {
        showLoading();
        compositeDisposable.add(repository.getApiService().userLogin(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            hideLoading();
                            if (response.getAccess_token() != null) {
                                repository.getSharedPreferences().setToken(response.getAccess_token());
                                repository.getSharedPreferences().saveAccessTokenObject(response);
                                compositeDisposable.add(repository.getApiService().getUserProfile()
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
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
                .subscribe(
                        response -> {
                            hideLoading();
                            if (response.getAccess_token() != null) {
                                repository.getSharedPreferences().setToken(response.getAccess_token());
                                repository.getSharedPreferences().saveAccessTokenObject(response);
                                compositeDisposable.add(repository.getApiService().getUserProfile()
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
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

    public void userRegister(MainCallback<ResponseWrapper> callback, UserRegisterRequest request) {
        showLoading();
        compositeDisposable.add(repository.getApiService().userRegister(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            hideLoading();
                            callback.doSuccess(response);
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
    @SuppressLint("CheckResult")
    public void userSignOut() {
        repository.getSharedPreferences().clearAuthData();

        compositeDisposable.add(
                repository.getRoomService().userDao().clear()
                        .subscribeOn(Schedulers.io())
                        .subscribe(() -> {
                        }, throwable -> {
                        })
        );
    }


    public void getUserProfile(MainCallback<UserResponse> callback) {
        compositeDisposable.add(repository.getApiService().getUserProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                repository.getSharedPreferences().setUserId(response.getData().getId());
                                UserEntity entity = UserMapper.fromResponse(response.getData());
                                compositeDisposable.add(

                                        repository.getRoomService().userDao().insert(entity)
                                                .subscribeOn(Schedulers.io())
                                                .subscribe(() -> {
                                                }, throwable -> {
                                                })
                                );
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
