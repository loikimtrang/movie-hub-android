package com.movie_hub.android.ui.main.account.forgot_password;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.Nullable;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.forgot.ForgotChangePasswordRequest;
import com.movie_hub.android.data.model.api.request.otp.VerifyOtpRequest;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityChangePasswordBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.utils.ClickUtils;

import java.util.Objects;

public class ChangePasswordActivity extends BaseActivity<ActivityChangePasswordBinding, ForgotPasswordViewModel> implements SystemBarColorProvider,
        View.OnClickListener{

    @Override
    public int getLayoutId() {
        return R.layout.activity_change_password;
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

    final boolean[] isPasswordVisible = {false};
    final boolean[] isRePasswordVisible = {false};
    String email = "";

    private int resendAttempts = 0;
    private final int maxResendAttempts = 3;
    private final long resendIntervalMillis = 60_000;
    private CountDownTimer countDownTimer;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        email = getIntent().getStringExtra("email");
        if (email == null) {
            email = "";
        }

        if (!email.isEmpty()) {
            viewBinding.email.setText(email);
        }
        setUpPassword();
        startResendCountdown();

    }
    private void startResendCountdown() {
        viewBinding.btnResend.setEnabled(false);
        resendAttempts++;

        countDownTimer = new CountDownTimer(resendIntervalMillis, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                viewBinding.btnResend.setText(getString(R.string.resend_otp) + " (" + seconds + "s)");
            }

            @Override
            public void onFinish() {
                if (resendAttempts < maxResendAttempts) {
                    viewBinding.btnResend.setText(getString(R.string.resend_otp));
                    viewBinding.btnResend.setEnabled(true);
                } else {
                    viewBinding.btnResend.setText(getString(R.string.resend_limit));
                    viewBinding.btnResend.setEnabled(false);
                }
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public void ResendOtp() {
        showLoading();
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail(email);

        ClickUtils.debounceClick(viewBinding.btnResend);
        hideKeyboard();

        viewModel.resendOtp(new MainCallback<ResponseWrapper>() {
            @Override
            public void doError(Throwable throwable) {
                hideLoading();
            }

            @Override
            public void doSuccess() {
                hideLoading();
            }

            @Override
            public void doSuccess(ResponseWrapper object) {
                hideLoading();
                startResendCountdown();
            }

            @Override
            public void doFail() {
                hideLoading();
            }
        }, request);
    }

    public void onChangePasswordClick() {
        if (validateInput()) {
            ClickUtils.debounceClick(viewBinding.btnChangePassword);
            hideKeyboard();

            viewBinding.tvChangePassword.setText("");
            viewBinding.changePasswordLoading.setVisibility(View.VISIBLE);
            viewBinding.btnChangePassword.setClickable(false);

            String password = viewBinding.password.getText().toString();
            String rePassword = viewBinding.rePassword.getText().toString();
            String otp = viewBinding.otp.getText().toString().trim();

            ForgotChangePasswordRequest request = new ForgotChangePasswordRequest();
            request.setPassword(password);
            request.setConfirmPassword(rePassword);
            request.setOtp(otp);
            request.setEmail(email);

            viewModel.changePassword(new MainCallback<ResponseWrapper>() {
                @Override
                public void doError(Throwable error) {
                    showChangePasswordError(getString(R.string.an_error_occurred));
                }

                @Override
                public void doSuccess() {

                }
                @Override
                public void doSuccess(ResponseWrapper responseWrapper) {
                    hideLoading();
                    if (responseWrapper.isResult()) {
                        navigateToLoginActivity();
                    } else {
                        showChangePasswordError(getString(R.string.otp_not_match));
                    }
                }

                @Override
                public void doFail() {

                }
            }, request);
        }
    }

    public void navigateToLoginActivity() {
        new ToastMessage(ToastMessage.TYPE_NORMAL,
                getString(R.string.change_password_success))
                .showMessage(this);
        finish();
    }

    private void showChangePasswordError(String message) {
        new ToastMessage(ToastMessage.TYPE_WARNING, message).showMessage(this);

        viewBinding.tvChangePassword.setText(getString(R.string.change_password));
        viewBinding.changePasswordLoading.setVisibility(View.GONE);
        viewBinding.btnChangePassword.setClickable(true);
    }
    public boolean validateInput() {
        String password = viewBinding.password.getText().toString();
        String rePassword = viewBinding.rePassword.getText().toString();
        String otp = viewBinding.otp.getText().toString().trim();
        if (otp.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
            new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.mgs_please_fill_in_all_the_required_information)).showMessage(this);
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_change_password:
                onChangePasswordClick();
                break;
            case R.id.btn_resend:
                if (resendAttempts < maxResendAttempts) {
                    ResendOtp();
                }
                break;
            default:
                break;
        }
    }
}
