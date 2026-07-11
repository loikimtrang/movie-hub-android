package com.movie_hub.android.ui.main.home.filter.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.ItemFilterHomeBinding;
import com.movie_hub.android.ui.main.home.filter.model.FilterItemModel;

import java.util.ArrayList;
import java.util.List;

public class FilterHomeAdapter extends RecyclerView.Adapter<FilterHomeAdapter.FilterHomeViewHolder> {

    private final List<FilterItemModel> items = new ArrayList<>();
    private OnDeleteFilterClick listener;

    public interface OnDeleteFilterClick {
        void onDeleteClick();
    }
    private Context context;
    public FilterHomeAdapter(OnDeleteFilterClick listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }
    public void setData(List<FilterItemModel> newData) {
        items.clear();
        if (newData != null) {
            items.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FilterHomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFilterHomeBinding binding = ItemFilterHomeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FilterHomeViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FilterHomeViewHolder holder, int position) {
        FilterItemModel item = items.get(position);
        holder.binding.tvTitle.setText(item.getName());
        if (position == items.size() - 1) {
            holder.binding.content.setBackground(
                    ContextCompat.getDrawable(context, R.drawable.bg_item_filter_delete));
            holder.binding.tvTitle.setTextColor(
                    ContextCompat.getColor(context, R.color.white));
        } else {
            holder.binding.content.setBackground(
                    ContextCompat.getDrawable(context, R.drawable.bg_item_filter_home));
            holder.binding.tvTitle.setTextColor(
                    ContextCompat.getColor(context, R.color.filter_select));
        }

        holder.itemView.setOnClickListener(v -> {
            if (position == items.size() - 1) {
                if (listener == null) return;
                listener.onDeleteClick();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FilterHomeViewHolder extends RecyclerView.ViewHolder {
        final ItemFilterHomeBinding binding;

        public FilterHomeViewHolder(ItemFilterHomeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}