package com.movie_hub.android.data.model.api.request.playlist;

import lombok.Data;

@Data
public class ActionUpdateItemPlaylistRequest {
    private Long playlistId;
    private int action;
}
