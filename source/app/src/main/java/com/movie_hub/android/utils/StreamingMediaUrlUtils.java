package com.movie_hub.android.utils;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.movie_hub.android.constant.Constants;

/** Build streaming asset URLs (VTT, sprite, subtitle files) using video hostname when available. */
public final class StreamingMediaUrlUtils {

    private StreamingMediaUrlUtils() {
    }

    public static String buildPublicDownloadUrl(@Nullable String hostname, @Nullable String relativePath) {
        if (TextUtils.isEmpty(relativePath)) {
            return "";
        }
        String path = relativePath.trim();
        if (path.startsWith("http")) {
            return path;
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        String pathPrefix = resolvePublicDownloadPrefix();
        if (!TextUtils.isEmpty(hostname)) {
            return trimTrailingSlash(hostname.trim()) + pathPrefix + path;
        }

        String configured = Constants.MEDIA_URL_VTT;
        if (!TextUtils.isEmpty(configured) && configured.startsWith("http")) {
            return trimTrailingSlash(configured) + path;
        }
        return configured + path;
    }

    private static String resolvePublicDownloadPrefix() {
        String configured = Constants.MEDIA_URL_VTT;
        if (TextUtils.isEmpty(configured)) {
            return "/v1/file/public-download";
        }
        int v1Index = configured.indexOf("/v1/");
        if (v1Index >= 0) {
            return configured.substring(v1Index);
        }
        if (configured.startsWith("/")) {
            return configured;
        }
        return "/" + configured;
    }

    private static String trimTrailingSlash(String value) {
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
