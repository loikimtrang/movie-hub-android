package com.movie_hub.android.data.remote;

import io.reactivex.rxjava3.core.Observable;

import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.comment.CreateCommentReactionRequest;
import com.movie_hub.android.data.model.api.request.comment.CreateCommentRequest;
import com.movie_hub.android.data.model.api.request.comment.UpdateCommentRequest;
import com.movie_hub.android.data.model.api.request.forgot.ForgotChangePasswordRequest;
import com.movie_hub.android.data.model.api.request.forgot.ForgotPasswordRequest;
import com.movie_hub.android.data.model.api.request.history.TrackingWatchHistoryRequest;
import com.movie_hub.android.data.model.api.request.login.UserLoginRequest;
import com.movie_hub.android.data.model.api.request.login.UserRegisterRequest;
import com.movie_hub.android.data.model.api.request.notification.UpdateReadRequest;
import com.movie_hub.android.data.model.api.request.otp.VerifyOtpRequest;
import com.movie_hub.android.data.model.api.request.playlist.CreatePlaylistRequest;
import com.movie_hub.android.data.model.api.request.playlist.GetListMoviePlayListRequest;
import com.movie_hub.android.data.model.api.request.playlist.RemoveItemPlaylistRequest;
import com.movie_hub.android.data.model.api.request.playlist.UpdatePlayListItemRequest;
import com.movie_hub.android.data.model.api.request.playlist.UpdatePlaylistRequest;
import com.movie_hub.android.data.model.api.request.review.CreateReviewReactionRequest;
import com.movie_hub.android.data.model.api.request.review.CreateReviewRequest;
import com.movie_hub.android.data.model.api.request.room.CreateRoomRequest;
import com.movie_hub.android.data.model.api.request.survey.MakeSurveyRequest;
import com.movie_hub.android.data.model.api.request.user.UserChangePasswordRequest;
import com.movie_hub.android.data.model.api.request.user.UserLoginGoogleRequest;
import com.movie_hub.android.data.model.api.request.user.UserUpdateProfileRequest;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.appversion.CheckAppVersionResponse;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.data.model.api.response.chat.ChatResponse;
import com.movie_hub.android.data.model.api.response.collection.CollectionResponse;
import com.movie_hub.android.data.model.api.response.comment.CommentResponse;
import com.movie_hub.android.data.model.api.response.comment.VoteListResponse;
import com.movie_hub.android.data.model.api.response.favourite.FavouriteResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.history.MovieHistoryResponse;
import com.movie_hub.android.data.model.api.response.history.WatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.moviePerson.MoviePersonResponse;
import com.movie_hub.android.data.model.api.response.notification.CountUnReadResponse;
import com.movie_hub.android.data.model.api.response.notification.NotificationResponse;
import com.movie_hub.android.data.model.api.response.person.PersonResponse;
import com.movie_hub.android.data.model.api.response.login.UserLoginResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.playlist.PlayListByMovieResponse;
import com.movie_hub.android.data.model.api.response.playlist.PlayListResponse;
import com.movie_hub.android.data.model.api.response.report.CreateReportRequest;
import com.movie_hub.android.data.model.api.response.review.ReviewResponse;
import com.movie_hub.android.data.model.api.response.room.RoomResponse;
import com.movie_hub.android.data.model.api.response.side_bar.SidebarResponse;
import com.movie_hub.android.data.model.api.response.subtitle.SubtitleResponse;
import com.movie_hub.android.data.model.api.response.suggesst.CategoryByWatch;
import com.movie_hub.android.data.model.api.response.suggesst.SuggestByWatchResponse;
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
    @Multipart
    @POST("/v1/file/upload")
    Call<ResponseWrapper<UserUploadImageResponse>> uploadAvatar(
            @Part MultipartBody.Part file,
            @Part("type") RequestBody type
    );


    // MOVIE CONTROLLER
    @GET("v1/movie/list")
    Observable<ResponseWrapper<ResponseListObj<MovieResponse>>> getListMovie(@QueryMap Map<String, Object> query);

    @GET("v1/movie/top-views")
    Observable<ResponseWrapper<ResponseListObj<MovieResponse>>> getListTopViewsMovie(@QueryMap Map<String, Object> query);

    @GET("v1/category/list")
    Observable<ResponseWrapper<ResponseListObj<CategoryResponse>>> getListCategory(@QueryMap Map<String, Object> query);
    @GET("v1/movie/suggestion/{id}")
    Observable<ResponseWrapper<List<MovieResponse>>> getListMovieRecommendation(@Path("id") Long id);

    @GET("v1/movie/get/{id}")
    Observable<ResponseWrapper<MovieResponse>> getMovie(@Path("id") Long id);

    @GET("v1/movie/history")
    Observable<ResponseWrapper<List<MovieHistoryResponse>>> getListMovieHistory();

    @GET("v1/movie/list-survey")
    Observable<ResponseWrapper<List<MovieResponse>>> getListMovieSurvey(@QueryMap Map<String, Object> query);
    @GET("v1/movie/suggest-by-watched")
    Observable<ResponseWrapper<SuggestByWatchResponse>> getSuggestByWatched(@QueryMap Map<String, Object> query);

    @GET("v1/movie/recommendation")
    Observable<ResponseWrapper<List<MovieResponse>>> getRecommendMovie();

    @GET("v1/movie/recommendation/knn")
    Observable<ResponseWrapper<ResponseListObj<MovieResponse>>> getRecommendMovieKnn();

    @GET("v1/movie/recommendation/recent-watched-category")
    Observable<ResponseWrapper<CategoryByWatch>> getCategoryByWatched();
    @POST("v1/movie/make-survey")
    Observable<ResponseWrapper> makeSurvey(@Body MakeSurveyRequest request);
//    Map<String, Object> query = RequestToMapConverter.convert(movieRequest);
    @GET("v1/movie/schedule")
    Observable<ResponseWrapper<List<MovieItemResponse>>> getListMovieSchedule(@QueryMap Map<String, Object> query);

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

    @DELETE("v1/comment/delete/{id}")
    Observable<ResponseWrapper> deleteComment(@Path("id") Long id);

    @PUT("v1/comment/update")
    Observable<ResponseWrapper> updateComment(@Body UpdateCommentRequest request);

    @GET("v1/comment/get/{id}")
    Observable<ResponseWrapper<CommentResponse>> getComment(@Path("id") Long id);

    // Review
    @GET("v1/review/list")
    Observable<ResponseWrapper<ResponseListObj<ReviewResponse>>> getReviewList(@QueryMap Map<String, Object> query);

    @POST("v1/review/create")
    Observable<ResponseWrapper<ReviewResponse>> createReview(@Body CreateReviewRequest request);

    @PATCH("v1/review/vote")
    Observable<ResponseWrapper> voteReview(@Body CreateReviewReactionRequest request);

    @GET("v1/review/vote-list/{movieId}")
    Observable<ResponseWrapper<List<VoteListResponse>>> getVoteListReview(@Path("movieId") Long movieId);

    @GET("v1/review/check/{movieId}")
    Observable<ResponseWrapper<ReviewResponse>> checkReview(@Path("movieId") Long id);
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
    Observable<ResponseWrapper<PlayListByMovieResponse>> getPlaylistsByMovie(@Path("movieId") Long movieId);

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

    @GET("v1/notification/list")
    Observable<ResponseWrapper<ResponseListObj<NotificationResponse>>> getNotifications(@QueryMap Map<String, Object> query);

    @PUT("v1/notification/update-read")
    Observable<ResponseWrapper> updateRead(@Body UpdateReadRequest request);

    @PUT("v1/notification/read-all")
    Observable<ResponseWrapper> readAllNotifications();

    @DELETE("v1/notification/delete/{id}")
    Observable<ResponseWrapper> deleteNotification(@Path("id") Long id);

    @GET("v1/notification/count-unread")
    Observable<ResponseWrapper<CountUnReadResponse>> countUnRead();

    @POST("v1/room/create")
    Observable<ResponseWrapper<RoomResponse>> createRoom(@Body CreateRoomRequest request);

    @GET("v1/room/list")
    Observable<ResponseWrapper<ResponseListObj<RoomResponse>>> getListRoom(@QueryMap Map<String, Object> query);

    @GET("v1/room/my-rooms")
    Observable<ResponseWrapper<ResponseListObj<RoomResponse>>> getListMyRoom(@QueryMap Map<String, Object> query);

    @GET("v1/room/check")
    Observable<ResponseWrapper<RoomResponse>> checkRoom();

    @DELETE("v1/room/delete/{id}")
    Observable<ResponseWrapper> deleteRoom(@Path("id") Long id);

    @POST("v1/room/end/{id}")
    Observable<ResponseWrapper<RoomResponse>> endRoom(@Path("id") Long id);

    @POST("v1/room/join/{id}")
    Observable<ResponseWrapper<RoomResponse>> joinRoom(@Path("id") Long id);

    @POST("v1/room/start/{id}")
    Observable<ResponseWrapper<RoomResponse>> startRoom(@Path("id") Long id);

    @GET("v1/room/get/{id}")
    Observable<ResponseWrapper<RoomResponse>> getRoom(@Path("id") Long id);
    @Headers("UseGuestToken: 1")
    @GET("v1/video-library-subtitle/list")
    Observable<ResponseWrapper<ResponseListObj<SubtitleResponse>>> getListSubtitle(@QueryMap Map<String, Object> query);

    @POST("v1/user-report/create")
    Observable<ResponseWrapper<Void>> createReport(@Body CreateReportRequest request);

    @GET("v1/room/get-by-code/{code}")
    Observable<ResponseWrapper<RoomResponse>> getRoomByCode(@Path("code") String code);

    @GET("v1/chat/list")
    Observable<ResponseWrapper<ResponseListObj<ChatResponse>>> getChatList(@QueryMap Map<String, Object> query);
}
