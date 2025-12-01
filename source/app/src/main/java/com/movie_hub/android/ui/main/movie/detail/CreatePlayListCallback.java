package com.movie_hub.android.ui.main.movie.detail;

import com.movie_hub.android.data.model.api.response.playlist.PlayListResponse;

public interface CreatePlayListCallback {
    void onCreateSuccessCallBack(PlayListResponse response);
}
