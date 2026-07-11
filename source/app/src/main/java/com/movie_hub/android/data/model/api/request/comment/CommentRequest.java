package com.movie_hub.android.data.model.api.request.comment;

import lombok.Data;

@Data
public class CommentRequest {
    private Long authorId;
    private Long id;
    private Boolean isParent;
    private Boolean isPinned;
    private Long movieId;
    private Long movieItemId;
    private Long offset;
    private Integer page;
    private Integer size;
    private Boolean paged;
    private Long parentId;
    private Boolean sortSorted;     // map với sort.sorted
    private Boolean sortUnsorted;   // map với sort.unsorted
    private Integer status;
    private Boolean unpaged;
    private Boolean isOpenChildComment;

}

