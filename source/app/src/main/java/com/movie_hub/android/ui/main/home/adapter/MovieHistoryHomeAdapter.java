package com.movie_hub.android.ui.main.home.adapter;

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
import com.movie_hub.android.databinding.ItemMovieHistoryHomeBinding;
import com.movie_hub.android.databinding.ItemMovieHistoryHomeBinding;
import com.movie_hub.android.ui.main.home.OnMovieClickCallback;
import com.movie_hub.android.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

public class MovieHistoryHomeAdapter extends RecyclerView.Adapter<MovieHistoryHomeAdapter.MovieHistoryHomeViewHolder> {

    private final List<MovieHistoryResponse> items = new ArrayList<>();
    private OnMovieClickCallback listener;
    private Context context;

    public MovieHistoryHomeAdapter(OnMovieClickCallback listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public MovieHistoryHomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMovieHistoryHomeBinding binding = ItemMovieHistoryHomeBinding.inflate(inflater, parent, false);
        return new MovieHistoryHomeViewHolder(binding);
    }
    private int lastPosition = -1;

    @SuppressLint({"SetTextI18n", "ResourceAsColor", "ClickableViewAccessibility"})
    @Override
    public void onBindViewHolder(@NonNull MovieHistoryHomeViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MovieHistoryResponse movieHistoryResponse = items.get(position);
        MovieResponse movieResponse = items.get(position).getMovie();
        holder.binding.seekBar.setOnTouchListener((v, event) -> true);

        holder.binding.tvTitle.setText(movieResponse.getTitle());

        if (movieHistoryResponse.getMovieItem().getKind() == Constants.TYPE_MOVIE_SINGLE) {
            holder.binding.tvSeasonEpisode.setVisibility(View.GONE);
            holder.binding.icSeasonDotEpisode.setVisibility(View.GONE);
        } else if (movieHistoryResponse.getMovieItem().getKind() == Constants.TYPE_MOVIE_SERIES) {
            holder.binding.tvSeasonEpisode.setVisibility(View.VISIBLE);
            holder.binding.icSeasonDotEpisode.setVisibility(View.VISIBLE);

            if (isNumericLabel(movieHistoryResponse.getMovieItem().getParent().getLabel())) {
                holder.binding.tvSeasonEpisode.setText(
                        context.getString(R.string.season_char) +
                                movieHistoryResponse.getMovieItem().getParent().getLabel() +
                                ":" +
                                context.getString(R.string.episode_char) +
                                movieHistoryResponse.getMovieItem().getLabel()
                );
            } else {
                holder.binding.tvSeasonEpisode.setText(
                        context.getString(R.string.episode_char) +
                                movieHistoryResponse.getMovieItem().getLabel()
                );
            }
        }


        Long currentTime = movieHistoryResponse.getLastWatchSeconds();
        Long totalTime = movieHistoryResponse.getMovieItem().getVideo().getDuration();

        holder.binding.seekBar.setMax(totalTime.intValue());

        holder.binding.seekBar.setProgress(currentTime.intValue());

        holder.binding.tvCurrentTime.setText(DisplayUtils.formatSecondsToHHMMSS(currentTime));
        holder.binding.tvTotalTime.setText(DisplayUtils.formatSecondsToHHMMSS(totalTime));

        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) listener.onMovieClick(movieHistoryResponse.getMovie());
        });


        Glide.with(holder.binding.getRoot().getContext())
                .load(movieResponse.getPosterUrl())
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(holder.binding.image);

        if (position > lastPosition) {
            holder.itemView.setAlpha(0f);
            holder.itemView.postDelayed(() -> {
                Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.item_slide_in_right);
                holder.itemView.startAnimation(animation);
                holder.itemView.setAlpha(1f);
            }, 50L);
            lastPosition = position;
        }

        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) holder.binding.getRoot().getLayoutParams();

        int margin = (int) context.getResources().getDimension(R.dimen._6sdp);
        layoutParams.setMarginStart(margin);
        layoutParams.setMarginEnd(margin);
        if (position == 0) {
            layoutParams.setMarginStart(margin * 2);
        }
        if (position == items.size() - 1) {
            layoutParams.setMarginEnd(margin * 2);
        }

        holder.binding.getRoot().setLayoutParams(layoutParams);
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

    static class MovieHistoryHomeViewHolder extends RecyclerView.ViewHolder {
        private final ItemMovieHistoryHomeBinding binding;

        public MovieHistoryHomeViewHolder(@NonNull ItemMovieHistoryHomeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}