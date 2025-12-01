package com.movie_hub.android.ui.main.account.playlist.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.request.playlist.CreatePlaylistRequest;
import com.movie_hub.android.data.model.api.request.playlist.UpdatePlaylistRequest;
import com.movie_hub.android.data.model.api.response.playlist.PlayListResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.LayoutBottomSheetUpdatePlaylistBinding;

public class UpdatePlaylistDialogFragment extends DialogFragment {
    private PlayListResponse playlist;
    private LayoutBottomSheetUpdatePlaylistBinding binding;
    private UpdatePlaylistDialogCallback callback;

    public interface UpdatePlaylistDialogCallback {
        void onUpdateClicked(UpdatePlaylistRequest request);
        void onDeleteClicked(Long idPlaylist);
    }

    public UpdatePlaylistDialogFragment(UpdatePlaylistDialogCallback callback, PlayListResponse playlist) {
        this.callback = callback;
        this.playlist = playlist;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = LayoutBottomSheetUpdatePlaylistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.title.setText(playlist.getName());

        binding.btnDeleteList.setOnClickListener(v -> {
            callback.onDeleteClicked(playlist.getId());
            dismiss();
        });

        binding.btnSave.setOnClickListener(v -> {
            String title = binding.title.getText().toString().trim();
            if (title.isEmpty()) {
                new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.please_enter_title)).showMessage(requireContext());
                return;
            }

            UpdatePlaylistRequest request = new UpdatePlaylistRequest();
            request.setName(title);
            request.setId(playlist.getId());

            if (callback != null) {
                callback.onUpdateClicked(request);
            }

            dismiss();
        });

        binding.getRoot().setOnTouchListener((v, event) -> {
            hideKeyboard(binding.title);
            return false;
        });
        
        binding.title.requestFocus(); 
        binding.title.setSelection(binding.title.getText().length());

        binding.title.post(() -> {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(binding.title, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        binding.tvMaxLength.setText("/" + Constants.MaxLengthNamePlaylist);
        binding.title.setMaxLines(1);
        binding.title.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.MaxLengthNamePlaylist)});

        binding.title.addTextChangedListener(new TextWatcher() {
            CharSequence beforeText;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                binding.tvCount.setText(String.valueOf(length));
                binding.lCountChar.setVisibility(length > 0 ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

    }
    public void hideKeyboard(View view) {
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            binding.title.clearFocus();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM); // ✨ Hiện từ dưới lên
            window.setWindowAnimations(R.style.DialogAnimation); // ✨ Optional: Animation như BottomSheet
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }


}
