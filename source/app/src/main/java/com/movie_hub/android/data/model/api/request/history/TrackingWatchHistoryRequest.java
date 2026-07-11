package com.movie_hub.android.data.model.api.request.history;

import lombok.Data;

@Data
public class TrackingWatchHistoryRequest {
    private long lastWatchSeconds;
    private long movieItemId;
}
