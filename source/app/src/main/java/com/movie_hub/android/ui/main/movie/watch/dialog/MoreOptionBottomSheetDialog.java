package com.movie_hub.android.ui.main.movie.watch.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.LayoutBottomSheetSettingsBinding;
import com.movie_hub.android.databinding.LayoutBottomSheetSettingsMoreOptionBinding;
import com.movie_hub.android.ui.main.movie.watch.setting.SettingVideoModel;
import com.movie_hub.android.ui.main.movie.watch.setting.VideoQuality;

public class MoreOptionBottomSheetDialog extends BaseBottomSheetDialog {

    private LayoutBottomSheetSettingsMoreOptionBinding binding;
    private SettingVideoModel settingVideoModel;
    private final MoreOptionBottomSheetCallback callback;
    public static MutableLiveData<VideoQuality> videoQuality = new MutableLiveData<>();
    public interface MoreOptionBottomSheetCallback {
        void onPlaybackSpeedPressClicked();
    }
    public MoreOptionBottomSheetDialog(@NonNull Context context, SettingVideoModel settingVideoModel, MoreOptionBottomSheetCallback callback) {
        super(context);
        this.settingVideoModel = settingVideoModel;
        this.callback = callback;
        init();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        binding = LayoutBottomSheetSettingsMoreOptionBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        setUpView();

        binding.layoutSetting.setOnClickListener(v -> dismiss());
        binding.btnPlaySpeed.setOnClickListener(v -> onPlaySpeedClicked());
        binding.btnSubtitle.setOnClickListener(v -> onSubtitleClicked());

    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void setUpView() {
        binding.tvPlaySpeed.setText(String.format("%.2fx",settingVideoModel.getPlaySpeedWhenPress().getSpeed()));
    }
    private void onPlaySpeedClicked() {
        callback.onPlaybackSpeedPressClicked();
        dismiss();
    }
    private void onSubtitleClicked() { }
}