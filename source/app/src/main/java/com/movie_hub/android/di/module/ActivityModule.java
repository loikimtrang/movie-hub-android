package com.movie_hub.android.di.module;

import android.content.Context;

import androidx.core.util.Supplier;
import androidx.lifecycle.ViewModelProvider;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.ViewModelProviderFactory;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.di.scope.ActivityScope;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.main.MainViewModel;
import com.movie_hub.android.ui.main.account.contact.ContactViewModel;
import com.movie_hub.android.ui.main.account.favourite.FavouriteViewModel;
import com.movie_hub.android.ui.main.account.forgot_password.ForgotPasswordViewModel;
import com.movie_hub.android.ui.main.account.history.HistoryViewModel;
import com.movie_hub.android.ui.main.account.language.LanguageViewModel;
import com.movie_hub.android.ui.main.account.login.LoginViewModel;
import com.movie_hub.android.ui.main.account.manage_account.ManageAccountViewModel;
import com.movie_hub.android.ui.main.account.playlist.PlayListViewModel;
import com.movie_hub.android.ui.main.account.privacy.PrivacyViewModel;
import com.movie_hub.android.ui.main.account.register.RegisterViewModel;
import com.movie_hub.android.ui.main.account.updateapp.CheckUpdateViewModel;
import com.movie_hub.android.ui.main.account.verifyotp.VerifyOtpViewModel;
import com.movie_hub.android.ui.main.home.detail.HomeSideBarDetailActivity;
import com.movie_hub.android.ui.main.home.detail.HomeSideBarDetailViewModel;
import com.movie_hub.android.ui.main.home.filter.FilterViewModel;
import com.movie_hub.android.ui.main.home.topic.HomeMoreTopicViewModel;
import com.movie_hub.android.ui.main.home.topic.topic_detail.HomeTopicDetailViewModel;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailViewModel;
import com.movie_hub.android.ui.main.movie.detail.comment.CommentViewModel;
import com.movie_hub.android.ui.main.movie.detail.review.ReviewViewModel;
import com.movie_hub.android.ui.main.movie.watch.WatchMovieViewModel;
import com.movie_hub.android.ui.main.person.PersonDetailViewModel;
import com.movie_hub.android.ui.main.splash.SplashViewModel;
import com.movie_hub.android.utils.GetInfo;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {

    private BaseActivity<?, ?> activity;

    public ActivityModule(BaseActivity<?, ?> activity) {
        this.activity = activity;
    }

    @Named("access_token")
    @Provides
    @ActivityScope
    String provideToken(Repository repository){
        return repository.getToken();
    }

    @Named("device_id")
    @Provides
    @ActivityScope
    String provideDeviceId( Context applicationContext){
        return GetInfo.getAll(applicationContext);
    }
    @Provides
    @ActivityScope
    MainViewModel provideMainViewModel(Repository repository, Context application) {
        Supplier<MainViewModel> supplier = () -> new MainViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<MainViewModel> factory = new ViewModelProviderFactory<>(MainViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(MainViewModel.class);
    }

    @Provides
    @ActivityScope
    SplashViewModel provideSplashViewModel(Repository repository, Context application) {
        Supplier<SplashViewModel> supplier = () -> new SplashViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<SplashViewModel> factory = new ViewModelProviderFactory<>(SplashViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(SplashViewModel.class);
    }

    @Provides
    @ActivityScope
    LanguageViewModel provideLanguageViewModel(Repository repository, Context application) {
        Supplier<LanguageViewModel> supplier = () -> new LanguageViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<LanguageViewModel> factory = new ViewModelProviderFactory<>(LanguageViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(LanguageViewModel.class);
    }

    @Provides
    @ActivityScope
    ManageAccountViewModel provideManageAccountViewModel(Repository repository, Context application) {
        Supplier<ManageAccountViewModel> supplier = () -> new ManageAccountViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<ManageAccountViewModel> factory = new ViewModelProviderFactory<>(ManageAccountViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(ManageAccountViewModel.class);
    }

    @Provides
    @ActivityScope
    WatchMovieViewModel provideWatchMovieViewModel(Repository repository, Context application) {
        Supplier<WatchMovieViewModel> supplier = () -> new WatchMovieViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<WatchMovieViewModel> factory = new ViewModelProviderFactory<>(WatchMovieViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(WatchMovieViewModel.class);
    }

    @Provides
    @ActivityScope
    MovieDetailViewModel provideMovieDetailViewModel(Repository repository, Context application) {
        Supplier<MovieDetailViewModel> supplier = () -> new MovieDetailViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<MovieDetailViewModel> factory = new ViewModelProviderFactory<>(MovieDetailViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(MovieDetailViewModel.class);
    }

    @Provides
    @ActivityScope
    LoginViewModel provideLoginViewModel(Repository repository, Context application) {
        Supplier<LoginViewModel> supplier = () -> new LoginViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<LoginViewModel> factory = new ViewModelProviderFactory<>(LoginViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(LoginViewModel.class);
    }

    @Provides
    @ActivityScope
    RegisterViewModel provideRegisterViewModel(Repository repository, Context application) {
        Supplier<RegisterViewModel> supplier = () -> new RegisterViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<RegisterViewModel> factory = new ViewModelProviderFactory<>(RegisterViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(RegisterViewModel.class);
    }

    @Provides
    @ActivityScope
    VerifyOtpViewModel provideVerifyOtpViewModel(Repository repository, Context application) {
        Supplier<VerifyOtpViewModel> supplier = () -> new VerifyOtpViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<VerifyOtpViewModel> factory = new ViewModelProviderFactory<>(VerifyOtpViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(VerifyOtpViewModel.class);
    }

    @Provides
    @ActivityScope
    CheckUpdateViewModel provideCheckUpdateViewModel(Repository repository, Context application) {
        Supplier<CheckUpdateViewModel> supplier = () -> new CheckUpdateViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<CheckUpdateViewModel> factory = new ViewModelProviderFactory<>(CheckUpdateViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(CheckUpdateViewModel.class);
    }

    @Provides
    @ActivityScope
    PersonDetailViewModel providePersonDetailViewModel(Repository repository, Context application) {
        Supplier<PersonDetailViewModel> supplier = () -> new PersonDetailViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<PersonDetailViewModel> factory = new ViewModelProviderFactory<>(PersonDetailViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(PersonDetailViewModel.class);
    }

    @Provides
    @ActivityScope
    FavouriteViewModel provideFavouriteViewModel(Repository repository, Context application) {
        Supplier<FavouriteViewModel> supplier = () -> new FavouriteViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<FavouriteViewModel> factory = new ViewModelProviderFactory<>(FavouriteViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(FavouriteViewModel.class);
    }

    @Provides
    @ActivityScope
    PlayListViewModel providePlayListViewModel(Repository repository, Context application) {
        Supplier<PlayListViewModel> supplier = () -> new PlayListViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<PlayListViewModel> factory = new ViewModelProviderFactory<>(PlayListViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(PlayListViewModel.class);
    }

    @Provides
    @ActivityScope
    HistoryViewModel provideHistoryViewModel(Repository repository, Context application) {
        Supplier<HistoryViewModel> supplier = () -> new HistoryViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<HistoryViewModel> factory = new ViewModelProviderFactory<>(HistoryViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(HistoryViewModel.class);
    }

    @Provides
    @ActivityScope
    CommentViewModel provideCommentViewModel(Repository repository, Context application) {
        Supplier<CommentViewModel> supplier = () -> new CommentViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<CommentViewModel> factory = new ViewModelProviderFactory<>(CommentViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(CommentViewModel.class);
    }

    @Provides
    @ActivityScope
    HomeSideBarDetailViewModel provideHomeSideBarDetailViewModel(Repository repository, Context application) {
        Supplier<HomeSideBarDetailViewModel> supplier = () -> new HomeSideBarDetailViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<HomeSideBarDetailViewModel> factory = new ViewModelProviderFactory<>(HomeSideBarDetailViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(HomeSideBarDetailViewModel.class);
    }

    @Provides
    @ActivityScope
    HomeMoreTopicViewModel provideHomeMoreTopicViewModel(Repository repository, Context application) {
        Supplier<HomeMoreTopicViewModel> supplier = () -> new HomeMoreTopicViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<HomeMoreTopicViewModel> factory = new ViewModelProviderFactory<>(HomeMoreTopicViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(HomeMoreTopicViewModel.class);
    }
    @Provides
    @ActivityScope
    HomeTopicDetailViewModel provideHomeTopicDetailViewModel(Repository repository, Context application) {
        Supplier<HomeTopicDetailViewModel> supplier = () -> new HomeTopicDetailViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<HomeTopicDetailViewModel> factory = new ViewModelProviderFactory<>(HomeTopicDetailViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(HomeTopicDetailViewModel.class);
    }

    @Provides
    @ActivityScope
    FilterViewModel provideFilterViewModel(Repository repository, Context application) {
        Supplier<FilterViewModel> supplier = () -> new FilterViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<FilterViewModel> factory = new ViewModelProviderFactory<>(FilterViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(FilterViewModel.class);
    }

    @Provides
    @ActivityScope
    ForgotPasswordViewModel provideForgotPasswordViewModel(Repository repository, Context application) {
        Supplier<ForgotPasswordViewModel> supplier = () -> new ForgotPasswordViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<ForgotPasswordViewModel> factory = new ViewModelProviderFactory<>(ForgotPasswordViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(ForgotPasswordViewModel.class);
    }

    @Provides
    @ActivityScope
    ReviewViewModel provideReviewViewModel(Repository repository, Context application) {
        Supplier<ReviewViewModel> supplier = () -> new ReviewViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<ReviewViewModel> factory = new ViewModelProviderFactory<>(ReviewViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(ReviewViewModel.class);
    }

    @Provides
    @ActivityScope
    ContactViewModel provideContactViewModel(Repository repository, Context application) {
        Supplier<ContactViewModel> supplier = () -> new ContactViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<ContactViewModel> factory = new ViewModelProviderFactory<>(ContactViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(ContactViewModel.class);
    }

    @Provides
    @ActivityScope
    PrivacyViewModel providePrivacyViewModel(Repository repository, Context application) {
        Supplier<PrivacyViewModel> supplier = () -> new PrivacyViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<PrivacyViewModel> factory = new ViewModelProviderFactory<>(PrivacyViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(PrivacyViewModel.class);
    }
}
