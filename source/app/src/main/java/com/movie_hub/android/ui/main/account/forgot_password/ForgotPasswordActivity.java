package com.movie_hub.android.ui.main.account.forgot_password;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.Nullable;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.forgot.ForgotPasswordRequest;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityForgotPasswordBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.utils.ClickUtils;

public class ForgotPasswordActivity extends BaseActivity<ActivityForgotPasswordBinding, ForgotPasswordViewModel>
        implements SystemBarColorProvider,
        View.OnClickListener {
    @Override
    public int getLayoutId() {
        return R.layout.activity_forgot_password;
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
    }

    public void onNextClick() {
        if (!isEmptyForm()) {
            showLoading();
            ClickUtils.debounceClick(viewBinding.btnNext);
            hideKeyboard();
            String email = viewBinding.email.getText().toString().trim();

            viewBinding.tvNext.setText("");
            viewBinding.nextLoading.setVisibility(View.VISIBLE);
            viewBinding.btnNext.setClickable(false);

            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setEmail(email);

            viewModel.requestForgotPassword(new MainCallback<ResponseWrapper>() {
                @Override
                public void doError(Throwable error) {
                    showLoginError(getString(R.string.an_error_occurred));
                }

                @Override
                public void doSuccess(ResponseWrapper responseWrapper) {
                    navigateToChangePassword(request.getEmail());
                }

                @Override
                public void doSuccess() {

                }

                @Override
                public void doFail() {
                    showLoginError(getString(R.string.incorrect_email));
                }
            }, request);
        }
    }

    public void navigateToChangePassword(String email) {
        Intent it = new Intent(this, ChangePasswordActivity.class);
        it.putExtra("email", email);
        startActivity(it);
        finish();
    }
    public Boolean isEmptyForm() {
        if (viewBinding.email.getText() == null || viewBinding.email.getText().toString().trim().isEmpty()) {
            new ToastMessage(ToastMessage.TYPE_NORMAL,
                    getString(R.string.mgs_please_fill_in_all_the_required_information)).showMessage(this);
            return true;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(viewBinding.email.getText().toString().trim()).matches()) {
            new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.error_invalid_email)).showMessage(this);
            return true;
        }
        return false;
    }

    private void showLoginError(String message) {
        hideLoading();
        new ToastMessage(ToastMessage.TYPE_WARNING, message).showMessage(this);

        viewBinding.tvNext.setText(getString(R.string.next));
        viewBinding.nextLoading.setVisibility(View.GONE);
        viewBinding.btnNext.setClickable(true);
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_next:
                onNextClick();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewBinding.tvNext.setText(getString(R.string.next));
        viewBinding.nextLoading.setVisibility(View.GONE);
        viewBinding.btnNext.setClickable(true);
        hideLoading();
    }
}
