package com.movie_hub.android.ui.main;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import com.movie_hub.android.BR;
import com.movie_hub.android.BuildConfig;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.databinding.ActivityMainBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.account.AccountFragment;
import com.movie_hub.android.ui.main.account.UnLoginAccountFragment;
import com.movie_hub.android.ui.main.account.language.LanguageActivity;
import com.movie_hub.android.ui.main.home.HomeFragment;
import com.movie_hub.android.ui.main.schedule.ScheduleFragment;
import com.movie_hub.android.ui.main.search.SearchFragment;


public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel> implements SystemBarColorProvider {
    private Fragment active;
    private FragmentManager fm;
    private HomeFragment homeFragment;
    private SearchFragment searchFragment;
    private ScheduleFragment scheduleFragment;
    private AccountFragment accountFragment;
    private UnLoginAccountFragment unLoginAccountFragment;
    private static final String HOME = "HOME";
    private static final String SEARCH = "SEARCH";
    private static final String SCHEDULE = "SCHEDULE";
    private static final String ACCOUNT = "ACCOUNT";
    private static final String ACCOUNT_UN_LOGIN = "ACCOUNT_UN_LOGIN";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        setUpFragment();
    }

    @SuppressLint("NonConstantResourceId")
    private void setUpFragment() {
        initFragments();
        viewBinding.bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    handleFragment(HOME);
                    return true;
                case R.id.search:
                    handleFragment(SEARCH);
                    return true;
                case R.id.schedule:
                    handleFragment(SCHEDULE);
                    return true;
                case R.id.account:
                    if (viewModel.isLogin()) {
                        handleFragment(ACCOUNT);
                    } else {
                        handleFragment(ACCOUNT_UN_LOGIN);
                    }
                    return true;
            }
            return false;
        });
    }
    private void initFragments() {
        homeFragment = new HomeFragment();
        fm = getSupportFragmentManager();
        fm.beginTransaction()
                .add(R.id.fragment_container, homeFragment, HOME)
                .commit();
        active = homeFragment;
    }
    public void handleFragment(String tag) {
        if (fm == null) fm = getSupportFragmentManager();

        if (homeFragment == null) homeFragment = new HomeFragment();
        if (searchFragment == null) searchFragment = new SearchFragment();
        if (scheduleFragment == null) scheduleFragment = new ScheduleFragment();
        if (accountFragment == null) accountFragment = new AccountFragment();
        if (unLoginAccountFragment == null) unLoginAccountFragment = new UnLoginAccountFragment();

        Fragment target = null;
        switch (tag) {
            case HOME:
                target = homeFragment;
                break;
            case SEARCH:
                target = searchFragment;
                break;
            case SCHEDULE:
                target = scheduleFragment;
                break;
            case ACCOUNT:
                target = accountFragment;
                break;
            case ACCOUNT_UN_LOGIN:
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
        return R.color.bg_app;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_tab_bar;
    }
}
