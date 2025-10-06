package com.movie_hub.android.ui.main.movie.detail.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.databinding.ItemTagCategoryMovieDetailBinding;
import com.movie_hub.android.databinding.ItemTagCategoryMovieDetailBinding;

import java.util.ArrayList;
import java.util.List;

public class TagCategoryAdapter extends RecyclerView.Adapter<TagCategoryAdapter.TagCategoryViewHolder> {

    private final List<CategoryResponse> items = new ArrayList<>();

    public TagCategoryAdapter() {

    }

    public void setData(List<CategoryResponse> newData) {
        items.clear();
        if (newData != null) {
            items.addAll(newData);
        }
        notifyDataSetChanged();
    }
    public void addData(List<CategoryResponse> moreData) {
        int start = items.size();
        items.addAll(moreData);
        notifyItemRangeInserted(start, moreData.size());
    }

    public void addItem(CategoryResponse item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }
    public void addItemToTop(CategoryResponse item) {
        items.add(0, item);
        notifyItemInserted(0);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TagCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTagCategoryMovieDetailBinding binding = ItemTagCategoryMovieDetailBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TagCategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TagCategoryViewHolder holder, int position) {
        CategoryResponse item = items.get(position);

        holder.binding.textHistory.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TagCategoryViewHolder extends RecyclerView.ViewHolder {
        final ItemTagCategoryMovieDetailBinding binding;

        public TagCategoryViewHolder(ItemTagCategoryMovieDetailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}