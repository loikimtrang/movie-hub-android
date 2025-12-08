package com.movie_hub.android.ui.main.account.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.request.login.UserLoginRequest;
import com.movie_hub.android.data.model.api.request.user.UserLoginGoogleRequest;
import com.movie_hub.android.data.model.api.response.login.UserLoginResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityLoginBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.forgot_password.ForgotPasswordActivity;
import com.movie_hub.android.ui.main.account.register.RegisterActivity;
import com.movie_hub.android.ui.main.splash.SplashActivity;
import com.movie_hub.android.utils.ClickUtils;

import java.net.ConnectException;
import java.util.Objects;

public class LoginActivity extends BaseActivity<ActivityLoginBinding, LoginViewModel> implements SystemBarColorProvider, View.OnClickListener {
    final boolean[] isPasswordVisible = {false};
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        setUpGoogleSignIn();
        setUpPassword();
    }
    private void onLoginClick() {
        if (!isEmptyForm()) {
            ClickUtils.debounceClick(viewBinding.btnLogin);

            viewBinding.login.setText("");
            viewBinding.loginLoading.setVisibility(View.VISIBLE);
            viewBinding.btnLogin.setClickable(false);

            UserLoginRequest request = new UserLoginRequest();
            request.setEmail(Objects.requireNonNull(viewBinding.email.getText()).toString().trim());
            request.setPassword(Objects.requireNonNull(viewBinding.password.getText()).toString().trim());

            viewModel.userLogin(request, new MainCallback<UserLoginResponse>() {
                @Override
                public void doSuccess(UserLoginResponse object) {
                    hideLoading();
                    if (object.getCode() != null && !object.getCode().isEmpty() && object.getCode().equals(Constants.CODE_ACCOUNT_LOCK)) {
                        showNotificationLockAccount();
                    } else {
                        handleLoginSuccess();
                    }
                }

                @Override
                public void doFail() {
                    showLoginError(getString(R.string.incorrect_email_or_password));
                }

                public void doError(Throwable throwable) {
                    if (throwable instanceof ConnectException) {
                        showLoginError(getString(R.string.cannot_connect_to_the_server_please_try_again));
                    } else {
                        showLoginError(getString(R.string.login_error_please_try_again));
                    }
                }

                @Override
                public void doSuccess() {}
            });
        }
    }

    public void onLoginGoogleClick() {
        showLoading();
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, Constants.RC_SIGN_IN);
        });
    }
    private void setUpGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.CLIENT_ID)
                .requestEmail()
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account == null || account.getIdToken() == null) {
                    showLoginError("Không thể lấy mã token từ Google. Vui lòng thử lại.");
                    return;
                }

                String idToken = account.getIdToken();

                UserLoginGoogleRequest request = new UserLoginGoogleRequest();
                request.setIdToken(idToken);
                request.setPlatform(Constants.PLATFORM_ANDROID);

                viewModel.userLoginGoogle(new MainCallback<UserLoginResponse>() {
                    @Override
                    public void doSuccess(UserLoginResponse object) {
                        hideLoading();
                        if (object.getCode() != null && !object.getCode().isEmpty() && object.getCode().equals(Constants.CODE_ACCOUNT_LOCK)) {
                            showNotificationLockAccount();
                        } else {
                            handleLoginSuccess();
                        }
                    }
                    @Override
                    public void doFail() {
                        hideLoading();
                        showLoginError(getString(R.string.login_error_please_try_again));
                    }

                    @Override
                    public void doError(Throwable throwable) {
                        hideLoading();
                        if (throwable instanceof ConnectException) {
                            showLoginError(getString(R.string.cannot_connect_to_the_server_please_try_again));
                        } else {
                            showLoginError(getString(R.string.login_error_please_try_again));
                        }
                    }

                    @Override
                    public void doSuccess() {}
                }, request);

            } catch (ApiException e) {
                e.printStackTrace();
                showLoginError(getString(R.string.login_error_please_try_again));
            }
        }
    }

    public Boolean isEmptyForm() {
        if (viewBinding.email.getText() == null || viewBinding.password.getText() == null || viewBinding.email.getText().toString().trim().isEmpty() || viewBinding.password.getText().toString().trim().isEmpty()) {
            new ToastMessage(ToastMessage.TYPE_NORMAL,
                    getString(R.string.mgs_please_fill_in_all_the_required_information)).showMessage(this);
            return true;
        }
        return false;
    }
    public void showNotificationLockAccount() {
        new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.account_has_been_lock)).showMessage(this);
    }
    private void showLoginError(String message) {
        new ToastMessage(ToastMessage.TYPE_WARNING, message).showMessage(this);

        viewBinding.login.setText(getString(R.string.login));
        viewBinding.loginLoading.setVisibility(View.GONE);
        viewBinding.btnLogin.setClickable(true);
    }
    private void setUpPassword() {
        viewBinding.hidePassword.setOnClickListener(v -> {
            isPasswordVisible[0] = !isPasswordVisible[0];
            if (isPasswordVisible[0]) {
                viewBinding.password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                viewBinding.ivTogglePassword.setImageResource(R.drawable.ic_eye);
            } else {
                viewBinding.password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                viewBinding.ivTogglePassword.setImageResource(R.drawable.ic_eye_hidden);
            }
            viewBinding.password.setSelection(Objects.requireNonNull(viewBinding.password.getText()).length());
        });
    }

    public static final int REQUEST_CODE_LOGIN = 1001;
    public void handleLoginSuccess() {
        if (Objects.equals(getIntent().getStringExtra("login_from_other"), "login_from_other")) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("login_success", true);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Intent intent = new Intent(LoginActivity.this, SplashActivity.class);
            intent.putExtra("login_success", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }


    private void onRegisterNowClick() {
        showLoading();
        navigateToNewActivity(this, RegisterActivity.class);
    }
    
    @Override
    public int getLayoutId() {
        return R.layout.activity_login;
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
        return R.color.bg_app;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                onLoginClick();
                break;
            case R.id.login_google:
                onLoginGoogleClick();
                break;
            case R.id.registerNow:
                onRegisterNowClick();
                break;
            case R.id.btn_forgot_password:
                Intent it = new Intent(this, ForgotPasswordActivity.class);
                startActivity(it);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewBinding.email.setText("");
        viewBinding.password.setText("");
    }
}
