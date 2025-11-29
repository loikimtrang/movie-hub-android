package com.movie_hub.android.data.model.api.request.playlist;

import lombok.Data;

@Data
public class GetListMoviePlayListRequest {
    private Long id;
    private Long offset;
    private Integer page;
    private Integer size;
    private Boolean paged;
    private Boolean sortSorted;
    private Boolean sortUnsorted;
    private Boolean unpaged;
}

