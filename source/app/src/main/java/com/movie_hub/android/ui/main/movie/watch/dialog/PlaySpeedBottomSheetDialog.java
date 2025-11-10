package com.movie_hub.android.ui.main.movie.watch.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

import com.movie_hub.android.databinding.LayoutBottomSheetSettingsPlaySpeedBinding;
import com.movie_hub.android.ui.main.movie.watch.setting.SettingVideoModel;

public class PlaySpeedBottomSheetDialog extends BaseBottomSheetDialog {

    private LayoutBottomSheetSettingsPlaySpeedBinding binding;
    private SettingVideoModel settingVideoModel;
    private final PlaySpeedBottomSheetCallback callback;

    private static final float MIN_SPEED = 0.25f;
    private static final float MAX_SPEED = 2.00f;
    private static final float STEP = 0.05f;
    private static final int SEEKBAR_MAX = 100; // 0.25 → 2.0 → 175 steps → chia đều 100

    public interface PlaySpeedBottomSheetCallback {
        void updatePlaySpeedVideo(float speed);
    }

    public PlaySpeedBottomSheetDialog(@NonNull Context context,
                                      SettingVideoModel settingVideoModel,
                                      PlaySpeedBottomSheetCallback callback) {
        super(context);
        this.settingVideoModel = settingVideoModel;
        this.callback = callback;
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
        float currentSpeed = settingVideoModel.getPlaySpeed().getSpeed();
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
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                float speed = progressToSpeed(seekBar.getProgress());
                snapToNearestStep(speed);
            }
        });

        binding.btnPlus.setOnClickListener(v -> adjustSpeed(STEP));
        binding.btnMinus.setOnClickListener(v -> adjustSpeed(-STEP));

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
        float current = settingVideoModel.getPlaySpeed().getSpeed();
        float newSpeed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, current + delta));
        newSpeed = Math.round(newSpeed / STEP) * STEP;
        setSpeed(newSpeed);
    }

    private void setSpeed(float speed) {
        updateSpeed(speed);
        callback.updatePlaySpeedVideo(speed); // GỬI VỀ ACTIVITY
    }

    private void updateSpeed(float speed) {
        settingVideoModel.getPlaySpeed().setSpeed(speed);
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
}