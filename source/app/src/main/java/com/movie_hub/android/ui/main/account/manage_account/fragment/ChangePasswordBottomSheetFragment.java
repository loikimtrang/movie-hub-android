package com.movie_hub.android.ui.main.account.manage_account.fragment;

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
import com.movie_hub.android.databinding.FragmentBottomSheetChangePasswordBinding;
import com.movie_hub.android.databinding.FragmentBottomSheetRegisterBinding;
import com.movie_hub.android.helper.BlurEffectManager;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.fragment.LoginBottomSheetFragment;
import com.movie_hub.android.utils.ClickUtils;

import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;

public class ChangePasswordBottomSheetFragment extends BottomSheetDialogFragment {

    private FragmentBottomSheetChangePasswordBinding binding;
    final boolean[] isOldPasswordVisible = {false};
    final boolean[] isNewPasswordVisible = {false};
    final boolean[] isReNewPasswordVisible = {false};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentBottomSheetChangePasswordBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // RenderEffect (nếu Android 12+)
        BlurEffectManager.addBlurEffect(requireActivity());

        setUpPassword();

        binding.layout.setOnClickListener(v -> hideKeyboard());

        binding.cancel.setOnClickListener(v -> dismiss());

        return view;
    }

    private void setUpPassword() {
        binding.hideOldPassword.setOnClickListener(v -> {
            isOldPasswordVisible[0] = !isOldPasswordVisible[0];
            if (isOldPasswordVisible[0]) {
                binding.oldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.ivToggleOldPassword.setImageResource(R.drawable.ic_eye);
            } else {
                binding.oldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.ivToggleOldPassword.setImageResource(R.drawable.ic_eye_hidden);
            }
            binding.oldPassword.setSelection(Objects.requireNonNull(binding.oldPassword.getText()).length());
        });

        binding.hideNewPassword.setOnClickListener(v -> {
            isNewPasswordVisible[0] = !isNewPasswordVisible[0];
            if (isNewPasswordVisible[0]) {
                binding.newPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.ivToggleNewPassword.setImageResource(R.drawable.ic_eye);
            } else {
                binding.newPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.ivToggleNewPassword.setImageResource(R.drawable.ic_eye_hidden);
            }
            binding.newPassword.setSelection(Objects.requireNonNull(binding.newPassword.getText()).length());
        });

        binding.hideReNewPassword.setOnClickListener(v -> {
            isReNewPasswordVisible[0] = !isReNewPasswordVisible[0];
            if (isReNewPasswordVisible[0]) {
                binding.reNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.ivToggleReNewPassword.setImageResource(R.drawable.ic_eye);
            } else {
                binding.reNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.ivToggleReNewPassword.setImageResource(R.drawable.ic_eye_hidden);
            }
            binding.reNewPassword.setSelection(Objects.requireNonNull(binding.reNewPassword.getText()).length());
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
