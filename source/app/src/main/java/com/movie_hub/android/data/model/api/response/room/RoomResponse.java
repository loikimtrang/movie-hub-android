package com.movie_hub.android.data.model.api.response.room;

import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;

import lombok.Data;

@Data
public class RoomResponse {
    private Long id;
    private String code;
    private String name;
    private Integer kind;
    private Integer state;
    private Integer status;
    private Integer participantCount;

    private String startTime;
    private String endTime;
    private String createdDate;
    private String modifiedDate;

    private UserResponse host;
    private MovieItemResponse movieItem;
}