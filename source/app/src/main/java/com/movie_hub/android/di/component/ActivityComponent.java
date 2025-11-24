package com.movie_hub.android.di.component;

import com.movie_hub.android.di.module.ActivityModule;
import com.movie_hub.android.di.scope.ActivityScope;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.account.language.LanguageActivity;
import com.movie_hub.android.ui.main.account.login.LoginActivity;
import com.movie_hub.android.ui.main.account.manage_account.ManageAccountActivity;
import com.movie_hub.android.ui.main.account.register.RegisterActivity;
import com.movie_hub.android.ui.main.account.updateapp.CheckUpdateActivity;
import com.movie_hub.android.ui.main.account.verifyotp.VerifyOtpActivity;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.ui.main.movie.watch.WatchMovieActivity;
import com.movie_hub.android.ui.main.person.PersonDetailActivity;
import com.movie_hub.android.ui.main.person.PersonDetailViewModel;
import com.movie_hub.android.ui.main.splash.SplashActivity;

import dagger.Component;

@ActivityScope
@Component(modules = {ActivityModule.class}, dependencies = AppComponent.class)
public interface ActivityComponent {
    void inject(MainActivity activity);
    void inject(SplashActivity activity);
    void inject(LanguageActivity activity);
    void inject(ManageAccountActivity activity);
    void inject(WatchMovieActivity activity);
    void inject(MovieDetailActivity activity);
    void inject(LoginActivity activity);
    void inject(RegisterActivity activity);
    void inject(VerifyOtpActivity activity);
    void inject(CheckUpdateActivity activity);
    void inject(PersonDetailActivity activity);
}

