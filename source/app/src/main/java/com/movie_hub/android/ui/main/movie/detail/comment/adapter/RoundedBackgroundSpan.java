package com.movie_hub.android.ui.main.movie.detail.comment.adapter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

public class RoundedBackgroundSpan extends ReplacementSpan {

    private final int backgroundColor;
    private final int textColor;
    private final float radius;
    private final float padding;

    public RoundedBackgroundSpan(int bgColor, int textColor, float radius, float padding) {
        this.backgroundColor = bgColor;
        this.textColor = textColor;
        this.radius = radius;
        this.padding = padding;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(text, start, end) + padding * 2);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {

        float width = paint.measureText(text, start, end);
        float height = paint.getFontMetrics().descent - paint.getFontMetrics().ascent;

        RectF rect = new RectF(
                x,
                y + paint.getFontMetrics().ascent - padding,
                x + width + padding * 2,
                y + paint.getFontMetrics().descent + padding
        );

        Paint bgPaint = new Paint(paint);
        bgPaint.setColor(backgroundColor);
        bgPaint.setAntiAlias(true);

        canvas.drawRoundRect(rect, radius, radius, bgPaint);

        paint.setColor(textColor);
        canvas.drawText(text, start, end, x + padding, y, paint);
    }
}

