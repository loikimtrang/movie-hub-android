package com.movie_hub.android.ui.base.activity;

import static com.movie_hub.android.ui.main.home.HomeFragment.NavigateToMovieDetails;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.local.prefs.AppPreferencesService;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.notification.CountUnReadResponse;
import com.movie_hub.android.data.model.onesignal.MessageCommentResponse;
import com.movie_hub.android.data.model.onesignal.MessageOneSignal;
import com.movie_hub.android.data.model.onesignal.MessageReviewResponse;
import com.movie_hub.android.data.model.onesignal.OneSignalCommand;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.data.mqtt.KittyRealtimeEvent;
import com.movie_hub.android.data.mqtt.Message;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.di.component.DaggerActivityComponent;
import com.movie_hub.android.di.module.ActivityModule;
import com.movie_hub.android.helper.LocaleHelper;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.utils.DialogUtils;
import com.movie_hub.android.utils.GsonUtils;

import javax.inject.Inject;
import javax.inject.Named;

import timber.log.Timber;

public abstract class BaseActivity<B extends ViewDataBinding, V extends BaseViewModel> extends AppCompatActivity implements KittyRealtimeEvent {

    protected B viewBinding;

    @Inject
    protected V viewModel;

    @Inject
    protected Context application;

    @Named("access_token")
    @Inject
    protected String token;

    @Named("device_id")
    @Inject
    protected String deviceId;

    private Dialog progressDialog;
    // Listen all action from local
    private BroadcastReceiver globalApplicationReceiver;
    private IntentFilter filterGlobalApplication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        performDependencyInjection(getBuildComponent());
        super.onCreate(savedInstanceState);
        performDataBinding();
        updateCurrentAcitivity();

        viewModel.mIsLoading.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback(){
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                ProgressBar loadingBar = findViewById(R.id.bottom_loading_bar);
                FrameLayout overLay = findViewById(R.id.loading_overlay);
                if (loadingBar != null && overLay != null) {
                    if (viewModel.mIsLoading.get()) {
                        loadingBar.setVisibility(View.VISIBLE);
                        overLay.setVisibility(View.VISIBLE);
                    }
                    else {
                        loadingBar.setVisibility(View.GONE);
                        overLay.setVisibility(View.GONE);
                    }
                }
            }
        });

        viewModel.mErrorMessage.observe(this, toastMessage -> {
            if(toastMessage!=null){
                toastMessage.showMessage(getApplicationContext());
            }
        });
        filterGlobalApplication = new IntentFilter();
        filterGlobalApplication.addAction(Constants.ACTION_EXPIRED_TOKEN);
        globalApplicationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action==null){
                    return;
                }
                if (action.equals(Constants.ACTION_EXPIRED_TOKEN)){
                    doExpireSession();
                }
            }
        };

        checkNotificationIntent(getIntent());
    }

    protected void applySystemBarColors() {
        if (!(this instanceof SystemBarColorProvider)) return;

        SystemBarColorProvider provider = (SystemBarColorProvider) this;
        setSystemBarsColor(provider.getStatusBarColor(), provider.getNavigationBarColor());
    }

    public void setSystemBarsColor(@ColorRes int statusBarColor, @ColorRes int navBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
            window.setStatusBarColor(ContextCompat.getColor(this, statusBarColor));
            window.setNavigationBarColor(ContextCompat.getColor(this, navBarColor));
        }
    }

    public void showLoading() {
        viewModel.showLoading();
    }

    public void hideLoading() {
        viewModel.hideLoading();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(globalApplicationReceiver, filterGlobalApplication);
        updateCurrentAcitivity();
        applySystemBarColors();
        hideLoading();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(globalApplicationReceiver);
    }
    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof SystemBarColorProvider) {
            SystemBarColorProvider provider = (SystemBarColorProvider) fragment;
            setSystemBarsColor(provider.getStatusBarColor(), provider.getNavigationBarColor());
        }
    }
    public abstract @LayoutRes int getLayoutId();

    public abstract int getBindingVariable();

    public void doExpireSession() {
        //implement later

    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionsSafely(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (imm != null) {
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            } else {
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            }
        }
    }


    private void performDataBinding() {
        viewBinding = DataBindingUtil.setContentView(this, getLayoutId());
        viewBinding.setVariable(getBindingVariable(), viewModel);
        viewBinding.executePendingBindings();
    }

    public void showProgressbar(String msg){
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        progressDialog = DialogUtils.createDialogLoading(this, msg);
        progressDialog.show();
    }

    private ActivityComponent getBuildComponent() {
        return DaggerActivityComponent.builder()
                .appComponent(((MVVMApplication)getApplication()).getAppComponent())
                .activityModule(new ActivityModule(this))
                .build();
    }

    public abstract void performDependencyInjection(ActivityComponent buildComponent);

    private void updateCurrentAcitivity(){
        MVVMApplication mvvmApplication = (MVVMApplication)application;
        mvvmApplication.setCurrentActivity(this);
    }

    public void navigateToNewActivity(Context from, Class<?> toActivityClass) {
        Intent it = new Intent(from, toActivityClass);
        from.startActivity(it);
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        AppPreferencesService prefs = new AppPreferencesService(newBase, Constants.PREF_NAME, new Gson());
        String langCode = prefs.getAppLanguage();
        Context context = LocaleHelper.setLocale(newBase, langCode);
        super.attachBaseContext(context);
    }

    private long lastTouchDown = 0;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            lastTouchDown = System.currentTimeMillis();
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            long clickDuration = System.currentTimeMillis() - lastTouchDown;
            if (clickDuration < 200) { // tap nhẹ, không phải scroll
                View view = getCurrentFocus();
                if (view instanceof EditText && view.isFocused()) {
                    Rect outRect = new Rect();
                    view.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                        view.clearFocus();
                        hideKeyboard();
                    }
                }
            }
        }

        return super.dispatchTouchEvent(ev);
    }
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
//            View view = getCurrentFocus();
//            if (view instanceof android.widget.EditText) {
//                Rect outRect = new Rect();
//                view.getGlobalVisibleRect(outRect);
//                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
//                    view.clearFocus();
//                    hideKeyboard();
//                }
//            }
//        }
//        return super.dispatchTouchEvent(ev);
//    }

    public void showError(String message) {
        new ToastMessage(ToastMessage.TYPE_WARNING, message).showMessage(this);
    }

    public void showMgs(int type, String mgs) {
        new ToastMessage(type,mgs).showMessage(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Cực kỳ quan trọng: Cập nhật intent mới cho Activity
        checkNotificationIntent(intent);
    }

    private void checkNotificationIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra("is_from_notification", false)) {
            String json = intent.getStringExtra("msg_onesignal_data");
            MessageOneSignal message = GsonUtils.fromJson(json, MessageOneSignal.class);
            if (message != null) {
                intent.putExtra("is_from_notification", false);
                handleNotificationData(message);
            }
        }
    }

    /**
     * Hàm này có thể được override ở Activity con nếu cần logic riêng biệt
     */
    protected void handleNotificationData(MessageOneSignal message) {
        switch (message.getCmd()) {
            case OneSignalCommand.CMD_REPLY_COMMENT:
            case OneSignalCommand.CMD_TOXIC_COMMENT_LOCKED:
            case OneSignalCommand.CMD_VOTE_COMMENT:
                MessageCommentResponse messageCommentResponse = GsonUtils.fromJson(message.getData(), MessageCommentResponse.class);
                if (messageCommentResponse != null && messageCommentResponse.getMovieId() != null) {
                    getMovieDetailByNotification(Long.valueOf(messageCommentResponse.getMovieId()), message);
                }
                break;
            case OneSignalCommand.CMD_TOXIC_REVIEW_LOCKED:
            case OneSignalCommand.CMD_VOTE_REVIEW:
                MessageReviewResponse messageReviewResponse = GsonUtils.fromJson(message.getData(), MessageReviewResponse.class);
                if (messageReviewResponse != null && messageReviewResponse.getMovieId() != null) {
                    getMovieDetailByNotification(Long.valueOf(messageReviewResponse.getMovieId()), message);
                }
                break;
            default:
                break;
        }
    }

    public void getMovieDetailByNotification(Long id, MessageOneSignal messageOneSignal) {
        showLoading();
        viewModel.getMovie(new MainCallback<MovieResponse>() {

            @Override
            public void doSuccess(MovieResponse data) {
                if (viewModel.isLogin()) {
                    getListMovieTrackingByNotification(data, messageOneSignal);
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
        }, id);
    }

    public void getListMovieTrackingByNotification(MovieResponse movieResponse, MessageOneSignal messageOneSignal) {
        showLoading();
        viewModel.getListMovieTracking(new MainCallback<ListWatchHistoryResponse>() {

            @Override
            public void doSuccess(ListWatchHistoryResponse data) {
                navigateToMovieDetailByNotification(movieResponse, data, messageOneSignal);

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

    public void navigateToMovieDetailByNotification(MovieResponse movieResponse, ListWatchHistoryResponse listWatchHistoryResponse, MessageOneSignal messageOneSignal) {
        Intent it = new Intent(this, MovieDetailActivity.class);
        if (viewModel.isLogin()) {
            it.putExtra("movie_details", GsonUtils.toJson(movieResponse));
            it.putExtra("movie_details_tracking", GsonUtils.toJson(listWatchHistoryResponse));
            it.putExtra(MovieDetailActivity.DATA_MSG, GsonUtils.toJson(messageOneSignal));
        } else {
            it.putExtra("movie_details", GsonUtils.toJson(movieResponse));
        }
        startActivity(it);
    }

    @Override
    public void onConnectionOpened() {
        Timber.i("MQTT đã kết nối");
    }

    @Override
    public void onConnectionClosed() {
        Timber.w("MQTT đã ngắt kết nối");
    }

    @Override
    public void onMessageTimeout(Message message) {
        Timber.e("Gửi tin nhắn thất bại (Timeout): " + message.getCmd());
    }

    @Override
    public void onConnectionFailed() {
        Timber.e("Kết nối MQTT thất bại");
    }

    @Override
    public void onMessageReceived(Message message) {
        Timber.d("Nhận message tại Base: " + message.getCmd());
    }
}
