package com.movie_hub.android.ui.main.movie.watch.dialog;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.movie_hub.android.databinding.LayoutBottomSheetSettingsSubtitleCustomizeBinding;
import com.movie_hub.android.ui.main.account.setting.SubtitleSettingBinder;
import com.movie_hub.android.ui.main.movie.watch.setting.SettingVideoModel;

public class SubtitleCustomizeBottomSheetDialog extends BaseBottomSheetDialog {

    private final SettingVideoModel settingVideoModel;
    private SubtitleSettingBinder subtitleSettingBinder;

    public SubtitleCustomizeBottomSheetDialog(@NonNull Context context, SettingVideoModel settingVideoModel) {
        super(context);
        this.settingVideoModel = settingVideoModel;
        init();
    }

    private void init() {
        LayoutBottomSheetSettingsSubtitleCustomizeBinding binding =
                LayoutBottomSheetSettingsSubtitleCustomizeBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        binding.layoutSetting.setOnClickListener(v -> dismiss());

        subtitleSettingBinder = new SubtitleSettingBinder(binding.subtitleSettingContent);
        subtitleSettingBinder.setListener(style -> settingVideoModel.updateSubtitleStyle(style));
        subtitleSettingBinder.bind(settingVideoModel.getSubtitleStyle());
    }
}
