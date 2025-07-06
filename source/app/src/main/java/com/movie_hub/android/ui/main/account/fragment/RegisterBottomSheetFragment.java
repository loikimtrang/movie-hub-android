package com.movie_hub.android.ui.main.account.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.FormError;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.login.UserRegisterRequest;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.FragmentBottomSheetRegisterBinding;
import com.movie_hub.android.helper.BlurEffectManager;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.utils.ClickUtils;

import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class RegisterBottomSheetFragment extends BottomSheetDialogFragment {

    private FragmentBottomSheetRegisterBinding binding;
    final boolean[] isPasswordVisible = {false};
    final boolean[] isRePasswordVisible = {false};
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentBottomSheetRegisterBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // RenderEffect (nếu Android 12+)
        BlurEffectManager.addBlurEffect(requireActivity());

        setUpPassword();
        onRegisterClick();
        onLoginNowClick();

        binding.layout.setOnClickListener(v -> hideKeyboard());

        return view;
    }
    private void onLoginNowClick() {
        binding.loginNow.setOnClickListener(v -> {
            ClickUtils.debounceClick(binding.loginNow);
            hideKeyboard();
            new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 500);
            LoginBottomSheetFragment loginBottom = new LoginBottomSheetFragment();
            loginBottom.show(requireActivity().getSupportFragmentManager(), loginBottom.getTag());
        });
    }
    private void onRegisterClick() {
        binding.register.setOnClickListener(v -> {
            if (validateInput()) {
                ClickUtils.debounceClick(binding.register);
                hideKeyboard();
                clearAllFocus();

                binding.register.setText("");
                binding.registerLoading.setVisibility(View.VISIBLE);
                binding.register.setClickable(false);

                UserRegisterRequest request = new UserRegisterRequest();
                request.setEmail(Objects.requireNonNull(binding.email.getText()).toString().trim());
                request.setUsername(Objects.requireNonNull(binding.username.getText()).toString().trim());
                request.setPassword(Objects.requireNonNull(binding.rePassword.getText()).toString().trim());

                ((MainActivity) requireActivity()).userRegister(request, new MainCallback<ResponseWrapper>() {
                    @Override
                    public void doSuccess(ResponseWrapper object) {
                        hideKeyboard();
                        if (object.isResult()) {

                            new Handler(Looper.getMainLooper()).postDelayed(() -> dismiss(), 500);
                            LoginBottomSheetFragment loginBottom = new LoginBottomSheetFragment();
                            loginBottom.show(requireActivity().getSupportFragmentManager(), loginBottom.getTag());
                        } else {
                            if (Objects.equals(object.getCode(), "ERROR-ACCOUNT-ERROR-0002"))
                                showRegisterError(getString(R.string.the_username_already_exists_or_is_invalid));
                            else showRegisterError(getString(R.string.the_email_already_exists_or_is_invalid));
                        }

                    }
                    @Override
                    public void doError(Throwable throwable) {
                        if (throwable instanceof UnknownHostException || throwable instanceof SocketTimeoutException) {
                            showRegisterError(getString(R.string.network_error_please_check_your_internet_connection));
                        } else if (throwable instanceof ConnectException) {
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
                    }
                });
            }
        });
    }

    private void showRegisterError(String message) {
        new ToastMessage(ToastMessage.TYPE_WARNING, message).showMessage(getContext());

        binding.register.setText(getString(R.string.sign_in));
        binding.registerLoading.setVisibility(View.GONE);
        binding.register.setClickable(true);
    }
    public boolean validateInput() {
        String username = binding.username.getText().toString().trim();
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString();
        String rePassword = binding.rePassword.getText().toString();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
            new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.mgs_please_fill_in_all_the_required_information)).showMessage(getContext());
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.error_invalid_email)).showMessage(getContext());
            return false;
        }

        if (password.length() < 6) {
            new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.error_password_length)).showMessage(getContext());
            return false;
        }

        if (!password.matches(".*[A-Z].*") || !password.matches(".*[!@#$%^&*+=?].*")) {
            new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.error_password_format)).showMessage(getContext());
            return false;
        }

        if (!password.equals(rePassword)) {
            new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.error_password_mismatch)).showMessage(getContext());
            return false;
        }
        return true;
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

        binding.hideRePassword.setOnClickListener(v -> {
            isRePasswordVisible[0] = !isRePasswordVisible[0];
            if (isRePasswordVisible[0]) {
                binding.rePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.ivToggleRePassword.setImageResource(R.drawable.ic_eye);
            } else {
                binding.rePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.ivToggleRePassword.setImageResource(R.drawable.ic_eye_hidden);
            }
            binding.rePassword.setSelection(Objects.requireNonNull(binding.rePassword.getText()).length());
        });
    }
    private void hideKeyboard() {
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
