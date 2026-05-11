package com.movie_hub.android.data.model.api.response.subtitle;

import com.movie_hub.android.BuildConfig;

import lombok.Data;

@Data
public class SubtitleResponse {
    private Long id;
    private String language;
    private String fileUrl;
    private Boolean isDefault;

    public String getSubtitleUrl() {
        if (fileUrl != null && fileUrl.startsWith("http")) {
            return fileUrl;
        }
        return BuildConfig.MEDIA_URL_VTT + fileUrl;
    }
}
