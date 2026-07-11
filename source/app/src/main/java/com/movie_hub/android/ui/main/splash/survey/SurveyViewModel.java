package com.movie_hub.android.ui.main.splash.survey;

import androidx.lifecycle.MutableLiveData;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.movie.MovieRequest;
import com.movie_hub.android.data.model.api.request.side_bar.SideBarRequest;
import com.movie_hub.android.data.model.api.request.survey.MakeSurveyRequest;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.side_bar.SidebarResponse;
import com.movie_hub.android.ui.base.activity.BaseViewModel;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.utils.NetworkUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class SurveyViewModel extends BaseViewModel {
    public MakeSurveyRequest makeSurveyRequest = new MakeSurveyRequest();
    public MutableLiveData<List<SidebarResponse>> movieBannerList = new MutableLiveData<>();

    public SurveyViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void getListMovieSurvey(MainCallback<List<MovieResponse>> callback) {
        showLoading();
        Map<String, Object> query = new HashMap<>();

        compositeDisposable.add(
                repository.getApiService().getListMovieSurvey(query)
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

    public void makeSurvey(MainCallback<ResponseWrapper> callback, MakeSurveyRequest request) {
        showLoading();
        compositeDisposable.add(
                repository.getApiService().makeSurvey(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
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
