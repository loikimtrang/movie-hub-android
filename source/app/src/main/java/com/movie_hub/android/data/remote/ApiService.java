package com.movie_hub.android.data.remote;

import io.reactivex.rxjava3.core.Observable;

import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.login.UserLoginRequest;
import com.movie_hub.android.data.model.api.request.login.UserRegisterRequest;
import com.movie_hub.android.data.model.api.request.user.UserChangePasswordRequest;
import com.movie_hub.android.data.model.api.request.user.UserUpdateProfileRequest;
import com.movie_hub.android.data.model.api.response.login.UserLoginResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.api.response.user.UserUploadImageResponse;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.QueryMap;

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

    @PUT("v1/user/update-profile")
    Observable<ResponseWrapper> updateUserProfile(@Body UserUpdateProfileRequest request);

    @PUT("v1/user/change-password")
    Observable<ResponseWrapper> changeUserPassword(@Body UserChangePasswordRequest request);
    @Multipart
    @POST("/v1/file/upload")
    Call<ResponseWrapper<UserUploadImageResponse>> uploadAvatar(
            @Part MultipartBody.Part file,
            @Part("type") RequestBody type
    );


    // MOVIE CONTROLLER
    @GET("v1/movie/list")
    Observable<ResponseWrapper<ResponseListObj<MovieResponse>>> getListMovie(@QueryMap Map<String, Object> query);
//    Map<String, Object> query = RequestToMapConverter.convert(movieRequest);

}
