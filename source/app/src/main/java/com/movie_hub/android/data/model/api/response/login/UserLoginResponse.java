package com.movie_hub.android.data.model.api.response.login;

import lombok.Data;

@Data
public class UserLoginResponse {
    private String access_token;
    private String additional_info;
    private long expires_in;
    private String grant_type;
    private String jti;
    private String refresh_token;
    private String scope;
    private String tenant_info;
    private String token_type;
    private long user_id;
    private int user_kind;
    private String code;

}
