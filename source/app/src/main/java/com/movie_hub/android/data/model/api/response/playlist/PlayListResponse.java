package com.movie_hub.android.data.model.api.response.playlist;

import lombok.Data;


@Data
public class PlayListResponse {
    private Long id;
    private String name;
    private Integer status;
    private Integer totalMovie;
    private String createdDate;
    private String modifiedDate;
}

