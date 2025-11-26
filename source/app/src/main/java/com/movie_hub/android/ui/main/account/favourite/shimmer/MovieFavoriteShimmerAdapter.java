package com.movie_hub.android.ui.main.account.favourite.shimmer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;

public class MovieFavoriteShimmerAdapter extends RecyclerView.Adapter<MovieFavoriteShimmerAdapter.ViewHolder> {
    private final int shimmerItemCount;

    public MovieFavoriteShimmerAdapter(int count) {
        this.shimmerItemCount = count;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_favourite_shimmer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        float startAlpha = 0.8f;
        float endAlpha = 0.25f;
        float alpha = startAlpha - (endAlpha * position);

        holder.itemView.setAlpha(alpha);
    }

    @Override
    public int getItemCount() {
        return shimmerItemCount;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
