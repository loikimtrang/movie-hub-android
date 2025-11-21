package com.movie_hub.android.ui.main.account.verifyotp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.otp.VerifyOtpRequest;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityVerifyOtpBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.utils.ClickUtils;

import java.net.ConnectException;

public class VerifyOtpActivity extends BaseActivity<ActivityVerifyOtpBinding, VerifyOtpViewModel> implements SystemBarColorProvider, View.OnClickListener {
    private int resendAttempts = 0;
    private final int maxResendAttempts = 3;
    private final long resendIntervalMillis = 60_000;
    private CountDownTimer countDownTimer;

    private String email = ""; // ← Thêm khởi tạo mặc định

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

        setupOtpInputs();
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

    private void setupOtpInputs() {
        EditText[] otpFields = {
                viewBinding.otp1,
                viewBinding.otp2,
                viewBinding.otp3,
                viewBinding.otp4,
                viewBinding.otp5,
                viewBinding.otp6
        };

        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            final EditText currentField = otpFields[index];

            currentField.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    currentField.setSelection(currentField.getText().length());

                    currentField.post(() -> currentField.setSelection(currentField.getText().length()));
                }
            });

            currentField.setCursorVisible(false);


            currentField.setTextIsSelectable(false);
            currentField.setFocusableInTouchMode(true);
            currentField.setFocusable(true);

            currentField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1) {
                        if (index < otpFields.length - 1) {
                            otpFields[index + 1].requestFocus();
                        }
                    } else if (s.length() == 0) {
                    }

                    currentField.post(() -> {
                        if (currentField.hasFocus()) {
                            currentField.setSelection(currentField.getText().length());
                        }
                    });
                }
            });

            currentField.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_DEL) {

                    if (currentField.getText().toString().isEmpty() && index > 0) {
                        otpFields[index - 1].setText("");
                        otpFields[index - 1].requestFocus();
                        otpFields[index - 1].post(() ->
                                otpFields[index - 1].setSelection(otpFields[index - 1].getText().length())
                        );
                    }
                }
                return false;
            });

            currentField.setOnClickListener(v -> {
                currentField.requestFocus();
                currentField.setSelection(currentField.getText().length());
            });
        }
    }

    public void onVerifyClick() {
        hideKeyboard();
        viewBinding.contentView.clearFocus();

        String otp = viewBinding.otp1.getText().toString() +
                viewBinding.otp2.getText().toString() +
                viewBinding.otp3.getText().toString() +
                viewBinding.otp4.getText().toString() +
                viewBinding.otp5.getText().toString() +
                viewBinding.otp6.getText().toString();

        if (otp.length() < 6 || otp.contains(" ")) {
            // Focus vào ô trống đầu tiên (UX cực tốt)
            EditText[] fields = {viewBinding.otp1, viewBinding.otp2, viewBinding.otp3,
                    viewBinding.otp4, viewBinding.otp5, viewBinding.otp6};
            for (EditText f : fields) {
                if (f.getText().toString().isEmpty()) {
                    f.requestFocus();
                    break;
                }
            }
            new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.enter_otp))
                    .showMessage(this);
            return;
        }

        sendOtp(otp);
    }
    public void sendOtp(String otp) {
        showLoading();
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail(email);
        request.setOtp(otp);

        ClickUtils.debounceClick(viewBinding.btnSend);
        hideKeyboard();

        viewBinding.send.setText("");
        viewBinding.sendLoading.setVisibility(View.VISIBLE);
        viewBinding.btnSend.setClickable(false);

        viewModel.verifyOtp(new MainCallback<ResponseWrapper>() {
            @Override
            public void doError(Throwable throwable) {
                if (throwable instanceof ConnectException) {
                    showRegisterError(getString(R.string.cannot_connect_to_the_server_please_try_again));
                } else {
                    showRegisterError(getString(R.string.otp_error_please_try_again));
                }
            }

            @Override
            public void doSuccess() {
                hideLoading();
            }

            @Override
            public void doSuccess(ResponseWrapper object) {
                hideLoading();
                if (object.isResult()) {
                    navigateToLoginActivity();
                } else {
                    showRegisterError(getString(R.string.otp_not_match));
                }
            }

            @Override
            public void doFail() {
                showRegisterError(getString(R.string.otp_error_please_try_again));
            }
        }, request);
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
    public void navigateToLoginActivity() {
        new ToastMessage(ToastMessage.TYPE_NORMAL,
                getString(R.string.your_account_has_been_successfully_registered))
                .showMessage(this);
        finish();
    }
    private void showRegisterError(String message) {
        hideLoading();
        new ToastMessage(ToastMessage.TYPE_WARNING, message).showMessage(this);

        viewBinding.send.setText(getString(R.string.send));
        viewBinding.sendLoading.setVisibility(View.GONE);
        viewBinding.btnSend.setClickable(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    @Override public int getLayoutId() { return R.layout.activity_verify_otp; }
    @Override public int getBindingVariable() { return BR.vm; }
    @Override public void performDependencyInjection(ActivityComponent buildComponent) {
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
            case R.id.btn_send:
                onVerifyClick();
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
    @Override
    public void onBackPressed() {

    }
}
