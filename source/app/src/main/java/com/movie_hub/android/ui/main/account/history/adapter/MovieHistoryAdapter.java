package com.movie_hub.android.ui.main.account.history.adapter;

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
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.history.MovieHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.ItemMovieFavouriteBinding;
import com.movie_hub.android.databinding.ItemMovieHistoryBinding;
import com.movie_hub.android.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

public class MovieHistoryAdapter extends RecyclerView.Adapter<MovieHistoryAdapter.MovieHistoryViewHolder> {

    private final List<MovieHistoryResponse> items = new ArrayList<>();
    private OnMovieClickListener listener;
    private Context context;
    public interface OnMovieClickListener {
        void onMovieClick(MovieHistoryResponse movieHistoryResponse);
    }

    public MovieHistoryAdapter(OnMovieClickListener listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public MovieHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMovieHistoryBinding binding = ItemMovieHistoryBinding.inflate(inflater, parent, false);
        return new MovieHistoryViewHolder(binding);
    }
    private int lastPosition = -1;

    @SuppressLint({"SetTextI18n", "ResourceAsColor", "ClickableViewAccessibility"})
    @Override
    public void onBindViewHolder(@NonNull MovieHistoryViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MovieHistoryResponse movieHistoryResponse = items.get(position);
        MovieResponse movieResponse = items.get(position).getMovie();
        holder.binding.seekBar.setOnTouchListener((v, event) -> true);

        holder.binding.tvTitle.setText(movieResponse.getTitle());
        holder.binding.tvSubTitle.setText(movieResponse.getOriginalTitle());

        if (movieHistoryResponse.getMovieItem().getKind() == Constants.TYPE_MOVIE_SINGLE) {
            holder.binding.tvSeasonEpisode.setVisibility(View.GONE);
            holder.binding.icSeasonDotEpisode.setVisibility(View.GONE);
        } else if (movieHistoryResponse.getMovieItem().getKind() == Constants.TYPE_MOVIE_SERIES) {
            if (isNumericLabel(movieHistoryResponse.getMovieItem().getParent().getLabel())) {
                holder.binding.tvSeasonEpisode.setText(context.getString(R.string.season_char) + movieHistoryResponse.getMovieItem().getParent().getLabel()
                + ":" + context.getString(R.string.episode_char) + movieHistoryResponse.getMovieItem().getLabel());
            } else {
                holder.binding.tvSeasonEpisode.setText(context.getString(R.string.episode_char) + movieHistoryResponse.getMovieItem().getLabel());
            }
        }

        Long currentTime = movieHistoryResponse.getLastWatchSeconds();
        Long totalTime = movieHistoryResponse.getMovieItem().getVideo().getDuration();

        holder.binding.seekBar.setMax(totalTime.intValue());

        holder.binding.seekBar.setProgress(currentTime.intValue());

        holder.binding.tvCurrentTime.setText(DisplayUtils.formatSecondsToHHMMSS(currentTime));
        holder.binding.tvTotalTime.setText(DisplayUtils.formatSecondsToHHMMSS(totalTime));

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) listener.onMovieClick(movieHistoryResponse);
        });


        Glide.with(holder.binding.getRoot().getContext())
                .load(movieResponse.getPosterUrl())
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(holder.binding.image);

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
    public boolean isNumericLabel(String label) {
        if (label == null || label.isEmpty()) return false;
        return label.matches("\\d+");
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);

            notifyItemRangeChanged(position, items.size());
        }
    }


    @Override
    public void onViewDetachedFromWindow(@NonNull MovieHistoryViewHolder holder) {
        holder.itemView.clearAnimation();
    }

    @SuppressLint("NewApi")
    public void setData(List<MovieHistoryResponse> newData) {
        items.clear();

        if (newData != null) {
            items.addAll(newData);
        }

        notifyDataSetChanged();
    }

    public void addData(List<MovieHistoryResponse> moreItems) {
        int startPos = items.size();
        items.addAll(moreItems);
        notifyItemRangeInserted(startPos, moreItems.size());
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MovieHistoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemMovieHistoryBinding binding;

        public MovieHistoryViewHolder(@NonNull ItemMovieHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}