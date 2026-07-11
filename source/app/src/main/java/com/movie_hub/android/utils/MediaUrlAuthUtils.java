package com.movie_hub.android.utils;

import android.text.TextUtils;

/** Rules for attaching Bearer token when ExoPlayer opens media URLs. */
public final class MediaUrlAuthUtils {

    private MediaUrlAuthUtils() {
    }

    /**
     * {@code /public-download/} assets (subtitle VTT, sprite VTT, etc.) are public and
     * return HTTP 400 when an Authorization header is attached.
     */
    public static boolean requiresAuthorization(String url) {
        if (TextUtils.isEmpty(url)) {
            return true;
        }
        return !url.contains("/public-download/");
    }
}
