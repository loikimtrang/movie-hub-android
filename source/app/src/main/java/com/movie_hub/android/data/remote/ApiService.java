package com.movie_hub.android.data.remote;

import io.reactivex.rxjava3.core.Observable;

import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.login.UserLoginRequest;
import com.movie_hub.android.data.model.api.request.login.UserRegisterRequest;
import com.movie_hub.android.data.model.api.request.otp.VerifyOtpRequest;
import com.movie_hub.android.data.model.api.request.user.UserChangePasswordRequest;
import com.movie_hub.android.data.model.api.request.user.UserLoginGoogleRequest;
import com.movie_hub.android.data.model.api.request.user.UserUpdateProfileRequest;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.appversion.CheckAppVersionResponse;
import com.movie_hub.android.data.model.api.response.moviePerson.MoviePersonResponse;
import com.movie_hub.android.data.model.api.response.person.PersonResponse;
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
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface ApiService {

    // USER CONTROLLER
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

    @POST("v1/user/resend-otp")
    Observable<ResponseWrapper> resendOtp(@Body VerifyOtpRequest request);

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

    @GET("v1/movie/get/{id}")
    Observable<ResponseWrapper<MovieResponse>> getMovie(@Path("id") Long id);
//    Map<String, Object> query = RequestToMapConverter.convert(movieRequest);


    // PERSION CONTROLLER
    @GET("v1/person/list")
    Observable<ResponseWrapper<ResponseListObj<PersonResponse>>> getListPerson(@QueryMap Map<String, Object> query);

    @GET("v1/person/get/{id}")
    Observable<ResponseWrapper<PersonResponse>> getPerson(@Path("id") Long id);
    // MOVIE PERSON

    @GET("v1/movie-person/list")
    Observable<ResponseWrapper<ResponseListObj<MoviePersonResponse>>> getListMoviePerson(@QueryMap Map<String, Object> query);

    // MOVIE ITEM CONTROLLER
    @GET("v1/movie-item/list")
    Observable<ResponseWrapper<ResponseListObj<MovieItemResponse>>> getListMovieItem(@QueryMap Map<String, Object> query);

    @GET("v1/movie-item/get/{id}")
    Observable<ResponseWrapper<MovieItemResponse>> getMovieItem(@Path("id") Long id);
//    Map<String, Object> query = RequestToMapConverter.convert(movieRequest);

    // APP
    @GET("v1/app-version/check-version")
    Observable<ResponseWrapper<CheckAppVersionResponse>> checkVersion(@QueryMap Map<String, Object> query);
}
