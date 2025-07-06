package com.movie_hub.android.ui.main.account.language.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.custom.CustomDialog;
import com.movie_hub.android.databinding.ItemLanguageMenuBinding;
import com.movie_hub.android.ui.main.account.language.model.LanguageItemModel;

import java.util.List;

public class LanguageMenuAdapter extends RecyclerView.Adapter<LanguageMenuAdapter.LanguageMenuViewHolder> {

    private final List<LanguageItemModel> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LanguageItemModel item);
    }

    public LanguageMenuAdapter(List<LanguageItemModel> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LanguageMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemLanguageMenuBinding binding = ItemLanguageMenuBinding.inflate(inflater, parent, false);
        return new LanguageMenuViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageMenuViewHolder holder, int position) {
        LanguageItemModel item = items.get(position);
        holder.binding.language.setText(item.stringResId);
        holder.binding.checked.setVisibility(item.isCheck ? View.VISIBLE : View.GONE);

        holder.binding.getRoot().setOnClickListener(v -> {
            if (!item.isCheck) {
                CustomDialog.show(v.getContext(), R.string.mgs_change_language, new CustomDialog.DialogCallback() {
                    @Override
                    public void onConfirm() {
                        for (LanguageItemModel lang : items) {
                            lang.isCheck = false;
                        }

                        item.isCheck = true;
                        notifyDataSetChanged();
                        listener.onItemClick(item);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LanguageMenuViewHolder extends RecyclerView.ViewHolder {
        ItemLanguageMenuBinding binding;

        LanguageMenuViewHolder(ItemLanguageMenuBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

