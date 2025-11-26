package com.movie_hub.android.data.model.api.request.favourite;

import lombok.Data;

@Data
public class FavouriteListRequest {
    private Long id;
    private Long movieId;
    private Long personId;
    private Long userId;
    private Integer type;
    private Integer status;
    private Integer page;
    private Integer size;
    private Long offset;
    private Boolean paged;
    private Boolean unpaged;
    private Boolean sortSorted;
    private Boolean sortUnsorted;
}