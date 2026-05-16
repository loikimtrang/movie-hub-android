package com.movie_hub.android.ui.main.account.setting;

import android.widget.RelativeLayout;
import android.widget.TextView;

import com.movie_hub.android.databinding.LayoutIncludeSettingSubtitleBinding;
import com.movie_hub.android.ui.main.movie.watch.setting.SubtitleStyle;

/**
 * Shared subtitle style UI (preview, font size, colors) for settings screen and watch dialog.
 */
public final class SubtitleSettingBinder {

    public interface Listener {
        void onStyleChanged(SubtitleStyle style);
    }

    private final LayoutIncludeSettingSubtitleBinding binding;
    private SubtitleStyle style;
    private Listener listener;

    public SubtitleSettingBinder(LayoutIncludeSettingSubtitleBinding binding) {
        this.binding = binding;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void bind(SubtitleStyle source) {
        style = copyStyle(source);
        refreshUi();
        setupListeners();
    }

    public SubtitleStyle getStyle() {
        return style;
    }

    private void setupListeners() {
        binding.btnSizeSmall.setOnClickListener(v -> selectFontSize(SubtitleStyle.FONT_SIZE_SMALL));
        binding.btnSizeMedium.setOnClickListener(v -> selectFontSize(SubtitleStyle.FONT_SIZE_MEDIUM));
        binding.btnSizeLarge.setOnClickListener(v -> selectFontSize(SubtitleStyle.FONT_SIZE_LARGE));

        binding.swatchBgYellow.setOnClickListener(v -> selectBgColor(SubtitleStyle.BG_COLOR_YELLOW));
        binding.swatchBgWhite.setOnClickListener(v -> selectBgColor(SubtitleStyle.BG_COLOR_WHITE));
        binding.swatchBgBlack.setOnClickListener(v -> selectBgColor(SubtitleStyle.BG_COLOR_BLACK));
        binding.selectNoneBg.setOnClickListener(v -> selectBgColor(SubtitleStyle.BG_COLOR_NONE));

        binding.swatchTextYellow.setOnClickListener(v -> selectTextColor(SubtitleStyle.TEXT_COLOR_YELLOW));
        binding.swatchTextWhite.setOnClickListener(v -> selectTextColor(SubtitleStyle.TEXT_COLOR_WHITE));
        binding.swatchTextBlack.setOnClickListener(v -> selectTextColor(SubtitleStyle.TEXT_COLOR_BLACK));
    }

    private void selectFontSize(int fontSize) {
        style.setFontSize(fontSize);
        commit();
    }

    private void selectBgColor(int bgColor) {
        style.setBackgroundColor(bgColor);
        commit();
    }

    private void selectTextColor(int textColor) {
        style.setTextColor(textColor);
        commit();
    }

    private void commit() {
        refreshUi();
        if (listener != null) listener.onStyleChanged(style);
    }

    private void refreshUi() {
        updateFontSizeSelection();
        updateBgColorSelection();
        updateTextColorSelection();
        style.applyTo(binding.tvPreviewSubtitle);
    }

    private void updateFontSizeSelection() {
        setSizeSelected(binding.btnSizeSmall, style.getFontSize() == SubtitleStyle.FONT_SIZE_SMALL);
        setSizeSelected(binding.btnSizeMedium, style.getFontSize() == SubtitleStyle.FONT_SIZE_MEDIUM);
        setSizeSelected(binding.btnSizeLarge, style.getFontSize() == SubtitleStyle.FONT_SIZE_LARGE);
    }

    private void setSizeSelected(RelativeLayout view, boolean selected) {
        view.setSelected(selected);
    }

    private void updateBgColorSelection() {
        binding.swatchBgYellow.setSelected(false);
        binding.swatchBgWhite.setSelected(false);
        binding.swatchBgBlack.setSelected(false);
        binding.selectNoneBg.setSelected(false);
        switch (style.getBackgroundColor()) {
            case SubtitleStyle.BG_COLOR_YELLOW:
                binding.swatchBgYellow.setSelected(true);
                break;
            case SubtitleStyle.BG_COLOR_WHITE:
                binding.swatchBgWhite.setSelected(true);
                break;
            case SubtitleStyle.BG_COLOR_NONE:
                binding.selectNoneBg.setSelected(true);
                break;
            case SubtitleStyle.BG_COLOR_BLACK:
            default:
                binding.swatchBgBlack.setSelected(true);
                break;
        }
    }

    private void updateTextColorSelection() {
        binding.swatchTextYellow.setSelected(false);
        binding.swatchTextWhite.setSelected(false);
        binding.swatchTextBlack.setSelected(false);
        switch (style.getTextColor()) {
            case SubtitleStyle.TEXT_COLOR_WHITE:
                binding.swatchTextWhite.setSelected(true);
                break;
            case SubtitleStyle.TEXT_COLOR_BLACK:
                binding.swatchTextBlack.setSelected(true);
                break;
            case SubtitleStyle.TEXT_COLOR_YELLOW:
            default:
                binding.swatchTextYellow.setSelected(true);
                break;
        }
    }

    private static SubtitleStyle copyStyle(SubtitleStyle source) {
        if (source == null) return new SubtitleStyle();
        SubtitleStyle copy = new SubtitleStyle();
        copy.setFontSize(source.getFontSize());
        copy.setBackgroundColor(source.getBackgroundColor());
        copy.setTextColor(source.getTextColor());
        return copy;
    }
}
