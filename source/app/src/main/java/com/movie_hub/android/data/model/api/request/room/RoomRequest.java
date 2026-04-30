package com.movie_hub.android.data.model.api.request.room;

import lombok.Data;

@Data
public class RoomRequest {
    private Long hostId;
    private Long id;
    private Integer kind;
    private Long movieItemId;
    private Long offset;
    private Integer page;
    private Integer size;
    private Boolean paged;

    private Boolean sortSorted;
    private Boolean sortUnsorted;

    private Boolean sortState;
    private Integer state;
    private Boolean unpaged;
}
