package com.movie_hub.android.ui.main.movie.watch.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.LayoutBottomSheetSettingsQualityBinding;
import com.movie_hub.android.ui.main.movie.watch.dialog.adapter.QualityItemAdapter;
import com.movie_hub.android.ui.main.movie.watch.setting.SettingVideoModel;
import com.movie_hub.android.ui.main.movie.watch.setting.VideoQuality;

public class QualityBottomSheetDialog extends BaseBottomSheetDialog implements QualityItemAdapter.OnItemClickListener{

    private LayoutBottomSheetSettingsQualityBinding binding;
    private SettingVideoModel settingVideoModel;
    private final QualityBottomSheetCallback callback;
    private QualityItemAdapter adapter;
    public static MutableLiveData<VideoQuality> videoQuality = new MutableLiveData<>();

    @Override
    public void onItemClick(SettingVideoModel setting) {
        callback.updateQualityVideo(setting);
        dismiss();
    }

    public interface QualityBottomSheetCallback {
        void updateQualityVideo(SettingVideoModel settingVideoModel);
    }

    public QualityBottomSheetDialog(@NonNull Context context,
                                    SettingVideoModel settingVideoModel,
                                    QualityBottomSheetCallback callback) {
        super(context);
        this.settingVideoModel = settingVideoModel;
        this.callback = callback;
        init();
    }

    private void init() {
        binding = LayoutBottomSheetSettingsQualityBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        setupView();
        binding.layoutSetting.setOnClickListener(v -> dismiss());

        videoQuality.observeForever(qualityObserver);
    }

    private void setupView() {
        adapter = new QualityItemAdapter(settingVideoModel, this, getContext());
        binding.rvQuality.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvQuality.setAdapter(adapter);
    }

    @SuppressLint("SetTextI18n")
    private final Observer<VideoQuality> qualityObserver = q -> {
        if (q != null) {
            if (settingVideoModel.getQuality().isAuto()) {
                binding.tvCurrentQuality.setText(getContext().getString(R.string.current_quality) + " " + getContext().getString(R.string.auto) + "(" + q.label +")") ;
            } else {
                binding.tvCurrentQuality.setText(getContext().getString(R.string.current_quality) + settingVideoModel.getQuality().getResolution().label);
            }
        }
    };

    @Override
    public void dismiss() {
        super.dismiss();
        videoQuality.removeObserver(qualityObserver);
    }
}