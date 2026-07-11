package com.movie_hub.android.ui.main.home.filter.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.request.movie.filter.TypeMovieRequest;
import com.movie_hub.android.databinding.ItemFilterTypeBinding;

import java.util.ArrayList;
import java.util.List;

public class FilterTypeItemAdapter extends RecyclerView.Adapter<FilterTypeItemAdapter.FilterTypeItemViewHolder> {

    private final List<TypeMovieRequest> items = new ArrayList<>();
    private OnFilterClick listener;
    private Context context;
    public FilterTypeItemAdapter(OnFilterClick listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }
    public void setData(List<TypeMovieRequest> newData) {
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
    public FilterTypeItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFilterTypeBinding binding = ItemFilterTypeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FilterTypeItemViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FilterTypeItemViewHolder holder, int position) {
        TypeMovieRequest item = items.get(position);
        holder.binding.tvTitle.setText(item.getLabel());

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
            boolean wasSelected = item.isSelect();

            for (TypeMovieRequest a : items) {
                a.setSelect(false);
            }

            item.setSelect(!wasSelected);

            notifyDataSetChanged();

            if (listener != null) {
                listener.onTypeFilterClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FilterTypeItemViewHolder extends RecyclerView.ViewHolder {
        final ItemFilterTypeBinding binding;

        public FilterTypeItemViewHolder(ItemFilterTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}