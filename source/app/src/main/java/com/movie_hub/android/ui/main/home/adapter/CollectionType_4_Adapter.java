package com.movie_hub.android.ui.main.home.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.ItemCollectionType3Binding;
import com.movie_hub.android.databinding.ItemCollectionType4Binding;
import com.movie_hub.android.ui.main.home.OnMovieClickCallback;

import java.util.ArrayList;
import java.util.List;

public class CollectionType_4_Adapter extends RecyclerView.Adapter<CollectionType_4_Adapter.CollectionType_4_ViewHolder> {

    private final List<MovieResponse> items = new ArrayList<>();
    private OnMovieClickCallback listener;
    private Context context;

    public CollectionType_4_Adapter(OnMovieClickCallback listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public CollectionType_4_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCollectionType4Binding binding = ItemCollectionType4Binding.inflate(inflater, parent, false);
        return new CollectionType_4_ViewHolder(binding);
    }
    private int lastPosition = -1;

    @SuppressLint({"SetTextI18n", "ResourceAsColor", "ClickableViewAccessibility"})
    @Override
    public void onBindViewHolder(@NonNull CollectionType_4_ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MovieResponse item = items.get(position);

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener == null) return;
            listener.onMovieClick(item);
        });

        holder.binding.getRoot().setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onMovieLongClick(item);
            }
            return true;
        });
        
        Glide.with(holder.binding.getRoot().getContext())
                .load(item.getThumbnailUrl())
                .placeholder(R.drawable.place_holder_16_9)
                .error(R.drawable.place_holder_16_9)
                .into(holder.binding.image);

        if (position > lastPosition) {
            holder.itemView.setAlpha(0f);
            holder.itemView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
            lastPosition = position;
        }
    }

    @SuppressLint("NewApi")
    public void setData(List<MovieResponse> newData) {
        items.clear();

        if (newData != null) {
            items.addAll(newData);
        }

        notifyDataSetChanged();
    }

    public void addData(List<MovieResponse> moreItems) {
        int startPos = items.size();
        items.addAll(moreItems);
        notifyItemRangeInserted(startPos, moreItems.size());
    }

    public MovieResponse getItemAt(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CollectionType_4_ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCollectionType4Binding binding;

        public CollectionType_4_ViewHolder(@NonNull ItemCollectionType4Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}