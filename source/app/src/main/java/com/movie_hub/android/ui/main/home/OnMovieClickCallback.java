package com.movie_hub.android.ui.main.home;

import com.movie_hub.android.data.model.api.response.movie.MovieResponse;

public interface OnMovieClickCallback {
    void onMovieClick(MovieResponse movieResponse);
    void onWatchMovieClick(MovieResponse movieResponse);
    void onMovieLongClick(MovieResponse movieResponse);
}
