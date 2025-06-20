package com.movie_hub.android.ui.main.splash;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.ui.base.activity.BaseViewModel;

public class SplashViewModel extends BaseViewModel {
    public SplashViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
}
