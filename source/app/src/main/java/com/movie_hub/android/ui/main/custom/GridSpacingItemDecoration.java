package com.movie_hub.android.ui.main.custom;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int spanCount;
    private final int spacing;

    public GridSpacingItemDecoration(int spanCount, int spacing) {
        this.spanCount = spanCount;
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // vị trí item
        int column = position % spanCount; // cột

        outRect.left = column * spacing / spanCount;
        outRect.right = spacing - (column + 1) * spacing / spanCount;

        if (position >= spanCount) {
            outRect.top = spacing; // spacing giữa các hàng
        }

        // 👉 THÊM SPACING CHO HÀNG CUỐI
        int itemCount = parent.getAdapter() != null ? parent.getAdapter().getItemCount() : 0;
        int rowCount = (int) Math.ceil((double) itemCount / spanCount);
        int currentRow = (position / spanCount) + 1;

        if (currentRow == rowCount) {
            outRect.bottom = spacing; // hàng cuối
        }
    }
}
