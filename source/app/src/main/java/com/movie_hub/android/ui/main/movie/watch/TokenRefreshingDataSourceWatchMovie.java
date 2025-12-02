package com.movie_hub.android.ui.main.movie.watch;

import androidx.annotation.NonNull;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.DefaultHttpDataSource;

import com.movie_hub.android.ui.main.movie.detail.MovieDetailViewModel;

public class TokenRefreshingDataSourceWatchMovie extends DefaultHttpDataSource {

    private final WatchMovieViewModel viewModel;

    public TokenRefreshingDataSourceWatchMovie(String userAgent, WatchMovieViewModel viewModel) {
        super(userAgent);
        this.viewModel = viewModel;
    }

    @Override
    public long open(@NonNull DataSpec dataSpec) throws HttpDataSourceException {
        setRequestProperty("Authorization", viewModel.getTokenVideo());
        return super.open(dataSpec);
    }
}

