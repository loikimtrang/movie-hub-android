package com.movie_hub.android.data.model.api.request.favourite;

import lombok.Data;

@Data
public class CreateFavouriteRequest {

    public static final int FAVOURITE_TYPE_MOVIE = 1;
    public static final int FAVOURITE_TYPE_PERSON = 2;

    private long targetId;
    private int type;
}
