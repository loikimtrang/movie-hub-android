package com.movie_hub.android.utils;

import android.content.Context;
import android.content.res.Resources;

public class GridUtil {
    public static int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    public static int calculateSpanCount(Context context, int itemMinDpWidth) {
        int screenWidthPx = Resources.getSystem().getDisplayMetrics().widthPixels;
        float density = context.getResources().getDisplayMetrics().density;
        int itemWidthPx = (int) (itemMinDpWidth * density);
        return Math.max(1, screenWidthPx / itemWidthPx);
    }
}
