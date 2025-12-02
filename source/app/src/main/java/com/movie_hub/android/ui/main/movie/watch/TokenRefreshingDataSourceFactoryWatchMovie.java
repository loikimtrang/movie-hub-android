package com.movie_hub.android.ui.main.movie.watch;

import androidx.media3.datasource.DataSource;

public class TokenRefreshingDataSourceFactoryWatchMovie implements DataSource.Factory {
    private final WatchMovieViewModel viewModel;

    public TokenRefreshingDataSourceFactoryWatchMovie(WatchMovieViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public DataSource createDataSource() {
        return new TokenRefreshingDataSourceWatchMovie("exoplayer", viewModel);
    }
}
