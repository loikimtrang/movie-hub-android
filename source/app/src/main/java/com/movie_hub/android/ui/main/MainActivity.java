package com.movie_hub.android.ui.main;

import static com.movie_hub.android.ui.main.home.HomeFragment.NavigateToMovieDetails;
import static com.movie_hub.android.ui.main.home.HomeFragment.NavigateToWatchMovie;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.movie_hub.android.BR;
import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.request.favourite.CreateFavouriteRequest;
import com.movie_hub.android.data.model.api.request.movie.MovieRequest;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.data.model.api.response.collection.CollectionResponse;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.history.WatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.room.RoomResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.onesignal.MessageCommentResponse;
import com.movie_hub.android.data.model.onesignal.MessageOneSignal;
import com.movie_hub.android.data.model.onesignal.MessageRoomNotificationResponse;
import com.movie_hub.android.data.model.onesignal.MessageReviewResponse;
import com.movie_hub.android.data.model.onesignal.OneSignalCommand;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityMainBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.account.AccountFragment;
import com.movie_hub.android.ui.main.account.UnLoginAccountFragment;
import com.movie_hub.android.ui.main.account.contact.ContactActivity;
import com.movie_hub.android.ui.main.account.favourite.FavouriteActivity;
import com.movie_hub.android.ui.main.account.history.HistoryActivity;
import com.movie_hub.android.ui.main.account.language.LanguageActivity;
import com.movie_hub.android.ui.main.account.login.LoginActivity;
import com.movie_hub.android.ui.main.account.playlist.PlayListActivity;
import com.movie_hub.android.ui.main.account.privacy.PrivacyActivity;
import com.movie_hub.android.ui.main.account.setting.SettingActivity;
import com.movie_hub.android.ui.main.account.updateapp.CheckUpdateActivity;
import com.movie_hub.android.ui.main.home.HomeFragment;
import com.movie_hub.android.ui.main.home.OnMovieClickCallback;
import com.movie_hub.android.ui.main.home.detail.HomeSideBarDetailActivity;
import com.movie_hub.android.ui.main.home.dialog.MovieDetailDialogFragment;
import com.movie_hub.android.ui.main.home.filter.FilterActivity;
import com.movie_hub.android.ui.main.home.filter.model.FilterTypeModel;
import com.movie_hub.android.ui.main.home.notification.NotificationActivity;
import com.movie_hub.android.ui.main.home.topic.HomeMoreTopicActivity;
import com.movie_hub.android.ui.main.home.topic.topic_detail.HomeTopicDetailActivity;
import com.movie_hub.android.ui.main.live.LiveFragment;
import com.movie_hub.android.ui.main.live.RoomClickCoordinator;
import com.movie_hub.android.ui.main.live.RoomClickHost;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.ui.main.movie.watch.WatchMovieActivity;
import com.movie_hub.android.ui.main.schedule.ScheduleFragment;
import com.movie_hub.android.ui.main.search.SearchFragment;
import com.movie_hub.android.ui.main.splash.SplashActivity;
import com.movie_hub.android.utils.DialogUtils;
import com.movie_hub.android.utils.GsonUtils;

import java.util.List;
import java.util.Objects;

import timber.log.Timber;


public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel> implements SystemBarColorProvider, View.OnClickListener, OnMovieClickCallback, RoomClickHost {
    private Fragment active;
    private FragmentManager fm;
    private HomeFragment homeFragment;
    private SearchFragment searchFragment;
    private LiveFragment liveFragment;
    private ScheduleFragment scheduleFragment;
    private AccountFragment accountFragment;
    private UnLoginAccountFragment unLoginAccountFragment;
    private RoomClickCoordinator roomClickCoordinator;

    public MainViewModel getViewModel() {
        return viewModel;
    }

    public void handleRoomClick(RoomResponse room) {
        if (roomClickCoordinator == null) {
            roomClickCoordinator = new RoomClickCoordinator(this);
        }
        roomClickCoordinator.handleRoomClick(room);
    }

    @Override
    public android.app.Activity getHostActivity() {
        return this;
    }

    @Override
    public boolean isUserLoggedIn() {
        return viewModel.isLogin();
    }

    @Override
    public long getUserId() {
        return viewModel.getUserId();
    }

    @Override
    public void showHostLoading() {
        showLoading();
    }

    @Override
    public void hideHostLoading() {
        hideLoading();
    }

    @Override
    public void showHostError(String message) {
        showError(message);
    }

    @Override
    public void fetchMovie(Long id, MainCallback<MovieResponse> callback) {
        viewModel.getMovie(callback, id);
    }

    @Override
    public void fetchListMovieTracking(Long movieId, MainCallback<ListWatchHistoryResponse> callback) {
        viewModel.getListMovieTracking(callback, movieId);
    }

    @Override
    public void fetchJoinRoom(Long roomId, MainCallback<RoomResponse> callback) {
        viewModel.joinRoom(callback, roomId);
    }

    @Override
    public void fetchStartRoom(Long roomId, MainCallback<RoomResponse> callback) {
        viewModel.startRoom(callback, roomId);
    }

    @Override
    public void createMqttAndWatch(MovieResponse movie, RoomResponse room, boolean isHost) {
        createMqtt(movie, room, isHost);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        setUpFragment();

//        if (viewModel.isLogin()) {
//            getUserProfile();
//        }
        String json = getIntent().getStringExtra("msg_onesignal_data");
        if (json != null && !json.isEmpty()) {
            viewModel.messageOneSignal = GsonUtils.fromJson(json, MessageOneSignal.class);

            if (viewModel.messageOneSignal != null
                    && OneSignalCommand.CMD_ROOM_INVITE.equals(viewModel.messageOneSignal.getCmd())) {
                handleRoomInviteNotification(viewModel.messageOneSignal);
            } else if (viewModel.isLogin()
                    && viewModel.messageOneSignal != null
                    && viewModel.messageOneSignal.getCmd() != null) {
                String dataJson = viewModel.messageOneSignal.getData();
                String cmd = viewModel.messageOneSignal.getCmd();
                String movieId = null;

                switch (cmd) {
                    case OneSignalCommand.CMD_REPLY_COMMENT:
                    case OneSignalCommand.CMD_TOXIC_COMMENT_LOCKED:
                    case OneSignalCommand.CMD_VOTE_COMMENT:
                        if (dataJson != null && !dataJson.isEmpty()) {
                            MessageCommentResponse messageCommentResponse = GsonUtils.fromJson(dataJson, MessageCommentResponse.class);
                            if (messageCommentResponse != null) {
                                movieId = messageCommentResponse.getMovieId();
                            }
                        }
                        if (movieId != null) {
                            viewModel.msgCommentData = GsonUtils.toJson(viewModel.messageOneSignal);
                        }
                        break;
                    case OneSignalCommand.CMD_TOXIC_REVIEW_LOCKED:
                    case OneSignalCommand.CMD_VOTE_REVIEW:
                        if (dataJson != null && !dataJson.isEmpty()) {
                            MessageReviewResponse messageReviewResponse = GsonUtils.fromJson(dataJson, MessageReviewResponse.class);
                            if (messageReviewResponse != null) {
                                movieId = messageReviewResponse.getMovieId();
                            }
                        }
                        if (movieId != null) {
                            viewModel.msgCommentData = GsonUtils.toJson(viewModel.messageOneSignal);
                        }
                        break;
                    default:
                        break;
                }

                if (movieId != null) {
                    MovieResponse movieResponse = new MovieResponse();
                    movieResponse.setId(Long.valueOf(movieId));
                    getMovieDetail(movieResponse, NavigateToMovieDetails);
                } else if (Objects.equals(cmd, OneSignalCommand.CMD_REPLY_COMMENT)) {
                    Timber.e("ONESIGNAL_LOG: messageCommentResponse hoặc MovieId bị null sau khi parse");
                }
            }
        }

        loginLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        boolean loginSuccess = data != null && data.getBooleanExtra("login_success", false);
                        if (loginSuccess) {
                            new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.login_successful)).showMessage(this);
                        }
                    }
                }
        );
    }

    @SuppressLint("NonConstantResourceId")
    private void setUpFragment() {
        initFragments();
        viewBinding.bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    handleFragment(Constants.HOME);
                    return true;
                case R.id.search:
                    handleFragment(Constants.SEARCH);
                    return true;
                case R.id.live:
                    if (viewModel.isLogin()) {
                        handleFragment(Constants.LIVE);
                        return true;
                    } else {
                        showLoginRequiredDialog();
                        return false;
                    }
                case R.id.schedule:
                    handleFragment(Constants.SCHEDULE);
                    return true;
                case R.id.account:
                    if (viewModel.isLogin()) {
                        handleFragment(Constants.ACCOUNT);
                    } else {
                        handleFragment(Constants.ACCOUNT_UN_LOGIN);
                    }
                    return true;
            }
            return false;
        });
    }
    private void initFragments() {
        fm = getSupportFragmentManager();
        homeFragment = new HomeFragment();
        fm.beginTransaction()
                .add(R.id.fragment_container, homeFragment, Constants.HOME)
                .commit();
        active = homeFragment;
    }

    public void handleFragment(String tag) {
        if (fm == null) fm = getSupportFragmentManager();

        if (homeFragment == null) homeFragment = new HomeFragment();
//        if (searchFragment == null) searchFragment = new SearchFragment();
        searchFragment = new SearchFragment();
        if (scheduleFragment == null) scheduleFragment = new ScheduleFragment();
        if (liveFragment == null) liveFragment = new LiveFragment();
        if (accountFragment == null) accountFragment = new AccountFragment();
        if (unLoginAccountFragment == null) unLoginAccountFragment = new UnLoginAccountFragment();

        Fragment target = null;
        switch (tag) {
            case Constants.HOME:
                target = homeFragment;
                break;
            case Constants.SEARCH:
                target = searchFragment;
                break;
            case Constants.LIVE:
                target = liveFragment;
                break;
            case Constants.SCHEDULE:
                target = scheduleFragment;
                break;
            case Constants.ACCOUNT:
                target = accountFragment;
                break;
            case Constants.ACCOUNT_UN_LOGIN:
                target = unLoginAccountFragment;
                break;
        }
        if (target == null || active == target) return;

        if (!target.isAdded()) {
            fm.beginTransaction()
                    .hide(active)
                    .add(R.id.fragment_container, target, tag)
                    .commit();
        } else {
            fm.beginTransaction()
                    .hide(active)
                    .show(target)
                    .commit();
        }
        active = target;
    }
    public void navigateToLanguage() {
        Intent intent = new Intent(this, LanguageActivity.class);
        startActivityForResult(intent, Constants.REQUEST_LANGUAGE);
    }
    public void navigateToCheckUpdate() {
        Intent intent = new Intent(this, CheckUpdateActivity.class);
        startActivity(intent);
    }

    public void navigateToPlayList() {
        Intent intent = new Intent(this, PlayListActivity.class);
        startActivity(intent);
    }

    public void navigateToHistory() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    public void navigateToContact() {
        Intent intent = new Intent(this, ContactActivity.class);
        startActivity(intent);
    }

    public void navigateToPrivacy() {
        Intent intent = new Intent(this, PrivacyActivity.class);
        startActivity(intent);
    }

    public void navigateToFavourite() {
        Intent intent = new Intent(this, FavouriteActivity.class);
        startActivity(intent);
    }

    public void navigateToHomeSideBarDetail(CollectionResponse collectionResponse) {
        Intent intent = new Intent(this, HomeSideBarDetailActivity.class);
        intent.putExtra("collection", GsonUtils.toJson(collectionResponse));
        startActivity(intent);
    }

    public void navigateToMoreTopic() {
        Intent intent = new Intent(this, HomeMoreTopicActivity.class);
        startActivity(intent);
    }

    public void navigateToTopicDetail(CollectionResponse collectionResponse) {
        Intent it = new Intent(this, HomeTopicDetailActivity.class);
        it.putExtra("collection", GsonUtils.toJson(collectionResponse));
        startActivity(it);
    }

    public void navigateToFilter(FilterTypeModel filterTypeModel, List<CategoryResponse> categoryResponseList, MovieRequest request) {
        Intent it = new Intent(MainActivity.this, FilterActivity.class);
        it.putExtra("filter_type", GsonUtils.toJson(filterTypeModel));
        it.putExtra("cate_list", GsonUtils.toJson(categoryResponseList));
        it.putExtra("movie_request", GsonUtils.toJson(request));

        startActivity(it);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_LANGUAGE && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("languageChanged", false)) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }
    }
        @Override
    protected void onResume() {
        super.onResume();

    }
    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }
    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public int getStatusBarColor() {
        return R.color.header_app;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_tab_bar;
    }
    public void userSignOut() {
        viewModel.showLoading();
        viewModel.userSignOut(new MainCallback<Void>() {
            @Override
            public void doSuccess(Void unused) {
                startActivity(new Intent(MainActivity.this, SplashActivity.class));
                new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.sign_out_success)).showMessage(MainActivity.this);
                finish();
            }

            @Override
            public void doError(Throwable throwable) {
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred)).showMessage(MainActivity.this);
            }

            @Override
            public void doFail() {
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred)).showMessage(MainActivity.this);
            }

            @Override
            public void doSuccess() {
                startActivity(new Intent(MainActivity.this, SplashActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        viewBinding.lDialogExit.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
            case R.id.l_dialog_exit:
                viewBinding.lDialogExit.setVisibility(View.GONE);
                break;
            case R.id.btn_ok:
                finish();
                break;
            default:
                break;
        }
    }
    
    public void navigateToMovieDetail(MovieResponse movieResponse, ListWatchHistoryResponse listWatchHistoryResponse) {
        Intent it = new Intent(this, MovieDetailActivity.class);
        if (viewModel.isLogin()) {
            it.putExtra("movie_details", GsonUtils.toJson(movieResponse));
            it.putExtra("movie_details_tracking", GsonUtils.toJson(listWatchHistoryResponse));
            if (viewModel.msgCommentData != null && !viewModel.msgCommentData.isEmpty()) {
                it.putExtra(MovieDetailActivity.DATA_MSG, viewModel.msgCommentData);
            }
        } else {
            it.putExtra("movie_details", GsonUtils.toJson(movieResponse));
        }
        startActivity(it);
    }
    public void navigateToWatchMovie(MovieResponse movieResponse, ListWatchHistoryResponse listWatchHistoryResponse) {
        Intent it = new Intent(this, WatchMovieActivity.class);
        if (viewModel.isLogin()) {
            if (movieResponse.getType() == Constants.TYPE_MOVIE_SERIES) {
                MovieItemResponse remainingEpisode;
                
                if (listWatchHistoryResponse == null || listWatchHistoryResponse.getWatchHistories() == null) {
                    remainingEpisode = null;
                } else {
                    
                    WatchHistoryResponse watchHistoryResponse = listWatchHistoryResponse.getFirstWatchHistory();
                    if (watchHistoryResponse == null) {
                        remainingEpisode = null;
                    } else {
                        
                        MovieItemResponse remaining = new MovieItemResponse();

                        if (!watchHistoryResponse.isCompleted()) {
                            remaining = movieResponse.getEpisodeById(watchHistoryResponse.getMovieItemId());
                        } else {
                            if (!movieResponse.isLastEpisode(watchHistoryResponse.getMovieItemId())) {
                                MovieItemResponse nextEpisode = movieResponse.getNextEpisode(watchHistoryResponse.getMovieItemId());

                                WatchHistoryResponse watchHistoryNoComplete = listWatchHistoryResponse.getWatchHistoryByMovieId(nextEpisode.getId());

                                if (watchHistoryNoComplete == null) {
                                    remaining = movieResponse.getEpisodeById(nextEpisode.getId());
                                } else {
                                    remaining = movieResponse.getEpisodeById(watchHistoryNoComplete.getMovieItemId());
                                }

                            } else {

                                WatchHistoryResponse watchHistoryNoComplete = listWatchHistoryResponse.getWatchHistoryNoComplete();

                                if (watchHistoryNoComplete == null) {
                                    remaining = null;
                                } else {
                                    remaining = movieResponse.getEpisodeById(watchHistoryNoComplete.getMovieItemId());
                                }
                            }
                        }

                        remainingEpisode = remaining;
                    }
                }

                if (remainingEpisode == null) {
                    movieResponse.setSeasonAndEpisodeSelectedAndPlaying(movieResponse.getSeasons().get(0).getEpisodes().get(0).getId());
                    it.putExtra("episode", GsonUtils.toJson(movieResponse.getSeasons().get(0).getEpisodes().get(0)));
                } else {
                    movieResponse.setSeasonAndEpisodeSelectedAndPlaying(remainingEpisode.getId());
                    it.putExtra("episode", GsonUtils.toJson(remainingEpisode));
                }
            }

            it.putExtra("movie_details", GsonUtils.toJson(movieResponse));
            it.putExtra("movie_details_tracking", GsonUtils.toJson(listWatchHistoryResponse));
        } else {
            it.putExtra("movie_details", GsonUtils.toJson(movieResponse));
            if (movieResponse.getType() == Constants.TYPE_MOVIE_SERIES) {
                it.putExtra("episode", GsonUtils.toJson(movieResponse.getSeasons().get(0).getEpisodes().get(0)));
            }
        }
        startActivity(it);
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
                navigateToSetting(response);
            }

            @Override
            public void doFail() {

            }
        });
    }
    public void navigateToSetting(UserResponse response) {
        Intent intent = new Intent(this, SettingActivity.class);
        intent.putExtra("USER_RESPONSE", GsonUtils.toJson(response));

        startActivity(intent);
    }

    public void showMovieDialogDetail(MovieResponse movieResponse) {
        new MovieDetailDialogFragment(movieResponse, this)
                .show(getSupportFragmentManager(), "MovieDialogDetail");
    }

    public void getMovieDetail(MovieResponse movieResponse, int typeNavigate) {
        showLoading();
        viewModel.getMovie(new MainCallback<MovieResponse>() {

            @Override
            public void doSuccess(MovieResponse data) {
                if (viewModel.isLogin()) {
                    getListMovieTracking(data, typeNavigate);
                } else {
                    if (typeNavigate == NavigateToMovieDetails) {
                        navigateToMovieDetail(data, null);
                    } else if (typeNavigate == NavigateToWatchMovie) {
                        navigateToWatchMovie(data, null);
                    }
                }
            }

            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {
                hideLoading();
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, movieResponse.getId());
    }

    public void getListMovieTracking(MovieResponse movieResponse, int typeNavigate) {
        showLoading();
        viewModel.getListMovieTracking(new MainCallback<ListWatchHistoryResponse>() {

            @Override
            public void doSuccess(ListWatchHistoryResponse data) {
                if (typeNavigate == NavigateToMovieDetails) {
                    navigateToMovieDetail(movieResponse, data);
                } else if (typeNavigate == NavigateToWatchMovie) {
                    navigateToWatchMovie(movieResponse, data);
                }
            }

            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {
                hideLoading();
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, movieResponse.getId());
    }
    @Override
    public void onMovieClick(MovieResponse movieResponse) {
        if (movieResponse != null) {
            getMovieDetail(movieResponse, NavigateToMovieDetails);
        }
    }

    @Override
    public void onWatchMovieClick(MovieResponse movieResponse) {
        if (movieResponse != null) {
            getMovieDetail(movieResponse, NavigateToWatchMovie);
        }
    }

    @Override
    public void onMovieLongClick(MovieResponse movieResponse) {

    }

    public void navigateToNotification() {
        Intent it = new Intent(this, NotificationActivity.class);
        startActivity(it);
    }

    public MovieResponse movieResponse;
    public RoomResponse roomResponse;
    public boolean isHost;
    public void createMqtt(MovieResponse movieResponse, RoomResponse model, boolean isHost) {
        viewModel.showLoading();
        this.movieResponse = movieResponse;
        this.roomResponse = model;
        this.isHost = isHost;

        String topicParent = Constants.TOPIC +  model.getId().toString();
        String topicChild = topicParent + "/" + viewModel.getUserId().toString();
        String[] myTopics = { topicParent, topicChild };
        ((MVVMApplication) application).createMqtt(viewModel.getUserId().toString(), myTopics);
    }

    @Override
    public void onConnectionOpened() {
        super.onConnectionOpened();
        Intent it = new Intent(this, WatchMovieActivity.class);
        it.putExtra("movie_details", GsonUtils.toJson(this.movieResponse));
        it.putExtra(WatchMovieActivity.ROOM, GsonUtils.toJson(this.roomResponse));
        if (this.movieResponse.getType() == Constants.TYPE_MOVIE_SERIES) {
            it.putExtra("episode", GsonUtils.toJson(this.movieResponse.getEpisodeById(this.roomResponse.getMovieItem().getId())));
        }

        it.putExtra(WatchMovieActivity.LiveRoom, true);
        it.putExtra(WatchMovieActivity.Host, this.isHost);

        startActivity(it);

        viewModel.hideLoading();
        this.roomResponse = new RoomResponse();
        this.movieResponse = new MovieResponse();
        this.isHost = false;
    }
    private ActivityResultLauncher<Intent> loginLauncher;

    @Override
    protected void handleNotificationData(MessageOneSignal message) {
        if (message != null && OneSignalCommand.CMD_ROOM_INVITE.equals(message.getCmd())) {
            handleRoomInviteNotification(message);
            return;
        }
        super.handleNotificationData(message);
    }

    public void handleRoomInviteNotification(MessageOneSignal messageOneSignal) {
        if (!viewModel.isLogin()) {
            showLoginRequiredDialog();
            return;
        }
        if (messageOneSignal == null || messageOneSignal.getData() == null || messageOneSignal.getData().isEmpty()) {
            return;
        }

        MessageRoomNotificationResponse roomNotification = GsonUtils.fromJson(
                messageOneSignal.getData(), MessageRoomNotificationResponse.class);
        if (roomNotification == null) {
            showError(getString(R.string.an_error_occurred));
            return;
        }

        Long roomId = parseRoomId(roomNotification.getId());
        String roomCode = roomNotification.getCode();
        if (roomId == null && (roomCode == null || roomCode.isEmpty())) {
            showError(getString(R.string.an_error_occurred));
            return;
        }

        showLoading();
        MainCallback<RoomResponse> roomCallback = new MainCallback<RoomResponse>() {
            @Override
            public void doSuccess(RoomResponse room) {
                hideLoading();
                if (room == null) {
                    showError(getString(R.string.an_error_occurred));
                    return;
                }
                handleRoomClick(room);
            }

            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {
                hideLoading();
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        };

        if (roomId != null) {
            viewModel.getRoom(roomCallback, roomId);
        } else {
            viewModel.getRoomByCode(roomCallback, roomCode);
        }
    }

    @Nullable
    private Long parseRoomId(@Nullable String roomId) {
        if (roomId == null || roomId.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(roomId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void showLoginRequiredDialog() {
        DialogUtils.dialogConfirm(
                this,
                getString(R.string.not_login),
                getString(R.string.login),
                (dialog, which) -> {
                    Intent it = new Intent(this, LoginActivity.class);
                    it.putExtra("login_from_other", "login_from_other");
                    loginLauncher.launch(it);
                },
                getString(R.string.cancel),
                null
        );
    }
}
