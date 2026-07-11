package com.movie_hub.android.utils;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;

import com.movie_hub.android.data.model.api.response.ToxicSpan;

import java.util.Collections;
import java.util.List;

public final class ToxicTextUtils {

    private static final float BLUR_RADIUS = 16f;
    public static final int STATUS_TOXIC_LOCKED = -1;

    private ToxicTextUtils() {
    }

    public static List<ToxicSpan> parseToxicSpans(String toxicSpansJson) {
        if (toxicSpansJson == null || toxicSpansJson.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<ToxicSpan> spans = GsonUtils.fromJsonToList(toxicSpansJson, ToxicSpan.class);
        return spans != null ? spans : Collections.emptyList();
    }

    public static boolean shouldShowRevealButton(String toxicSpansJson, Integer status) {
        return !parseToxicSpans(toxicSpansJson).isEmpty()
                || (status != null && status == STATUS_TOXIC_LOCKED);
    }

    public static boolean bindToxicContent(TextView textView, CharSequence displayText, String toxicSpansJson,
                                           Integer status, boolean revealed, int spanOffset) {
        boolean needDisplayButton = shouldShowRevealButton(toxicSpansJson, status);
        CharSequence safeText = displayText == null ? "" : displayText;
        List<ToxicSpan> spans = parseToxicSpans(toxicSpansJson);
        boolean hasToxicSpans = !spans.isEmpty();
        boolean isToxicLocked = status != null && status == STATUS_TOXIC_LOCKED;

        if (revealed || !needDisplayButton) {
            clearBlur(textView);
            textView.setText(safeText);
            return needDisplayButton;
        }

        if (isToxicLocked && !hasToxicSpans) {
            clearSpanBlur(textView);
            textView.setText(safeText);
            applyFullBlur(textView);
            return true;
        }

        if (hasToxicSpans) {
            applyPartialBlur(textView, safeText, spans, spanOffset);
            return true;
        }

        clearBlur(textView);
        textView.setText(safeText);
        return false;
    }

    public static void updateRevealIcon(boolean revealed, android.widget.ImageView iconView, int hiddenRes, int visibleRes) {
        iconView.setImageResource(revealed ? visibleRes : hiddenRes);
    }

    private static void applyPartialBlur(TextView textView, CharSequence displayText, List<ToxicSpan> spans, int spanOffset) {
        SpannableStringBuilder builder = displayText instanceof Spannable
                ? new SpannableStringBuilder((Spannable) displayText)
                : new SpannableStringBuilder(displayText);

        for (ToxicSpan span : spans) {
            if (span == null) continue;
            int start = span.getStart() + spanOffset;
            int end = span.getEnd() + spanOffset;
            if (start < 0 || end > builder.length() || start >= end) continue;
            builder.setSpan(new BlurTextSpan(BLUR_RADIUS), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        clearFullBlur(textView);
        textView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }

    private static void applyFullBlur(TextView textView) {
        textView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        textView.getPaint().setMaskFilter(new android.graphics.BlurMaskFilter(BLUR_RADIUS, android.graphics.BlurMaskFilter.Blur.NORMAL));
        textView.invalidate();
    }

    private static void clearFullBlur(TextView textView) {
        textView.getPaint().setMaskFilter(null);
    }

    private static void clearSpanBlur(TextView textView) {
        textView.setLayerType(View.LAYER_TYPE_NONE, null);
    }

    public static void clearBlur(TextView textView) {
        clearFullBlur(textView);
        clearSpanBlur(textView);
        textView.invalidate();
    }
}
