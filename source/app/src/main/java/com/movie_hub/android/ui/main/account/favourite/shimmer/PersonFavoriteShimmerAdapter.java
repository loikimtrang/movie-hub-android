package com.movie_hub.android.ui.main.account.favourite.shimmer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;

public class PersonFavoriteShimmerAdapter extends RecyclerView.Adapter<PersonFavoriteShimmerAdapter.ViewHolder> {
    private final int shimmerItemCount;
    private Boolean isFavourite = false;

    public PersonFavoriteShimmerAdapter(int count, Boolean isFavourite) {
        this.isFavourite = isFavourite;
        this.shimmerItemCount = count;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_person_shimmer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        float startAlpha = Constants.SHIMMER_START_ALPHA;
        float endAlpha = Constants.SHIMMER_END_ALPHA;
        float alpha = startAlpha - (endAlpha * position);

        holder.itemView.setAlpha(alpha);
        FrameLayout btnUnFavourite = holder.itemView.findViewById(R.id.btn_un_favourite);
        if (btnUnFavourite != null) {
            btnUnFavourite.setVisibility(isFavourite ? View.VISIBLE : View.GONE);
        }
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
