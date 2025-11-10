package com.movie_hub.android.ui.main.movie.watch.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

import com.movie_hub.android.databinding.LayoutBottomSheetSettingsPlaySpeedBinding;
import com.movie_hub.android.ui.main.movie.watch.setting.SettingVideoModel;

public class PlaySpeedBottomSheetDialog extends BaseBottomSheetDialog {

    private LayoutBottomSheetSettingsPlaySpeedBinding binding;
    private SettingVideoModel settingVideoModel;
    private final PlaySpeedBottomSheetCallback callback;
    public static final int TYPE_SPEED = 1;
    public static final int TYPE_SPEED_PRESS = 2;
    private int TYPE_SPEED_OPTION = 1;
    private static final float MIN_SPEED = 0.25f;
    private static final float MAX_SPEED = 2.00f;
    private static final float STEP = 0.05f;
    private static final int SEEKBAR_MAX = 100; // 0.25 → 2.0 → 175 steps → chia đều 100

    public interface PlaySpeedBottomSheetCallback {
        void updatePlaySpeedVideo(float speed, int typeSpeedOption);
    }

    public PlaySpeedBottomSheetDialog(@NonNull Context context,
                                      SettingVideoModel settingVideoModel,
                                      PlaySpeedBottomSheetCallback callback, int type) {
        super(context);
        this.settingVideoModel = settingVideoModel;
        this.callback = callback;
        this.TYPE_SPEED_OPTION = type;
        init();
    }

    private void init() {
        binding = LayoutBottomSheetSettingsPlaySpeedBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        setupView();
        setupListeners();
        binding.layoutSetting.setOnClickListener(v -> dismiss());
    }

    private void setupView() {
        float currentSpeed;
        if (TYPE_SPEED_OPTION == TYPE_SPEED) {
            currentSpeed = settingVideoModel.getPlaySpeed().getSpeed();
        } else {
            currentSpeed = settingVideoModel.getPlaySpeedWhenPress().getSpeed();
        }
        binding.seekBar.setHapticFeedbackEnabled(true);
        binding.btnMinus.setHapticFeedbackEnabled(true);
        binding.btnPlus.setHapticFeedbackEnabled(true);

        updateSpeedDisplay(currentSpeed);
        updateSeekBar(currentSpeed);
    }

    private void setupListeners() {
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float speed = progressToSpeed(progress);
                    updateSpeed(speed);
                    binding.seekBar.setHapticFeedbackEnabled(true);
                    performHaptic();
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                float speed = progressToSpeed(seekBar.getProgress());
                snapToNearestStep(speed);
            }
        });

        binding.btnPlus.setOnClickListener(v -> {
            adjustSpeed(STEP);
            performHaptic();
        });
        binding.btnMinus.setOnClickListener(v -> {
            adjustSpeed(-STEP);
            performHaptic();
        });

        binding.speed100.setOnClickListener(v -> setSpeed(1.00f));
        binding.speed125.setOnClickListener(v -> setSpeed(1.25f));
        binding.speed150.setOnClickListener(v -> setSpeed(1.50f));
        binding.speed200.setOnClickListener(v -> setSpeed(2.00f));
    }

    // === CHUYỂN ĐỔI GIỮA SEEKBAR & SPEED ===

    private float progressToSpeed(int progress) {
        return MIN_SPEED + (MAX_SPEED - MIN_SPEED) * progress / SEEKBAR_MAX;
    }

    private int speedToProgress(float speed) {
        return Math.round((speed - MIN_SPEED) / (MAX_SPEED - MIN_SPEED) * SEEKBAR_MAX);
    }

    private float snapToNearestStep(float speed) {
        float snapped = Math.round(speed / STEP) * STEP;
        snapped = Math.max(MIN_SPEED, Math.min(MAX_SPEED, snapped));
        setSpeed(snapped);
        return snapped;
    }

    // === ĐIỀU CHỈNH TỐC ĐỘ ===

    private void adjustSpeed(float delta) {
        float current;
        if (TYPE_SPEED_OPTION == TYPE_SPEED) {
            current = settingVideoModel.getPlaySpeed().getSpeed();
        } else {
            current = settingVideoModel.getPlaySpeedWhenPress().getSpeed();
        }
        float newSpeed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, current + delta));
        newSpeed = Math.round(newSpeed / STEP) * STEP;
        setSpeed(newSpeed);
    }

    private void setSpeed(float speed) {
        updateSpeed(speed);
        callback.updatePlaySpeedVideo(speed, TYPE_SPEED_OPTION); // GỬI VỀ ACTIVITY
    }

    private void updateSpeed(float speed) {
        if (TYPE_SPEED_OPTION == TYPE_SPEED) {
            settingVideoModel.getPlaySpeed().setSpeed(speed);
        } else {
            settingVideoModel.getPlaySpeedWhenPress().setSpeed(speed);
        }
        updateSpeedDisplay(speed);
        updateSeekBar(speed);
    }

    // === CẬP NHẬT UI ===

    @SuppressLint("SetTextI18n")
    private void updateSpeedDisplay(float speed) {
        binding.tvSpeed.setText(String.format("%.2fx", speed));
    }

    private void updateSeekBar(float speed) {
        binding.seekBar.setProgress(speedToProgress(speed));
    }

    private void performHaptic() {
        binding.getRoot().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}