package com.movie_hub.android.ui.main.movie.detail;

import androidx.annotation.NonNull;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.DefaultHttpDataSource;

public class TokenRefreshingDataSourceMovieDetail extends DefaultHttpDataSource {

    private final MovieDetailViewModel viewModel;

    public TokenRefreshingDataSourceMovieDetail(String userAgent, MovieDetailViewModel viewModel) {
        super(userAgent);
        this.viewModel = viewModel;
    }

    @Override
    public long open(@NonNull DataSpec dataSpec) throws HttpDataSourceException {
        setRequestProperty("Authorization", viewModel.getTokenVideo());
        return super.open(dataSpec);
    }
}

