package com.movie_hub.android.ui.main.account.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.ui.main.account.model.MenuItemModel;

import java.util.List;

public class AccountMenuAdapter extends RecyclerView.Adapter<AccountMenuAdapter.MenuViewHolder> {

    private final List<MenuItemModel> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MenuItemModel item);
    }

    public AccountMenuAdapter(List<MenuItemModel> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItemModel item = items.get(position);
        holder.icon.setImageResource(item.iconResId);
        holder.title.setText(item.title);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;

        MenuViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
        }
    }
}

