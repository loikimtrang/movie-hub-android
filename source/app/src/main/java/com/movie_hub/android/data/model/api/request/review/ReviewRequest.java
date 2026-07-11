package com.movie_hub.android.data.model.api.request.review;
import lombok.Data;

@Data
public class ReviewRequest {
    private Long id;
    private Long authorId;
    private Long movieId;
    private Integer rate;
    private Integer status;

    private Integer page;
    private Integer size;
    private Long offset;

    private Boolean paged;
    private Boolean unpaged;

    private Boolean sortSorted;
    private Boolean sortUnsorted;
}

