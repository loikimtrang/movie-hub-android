package com.movie_hub.android.ui.main.search.topTrending.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.room.SearchHistoryEntity;
import com.movie_hub.android.databinding.ItemHistorySearchBinding;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.SearchHistoryViewHolder> {

    private final List<SearchHistoryEntity> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SearchHistoryEntity item, int position);
    }

    public SearchHistoryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<SearchHistoryEntity> newData) {
        items.clear();
        if (newData != null) {
            items.addAll(newData);
        }
        notifyDataSetChanged();
    }
    public void addData(List<SearchHistoryEntity> moreData) {
        int start = items.size();
        items.addAll(moreData);
        notifyItemRangeInserted(start, moreData.size());
    }

    public void addItem(SearchHistoryEntity item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }
    public void addItemToTop(SearchHistoryEntity item) {
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
    public SearchHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistorySearchBinding binding = ItemHistorySearchBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SearchHistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHistoryViewHolder holder, int position) {
        SearchHistoryEntity item = items.get(position);

        holder.binding.textHistory.setText(item.keyword);

        if (position == getItemCount() - 1) {
            holder.binding.bgItem.setBackgroundResource(R.drawable.bg_item_delete_history);
        } else {
            holder.binding.bgItem.setBackgroundResource(R.drawable.bg_item_search_history);
        }

        holder.binding.getRoot().setOnClickListener(v -> listener.onItemClick(item, position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SearchHistoryViewHolder extends RecyclerView.ViewHolder {
        final ItemHistorySearchBinding binding;

        public SearchHistoryViewHolder(ItemHistorySearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
