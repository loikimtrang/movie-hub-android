package com.movie_hub.android.ui.main.movie.detail.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.request.playlist.ActionUpdateItemPlaylistRequest;
import com.movie_hub.android.data.model.api.request.playlist.CreatePlaylistRequest;
import com.movie_hub.android.data.model.api.request.playlist.UpdatePlayListItemRequest;
import com.movie_hub.android.data.model.api.response.playlist.PlayListResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.LayoutBottomSheetAddToPlaylistBinding;
import com.movie_hub.android.ui.main.movie.detail.CreatePlayListCallback;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.ui.main.movie.detail.adapter.PlaylistMovieDetailItemAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddToPlaylistDialogFragment extends DialogFragment implements PlaylistMovieDetailItemAdapter.OnPlaylistClickListener, CreatePlayListCallback {

    private LayoutBottomSheetAddToPlaylistBinding binding;
    private AddToPlaylistPlaylistDialogCallback callback;
    private List<PlayListResponse> playListResponseList = new ArrayList<>();
    private PlaylistMovieDetailItemAdapter playlistMovieDetailItemAdapter;
    private Long movieId;

    private final int action_delete = 0;
    private final int action_add = 1;
    @Override
    public void onPlaylistClick(PlayListResponse playlist) {
        for (PlayListResponse response: playListResponseList) {
            if (response.getId().equals(playlist.getId())) {
                response.setSelect(playlist.isSelect());
                return;
            }
        }
    }
    @Override
    public void onCreateSuccessCallBack(PlayListResponse response) {
        if (response == null) {
            new ToastMessage(ToastMessage.TYPE_WARNING, Objects.requireNonNull(getContext()).getString(R.string.an_error_occurred)).showMessage(getContext());
            return;
        }

        updatePlayList(response);
    }

    public CreatePlayListCallback getCreatePlayListCallback() {
        return this;
    }

    public interface AddToPlaylistPlaylistDialogCallback {
        void onCreateClicked(CreatePlaylistRequest request);
        void onChooseClicked(UpdatePlayListItemRequest request);
    }

    private CreatePlayListCallback createPlayListCallback;

    public AddToPlaylistDialogFragment(AddToPlaylistPlaylistDialogCallback callback,
                                       List<PlayListResponse> playListResponseList,
                                       Long movieId) {
        this.callback = callback;
        this.playListResponseList = playListResponseList;
        this.movieId = movieId;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = LayoutBottomSheetAddToPlaylistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playlistMovieDetailItemAdapter = new PlaylistMovieDetailItemAdapter(this, getContext());
        binding.rvPlaylist.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false));

        binding.rvPlaylist.setAdapter(playlistMovieDetailItemAdapter);
        playlistMovieDetailItemAdapter.setData(playListResponseList);

        binding.btnCancelCreate.setOnClickListener(v -> {
            hideKeyboard(binding.title);
            binding.layoutCreate.setVisibility(View.GONE);
            binding.layoutContent.setVisibility(View.VISIBLE);
        });

        binding.btnCreate.setOnClickListener(v -> {
            String title = binding.title.getText().toString().trim();
            if (title.isEmpty()) {
                new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.please_enter_title)).showMessage(requireContext());
                return;
            }

            CreatePlaylistRequest request = new CreatePlaylistRequest();
            request.setName(title);

            if (callback != null) {
                callback.onCreateClicked(request);
            }
            hideKeyboard(binding.title);
            binding.title.setText("");
            binding.layoutCreate.setVisibility(View.GONE);
            binding.layoutContent.setVisibility(View.VISIBLE);
        });

        binding.getRoot().setOnTouchListener((v, event) -> {
            hideKeyboard(binding.title);
            return false;
        });

        binding.btnAddNew.setOnClickListener(v -> {

            if (playListResponseList.size() >= Constants.MaxPlaylist) {
                new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.limit_list)).showMessage(getContext());
                return;
            }

            binding.title.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(binding.title, InputMethodManager.SHOW_IMPLICIT);
            }
            binding.layoutContent.setVisibility(View.GONE);
            binding.layoutCreate.setVisibility(View.VISIBLE);
        });

        binding.btnChoose.setOnClickListener(v -> {

            if (playListResponseList == null || playListResponseList.isEmpty()) {
                dismiss();
                return;
            }

            UpdatePlayListItemRequest request = new UpdatePlayListItemRequest();
            request.setMovieId(movieId);
            request.setActions(new ArrayList<>());

            for (PlayListResponse response : playListResponseList) {
                ActionUpdateItemPlaylistRequest re = new ActionUpdateItemPlaylistRequest();
                re.setPlaylistId(response.getId());
                re.setAction(response.isSelect() ? action_add : action_delete);
                request.getActions().add(re);
            }


            callback.onChooseClicked(request);
            dismiss();
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
        }
        binding.title.clearFocus();
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

    public void updatePlayList(PlayListResponse response) {
        playListResponseList.add(0, response);
        playlistMovieDetailItemAdapter.setData(playListResponseList);

        new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.create_playlist_succes)).showMessage(getContext());
    }
}
