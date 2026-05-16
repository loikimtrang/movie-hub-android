package com.movie_hub.android.ui.main.movie.watch.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.LayoutBottomSheetSettingsBinding;
import com.movie_hub.android.ui.main.movie.watch.setting.SettingVideoModel;
import com.movie_hub.android.ui.main.movie.watch.setting.VideoQuality;

import java.util.Locale;

public class SettingBottomSheetDialog extends BaseBottomSheetDialog {

    private LayoutBottomSheetSettingsBinding binding;
    private SettingVideoModel settingVideoModel;
    private final SettingBottomSheetCallback callback;
    private final boolean hidePlaybackSpeedAndMoreOptions;
    private final String currentSubtitleLabel;
    public static MutableLiveData<VideoQuality> videoQuality = new MutableLiveData<>();
    public interface SettingBottomSheetCallback {
        void onQualityClicked();
        void onPlaybackSpeedClicked();
        void onSubtitleClicked();
        void onLockScreenClicked();
        void onMoreOptionsClicked();
    }
    public SettingBottomSheetDialog(@NonNull Context context, SettingVideoModel settingVideoModel,
                                    SettingBottomSheetCallback callback) {
        this(context, settingVideoModel, null, callback, false);
    }

    public SettingBottomSheetDialog(@NonNull Context context, SettingVideoModel settingVideoModel,
                                    SettingBottomSheetCallback callback, boolean hidePlaybackSpeedAndMoreOptions) {
        this(context, settingVideoModel, null, callback, hidePlaybackSpeedAndMoreOptions);
    }

    public SettingBottomSheetDialog(@NonNull Context context, SettingVideoModel settingVideoModel,
                                    String currentSubtitleLabel,
                                    SettingBottomSheetCallback callback, boolean hidePlaybackSpeedAndMoreOptions) {
        super(context);
        this.settingVideoModel = settingVideoModel;
        this.currentSubtitleLabel = currentSubtitleLabel;
        this.callback = callback;
        this.hidePlaybackSpeedAndMoreOptions = hidePlaybackSpeedAndMoreOptions;
        init();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        binding = LayoutBottomSheetSettingsBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        setUpView();
        setupTransparentWindow();

        if (hidePlaybackSpeedAndMoreOptions) {
            binding.btnPlaySpeed.setVisibility(View.GONE);
            binding.btnMoreOption.setVisibility(View.GONE);
        }

        binding.layoutSetting.setOnClickListener(v -> dismiss());
        binding.btnQuality.setOnClickListener(v -> onQualityClicked());
        binding.btnPlaySpeed.setOnClickListener(v -> onPlaySpeedClicked());
        binding.btnSubtitle.setOnClickListener(v -> onSubtitleClicked());
        binding.btnLockScreen.setOnClickListener(v -> onLockScreenClicked());
        binding.btnMoreOption.setOnClickListener(v -> onMoreOptionClicked());

        videoQuality.observeForever(qualityObserver);
        settingVideoModel.getPlaybackSpeedLive().observeForever(playSpeedObserver);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void setUpView() {
        if (settingVideoModel.getQuality().isAuto()) {
            binding.tvQuality.setText(getContext().getString(R.string.auto));
        } else {
            binding.tvQuality.setText(settingVideoModel.getQuality().getResolution().label);
        }

        if (settingVideoModel.getPlaySpeed().getSpeed() == 1.0f) {
            binding.tvPlaySpeed.setText(getContext().getString(R.string.normal) + " ("
                                + String.format(Locale.US, "%.2fx",settingVideoModel.getPlaySpeed().getSpeed()) + ")");
        } else {
            binding.tvPlaySpeed.setText(String.format(Locale.US, "%.2fx",settingVideoModel.getPlaySpeed().getSpeed()));
        }

        binding.tvSubtitle.setText(
                currentSubtitleLabel != null ? currentSubtitleLabel : getContext().getString(R.string.off));
    }

    private void onQualityClicked() {
        callback.onQualityClicked();
        dismiss();
    }
    private void onPlaySpeedClicked() {
        callback.onPlaybackSpeedClicked();
        dismiss();
    }
    private void onSubtitleClicked() {
        callback.onSubtitleClicked();
        dismiss();
    }
    private void onLockScreenClicked() {
        callback.onLockScreenClicked();
        dismiss();
    }
    private void onMoreOptionClicked() {
        callback.onMoreOptionsClicked();
        dismiss();
    }
    @SuppressLint("SetTextI18n")
    private final Observer<VideoQuality> qualityObserver = q -> {
        if (q != null) {
            if (settingVideoModel.getQuality().isAuto()) {
                binding.tvQuality.setText(getContext().getString(R.string.auto) + "(" + q.label +")") ;
            } else {
                binding.tvQuality.setText(settingVideoModel.getQuality().getResolution().label);
            }
        }
    };

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private final Observer<Float> playSpeedObserver = speed -> {
        if (speed == null) return;
        if (speed == 1.0f) {
            binding.tvPlaySpeed.setText(getContext().getString(R.string.normal) + " ("
                    + String.format(Locale.US, "%.2fx", speed) + ")");
        } else {
            binding.tvPlaySpeed.setText(String.format(Locale.US, "%.2fx", speed));
        }
    };

    @Override
    public void dismiss() {
        super.dismiss();
        videoQuality.removeObserver(qualityObserver);
        settingVideoModel.getPlaybackSpeedLive().removeObserver(playSpeedObserver);
    }
}