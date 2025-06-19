package com.movie_hub.android.data.remote;

import io.reactivex.rxjava3.core.Observable;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.login.LoginRequest;
import com.movie_hub.android.data.model.api.response.login.LoginResponse;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/v1/employee/login")
    @Headers({"IgnoreAuth: 1"})
    Observable<ResponseWrapper<LoginResponse>> login(@Body LoginRequest request);
}
