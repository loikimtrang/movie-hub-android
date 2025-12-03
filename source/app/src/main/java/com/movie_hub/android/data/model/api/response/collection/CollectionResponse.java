package com.movie_hub.android.data.model.api.response.collection;

import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.style.StyleResponse;

import lombok.Data;
import java.util.List;

@Data
public class CollectionResponse {
    private Long id;
    private String name;
    private String color;
    private String filter;
    private Integer ordering;
    private Integer status;
    private Integer type;
    private Integer styleType;
    private Boolean randomData;
    private String createdDate;
    private String modifiedDate;

    private StyleResponse style;
    private List<MovieResponse> movies;
    private List<CollectionItemResponse> collectionItems;
}

