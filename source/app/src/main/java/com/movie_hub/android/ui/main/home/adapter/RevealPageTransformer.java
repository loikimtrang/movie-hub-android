package com.movie_hub.android.ui.main.home.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.movie_hub.android.R;

public class RevealPageTransformer implements ViewPager2.PageTransformer {

    @Override
    public void transformPage(@NonNull View page, float position) {
        View image = page.findViewById(R.id.image);
        if (image == null) return;

        float pageWidth = page.getWidth();
        image.setTranslationX(-position * pageWidth * 0.3f); // hiệu ứng trượt nhẹ
    }
}


