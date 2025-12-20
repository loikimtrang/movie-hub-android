package com.movie_hub.android.data.model.api.response.video;


import android.os.Parcel;

import androidx.annotation.NonNull;

import com.movie_hub.android.constant.Constants;

import lombok.Data;

@Data
public class VideoResponse {
    private Long id;
    private String name;
    private String description;
    private String shortDescription;
    private String content;
    private String relativeContentPath;
    private String thumbnailUrl;
    private String spriteUrl;
    private String vttUrl;
    private Long duration;
    private Long introStart;
    private Long introEnd;
    private Long outroStart;
    private Integer state;
    private Integer status;
    private String createdDate;
    private String modifiedDate;
    private boolean isPlaying;

    public String getSpriteUrl() {
        if (spriteUrl == null || spriteUrl.isEmpty()) return "";
        if (spriteUrl.contains("http")) return spriteUrl;
        return Constants.MEDIA_URL_VTT + spriteUrl;
    }

    public String getThumbnailUrl() {
        if (thumbnailUrl == null || thumbnailUrl.isEmpty()) return "";
        if (thumbnailUrl.contains("http")) return thumbnailUrl;
        return Constants.MEDIA_URL + thumbnailUrl;
    }

    public String getVttUrl() {
        if (vttUrl == null || vttUrl.isEmpty()) return "";
        if (vttUrl.contains("http")) return vttUrl;
        return Constants.MEDIA_URL_VTT + vttUrl;
    }
}
