package com.movie_hub.android.ui.main.account.setting.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.custom.CustomDialog;
import com.movie_hub.android.databinding.ItemSettingMenuBinding;

import java.util.List;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.LanguageMenuViewHolder> {

    private final List<SettingAccountModel> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SettingAccountModel item);
    }

    public SettingAdapter(List<SettingAccountModel> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LanguageMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSettingMenuBinding binding = ItemSettingMenuBinding.inflate(inflater, parent, false);
        return new LanguageMenuViewHolder(binding);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull LanguageMenuViewHolder holder, int position) {
        SettingAccountModel item = items.get(position);
        holder.binding.name.setText(item.getName());
        holder.binding.checked.setVisibility(item.isCheck ? View.VISIBLE : View.GONE);

        holder.binding.getRoot().setOnClickListener(v -> {
            if (!item.isCheck) {
                for (SettingAccountModel res : items) {
                    res.isCheck = false;
                }
                item.isCheck = true;
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onItemClick(item);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LanguageMenuViewHolder extends RecyclerView.ViewHolder {
        ItemSettingMenuBinding binding;

        LanguageMenuViewHolder(ItemSettingMenuBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

