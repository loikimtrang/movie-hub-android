package com.movie_hub.android.ui.main.movie.watch;

import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.favourite.CreateFavouriteRequest;
import com.movie_hub.android.data.model.api.request.history.ListWatchHistoryRequest;
import com.movie_hub.android.data.model.api.request.history.TrackingWatchHistoryRequest;
import com.movie_hub.android.data.model.api.request.setting.UserSettingsRequest;
import com.movie_hub.android.data.model.api.request.subtitle.SubtitleRequest;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.room.RoomResponse;
import com.movie_hub.android.data.model.api.response.subtitle.SubtitleResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.api.response.video.VideoResponse;
import com.movie_hub.android.data.model.mqtt.ClientPingModel;
import com.movie_hub.android.data.model.mqtt.CreateChatModel;
import com.movie_hub.android.data.mqtt.Command;
import com.movie_hub.android.data.mqtt.Message;
import com.movie_hub.android.ui.base.activity.BaseViewModel;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.movie.watch.setting.SettingVideoModel;
import com.movie_hub.android.ui.main.movie.watch.setting.VideoQuality;
import com.movie_hub.android.utils.NetworkUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.reactivex.exceptions.Exceptions;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.Getter;
import timber.log.Timber;

public class WatchMovieViewModel extends BaseViewModel {
    public MovieResponse movieDetails;
    public String nowUriPlay;
    public MovieItemResponse nowEpisodePlay;
    public VideoResponse nowVideoPlay; // Single movie

    public UserSettingsRequest setting = new UserSettingsRequest();

    private MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(true);
    public MutableLiveData<ListWatchHistoryResponse> movieDetailsTracking = new MutableLiveData<>();
    public long lastPlaybackPosition = 0L;
    /** True while the chat panel is visible ({@link #setChatOpen(boolean)}). */
    public boolean isChatOpen = false;

    private final MutableLiveData<Boolean> chatUnreadIndicatorVisible = new MutableLiveData<>(false);

    public boolean isLiveRoom = false;
    public boolean isHost = false;
    public RoomResponse roomDetail;
    public UserResponse userResponse;
    private MutableLiveData<List<SubtitleResponse>> subtitleList = new MutableLiveData<>(new ArrayList<>());

    private final List<CreateChatModel> chatModels = new ArrayList<>();
    private final MutableLiveData<List<CreateChatModel>> chatModelsLiveData =
            new MutableLiveData<>(Collections.emptyList());
    private MutableLiveData<Boolean> isSyncWithHost = new MutableLiveData<>(true);
    /**
     * When true, participant with Sync-with-Host must not use local playback controls
     * (play/pause, seek, ±10s, speed); volume, brightness, quality, lock, exit remain allowed.
     * Public for layout Data Binding ({@code vm.participantPlaybackRestricted}).
     */
    public final MutableLiveData<Boolean> participantPlaybackRestricted = new MutableLiveData<>(false);
    public ClientPingModel clientPingModel = new ClientPingModel();
    public Message msgPing = new Message();
    SettingVideoModel settingVideoModel = new SettingVideoModel();
    public WatchMovieViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
        settingVideoModel.initSetting();

        isSyncWithHost.setValue(true);
        refreshLiveRoomUiState();

        clientPingModel.setAccountId(getUserId().toString());
        msgPing.setCmd(Command.COMMAND_CLIENT_PING);
        msgPing.setData(clientPingModel);
    }

    public LiveData<Boolean> getIsSyncWithHost() {
        return isSyncWithHost;
    }

    public void setSyncWithHost(boolean enable) {
        isSyncWithHost.setValue(enable);
        refreshLiveRoomUiState();
    }

    public boolean isSyncWithHostEnabled() {
        Boolean v = isSyncWithHost.getValue();
        return v == null || v;
    }

    /**
     * Recompute flags after {@link #isLiveRoom}, {@link #isHost}, or sync toggle changes.
     */
    public void refreshLiveRoomUiState() {
        boolean restricted = isLiveRoom && !isHost && isSyncWithHostEnabled();
        participantPlaybackRestricted.setValue(restricted);
    }

    public List<CreateChatModel> getChatModels() {
        return Collections.unmodifiableList(chatModels);
    }

    public LiveData<List<CreateChatModel>> getChatModelsLiveData() {
        return chatModelsLiveData;
    }

    public LiveData<Boolean> getChatUnreadIndicatorVisible() {
        return chatUnreadIndicatorVisible;
    }

    /**
     * Call when the user opens or closes the chat panel. Opening clears the unread dot and marks messages read.
     */
    public void setChatOpen(boolean open) {
        isChatOpen = open;
        if (open) {
            chatUnreadIndicatorVisible.setValue(false);
            markAllChatMessagesRead();
            postChatListUpdate();
        }
    }

    private void markAllChatMessagesRead() {
        for (CreateChatModel m : chatModels) {
            m.setUnread(false);
        }
    }

    private void postChatListUpdate() {
        chatModelsLiveData.setValue(new ArrayList<>(chatModels));
    }

    /**
     * Append a chat line from MQTT (including self after broker echo). No duplicate local insert on send.
     */
    public void appendIncomingChatMessage(CreateChatModel model) {
        if (model == null || TextUtils.isEmpty(model.getContent())) return;
        if (isChatOpen) {
            model.setUnread(false);
        } else {
            model.setUnread(true);
            chatUnreadIndicatorVisible.setValue(true);
        }
        chatModels.add(model);
        postChatListUpdate();
    }

    public void clearChatMessages() {
        chatModels.clear();
        chatUnreadIndicatorVisible.setValue(false);
        postChatListUpdate();
    }

    public Message buildRoomChatMqttMessage(CreateChatModel payload) {
        Message m = new Message();
        m.setCmd(Command.CMD_CREATE_CHAT);
        m.setData(payload);
        return m;
    }

    /** Topic {@code room/{roomId}} for chat publish/subscribe. */
    public String getRoomMqttTopicForChat() {
        if (roomDetail == null) return null;
        return Constants.TOPIC + roomDetail.getId();
    }

    public CreateChatModel buildOutgoingChatMessage(String messageContent, UserResponse user) {
        String accountId = user != null
                ? String.valueOf(user.getId())
                : String.valueOf(getUserId());
        SimpleDateFormat isoUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        isoUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
        String createDate = isoUtc.format(new Date());
        return new CreateChatModel(accountId, messageContent.trim(), user, createDate);
    }

    public LiveData<Boolean> getParticipantPlaybackRestricted() {
        return participantPlaybackRestricted;
    }

    public boolean isParticipantPlaybackRestricted() {
        return Boolean.TRUE.equals(participantPlaybackRestricted.getValue());
    }

    /** Next episode / episode list are hidden for any live room (host or participant). */
    public boolean shouldHideEpisodeNavInLiveRoom() {
        return isLiveRoom && movieDetails != null && movieDetails.getType() == Constants.TYPE_MOVIE_SERIES;
    }

    /**
     * Watch party ended but user continues locally: clear room identity so MQTT handlers no-op,
     * sync restrictions lift, and episode UI can behave like normal playback.
     */
    public void clearLiveRoomSessionForSoloPlayback() {
        isLiveRoom = false;
        isHost = false;
        roomDetail = null;
        setSyncWithHost(false);
        clearChatMessages();
        refreshLiveRoomUiState();
    }

    public LiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying.setValue(playing);
    }

    public void updateSettingWhenChangeEpisode() {
        settingVideoModel.getQuality().setAuto(true);
        settingVideoModel.getQuality().setResolution(new VideoQuality());
        settingVideoModel.getAvailableQualities().clear();
    }
    @Getter
    private String tokenVideo;

    public void setTokenVideo(String token) {
        this.tokenVideo = "Bearer " + token;
    }

    private Disposable tokenRefreshDisposable;

    public void startTokenAutoRefresh() {
        if (isLogin()) {
            setTokenVideo(repository.getToken());
            tokenReady.postValue(true);
            Timber.d("👤 User đã đăng nhập → dùng token login, không auto refresh.");
            return;
        }

        getAnonymousToken();

        if (tokenRefreshDisposable != null && !tokenRefreshDisposable.isDisposed()) return;

        tokenRefreshDisposable = Observable.interval(10, 10, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    Timber.d("🔄 Refreshing anonymous token...");
                    getAnonymousToken();
                });
    }


    public void stopTokenAutoRefresh() {
        if (tokenRefreshDisposable != null && !tokenRefreshDisposable.isDisposed()) {
            tokenRefreshDisposable.dispose();
        }
    }

    private final MutableLiveData<Boolean> tokenReady = new MutableLiveData<>();

    public LiveData<Boolean> getTokenReady() {
        return tokenReady;
    }

    public void getAnonymousToken() {
        if (isLogin()) {
            setTokenVideo(repository.getToken());
            tokenReady.postValue(true);
            return;
        }

        compositeDisposable.add(repository.getMasterApiService().getAnonymousToken()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable ->
                        throwable.flatMap(throwable1 -> {
                            if (NetworkUtils.checkNetworkError(throwable1)) {
                                return application.showDialogNoInternetAccess();
                            } else {
                                return Observable.error(throwable1);
                            }
                        })
                )
                .subscribe(
                        response -> {
                            String token = response.getAccessToken();
                            setTokenVideo(token);
                            Timber.d("✅ Token đã sẵn sàng: %s", token);
                            tokenReady.postValue(true);
                            Constants.TOKEN_GUEST = token;
                        },
                        throwable -> {
                            Timber.e(throwable, "❌ Lỗi khi gọi anonymous token");
                            tokenReady.postValue(false); // hoặc xử lý retry
                        }
                ));
    }

    public void updateTrackingMovie(TrackingWatchHistoryRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(repository.getApiService().updateHistory(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(errors ->
                        errors
                                .zipWith(Observable.range(1, 3), (Throwable error, Integer retryCount) -> {
                                    if (NetworkUtils.checkNetworkError(error)) {
                                        return retryCount;
                                    } else {
                                        throw Exceptions.propagate(error);
                                    }
                                })
                                .flatMap(retryCount -> {
                                    return Observable.timer(3, TimeUnit.SECONDS);
                                })
                )

                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                getListMovieTracking(movieDetails.getId());
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                        }
                )
        );
    }

    public void getListMovieTracking(long movieId) {
        ListWatchHistoryRequest request = new ListWatchHistoryRequest();
        request.setMovieId(movieId);

        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(repository.getApiService().getListWatchHistory(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(errors ->
                        errors
                                .zipWith(Observable.range(1, 3), (Throwable error, Integer retryCount) -> {
                                    if (NetworkUtils.checkNetworkError(error)) {
                                        return retryCount;
                                    } else {
                                        throw Exceptions.propagate(error);
                                    }
                                })
                                .flatMap(retryCount -> {
                                    return Observable.timer(3, TimeUnit.SECONDS);
                                })
                )
                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                movieDetailsTracking.postValue(response.getData());
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                        }
                )
        );
    }


    public void getUserProfile(MainCallback<UserResponse> callback) {
        compositeDisposable.add(repository.getMasterApiService().getUserProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable ->
                        throwable.flatMap((Function<Throwable, ObservableSource<?>>) throwable1 -> {
                            if (NetworkUtils.checkNetworkError(throwable1)) {
                                hideLoading();
                                return application.showDialogNoInternetAccess();
                            }else{
                                return Observable.error(throwable1);
                            }
                        })
                )
                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                callback.doSuccess(response.getData());
                            } else {
                                callback.doFail();
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                            callback.doError(throwable);
                        }
                )
        );
    }
    public void endRoom(Long roomId) {
        compositeDisposable.add(
                repository.getApiService().endRoom(roomId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retryWhen(throwable ->
                                throwable.flatMap((Function<Throwable, ObservableSource<?>>) throwable1 -> {
                                    if (NetworkUtils.checkNetworkError(throwable1)) {
                                        return application.showDialogNoInternetAccess();
                                    } else {
                                        return Observable.error(throwable1);
                                    }
                                })
                        )
                        .subscribe(
                                (ResponseWrapper response) -> {
                                },
                                throwable -> {
                                    hideLoading();
                                    Timber.e(throwable);
                                }
                        )
        );
    }

    public void getListSubtitle(MainCallback<List<SubtitleResponse>> callback, Long idVideo) {
        SubtitleRequest request = new SubtitleRequest();
        request.setVideoLibraryId(idVideo);

        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(
                repository.getApiService().getListSubtitle(query)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retryWhen(throwable ->
                                throwable.flatMap((Function<Throwable, ObservableSource<?>>) throwable1 -> {
                                    if (NetworkUtils.checkNetworkError(throwable1)) {
                                        return application.showDialogNoInternetAccess();
                                    } else {
                                        return Observable.error(throwable1);
                                    }
                                })
                        )
                        .subscribe(
                                (response) -> {
                                    if (response.isResult()) {
                                        callback.doSuccess(response.getData().getContent());
                                    } else {
                                        callback.doFail();
                                    }
                                },
                                throwable -> {
                                    hideLoading();
                                    callback.doError(throwable);
                                    Timber.e(throwable);
                                }
                        )
        );
    }
}
