package com.movie_hub.android.data.remote;

import com.movie_hub.android.data.model.api.response.token_anonymous.AnonymousTokenResponse;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface MasterApiService {
    @POST("/v1/auth/get-anonymous-token")
    @Headers({"IgnoreAuth: 1"})
    Observable<AnonymousTokenResponse> getAnonymousToken();
}
