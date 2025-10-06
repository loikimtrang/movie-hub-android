package com.movie_hub.android.utils;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

public class HtmlUtils {

    /**
     * Chuyển đổi nội dung HTML có thẻ <p> sang <strong> và trả về Spanned.
     *
     * @param rawHtmlContent Chuỗi HTML gốc
     * @return Spanned đã xử lý có thể gán cho TextView
     */
    public static Spanned convertPtoStrong(String rawHtmlContent) {
        if (rawHtmlContent == null) return null;

        // Thay thế <p> thành <strong> và </p> thành </strong>
        String strongHtml = rawHtmlContent
                .replace("<p", "<strong")
                .replace("</p>", "</strong>");

        // Convert HTML string sang Spanned
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(strongHtml, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(strongHtml);
        }
    }
}

