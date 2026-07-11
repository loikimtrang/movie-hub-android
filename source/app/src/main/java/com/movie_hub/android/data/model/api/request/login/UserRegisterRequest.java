package com.movie_hub.android.data.model.api.request.login;

import lombok.Data;

@Data
public class UserRegisterRequest {
    private String email;
    private String fullName;
    private String password;
}
