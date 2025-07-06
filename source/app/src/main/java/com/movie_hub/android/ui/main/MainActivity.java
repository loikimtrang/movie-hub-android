package com.movie_hub.android.ui.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.login.UserLoginRequest;
import com.movie_hub.android.data.model.api.request.login.UserRegisterRequest;
import com.movie_hub.android.data.model.api.response.login.UserLoginResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityMainBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.account.AccountFragment;
import com.movie_hub.android.ui.main.account.UnLoginAccountFragment;
import com.movie_hub.android.ui.main.account.language.LanguageActivity;
import com.movie_hub.android.ui.main.account.manage_account.ManageAccountActivity;
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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        setUpFragment();

        if (viewModel.isLogin()) {
            getUserProfile();
        }
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
        homeFragment = new HomeFragment();
        fm = getSupportFragmentManager();
        fm.beginTransaction()
                .add(R.id.fragment_container, homeFragment, Constants.HOME)
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
    public void userLogin(UserLoginRequest request, MainCallback<UserLoginResponse> callback) {
        viewModel.userLogin(new MainCallback<UserLoginResponse>() {
            @Override
            public void doSuccess(UserLoginResponse object) {
                callback.doSuccess(object);

                runOnUiThread(() -> {
                    new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.login_successful))
                            .showMessage(MainActivity.this);
                    viewBinding.bottomNav.setSelectedItemId(R.id.home);
                });

            }

            @Override
            public void doFail() {
                callback.doFail();
            }

            @Override
            public void doError(Throwable throwable) {
                callback.doError(throwable);
            }

            @Override
            public void doSuccess() {}
        }, request);
    }

    public void userRegister(UserRegisterRequest request, MainCallback<ResponseWrapper> callback) {
        viewModel.userRegister(new MainCallback<ResponseWrapper>() {
            @Override
            public void doSuccess(ResponseWrapper object) {
                if (object.isResult()) {
                    new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.your_account_has_been_successfully_registered)).showMessage(MainActivity.this);
                }
                callback.doSuccess(object);
            }
            @Override
            public void doFail() {
                callback.doFail();
            }

            @Override
            public void doError(Throwable throwable) {
                callback.doError(throwable);
            }

            @Override
            public void doSuccess() {}

        }, request);
    }
    public void userSignOut() {
        viewModel.showLoading();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            viewModel.userSignOut();
            handleFragment(Constants.ACCOUNT_UN_LOGIN);
            viewModel.hideLoading();
        }, 1000);
    }

    public void getUserProfile() {
        viewModel.getUserProfile(new MainCallback<UserResponse>() {
            @Override
            public void doError(Throwable error) {

            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doFail() {

            }
        });
    }

    public void navigateToManageAccount() {
        Intent it = new Intent(this, ManageAccountActivity.class);
        startActivity(it);
    }
}
