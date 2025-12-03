package com.movie_hub.android.ui.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.appversion.CheckAppVersionRequest;
import com.movie_hub.android.data.model.api.request.login.UserLoginRequest;
import com.movie_hub.android.data.model.api.request.login.UserRegisterRequest;
import com.movie_hub.android.data.model.api.request.user.UserLoginGoogleRequest;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.history.WatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.login.UserLoginResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityMainBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.account.AccountFragment;
import com.movie_hub.android.ui.main.account.UnLoginAccountFragment;
import com.movie_hub.android.ui.main.account.favourite.FavouriteActivity;
import com.movie_hub.android.ui.main.account.history.HistoryActivity;
import com.movie_hub.android.ui.main.account.language.LanguageActivity;
import com.movie_hub.android.ui.main.account.login.LoginActivity;
import com.movie_hub.android.ui.main.account.manage_account.ManageAccountActivity;
import com.movie_hub.android.ui.main.account.playlist.PlayListActivity;
import com.movie_hub.android.ui.main.account.updateapp.CheckUpdateActivity;
import com.movie_hub.android.ui.main.home.HomeFragment;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.ui.main.movie.watch.WatchMovieActivity;
import com.movie_hub.android.ui.main.schedule.ScheduleFragment;
import com.movie_hub.android.ui.main.search.SearchFragment;
import com.movie_hub.android.ui.main.splash.SplashActivity;
import com.movie_hub.android.utils.GsonUtils;


public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel> implements SystemBarColorProvider, View.OnClickListener {
    private Fragment active;
    private FragmentManager fm;
    private HomeFragment homeFragment;
    private SearchFragment searchFragment;
    private ScheduleFragment scheduleFragment;
    private AccountFragment accountFragment;
    private UnLoginAccountFragment unLoginAccountFragment;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        setUpFragment();

//        if (viewModel.isLogin()) {
//            getUserProfile();
//        }
    }

    @SuppressLint("NonConstantResourceId")
    private void setUpFragment() {
        initFragments();
        viewBinding.bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    handleFragment(Constants.HOME);
                    return true;
                case R.id.search:
                    handleFragment(Constants.SEARCH);
                    return true;
                case R.id.schedule:
                    handleFragment(Constants.SCHEDULE);
                    return true;
                case R.id.account:
                    if (viewModel.isLogin()) {
                        handleFragment(Constants.ACCOUNT);
                    } else {
                        handleFragment(Constants.ACCOUNT_UN_LOGIN);
                    }
                    return true;
            }
            return false;
        });
    }
    private void initFragments() {
        fm = getSupportFragmentManager();

        String bannerJson = getIntent().getStringExtra("home_banner");

        Bundle bundle = new Bundle();
        bundle.putString("banner_json", bannerJson);

        homeFragment = new HomeFragment();
        homeFragment.setArguments(bundle);

        fm.beginTransaction()
                .add(R.id.fragment_container, homeFragment, Constants.HOME)
                .commit();
        active = homeFragment;
    }

    public void handleFragment(String tag) {
        if (fm == null) fm = getSupportFragmentManager();

        if (homeFragment == null) homeFragment = new HomeFragment();
//        if (searchFragment == null) searchFragment = new SearchFragment();
        searchFragment = new SearchFragment();
        if (scheduleFragment == null) scheduleFragment = new ScheduleFragment();
        if (accountFragment == null) accountFragment = new AccountFragment();
        if (unLoginAccountFragment == null) unLoginAccountFragment = new UnLoginAccountFragment();

        Fragment target = null;
        switch (tag) {
            case Constants.HOME:
                target = homeFragment;
                break;
            case Constants.SEARCH:
                target = searchFragment;
                break;
            case Constants.SCHEDULE:
                target = scheduleFragment;
                break;
            case Constants.ACCOUNT:
                target = accountFragment;
                break;
            case Constants.ACCOUNT_UN_LOGIN:
                target = unLoginAccountFragment;
                break;
        }
        if (target == null || active == target) return;

        if (!target.isAdded()) {
            fm.beginTransaction()
                    .hide(active)
                    .add(R.id.fragment_container, target, tag)
                    .commit();
        } else {
            fm.beginTransaction()
                    .hide(active)
                    .show(target)
                    .commit();
        }
        active = target;
    }
    public void navigateToLanguage() {
        Intent intent = new Intent(this, LanguageActivity.class);
        startActivityForResult(intent, Constants.REQUEST_LANGUAGE);
    }
    public void navigateToCheckUpdate() {
        Intent intent = new Intent(this, CheckUpdateActivity.class);
        startActivity(intent);
    }

    public void navigateToPlayList() {
        Intent intent = new Intent(this, PlayListActivity.class);
        startActivity(intent);
    }

    public void navigateToHistory() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    public void navigateToFavourite() {
        Intent intent = new Intent(this, FavouriteActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_LANGUAGE && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("languageChanged", false)) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }
    }
        @Override
    protected void onResume() {
        super.onResume();

    }
    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }
    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public int getStatusBarColor() {
        return R.color.header_app;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_tab_bar;
    }
    public void userSignOut() {
        viewModel.showLoading();
        viewModel.userSignOut(new MainCallback<Void>() {
            @Override
            public void doSuccess(Void unused) {
                startActivity(new Intent(MainActivity.this, SplashActivity.class));
                finish();
            }

            @Override
            public void doError(Throwable throwable) {
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred)).showMessage(MainActivity.this);
            }

            @Override
            public void doFail() {
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred)).showMessage(MainActivity.this);
            }

            @Override
            public void doSuccess() {
                startActivity(new Intent(MainActivity.this, SplashActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        viewBinding.lDialogExit.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
            case R.id.l_dialog_exit:
                viewBinding.lDialogExit.setVisibility(View.GONE);
                break;
            case R.id.btn_ok:
                finish();
                break;
            default:
                break;
        }
    }
    
    public void navigateToMovieDetail(MovieResponse movieResponse, ListWatchHistoryResponse listWatchHistoryResponse) {
        Intent it = new Intent(this, MovieDetailActivity.class);
        if (viewModel.isLogin()) {
            it.putExtra("movie_details", GsonUtils.toJson(movieResponse));
            it.putExtra("movie_details_tracking", GsonUtils.toJson(listWatchHistoryResponse));
            
        } else {
            it.putExtra("movie_details", GsonUtils.toJson(movieResponse));
        }
        startActivity(it);
    }
    public void navigateToWatchMovie(MovieResponse movieResponse, ListWatchHistoryResponse listWatchHistoryResponse) {
        Intent it = new Intent(this, WatchMovieActivity.class);
        if (viewModel.isLogin()) {
            if (movieResponse.getType() == Constants.TYPE_MOVIE_SERIES) {
                MovieItemResponse remainingEpisode;
                
                if (listWatchHistoryResponse == null || listWatchHistoryResponse.getWatchHistories() == null) {
                    remainingEpisode = null;
                } else {
                    
                    WatchHistoryResponse watchHistoryResponse = listWatchHistoryResponse.getFirstWatchHistory();
                    if (watchHistoryResponse == null) {
                        remainingEpisode = null;
                    } else {
                        
                        MovieItemResponse remaining = new MovieItemResponse();

                        if (!watchHistoryResponse.isCompleted()) {
                            remaining = movieResponse.getEpisodeById(watchHistoryResponse.getMovieItemId());
                        } else {
                            if (!movieResponse.isLastEpisode(watchHistoryResponse.getMovieItemId())) {
                                MovieItemResponse nextEpisode = movieResponse.getNextEpisode(watchHistoryResponse.getMovieItemId());

                                WatchHistoryResponse watchHistoryNoComplete = listWatchHistoryResponse.getWatchHistoryByMovieId(nextEpisode.getId());

                                if (watchHistoryNoComplete == null) {
                                    remaining = movieResponse.getEpisodeById(nextEpisode.getId());
                                } else {
                                    remaining = movieResponse.getEpisodeById(watchHistoryNoComplete.getMovieItemId());
                                }

                            } else {

                                WatchHistoryResponse watchHistoryNoComplete = listWatchHistoryResponse.getWatchHistoryNoComplete();

                                if (watchHistoryNoComplete == null) {
                                    remainingEpisode = null;
                                } else {
                                    remaining = movieResponse.getEpisodeById(watchHistoryNoComplete.getMovieItemId());
                                }
                            }
                        }

                        remainingEpisode = remaining;
                    }
                }

                if (remainingEpisode == null) {
                    movieResponse.setSeasonAndEpisodeSelectedAndPlaying(movieResponse.getSeasons().get(0).getEpisodes().get(0).getId());
                    it.putExtra("episode", GsonUtils.toJson(movieResponse.getSeasons().get(0).getEpisodes().get(0)));
                } else {
                    movieResponse.setSeasonAndEpisodeSelectedAndPlaying(remainingEpisode.getId());
                    it.putExtra("episode", GsonUtils.toJson(remainingEpisode));
                }
            }

            it.putExtra("movie_details", GsonUtils.toJson(movieResponse));
            it.putExtra("movie_details_tracking", GsonUtils.toJson(listWatchHistoryResponse));
        } else {
            it.putExtra("movie_details", GsonUtils.toJson(movieResponse));
            if (movieResponse.getType() == Constants.TYPE_MOVIE_SERIES) {
                it.putExtra("episode", GsonUtils.toJson(movieResponse.getSeasons().get(0).getEpisodes().get(0)));
            }
        }
        startActivity(it);
    }
}
