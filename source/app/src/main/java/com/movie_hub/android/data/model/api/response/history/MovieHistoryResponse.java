package com.movie_hub.android.data.model.api.response.history;

import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;

import lombok.Data;

@Data
public class MovieHistoryResponse {
    private Long id;
    private Long userId;
    private Long movieId;
    private Long movieItemId;
    private boolean isCompleted;
    private Long lastWatchSeconds;
    private int status;
    private int timesWatched;
    private String createdDate;
    private String modifiedDate;
    private MovieResponse movie;
    private MovieItemResponse movieItem;
}
