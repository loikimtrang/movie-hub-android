package com.movie_hub.android.data.model.api.response.MovieItem;


import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.video.VideoResponse;

import lombok.Data;

@Data
public class MovieItemResponse {
    private Long id;
    private String title;
    private String description;
    private String releaseDate;
    private Integer status;
    private Integer ordering;
    private String label;
    private Integer kind;
    private String createdDate;
    private String modifiedDate;
    private MovieResponse movie;
    private VideoResponse video;
    private boolean isPlaying = false;
    private String thumbnailUrl;
    public String getThumbnailUrl() {
        return (thumbnailUrl == null || thumbnailUrl.trim().isEmpty())
                ? null
                : Constants.MEDIA_URL + thumbnailUrl.trim();
    }
}

