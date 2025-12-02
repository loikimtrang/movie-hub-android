package com.movie_hub.android.ui.main.movie.detail.comment.model;

import lombok.Data;

@Data
public class TagComment {
    private String label;
    private Long movieId;
    private Long movieItemId;
    private boolean isSelect;
}
