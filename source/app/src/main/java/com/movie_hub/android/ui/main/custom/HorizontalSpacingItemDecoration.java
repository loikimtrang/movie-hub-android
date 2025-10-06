package com.movie_hub.android.ui.main.custom;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HorizontalSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int spacing;

    public HorizontalSpacingItemDecoration(int spacingPx) {
        this.spacing = spacingPx;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int itemCount = parent.getAdapter() != null ? parent.getAdapter().getItemCount() : 0;

        // Không set left cho item đầu, không set right cho item cuối
        if (position > 0) {
            outRect.left = spacing;
        }

        if (position < itemCount - 1) {
            outRect.right = 0;
        }
    }
}

