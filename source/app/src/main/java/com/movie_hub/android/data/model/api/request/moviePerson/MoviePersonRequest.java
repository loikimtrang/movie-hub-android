package com.movie_hub.android.data.model.api.request.moviePerson;

import lombok.Data;

@Data
public class MoviePersonRequest {
    private Long id;                // integer($int64)
    private Integer kind;           // integer($int32)
    private String movieId;         // string
    private Long offset;            // integer($int64)
    private Integer page;     // integer($int32)
    private Integer size;       // integer($int32)
    private Boolean paged;          // boolean
    private String personId;        // string
    private Boolean sortSorted;     // boolean
    private Boolean sortUnsorted;   // boolean
    private Boolean unpaged;        // boolean
}

