package com.movie_hub.android.utils;

import android.graphics.BlurMaskFilter;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

public class BlurTextSpan extends CharacterStyle implements UpdateAppearance {

    private final float blurRadius;

    public BlurTextSpan(float blurRadius) {
        this.blurRadius = blurRadius;
    }

    @Override
    public void updateDrawState(TextPaint textPaint) {
        textPaint.setMaskFilter(new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL));
    }
}
