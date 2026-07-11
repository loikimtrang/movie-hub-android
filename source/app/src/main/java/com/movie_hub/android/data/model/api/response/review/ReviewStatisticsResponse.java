package com.movie_hub.android.data.model.api.response.review;

import lombok.Data;

@Data
public class ReviewStatisticsResponse {
    private Long reviewCount;
    private Double averageRating;
}
