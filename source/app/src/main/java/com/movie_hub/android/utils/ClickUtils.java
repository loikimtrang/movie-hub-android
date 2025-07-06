package com.movie_hub.android.utils;

import android.view.View;

import androidx.annotation.NonNull;

public class ClickUtils {

    public static void debounceClick(@NonNull View view, long delayMillis) {
        view.setClickable(false);
        view.postDelayed(() -> view.setClickable(true), delayMillis);
    }

    public static void debounceClick(@NonNull View view) {
        debounceClick(view, 1000);
    }
}
