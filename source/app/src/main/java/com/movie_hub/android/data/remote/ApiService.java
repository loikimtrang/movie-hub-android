package com.movie_hub.android.data.remote;

import io.reactivex.rxjava3.core.Observable;

import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.comment.CreateCommentReactionRequest;
import com.movie_hub.android.data.model.api.request.comment.CreateCommentRequest;
import com.movie_hub.android.data.model.api.request.forgot.ForgotChangePasswordRequest;
import com.movie_hub.android.data.model.api.request.forgot.ForgotPasswordRequest;
import com.movie_hub.android.data.model.api.request.history.TrackingWatchHistoryRequest;
import com.movie_hub.android.data.model.api.request.login.UserLoginRequest;
import com.movie_hub.android.data.model.api.request.login.UserRegisterRequest;
import com.movie_hub.android.data.model.api.request.otp.VerifyOtpRequest;
import com.movie_hub.android.data.model.api.request.playlist.CreatePlaylistRequest;
import com.movie_hub.android.data.model.api.request.playlist.GetListMoviePlayListRequest;
import com.movie_hub.android.data.model.api.request.playlist.RemoveItemPlaylistRequest;
import com.movie_hub.android.data.model.api.request.playlist.UpdatePlayListItemRequest;
import com.movie_hub.android.data.model.api.request.playlist.UpdatePlaylistRequest;
import com.movie_hub.android.data.model.api.request.user.UserChangePasswordRequest;
import com.movie_hub.android.data.model.api.request.user.UserLoginGoogleRequest;
import com.movie_hub.android.data.model.api.request.user.UserUpdateProfileRequest;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.appversion.CheckAppVersionResponse;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.data.model.api.response.collection.CollectionResponse;
import com.movie_hub.android.data.model.api.response.comment.CommentResponse;
import com.movie_hub.android.data.model.api.response.comment.VoteListResponse;
import com.movie_hub.android.data.model.api.response.favourite.FavouriteResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.history.MovieHistoryResponse;
import com.movie_hub.android.data.model.api.response.history.WatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.moviePerson.MoviePersonResponse;
import com.movie_hub.android.data.model.api.response.person.PersonResponse;
import com.movie_hub.android.data.model.api.response.login.UserLoginResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.playlist.PlayListResponse;
import com.movie_hub.android.data.model.api.response.side_bar.SidebarResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.api.response.user.UserUploadImageResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
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
    @POST("v1/user/request-forgot-password")
    Observable<ResponseWrapper> requestForgotPassword(@Body ForgotPasswordRequest request);
    @POST("v1/user/forgot-password")
    Observable<ResponseWrapper> forgotPassword(@Body ForgotChangePasswordRequest request);
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

    @GET("v1/category/list")
    Observable<ResponseWrapper<ResponseListObj<CategoryResponse>>> getListCategory(@QueryMap Map<String, Object> query);
    @GET("v1/movie/recommendations/{id}")
    Observable<ResponseWrapper<List<MovieResponse>>> getListMovieRecommendation(@Path("id") Long id);

    @GET("v1/movie/get/{id}")
    Observable<ResponseWrapper<MovieResponse>> getMovie(@Path("id") Long id);

    @GET("v1/movie/history")
    Observable<ResponseWrapper<List<MovieHistoryResponse>>> getListMovieHistory();
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

    // FAVOURITE CONTROLLER
    @POST("v1/favourite/create")
    Observable<ResponseWrapper<Long>> createFavourite(@Body Map<String, Object> body);

    @DELETE("v1/favourite/delete/{id}")
    Observable<ResponseWrapper> deleteFavourite(@Path("id") Long id);

    @GET("v1/favourite/get")
    Observable<ResponseWrapper<FavouriteResponse>> getFavourite(@QueryMap Map<String, Object> query);
    @GET("v1/favourite/list")
    Observable<ResponseWrapper<ResponseListObj<FavouriteResponse>>> getFavouriteList(@QueryMap Map<String, Object> query);

    //Watch History Controller

    @POST("v1/watch-history/tracking")
    Observable<ResponseWrapper> updateHistory(@Body Map<String, Object> body);

    @GET("v1/watch-history/list")
    Observable<ResponseWrapper<ListWatchHistoryResponse>> getListWatchHistory(@QueryMap Map<String, Object> query);

    //Comment controller

    @GET("v1/comment/list")
    Observable<ResponseWrapper<ResponseListObj<CommentResponse>>> getCommentList(@QueryMap Map<String, Object> query);

    @POST("v1/comment/create")
    Observable<ResponseWrapper> createComment(@Body CreateCommentRequest request);

    @PUT("v1/comment/vote")
    Observable<ResponseWrapper> voteComment(@Body CreateCommentReactionRequest request);

    @GET("v1/comment/vote-list/{movieId}")
    Observable<ResponseWrapper<List<VoteListResponse>>> getVoteList(@Path("movieId") Long movieId);

    // Playlist
    @GET("v1/playlist/list")
    Observable<ResponseWrapper<List<PlayListResponse>>> getPlaylistList();

    @GET("v1/playlist/get/{id}")
    Observable<ResponseWrapper<PlayListResponse>> getPlaylistById(@Path("id") Long id);

    @POST("v1/playlist/create")
    Observable<ResponseWrapper<PlayListResponse>> createPlaylist(@Body CreatePlaylistRequest request);

    @PUT("v1/playlist/update")
    Observable<ResponseWrapper> updatePlaylist(@Body UpdatePlaylistRequest request);

    @DELETE("v1/playlist/delete/{id}")
    Observable<ResponseWrapper> deletePlaylist(@Path("id") Long id);

    @GET("v1/playlist/{id}/movies")
    Observable<ResponseWrapper<ResponseListObj<MovieResponse>>> getPlaylistMovies(
            @Path("id") Long playlistId,
            @QueryMap Map<String, Object> queryParams
    );

    @DELETE("v1/playlist/remove-item")
    Observable<ResponseWrapper> removeMovieFromPlaylist(@QueryMap Map<String, Object> queryParams);
    @GET("v1/playlist/list-by-movie/{movieId}")
    Observable<ResponseWrapper<List<Long>>> getPlaylistsByMovie(@Path("movieId") Long movieId);

    @POST("v1/playlist/update-item")
    Observable<ResponseWrapper> updateItemPlayList(@Body UpdatePlayListItemRequest request);

    // SideBar
    @GET("v1/sidebar/list")
    Observable<ResponseWrapper<ResponseListObj<SidebarResponse>>> getSideBarList(@QueryMap Map<String, Object> query);

    @GET("v1/collection/list")
    Observable<ResponseWrapper<ResponseListObj<CollectionResponse>>> getCollectionList(@QueryMap Map<String, Object> query);

    @GET("v1/collection/topics")
    Observable<ResponseWrapper<ResponseListObj<CollectionResponse>>> getTopicList(@QueryMap Map<String, Object> query);

    @GET("v1/collection-item/list")
    Observable<ResponseWrapper<ResponseListObj<MovieResponse>>> getCollectionItemList(@QueryMap Map<String, Object> query);
}
