package com.movie_hub.android.ui.main.search.topTrending.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.ItemMovieBinding;
import com.movie_hub.android.utils.GridUtil;

import java.util.ArrayList;
import java.util.List;

public class MovieVerticalAdapter extends RecyclerView.Adapter<MovieVerticalAdapter.MovieViewHolder> {

    private final List<MovieResponse> movieList = new ArrayList<>();
    private OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(MovieResponse movie);
    }

    public MovieVerticalAdapter(OnMovieClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMovieBinding binding = ItemMovieBinding.inflate(inflater, parent, false);
        return new MovieViewHolder(binding);
    }
    private int lastPosition = -1;

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MovieResponse movie = movieList.get(position);

        holder.binding.title.setText(movie.getTitle());
        holder.binding.subtitle.setText(movie.getOriginalTitle());

        Glide.with(holder.binding.getRoot().getContext())
                .load(Constants.MEDIA_URL + movie.getThumbnailUrl())
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(holder.binding.image);

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) listener.onMovieClick(movie);
        });
        if (position > lastPosition) {
            holder.itemView.setAlpha(0f);
            holder.itemView.postDelayed(() -> {
                Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.item_slide_in_bottom);
                holder.itemView.startAnimation(animation);
                holder.itemView.setAlpha(1f);
            }, position * 150L);
            lastPosition = position;
        }
    }
    @Override
    public void onViewDetachedFromWindow(@NonNull MovieViewHolder holder) {
        holder.itemView.clearAnimation();
    }
    public void setData(List<MovieResponse> newData) {
        movieList.clear();
        if (newData != null) {
            movieList.addAll(newData);
        }
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return movieList.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        private final ItemMovieBinding binding;

        public MovieViewHolder(@NonNull ItemMovieBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

