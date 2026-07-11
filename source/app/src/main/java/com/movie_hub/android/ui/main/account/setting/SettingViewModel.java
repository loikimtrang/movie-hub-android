package com.movie_hub.android.ui.main.account.setting;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.setting.UserSettingsRequest;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.ui.base.activity.BaseViewModel;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.data.local.prefs.PreferencesService;
import com.movie_hub.android.utils.GsonUtils;
import com.movie_hub.android.utils.NetworkUtils;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;
import timber.log.Timber;

public class SettingViewModel extends BaseViewModel {
    UserSettingsRequest setting = new UserSettingsRequest();
    UserResponse userResponse = new UserResponse();
    public SettingViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void updateSetting(MainCallback<ResponseWrapper> callback, UserSettingsRequest request) {
        showLoading();
        compositeDisposable.add(repository.getMasterApiService().updateUserSetting(request)
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
                                persistUserSettings(request);
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

    private void persistUserSettings(UserSettingsRequest request) {
        if (request == null || userResponse == null || userResponse.getId() <= 0L) {
            return;
        }
        String settingsJson = GsonUtils.toJson(request);
        userResponse.setSettings(settingsJson);
        repository.getSharedPreferences().setString(
                PreferencesService.KEY_USER_SETTING + userResponse.getId(),
                settingsJson);
    }
}
