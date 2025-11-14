package com.movie_hub.android.data.model.api.response.movie;

import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;

import java.util.List;

import lombok.Data;

@Data
public class MovieResponse {
    private Long id;
    private String title;
    private String originalTitle;
    private String description;
    private String posterUrl;
    private String thumbnailUrl;
    private String slug;
    private Integer ageRating;
    private Integer status;
    private Integer type;
    private Long viewCount;
    private Boolean isFeatured;
    private String country;
    private String language;
    private String releaseDate;
    private String createdDate;
    private String modifiedDate;
    private List<CategoryResponse> categories;
    private List<SeasonResponse> seasons;
}
