package com.movie_hub.android.data.model.api.request.login;

import lombok.Data;

@Data
public class UserLoginRequest {
    String username;
    String password;
}
