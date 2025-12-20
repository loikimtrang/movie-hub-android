package com.movie_hub.android.ui.main.person;

import androidx.lifecycle.MutableLiveData;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.request.favourite.CreateFavouriteRequest;
import com.movie_hub.android.data.model.api.response.favourite.FavouriteResponse;
import com.movie_hub.android.data.model.api.response.person.PersonResponse;
import com.movie_hub.android.ui.base.activity.BaseViewModel;
import com.movie_hub.android.utils.NetworkUtils;

import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class PersonDetailViewModel extends BaseViewModel {
    public PersonResponse person = new PersonResponse();
    public Boolean isFavourite = false;
    public Boolean isDirection = false;
    public MutableLiveData<FavouriteResponse> favouriteResponseFirst = new MutableLiveData<>();

    public FavouriteResponse favourite = new FavouriteResponse();
    public PersonDetailViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void createFavorite(CreateFavouriteRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(repository.getApiService().createFavourite(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable ->
                        throwable.flatMap((Function<Throwable, ObservableSource<?>>) throwable1 -> {
                            if (NetworkUtils.checkNetworkError(throwable1)) {
                                return application.showDialogNoInternetAccess();
                            } else{
                                return Observable.error(throwable1);
                            }
                        })
                )
                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                favourite.setId(response.getData());
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                        }
                )
        );
    }
    public void deleteFavorite(Long id) {
        compositeDisposable.add(repository.getApiService().deleteFavourite(id)
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
                                favourite.setId(null);
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                        }
                )
        );
    }

    public void getFavourite(CreateFavouriteRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(repository.getApiService().getFavourite(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable ->
                        throwable.flatMap((Function<Throwable, ObservableSource<?>>) throwable1 -> {
                            if (NetworkUtils.checkNetworkError(throwable1)) {
                                return application.showDialogNoInternetAccess();
                            } else{
                                return Observable.error(throwable1);
                            }
                        })
                )
                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                favouriteResponseFirst.postValue(response.getData());
                                favourite.setId(response.getData().getId());
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                        }
                )
        );
    }
}
