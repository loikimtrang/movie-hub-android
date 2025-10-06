package com.movie_hub.android.ui.main.search.topTrending;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FlexSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int spacing;

    public FlexSpacingItemDecoration(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect,
                               @NonNull View view,
                               @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) return;

        // spacing giữa các item, không padding ở mép ngoài
        outRect.left = spacing / 2;
        outRect.right = spacing / 2;
        outRect.top = spacing / 2;
        outRect.bottom = spacing / 2;
    }
}
