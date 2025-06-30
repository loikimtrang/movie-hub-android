package com.movie_hub.android.ui.main.account.manage_account.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.databinding.ItemManageAccountMenuBinding;
import com.movie_hub.android.ui.main.account.manage_account.model.ManageAccountItemModel;
import com.movie_hub.android.utils.ClickUtils;

import java.util.List;

public class ManageAccountMenuAdapter extends RecyclerView.Adapter<ManageAccountMenuAdapter.LanguageMenuViewHolder> {

    private final List<ManageAccountItemModel> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ManageAccountItemModel item);
    }

    public ManageAccountMenuAdapter(List<ManageAccountItemModel> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LanguageMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemManageAccountMenuBinding binding = ItemManageAccountMenuBinding.inflate(inflater, parent, false);
        return new LanguageMenuViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageMenuViewHolder holder, int position) {
        ManageAccountItemModel item = items.get(position);
        holder.binding.icon.setImageResource(item.idIcon);
        holder.binding.title.setText(item.idTitle);
        holder.itemView.setOnClickListener( v-> {
            ClickUtils.debounceClick(holder.itemView);
            listener.onItemClick(item);
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LanguageMenuViewHolder extends RecyclerView.ViewHolder {
        ItemManageAccountMenuBinding binding;

        LanguageMenuViewHolder(ItemManageAccountMenuBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

