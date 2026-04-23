package com.movie_hub.android.di.component;

import com.movie_hub.android.di.module.ActivityModule;
import com.movie_hub.android.di.scope.ActivityScope;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.account.contact.ContactActivity;
import com.movie_hub.android.ui.main.account.favourite.FavouriteActivity;
import com.movie_hub.android.ui.main.account.forgot_password.ChangePasswordActivity;
import com.movie_hub.android.ui.main.account.forgot_password.ForgotPasswordActivity;
import com.movie_hub.android.ui.main.account.history.HistoryActivity;
import com.movie_hub.android.ui.main.account.language.LanguageActivity;
import com.movie_hub.android.ui.main.account.login.LoginActivity;
import com.movie_hub.android.ui.main.account.manage_account.ManageAccountActivity;
import com.movie_hub.android.ui.main.account.playlist.PlayListActivity;
import com.movie_hub.android.ui.main.account.privacy.PrivacyActivity;
import com.movie_hub.android.ui.main.account.register.RegisterActivity;
import com.movie_hub.android.ui.main.account.setting.SettingActivity;
import com.movie_hub.android.ui.main.account.updateapp.CheckUpdateActivity;
import com.movie_hub.android.ui.main.account.verifyotp.VerifyOtpActivity;
import com.movie_hub.android.ui.main.home.detail.HomeSideBarDetailActivity;
import com.movie_hub.android.ui.main.home.filter.FilterActivity;
import com.movie_hub.android.ui.main.home.notification.NotificationActivity;
import com.movie_hub.android.ui.main.home.topic.HomeMoreTopicActivity;
import com.movie_hub.android.ui.main.home.topic.topic_detail.HomeTopicDetailActivity;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.ui.main.movie.detail.comment.CommentActivity;
import com.movie_hub.android.ui.main.movie.detail.review.ReviewActivity;
import com.movie_hub.android.ui.main.movie.watch.WatchMovieActivity;
import com.movie_hub.android.ui.main.person.PersonDetailActivity;
import com.movie_hub.android.ui.main.person.PersonDetailViewModel;
import com.movie_hub.android.ui.main.splash.SplashActivity;
import com.movie_hub.android.ui.main.splash.survey.SurveyActivity;

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
    void inject(FavouriteActivity activity);
    void inject(PlayListActivity activity);
    void inject(HistoryActivity activity);
    void inject(CommentActivity activity);
    void inject(HomeSideBarDetailActivity activity);
    void inject(HomeMoreTopicActivity activity);
    void inject(HomeTopicDetailActivity activity);
    void inject(FilterActivity activity);
    void inject(ChangePasswordActivity activity);
    void inject(ForgotPasswordActivity activity);
    void inject(ReviewActivity activity);
    void inject(ContactActivity activity);
    void inject(PrivacyActivity activity);
    void inject(SettingActivity activity);
    void inject(SurveyActivity activity);
    void inject(NotificationActivity activity);
}

