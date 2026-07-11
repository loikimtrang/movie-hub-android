package com.movie_hub.android.ui.main.account.updateapp;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.appversion.CheckAppVersionRequest;
import com.movie_hub.android.data.model.api.response.appversion.CheckAppVersionResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.mapper.UserMapper;
import com.movie_hub.android.data.model.room.UserEntity;
import com.movie_hub.android.ui.base.activity.BaseViewModel;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.utils.NetworkUtils;

import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class CheckUpdateViewModel extends BaseViewModel {
    CheckAppVersionResponse checkAppVersionResponse = new CheckAppVersionResponse();
    public CheckUpdateViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void checkUpdate(MainCallback<ResponseWrapper<CheckAppVersionResponse>> callback, CheckAppVersionRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(repository.getApiService().checkVersion(query)
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
                             callback.doSuccess(response);
                        }, throwable -> {
                            Timber.e(throwable);
                            callback.doError(throwable);
                        }
                )
        );
    }
}
