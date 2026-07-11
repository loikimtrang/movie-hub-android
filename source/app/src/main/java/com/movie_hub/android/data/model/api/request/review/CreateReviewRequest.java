package com.movie_hub.android.data.model.api.request.review;

import lombok.Data;

@Data
public class CreateReviewRequest {
    private String content;
    private Long movieId;
    private int rate;
    private boolean isSelect;

    public CreateReviewRequest(int rate, boolean isSelect) {
        this.rate = rate;
        this.isSelect = isSelect;
    }

    public CreateReviewRequest() {
    }
}
