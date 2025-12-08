package com.movie_hub.android.data.model.api.request.movie;

import com.movie_hub.android.data.model.api.request.movie.filter.AgeRatingRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.CountryRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.LanguageRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.TypeMovieRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.YearReleaseRequest;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class MovieRequest {
    private String country;
    private Integer type;
    private List<Long> categoryIds;
    private Integer ageRating; // truyền constan
    private Integer releaseYear;
    private String language;
    private Boolean isFeatured;

    private Long id;
    private Long offset;
    private String originalTitle;
    private Integer page;
    private Integer size;
    private Boolean paged;
    private Boolean sortSorted;
    private Boolean sortUnsorted;
    private Integer status;
    private String title;
    private String keyword;
    private Boolean unpaged;
    private Long collectionId;

    private CountryRequest countryRequest;
    private TypeMovieRequest typeMovieRequest;
    private List<CategoryResponse> categoryRequest;
    private LanguageRequest languageRequest;
    private AgeRatingRequest ageRatingRequest;
    private YearReleaseRequest yearReleaseRequest;
    public void generateRequest() {
        if (this.countryRequest != null && this.countryRequest.getValue() != null && !this.countryRequest.getValue().isEmpty()) {
            this.country = this.countryRequest.getValue();
        } else {
            this.country = "";
        }

        if (this.typeMovieRequest != null) {
            this.type = this.typeMovieRequest.getType();
        } else {
            this.type = null;
        }

        if (categoryRequest != null && !categoryRequest.isEmpty()) {
            categoryIds = new ArrayList<>();
            for (CategoryResponse re: categoryRequest) {
                categoryIds.add(re.getId());
            }
        } else {
            categoryIds = new ArrayList<>();
        }

        if (this.languageRequest != null && this.languageRequest.getValue() != null && !this.languageRequest.getValue().isEmpty()) {
            this.language = this.languageRequest.getValue();
        } else {
            this.language = "";
        }

        if (this.ageRatingRequest != null) {
            this.ageRating = this.ageRatingRequest.getType();
        } else {
            this.ageRating = null;
        }
    }
}
