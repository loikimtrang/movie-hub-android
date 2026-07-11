package com.movie_hub.android.data.model.api.response.token_anonymous;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class AnonymousTokenResponse {

    private Map<String, Object> additionalInformation;
    private String expiration;
    private boolean expired;

    @SerializedName("expires_in")
    private int expiresIn;

    private RefreshToken refreshToken;
    private List<String> scope;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("access_token")
    private String accessToken;

    @Data
    public static class RefreshToken {
        private String value;
    }
}
