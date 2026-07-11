package com.movie_hub.android.ui.main.home.filter.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.databinding.ItemFilterCategoryBinding;

import java.util.ArrayList;
import java.util.List;

public class FilterCategoryItemAdapter extends RecyclerView.Adapter<FilterCategoryItemAdapter.FilterCategoryItemViewHolder> {

    private final List<CategoryResponse> items = new ArrayList<>();
    private OnFilterClick listener;
    private Context context;
    public FilterCategoryItemAdapter(OnFilterClick listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }
    public void setData(List<CategoryResponse> newData) {
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
    public FilterCategoryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFilterCategoryBinding binding = ItemFilterCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FilterCategoryItemViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FilterCategoryItemViewHolder holder, int position) {
        CategoryResponse item = items.get(position);
        holder.binding.tvTitle.setText(item.getName());

        if (item.isSelect()) {
            holder.binding.content.setSelected(true);
            holder.binding.tvTitle.setTextColor(
                    ContextCompat.getColor(context, R.color.filter_select));
        } else {
            holder.binding.content.setSelected(false);
            holder.binding.tvTitle.setTextColor(
                    ContextCompat.getColor(context, R.color.filter_un_select));
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            item.setSelect(!item.isSelect());
            notifyItemChanged(position);
            if (listener != null) {
                listener.onCategoryFilterClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FilterCategoryItemViewHolder extends RecyclerView.ViewHolder {
        final ItemFilterCategoryBinding binding;

        public FilterCategoryItemViewHolder(ItemFilterCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}