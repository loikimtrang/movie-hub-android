package com.movie_hub.android.data.model.api.response.comment;

import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;

import java.util.List;

import lombok.Data;

@Data
public class CommentResponse {
    private Long id;
    private Long movieId;
    private String content;
    private String createdDate;
    private String modifiedDate;
    private boolean isPinned;
    private int status;
    private int totalChildren;
    private int totalLike;
    private int totalDislike;
    private UserResponse author;
    private MovieItemResponse movieItem;
    private CommentResponse parent;
    private Boolean isOpenChildComment;
    private List<CommentResponse> childComments;
    private boolean isLike;
    private boolean isDislike;
}
