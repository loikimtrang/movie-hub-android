package com.movie_hub.android.ui.main.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivitySplashBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.AccountFragment;
import com.movie_hub.android.ui.main.account.login.LoginActivity;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import eu.davidea.flexibleadapter.databinding.BR;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity<ActivitySplashBinding, SplashViewModel> implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        if (viewModel.isLogin()) {
            if (getIntent().getBooleanExtra("login_success", false)) {
                showLoginSuccessMessage(); // Hiển thị thông báo đăng nhập thành công
            } else {
                getUserProfile();
            }
        } else {
            new Handler().postDelayed(this::showLoginAndSkip, 3000);
        }
    }

    public void showLoginAndSkip() {
        viewBinding.layoutButton.setVisibility(View.VISIBLE);
        viewBinding.loadingProgress.setVisibility(View.GONE);
    }
    private void showLoginSuccessMessage() {
        new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.login_successful)).showMessage(this);
        getUserProfile();
    }

    public void getUserProfile() {
        viewModel.getUserProfile(new MainCallback<UserResponse>() {
            @Override
            public void doError(Throwable error) {
                if (error instanceof ConnectException) {
                    showError(getString(R.string.cannot_connect_to_the_server_please_try_again));
                } else {
                    userSignOut();
                }
            }

            @Override
            public void doSuccess(UserResponse response) {
                navigateToMainActivity();
            }

            @Override
            public void doSuccess() {
                navigateToMainActivity();
            }

            @Override
            public void doFail() {
                showError(getString(R.string.an_error_occurred));
            }
        });
    }

    public void userSignOut() {
        viewModel.showLoading();
        viewModel.userSignOut(new MainCallback<Void>() {
            @Override
            public void doSuccess(Void unused) {
                navigateToMainActivity();
            }

            @Override
            public void doError(Throwable throwable) {
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred)).showMessage(SplashActivity.this);
            }

            @Override
            public void doFail() {
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred)).showMessage(SplashActivity.this);
            }

            @Override
            public void doSuccess() {
                navigateToMainActivity();
            }
        });
    }
    public void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void navigateToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId())  {
            case R.id.btn_login:
                navigateToLoginActivity();
                break;
            case R.id.btn_skip:
                navigateToMainActivity();
                break;
            default:
                break;
        }
    }
}
