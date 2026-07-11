package com.movie_hub.android.ui.main.movie.watch.dialog.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.custom.CustomDialog;
import com.movie_hub.android.databinding.ItemLanguageMenuBinding;
import com.movie_hub.android.databinding.ItemQualityVideoBinding;
import com.movie_hub.android.ui.main.account.language.model.LanguageItemModel;
import com.movie_hub.android.ui.main.movie.watch.setting.SettingVideoModel;
import com.movie_hub.android.ui.main.movie.watch.setting.VideoQuality;

import java.util.List;
import java.util.Objects;

public class QualityItemAdapter extends RecyclerView.Adapter<QualityItemAdapter.QualityItemViewHolder> {
    private final OnItemClickListener listener;
    private SettingVideoModel settingVideoModel;
    private Context context;
    public interface OnItemClickListener {
        void onItemClick(SettingVideoModel settingVideoModel);
    }

    public QualityItemAdapter(SettingVideoModel settingVideoModel, OnItemClickListener listener, Context context) {
        this.settingVideoModel = settingVideoModel;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public QualityItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemQualityVideoBinding binding = ItemQualityVideoBinding.inflate(inflater, parent, false);
        return new QualityItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull QualityItemViewHolder holder, int position) {
        VideoQuality item = settingVideoModel.getAvailableQualities().get(position);
        holder.binding.tvQuality.setText(item.label);
        holder.binding.icCheck.setVisibility(item.isCheck ? View.VISIBLE : View.GONE);

        holder.binding.getRoot().setOnClickListener(v -> {
            if (!item.isCheck) {
                for (VideoQuality quality : settingVideoModel.getAvailableQualities()) {
                    quality.isCheck = false;
                    if (Objects.equals(item.label, quality.label)) {
                        quality.isCheck = true;
                        item.isCheck = true;
                    }
                }

                if (!Objects.equals(item.label, context.getString(R.string.auto))) {
                    settingVideoModel.getQuality().setAuto(false);
                    settingVideoModel.getQuality().setResolution(item);
                } else {
                    settingVideoModel.getQuality().setAuto(true);
                    settingVideoModel.getQuality().setResolution(null);
                }
                listener.onItemClick(settingVideoModel);

                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return settingVideoModel.getAvailableQualities().size();
    }

    static class QualityItemViewHolder extends RecyclerView.ViewHolder {
        ItemQualityVideoBinding binding;

        QualityItemViewHolder(ItemQualityVideoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

