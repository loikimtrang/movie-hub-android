package com.movie_hub.android.ui.main.movie.watch.adapter;

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
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.databinding.ItemEpisodeHoriBinding;

import java.util.ArrayList;
import java.util.List;

public class EpisodesWatchMovieAdapter extends RecyclerView.Adapter<EpisodesWatchMovieAdapter.EpisodesWatchMovieViewHolder> {

    private final List<MovieItemResponse> movieList = new ArrayList<>();
    private onMovieItemClickListener listener;

    public interface onMovieItemClickListener {
        void onMovieItemClick(MovieItemResponse movie);
    }

    public EpisodesWatchMovieAdapter(onMovieItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EpisodesWatchMovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemEpisodeHoriBinding binding = ItemEpisodeHoriBinding.inflate(inflater, parent, false);
        return new EpisodesWatchMovieViewHolder(binding);
    }
    private int lastPosition = -1;

    @Override
    public void onBindViewHolder(@NonNull EpisodesWatchMovieViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MovieItemResponse movie = movieList.get(position);

        holder.binding.title.setText(movie.getVideo().getName());
        holder.binding.description.setText(movie.getVideo().getDescription());

        Glide.with(holder.binding.getRoot().getContext())
                .load(Constants.MEDIA_URL + movie.getVideo().getThumbnailUrl())
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(holder.binding.image);

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) listener.onMovieItemClick(movie);
        });
    }
    @Override
    public void onViewDetachedFromWindow(@NonNull EpisodesWatchMovieViewHolder holder) {
        holder.itemView.clearAnimation();
    }
    public void setData(List<MovieItemResponse> newData) {
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
    static class EpisodesWatchMovieViewHolder extends RecyclerView.ViewHolder {
        private final ItemEpisodeHoriBinding binding;
        public EpisodesWatchMovieViewHolder(@NonNull ItemEpisodeHoriBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

