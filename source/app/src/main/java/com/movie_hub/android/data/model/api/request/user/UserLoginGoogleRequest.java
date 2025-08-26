package com.movie_hub.android.data.model.api.request.user;

import lombok.Data;

@Data
public class UserLoginGoogleRequest {
    private String idToken;
    private int platform = 2;
}
