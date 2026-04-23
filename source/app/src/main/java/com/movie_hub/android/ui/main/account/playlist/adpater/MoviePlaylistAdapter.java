package com.movie_hub.android.ui.main.account.playlist.adpater;

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
import com.movie_hub.android.databinding.ItemMoviePlaylistBinding;
import com.movie_hub.android.databinding.ItemMoviePlaylistBinding;
import com.movie_hub.android.utils.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

public class MoviePlaylistAdapter extends RecyclerView.Adapter<MoviePlaylistAdapter.MoviePlaylistViewHolder> {

    private final List<MovieResponse> items = new ArrayList<>();
    private OnMovieClickListener listener;
    private Context context;
    public interface OnMovieClickListener {
        void onMovieClick(MovieResponse movie);
        void onDeleteClick(MovieResponse movie);
    }

    public MoviePlaylistAdapter(OnMovieClickListener listener, Context context) {
        super();
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public MoviePlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMoviePlaylistBinding binding = ItemMoviePlaylistBinding.inflate(inflater, parent, false);
        return new MoviePlaylistViewHolder(binding);
    }
    private int lastPosition = -1;

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull MoviePlaylistViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MovieResponse item = items.get(position);

        holder.binding.tvTitle.setText(item.getTitle());
        holder.binding.tvSubTitle.setText(item.getOriginalTitle());
        holder.binding.ageRating.setText(DisplayUtils.displayAgeRating(item.getAgeRating()));
        holder.binding.dateRelease.setText(DisplayUtils.getYearFromReleaseDate(item.getReleaseDate()));
        holder.binding.getRoot().setOnClickListener(v -> {
            if (listener != null) {
                listener.onMovieClick(item);
            }
        });

        holder.binding.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(items.get(position));
                removeItem(position);
            }
        });


        Glide.with(holder.binding.getRoot().getContext())
                .load(item.getPosterUrl())
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

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);

            notifyItemRangeChanged(position, items.size());
        }
    }


    @Override
    public void onViewDetachedFromWindow(@NonNull MoviePlaylistViewHolder holder) {
        holder.itemView.clearAnimation();
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


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MoviePlaylistViewHolder extends RecyclerView.ViewHolder {
        private final ItemMoviePlaylistBinding binding;

        public MoviePlaylistViewHolder(@NonNull ItemMoviePlaylistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}