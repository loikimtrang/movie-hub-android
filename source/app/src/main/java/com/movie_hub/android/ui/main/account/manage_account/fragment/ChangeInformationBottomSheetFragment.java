package com.movie_hub.android.ui.main.account.manage_account.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.user.UserChangePasswordRequest;
import com.movie_hub.android.data.model.api.request.user.UserUpdateProfileRequest;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.FragmentBottomSheetChangePasswordBinding;
import com.movie_hub.android.databinding.FragmentBottomSheetUpdateProfileBinding;
import com.movie_hub.android.helper.BlurEffectManager;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.AccountFragment;
import com.movie_hub.android.ui.main.account.manage_account.ManageAccountActivity;
import com.movie_hub.android.utils.ClickUtils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Objects;

public class ChangeInformationBottomSheetFragment extends BottomSheetDialogFragment {

    public static UserResponse PROFILE;
    private FragmentBottomSheetUpdateProfileBinding binding;
    private int gender;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentBottomSheetUpdateProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // RenderEffect (nếu Android 12+)
        BlurEffectManager.addBlurEffect(requireActivity());
        setUpViews();
        binding.layout.setOnClickListener(v -> hideKeyboard());

        binding.cancel.setOnClickListener(v -> onCancelClick());

        binding.update.setOnClickListener(v -> onUpdateClick());

        binding.male.setOnClickListener(v -> setUpGender(Constants.GENDER_MALE));
        binding.female.setOnClickListener(v -> setUpGender(Constants.GENDER_FEMALE));
        binding.unspecified.setOnClickListener(v -> setUpGender(Constants.GENDER_UNSPECIFIED));

        return view;
    }
    public void setUpViews() {
        if (PROFILE.getFullName() == null) {
            binding.name.setText(PROFILE.getUsername());
        } else {
            binding.name.setText(PROFILE.getFullName());
        }

        binding.phone.setText(PROFILE.getPhone());
        setUpGender(PROFILE.getGender());
    }
    @SuppressLint("ResourceAsColor")
    public void setUpGender(int gender) {
        setUpUnSelectGender();
        this.gender = gender;
        Context context = getContext();

        switch (gender) {
            case Constants.GENDER_MALE:
                binding.male.setTextColor(ContextCompat.getColor(context, R.color.text));
                binding.male.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_item_stroke_white_radius_6sdp));
                break;
            case Constants.GENDER_FEMALE:
                binding.female.setTextColor(ContextCompat.getColor(context, R.color.text));
                binding.female.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_item_stroke_white_radius_6sdp));
                break;
            case Constants.GENDER_UNSPECIFIED:
                binding.unspecified.setTextColor(ContextCompat.getColor(context, R.color.text));
                binding.unspecified.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_item_stroke_white_radius_6sdp));
                break;
        }
    }

    public void setUpUnSelectGender() {
        Context context = getContext();

        binding.male.setTextColor(ContextCompat.getColor(context, R.color.text_gray));
        binding.female.setTextColor(ContextCompat.getColor(context, R.color.text_gray));
        binding.unspecified.setTextColor(ContextCompat.getColor(context, R.color.text_gray));

        binding.male.setBackground(null);
        binding.female.setBackground(null);
        binding.unspecified.setBackground(null);
    }

    public void onUpdateClick() {
        if (validateInput()) {
            ClickUtils.debounceClick(binding.update);
            hideKeyboard();
            clearAllFocus();
            binding.update.setText("");
            binding.loadingUpdate.setVisibility(View.VISIBLE);
            binding.update.setClickable(false);

            UserUpdateProfileRequest request = new UserUpdateProfileRequest();

            request.setUsername(PROFILE.getUsername());
            request.setAvatarPath(PROFILE.getAvatarPath());

            request.setFullName(Objects.requireNonNull(binding.name.getText()).toString().trim());
            request.setPhone(Objects.requireNonNull(binding.phone.getText()).toString().trim());

            request.setGender(this.gender);

            ((ManageAccountActivity) requireActivity()).userChangeInformation(request, new MainCallback<ResponseWrapper>() {
                @Override
                public void doSuccess(ResponseWrapper object) {
                    hideKeyboard();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> dismiss(), 500);
                }
                @Override
                public void doError(Throwable throwable) {
                    if (throwable instanceof UnknownHostException || throwable instanceof SocketTimeoutException) {
                        showUpdateError(getString(R.string.network_error_please_check_your_internet_connection));
                    } else if (throwable instanceof ConnectException) {
                        showUpdateError(getString(R.string.cannot_connect_to_the_server_please_try_again));
                    } else {
                        showUpdateError(getString(R.string.mgs_update_failed));
                    }
                }

                @Override
                public void doSuccess() {

                }

                @Override
                public void doFail() {
                    showUpdateError(getString(R.string.mgs_update_failed));
                }
            });
        }
    }
    public boolean validateInput() {
        String fullName = binding.name.getText().toString().trim();
        String phone = binding.phone.getText().toString().trim();

        if (fullName.isEmpty() || phone.isEmpty()) {
            new ToastMessage(
                    ToastMessage.TYPE_WARNING,
                    getString(R.string.mgs_please_fill_in_all_the_required_information)
            ).showMessage(getContext());
            return false;
        }

        if (!isValidVietnamPhoneNumber(phone)) {
            new ToastMessage(
                    ToastMessage.TYPE_WARNING,
                    getString(R.string.mgs_invalid_phone_number)
            ).showMessage(getContext());
            return false;
        }

        return true;
    }
    private boolean isValidVietnamPhoneNumber(String phone) {
        String regex = "^(03|05|07|08|09)[0-9]{8}$";
        return phone.matches(regex);
    }

    private void showUpdateError(String message) {
        new ToastMessage(ToastMessage.TYPE_WARNING, message).showMessage(getContext());

        binding.update.setText(getString(R.string.update));
        binding.loadingUpdate.setVisibility(View.GONE);
        binding.update.setClickable(true);
    }
    public void onCancelClick() {
        hideKeyboard();
        dismiss();
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
