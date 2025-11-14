package com.movie_hub.android.data.model.api.response.video;


import android.os.Parcel;

import androidx.annotation.NonNull;

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
}
