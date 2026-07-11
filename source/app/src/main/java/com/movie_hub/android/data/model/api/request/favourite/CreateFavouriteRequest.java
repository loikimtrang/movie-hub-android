package com.movie_hub.android.data.model.api.request.favourite;

import lombok.Data;

@Data
public class CreateFavouriteRequest {
    private Long targetId;
    private Integer type;
}

