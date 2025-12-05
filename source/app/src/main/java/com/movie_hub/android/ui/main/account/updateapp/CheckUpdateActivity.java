package com.movie_hub.android.ui.main.account.updateapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.movie_hub.android.BR;
import com.movie_hub.android.BuildConfig;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.appversion.CheckAppVersionRequest;
import com.movie_hub.android.data.model.api.response.appversion.CheckAppVersionResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityCheckUpdateBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.updateapp.dialog.UpdateVersionBottomSheetDialog;

import java.io.File;

public class CheckUpdateActivity extends BaseActivity<ActivityCheckUpdateBinding, CheckUpdateViewModel> implements SystemBarColorProvider, View.OnClickListener, UpdateVersionBottomSheetDialog.UpdateVersionBottomSheetCallback {
    private int versionCode;
    private String currentVersion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        setUpView();
    }

    @SuppressLint("SetTextI18n")
    public void setUpView() {
        currentVersion = BuildConfig.VERSION_NAME;
        versionCode = BuildConfig.VERSION_CODE;

        viewBinding.tvCurrentVersion.setText(getString(R.string.current_version) + ": " + currentVersion);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_check_update;
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
        return R.color.bg_app;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_check_update) {
            checkUpdate();
        }
    }

    public void checkUpdate() {
        CheckAppVersionRequest request = new CheckAppVersionRequest();
        request.setName(currentVersion);
        viewModel.showLoading();
        viewModel.checkUpdate(new MainCallback<ResponseWrapper<CheckAppVersionResponse>>() {
            @Override
            public void doError(Throwable error) {
                hideLoading();
                showMgs(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {}

            @Override
            public void doSuccess(ResponseWrapper<CheckAppVersionResponse> response) {
                hideLoading();
                if (response.isResult()) {
                    viewModel.checkAppVersionResponse = response.getData();
                    if (viewModel.checkAppVersionResponse.getUpdateRequired()) {
                        showUpdateVersionBottomSheet();
                    } else {
                        showMgs(ToastMessage.TYPE_NORMAL, getString(R.string.current_version_is_lasted));
                    }
                } else {
                    showMgs(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred));
                }
            }

            @Override
            public void doFail() {
                hideLoading();
                showMgs(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred));
            }
        }, request);
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

    public void showUpdateVersionBottomSheet() {
        UpdateVersionBottomSheetDialog sheet = new UpdateVersionBottomSheetDialog(this, this, viewModel.checkAppVersionResponse);
        sheet.show();
        if (sheet.getWindow() != null) {
            sheet.getWindow().getDecorView().post(sheet::setupWindow);
        }
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
}