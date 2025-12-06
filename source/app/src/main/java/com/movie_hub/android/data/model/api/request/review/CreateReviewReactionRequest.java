package com.movie_hub.android.data.model.api.request.review;

import lombok.Data;

@Data
public class CreateReviewReactionRequest {
    private Long id;
    private int type;
}
