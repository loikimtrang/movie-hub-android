package com.movie_hub.android.data.model.api.request.movie;

import lombok.Data;

@Data
public class MovieRequest {
    private Integer ageRating;
    private Long id;
    private Long offset;
    private String originalTitle;
    private Integer page;
    private Integer pageSize;
    private Boolean paged;
    private Boolean sortSorted;
    private Boolean sortUnsorted;
    private Integer status;
    private String title;
    private Integer type;
    private Boolean unpaged;
}

