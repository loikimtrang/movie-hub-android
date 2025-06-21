package com.movie_hub.android.ui.base.activity;

import androidx.annotation.ColorRes;

public interface SystemBarColorProvider {
    @ColorRes
    int getStatusBarColor();
    @ColorRes int getNavigationBarColor();
}
