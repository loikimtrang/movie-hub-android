package com.movie_hub.android.ui.main.home.topic.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.DimenRes;
import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private final int spanCount;
    private final int spacingPx;
    private final boolean includeEdge;

    public GridSpacingItemDecoration(Context context, int spanCount, @DimenRes int spacingResId, boolean includeEdge) {
        this.spanCount = spanCount;
        this.includeEdge = includeEdge;
        this.spacingPx = context.getResources().getDimensionPixelSize(spacingResId);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int column = position % spanCount;

        if (includeEdge) {
            outRect.left = spacingPx - column * spacingPx / spanCount;
            outRect.right = (column + 1) * spacingPx / spanCount;

            outRect.top = spacingPx;

            int totalItemCount = parent.getAdapter().getItemCount();
            int rowCount = (int) Math.ceil((double) totalItemCount / spanCount);
            int currentRow = position / spanCount + 1;

            if (currentRow == rowCount) {
                outRect.bottom = spacingPx;
            }
        } else {
            outRect.left = column * spacingPx / spanCount;
            outRect.right = spacingPx - (column + 1) * spacingPx / spanCount;
            if (position >= spanCount) {
                outRect.top = spacingPx;
            }
        }
    }
}

