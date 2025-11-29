package com.movie_hub.android.data.model.api.request.comment;

import lombok.Data;

@Data
public class CreateCommentRequest {
    private String content;
    private boolean isPinned;
    private Long movieId;
    private Long movieItemId;
    private Long parentId;
}
