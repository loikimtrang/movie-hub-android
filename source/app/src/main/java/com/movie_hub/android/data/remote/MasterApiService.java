package com.movie_hub.android.data.remote;

import com.movie_hub.android.data.model.api.request.user.RefreshTokenRequest;
import com.movie_hub.android.data.model.api.response.login.UserLoginResponse;
import com.movie_hub.android.data.model.api.response.token_anonymous.AnonymousTokenResponse;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface MasterApiService {
    @POST("/v1/auth/get-anonymous-token")
    @Headers({"IgnoreAuth: 1"})
    Observable<AnonymousTokenResponse> getAnonymousToken();

    @POST("api/token")
    @Headers({"UseBasicAuth: 1"})
    Observable<UserLoginResponse> refreshToken(@Body RefreshTokenRequest request);

    @POST("api/token")
    @Headers({"UseBasicAuth: 1"})
    Call<UserLoginResponse> refreshTokenSync(@Body RefreshTokenRequest request);

}
