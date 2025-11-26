package com.movie_hub.android.data.model.api.response.history;

import lombok.Data;
import java.util.List;

@Data
public class ListWatchHistoryResponse {
    private boolean isCompletedMovie;
    private List<WatchHistoryResponse> watchHistories;
}

