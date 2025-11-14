package com.movie_hub.android.ui.main.movie.watch.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;
import com.movie_hub.android.databinding.ItemSeasonBinding;
import com.movie_hub.android.databinding.ItemTagCategoryMovieDetailBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SeasonItemAdapter extends RecyclerView.Adapter<SeasonItemAdapter.SeasonItemViewHolder> {

    private final List<SeasonResponse> items = new ArrayList<>();
    private OnSeasonClickListener listener;
    private Context context;
    public interface OnSeasonClickListener {
        void onSeasonClick(SeasonResponse season);
    }
    public SeasonItemAdapter(OnSeasonClickListener listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }
    public void setData(List<SeasonResponse> newData) {
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
    public SeasonItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSeasonBinding binding = ItemSeasonBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SeasonItemViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SeasonItemViewHolder holder, int position) {
        SeasonResponse item = items.get(position);
        holder.binding.tvSeason.setText(context.getString(R.string.season) + " " + (position + 1));

        if (item.isSelect()) {
            holder.binding.content.setSelected(true);
            holder.binding.tvSeason.setTextColor(
                    ContextCompat.getColor(context, R.color.black));
        } else {
            holder.binding.content.setSelected(false);
            holder.binding.tvSeason.setTextColor(
                    ContextCompat.getColor(context, R.color.text));
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) {
                if (!item.isSelect()) {
                    for (SeasonResponse s: items) {
                        if (Objects.equals(s.getId(), item.getId())) {
                            s.setSelect(true);
                            item.setSelect(true);
                        } else {
                            s.setSelect(false);
                        }
                    }
                    notifyDataSetChanged();
                    listener.onSeasonClick(item);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SeasonItemViewHolder extends RecyclerView.ViewHolder {
        final ItemSeasonBinding binding;

        public SeasonItemViewHolder(ItemSeasonBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}