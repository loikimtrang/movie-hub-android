package com.movie_hub.android.ui.main.account.history.shimmer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;

public class MovieHistoryShimmerAdapter extends RecyclerView.Adapter<MovieHistoryShimmerAdapter.ViewHolder> {
    private final int shimmerItemCount;

    public MovieHistoryShimmerAdapter(int count) {
        this.shimmerItemCount = count;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_history_shimmer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        float startAlpha = Constants.SHIMMER_START_ALPHA;
        float endAlpha = Constants.SHIMMER_END_ALPHA;
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
