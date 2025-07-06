package com.movie_hub.android.ui.main.account.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.request.login.UserLoginRequest;
import com.movie_hub.android.data.model.api.response.login.UserLoginResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.FragmentBottomSheetLoginBinding;
import com.movie_hub.android.databinding.FragmentBottomSheetRegisterBinding;
import com.movie_hub.android.helper.BlurEffectManager;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.utils.ClickUtils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Objects;

public class LoginBottomSheetFragment extends BottomSheetDialogFragment {

    private FragmentBottomSheetLoginBinding binding;
    final boolean[] isPasswordVisible = {false};
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentBottomSheetLoginBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        BlurEffectManager.addBlurEffect(requireActivity());

        setUpPassword();
        onLoginClick();
        onRegisterNowClick();
        binding.layout.setOnClickListener(v -> hideKeyboard());

        return view;
    }

    public Boolean isEmptyForm() {
        if (binding.username.getText() == null || binding.password.getText() == null || binding.username.getText().toString().trim().isEmpty() || binding.password.getText().toString().trim().isEmpty()) {
            new ToastMessage(ToastMessage.TYPE_NORMAL,
                    getString(R.string.mgs_please_fill_in_all_the_required_information)).showMessage(getContext());
            return true;
        }
        return false;
    }

    private void onRegisterNowClick() {
        binding.registerNow.setOnClickListener(v -> {
            ClickUtils.debounceClick(binding.registerNow);
            hideKeyboard();
            new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 500);
            RegisterBottomSheetFragment registerBottom = new RegisterBottomSheetFragment();
            registerBottom.show(requireActivity().getSupportFragmentManager(), registerBottom.getTag());
        });
    }
    private void onLoginClick() {
        binding.login.setOnClickListener(v -> {
            if (!isEmptyForm()) {
                ClickUtils.debounceClick(binding.login);
                hideKeyboard();
                clearAllFocus();

                binding.login.setText("");
                binding.loginLoading.setVisibility(View.VISIBLE);
                binding.login.setClickable(false);

                UserLoginRequest request = new UserLoginRequest();
                request.setUsername(Objects.requireNonNull(binding.username.getText()).toString().trim());
                request.setPassword(Objects.requireNonNull(binding.password.getText()).toString().trim());

                ((MainActivity) requireActivity()).userLogin(request, new MainCallback<UserLoginResponse>() {
                    @Override
                    public void doSuccess(UserLoginResponse object) {

                        dismiss();
                    }

                    @Override
                    public void doFail() {
                        showLoginError(getString(R.string.incorrect_username_or_password));
                    }

                    public void doError(Throwable throwable) {
                        if (throwable instanceof UnknownHostException || throwable instanceof SocketTimeoutException) {
                            showLoginError(getString(R.string.network_error_please_check_your_internet_connection));
                        } else if (throwable instanceof ConnectException) {
                            showLoginError(getString(R.string.cannot_connect_to_the_server_please_try_again));
                        } else {
                            showLoginError(getString(R.string.login_error_please_try_again));
                        }
                    }

                    @Override
                    public void doSuccess() {}
                });
            }
        });
    }
    private void showLoginError(String message) {
        new ToastMessage(ToastMessage.TYPE_WARNING, message).showMessage(getContext());

        binding.login.setText(getString(R.string.login));
        binding.loginLoading.setVisibility(View.GONE);
        binding.login.setClickable(true);
    }

    private void setUpPassword() {
        binding.hidePassword.setOnClickListener(v -> {
            isPasswordVisible[0] = !isPasswordVisible[0];
            if (isPasswordVisible[0]) {
                binding.password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye);
            } else {
                binding.password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.ivTogglePassword.setImageResource(R.drawable.ic_eye_hidden);
            }
            binding.password.setSelection(Objects.requireNonNull(binding.password.getText()).length());
        });
    }
    private void hideKeyboard() {
//        clearAllFocus();
        View view = requireDialog().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void clearAllFocus() {
        View root = requireView();
        root.clearFocus();

        binding.layout.setFocusableInTouchMode(true);
        binding.layout.requestFocus();
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if (view != null && view.getParent() instanceof View) {
            View parent = (View) view.getParent();
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);

            behavior.setPeekHeight(ViewGroup.LayoutParams.MATCH_PARENT);
            parent.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

            behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING ||
                            newState == BottomSheetBehavior.STATE_SETTLING ||
                            newState == BottomSheetBehavior.STATE_HIDDEN) {
                        hideKeyboard();
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                }
            });
        }
    }
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        hideKeyboard();
        BlurEffectManager.removeBlurEffect(requireActivity());
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            View touchOutsideView = dialog.findViewById(R.id.touch_outside);
            if (touchOutsideView != null) {
                touchOutsideView.setOnClickListener(v -> {
                    hideKeyboard();
                    dismiss();
                });
            }
        });

        return dialog;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
