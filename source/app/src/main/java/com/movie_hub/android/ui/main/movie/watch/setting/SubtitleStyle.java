package com.movie_hub.android.ui.main.movie.watch.setting;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.widget.TextView;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.request.setting.UserSettingsRequest;

import lombok.Data;

@Data
public class SubtitleStyle {

    public static final int FONT_SIZE_SMALL = 0;
    public static final int FONT_SIZE_MEDIUM = 1;
    public static final int FONT_SIZE_LARGE = 2;

    public static final int TEXT_COLOR_YELLOW = 0;
    public static final int TEXT_COLOR_WHITE = 1;
    public static final int TEXT_COLOR_BLACK = 2;

    public static final int BG_COLOR_YELLOW = 0;
    public static final int BG_COLOR_WHITE = 1;
    public static final int BG_COLOR_BLACK = 2;
    public static final int BG_COLOR_NONE = 3;

    private int fontSize = FONT_SIZE_MEDIUM;
    private int backgroundColor = BG_COLOR_BLACK;
    private int textColor = TEXT_COLOR_YELLOW;

    public int resolveTextColor(Context context) {
        switch (textColor) {
            case TEXT_COLOR_WHITE:
                return context.getColor(R.color.white);
            case TEXT_COLOR_BLACK:
                return context.getColor(R.color.black);
            case TEXT_COLOR_YELLOW:
            default:
                return context.getColor(R.color.subtitle_text_yellow);
        }
    }

    public int resolveBackgroundColor(Context context) {
        switch (backgroundColor) {
            case BG_COLOR_YELLOW:
                return context.getColor(R.color.subtitle_text_yellow);
            case BG_COLOR_WHITE:
                return context.getColor(R.color.white);
            case BG_COLOR_BLACK:
                return context.getColor(R.color.black);
            case BG_COLOR_NONE:
            default:
                return android.graphics.Color.TRANSPARENT;
        }
    }

    public boolean hasBackground() {
        return backgroundColor != BG_COLOR_NONE;
    }

    public float getTextSizeSp() {
        switch (fontSize) {
            case FONT_SIZE_SMALL:
                return 12f;
            case FONT_SIZE_LARGE:
                return 24f;
            case FONT_SIZE_MEDIUM:
            default:
                return 18f;
        }
    }

    public static SubtitleStyle fromUserSettings(UserSettingsRequest settings) {
        SubtitleStyle style = new SubtitleStyle();
        if (settings == null) return style;
        if (settings.getSubtitleFontSize() != null) {
            style.setFontSize(settings.getSubtitleFontSize());
        }
        if (settings.getSubtitleBackgroundColor() != null) {
            style.setBackgroundColor(settings.getSubtitleBackgroundColor());
        }
        if (settings.getSubtitleTextColor() != null) {
            style.setTextColor(settings.getSubtitleTextColor());
        }
        return style;
    }

    public void applyToUserSettings(UserSettingsRequest settings) {
        if (settings == null) return;
        settings.setSubtitleFontSize(fontSize);
        settings.setSubtitleBackgroundColor(backgroundColor);
        settings.setSubtitleTextColor(textColor);
    }

    public void applyTo(TextView textView) {
        Context context = textView.getContext();
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, getTextSizeSp());
        textView.setTextColor(resolveTextColor(context));

        float density = context.getResources().getDisplayMetrics().density;
        if (hasBackground()) {
            int paddingH = Math.round(8 * density);
            int paddingV = Math.round(4 * density);
            textView.setPadding(paddingH, paddingV, paddingH, paddingV);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(resolveBackgroundColor(context));
            bg.setCornerRadius(4 * density);
            textView.setBackground(bg);
        } else {
            textView.setPadding(0, 0, 0, 0);
            textView.setBackground(null);
        }
    }
}
