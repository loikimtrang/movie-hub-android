package com.movie_hub.android.ui.main.splash;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.local.prefs.PreferencesService;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.appversion.CheckAppVersionRequest;
import com.movie_hub.android.data.model.api.request.comment.CommentRequest;
import com.movie_hub.android.data.model.api.request.movie.MovieRequest;
import com.movie_hub.android.data.model.api.request.side_bar.SideBarRequest;
import com.movie_hub.android.data.model.api.response.appversion.CheckAppVersionResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.side_bar.SidebarResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.mapper.UserMapper;
import com.movie_hub.android.data.model.onesignal.MessageOneSignal;
import com.movie_hub.android.data.model.room.UserEntity;
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
import timber.log.Timber;

public class SplashViewModel extends BaseViewModel {
    CheckAppVersionResponse checkAppVersionResponse = new CheckAppVersionResponse();
    String messageOneSignal;
    public SplashViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void userSignOut(MainCallback<Void> callback) {
        repository.getSharedPreferences().clearAuthData();

        compositeDisposable.add(
                repository.getRoomService().userDao().clear()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    callback.doSuccess();
                                },
                                throwable -> {
                                    Timber.e(throwable, "Sign out: Failed to clear user data from DB");
                                    callback.doError(throwable);
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
                            } else {
                                return Observable.error(throwable1);
                            }
                        })
                )
                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                application.setOneSignalExternalId(String.valueOf(response.getData().getId()));

                                repository.getSharedPreferences().setUserId(response.getData().getId());
                                if (response.getData().getSettings() != null && !response.getData().getSettings().isEmpty()) {
                                    repository.getSharedPreferences().setString(PreferencesService.KEY_USER_SETTING + response.getData().getId(), response.getData().getSettings());
                                } else {
                                    repository.getSharedPreferences().setString(PreferencesService.KEY_USER_SETTING + response.getData().getId(), "");
                                }
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

    public void checkUpdate(MainCallback<ResponseWrapper<CheckAppVersionResponse>> callback, CheckAppVersionRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        showLoading();
        compositeDisposable.add(repository.getApiService().checkVersion(query)
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
                        }, throwable -> {
                            Timber.e(throwable);
                            callback.doError(throwable);
                        }
                )
        );
    }

    public void getListMovie(MainCallback<List<MovieResponse>> callback, MovieRequest request) {
        showLoading();
        Map<String, Object> query = RequestToMapConverter.convert(request);

        compositeDisposable.add(
                repository.getApiService().getListMovie(query)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    hideLoading();
                                    if (response.isResult()) {
                                        callback.doSuccess(response.getData().getContent());
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

    public void getListSideBar(MainCallback<ResponseListObj<SidebarResponse>> callback, SideBarRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        request.setSize(1000);
        compositeDisposable.add(repository.getApiService().getSideBarList(query)
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
