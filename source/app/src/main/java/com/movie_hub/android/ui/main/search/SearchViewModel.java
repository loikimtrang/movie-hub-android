package com.movie_hub.android.ui.main.search;

import android.util.Log;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.request.movie.MovieRequest;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.room.SearchHistoryEntity;
import com.movie_hub.android.ui.base.fragment.BaseFragmentViewModel;
import com.movie_hub.android.ui.main.MainCallback;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class SearchViewModel extends BaseFragmentViewModel {
    public SearchViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void insertKeyWord(String keyword) {
        if (!isLogin()) return;

        long userId = repository.getSharedPreferences().getUserId();

        SearchHistoryEntity entity = new SearchHistoryEntity();
        entity.userId = userId;
        entity.keyword = keyword;
        entity.createdAt = System.currentTimeMillis();

        compositeDisposable.add(
                repository.getRoomService().searchHistoryDao()
                        .deleteDuplicate(userId, keyword)
                        .andThen(repository.getRoomService().searchHistoryDao().insert(entity))
                        .andThen(
                                repository.getRoomService().searchHistoryDao().countByUser(userId)
                                        .flatMapCompletable(count -> {
                                            if (count > 10) {
                                                return repository.getRoomService().searchHistoryDao().deleteOldest(userId);
                                            } else {
                                                return Completable.complete(); // RxJava 3
                                            }
                                        })
                        )
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                () -> Log.d("OK", "Insert thành công"),
                                throwable -> Log.e("ERR", "Insert lỗi", throwable)
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
}