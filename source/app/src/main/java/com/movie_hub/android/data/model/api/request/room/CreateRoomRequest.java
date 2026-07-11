package com.movie_hub.android.data.model.api.request.room;

import java.util.List;
import lombok.Data;

@Data
public class CreateRoomRequest {
    private List<Long> accountIds;
    private boolean isStartNow;
    private Integer kind;
    private Long movieItemId;
    private String name;
    private String startTime;
}
