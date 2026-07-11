package com.movie_hub.android.ui.main.movie.watch;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.DefaultHttpDataSource;

import com.movie_hub.android.utils.MediaUrlAuthUtils;

public class TokenRefreshingDataSourceWatchMovie extends DefaultHttpDataSource {

    private final WatchMovieViewModel viewModel;

    public TokenRefreshingDataSourceWatchMovie(String userAgent, WatchMovieViewModel viewModel) {
        super(userAgent);
        this.viewModel = viewModel;
    }

    @Override
    public long open(@NonNull DataSpec dataSpec) throws HttpDataSourceException {
        String url = dataSpec.uri != null ? dataSpec.uri.toString() : "";
        Log.d("WATCH_MOVIE", "Opening URL: " + url);
        if (MediaUrlAuthUtils.requiresAuthorization(url)) {
            String token = viewModel.getTokenVideo();
            Log.d("WATCH_MOVIE", "Token: " + token);
            if (!TextUtils.isEmpty(token)) {
                setRequestProperty("Authorization", token);
            }
        }
        return super.open(dataSpec);
    }
}

