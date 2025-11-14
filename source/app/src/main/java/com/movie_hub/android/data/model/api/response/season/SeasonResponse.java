package com.movie_hub.android.data.model.api.response.season;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.video.VideoResponse;

import java.util.List;

import lombok.Data;

@Data
public class SeasonResponse {
    private Long id;
    private String title;
    private String description;
    private String label;
    private Integer kind;
    private Integer ordering;
    private Integer status;
    private String releaseDate;
    private String createdDate;
    private String modifiedDate;
    private VideoResponse video;
    private MovieItemResponse trailer;
    private List<MovieItemResponse> episodes;
    private boolean isSelect = false;
}

