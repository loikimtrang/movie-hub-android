package com.movie_hub.android.data.model.api.response.review;

import com.movie_hub.android.data.model.api.response.user.UserResponse;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private String content;
    private String createdDate;
    private String modifiedDate;
    private Integer rate;
    private Integer status;
    private Integer totalLike;
    private Integer totalDislike;
    private Long movieId;
    private UserResponse author;

    private boolean isLike;
    private boolean isDislike;

    private ReviewStatisticsResponse statistics;
}

