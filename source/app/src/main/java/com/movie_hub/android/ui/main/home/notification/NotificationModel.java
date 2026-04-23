package com.movie_hub.android.ui.main.home.notification;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.request.notification.NotificationRequest;
import com.movie_hub.android.data.model.api.response.history.MovieHistoryResponse;
import com.movie_hub.android.data.model.api.response.notification.NotificationResponse;
import com.movie_hub.android.ui.base.activity.BaseViewModel;
import com.movie_hub.android.ui.main.MainCallback;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class NotificationModel extends BaseViewModel {
    public NotificationModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void getListMovieHistory(MainCallback<ResponseListObj<NotificationResponse>> callback, NotificationRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(
                repository.getApiService().getNotifications(query)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
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
}
