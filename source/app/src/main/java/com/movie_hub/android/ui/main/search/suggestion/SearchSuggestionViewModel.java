package com.movie_hub.android.ui.main.search.suggestion;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.request.actor.ActorRequest;
import com.movie_hub.android.data.model.api.request.movie.MovieRequest;
import com.movie_hub.android.data.model.api.response.actor.ActorResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.ui.base.fragment.BaseFragmentViewModel;
import com.movie_hub.android.ui.main.MainCallback;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class SearchSuggestionViewModel extends BaseFragmentViewModel {

    private final MutableLiveData<List<MovieResponse>> movieResult = new MutableLiveData<>();
    private final MutableLiveData<List<ActorResponse>> actorResult = new MutableLiveData<>();

    public LiveData<List<MovieResponse>> getMovieResult() {
        return movieResult;
    }

    public LiveData<List<ActorResponse>> getActorResult() {
        return actorResult;
    }
    public SearchSuggestionViewModel(Repository repository, MVVMApplication application) {
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

    public void getListActor(MainCallback<List<ActorResponse>> callback, ActorRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);

        compositeDisposable.add(repository.getApiService().getListActor(query)
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
