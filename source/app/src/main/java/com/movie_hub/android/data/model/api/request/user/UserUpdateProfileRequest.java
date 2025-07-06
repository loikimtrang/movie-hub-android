package com.movie_hub.android.data.model.api.request.user;

import lombok.Data;

@Data
public class UserUpdateProfileRequest {
    private String avatarPath;
    private String fullName;
    private String phone;
    private String username;
    private int gender;
}

