package com.movie_hub.android.ui.main.home.filter.adapter;

import com.movie_hub.android.data.model.api.request.movie.filter.AgeRatingRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.CountryRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.LanguageRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.TypeMovieRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.YearReleaseRequest;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;

public interface OnFilterClick {
    void onCategoryFilterClick(CategoryResponse categoryResponse);
    void onCountryFilterClick(CountryRequest request);
    void onLanguageFilterClick(LanguageRequest request);
    void onAgeRatingFilterClick(AgeRatingRequest request);
    void onTypeFilterClick(TypeMovieRequest request);
    void onYearFilterClick(YearReleaseRequest request);

}
