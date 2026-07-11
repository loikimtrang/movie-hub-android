package com.movie_hub.android.ui.main.schedule.adapter.movie;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.ItemMovieScheduleBinding;
import com.movie_hub.android.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

public class MovieScheduleAdapter extends RecyclerView.Adapter<MovieScheduleAdapter.MovieScheduleViewHolder> {

    private final List<MovieItemResponse> items = new ArrayList<>();
    private final OnMovieClickListener listener;
    private final Context context;
    private int lastPosition = -1;

    public interface OnMovieClickListener {
        void onMovieClick(MovieItemResponse movie);
    }

    public MovieScheduleAdapter(Context context, OnMovieClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMovieScheduleBinding binding = ItemMovieScheduleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MovieScheduleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieScheduleViewHolder holder, int position) {
        MovieResponse item = items.get(position).getMovie();
        MovieItemResponse movieItemResponse = items.get(position);

        // Bind data
        holder.binding.tvSubTitle.setText(item.getTitle());
        holder.binding.tvTitle.setText(movieItemResponse.getTitle());

        if (item.getAgeRating() != null) {
            holder.binding.ageRating.setText(DisplayUtils.displayAgeRating(item.getAgeRating()));
            holder.binding.bgAge.setVisibility(View.VISIBLE);

        } else {
            holder.binding.bgAge.setVisibility(View.GONE);
        }

        if (item.getReleaseDate() != null) {
            holder.binding.dateRelease.setText(DisplayUtils.getYearFromReleaseDate(item.getReleaseDate()));
        }

        // Load Poster
        Glide.with(context)
                .load(item.getPosterUrl())
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(holder.binding.image);

        // Click Listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMovieClick(movieItemResponse);
            }
        });

        // Animation giống MovieFavouriteAdapter
        setAnimation(holder.itemView, position);
    }

    private void setAnimation(android.view.View viewToAnimate, int position) {
        if (position > lastPosition) {
            viewToAnimate.setAlpha(0f);
            viewToAnimate.postDelayed(() -> {
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_slide_in_bottom);
                viewToAnimate.startAnimation(animation);
                viewToAnimate.setAlpha(1f);
            }, 50L);
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MovieScheduleViewHolder holder) {
        holder.itemView.clearAnimation();
        super.onViewDetachedFromWindow(holder);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<MovieItemResponse> newData) {
        items.clear();
        lastPosition = -1;
        if (newData != null) {
            items.addAll(newData);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MovieScheduleViewHolder extends RecyclerView.ViewHolder {
        private final ItemMovieScheduleBinding binding;

        public MovieScheduleViewHolder(@NonNull ItemMovieScheduleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
