package com.movie_hub.android.data.remote;

import io.reactivex.rxjava3.core.Observable;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.login.UserLoginRequest;
import com.movie_hub.android.data.model.api.request.login.UserRegisterRequest;
import com.movie_hub.android.data.model.api.request.user.UserChangePasswordRequest;
import com.movie_hub.android.data.model.api.response.login.UserLoginResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {

    // USER CONTROLLER
    @POST("v1/user/login")
    @Headers({"IgnoreAuth: 1"})
    Observable<UserLoginResponse> userLogin(@Body UserLoginRequest request);

    @POST("v1/user/register")
    @Headers({"IgnoreAuth: 1"})
    Observable<ResponseWrapper> userRegister(@Body UserRegisterRequest request);

    @GET("v1/user/profile")
    Observable<ResponseWrapper<UserResponse>> getUserProfile();

    @GET("v1/user/update-profile")
    Observable<ResponseWrapper> updateUserProfile();

    @GET("v1/user/change-password")
    Observable<ResponseWrapper> changeUserPassword(@Body UserChangePasswordRequest request);
}
