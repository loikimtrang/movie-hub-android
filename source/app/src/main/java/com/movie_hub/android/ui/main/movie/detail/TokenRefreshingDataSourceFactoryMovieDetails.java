package com.movie_hub.android.ui.main.movie.detail;

import androidx.media3.datasource.DataSource;

public class TokenRefreshingDataSourceFactoryMovieDetails implements DataSource.Factory {
    private final MovieDetailViewModel viewModel;

    public TokenRefreshingDataSourceFactoryMovieDetails(MovieDetailViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public DataSource createDataSource() {
        return new TokenRefreshingDataSourceMovieDetail("exoplayer", viewModel);
    }
}
