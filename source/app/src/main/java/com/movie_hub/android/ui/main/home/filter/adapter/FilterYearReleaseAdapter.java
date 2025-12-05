package com.movie_hub.android.ui.main.home.filter.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.request.movie.filter.YearReleaseRequest;
import com.movie_hub.android.databinding.ItemFilterYearReleaseBinding;

import java.util.ArrayList;
import java.util.List;

public class FilterYearReleaseAdapter extends RecyclerView.Adapter<FilterYearReleaseAdapter.FilterYearReleaseViewHolder> {

    private final List<YearReleaseRequest> items = new ArrayList<>();
    private OnFilterClick listener;
    private Context context;
    public FilterYearReleaseAdapter(OnFilterClick listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }
    public void setData(List<YearReleaseRequest> newData) {
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
    public FilterYearReleaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFilterYearReleaseBinding binding = ItemFilterYearReleaseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FilterYearReleaseViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FilterYearReleaseViewHolder holder, int position) {
        YearReleaseRequest item = items.get(position);
        holder.binding.tvTitle.setText(item.getReleaseYear().toString());

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

            for (YearReleaseRequest a : items) {
                a.setSelect(false);
            }

            item.setSelect(!wasSelected);

            notifyDataSetChanged();

            if (listener != null) {
                listener.onYearFilterClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FilterYearReleaseViewHolder extends RecyclerView.ViewHolder {
        final ItemFilterYearReleaseBinding binding;

        public FilterYearReleaseViewHolder(ItemFilterYearReleaseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}