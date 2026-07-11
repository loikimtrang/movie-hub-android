package com.movie_hub.android.ui.main.account.playlist.shimmer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;

public class PlaylistShimmerAdapter extends RecyclerView.Adapter<PlaylistShimmerAdapter.ViewHolder> {
    private final int shimmerItemCount;
    private Context context;

    public PlaylistShimmerAdapter(int count, Context context) {
        this.context = context;
        this.shimmerItemCount = count;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_play_list_shimmer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        float startAlpha = Constants.SHIMMER_START_ALPHA;
        float endAlpha = Constants.SHIMMER_END_ALPHA;
        float alpha = startAlpha - (endAlpha * position);

        holder.itemView.setAlpha(alpha);

        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();

        int marginStart = 0;
        int marginEnd = 0;

        if (position == 0) {
            marginStart = (int) context.getResources().getDimension(R.dimen._12sdp);
            layoutParams.setMarginStart(marginStart);
        }
        if (position == shimmerItemCount - 1) {
            marginEnd = (int) context.getResources().getDimension(R.dimen._12sdp);
            layoutParams.setMarginEnd(marginEnd);
        }
        holder.itemView.setLayoutParams(layoutParams);

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
