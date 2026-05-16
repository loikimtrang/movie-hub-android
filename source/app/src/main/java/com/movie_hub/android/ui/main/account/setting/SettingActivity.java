package com.movie_hub.android.ui.main.account.setting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.setting.UserSettingsRequest;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivitySettingAccountBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.setting.adapter.SettingAccountModel;
import com.movie_hub.android.ui.main.account.setting.adapter.SettingAdapter;
import com.movie_hub.android.ui.main.movie.watch.setting.SubtitleStyle;
import com.movie_hub.android.utils.DialogUtils;
import com.movie_hub.android.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SettingActivity extends BaseActivity<ActivitySettingAccountBinding, SettingViewModel>
        implements SystemBarColorProvider {

    private SettingAdapter settingAdapter;
    private final List<SettingAccountModel> resolutionList = new ArrayList<>();
    private AudioManager audioManager;
    private SubtitleSettingBinder subtitleSettingBinder;
    private SubtitleStyle subtitleStyle = new SubtitleStyle();

    @Override
    public int getLayoutId() {
        return R.layout.activity_setting_account;
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public int getStatusBarColor() {
        return R.color.header_app;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_app;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        setUpView();

        viewBinding.btnSelectResolution.setOnClickListener(v ->
                viewBinding.layoutResolution.setVisibility(View.VISIBLE));

        viewBinding.layoutResolution.setOnClickListener(v ->
                viewBinding.layoutResolution.setVisibility(View.GONE));

        viewBinding.save.setOnClickListener(v -> onUpdateClick());
    }

    public void setUpView() {
        String json = getIntent().getStringExtra("USER_RESPONSE");
        if (json == null || json.isEmpty()) return;

        viewModel.userResponse = GsonUtils.fromJson(json, UserResponse.class);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        String settingJson = viewModel.userResponse.getSettings();
        if (settingJson != null && !settingJson.isEmpty()) {
            viewModel.setting = GsonUtils.fromJson(settingJson, UserSettingsRequest.class);
        } else {
            viewModel.setting = new UserSettingsRequest();
            viewModel.setting.setPlaybackSpeed(1.0);
            viewModel.setting.setAutoSkipIntro(false);
            viewModel.setting.setAutoNextEpisode(false);
            viewModel.setting.setResolution(2);
            viewModel.setting.setBrightness(50);
            viewModel.setting.setAudio(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            applyDefaultSubtitleSettings();
        }

        if (viewModel.setting.getSubtitleEnabled() == null) {
            viewModel.setting.setSubtitleEnabled(true);
        }
        if (viewModel.setting.getSubtitleFontSize() == null) {
            applyDefaultSubtitleSettings();
        }

        viewBinding.switchAutoSkipIntro.setChecked(viewModel.setting.getAutoSkipIntro());
        viewBinding.switchAutoNextEpisode.setChecked(viewModel.setting.getAutoNextEpisode());

        String resName = getResolutionName(viewModel.setting.getResolution());
        viewBinding.tvResolution.setText(resName);

        initData();
        setUpAdapter();
        setupPlaySpeedSeekBar();
        setupBrightnessSeekBar();
        setupVolumeSeekBar();
        setupSubtitleSettings();

        viewBinding.switchAutoSkipIntro.setOnCheckedChangeListener((v, isChecked) ->
                viewModel.setting.setAutoSkipIntro(isChecked));
        viewBinding.switchAutoNextEpisode.setOnCheckedChangeListener((v, isChecked) ->
                viewModel.setting.setAutoNextEpisode(isChecked));
    }

    private void applyDefaultSubtitleSettings() {
        SubtitleStyle defaults = new SubtitleStyle();
        defaults.applyToUserSettings(viewModel.setting);
        viewModel.setting.setSubtitleEnabled(true);
    }

    private void setupSubtitleSettings() {
        subtitleStyle = SubtitleStyle.fromUserSettings(viewModel.setting);

        subtitleSettingBinder = new SubtitleSettingBinder(viewBinding.subtitleSettingPanel);
        subtitleSettingBinder.setListener(style -> {
            subtitleStyle = style;
            subtitleStyle.applyToUserSettings(viewModel.setting);
        });
        subtitleSettingBinder.bind(subtitleStyle);

        viewBinding.switchSubtitleEnabled.setChecked(
                Boolean.TRUE.equals(viewModel.setting.getSubtitleEnabled()));

        viewBinding.switchSubtitleEnabled.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setting.setSubtitleEnabled(isChecked));
    }

    private String getResolutionName(int resId) {
        switch (resId) {
            case 0: return getString(R.string.auto);
            case 1: return getString(R.string.quality_720p);
            case 3: return getString(R.string.quality_2k);
            case 4: return getString(R.string.max_resolution);
            default: return getString(R.string.quality_1080p);
        }
    }

    private void initData() {
        int savedResId = (viewModel.setting.getResolution() != null)
                ? viewModel.setting.getResolution() : 2;

        resolutionList.clear();
        resolutionList.add(new SettingAccountModel(getString(R.string.auto), 0, savedResId == 0));
        resolutionList.add(new SettingAccountModel(getString(R.string.quality_720p), 1, savedResId == 1));
        resolutionList.add(new SettingAccountModel(getString(R.string.quality_1080p), 2, savedResId == 2));
        resolutionList.add(new SettingAccountModel(getString(R.string.quality_2k), 3, savedResId == 3));
        resolutionList.add(new SettingAccountModel(getString(R.string.max_resolution), 4, savedResId == 4));
    }

    public void setUpAdapter() {
        settingAdapter = new SettingAdapter(resolutionList, item -> {
            viewModel.setting.setResolution(item.getResolution());
            viewBinding.tvResolution.setText(item.getName());

            for (SettingAccountModel model : resolutionList) {
                model.setCheck(model.getResolution() == item.getResolution());
            }

            settingAdapter.notifyDataSetChanged();
            viewBinding.layoutResolution.setVisibility(View.GONE);
        });

        viewBinding.rvResolution.setLayoutManager(new LinearLayoutManager(this));
        viewBinding.rvResolution.setAdapter(settingAdapter);
    }

    private void setupPlaySpeedSeekBar() {
        viewBinding.seekBarPlaySpeed.setMax(35);

        double speed = (viewModel.setting.getPlaybackSpeed() != null)
                ? viewModel.setting.getPlaybackSpeed() : 1.0;

        int progress = (int) ((speed - 0.25) / 0.05);
        viewBinding.seekBarPlaySpeed.setProgress(progress);
        viewBinding.tvPlaySpeed.setText(String.format(Locale.US, "%.2fx", speed));

        viewBinding.seekBarPlaySpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float newSpeed = 0.25f + (progress * 0.05f);
                viewBinding.tvPlaySpeed.setText(String.format(Locale.US, "%.2fx", newSpeed));
                viewModel.setting.setPlaybackSpeed((double) newSpeed);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @SuppressLint("SetTextI18n")
    private void setupBrightnessSeekBar() {
        viewBinding.seekBarBrightness.setMax(100);

        int oldBrightness = (viewModel.setting.getBrightness() != null)
                ? viewModel.setting.getBrightness() : 50;

        viewBinding.seekBarBrightness.setProgress(oldBrightness);
        viewBinding.tvBrightness.setText(oldBrightness + "%");

        viewBinding.seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                viewBinding.tvBrightness.setText(progress + "%");
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.screenBrightness = progress / 100f;
                getWindow().setAttributes(lp);
                viewModel.setting.setBrightness(progress);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @SuppressLint("SetTextI18n")
    private void setupVolumeSeekBar() {
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        viewBinding.seekBarVolume.setMax(maxVol);

        int oldVol = (viewModel.setting.getAudio() != null)
                ? viewModel.setting.getAudio()
                : audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        viewBinding.seekBarVolume.setProgress(oldVol);
        viewBinding.tvVolume.setText((int) ((double) oldVol / maxVol * 100) + "%");

        viewBinding.seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                int percent = (int) ((double) progress / maxVol * 100);
                viewBinding.tvVolume.setText(percent + "%");
                viewModel.setting.setAudio(progress);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    public void onUpdateClick() {
        if (subtitleSettingBinder != null) {
            subtitleSettingBinder.getStyle().applyToUserSettings(viewModel.setting);
        }
        DialogUtils.dialogConfirm(
                this,
                getString(R.string.do_you_want_update_setting),
                getString(R.string.confirm),
                (dialog, which) -> updateSetting(),
                getString(R.string.cancel),
                (dialog, which) -> dialog.dismiss()
        );
    }

    public void updateSetting() {
        showLoading();
        viewModel.updateSetting(new MainCallback<ResponseWrapper>() {
            @Override
            public void doError(Throwable error) {
                hideLoading();
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred))
                        .showMessage(getApplicationContext());
            }

            @Override
            public void doSuccess() {}

            @Override
            public void doSuccess(ResponseWrapper responseWrapper) {
                hideLoading();
                new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.setting_have_been_success_update))
                        .showMessage(getApplicationContext());
            }

            @Override
            public void doFail() {
                hideLoading();
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred))
                        .showMessage(getApplicationContext());
            }
        }, viewModel.setting);
    }
}
