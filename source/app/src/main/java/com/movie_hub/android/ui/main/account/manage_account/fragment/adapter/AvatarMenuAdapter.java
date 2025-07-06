package com.movie_hub.android.ui.main.account.manage_account.fragment.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.databinding.ItemAvatarMenuBinding;
import com.movie_hub.android.databinding.ItemAvatarMenuBinding;
import com.movie_hub.android.ui.main.account.manage_account.model.ManageAccountItemModel;
import com.movie_hub.android.utils.ClickUtils;

import java.util.List;

public class AvatarMenuAdapter extends RecyclerView.Adapter<AvatarMenuAdapter.AvatarMenuViewHolder> {

    private final List<ManageAccountItemModel> items;
    private final OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(ManageAccountItemModel item);
    }

    public AvatarMenuAdapter(List<ManageAccountItemModel> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AvatarMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAvatarMenuBinding binding = ItemAvatarMenuBinding.inflate(inflater, parent, false);
        return new AvatarMenuViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AvatarMenuViewHolder holder, int position) {
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

    static class AvatarMenuViewHolder extends RecyclerView.ViewHolder {
        ItemAvatarMenuBinding binding;

        AvatarMenuViewHolder(ItemAvatarMenuBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

