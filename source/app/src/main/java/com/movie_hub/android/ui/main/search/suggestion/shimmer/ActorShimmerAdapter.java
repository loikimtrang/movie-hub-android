package com.movie_hub.android.ui.main.search.suggestion.shimmer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;

public class ActorShimmerAdapter extends RecyclerView.Adapter<ActorShimmerAdapter.ViewHolder> {
    private final int shimmerItemCount;

    public ActorShimmerAdapter(int count, Context context) {
        this.shimmerItemCount = count;
        this.context = context;
    }
    Context context;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_actor_shimmer, parent, false);
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
        int margin = (int) context.getResources().getDimension(R.dimen._6sdp);
        layoutParams.setMarginStart(margin);
        layoutParams.setMarginEnd(margin);
        if (position == 0) {
            layoutParams.setMarginStart(margin * 2);
        }
        if (position == shimmerItemCount - 1) {
            layoutParams.setMarginEnd(margin * 2);
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
