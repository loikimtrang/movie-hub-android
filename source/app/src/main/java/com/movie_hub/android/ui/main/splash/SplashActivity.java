package com.movie_hub.android.ui.main.splash;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.movie_hub.android.BR;
import com.movie_hub.android.BuildConfig;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.appversion.CheckAppVersionRequest;
import com.movie_hub.android.data.model.api.request.side_bar.SideBarRequest;
import com.movie_hub.android.data.model.api.response.appversion.CheckAppVersionResponse;
import com.movie_hub.android.data.model.api.response.side_bar.SidebarResponse;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivitySplashBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.login.LoginActivity;
import com.movie_hub.android.ui.main.account.updateapp.UpdateManager;
import com.movie_hub.android.ui.main.account.updateapp.dialog.UpdateVersionBottomSheetDialog;
import com.movie_hub.android.ui.main.splash.survey.SurveyActivity;
import com.movie_hub.android.utils.GsonUtils;
import com.movie_hub.android.utils.LogService;

import java.io.File;
import java.net.ConnectException;

import timber.log.Timber;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity<ActivitySplashBinding, SplashViewModel> implements View.OnClickListener, SystemBarColorProvider, UpdateVersionBottomSheetDialog.UpdateVersionBottomSheetCallback {
    private static final int REQUEST_STORAGE_PERMISSION = 123;

    private final BroadcastReceiver expiredTokenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogService.i("Received expired token broadcast, logging out user");
            userSignOut();
        }
    };
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    LogService.i("Notification permission granted");
                } else {
                    LogService.w("Notification permission denied");
                }
                handleCheckUpdate();
            });
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        checkNotificationPermission();
        hideSystemUI();;

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(expiredTokenReceiver, new IntentFilter(Constants.ACTION_EXPIRED_TOKEN));

        handleNotificationIntent(getIntent());
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent != null && intent.hasExtra("msg_onesignal_data")) {
            String json = intent.getStringExtra("msg_onesignal_data");
            Timber.d("ONESIGNAL_LOG: SplashActivity nhận được JSON: %s", json);
            getIntent().removeExtra("msg_onesignal_data");

            if (json != null && !json.isEmpty()) {
                viewModel.messageOneSignal = json;
            }
        } else {
            Timber.d("ONESIGNAL_LOG: Intent không chứa dữ liệu thông báo.");
        }
    }
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                handleCheckUpdate();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            handleCheckUpdate();
        }
    }
    public void setupSystemBars() {
        Window window = getWindow();
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
        }
    }
    public void handleCheckUpdate() {
        checkUpdate();
    }

    public void handleLogin() {
        if (viewModel.isLogin()) {
            if (getIntent().getBooleanExtra("login_success", false)) {
                showLoginSuccessMessage();
            } else {
                getUserProfile();
            }
        } else {
            new Handler().postDelayed(this::showLoginAndSkip, 1000);
        }
    }
    public void showLoginAndSkip() {
        viewBinding.layoutButton.setVisibility(View.VISIBLE);
        viewBinding.loadingProgress.setVisibility(View.GONE);
    }
    private void showLoginSuccessMessage() {
        new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.login_successful)).showMessage(this);
        getUserProfile();
    }

    public void getUserProfile() {
        viewModel.getUserProfile(new MainCallback<UserResponse>() {
            @Override
            public void doError(Throwable error) {
                if (error instanceof ConnectException) {
                    showError(getString(R.string.cannot_connect_to_the_server_please_try_again));
                } else {
                    userSignOut();
                }
            }

            @Override
            public void doSuccess(UserResponse response) {
                if (response.isMakeSurvey()) {
                    navigateToMainActivity();
                } else {
                    getListSideBar();
                }
            }

            @Override
            public void doSuccess() {
                navigateToMainActivity();
            }

            @Override
            public void doFail() {
                userSignOut();
            }
        });
    }

    public void userSignOut() {
        viewModel.showLoading();
        viewModel.userSignOut(new MainCallback<Void>() {
            @Override
            public void doSuccess(Void unused) {
                showLoginAndSkip();
            }

            @Override
            public void doError(Throwable throwable) {
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred)).showMessage(SplashActivity.this);
            }

            @Override
            public void doFail() {
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred)).showMessage(SplashActivity.this);
            }

            @Override
            public void doSuccess() {
                showLoginAndSkip();
            }
        });
    }
    public void navigateToMainActivity() {
        Intent it = new Intent(this, MainActivity.class);
        it.putExtra("msg_onesignal_data", viewModel.messageOneSignal);
        startActivity(it);
        finish();
//        getListSideBar();
    }

    public void navigateToSurveyActivity() {
        startActivity(new Intent(this, SurveyActivity.class));
        finish();
//        getListSideBar();
    }

    public void navigateToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId())  {
            case R.id.btn_login:
                navigateToLoginActivity();
                break;
            case R.id.btn_skip:
                navigateToMainActivity();
                break;
            default:
                break;
        }
    }

    public void checkUpdate() {
        CheckAppVersionRequest request = new CheckAppVersionRequest();
        request.setName(BuildConfig.VERSION_NAME);
        viewModel.checkUpdate(new MainCallback<ResponseWrapper<CheckAppVersionResponse>>() {
            @Override
            public void doError(Throwable error) {
                hideLoading();
            }

            @Override
            public void doSuccess() {
                hideLoading();
            }

            @Override
            public void doSuccess(ResponseWrapper<CheckAppVersionResponse> response) {
                hideLoading();
                if (response.isResult()) {
                    viewModel.checkAppVersionResponse = response.getData();

                    if (viewModel.checkAppVersionResponse.getUpdateRequired()) {
                        showUpdateVersionBottomSheet();
                    } else {
                        handleLogin();
                    }
                } else {
                    showMgs(ToastMessage.TYPE_NORMAL, getString(R.string.current_version_is_lasted));
                }
            }

            @Override
            public void doFail() {
                hideLoading();
            }
        }, request);
    }

    public void showUpdateVersionBottomSheet() {
        UpdateVersionBottomSheetDialog sheet =
                new UpdateVersionBottomSheetDialog(
                        this,
                        this,
                        viewModel.checkAppVersionResponse
                );

        sheet.show();

        if (sheet.getWindow() != null) {
            sheet.getWindow().getDecorView().post(sheet::setupWindow);
        }
    }
    private void startUpdate() {
        if (viewModel.checkAppVersionResponse != null) {
            new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.app_will_update)).showMessage(getApplicationContext());
            new UpdateManager(this).downloadApk(viewModel.checkAppVersionResponse.getLatestVersion().getUrl(), this::handleInstallApkAfterDownload);
        }
    }

    private void handleInstallApkAfterDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .setData(Uri.parse("package:" + getPackageName()));
                unknownSourcesLauncher.launch(intent);
                return;
            }
        }
        installApk(new File(getExternalFilesDir(null), "app_update.apk"));
    }

    private void installApk(File apkFile) {
        Uri apkUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                ? androidx.core.content.FileProvider.getUriForFile(this, getPackageName() + ".provider", apkFile)
                : Uri.fromFile(apkFile);

        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, getPackageName());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public int getStatusBarColor() {
        return R.color.bg_app;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_app;
    }

    @Override
    public void onUpdateClicked() {
        startUpdate();
    }

    @Override
    public void onSkipClicked() {
        if (viewModel.checkAppVersionResponse.getForceUpdate()) {
            finishAffinity();
            android.os.Process.killProcess(android.os.Process.myPid());
        } else {
            handleLogin();
        }
    }

    private final ActivityResultLauncher<Intent> unknownSourcesLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (getPackageManager().canRequestPackageInstalls()) {
                        installApk(new File(getExternalFilesDir(null), "app_update.apk"));
                    } else {
                        new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.permission_install)).showMessage(this);
                    }
                }
            });

    public void hideSystemUI() {
        Window window = getWindow();

        WindowCompat.setDecorFitsSystemWindows(window, false);

        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
        window.setNavigationBarColor(android.graphics.Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.show(WindowInsets.Type.systemBars());
            }
        } else {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }

        setupSystemBars();
    }
    public void getListSideBar() {
        showLoading();
        viewModel.getListSideBar(new MainCallback<ResponseListObj<SidebarResponse>>() {
            @Override
            public void doSuccess(ResponseListObj<SidebarResponse> data) {
                hideLoading();
                Intent it = new Intent(SplashActivity.this, SurveyActivity.class);
                it.putExtra("home_banner", GsonUtils.toJson(data.getContent()));
                startActivity(it);
                finish();
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
        }, new SideBarRequest());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(expiredTokenReceiver);
    }

}
