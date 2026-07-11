package com.movie_hub.android.data.model.api.response.subtitle;

import androidx.annotation.Nullable;

import com.movie_hub.android.utils.StreamingMediaUrlUtils;

import lombok.Data;

@Data
public class SubtitleResponse {
    private Long id;
    private String language;
    private String fileUrl;
    private Boolean isDefault;
    private String label;

    public String getSubtitleUrl() {
        return getSubtitleUrl(null);
    }

    public String getSubtitleUrl(@Nullable String hostname) {
        return StreamingMediaUrlUtils.buildPublicDownloadUrl(hostname, fileUrl);
    }
}
