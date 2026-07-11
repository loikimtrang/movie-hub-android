package com.movie_hub.android.data.remote;

import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.forgot.ForgotChangePasswordRequest;
import com.movie_hub.android.data.model.api.request.forgot.ForgotPasswordRequest;
import com.movie_hub.android.data.model.api.request.login.UserLoginRequest;
import com.movie_hub.android.data.model.api.request.login.UserRegisterRequest;
import com.movie_hub.android.data.model.api.request.otp.VerifyOtpRequest;
import com.movie_hub.android.data.model.api.request.setting.UserSettingsRequest;
import com.movie_hub.android.data.model.api.request.user.RefreshTokenRequest;
import com.movie_hub.android.data.model.api.request.user.UserChangePasswordRequest;
import com.movie_hub.android.data.model.api.request.user.UserLoginGoogleRequest;
import com.movie_hub.android.data.model.api.request.user.UserUpdateProfileRequest;
import com.movie_hub.android.data.model.api.response.login.UserLoginResponse;
import com.movie_hub.android.data.model.api.response.token_anonymous.AnonymousTokenResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.QueryMap;

import java.util.Map;

public interface MasterApiService {
    @POST("/v1/auth/get-anonymous-token")
    @Headers({"IgnoreAuth: 1"})
    Observable<AnonymousTokenResponse> getAnonymousToken();
    @POST("v1/auth/logout")
    Observable<ResponseWrapper> logout();
    @POST("api/token")
    @Headers({"UseBasicAuth: 1"})
    Observable<UserLoginResponse> refreshToken(@Body RefreshTokenRequest request);

    @POST("api/token")
    @Headers({"UseBasicAuth: 1"})
    Call<UserLoginResponse> refreshTokenSync(@Body RefreshTokenRequest request);


    @POST("v1/user/login")
    @Headers({"IgnoreAuth: 1"})
    Observable<UserLoginResponse> userLogin(@Body UserLoginRequest request);
    @POST("v1/user/auth/mobile-callback")
    @Headers({"IgnoreAuth: 1"})
    Observable<UserLoginResponse> userLoginGoogle(@Body UserLoginGoogleRequest request);
    @POST("v1/user/register")
    @Headers({"IgnoreAuth: 1"})
    Observable<ResponseWrapper> userRegister(@Body UserRegisterRequest request);

    @GET("v1/user/profile")
    Observable<ResponseWrapper<UserResponse>> getUserProfile();

    @PUT("v1/user/update-profile")
    Observable<ResponseWrapper> updateUserProfile(@Body UserUpdateProfileRequest request);

    @POST("v1/user/verify-otp")
    Observable<ResponseWrapper> verifyOtp(@Body VerifyOtpRequest request);
    @POST("v1/user/request-forgot-password")
    Observable<ResponseWrapper> requestForgotPassword(@Body ForgotPasswordRequest request);
    @POST("v1/user/forgot-password")
    Observable<ResponseWrapper> forgotPassword(@Body ForgotChangePasswordRequest request);
    @POST("v1/user/resend-otp")
    Observable<ResponseWrapper> resendOtp(@Body VerifyOtpRequest request);

    @PUT("v1/user/change-password")
    Observable<ResponseWrapper> changeUserPassword(@Body UserChangePasswordRequest request);

    @PUT("v1/user/update-settings")
    Observable<ResponseWrapper> updateUserSetting(@Body UserSettingsRequest request);

    @GET("v1/user/auto-complete")
    Observable<ResponseWrapper<ResponseListObj<UserResponse>>> getUserAutoComplete(@QueryMap Map<String, Object> query);
}
