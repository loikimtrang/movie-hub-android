package com.movie_hub.android.ui.main.movie.watch;

import android.animation.ObjectAnimator;
import android.net.Uri;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.WindowCompat;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.common.text.Cue;
import androidx.media3.common.text.CueGroup;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.mediacodec.MediaCodecInfo;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.AutoTransition;
import androidx.transition.ChangeBounds;
import androidx.transition.Fade;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.bumptech.glide.Glide;
import com.google.common.collect.ImmutableList;
import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.request.history.TrackingWatchHistoryRequest;
import com.movie_hub.android.data.model.api.request.setting.UserSettingsRequest;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.history.WatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.room.RoomResponse;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;
import com.movie_hub.android.data.model.api.response.subtitle.SubtitleResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.api.response.video.VideoResponse;
import com.movie_hub.android.data.model.mqtt.CreateChatModel;
import com.movie_hub.android.data.model.mqtt.ClientPingModel;
import com.movie_hub.android.data.model.mqtt.EndRoomModel;
import com.movie_hub.android.data.model.mqtt.ParticipantJoinModel;
import com.movie_hub.android.data.model.mqtt.RoomStateModel;
import com.movie_hub.android.data.model.mqtt.UpdateParticipantCountModel;
import com.movie_hub.android.data.mqtt.Command;
import com.movie_hub.android.data.mqtt.Message;
import com.movie_hub.android.databinding.ActivityWatchMovieBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.login.LoginActivity;
import com.movie_hub.android.ui.main.movie.watch.Provider.SpriteThumbnailManager;
import com.movie_hub.android.ui.main.movie.watch.adapter.ChatAdapter;
import com.movie_hub.android.ui.main.movie.watch.adapter.EpisodeItemListHoriAdapter;
import com.movie_hub.android.ui.main.movie.watch.adapter.SeasonItemAdapter;
import com.movie_hub.android.ui.main.movie.watch.dialog.MoreOptionBottomSheetDialog;
import com.movie_hub.android.ui.main.movie.watch.dialog.PlaySpeedBottomSheetDialog;
import com.movie_hub.android.ui.main.movie.watch.dialog.QualityBottomSheetDialog;
import com.movie_hub.android.ui.main.movie.watch.dialog.SettingBottomSheetDialog;
import com.movie_hub.android.ui.main.movie.watch.dialog.SubtitleBottomSheetDialog;
import com.movie_hub.android.ui.main.movie.watch.dialog.SubtitleCustomizeBottomSheetDialog;
import com.movie_hub.android.ui.main.movie.watch.setting.SubtitleStyle;
import com.movie_hub.android.ui.main.movie.watch.setting.SettingVideoModel;
import com.movie_hub.android.ui.main.movie.watch.setting.VideoQuality;
import com.movie_hub.android.utils.DeviceUtils;
import com.movie_hub.android.utils.DialogUtils;
import com.movie_hub.android.utils.RoomDialogUtils;
import com.movie_hub.android.utils.GsonUtils;
import com.movie_hub.android.utils.LiveDataUtils;
import com.movie_hub.android.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.movie_hub.android.BR;
import timber.log.Timber;

public class WatchMovieActivity extends BaseActivity<ActivityWatchMovieBinding, WatchMovieViewModel> implements View.OnClickListener,
        SettingBottomSheetDialog.SettingBottomSheetCallback,
        PlaySpeedBottomSheetDialog.PlaySpeedBottomSheetCallback,
        QualityBottomSheetDialog .QualityBottomSheetCallback,
        MoreOptionBottomSheetDialog.MoreOptionBottomSheetCallback,
        SeasonItemAdapter.OnSeasonClickListener,
        EpisodeItemListHoriAdapter.OnEpisodeClickListener {
    private static final long CHAT_ANIM_DURATION_MS = 260L;
    private static final float CHAT_SWIPE_CLOSE_THRESHOLD = 0.32f; // fraction of chat width
    private boolean chatTransitionRunning = false;
    private ExoPlayer player;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int SEEK_DOUBLE_TAP_MILLIS = 10000;
    private Handler seekHandler = new Handler(Looper.getMainLooper());
    private Handler autoHideHandler = new Handler(Looper.getMainLooper());
    private Handler countResetHandler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarRunnable;
    private Runnable resetCountRunnable;
    private boolean isUserSeeking = false;
    private boolean isBuffering = false;
    private boolean isLockScreen = false;
    private boolean isVideoReadyWhenStartActivity = false;
    boolean isZoomed  = false;
    private ScaleGestureDetector scaleGestureDetector;
    private static final int DOUBLE_TAP_TIMEOUT = 800;
    private int forwardCount = 0;
    private int previousCount = 0;
    private AudioManager audioManager;
    private int maxVolume;
    private int currentVolume;
    private SeasonItemAdapter seasonItemAdapter;
    private EpisodeItemListHoriAdapter episodeItemListHoriAdapter;
    private SpriteThumbnailManager thumbnailManager;
    public boolean isStartContinueWatch = false;
    boolean isSeries = false;
    private Handler trackingHandler = new Handler(Looper.getMainLooper());
    private Runnable trackingRunnable;
    private static final long TRACKING_INTERVAL_MS = 5 * 60 * 1000L;
    private int currentSeasonIndex = 0;
    public static String LiveRoom = "isLiveRoom";
    public static String Host = "isHost";
    public static String ROOM = "room_details";
    private boolean suppressSpeedBroadcast = false;
    private boolean suppressModelSpeedObserver = false;
    private boolean suppressMqttPublishFromModel = false;
    @Nullable private Float normalSpeedBeforePress = null;
    private boolean suppressSyncSwitchListener = false;

    private Handler handlerPing = new Handler(Looper.getMainLooper());
    private Runnable runnablePing;

    private ChatAdapter chatAdapter;
    private final Handler handlerRetryGetListSubtitle = new Handler(Looper.getMainLooper());
    private boolean lastImeVisible = false;
    @Nullable private ViewTreeObserver.OnGlobalLayoutListener imeLayoutListener;
    private void startSchedule() {
        runnablePing = new Runnable() {
            @SuppressLint("TimberArgCount")
            @Override
            public void run() {
                Timber.d("MQTT_LOG: ", "Ping to server");
                sendMqttMessage(viewModel.msgPing, Constants.TOPIC + viewModel.roomDetail.getId());
                handlerPing.postDelayed(this, 10000);
            }
        };
        handlerPing.post(runnablePing);
    }

    private void stopSchedule() {
        if (handlerPing != null && runnablePing != null) {
            handlerPing.removeCallbacks(runnablePing);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setLifecycleOwner(this);
        viewModel.getParticipantPlaybackRestricted().observe(this,
                restricted -> applyParticipantSyncRestrictedUi(Boolean.TRUE.equals(restricted)));

        applyScreenCaptureProtection();
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        hideSystemUI();
        viewModel.startTokenAutoRefresh();

        String json = getIntent().getStringExtra("movie_details");
        MovieResponse movie = GsonUtils.fromJson(json, MovieResponse.class);

        if (movie != null) {
            viewModel.movieDetails = movie;
            viewModel.isLiveRoom = getIntent().getBooleanExtra(LiveRoom, false);
            viewModel.setting = createDefaultSettings();
            applyUserSubtitleSettings();
            thumbnailManager = new SpriteThumbnailManager(this, viewBinding.ivThumbnailPreview, viewBinding.thumbnailPreviewContainer);

            isSeries = viewModel.movieDetails != null &&
                    viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES;

            if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SINGLE) {
                viewModel.nowVideoPlay = viewModel.movieDetails.getSeasons().get(viewModel.movieDetails.getSeasons().size() - 1).getVideo();
                setUpViewForSingleMovie();
            } else if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES) {
                String jsonEpisode = getIntent().getStringExtra("episode");
                viewModel.nowEpisodePlay = GsonUtils.fromJson(jsonEpisode, MovieItemResponse.class);
                assert viewModel.nowEpisodePlay != null;
                viewModel.nowVideoPlay = viewModel.nowEpisodePlay.getVideo();
                setUpAdapterEpisode();
                setUpViewForSeriesMovie();
            }

            if (!viewModel.isLiveRoom) {
                if (viewModel.isLogin()) {
                    String jsonTracking = getIntent().getStringExtra("movie_details_tracking");
                    ListWatchHistoryResponse listTracking = GsonUtils.fromJson(jsonTracking, ListWatchHistoryResponse.class);
                    if (listTracking != null) {
                        viewModel.movieDetailsTracking.postValue(listTracking);
                    }
                    getUserProfile();
                } else {
                    initMovie();
                }
            } else {
                viewModel.isHost = getIntent().getBooleanExtra(Host, false);
                String roomJson = getIntent().getStringExtra(ROOM);
                viewModel.roomDetail = GsonUtils.fromJson(roomJson, RoomResponse.class);
                viewModel.refreshLiveRoomUiState();
                viewModel.initLiveRoomViewerCount(
                        viewModel.roomDetail != null ? viewModel.roomDetail.getParticipantCount() : null);
                setupSyncWithHostToggle();

                if (viewModel.isHost) {
                    startSchedule();
                }
                getUserProfileForLive();
                viewBinding.btnOpenChat.setVisibility(View.VISIBLE);
                viewBinding.btnRoomInfo.setVisibility(View.VISIBLE);
                setupChatUi();
                setupImeAwareChatInput();
            }


            loadSubtitlesForCurrentVideo();
        }

        setupChatSwipeToClose();
    }

    private void setupImeAwareChatInput() {
        if (imeLayoutListener != null) return;

        final View root = viewBinding.getRoot();
        imeLayoutListener = () -> {
            Rect r = new Rect();
            root.getWindowVisibleDisplayFrame(r);

            int screenHeight = root.getRootView().getHeight();
            int visibleHeight = r.height();
            int heightDiff = Math.max(0, screenHeight - visibleHeight);

            // Heuristic threshold to decide IME visibility (landscape + immersive can vary)
            boolean imeVisible = heightDiff > dpToPx(140);
            if (imeVisible == lastImeVisible) return;
            lastImeVisible = imeVisible;

            applyChatImeState(imeVisible);
        };
        root.getViewTreeObserver().addOnGlobalLayoutListener(imeLayoutListener);
    }

    private void applyChatImeState(boolean imeVisible) {
        // Only animate when chat is actually open/visible (prevents background layout churn).
        if (viewBinding.layoutChat.getVisibility() != View.VISIBLE) return;

        TransitionManager.beginDelayedTransition(viewBinding.layoutChat, new ChangeBounds());
        ViewGroup.LayoutParams lp = viewBinding.bottomComment.getLayoutParams();
        ViewGroup.LayoutParams inputLp = viewBinding.lInput.getLayoutParams();
        ViewGroup.LayoutParams editLp = viewBinding.edtComment.getLayoutParams();

        if (imeVisible) {
            // Expand the input panel so the EditText can grow into remaining space.
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            viewBinding.bottomComment.setLayoutParams(lp);
            viewBinding.rvChat.setVisibility(View.GONE);
            inputLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            viewBinding.lInput.setLayoutParams(inputLp);
            editLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            viewBinding.edtComment.setLayoutParams(editLp);
            viewBinding.edtComment.setMinLines(4);
            viewBinding.edtComment.setMaxLines(12);
            viewBinding.edtComment.setGravity(Gravity.START | Gravity.TOP);
        } else {
            // Restore normal chat list + compact input.
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            viewBinding.bottomComment.setLayoutParams(lp);
            viewBinding.rvChat.setVisibility(View.VISIBLE);
            inputLp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            viewBinding.lInput.setLayoutParams(inputLp);
            editLp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            viewBinding.edtComment.setLayoutParams(editLp);
            viewBinding.edtComment.setMinLines(1);
            viewBinding.edtComment.setMaxLines(4);
            viewBinding.edtComment.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private Transition buildChatTransition(boolean opening) {
        Transition slide = new Slide(Gravity.END);
        slide.setDuration(CHAT_ANIM_DURATION_MS);

        Transition fade = new Fade(opening ? Fade.IN : Fade.OUT);
        fade.setDuration(CHAT_ANIM_DURATION_MS);

        Transition bounds = new ChangeBounds();
        bounds.setDuration(CHAT_ANIM_DURATION_MS);

        TransitionSet set = new TransitionSet()
                .setOrdering(TransitionSet.ORDERING_TOGETHER)
                .addTransition(bounds)
                .addTransition(slide)
                .addTransition(fade);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new Transition.TransitionListener() {
            @Override public void onTransitionStart(@NonNull Transition transition) { chatTransitionRunning = true; }
            @Override public void onTransitionEnd(@NonNull Transition transition) { chatTransitionRunning = false; transition.removeListener(this); }
            @Override public void onTransitionCancel(@NonNull Transition transition) { chatTransitionRunning = false; transition.removeListener(this); }
            @Override public void onTransitionPause(@NonNull Transition transition) {}
            @Override public void onTransitionResume(@NonNull Transition transition) {}
        });
        return set;
    }

    private void openChatAnimated() {
        if (chatTransitionRunning) return;
        if (viewBinding.layoutChat.getVisibility() == View.VISIBLE) return;

        viewModel.setChatOpen(true);
        viewBinding.layoutChat.animate().cancel();
        viewBinding.layoutChat.setTranslationX(0f);
        viewBinding.layoutChat.setAlpha(1f);

        ViewGroup root = (ViewGroup) viewBinding.getRoot();
        TransitionManager.beginDelayedTransition(root, buildChatTransition(true));
        viewBinding.layoutChat.setVisibility(View.VISIBLE);
    }

    private void closeChatAnimated() {
        if (chatTransitionRunning) return;
        if (viewBinding.layoutChat.getVisibility() != View.VISIBLE) {
            viewModel.setChatOpen(false);
            return;
        }

        viewModel.setChatOpen(false);
        // IMPORTANT: Do a single TransitionManager transition so bounds + slide/fade stay in sync.
        // Manual property animations here will cause a second pass (stutter) and delay the video resize.
        viewBinding.layoutChat.animate().cancel();
        viewBinding.layoutChat.setTranslationX(0f);
        viewBinding.layoutChat.setAlpha(1f);

        ViewGroup root = (ViewGroup) viewBinding.getRoot();
        TransitionManager.beginDelayedTransition(root, buildChatTransition(false));
        viewBinding.layoutChat.setVisibility(View.GONE);
    }

    private void toggleChatAnimated() {
        if (viewBinding.layoutChat.getVisibility() == View.VISIBLE) {
            closeChatAnimated();
        } else {
            openChatAnimated();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupChatSwipeToClose() {
        // Allow swiping the chat panel (from right side) to close it.
        final int touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();

        viewBinding.layoutChat.setClickable(true);
        viewBinding.layoutChat.setFocusable(true);

        viewBinding.layoutChat.setOnTouchListener(new View.OnTouchListener() {
            float downX;
            float downY;
            float startTx;
            boolean dragging;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (viewBinding.layoutChat.getVisibility() != View.VISIBLE) return false;

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        downY = event.getRawY();
                        startTx = v.getTranslationX();
                        dragging = false;
                        v.animate().cancel();
                        return false; // let children (RecyclerView/EditText) receive events unless we start dragging

                    case MotionEvent.ACTION_MOVE: {
                        float dx = event.getRawX() - downX;
                        float dy = event.getRawY() - downY;

                        if (!dragging) {
                            if (Math.abs(dx) > touchSlop && Math.abs(dx) > Math.abs(dy)) {
                                // Start horizontal drag (swipe right to close).
                                dragging = dx > 0;
                                if (dragging) {
                                    v.getParent().requestDisallowInterceptTouchEvent(true);
                                    v.animate().cancel();
                                }
                            } else {
                                return false;
                            }
                        }

                        if (dragging) {
                            float tx = Math.max(0f, startTx + dx);
                            v.setTranslationX(tx);
                            float w = Math.max(1f, v.getWidth());
                            v.setAlpha(Math.max(0.3f, 1f - (tx / w)));
                            return true;
                        }
                        return false;
                    }

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL: {
                        if (!dragging) return false;

                        float w = Math.max(1f, v.getWidth());
                        float tx = v.getTranslationX();
                        boolean shouldClose = (tx / w) >= CHAT_SWIPE_CLOSE_THRESHOLD;

                        if (shouldClose) {
                            // Reset transient drag state; closing uses TransitionManager for a single, synced animation.
                            v.setTranslationX(0f);
                            v.setAlpha(1f);
                            closeChatAnimated();
                        } else {
                            v.animate()
                                    .translationX(0f)
                                    .alpha(1f)
                                    .setDuration(180L)
                                    .setInterpolator(new DecelerateInterpolator())
                                    .start();
                        }
                        dragging = false;
                        return true;
                    }
                }
                return false;
            }
        });
    }
    private void startGetListSubtitle() {
        handlerRetryGetListSubtitle.removeCallbacksAndMessages(null);
        if (Constants.TOKEN_GUEST == null || Constants.TOKEN_GUEST.isEmpty()) {
            Log.d("Subtitle", "Guest Token chưa có, đang thử lại sau 2s...");
            handlerRetryGetListSubtitle.postDelayed(this::startGetListSubtitle, 2000);
        } else {
            Log.d("Subtitle", "Đã có Guest Token, tiến hành lấy Subtitle.");
            getListSubtitle();
        }
    }
    private void setupChatUi() {
        chatAdapter = new ChatAdapter();
        viewBinding.rvChat.setLayoutManager(new LinearLayoutManager(this));
        viewBinding.rvChat.setAdapter(chatAdapter);
        viewModel.getChatModelsLiveData().observe(this, list -> {
            if (list == null) {
                chatAdapter.submitList(Collections.emptyList());
                return;
            }
            chatAdapter.submitList(new ArrayList<>(list));
            if (!list.isEmpty()) {
                viewBinding.rvChat.post(() ->
                        viewBinding.rvChat.scrollToPosition(list.size() - 1));
            }
        });
        viewModel.getChatUnreadIndicatorVisible().observe(this, visible ->
                viewBinding.icUnread.setVisibility(Boolean.TRUE.equals(visible)
                        ? View.VISIBLE
                        : View.INVISIBLE));
    }

    private void applyScreenCaptureProtection() {
        if (viewModel.isBlockScreenCaptureEnabled()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    private void applyParticipantSyncRestrictedUi(boolean restricted) {
        viewBinding.seekBar.setOnTouchListener(restricted ? (v, event) -> true : null);
        if (restricted) {
            viewBinding.btnPlayPause.setVisibility(View.GONE);
            viewBinding.btnReplay.setVisibility(View.GONE);
            viewBinding.btnSkipIntro.setVisibility(View.GONE);
            viewBinding.btnSkipOutro.setVisibility(View.GONE);
            viewBinding.layoutSpeedPress.setVisibility(View.GONE);
            forwardCount = 0;
            previousCount = 0;
            viewBinding.layoutCountForward.setVisibility(View.GONE);
            viewBinding.layoutCountPrevious.setVisibility(View.GONE);
            normalSpeedBeforePress = null;
        } else {
            if (player != null && player.getPlaybackState() == Player.STATE_ENDED) {
                viewBinding.btnPlayPause.setVisibility(View.GONE);
            } else if (!isBuffering) {
                viewBinding.btnPlayPause.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupSyncWithHostToggle() {
        if (!viewModel.isHost) {
            viewModel.setSyncWithHost(true);
            viewBinding.layoutSync.setVisibility(View.VISIBLE);
        } else {
            return;
        }

        SwitchCompat syncSwitch = viewBinding.switchSyncWithHost;

        viewModel.getIsSyncWithHost().observe(this, enabled -> {
            boolean v = enabled == null || enabled;
            if (syncSwitch.isChecked() == v) return;
            suppressSyncSwitchListener = true;
            syncSwitch.setChecked(v);
            suppressSyncSwitchListener = false;
        });

        syncSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressSyncSwitchListener) return;
            boolean wasEnabled = viewModel.isSyncWithHostEnabled();
            viewModel.setSyncWithHost(isChecked);

            if (!wasEnabled && isChecked && viewModel.isLiveRoom && !viewModel.isHost) {
                requestSyncFromHost();
            }
        });
    }

    private void requestSyncFromHost() {
        if (!viewModel.isLiveRoom || viewModel.isHost) return;
        if (viewModel.roomDetail == null) return;
        String topic = Constants.TOPIC + viewModel.roomDetail.getId();

        Message msg = new Message();
        msg.setCmd(Command.CMD_ROOM_SYNC);

        ParticipantJoinModel participantJoinModel = new ParticipantJoinModel();
        participantJoinModel.setId(viewModel.getUserId().toString());
        msg.setData(participantJoinModel);
        sendMqttMessage(msg, topic);
    }

    private boolean shouldApplyHostPlaybackCommands() {
        return viewModel.isLiveRoom && !viewModel.isHost && viewModel.isSyncWithHostEnabled();
    }

    public void getUserProfile() {
        viewModel.getUserProfile(new MainCallback<UserResponse>() {
            @Override
            public void doError(Throwable error) {

            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doSuccess(UserResponse response) {
                viewModel.userResponse = response;
                String settingJson = response.getSettings();

                if (settingJson != null && !settingJson.isEmpty()) {
                    try {
                        viewModel.setting = GsonUtils.fromJson(settingJson, UserSettingsRequest.class);
                    } catch (Exception e) {
                        viewModel.setting = createDefaultSettings();
                    }
                } else {
                    viewModel.setting = createDefaultSettings();
                }

                applyUserSubtitleSettings();
                initMovie();
            }

            @Override
            public void doFail() {

            }
        });
    }

    public void getUserProfileForLive() {
        viewModel.getUserProfile(new MainCallback<UserResponse>() {
            @Override
            public void doError(Throwable error) {

            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doSuccess(UserResponse response) {
                viewModel.userResponse = response;
                initMovie();
            }

            @Override
            public void doFail() {

            }
        });
    }

    private void loadSubtitlesForCurrentVideo() {
        handlerRetryGetListSubtitle.removeCallbacksAndMessages(null);
        if (viewModel.nowVideoPlay == null || viewModel.nowVideoPlay.getId() == null) return;
        if (!isSubtitleEnabledInSettings()) return;
        if (viewModel.isLogin()) {
            getListSubtitle();
        } else {
            startGetListSubtitle();
        }
    }

    public void getListSubtitle() {
        if (viewModel.nowVideoPlay == null || viewModel.nowVideoPlay.getId() == null) return;
        viewModel.getListSubtitle(new MainCallback<List<SubtitleResponse>>() {
            @Override
            public void doSuccess(List<SubtitleResponse> data) {
                onSubtitleListLoaded(data);
            }
            @Override
            public void doError(Throwable error) {
                onSubtitleListLoaded(null);
            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doFail() {
                onSubtitleListLoaded(null);
            }
        }, viewModel.nowVideoPlay.getId());
    }

    /**
     * Settings may have subtitles enabled, but a given video can have zero tracks from the API.
     */
    private void onSubtitleListLoaded(@Nullable List<SubtitleResponse> data) {
        viewModel.subtitleApiSettledForCurrentVideo = true;
        boolean hasTracks = data != null && !data.isEmpty();
        viewModel.hasSubtitleTracksForCurrentVideo = hasTracks;
        if (!hasTracks) {
            viewModel.currentSubtitle = null;
            clearSubtitleOverlay();
            return;
        }
        applyDefaultSubtitleIfAvailable(data);
    }

    private void applyDefaultSubtitleIfAvailable(List<SubtitleResponse> data) {
        if (!isSubtitleEnabledInSettings()) return;
        if (data == null || data.isEmpty()) return;
        if (player == null) return;

        Runnable applyTrack = () -> {
            for (SubtitleResponse sub : data) {
                if (Boolean.TRUE.equals(sub.getIsDefault())) {
                    viewModel.currentSubtitle = sub;
                    applySubtitle(sub);
                    return;
                }
            }
            viewModel.currentSubtitle = data.get(0);
            applySubtitle(data.get(0));
        };

        if (player.getPlaybackState() == Player.STATE_READY) {
            applyTrack.run();
        } else {
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_READY) {
                        player.removeListener(this);
                        applyTrack.run();
                    }
                }
            });
        }
    }

    private String getSubtitleLabelForSettingsUi() {
        if (!isSubtitleEnabledInSettings()) {
            return getString(R.string.off);
        }
        if (viewModel.subtitleApiSettledForCurrentVideo && !viewModel.hasSubtitleTracksForCurrentVideo) {
            return getString(R.string.subtitle_not_available);
        }
        if (viewModel.currentSubtitle != null && viewModel.currentSubtitle.getLabel() != null) {
            return viewModel.currentSubtitle.getLabel();
        }
        return getString(R.string.off);
    }

    private UserSettingsRequest createDefaultSettings() {
        UserSettingsRequest defaultSetting = new UserSettingsRequest();
        defaultSetting.setPlaybackSpeed(1.0);
        defaultSetting.setAutoSkipIntro(false);
        defaultSetting.setAutoNextEpisode(false);
        defaultSetting.setResolution(0);

        if (audioManager == null) {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        int currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        defaultSetting.setAudio(currentVol);

        try {
            int systemBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            int brightnessPercent = Math.round((systemBrightness / 255f) * 100);
            defaultSetting.setBrightness(brightnessPercent);
        } catch (Settings.SettingNotFoundException e) {
            defaultSetting.setBrightness(50);
        }

        defaultSetting.setSubtitleEnabled(true);
        new SubtitleStyle().applyToUserSettings(defaultSetting);
        return defaultSetting;
    }

    private boolean isSubtitleEnabledInSettings() {
        return viewModel.setting == null
                || viewModel.setting.getSubtitleEnabled() == null
                || Boolean.TRUE.equals(viewModel.setting.getSubtitleEnabled());
    }

    private void applyUserSubtitleSettings() {
        if (viewModel.setting == null) {
            viewModel.setting = createDefaultSettings();
        }
        if (viewModel.setting.getSubtitleEnabled() == null) {
            viewModel.setting.setSubtitleEnabled(true);
        }
        viewModel.settingVideoModel.updateSubtitleStyle(SubtitleStyle.fromUserSettings(viewModel.setting));
        if (!isSubtitleEnabledInSettings()) {
            viewModel.currentSubtitle = null;
            clearSubtitleOverlay();
        }
    }

    //region === Init Movie ===

    public void loadVtt() {
        if (viewModel.nowVideoPlay != null) {
            String sprite = viewModel.nowVideoPlay.getSpriteUrl();
            String vtt = viewModel.nowVideoPlay.getVttUrl();
            if (sprite != null && vtt != null) {
                thumbnailManager.load(vtt, sprite);
            }
        }
    }
    public void initMovie() {
        reset();
        viewModel.nowUriPlay = getVideoUri(viewModel.nowVideoPlay);

        if (Boolean.TRUE.equals(viewModel.getTokenReady().getValue())) {
            setUpMovie(viewModel.nowUriPlay);

        } else {
            LiveDataUtils.observeOnce(viewModel.getTokenReady(), this, isReady -> {
                if (Boolean.TRUE.equals(isReady)) {
                    setUpMovie(viewModel.nowUriPlay);

                }
            });
        }
        viewModel.getIsPlaying().observe(this, this::updatePlayPauseIcons);
    }

    public void handleObserveTracking() {
        viewModel.movieDetailsTracking.observe(this, response -> {
            if (player == null || response == null || response.getWatchHistories() == null) return;

            viewModel.movieDetails.applyWatchHistory(response);
            if (episodeItemListHoriAdapter != null) {
                if (currentSeasonIndex >= 0 && currentSeasonIndex < viewModel.movieDetails.getSeasons().size()) {
                    List<MovieItemResponse> episodes = viewModel.movieDetails.getSeasons().get(currentSeasonIndex).getEpisodes();
                    episodeItemListHoriAdapter.setData(episodes, viewBinding.layoutListEpisodes.rvEpisode);
                } else {
                    Log.e("WatchMovie", "Invalid currentSeasonIndex = " + currentSeasonIndex);
                }

            }

            if (isStartContinueWatch) return;

            Long movieItemId = null;
            if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SINGLE) {
                movieItemId = viewModel.movieDetails.getSeasons().get(0).getId();
            } else if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES && viewModel.nowEpisodePlay != null) {
                movieItemId = viewModel.nowEpisodePlay.getId();
            }

            if (movieItemId == null) return;

            WatchHistoryResponse watchHistory = response.getWatchHistoryByMovieId(movieItemId);
            if (watchHistory == null) return;

            long seekTime = 0;

            if (!Boolean.TRUE.equals(watchHistory.isCompleted()) && watchHistory.getLastWatchSeconds() != null) {
                seekTime = watchHistory.getLastWatchSeconds() * 1000L;
            }

            // Nếu player đã sẵn sàng → seek liền, còn chưa thì chờ tới STATE_READY rồi mới seek
            if (player.getPlaybackState() == Player.STATE_READY) {
                player.seekTo(seekTime);
                isStartContinueWatch = true;
            } else {
                long finalSeekTime = seekTime;
                player.addListener(new Player.Listener() {
                    @Override
                    public void onPlaybackStateChanged(int state) {
                        if (state == Player.STATE_READY) {
                            player.seekTo(finalSeekTime);
                            isStartContinueWatch = true;
                            player.removeListener(this);
                        }
                    }
                });
            }
        });
    }


    public void setUpViewForSingleMovie() {
        viewBinding.nameMovie.setText(Objects.requireNonNull(viewModel.movieDetails.getTitle()));
        viewBinding.nameMovieOriginal.setText(Objects.requireNonNull(viewModel.movieDetails.getOriginalTitle()));
        viewBinding.btnNextEpisode.setVisibility(View.GONE);
        viewBinding.btnEpisodes.setVisibility(View.GONE);
        loadVtt();
    }

    @SuppressLint("SetTextI18n")
    public void setUpViewForSeriesMovie() {
        currentSeasonIndex = viewModel.movieDetails.getIndexSeasonSelect();

        viewBinding.nameMovie.setText(getString(R.string.episode_index) + " " + viewModel.nowEpisodePlay.getLabel()
                + ". " + viewModel.nowEpisodePlay.getTitle());
        viewBinding.nameMovieOriginal.setText(viewModel.movieDetails.getTitle() + findLabelSeason());
        loadVtt();
        if (viewModel.shouldHideEpisodeNavInLiveRoom()) {
            viewBinding.btnNextEpisode.setVisibility(View.GONE);
            viewBinding.btnEpisodes.setVisibility(View.GONE);
        } else if (isLastEpisode()) {
            viewBinding.btnNextEpisode.setVisibility(View.GONE);
        } else {
            viewBinding.btnNextEpisode.setVisibility(View.VISIBLE);
            setUpNextEpisodeLayout();
        }
    }

    @SuppressLint("SetTextI18n")
    public void reset() {
        forwardCount = 0;
        previousCount = 0;
        isVideoReadyWhenStartActivity = false;
        viewBinding.seekBar.setProgress(0);
        viewBinding.tvCurrentTime.setText("00:00");
        viewBinding.tvTotalTime.setText("00:00");

        viewModel.lastPlaybackPosition = 0L;
    }
    public void setUpMovie(String uri) {
        if (!viewModel.setting.isSetupPlaybackSpeed()) {
            if (viewModel.setting.getPlaybackSpeed() != null) {
                float speed = viewModel.setting.getPlaybackSpeed().floatValue();
                viewModel.settingVideoModel.setPlaybackSpeed(speed);
            }

            viewModel.setting.setSetupPlaybackSpeed(true);
        }
        DefaultMediaSourceFactory mediaSourceFactory =
                new DefaultMediaSourceFactory(
                        new TokenRefreshingDataSourceFactoryWatchMovie(viewModel)
                );

        ExoPlayer.Builder playerBuilder = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory);

        // === RENDERERS FACTORY ===
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this)
                .setEnableDecoderFallback(true)
                .setMediaCodecSelector(getCustomCodecSelector());

        if (DeviceUtils.isLowEndDevice(this)) {
            renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
        }

        playerBuilder.setRenderersFactory(renderersFactory);

        // === TRACK SELECTOR (thay thế setForceLowestBitrate) ===
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
        DefaultTrackSelector.Parameters.Builder paramsBuilder = trackSelector.buildUponParameters();

        if (DeviceUtils.isLowEndDevice(this)) {
            paramsBuilder
                    .setMaxVideoBitrate(600_000)     // 600kbps
                    .setMaxVideoSize(854, 480)       // 480p
                    .setForceLowestBitrate(true);    // Ép chọn bitrate thấp nhất
        }

        trackSelector.setParameters(paramsBuilder.build());
        playerBuilder.setTrackSelector(trackSelector);

        // TẠO PLAYER
        player = playerBuilder.build();
        viewBinding.playerView.setPlayer(player);
        if (viewBinding.playerView.getSubtitleView() != null) {
            viewBinding.playerView.getSubtitleView().setVisibility(View.GONE);
        }
        showLoadingVideo();

        MediaItem mediaItem = MediaItem.fromUri(uri);
        player.setMediaItem(mediaItem);
        player.prepare();
        long lastPosition = viewModel.lastPlaybackPosition;
        if (lastPosition > 0) {
            player.seekTo(lastPosition);
            hostSeekVideo();
        }
        player.setPlayWhenReady(true);

        setupPlayerListener();
        bindPlaybackSpeedModelToPlayer();
        bindSubtitleStyleToOverlay();
        setupSeekBar();
        setupGestureDetector();
        setupSeekBarBrightNess();
        setupSeekBarVolume();

        handleObserveTracking();
    }

    private String getVideoUri(VideoResponse videoResponse) {
        if (videoResponse != null) {
            if (!videoResponse.getContent().contains("http")) videoResponse.setContent(videoResponse.getHostname() + Constants.MEDIA_URL_VIDEO + videoResponse.getContent());
            return videoResponse.getContent();
        }
        return "";
    }

    //region === SetUp for Device ===

    @SuppressLint("VisibleForTests")
    private MediaCodecSelector getCustomCodecSelector() {
        return (mimeType, requiresSecureDecoder, requiresTunnelingDecoder) -> {
            if (MimeTypes.VIDEO_H264.equals(mimeType) && DeviceUtils.isQualcommOldChipset()) {
                try {
                    // Tìm decoder software-only cho H.264
                    List<MediaCodecInfo> decoders = MediaCodecUtil.getDecoderInfos(
                            mimeType, requiresSecureDecoder, requiresTunnelingDecoder
                    );

                    for (MediaCodecInfo decoder : decoders) {
                        if (decoder.softwareOnly && decoder.name.contains("google")) {
                            return ImmutableList.of(decoder);
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
            }

            // Mặc định: dùng hệ thống
            return MediaCodecSelector.DEFAULT.getDecoderInfos(mimeType, requiresSecureDecoder, requiresTunnelingDecoder);
        };
    }

    private void autoSelectQualityOnStart() {
        if (!viewModel.settingVideoModel.getQuality().isAuto()) return;

        DeviceUtils.DeviceTier tier = DeviceUtils.getDeviceTier(this);
        int maxHeight;

        if (tier == DeviceUtils.DeviceTier.LOW_END) {
            maxHeight = 480;
        } else if (tier == DeviceUtils.DeviceTier.MID_RANGE) {
            maxHeight = 720;
        } else {
            maxHeight = 1080;
        }

        VideoQuality best = null;
        for (VideoQuality q : viewModel.settingVideoModel.getAvailableQualities()) {
            if (q.height <= maxHeight && (best == null || q.height > best.height)) {
                best = q;
            }
        }

        if (best != null) {
            viewModel.settingVideoModel.getQuality().setResolution(best);
            applyQualityFromSetting();
        }
    }

    //region === Get Quality Video ===
    private void extractAvailableQualities(Tracks tracks) {
        viewModel.settingVideoModel.getAvailableQualities().clear();

        for (Tracks.Group group : tracks.getGroups()) {
            if (group.getType() != C.TRACK_TYPE_VIDEO) continue;

            TrackGroup mediaTrackGroup = group.getMediaTrackGroup();

            for (int i = 0; i < group.length; i++) {
                Format format = group.getTrackFormat(i);
                int height = format.height;
                int bitrate = format.bitrate;

                if (height > 0) {
                    VideoQuality quality = new VideoQuality(
                            height,
                            bitrate,
                            mediaTrackGroup.id,
                            i
                    );
                    viewModel.settingVideoModel.getAvailableQualities().add(quality);
                }
            }
        }

        Collections.sort(viewModel.settingVideoModel.getAvailableQualities(),
                (q1, q2) -> Integer.compare(q2.height, q1.height));

        boolean isCurrentAuto = viewModel.settingVideoModel.getQuality().isAuto();
        VideoQuality currentSelected = viewModel.settingVideoModel.getQuality().getResolution();

        VideoQuality auto = new VideoQuality();
        auto.label = getString(R.string.auto);
        auto.isCheck = isCurrentAuto; // Check nếu đang là auto

        for (VideoQuality quality : viewModel.settingVideoModel.getAvailableQualities()) {
            if (!isCurrentAuto && currentSelected != null) {
                if (quality.height == currentSelected.height) {
                    quality.isCheck = true;
                } else {
                    quality.isCheck = false;
                }
            } else {
                quality.isCheck = false;
            }
        }

        viewModel.settingVideoModel.getAvailableQualities().add(0, auto);
    }

    private VideoQuality findBestMatchingQuality(int settingResolutionId) {
        List<VideoQuality> available = viewModel.settingVideoModel.getAvailableQualities();
        if (available == null || available.isEmpty()) return null;

        int targetHeight;
        switch (settingResolutionId) {
            case 1: targetHeight = 720; break;
            case 2: targetHeight = 1080; break;
            case 3: targetHeight = 1440; break;
            case 4: targetHeight = 4320; break;
            default: return null;
        }

        VideoQuality bestMatch = null;
        for (VideoQuality q : available) {
            if (q.height == targetHeight) return q;

            if (bestMatch == null || q.height > bestMatch.height) {
                bestMatch = q;
            }
        }

        return bestMatch;
    }

    // region === Set Up Listener Video ===
    private boolean isPlayLiveRoom = false;
    private void setupPlayerListener() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_BUFFERING) {
                    viewBinding.btnReplay.setVisibility(View.GONE);
                    showLoadingVideo();
                } else if (state == Player.STATE_READY) {

                    if (!isPlayLiveRoom && viewModel.isLiveRoom && !viewModel.isHost) {
                        isPlayLiveRoom = true;

                        Message message = new Message();
                        message.setCmd(Command.CMD_PARTICIPANT_JOIN);
                        ParticipantJoinModel participantJoinModel = new ParticipantJoinModel();
                        participantJoinModel.setId(viewModel.getUserId().toString());

                        message.setData(participantJoinModel);
                        sendMqttMessage(message, Constants.TOPIC + viewModel.roomDetail.getId().toString());
                    }

                    if (isStartContinueWatch) {
                        updateVideoTracking();
                    }

                    isVideoReadyWhenStartActivity = true;
                    viewBinding.btnReplay.setVisibility(View.GONE);
                    hideLoadingVideo();
                    if (player.getDuration() > 0) {
                        viewBinding.tvTotalTime.setText(formatTime(player.getDuration()));
                    }
                    viewBinding.loadingProgress.setVisibility(View.GONE);

                } else if (state == Player.STATE_ENDED) {
                    handleEndVideo();
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                viewModel.setPlaying(isPlaying);
                updatePlayPauseIcons(isPlaying);

                if (isPlaying) {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }

            @Override
            public void onPlaybackParametersChanged(@NonNull PlaybackParameters playbackParameters) {
                Player.Listener.super.onPlaybackParametersChanged(playbackParameters);
                // Keep SettingVideoModel in sync even if speed was changed outside our model.
                if (suppressModelSpeedObserver) return;
                float speed = playbackParameters.speed;
                Float current = viewModel.settingVideoModel.getPlaySpeed().getSpeed();
                if (current == null || Math.abs(current - speed) > 0.0001f) {
                    suppressMqttPublishFromModel = true; // avoid loop: player -> model -> publish
                    viewModel.settingVideoModel.setPlaybackSpeed(speed);
                    suppressMqttPublishFromModel = false;
                }
            }

            @Override
            public void onTracksChanged(Tracks tracks) {
                extractAvailableQualities(tracks);

                if (!viewModel.setting.isSetupResolution()) {
                    applyInitialSettingResolution();
                    refreshCheckStatus();
                } else {
                    autoSelectQualityOnStart();
                }
            }

            @Override
            public void onVideoSizeChanged(VideoSize videoSize) {
                runOnUiThread(() -> {
                    VideoQuality current = new VideoQuality();
                    current.height = videoSize.height;
                    current.label = current.getQualityLabel(videoSize.height);
                    SettingBottomSheetDialog.videoQuality.postValue(current);
                    QualityBottomSheetDialog.videoQuality.postValue(current);
                });
            }
            @Override
            public void onPlayerError(PlaybackException error) {
                handlePlaybackError(error);
            }

            @Override
            public void onCues(@NonNull CueGroup cueGroup) {
                runOnUiThread(() -> updateSubtitleOverlay(cueGroup));
            }
        });
    }

    private void clearSubtitleOverlay() {
        viewBinding.tvSubTitle.setText("");
        viewBinding.tvSubTitle.setVisibility(View.GONE);
    }

    private void updateSubtitleOverlay(CueGroup cueGroup) {
        renderSubtitleCues(cueGroup);
    }

    /** Media3 1.1.1: cue timing is on {@link CueGroup}, not per {@link Cue}. Poll current cues while playing. */
    private void syncSubtitleOverlayToPlaybackPosition() {
        if (viewModel.currentSubtitle == null || player == null) return;
        renderSubtitleCues(player.getCurrentCues());
    }

    private void renderSubtitleCues(CueGroup cueGroup) {
        if (viewModel.currentSubtitle == null) {
            clearSubtitleOverlay();
            return;
        }
        if (cueGroup.cues == null || cueGroup.cues.isEmpty()) {
            clearSubtitleOverlay();
            return;
        }

        StringBuilder text = new StringBuilder();
        for (Cue cue : cueGroup.cues) {
            CharSequence cueText = cue.text;
            if (cueText == null) continue;
            String line = cueText.toString().trim();
            if (line.isEmpty()) continue;
            if (text.length() > 0) text.append('\n');
            text.append(line);
        }

        if (text.length() > 0) {
            viewBinding.tvSubTitle.setText(text);
            viewBinding.tvSubTitle.setVisibility(View.VISIBLE);
            applySubtitleStyleToOverlay();
        } else {
            clearSubtitleOverlay();
        }
    }

    private void bindSubtitleStyleToOverlay() {
        viewModel.settingVideoModel.getSubtitleStyleLive().observe(this, style -> {
            if (style != null) applySubtitleStyleToOverlay();
        });
    }

    private void applySubtitleStyleToOverlay() {
        SubtitleStyle style = viewModel.settingVideoModel.getSubtitleStyle();
        if (style != null) {
            style.applyTo(viewBinding.tvSubTitle);
        }
    }

    private void bindPlaybackSpeedModelToPlayer() {
        viewModel.settingVideoModel.getPlaybackSpeedLive().observe(this, speed -> {
            if (speed == null || player == null) return;
            if (suppressModelSpeedObserver) return;

            // Model -> player (always), and model -> MQTT (host-only).
            try {
                suppressModelSpeedObserver = true;
                applyPlaybackSpeed(speed, false); // don't let player-listener publish
            } finally {
                suppressModelSpeedObserver = false;
            }

            if (!suppressMqttPublishFromModel && viewModel.isLiveRoom && viewModel.isHost) {
                publishRoomSpeed(speed);
            }
        });
    }

    private void publishRoomSpeed(float speed) {
        if (!viewModel.isLiveRoom || !viewModel.isHost) return;
        if (viewModel.roomDetail == null) return;
        String topic = Constants.TOPIC + viewModel.roomDetail.getId();

        Message msg = new Message();
        msg.setCmd(Command.CMD_ROOM_STATE);

        RoomStateModel roomStateModel = new RoomStateModel();
        roomStateModel.setSubCmd(Command.CMD_ROOM_PLAY_SPEED);
        roomStateModel.setPlaySpeed(speed);

        msg.setData(roomStateModel);
        sendMqttMessage(msg, topic);
    }

    private void applyPlaybackSpeed(float speed, boolean shouldBroadcastIfHost) {
        if (player == null) return;
        try {
            suppressSpeedBroadcast = !shouldBroadcastIfHost;
            player.setPlaybackParameters(new PlaybackParameters(speed));
        } finally {
            suppressSpeedBroadcast = false;
        }
    }

    private void refreshCheckStatus() {
        List<VideoQuality> list = viewModel.settingVideoModel.getAvailableQualities();
        if (list == null || list.isEmpty()) return;

        boolean isAuto = viewModel.settingVideoModel.getQuality().isAuto();
        VideoQuality selected = viewModel.settingVideoModel.getQuality().getResolution();

        for (VideoQuality q : list) {
            if (q.label != null && q.label.equals(getString(R.string.auto))) {
                q.isCheck = isAuto;
            } else {
                q.isCheck = (!isAuto && selected != null && q.height == selected.height);
            }
        }
    }
    private void applyInitialSettingResolution() {
        Integer savedResId = viewModel.setting.getResolution();

        if (savedResId == null || savedResId == 0) {
            viewModel.setting.setSetupResolution(true);
            autoSelectQualityOnStart();
            return;
        }

        VideoQuality target = findBestMatchingQuality(savedResId);

        if (target != null) {
            viewModel.settingVideoModel.getQuality().setResolution(target);
            viewModel.settingVideoModel.getQuality().setAuto(false);

            applyQualityFromSetting();

            viewModel.setting.setSetupResolution(true);
        }
    }
    private void handlePlaybackError(PlaybackException error) {
        hideLoadingVideo();
        boolean isNetworkAvailable = NetworkUtils.isNetworkAvailable(getApplication());
        String mgs = getString(R.string.an_error_occurred);
        if (!isNetworkAvailable) {
            mgs = getString(R.string.network_error_please_check_your_internet_connection);
        }
        String finalMgs = mgs;

        viewModel.lastPlaybackPosition = player != null ? player.getCurrentPosition() : 0L;

        runOnUiThread(() -> {
            viewBinding.lDialogErrorVideo.setVisibility(View.VISIBLE);
            viewBinding.dialogErrorVideoMessage.setText(finalMgs);

            viewBinding.btnRetryErrorVideo.setOnClickListener(v -> {
                viewBinding.lDialogErrorVideo.setVisibility(View.GONE);
                if (viewModel.lastPlaybackPosition == 0L) {
                    reset();
                }
                if (Boolean.TRUE.equals(viewModel.getTokenReady().getValue())) {
                    setUpMovie(viewModel.nowUriPlay);

                } else {
                    LiveDataUtils.observeOnce(viewModel.getTokenReady(), this, isReady -> {
                        if (Boolean.TRUE.equals(isReady)) {
                            setUpMovie(viewModel.nowUriPlay);

                        }
                    });
                }
            });

            viewBinding.btnExitErrorVideo.setOnClickListener(v -> {
                viewBinding.lDialogErrorVideo.setVisibility(View.GONE);
                finish();
            });

        });
    }
    private void updatePlayPauseState(boolean isPlaying) {
        updatePlayPauseState(isPlaying, false);
    }

    private void updatePlayPauseState(boolean isPlaying, boolean fromRemoteRoomState) {
        if (player == null) return;
        if (viewModel.isParticipantPlaybackRestricted() && !fromRemoteRoomState) return;
        if (isPlaying) {
            player.play();
        } else {
            player.pause();
        }
        viewModel.setPlaying(isPlaying);
        updatePlayPauseIcons(isPlaying);
        autoHideHandler.removeCallbacks(hideControlsRunnable);
        autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);

        if (viewModel.isLiveRoom && viewModel.isHost) {
            String topic = Constants.TOPIC + viewModel.roomDetail.getId().toString();
            Message message = new Message();
            message.setCmd(Command.CMD_ROOM_STATE);

            RoomStateModel roomStateModel = new RoomStateModel();
            roomStateModel.setPlay(isPlaying);
            roomStateModel.setSubCmd(isPlaying ? Command.CMD_ROOM_PLAY : Command.CMD_ROOM_PAUSE);

            message.setData(roomStateModel);

            sendMqttMessage(message, topic);
        }
    }

    private int countdown = 5;
    private Handler nextEpisodeHandler = new Handler(Looper.getMainLooper());
    private Runnable nextEpisodeRunnable;

    public void handleEndVideo() {
        updateVideoTracking();

        hideLoadingVideo();
        updatePlayPauseIcons(false);
        viewModel.setPlaying(false);
        viewBinding.btnPlayPause.setVisibility(View.GONE);

        if (viewModel.isLiveRoom) {
            // Watch party: no local replay overlay — playback is driven by the host over MQTT.
            viewBinding.btnReplay.setVisibility(View.GONE);
            applyParticipantSyncRestrictedUi(viewModel.isParticipantPlaybackRestricted());
            return;
        }

        viewBinding.btnReplay.setVisibility(View.VISIBLE);

        if (!isLastEpisode() && isSeries) {
            toggleControls();
            viewBinding.layoutNextEpisode.layoutNextEpisode.setVisibility(View.VISIBLE);
            countdown = 5;
            updateNextEpisodeButtonText();
            startNextEpisodeCountdown();
        }
    }

    private void startNextEpisodeCountdown() {
        nextEpisodeRunnable = new Runnable() {
            @Override
            public void run() {
                countdown--;
                if (countdown > 0) {
                    updateNextEpisodeButtonText();
                    nextEpisodeHandler.postDelayed(this, 1000);
                } else {
                    viewBinding.layoutNextEpisode.layoutNextEpisode.setVisibility(View.GONE);
                    onNextEpisodeClick();
                    stopNextEpisodeCountdown();
                }
            }
        };
        nextEpisodeHandler.postDelayed(nextEpisodeRunnable, 1000);
    }

    private void stopNextEpisodeCountdown() {
        if (nextEpisodeHandler != null && nextEpisodeRunnable != null) {
            nextEpisodeHandler.removeCallbacks(nextEpisodeRunnable);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateNextEpisodeButtonText() {
        viewBinding.layoutNextEpisode.tvCountDown.setText(getString(R.string.next_episode) + " (" + countdown + ")");
    }


    // region === Set Up Seek Bar Video ===
    public void setupSeekBar() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY && player.getDuration() > 0) {
                    viewBinding.tvTotalTime.setText(formatTime(player.getDuration()));
                }
            }
        });

        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying() && !isUserSeeking) {
                    long current = player.getCurrentPosition();
                    long duration = player.getDuration();

                    if (duration > 0) {
                        int progress = (int) (1000 * current / duration);
                        viewBinding.seekBar.setProgress(progress);
                        viewBinding.tvCurrentTime.setText(formatTime(current));
                    }

                    if (viewModel.nowVideoPlay != null) {
                        Long introStart = viewModel.nowVideoPlay.getIntroStart();
                        Long introEnd = viewModel.nowVideoPlay.getIntroEnd();

                        if (introStart != null && introEnd != null) {
                            long introStartMs = introStart * 1000L;
                            long introEndMs = introEnd * 1000L;

                             if (!viewModel.isParticipantPlaybackRestricted() && current >= introStartMs && current < introEndMs && viewBinding.layoutSeekBar.getVisibility() != View.VISIBLE) {
                                if (viewBinding.btnSkipIntro.getVisibility() != View.VISIBLE) {

                                    viewBinding.btnSkipIntro.setVisibility(View.VISIBLE);
                                    if (viewModel.setting.getAutoSkipIntro()) {
                                        handleSkipIntro();
                                    }
                                }
                            } else {
                                if (viewBinding.btnSkipIntro.getVisibility() == View.VISIBLE) {
                                    viewBinding.btnSkipIntro.setVisibility(View.GONE);
                                }
                            }
                        }
                    }

                    if (!viewModel.isParticipantPlaybackRestricted() && viewModel.nowVideoPlay != null && isSeries && !isLastEpisode()) {
                        Long outroStart = viewModel.nowVideoPlay.getOutroStart();

                        if (outroStart != null && player != null) {
                            long outroStartMs = outroStart * 1000L;
                            long outroEndMs = player.getDuration();

                            if (current >= outroStartMs && current < outroEndMs && viewBinding.layoutSeekBar.getVisibility() != View.VISIBLE) {
                                if (viewBinding.btnSkipOutro.getVisibility() != View.VISIBLE) {
                                    viewBinding.btnSkipOutro.setVisibility(View.VISIBLE);
                                    if (viewModel.setting.getAutoNextEpisode()) {
                                        handleSkipOutro();
                                    }
                                }
                            } else {
                                if (viewBinding.btnSkipOutro.getVisibility() == View.VISIBLE) {
                                    viewBinding.btnSkipOutro.setVisibility(View.GONE);
                                }
                            }
                        }
                    }

                    if (viewModel.currentSubtitle != null) {
                        syncSubtitleOverlayToPlaybackPosition();
                    }
                }


                seekHandler.postDelayed(this, 500);
            }
        };

        seekHandler.post(updateSeekBarRunnable);

        viewBinding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser || player == null || viewModel.isParticipantPlaybackRestricted()) return;

                long duration = player.getDuration();
                if (duration <= 0) return;

                long newPosition = (duration * progress) / 1000;
                player.seekTo(newPosition);
                hostSeekVideo();
                viewBinding.tvCurrentTime.setText(formatTime(newPosition));

                if (thumbnailManager != null) {
                    thumbnailManager.showPreviewAt(newPosition);
                    updateThumbnailPosition(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
                autoHideHandler.removeCallbacks(hideControlsRunnable);
                viewBinding.controlVideo.setVisibility(View.GONE);
                viewBinding.layoutBrightness.setVisibility(View.GONE);
                viewBinding.layoutVolume.setVisibility(View.GONE);
                viewBinding.loadingProgress.setVisibility(View.GONE);

                viewBinding.thumbnailPreviewContainer.setVisibility(View.VISIBLE);

                updateThumbnailPosition(seekBar.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                viewBinding.controlVideo.setVisibility(View.VISIBLE);
                viewBinding.layoutBrightness.setVisibility(View.VISIBLE);
                viewBinding.layoutVolume.setVisibility(View.VISIBLE);
                viewBinding.loadingProgress.setVisibility(View.VISIBLE);
                if (thumbnailManager != null) {
                    thumbnailManager.hidePreview();
                }
                viewBinding.thumbnailPreviewContainer.setVisibility(View.GONE);
                autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);

                updateVideoTracking();
            }
        });
    }

    private void updateThumbnailPosition(int progress) {
        viewBinding.seekBar.post(() -> {
            View container = viewBinding.thumbnailPreviewContainer;
            SeekBar seekBar = viewBinding.seekBar;

            int seekBarWidth = seekBar.getWidth();

            int max = seekBar.getMax(); // 1000
            float percent = progress / (float) max;

            int paddingLeft = seekBar.getPaddingLeft();
            int paddingRight = seekBar.getPaddingRight();

            int usableWidth = seekBarWidth - paddingLeft - paddingRight;

            float thumbCenterX = seekBar.getX() + paddingLeft + usableWidth * percent;

            int containerWidth = container.getWidth();
            float translationX = thumbCenterX - containerWidth / 2f;
            float layOutWidth = viewBinding.layoutSeekBar.getWidth() - containerWidth - viewBinding.layoutSeekBar.getPaddingRight() - viewBinding.layoutSeekBar.getPaddingLeft();

            // Clamp
            translationX = Math.max(0, Math.min(translationX, layOutWidth));

            container.setTranslationX(translationX);
        });
    }



    private String formatTime(long millis) {
        if (millis < 0) return "00:00";

        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    // region === Set Up Double Tap, One Tap, Zoom Fit ===
    @SuppressLint("ClickableViewAccessibility")
    public void setupGestureDetector() {
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                if (!isVideoReadyWhenStartActivity || isLockScreen || viewModel.isParticipantPlaybackRestricted()) return true;
                viewBinding.btnSkipOutro.setVisibility(View.GONE);
                viewBinding.btnSkipIntro.setVisibility(View.GONE);
                viewBinding.layoutSeekBar.setVisibility(View.VISIBLE);
                viewBinding.controlVideo.setVisibility(View.GONE);
                viewBinding.layoutBrightness.setVisibility(View.GONE);
                viewBinding.layoutVolume.setVisibility(View.GONE);

                boolean isForward = e.getX() >= (float) viewBinding.playerView.getWidth() / 2;
                handleForwardAndPreviousVideo(isForward);

                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isVideoReadyWhenStartActivity) {
                    toggleControls();
                }
                return true;
            }

            @SuppressLint("DefaultLocale")
            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                if (player != null && player.isPlaying() && !isLockScreen && !viewModel.isParticipantPlaybackRestricted()) {
                    viewBinding.playerView.setHapticFeedbackEnabled(true);
                    viewBinding.playerView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                    // "Hold for 2x" style: temporarily boost speed while finger is down.
                    // Update SettingVideoModel so UI reflects the boosted speed.
                    normalSpeedBeforePress = viewModel.settingVideoModel.getPlaySpeed().getSpeed();
                    float boosted = viewModel.settingVideoModel.getPlaySpeedWhenPress().getSpeed();
                    viewModel.settingVideoModel.setPlaybackSpeed(boosted); // observer will update player + MQTT
                    autoHideHandler.removeCallbacks(hideControlsRunnable);
                    autoHideHandler.postDelayed(hideControlsRunnable, 0);

                    runOnUiThread(() -> {
                        viewBinding.tvSpeed.setText(String.format(Locale.US,"%.2fx", viewModel.settingVideoModel.getPlaySpeedWhenPress().getSpeed()));
                        viewBinding.layoutSpeedPress.setVisibility(View.VISIBLE);
                    });
                }
            }
        });

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private float startSpan;

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                startSpan = detector.getCurrentSpan();
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (isLockScreen) return true;
                float scale = detector.getCurrentSpan() / startSpan;
                if (scale > 1.1f && !isZoomed) {
                    isZoomed = true;
                    viewBinding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                }

                else if (scale < 0.9f && isZoomed) {
                    isZoomed = false;
                    viewBinding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                }
                return true;
            }
        });

        viewBinding.playerView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            scaleGestureDetector.onTouchEvent(event);

            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (player != null && player.isPlaying()) {
                    // Restore the speed that was active before long-press.
                    Float restore = normalSpeedBeforePress != null
                            ? normalSpeedBeforePress
                            : viewModel.settingVideoModel.getPlaySpeed().getSpeed();
                    if (restore != null) {
                        viewModel.settingVideoModel.setPlaybackSpeed(restore); // observer will update player + MQTT
                    }
                    normalSpeedBeforePress = null;
                    runOnUiThread(() -> viewBinding.layoutSpeedPress.setVisibility(View.GONE));
                }
            }
            return true;
        });
    }
    public void handleForwardAndPreviousVideo(boolean isForward) {
        if (!isVideoReadyWhenStartActivity || viewModel.isParticipantPlaybackRestricted()) return;

        long currentPosition = player.getCurrentPosition();
        long seekToPosition = currentPosition + (isForward ? SEEK_DOUBLE_TAP_MILLIS : -SEEK_DOUBLE_TAP_MILLIS);
        Log.d("SEE", String.valueOf(seekToPosition));
        seekToPosition = Math.max(0, Math.min(seekToPosition, player.getDuration()));
        player.seekTo(seekToPosition);
        hostSeekVideo();
        showSeekPreview(seekToPosition, isForward);

        showSeekCount(isForward, SEEK_DOUBLE_TAP_MILLIS);
    }
    private void showSeekPreview(long position, boolean isForward) {
        long duration = player.getDuration();
        if (duration > 0) {
            int progress = (int) (1000 * position / duration);
            viewBinding.seekBar.setProgress(progress);
        }
        viewBinding.tvCurrentTime.setText(formatTime(position));

        if (autoHideHandler != null) {
            autoHideHandler.removeCallbacks(hideControlsRunnable);
            long delay = viewBinding.controlVideo.getVisibility() == View.VISIBLE
                    ? AUTO_HIDE_DELAY_MILLIS
                    : DOUBLE_TAP_TIMEOUT;
            autoHideHandler.postDelayed(hideControlsRunnable, delay);
        }
    }
    private void showSeekCount(boolean isForward, int seconds) {
        if (isForward) {
            forwardCount += seconds / 1000; // +10s
            previousCount = 0;
            viewBinding.layoutCountForward.setVisibility(View.VISIBLE);
            viewBinding.layoutCountPrevious.setVisibility(View.GONE);
            viewBinding.tvCountForward.setText(String.format("+ %d", forwardCount));
            animateSeekIcon(true);
            if (player.getCurrentPosition() + SEEK_DOUBLE_TAP_MILLIS >= player.getDuration()) {
                forwardCount = 0;
            }
        } else {
            previousCount += seconds / 1000; // -10s
            forwardCount = 0;
            viewBinding.layoutCountPrevious.setVisibility(View.VISIBLE);
            viewBinding.layoutCountForward.setVisibility(View.GONE);
            viewBinding.tvCountPrevious.setText(String.format("- %d", previousCount));
            animateSeekIcon(false);
            if (player.getCurrentPosition() == 0) {
                previousCount = 0;
            }
        }

        // Reset counter sau 800ms nếu không có tap mới
        countResetHandler.removeCallbacks(resetCountRunnable);
        resetCountRunnable = () -> {
            forwardCount = 0;
            previousCount = 0;
            viewBinding.layoutCountForward.setVisibility(View.GONE);
            viewBinding.layoutCountPrevious.setVisibility(View.GONE);
        };
        countResetHandler.postDelayed(resetCountRunnable, DOUBLE_TAP_TIMEOUT);
    }

    // region === Set Up Brightness and Volume SeekBar ===
    public void setupSeekBarBrightNess() {
        Window window = getWindow();

        if (!viewModel.setting.isSetupBrightness() && viewModel.setting.getBrightness() != null) {
            float brightnessValue = viewModel.setting.getBrightness() / 100f;

            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.screenBrightness = brightnessValue;
            window.setAttributes(layoutParams);
            viewBinding.seekBrightness.setProgress(viewModel.setting.getBrightness());
            updateBrightnessIcon(brightnessValue);
            viewModel.setting.setSetupBrightness(true);

        } else {
            float currentBrightness = window.getAttributes().screenBrightness;
            if (currentBrightness < 0) {
                currentBrightness = 0.5f;
            }
            viewBinding.seekBrightness.setProgress((int) (currentBrightness * 100));
        }
        viewBinding.seekBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float brightness = progress / 100f;

                    Window window = getWindow();
                    WindowManager.LayoutParams layoutParams = window.getAttributes();
                    layoutParams.screenBrightness = brightness;
                    window.setAttributes(layoutParams);

                   updateBrightnessIcon(brightness);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                autoHideHandler.removeCallbacks(hideControlsRunnable);
                viewBinding.controlVideo.setVisibility(View.GONE);
                viewBinding.layoutSeekBar.setVisibility(View.GONE);
                viewBinding.layoutVolume.setVisibility(View.GONE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                viewBinding.btnSkipOutro.setVisibility(View.GONE);
                viewBinding.btnSkipIntro.setVisibility(View.GONE);
                viewBinding.controlVideo.setVisibility(View.VISIBLE);
                viewBinding.layoutSeekBar.setVisibility(View.VISIBLE);
                viewBinding.layoutVolume.setVisibility(View.VISIBLE);
                autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
            }
        });
    }

    // Hàm phụ trợ để tái sử dụng logic đổi icon
    private void updateBrightnessIcon(float brightness) {
        if (brightness < 0.33f) {
            viewBinding.icBrightness.setImageResource(R.drawable.ic_brightness_low);
        } else if (brightness < 0.66f) {
            viewBinding.icBrightness.setImageResource(R.drawable.ic_brightness_medium);
        } else {
            viewBinding.icBrightness.setImageResource(R.drawable.ic_brightness_high);
        }
    }
    public void setupSeekBarVolume() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        int initialProgress;

        if (viewModel.setting != null && !viewModel.setting.isSetupAudio() && viewModel.setting.getAudio() != null) {
            initialProgress = viewModel.setting.getAudio();

            int systemVolume = (int) ((initialProgress / 100f) * maxVolume);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, systemVolume, 0);

            viewModel.setting.setSetupAudio(true);
        } else {
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            initialProgress = (int) ((currentVolume / (float) maxVolume) * 100);
        }

        viewBinding.seekVolume.setProgress(initialProgress);
        updateVolumeIcon(initialProgress);

        viewBinding.seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int newVolume = (int) ((progress / 100f) * maxVolume);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
                    updateVolumeIcon(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                autoHideHandler.removeCallbacks(hideControlsRunnable);
                viewBinding.controlVideo.setVisibility(View.GONE);
                viewBinding.layoutSeekBar.setVisibility(View.GONE);
                viewBinding.layoutBrightness.setVisibility(View.GONE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                viewBinding.btnSkipOutro.setVisibility(View.GONE);
                viewBinding.btnSkipIntro.setVisibility(View.GONE);
                viewBinding.controlVideo.setVisibility(View.VISIBLE);
                viewBinding.layoutSeekBar.setVisibility(View.VISIBLE);
                viewBinding.layoutBrightness.setVisibility(View.VISIBLE);
                autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
            }
        });

    }

    // region === Set Up Display Control ===
    private final Runnable hideControlsRunnable = () -> {
        viewBinding.controlVideo.setVisibility(View.GONE);
        viewBinding.layoutSeekBar.setVisibility(View.GONE);
        viewBinding.layoutBrightness.setVisibility(View.GONE);
        viewBinding.layoutVolume.setVisibility(View.GONE);
    };
    private void toggleControls() {
        boolean show = viewBinding.controlVideo.getVisibility() != View.VISIBLE;
        if (show) {
            viewBinding.btnSkipOutro.setVisibility(View.GONE);
            viewBinding.btnSkipIntro.setVisibility(View.GONE);
        }
        viewBinding.controlVideo.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!isLockScreen) viewBinding.layoutSeekBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!isLockScreen) viewBinding.layoutBrightness.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!isLockScreen) viewBinding.layoutVolume.setVisibility(show ? View.VISIBLE : View.GONE);

        if (show && autoHideHandler != null) {
            autoHideHandler.removeCallbacks(hideControlsRunnable);
            autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
        }
    }

    // region === Set Up View ===
    public void hideSystemUI() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            getWindow().getInsetsController().hide(WindowInsets.Type.systemBars());
            getWindow().getInsetsController().setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }
    }
    public void handleLockScreen() {
        if (isLockScreen) {
            isLockScreen = false;
            viewBinding.btnSkipOutro.setVisibility(View.GONE);
            viewBinding.btnSkipIntro.setVisibility(View.GONE);
            viewBinding.btnUnLockScreen.setVisibility(View.GONE);
            viewBinding.btnLockScreen.setVisibility(View.VISIBLE);
            viewBinding.buttonControlVideo.setVisibility(View.VISIBLE);
            viewBinding.layoutSeekBar.setVisibility(View.VISIBLE);
            viewBinding.layoutBrightness.setVisibility(View.VISIBLE);
            viewBinding.layoutVolume.setVisibility(View.VISIBLE);
        } else {
            isLockScreen = true;
            viewBinding.btnUnLockScreen.setVisibility(View.VISIBLE);
            viewBinding.btnLockScreen.setVisibility(View.GONE);
            viewBinding.buttonControlVideo.setVisibility(View.GONE);
            viewBinding.layoutSeekBar.setVisibility(View.GONE);
            viewBinding.layoutBrightness.setVisibility(View.GONE);
            viewBinding.layoutVolume.setVisibility(View.GONE);
        }
        autoHideHandler.removeCallbacks(hideControlsRunnable);
        autoHideHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
        applyParticipantSyncRestrictedUi(viewModel.isParticipantPlaybackRestricted());
    }

    private void showLoadingVideo() {
        if (!isBuffering) {
            isBuffering = true;
            if (isUserSeeking) {
                viewBinding.loadingProgress.setVisibility(View.GONE);
            } else {
                viewBinding.loadingProgress.setVisibility(View.VISIBLE);

            }
            viewBinding.btnPlayPause.setVisibility(View.GONE);
        }
    }
    private void hideLoadingVideo() {
        if (isBuffering) {
            isBuffering = false;
            viewBinding.loadingProgress.setVisibility(View.GONE);
            if (!viewModel.isParticipantPlaybackRestricted()) {
                viewBinding.btnPlayPause.setVisibility(View.VISIBLE);
            }
        }
    }
    private void updateVolumeIcon(int progress) {
        if (progress == 0) {
            viewBinding.icVolume.setImageResource(R.drawable.ic_volume_mute);
        } else if (progress < 50) {
            viewBinding.icVolume.setImageResource(R.drawable.ic_volume_low);
        } else {
            viewBinding.icVolume.setImageResource(R.drawable.ic_volume_high);
        }
    }
    private void animateSeekIcon(boolean isForward) {
        ImageView icon = isForward
                ? viewBinding.icForward
                : viewBinding.icPrevious;

        View layout = isForward ? viewBinding.layoutCountForward : viewBinding.layoutCountPrevious;
        layout.setVisibility(View.VISIBLE);
        icon.setVisibility(View.VISIBLE);

        icon.setAlpha(1f);
        icon.setTranslationX(0f);
        icon.setRotation(0f);

        float moveDistance = isForward ? 50f : -50f;

        ObjectAnimator moveAnim = ObjectAnimator.ofFloat(icon, "translationX", 0f, moveDistance);
        moveAnim.setDuration(400);
        moveAnim.setInterpolator(new DecelerateInterpolator());

        moveAnim.start();
    }

    // region === Set Up Icon ===
    private void updatePlayPauseIcons(boolean isPlaying) {
        viewBinding.icPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }
    private void animateRotate(View view, boolean isForward) {
        float start = 0f;
        float end = isForward ? 360f : -360f;

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", start, end);
        animator.setDuration(400);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_watch_movie;
    }
    @Override
    public int getBindingVariable() {
        return BR.vm;
    }
    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    // region === Show Setting Video ===
    private void showSettingsBottomSheet() {
        toggleControls();
        SettingBottomSheetDialog sheet = new SettingBottomSheetDialog(
                this, viewModel.settingVideoModel, getSubtitleLabelForSettingsUi(), this,
                viewModel.isParticipantPlaybackRestricted());
        sheet.show();
        Objects.requireNonNull(sheet.getWindow()).getDecorView().post(sheet::setupTransparentWindow);
        sheet.setOnDismissListener(v -> {
            hideSystemUI();
        });
    }
    private void showSettingsPlaySpeedBottomSheet(int type) {
        if (viewModel.isParticipantPlaybackRestricted()) return;
        PlaySpeedBottomSheetDialog sheet = new PlaySpeedBottomSheetDialog(this, viewModel.settingVideoModel, this, type);
        sheet.show();
        Objects.requireNonNull(sheet.getWindow()).getDecorView().post(sheet::setupTransparentWindow);
        sheet.setOnDismissListener(v -> {
            hideSystemUI();
        });
    }
    private void showSettingsQualityBottomSheet() {
        QualityBottomSheetDialog sheet = new QualityBottomSheetDialog(this, viewModel.settingVideoModel, this);
        sheet.show();
        Objects.requireNonNull(sheet.getWindow()).getDecorView().post(sheet::setupTransparentWindow);
        sheet.setOnDismissListener(v -> {
            hideSystemUI();
        });
    }
    private void showSettingsMoreOptionBottomSheet() {
        if (viewModel.isParticipantPlaybackRestricted()) return;
        MoreOptionBottomSheetDialog sheet = new MoreOptionBottomSheetDialog(this, viewModel.settingVideoModel, this);
        sheet.show();
        Objects.requireNonNull(sheet.getWindow()).getDecorView().post(sheet::setupTransparentWindow);
        sheet.setOnDismissListener(v -> {
            hideSystemUI();
        });
    }

    private void showSettingsSubtitleCustomizeBottomSheet() {
        SubtitleCustomizeBottomSheetDialog sheet =
                new SubtitleCustomizeBottomSheetDialog(this, viewModel.settingVideoModel);
        sheet.show();
        Objects.requireNonNull(sheet.getWindow()).getDecorView().post(sheet::setupTransparentWindow);
        sheet.setOnDismissListener(v -> hideSystemUI());
    }

    private void showSettingsSubtitleBottomSheet() {
        if (viewModel.subtitleApiSettledForCurrentVideo && !viewModel.hasSubtitleTracksForCurrentVideo) {
            Toast.makeText(this, R.string.subtitle_not_available_for_video, Toast.LENGTH_SHORT).show();
            return;
        }
        List<SubtitleResponse> subtitles = viewModel.getSubtitleList().getValue();
        if (subtitles == null) subtitles = new ArrayList<>();
        SubtitleBottomSheetDialog sheet = new SubtitleBottomSheetDialog(
                this, subtitles, viewModel.currentSubtitle,
                subtitle -> {
                    viewModel.currentSubtitle = subtitle;
                    applySubtitle(subtitle);
                });
        sheet.show();
        Objects.requireNonNull(sheet.getWindow()).getDecorView().post(sheet::setupTransparentWindow);
        sheet.setOnDismissListener(v -> hideSystemUI());
    }

    private void applySubtitle(SubtitleResponse subtitle) {
        if (player == null || viewModel.nowUriPlay == null) return;
        long currentPosition = player.getCurrentPosition();
        // isPlaying() is false while buffering; playWhenReady reflects user/autoplay intent.
        boolean shouldPlay = player.getPlayWhenReady();

        MediaItem.Builder builder = new MediaItem.Builder().setUri(viewModel.nowUriPlay);
        if (subtitle != null) {
            MediaItem.SubtitleConfiguration subtitleConfig =
                    new MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitle.getSubtitleUrl()))
                            .setMimeType(MimeTypes.TEXT_VTT)
                            .setLanguage(subtitle.getLanguage())
                            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                            .build();
            builder.setSubtitleConfigurations(ImmutableList.of(subtitleConfig));
        }

        // Pass start position directly so the player buffers from the right point,
        // avoiding the A/V drift that occurs when seekTo is called after prepare().
        player.setMediaItem(builder.build(), currentPosition);
        player.prepare();
        player.setPlayWhenReady(shouldPlay);

        if (subtitle == null) {
            clearSubtitleOverlay();
        }
    }

    // region === Show Runnable ===
    private void startSeekBarUpdate() {
        if (updateSeekBarRunnable != null) {
            seekHandler.post(updateSeekBarRunnable);
        }
    }
    private void stopSeekBarUpdate() {
        if (seekHandler != null && updateSeekBarRunnable != null) {
            seekHandler.removeCallbacks(updateSeekBarRunnable);
        }
    }
    private void startVolumeObserver() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        volumeObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int progress = (int) ((currentVolume / (float) maxVolume) * 100);
                viewBinding.seekVolume.setProgress(progress);
                updateVolumeIcon(progress);
            }
        };

        getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI,
                true,
                volumeObserver
        );
    }
    private void stopVolumeObserver() {
        if (volumeObserver != null) {
            getContentResolver().unregisterContentObserver(volumeObserver);
            volumeObserver = null;
        }
    }
    private ContentObserver volumeObserver;

    // region === Life Cycle===
    @Override
    protected void onResume() {
        super.onResume();
        applyScreenCaptureProtection();
        hideSystemUI();
        startSeekBarUpdate();
        startVolumeObserver();
        startTrackingLoop();
        viewModel.startTokenAutoRefresh();

    }
    @Override
    protected void onPause() {
        super.onPause();
        stopSeekBarUpdate();

        if (player != null) {
            updateVideoTracking();
            player.pause();
        }
        stopVolumeObserver();

        viewModel.stopTokenAutoRefresh();
    }
    @Override
    protected void onDestroy() {
        if (imeLayoutListener != null) {
            View root = viewBinding != null ? viewBinding.getRoot() : null;
            if (root != null) {
                ViewTreeObserver vto = root.getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.removeOnGlobalLayoutListener(imeLayoutListener);
                }
            }
            imeLayoutListener = null;
        }
        super.onDestroy();

        if (player != null) {
            updateVideoTracking();
            player.release();
            player = null;
        }

        // XÓA TẤT CẢ RUNNABLE TRƯỚC KHI HỦY
        if (seekHandler != null) {
            seekHandler.removeCallbacksAndMessages(null);
        }
        if (autoHideHandler != null) {
            autoHideHandler.removeCallbacksAndMessages(null);
        }
        if (countResetHandler != null) {
            countResetHandler.removeCallbacksAndMessages(null);
        }

        if (nextEpisodeHandler != null) {
            nextEpisodeHandler.removeCallbacksAndMessages(null);
        }

        stopSeekBarUpdate();
        stopVolumeObserver();
        stopTrackingLoop();
        viewModel.stopTokenAutoRefresh();
        if (viewModel.isLiveRoom && viewModel.isHost) {
            leaveRoom();
            if (viewModel.isHost) {
                stopSchedule();
            }
        }
        ((MVVMApplication) application).destroyMqtt();
        handlerRetryGetListSubtitle.removeCallbacksAndMessages(null);
        Constants.TOKEN_GUEST = "";
    }

    // region === Click ===

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_close:
                player.pause();
                toggleControls();
                viewBinding.lDialogExit.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_cancel:
            case R.id.l_dialog_exit:
                viewBinding.lDialogExit.setVisibility(View.GONE);
                updatePlayPauseState(Boolean.TRUE.equals(viewModel.getIsPlaying().getValue()), true);
                break;
            case R.id.btn_ok:
                finish();
                break;
            case R.id.btn_lock_screen:
            case R.id.btn_un_lock_screen:
                handleLockScreen();
                break;
            case R.id.btn_play_pause:
                updatePlayPauseState(!player.isPlaying());
                break;
            case R.id.btn_replay:
                viewBinding.btnPlayPause.setVisibility(View.VISIBLE);
                viewBinding.btnReplay.setVisibility(View.GONE);
                player.seekTo(0);
                hostSeekVideo();
                updatePlayPauseState(true);
                break;
            case R.id.btn_forward:
                animateRotate(viewBinding.icForwardLooping, true);
                handleForwardAndPreviousVideo(true);
                break;

            case R.id.btn_previous:
                animateRotate(viewBinding.icPreviousLooping, false);
                handleForwardAndPreviousVideo(false);
                break;

            case R.id.btn_setting:
                showSettingsBottomSheet();
                break;

            case R.id.btn_next_episode:
//                https://files.vidstack.io/sprite-fight/hls/stream.m3u8
                onNextEpisodeClick();
                break;

            case R.id.btn_episodes:
                showListEpisode();
                break;

            case R.id.btn_close_episodes:
                viewBinding.layoutListEpisodes.listEpisode.setVisibility(View.GONE);
                toggleControls();
                player.play();
                break;
            case R.id.btn_skip_intro:
                handleSkipIntro();
                break;

            case R.id.btn_skip_outro:
                handleSkipOutro();
                break;

            case R.id.btn_close_next_episode:
                viewBinding.layoutNextEpisode.layoutNextEpisode.setVisibility(View.GONE);
                stopNextEpisodeCountdown();
                break;
            case R.id.btn_layout_next_episode:
                viewBinding.layoutNextEpisode.layoutNextEpisode.setVisibility(View.GONE);
                stopNextEpisodeCountdown();
                onNextEpisodeClick();
                break;
            case R.id.btn_open_chat:
                toggleChatAnimated();
                break;
            case R.id.btn_close_chat:
                closeChatAnimated();
                break;
            case R.id.btn_create_comment:
                sendRoomChatMessage();
                break;
            case R.id.btn_room_info:
                showRoomInfoDialog();
                break;
            default:
                break;
        }
    }
    private void handleSkipIntro() {
        if (viewModel.isParticipantPlaybackRestricted()) return;
        if (viewModel.nowVideoPlay != null && viewModel.nowVideoPlay.getIntroEnd() != null) {
            long introEndMs = viewModel.nowVideoPlay.getIntroEnd() * 1000L;
            player.seekTo(introEndMs);
            hostSeekVideo();
            viewBinding.btnSkipIntro.setVisibility(View.GONE);
        }
    }

    private void handleSkipOutro() {
        if (viewModel.nowVideoPlay == null || viewModel.isParticipantPlaybackRestricted()) return;

        if (isSeries && !isLastEpisode()) {
            viewBinding.btnSkipOutro.setVisibility(View.GONE);
            onNextEpisodeClick();
        }
    }


    @Override
    public void onQualityClicked() {
        showSettingsQualityBottomSheet();
    }
    @Override
    public void onPlaybackSpeedPressClicked() {
        showSettingsPlaySpeedBottomSheet(PlaySpeedBottomSheetDialog.TYPE_SPEED_PRESS);
    }

    @Override
    public void onSubtitleCustomizeClicked() {
        showSettingsSubtitleCustomizeBottomSheet();
    }
    @Override
    public void onPlaybackSpeedClicked() {
        showSettingsPlaySpeedBottomSheet(PlaySpeedBottomSheetDialog.TYPE_SPEED);
    }
    @Override
    public void onSubtitleClicked() {
        showSettingsSubtitleBottomSheet();
    }
    @Override
    public void onLockScreenClicked() {
        handleLockScreen();
    }
    @Override
    public void onMoreOptionsClicked() {
        showSettingsMoreOptionBottomSheet();
    }

    // region === Handle Setting ===
    @Override
    public void updatePlaySpeedVideo(float speed, int typeSpeedOption) {
        if (viewModel.isParticipantPlaybackRestricted()) return;
        if (typeSpeedOption == PlaySpeedBottomSheetDialog.TYPE_SPEED) {
            viewModel.settingVideoModel.setPlaybackSpeed(speed); // observer will update player + MQTT
        } else {
            viewModel.settingVideoModel.getPlaySpeedWhenPress().setSpeed(speed);
        }
    }
    @Override
    public void updateQualityVideo(SettingVideoModel settingVideoModel) {
        viewModel.settingVideoModel = settingVideoModel;
        applyQualityFromSetting();
    }
    private void applyQualityFromSetting() {
        if (player == null) return;

        SettingVideoModel.Quality qualitySetting =  viewModel.settingVideoModel.getQuality();
        TrackSelectionParameters.Builder builder = player.getTrackSelectionParameters().buildUpon();

        if (qualitySetting.isAuto()) {
            builder.clearOverridesOfType(C.TRACK_TYPE_VIDEO);
        } else {
            VideoQuality selected = qualitySetting.getResolution();
            if (selected == null) return;

            Tracks currentTracks = player.getCurrentTracks();

            for (Tracks.Group group : currentTracks.getGroups()) {
                if (group.getType() == C.TRACK_TYPE_VIDEO &&
                        group.getMediaTrackGroup().id.equals(selected.groupIndex)) {

                    TrackSelectionOverride override = new TrackSelectionOverride(
                            group.getMediaTrackGroup(),
                            ImmutableList.of(selected.trackIndex)
                    );

                    builder.clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                            .addOverride(override);
                    break;
                }
            }
        }
        player.setTrackSelectionParameters(builder.build());
    }

    //region === Change Video ===
    private void changeEpisode(String newUri) {
        if (player == null) return;

        viewModel.nowUriPlay = newUri;

        toggleControls();
        viewModel.updateSettingWhenChangeEpisode();
        showLoadingVideo();
        reset();
        clearSubtitleOverlay();
        loadSubtitlesForCurrentVideo();

        TrackSelectionParameters.Builder builder = player.getTrackSelectionParameters().buildUpon();
        builder.clearOverridesOfType(C.TRACK_TYPE_VIDEO);
        player.setTrackSelectionParameters(builder.build());

        MediaItem newItem = MediaItem.fromUri(newUri);
        isStartContinueWatch = false;
        player.stop();
        player.clearMediaItems();
        player.setMediaItem(newItem);
        player.prepare();
        player.setPlayWhenReady(true);
    }
    public Boolean isLastEpisode() {
        if (viewModel.movieDetails == null || viewModel.nowEpisodePlay == null) {
            return true;
        }

        if (viewModel.movieDetails.getType() != Constants.TYPE_MOVIE_SERIES) {
            return true; // Phim lẻ
        }

        List<SeasonResponse> seasons = viewModel.movieDetails.getSeasons();
        if (seasons == null || seasons.isEmpty()) {
            return true;
        }

        // Tìm season hiện tại
        SeasonResponse currentSeason = findCurrentSeason();
        if (currentSeason == null || currentSeason.getEpisodes() == null || currentSeason.getEpisodes().isEmpty()) {
            return true;
        }

        // Lấy tập cuối của mùa hiện tại
        MovieItemResponse lastInCurrentSeason = currentSeason.getEpisodes()
                .get(currentSeason.getEpisodes().size() - 1);

        // Nếu tập hiện tại là tập cuối của mùa hiện tại → kiểm tra có mùa sau không
        if (viewModel.nowEpisodePlay.getId().equals(lastInCurrentSeason.getId())) {
            int currentSeasonIndex = seasons.indexOf(currentSeason);
            return currentSeasonIndex == seasons.size() - 1; // Không còn mùa sau
        }

        return false; // Còn tập trong mùa hiện tại
    }

    private SeasonResponse findCurrentSeason() {
        for (SeasonResponse season : viewModel.movieDetails.getSeasons()) {
            if (season.getEpisodes() != null) {
                for (MovieItemResponse episode : season.getEpisodes()) {
                    if (episode.getId().equals(viewModel.nowEpisodePlay.getId())) {
                        return season;
                    }
                }
            }
        }
        return null;
    }

    private String findLabelSeason() {
        SeasonResponse currentSeason = findCurrentSeason();
        if (currentSeason == null) {
            return "";
        }

        List<SeasonResponse> seasons = viewModel.movieDetails.getSeasons();
        if (seasons == null) return "";

        int seasonIndex = seasons.indexOf(currentSeason);
        if (seasonIndex == -1) return "";

        return " (" + getString(R.string.season) + " " + (seasonIndex + 1) + ")";
    }

    public MovieItemResponse getNextEpisode() {
        if (viewModel.movieDetails == null || viewModel.nowEpisodePlay == null) {
            return null;
        }

        List<SeasonResponse> seasons = viewModel.movieDetails.getSeasons();
        if (seasons == null || seasons.isEmpty()) {
            return null;
        }

        SeasonResponse currentSeason = findCurrentSeason();
        if (currentSeason == null || currentSeason.getEpisodes() == null || currentSeason.getEpisodes().isEmpty()) {
            return null;
        }

        List<MovieItemResponse> episodes = currentSeason.getEpisodes();
        int currentIndex = -1;

        // Tìm vị trí tập hiện tại
        for (int i = 0; i < episodes.size(); i++) {
            if (episodes.get(i).getId().equals(viewModel.nowEpisodePlay.getId())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1) return null;

        // 1. Còn tập trong mùa hiện tại → trả về tập kế
        if (currentIndex < episodes.size() - 1) {
            return episodes.get(currentIndex + 1);
        }

        // 2. Là tập cuối mùa → tìm mùa tiếp theo
        int currentSeasonIndex = seasons.indexOf(currentSeason);
        if (currentSeasonIndex == -1 || currentSeasonIndex == seasons.size() - 1) {
            return null; // Không có mùa sau
        }

        SeasonResponse nextSeason = seasons.get(currentSeasonIndex + 1);
        if (nextSeason.getEpisodes() == null || nextSeason.getEpisodes().isEmpty()) {
            return null;
        }

        return nextSeason.getEpisodes().get(0); // Tập đầu của mùa sau
    }

    public void onNextEpisodeClick() {
        updateVideoTracking();
        viewModel.nowEpisodePlay = getNextEpisode();
        viewModel.nowVideoPlay = viewModel.nowEpisodePlay.getVideo();
        setUpViewForSeriesMovie();
        changeEpisode(getVideoUri(viewModel.nowVideoPlay));
    }

    public void setUpAdapterEpisode() {
        seasonItemAdapter = new SeasonItemAdapter(this, this);
        viewBinding.layoutListEpisodes.rvSeason.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        viewBinding.layoutListEpisodes.rvSeason.setAdapter(seasonItemAdapter);

        episodeItemListHoriAdapter = new EpisodeItemListHoriAdapter(this, this);
        viewBinding.layoutListEpisodes.rvEpisode.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        viewBinding.layoutListEpisodes.rvEpisode.setAdapter(episodeItemListHoriAdapter);
    }
    @SuppressLint("NotifyDataSetChanged")
    public void showListEpisode() {
        toggleControls();
        player.pause();
        viewModel.movieDetails.setSeasonAndEpisodeSelectedAndPlaying(viewModel.nowEpisodePlay.getId());
        seasonItemAdapter.setData(viewModel.movieDetails.getSeasons());
        episodeItemListHoriAdapter.setData(viewModel.movieDetails.getSelectedSeasonEpisodes(), viewBinding.layoutListEpisodes.rvEpisode);
        currentSeasonIndex = viewModel.movieDetails.getIndexSeasonSelect();
        viewBinding.layoutListEpisodes.listEpisode.setVisibility(View.VISIBLE);
    }
    @Override
    public void onEpisodeClick(MovieItemResponse season) {
        updateVideoTracking();
        viewBinding.layoutListEpisodes.listEpisode.setVisibility(View.GONE);
        viewModel.nowEpisodePlay = season;
        viewModel.nowVideoPlay = viewModel.nowEpisodePlay.getVideo();
        setUpViewForSeriesMovie();
        changeEpisode(getVideoUri(viewModel.nowVideoPlay));
        toggleControls();
    }

    public void setUpNextEpisodeLayout() {
        if (getNextEpisode() == null) return;

        String img = "";

        if (getNextEpisode().getThumbnailUrl() != null) {
            img = getNextEpisode().getThumbnailUrl();
        } else {
            img = getNextEpisode().getVideo().getThumbnailUrl();
        }

        Glide.with(this)
                .load(img)
                .placeholder(R.drawable.place_holder_16_9)
                .error(R.drawable.place_holder_16_9)
                .into(viewBinding.layoutNextEpisode.image);
    }

    @Override
    public void onSeasonClick(SeasonResponse season) {
        viewModel.movieDetails.setSeasonSelect(season.getId());
        episodeItemListHoriAdapter.setData(viewModel.movieDetails.getSeasonEpisodesById(season.getId()), viewBinding.layoutListEpisodes.rvEpisode);

        currentSeasonIndex = viewModel.movieDetails.getIndexSeasonSelect();
    }

    public void updateVideoTracking() {
        if (!viewModel.isLogin()) return;
        TrackingWatchHistoryRequest request = new TrackingWatchHistoryRequest();

        request.setLastWatchSeconds(player.getCurrentPosition() /1000);

        if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SINGLE) {
            request.setMovieItemId(viewModel.movieDetails.getSeasons().get(0).getId());
        } else if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES) {
            request.setMovieItemId(viewModel.nowEpisodePlay.getId());
        }

        viewModel.updateTrackingMovie(request);
    }

    private void startTrackingLoop() {
        if (trackingRunnable == null) {
            trackingRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isStartContinueWatch) {
                        updateVideoTracking();
                    }
                    trackingHandler.postDelayed(this, TRACKING_INTERVAL_MS);
                }
            };
        }

        trackingHandler.postDelayed(trackingRunnable, TRACKING_INTERVAL_MS);
    }

    private void stopTrackingLoop() {
        if (trackingHandler != null && trackingRunnable != null) {
            trackingHandler.removeCallbacks(trackingRunnable);
        }
    }

    @Override
    public void onMessageReceived(Message message) {
        super.onMessageReceived(message);
        Log.d("MQTT_LOG", Objects.requireNonNull(GsonUtils.toJson(message)));
        if (!viewModel.isLiveRoom) return;
        switch (message.getCmd()) {
            case Command.CMD_ROOM_SYNC:
                // Participant -> Host: request sync snapshot
                if (viewModel.isHost) {
                    hostBroadcastSyncData(message);
                }
                break;
            case Command.CMD_PARTICIPANT_JOIN:
                participantJoin(message);
                break;
            case Command.CMD_ROOM_STATE:
                if (shouldApplyHostPlaybackCommands()) {
                    roomOption(message);
                }
                break;
            case Command.CMD_END_ROOM:
                endRoom(message);
                break;
            case Command.CMD_UPDATE_PARTICIPANT_COUNT:
                handleParticipantCountUpdate(message);
                break;
            case Command.CMD_CREATE_CHAT:
                handleIncomingRoomChatMessage(message);
                break;
            default:
                break;
        }
    }

    private void handleIncomingRoomChatMessage(Message message) {
        if (message.getData() == null) return;
        try {
            String json = GsonUtils.toJson(message.getData());
            CreateChatModel chat = GsonUtils.fromJson(json, CreateChatModel.class);
            if (chat != null && chat.getContent() != null && !chat.getContent().isEmpty()) {
                runOnUiThread(() -> viewModel.appendIncomingChatMessage(chat));
            }
        } catch (Exception e) {
            Timber.e(e, "MQTT chat parse");
        }
    }

    private void handleParticipantCountUpdate(Message message) {
        if (!viewModel.isLiveRoom || message.getData() == null) return;
        try {
            UpdateParticipantCountModel payload = GsonUtils.fromJson(
                    GsonUtils.toJson(message.getData()),
                    UpdateParticipantCountModel.class);
            if (payload == null || payload.getRoomId() == null || payload.getCurrentViewers() == null) {
                return;
            }
            runOnUiThread(() -> viewModel.applyServerViewerCount(
                    payload.getRoomId(),
                    payload.getCurrentViewers()));
        } catch (Exception e) {
            Timber.e(e, "MQTT participant count parse");
        }
    }

    private void sendRoomChatMessage() {
        if (!viewModel.isLiveRoom || viewModel.roomDetail == null) return;
        String text = viewBinding.edtComment.getText() != null
                ? viewBinding.edtComment.getText().toString().trim()
                : "";
        if (text.isEmpty()) return;
        CreateChatModel payload = viewModel.buildOutgoingChatMessage(text, viewModel.userResponse);
        String topic = viewModel.getRoomMqttTopicForChat();
        if (topic == null) return;
        sendMqttMessage(viewModel.buildRoomChatMqttMessage(payload), topic);
        viewBinding.edtComment.setText("");
    }

    private void showRoomInfoDialog() {
        if (!viewModel.isLiveRoom || viewModel.roomDetail == null) return;
        String roomName = viewModel.roomDetail.getName();
        String roomCode = viewModel.roomDetail.getCode();
        RoomDialogUtils.showRoomInfoDialog(this, roomName, roomCode);
    }

    private void hostBroadcastSyncData(Message message) {
        if (!viewModel.isLiveRoom || !viewModel.isHost) return;
        if (player == null || viewModel.roomDetail == null) return;
        ParticipantJoinModel participantJoinModel = GsonUtils.fromJson(GsonUtils.toJson(message.getData()), ParticipantJoinModel.class);

        if (participantJoinModel != null && participantJoinModel.getId() != null) {
            String topic = Constants.TOPIC + viewModel.roomDetail.getId() + "/" + participantJoinModel.getId();

            Message msg = new Message();
            msg.setCmd(Command.CMD_ROOM_STATE);

            RoomStateModel data = new RoomStateModel();
            data.setSubCmd(Command.CMD_ROOM_ALL_STATE);
            data.setPlay(player.isPlaying());
            data.setCurrentPositionMovie(player.getCurrentPosition());
            data.setPlaySpeed(player.getPlaybackParameters().speed);
            msg.setData(data);
            sendMqttMessage(msg, topic);
        }
    }

    private void sendMqttMessage(Message message, String topic) {
        Timber.w("MQTT_LOG: MESSAGE SEND! Topic: %s | Payload: %s", topic, GsonUtils.toJson(message));
        ((MVVMApplication) application).sendMessageMqtt(message, topic);
    }
    public void participantJoin(Message message) {
        if (!viewModel.isHost) return;
        String json =  GsonUtils.toJson(message.getData());
        if (json != null) {
            ParticipantJoinModel participantJoinModel = GsonUtils.fromJson(json, ParticipantJoinModel.class);
            if (participantJoinModel != null) {
                String topic = Constants.TOPIC + viewModel.roomDetail.getId() + "/" + participantJoinModel.getId();
                Message msg = new Message();

                msg.setCmd(Command.CMD_ROOM_STATE);
                if (player == null) return;
                RoomStateModel roomStateModel = new RoomStateModel();
                roomStateModel.setSubCmd(Command.CMD_ROOM_ALL_STATE);

                roomStateModel.setPlay(player.isPlaying());
                roomStateModel.setCurrentPositionMovie(player.getCurrentPosition());
                roomStateModel.setPlaySpeed(player.getPlaybackParameters().speed);

                msg.setData(roomStateModel);
                sendMqttMessage(msg, topic);
            }
        }
    }

    public void hostSeekVideo() {
        if (!viewModel.isLiveRoom || !viewModel.isHost) return;
        String topic = Constants.TOPIC + viewModel.roomDetail.getId();
        Message msg = new Message();

        msg.setCmd(Command.CMD_ROOM_STATE);
        if (player == null) return;
        RoomStateModel roomStateModel = new RoomStateModel();
        roomStateModel.setSubCmd(Command.CMD_ROOM_SEEK);
        roomStateModel.setCurrentPositionMovie(player.getCurrentPosition());
        msg.setData(roomStateModel);
        sendMqttMessage(msg, topic);
    }

    public void roomOption(Message message) {
        if (viewModel.isHost) return;
        if (!viewModel.isSyncWithHostEnabled()) return;
        String json =  GsonUtils.toJson(message.getData());
        if (json != null) {
            RoomStateModel roomStateModel = GsonUtils.fromJson(json, RoomStateModel.class);
            if (player != null && roomStateModel != null) {

                switch (roomStateModel.getSubCmd()) {
                    case Command.CMD_ROOM_ALL_STATE:
                        syncRoomWithHost(roomStateModel);
                        break;
                    case Command.CMD_ROOM_PAUSE:
                    case Command.CMD_ROOM_PLAY:
                        updatePlayPauseState(roomStateModel.isPlay(), true);
                        break;
                    case Command.CMD_ROOM_SEEK:
                        seekRoom(roomStateModel);
                        break;
                    case Command.CMD_ROOM_PLAY_SPEED:
                        suppressMqttPublishFromModel = true;
                        viewModel.settingVideoModel.setPlaybackSpeed((float) roomStateModel.getPlaySpeed());
                        suppressMqttPublishFromModel = false;
                        break;
                }
            }
        }
    }

    public void syncRoomWithHost(RoomStateModel roomStateModel) {
        player.seekTo(roomStateModel.getCurrentPositionMovie());
        updatePlayPauseState(roomStateModel.isPlay(), true);
        applyPlaybackSpeed((float) roomStateModel.getPlaySpeed(), false);
    }

    public void seekRoom(RoomStateModel roomStateModel) {
        player.seekTo(roomStateModel.getCurrentPositionMovie());
    }

    public void endRoom(Message message) {
        EndRoomModel endRoomModel = GsonUtils.fromJson(GsonUtils.toJson(message.getData()), EndRoomModel.class);

        if (endRoomModel == null || endRoomModel.getReason() == null) return;

        switch (endRoomModel.getReason()) {
            case Constants.ROOM_REASON_TIMEOUT:
                if (viewModel.isHost) {
                    setCountdownHostEndRoom();
                } else {
                    handleEndRoomClient(getString(R.string.msg_room_timeout_ask));
                }
                break;
            case Constants.ROOM_REASON_END:
                handleEndRoomClient(getString(R.string.msg_room_ended_ask));

                break;
            case Constants.ROOM_REASON_HOST_LEFT:
                if (viewModel.isHost) break;
                handleEndRoomClient(getString(R.string.msg_host_left_ask));
                break;
            default:
                break;
        }
    }
    private CountDownTimer countDownTimer;
    public void setCountdownHostEndRoom() {
        Dialog dialog = DialogUtils.dialogConfirmSingleButton(
                this,
                getString(R.string.msg_room_timeout_countdown, "5s"),
                getString(R.string.action_exit),
                (dialogInterface, i) -> {
                    if (countDownTimer != null) countDownTimer.cancel();
                    this.finish();
                }
        );

        TextView messageView = dialog.findViewById(R.id.dialog_message);

        countDownTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (messageView != null) {
                    messageView.setText(getString(R.string.msg_room_timeout_countdown, (millisUntilFinished / 1000) + "s"));
                }
            }

            @Override
            public void onFinish() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                    finish();
                }
            }
        }.start();
    }

    public void handleEndRoomClient(String msg) {
        DialogUtils.dialogConfirm(
                this,
                msg,
                getString(R.string.action_continue),
                (dialog, which) -> transitionToSoloPlaybackAfterRoomEnded(),
                getString(R.string.action_exit),
                (dialog, which) -> {
                    finish();
                }
        );
    }

    /**
     * Room ended (timeout / ended / host left) and user tapped Continue: disconnect MQTT so late
     * room messages are ignored, clear session flags, hide sync UI, restore full controls.
     */
    private void transitionToSoloPlaybackAfterRoomEnded() {
        leaveRoom();
        ((MVVMApplication) application).destroyMqtt();
        viewModel.clearLiveRoomSessionForSoloPlayback();
        viewBinding.layoutSync.setVisibility(View.GONE);
        viewModel.setChatOpen(false);
        viewBinding.layoutChat.setVisibility(View.GONE);
        viewBinding.btnOpenChat.setVisibility(View.GONE);
        viewBinding.btnRoomInfo.setVisibility(View.GONE);
        applyParticipantSyncRestrictedUi(false);
        viewBinding.executePendingBindings();
        restorePlaybackChromeAfterLeavingLiveRoom();
    }

    /** Play/Pause vs Replay visibility after exiting watch-party restrictions (normal rules). */
    private void restorePlaybackChromeAfterLeavingLiveRoom() {
        if (player == null) return;
        int state = player.getPlaybackState();
        if (state == Player.STATE_ENDED) {
            viewBinding.btnPlayPause.setVisibility(View.GONE);
            viewBinding.btnReplay.setVisibility(View.VISIBLE);
        } else {
            viewBinding.btnReplay.setVisibility(View.GONE);
            if (!isBuffering) {
                viewBinding.btnPlayPause.setVisibility(View.VISIBLE);
            }
        }
    }

    public void leaveRoom() {
        if (!viewModel.isLiveRoom || viewModel.roomDetail == null) return;
        Message message = new Message();
        message.setCmd(Command.CMD_PARTICIPANT_LEFT);
        ClientPingModel clientPingModel = new ClientPingModel();
        clientPingModel.setAccountId(viewModel.getUserId().toString());

        message.setData(clientPingModel);
        String topic = Constants.TOPIC + viewModel.roomDetail.getId();
        sendMqttMessage(message, topic);
    }
}

