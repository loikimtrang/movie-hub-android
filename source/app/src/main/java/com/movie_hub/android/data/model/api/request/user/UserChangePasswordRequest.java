package com.movie_hub.android.data.model.api.request.user;

import lombok.Data;

@Data
public class UserChangePasswordRequest {
    private String newPassword;
    private String oldPassword;
}
