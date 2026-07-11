package com.movie_hub.android.ui.main.home.topic.topic_detail;

import androidx.lifecycle.MutableLiveData;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.request.collection.CollectionItemRequest;
import com.movie_hub.android.data.model.api.request.history.ListWatchHistoryRequest;
import com.movie_hub.android.data.model.api.response.collection.CollectionResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
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

public class HomeTopicDetailViewModel extends BaseViewModel {
    CollectionResponse collectionResponse = new CollectionResponse();
    MutableLiveData<List<MovieResponse>> listMovieResponse = new MutableLiveData<>();
    public HomeTopicDetailViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void getListCollectionItem(MainCallback<ResponseListObj<MovieResponse>> callback, CollectionItemRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(
                repository.getApiService().getCollectionItemList(query)
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

    public void getMovie(MainCallback<MovieResponse> callback, Long id) {
        compositeDisposable.add(repository.getApiService().getMovie(id)
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
                            if (response.isResult()) {
                                callback.doSuccess(response.getData());
                            } else {
                                callback.doFail();
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                            hideLoading();
                            callback.doError(throwable);
                        }
                )
        );
    }

    public void getListMovieTracking(MainCallback<ListWatchHistoryResponse> callback, long movieId) {
        ListWatchHistoryRequest request = new ListWatchHistoryRequest();
        request.setMovieId(movieId);

        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(repository.getApiService().getListWatchHistory(query)
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
