package com.movie_hub.android.data.model.api.response.user;

import com.movie_hub.android.data.model.api.response.group.GroupResponse;

import lombok.Data;

@Data
public class UserResponse {
    private String avatarPath;
    private String createdDate;
    private String email;
    private String fullName;
    private GroupResponse group;
    private long id;
    private int kind;
    private String modifiedDate;
    private String phone;
    private int status;
    private String username;
}
