package com.movie_hub.android.ui.main.movie.detail.fragment;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.request.movie.MovieRequest;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.ui.base.fragment.BaseFragmentViewModel;
import com.movie_hub.android.ui.main.MainCallback;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class RecommendationFragmentViewModel extends BaseFragmentViewModel {
    public RecommendationFragmentViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void getListMovie(MainCallback<List<MovieResponse>> callback, MovieRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);

        compositeDisposable.add(repository.getApiService().getListMovie(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
}
