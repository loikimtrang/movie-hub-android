package com.movie_hub.android.ui.main.search.suggestion.adapter;

import android.annotation.SuppressLint;
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
import com.movie_hub.android.databinding.ItemMovieSuggestionBinding;

import java.util.ArrayList;
import java.util.List;
public class MovieSuggestAdapter extends RecyclerView.Adapter<MovieSuggestAdapter.MovieSuggestViewHolder> {

    private final List<MovieResponse> movieList = new ArrayList<>();
    private OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(MovieResponse movie);
    }

    public MovieSuggestAdapter(OnMovieClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieSuggestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMovieSuggestionBinding binding = ItemMovieSuggestionBinding.inflate(inflater, parent, false);
        return new MovieSuggestViewHolder(binding);
    }
    private int lastPosition = -1;

    @Override
    public void onBindViewHolder(@NonNull MovieSuggestViewHolder holder, @SuppressLint("RecyclerView") int position) {
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
                Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.item_slide_in_right);
                holder.itemView.startAnimation(animation);
                holder.itemView.setAlpha(1f);
            }, 50L);
            lastPosition = position;
        }
    }
    @Override
    public void onViewDetachedFromWindow(@NonNull MovieSuggestViewHolder holder) {
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

    static class MovieSuggestViewHolder extends RecyclerView.ViewHolder {
        private final ItemMovieSuggestionBinding binding;

        public MovieSuggestViewHolder(@NonNull ItemMovieSuggestionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}