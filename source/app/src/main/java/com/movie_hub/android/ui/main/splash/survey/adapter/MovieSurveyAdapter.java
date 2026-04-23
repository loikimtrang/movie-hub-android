package com.movie_hub.android.ui.main.splash.survey.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.ItemMovieSurveyBinding;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class MovieSurveyAdapter extends RecyclerView.Adapter<MovieSurveyAdapter.MovieViewHolder> {

    @Getter
    private final List<MovieResponse> movieList = new ArrayList<>();
    private OnMovieClickListener listener;
    private int lastPosition = -1;

    public interface OnMovieClickListener {
        void onMovieClick(MovieResponse movie);
    }

    public MovieSurveyAdapter(OnMovieClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMovieSurveyBinding binding = ItemMovieSurveyBinding.inflate(inflater, parent, false);
        return new MovieViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MovieResponse movie = movieList.get(position);

        holder.binding.title.setText(movie.getTitle());
        Glide.with(holder.binding.getRoot().getContext())
                .load(movie.getPosterUrl())
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(holder.binding.image);

        holder.binding.layoutSelect.setSelected(movie.isSelect());

        if (movie.isSelect()) {
            holder.binding.icSelect.setVisibility(ViewGroup.VISIBLE);
        } else {
            holder.binding.icSelect.setVisibility(ViewGroup.GONE);
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            movie.setSelect(!movie.isSelect());

            notifyItemChanged(position);

            if (listener != null) {
                listener.onMovieClick(movie);
            }
        });

        setAnimation(holder, position);
    }

    private void setAnimation(MovieViewHolder holder, int position) {
        if (position > lastPosition) {
            holder.itemView.setAlpha(0f);
            holder.itemView.postDelayed(() -> {
                Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.item_slide_in_bottom);
                holder.itemView.startAnimation(animation);
                holder.itemView.setAlpha(1f);
            }, 50L);
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MovieViewHolder holder) {
        holder.itemView.clearAnimation();
        super.onViewDetachedFromWindow(holder);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<MovieResponse> newData) {
        movieList.clear();
        if (newData != null) {
            movieList.addAll(newData);
        }
        lastPosition = -1; // Reset animation khi load data mới
        notifyDataSetChanged();
    }

    public void addData(List<MovieResponse> moreItems) {
        if (moreItems == null) return;
        int startPos = movieList.size();
        movieList.addAll(moreItems);
        notifyItemRangeInserted(startPos, moreItems.size());
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        private final ItemMovieSurveyBinding binding;

        public MovieViewHolder(@NonNull ItemMovieSurveyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}