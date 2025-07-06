package com.movie_hub.android.ui.main.splash;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.ui.base.activity.BaseViewModel;
import com.movie_hub.android.ui.main.MainCallback;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class SplashViewModel extends BaseViewModel {
    public SplashViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
}
