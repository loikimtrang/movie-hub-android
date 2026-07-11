package com.movie_hub.android.ui.main.schedule;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.request.category.CategoryRequest;
import com.movie_hub.android.data.model.api.request.schedule.MovieScheduleRequest;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
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

public class ScheduleViewModel extends BaseFragmentViewModel {
    public ScheduleViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void getListMovie(MainCallback<List<MovieItemResponse>> callback, MovieScheduleRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(repository.getApiService().getListMovieSchedule(query)
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
