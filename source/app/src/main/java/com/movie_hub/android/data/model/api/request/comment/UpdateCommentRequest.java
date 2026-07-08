package com.movie_hub.android.data.model.api.request.comment;

import lombok.Data;

@Data
public class UpdateCommentRequest {
    private String content;
    private Long id;
}
