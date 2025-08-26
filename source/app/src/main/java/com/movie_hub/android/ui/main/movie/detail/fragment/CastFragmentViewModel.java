package com.movie_hub.android.ui.main.movie.detail.fragment;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.request.moviePerson.MoviePersonRequest;
import com.movie_hub.android.data.model.api.request.person.PersonRequest;
import com.movie_hub.android.data.model.api.response.moviePerson.MoviePersonResponse;
import com.movie_hub.android.data.model.api.response.person.PersonResponse;
import com.movie_hub.android.ui.base.fragment.BaseFragmentViewModel;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailViewModel;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class CastFragmentViewModel extends BaseFragmentViewModel {
    public CastFragmentViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void getListMoviePerson(MainCallback<List<MoviePersonResponse>> callback, MoviePersonRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(repository.getApiService().getListMoviePerson(query)
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
