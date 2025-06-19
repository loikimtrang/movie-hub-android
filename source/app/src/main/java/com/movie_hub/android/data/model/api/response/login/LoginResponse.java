package com.movie_hub.android.data.model.api.response.login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String access_token;
    private String token_type;
    private String refresh_token;
    private Long expires_in;
    private String scope;
    private Long user_kind;
    private Long user_id;
    private String grant_type;
    private String additional_info;
    private String jti;
    private String url;
    private String tenant_name;
    private String tenant_info;
}
