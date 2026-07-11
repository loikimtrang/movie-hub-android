package com.movie_hub.android.data.model.api.request.comment;

import lombok.Data;

@Data
public class CreateCommentReactionRequest {
    private Long id;
    private int type;
}
