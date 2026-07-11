package com.movie_hub.android.data.model.api.response.chat;

import com.movie_hub.android.data.model.api.response.user.UserResponse;

import lombok.Data;

@Data
public class ChatResponse {
    private Long id;
    private String content;
    private String createdDate;
    private String modifiedDate;
    private Integer status;
    private UserResponse user;
}
