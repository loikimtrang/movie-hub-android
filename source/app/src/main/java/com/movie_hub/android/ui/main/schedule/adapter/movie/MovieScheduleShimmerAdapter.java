package com.movie_hub.android.ui.main.schedule.adapter.movie;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;

public class MovieScheduleShimmerAdapter extends RecyclerView.Adapter<MovieScheduleShimmerAdapter.ViewHolder> {
    private final int shimmerItemCount;

    public MovieScheduleShimmerAdapter(int count) {
        this.shimmerItemCount = count;
    }

    @NonNull
    @Override
    public MovieScheduleShimmerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_schedule_shimmer, parent, false);
        return new MovieScheduleShimmerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieScheduleShimmerAdapter.ViewHolder holder, int position) {
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
