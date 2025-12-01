package com.movie_hub.android.data.model.api.request.playlist;

import java.util.List;

import lombok.Data;

@Data
public class UpdatePlayListItemRequest {
    private Long movieId;
    private List<ActionUpdateItemPlaylistRequest> actions;
}
