package com.movie_hub.android.ui.main.account.forgot_password;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.forgot.ForgotChangePasswordRequest;
import com.movie_hub.android.data.model.api.request.forgot.ForgotPasswordRequest;
import com.movie_hub.android.data.model.api.request.otp.VerifyOtpRequest;
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

public class ForgotPasswordViewModel extends BaseViewModel {
    public ForgotPasswordViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void resendOtp(MainCallback<ResponseWrapper> callback, VerifyOtpRequest request) {
        compositeDisposable.add(repository.getMasterApiService().resendOtp(request)
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
                                callback.doSuccess(response);
                            } else {
                                callback.doFail();
                            }
                        },
                        throwable -> {
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

    public void requestForgotPassword(MainCallback<ResponseWrapper> callback, ForgotPasswordRequest request) {
        showLoading();
        compositeDisposable.add(repository.getMasterApiService().requestForgotPassword(request)
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
                                callback.doSuccess(response);
                            } else {
                                callback.doFail();
                            }
                        },
                        throwable -> {
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

    public void changePassword(MainCallback<ResponseWrapper> callback, ForgotChangePasswordRequest request) {
        compositeDisposable.add(repository.getMasterApiService().forgotPassword(request)
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
                            callback.doSuccess(response);
                        },
                        throwable -> {
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
