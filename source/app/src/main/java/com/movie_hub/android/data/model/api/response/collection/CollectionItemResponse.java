package com.movie_hub.android.data.model.api.response.collection;

import com.movie_hub.android.data.model.api.response.movie.MovieResponse;

import lombok.Data;

@Data
public class CollectionItemResponse {
    private Long id;
    private Long collectionId;
    private Integer ordering;
    private Integer status;
    private String createdDate;
    private String modifiedDate;

    private MovieResponse movie;
}
