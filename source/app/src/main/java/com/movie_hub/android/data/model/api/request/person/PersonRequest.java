package com.movie_hub.android.data.model.api.request.person;

import lombok.Data;

@Data
public class PersonRequest {
    private String country;
    private Integer gender;
    private Long id;
    private Integer kind;
    private Long movieId;
    private String name;
    private Long offset;
    private String otherName;
    private Integer page;
    private Integer size;
    private Boolean paged;
    private Boolean sortSorted;
    private Boolean sortUnsorted;
    private Integer status;
    private Boolean unpaged;
}

