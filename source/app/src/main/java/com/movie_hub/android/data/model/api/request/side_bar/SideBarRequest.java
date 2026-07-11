package com.movie_hub.android.data.model.api.request.side_bar;

import lombok.Data;

@Data
public class SideBarRequest {
    private Long id;
    private Long offset;
    private Integer page;
    private Integer size;
    private Boolean paged;
    private Boolean sortSorted;
    private Boolean sortUnsorted;
    private Integer status;
    private String title;
    private Integer type;
    private Boolean unpaged;
    private Boolean active;
}

