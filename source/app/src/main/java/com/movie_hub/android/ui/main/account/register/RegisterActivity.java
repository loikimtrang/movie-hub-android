package com.movie_hub.android.ui.main.account.register;

import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.Nullable;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.login.UserRegisterRequest;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityRegisterBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.utils.ClickUtils;

import java.net.ConnectException;
import java.util.Objects;

import eu.davidea.flexibleadapter.databinding.BR;

public class RegisterActivity extends BaseActivity<ActivityRegisterBinding, RegisterViewModel> implements SystemBarColorProvider, View.OnClickListener {
    final boolean[] isPasswordVisible = {false};
    final boolean[] isRePasswordVisible = {false};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        setUpPassword();
    }

    private void onRegisterClick() {
        if (validateInput()) {
            ClickUtils.debounceClick(viewBinding.btnRegister);
            hideKeyboard();

            viewBinding.register.setText("");
            viewBinding.registerLoading.setVisibility(View.VISIBLE);
            viewBinding.btnRegister.setClickable(false);

            UserRegisterRequest request = new UserRegisterRequest();
            request.setEmail(Objects.requireNonNull(viewBinding.email.getText()).toString().trim());
            request.setFullName(Objects.requireNonNull(viewBinding.username.getText()).toString().trim());
            request.setPassword(Objects.requireNonNull(viewBinding.rePassword.getText()).toString().trim());

            viewModel.userRegister(new MainCallback<ResponseWrapper>() {
                @Override
                public void doSuccess(ResponseWrapper object) {
                    if (object.isResult()) {
                        navigateToLoginActivity();
                    } else {
                        if (Objects.equals(object.getCode(), "ERROR-ACCOUNT-ERROR-0002"))
                            showRegisterError(getString(R.string.the_username_already_exists_or_is_invalid));
                        else showRegisterError(getString(R.string.the_email_already_exists_or_is_invalid));
                    }

                }
                @Override
                public void doError(Throwable throwable) {
                    if (throwable instanceof ConnectException) {
                        showRegisterError(getString(R.string.cannot_connect_to_the_server_please_try_again));
                    } else {
                        showRegisterError(getString(R.string.register_error_please_try_again));
                    }
                }

                @Override
                public void doSuccess() {

                }

                @Override
                public void doFail() {
                    showRegisterError(getString(R.string.register_error_please_try_again));
                }
            }, request);
        }
    }

    public void navigateToLoginActivity() {
        new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.your_account_has_been_successfully_registered)).showMessage(this);
        viewBinding.getRoot().postDelayed(this::finish, 300);
    }

    private void showRegisterError(String message) {
        new ToastMessage(ToastMessage.TYPE_WARNING, message).showMessage(this);

        viewBinding.register.setText(getString(R.string.sign_in));
        viewBinding.registerLoading.setVisibility(View.GONE);
        viewBinding.btnRegister.setClickable(true);
    }
    public boolean validateInput() {
        String fullName = viewBinding.username.getText().toString().trim();
        String email = viewBinding.email.getText().toString().trim();
        String password = viewBinding.password.getText().toString();
        String rePassword = viewBinding.rePassword.getText().toString();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
            new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.mgs_please_fill_in_all_the_required_information)).showMessage(this);
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.error_invalid_email)).showMessage(this);
            return false;
        }

        if (password.length() < 6) {
            new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.error_password_length)).showMessage(this);
            return false;
        }

        if (!password.matches(".*[A-Z].*") || !password.matches(".*[!@#$%^&*+=?].*")) {
            new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.error_password_format)).showMessage(this);
            return false;
        }

        if (!password.equals(rePassword)) {
            new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.error_password_mismatch)).showMessage(this);
            return false;
        }
        return true;
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

        viewBinding.hideRePassword.setOnClickListener(v -> {
            isRePasswordVisible[0] = !isRePasswordVisible[0];
            if (isRePasswordVisible[0]) {
                viewBinding.rePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                viewBinding.ivToggleRePassword.setImageResource(R.drawable.ic_eye);
            } else {
                viewBinding.rePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                viewBinding.ivToggleRePassword.setImageResource(R.drawable.ic_eye_hidden);
            }
            viewBinding.rePassword.setSelection(Objects.requireNonNull(viewBinding.rePassword.getText()).length());
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_register;
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_register:
                onRegisterClick();
                break;
            case R.id.loginNow:
                showLoading();
                finish();
                break;
            default:
                break;
        }
    }
}
