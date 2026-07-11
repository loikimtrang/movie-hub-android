package com.movie_hub.android.data.model.api.response.history;

import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;

import lombok.Data;

@Data
public class WatchHistoryResponse {
    private String createdDate;
    private Long id;
    private boolean isCompleted;
    private Long lastWatchSeconds;
    private String modifiedDate;
    private MovieResponse movie;
    private Long movieId;
    private MovieItemResponse movieItem;
    private Long movieItemId;
    private Integer status;
    private Integer timesWatched;
    private Long userId;
}

