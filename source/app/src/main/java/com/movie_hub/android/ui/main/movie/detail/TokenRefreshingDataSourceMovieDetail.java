package com.movie_hub.android.ui.main.movie.detail;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.DefaultHttpDataSource;

import com.movie_hub.android.utils.MediaUrlAuthUtils;

public class TokenRefreshingDataSourceMovieDetail extends DefaultHttpDataSource {

    private final MovieDetailViewModel viewModel;

    public TokenRefreshingDataSourceMovieDetail(String userAgent, MovieDetailViewModel viewModel) {
        super(userAgent);
        this.viewModel = viewModel;
    }

    @Override
    public long open(@NonNull DataSpec dataSpec) throws HttpDataSourceException {
        String url = dataSpec.uri != null ? dataSpec.uri.toString() : "";
        if (MediaUrlAuthUtils.requiresAuthorization(url)) {
            String token = viewModel.getTokenVideo();
            if (!TextUtils.isEmpty(token)) {
                setRequestProperty("Authorization", token);
            }
        }
        return super.open(dataSpec);
    }
}

