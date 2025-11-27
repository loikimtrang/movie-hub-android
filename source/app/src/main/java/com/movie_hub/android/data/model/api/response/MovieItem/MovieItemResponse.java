package com.movie_hub.android.data.model.api.response.MovieItem;


import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;
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
    private SeasonResponse parent;
    private boolean isCompleted;
    private Long lastWatchSeconds;

    public MovieItemResponse(MovieItemResponse source) {
        this.id = source.id;
        this.title = source.title;
        this.label = source.label;
        this.description = source.description;
        this.isCompleted = source.isCompleted;
        this.lastWatchSeconds = source.lastWatchSeconds;
        this.thumbnailUrl = source.thumbnailUrl;
        this.isPlaying = source.isPlaying;
        this.video = source.video;
    }

    public MovieItemResponse() {
    }

    public String getThumbnailUrl() {
        return (thumbnailUrl == null || thumbnailUrl.trim().isEmpty())
                ? null
                : Constants.MEDIA_URL + thumbnailUrl.trim();
    }
}

