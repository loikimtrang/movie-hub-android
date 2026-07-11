package com.movie_hub.android.ui.main.live;

import android.app.Activity;

import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.room.RoomResponse;
import com.movie_hub.android.ui.main.MainCallback;

public interface RoomClickHost {

    Activity getHostActivity();

    boolean isUserLoggedIn();

    long getUserId();

    void showHostLoading();

    void hideHostLoading();

    void showHostError(String message);

    void showLoginRequiredDialog();

    void fetchMovie(Long id, MainCallback<MovieResponse> callback);

    void fetchListMovieTracking(Long movieId, MainCallback<ListWatchHistoryResponse> callback);

    void fetchJoinRoom(Long roomId, MainCallback<RoomResponse> callback);

    void fetchStartRoom(Long roomId, MainCallback<RoomResponse> callback);

    void navigateToMovieDetail(MovieResponse movie, ListWatchHistoryResponse tracking);

    void createMqttAndWatch(MovieResponse movie, RoomResponse room, boolean isHost);
}
