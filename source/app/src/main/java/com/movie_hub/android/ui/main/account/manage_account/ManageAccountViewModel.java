package com.movie_hub.android.ui.main.account.manage_account;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.login.UserRegisterRequest;
import com.movie_hub.android.data.model.api.request.user.UserChangePasswordRequest;
import com.movie_hub.android.data.model.api.request.user.UserUpdateProfileRequest;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.api.response.user.UserUploadImageResponse;
import com.movie_hub.android.data.model.mapper.UserMapper;
import com.movie_hub.android.data.model.room.UserEntity;
import com.movie_hub.android.data.remote.UploadApiService;
import com.movie_hub.android.ui.base.activity.BaseViewModel;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.utils.NetworkUtils;

import java.io.File;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;
import timber.log.Timber;

public class ManageAccountViewModel extends BaseViewModel {
    public ManageAccountViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
    private final MutableLiveData<UserResponse> currentUser = new MutableLiveData<>();

    public LiveData<UserResponse> getCurrentUserLiveData() {
        return currentUser;
    }

    @SuppressLint("CheckResult")
    public void getUser() {
        compositeDisposable.add(
                repository.getRoomService().userDao().getCurrentUser()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(user -> {
                            UserResponse response = UserMapper.toResponse(user);
                            currentUser.setValue(response);
                        }, throwable -> {
                        })
        );
    }

    public void getUserProfile(MainCallback<UserResponse> callback) {
        compositeDisposable.add(repository.getMasterApiService().getUserProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
    public void userChangePassWord(MainCallback<ResponseWrapper> callback, UserChangePasswordRequest request) {
        showLoading();
        compositeDisposable.add(repository.getMasterApiService().changeUserPassword(request)
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
                            if (response.isResult()) {
                                callback.doSuccess(response);
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
    @SuppressLint("CheckResult")
    public void userChangeInformation(MainCallback<ResponseWrapper> callback, UserUpdateProfileRequest request) {
        showLoading();
        compositeDisposable.add(repository.getMasterApiService().updateUserProfile(request)
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
                            if (response.isResult()) {
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
                                                profile -> {
                                                    if (profile.isResult()) {
                                                        UserEntity entity = UserMapper.fromResponse(profile.getData());
                                                        compositeDisposable.add(
                                                                repository.getRoomService().userDao().insert(entity)
                                                                        .subscribeOn(Schedulers.io())
                                                                        .subscribe(() -> {
                                                                        }, throwable -> {
                                                                        })
                                                        );

                                                        callback.doSuccess(response);

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

    public void uploadAvatar(File file, MainCallback<UserUploadImageResponse> callback) {
        RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        RequestBody typeBody = RequestBody.create("AVATAR", MediaType.parse("text/plain"));

        UploadApiService.getInstance().getApi().uploadAvatar(body, typeBody)
                .enqueue(new Callback<ResponseWrapper<UserUploadImageResponse>>() {
                    @Override
                    public void onResponse(Call<ResponseWrapper<UserUploadImageResponse>> call, Response<ResponseWrapper<UserUploadImageResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isResult()) {


                            callback.doSuccess(response.body().getData());
                        } else {
                            callback.doFail();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseWrapper<UserUploadImageResponse>> call, Throwable t) {
                        callback.doError(t);
                    }
                });
    }


}
