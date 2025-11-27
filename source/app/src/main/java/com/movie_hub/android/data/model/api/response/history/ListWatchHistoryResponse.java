package com.movie_hub.android.data.model.api.response.history;

import lombok.Data;
import java.util.List;

@Data
public class ListWatchHistoryResponse {
    private boolean isCompletedMovie;
    private List<WatchHistoryResponse> watchHistories;

    public WatchHistoryResponse getWatchHistoryByMovieId(Long movieItemId) {
        if (watchHistories == null || movieItemId == null) {
            return null;
        }

        for (WatchHistoryResponse item : watchHistories) {
            if (movieItemId.equals(item.getMovieItemId())) {
                return item;
            }
        }

        return null;
    }

    public WatchHistoryResponse getWatchHistoryNoComplete() {
        if (watchHistories == null) {
            return null;
        }

        for (WatchHistoryResponse item : watchHistories) {
            if (!item.isCompleted()) {
                return item;
            }
        }

        return null;
    }

    public WatchHistoryResponse getFirstWatchHistory() {
        if (watchHistories == null) {
            return null;
        }

        return watchHistories.get(0);
    }
}

