package com.movie_hub.android.ui.main.search.topTrending;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.request.history.ListWatchHistoryRequest;
import com.movie_hub.android.data.model.api.request.movie.MovieRequest;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.mapper.UserMapper;
import com.movie_hub.android.data.model.room.SearchHistoryEntity;
import com.movie_hub.android.ui.base.fragment.BaseFragmentViewModel;
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

public class SearchTopTrendingViewModel extends BaseFragmentViewModel {

    private final MutableLiveData<List<SearchHistoryEntity>> listHistory = new MutableLiveData<>();
    public SearchTopTrendingViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public LiveData<List<SearchHistoryEntity>> getHistoriesLiveData() {
        return listHistory;
    }

    @SuppressLint("CheckResult")
    public void getHistory() {
        if (!isLogin()) return;
        compositeDisposable.add(
                repository.getRoomService().searchHistoryDao().getRecentHistory(repository.getSharedPreferences().getUserId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(listHistory::setValue, throwable -> {
                        })
        );

    }
//    public void insertSearchKeyword(String keyword) {
//        SearchHistoryEntity entity = new SearchHistoryEntity();
//        entity.userId = repository.getSharedPreferences().getUserId();
//        entity.keyword = keyword;
//        entity.createdAt = System.currentTimeMillis();
//
//        compositeDisposable.add(
//                repository.getRoomService().searchHistoryDao()
//                        .deleteDuplicate(entity.userId, keyword) // xoá trùng nếu có
//                        .andThen(repository.getRoomService().searchHistoryDao().insert(entity)) // thêm mới
//                        .andThen(repository.getRoomService().searchHistoryDao().deleteOldestIfExceeded(entity.userId)) // giới hạn 10
//                        .andThen(repository.getRoomService().searchHistoryDao().getRecentHistory(entity.userId))
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(listHistory::setValue, throwable -> {
//                        })
//        );
//    }
    public void clearAllHistory() {
        long userId = repository.getSharedPreferences().getUserId();
        compositeDisposable.add(
                repository.getRoomService().searchHistoryDao()
                        .clearByUser(userId)
                        .andThen(repository.getRoomService().searchHistoryDao().getRecentHistory(userId))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(listHistory::setValue, throwable -> {
                        })
        );
    }
    public void getListMovie(MainCallback<List<MovieResponse>> callback, MovieRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);

        compositeDisposable.add(repository.getApiService().getListMovie(query)
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
                                callback.doSuccess(response.getData().getContent());
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
