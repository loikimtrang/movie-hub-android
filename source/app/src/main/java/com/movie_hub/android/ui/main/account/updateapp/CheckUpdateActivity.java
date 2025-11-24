package com.movie_hub.android.ui.main.account.updateapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;

public class CheckUpdateActivity extends BaseActivity<ActivityCheckUpdateBinding, CheckUpdateViewModel> implements SystemBarColorProvider, View.OnClickListener {
    private int versionCode;
    private String currentVersion;
    private static final int REQUEST_STORAGE_PERMISSION = 123;
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

        viewBinding.layoutDialogConfirm.dialogMessage.setText(getString(R.string.new_update_available));
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
        return R.color.account_header;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_app;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_check_update:
                checkUpdate();
                break;
            case R.id.btn_ok:
                viewBinding.layoutDialogConfirm.lDialog.setVisibility(View.GONE);
                checkAndRequestPermission();
                break;
            case R.id.btn_cancel:
            case R.id.l_dialog:
                viewBinding.layoutDialogConfirm.lDialog.setVisibility(View.GONE);
                break;
            default:
                break;
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
                showMgs(ToastMessage.TYPE_ERROR, getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {
            }

            @Override
            public void doSuccess(ResponseWrapper<CheckAppVersionResponse> response) {
                hideLoading();
                if (response.isResult()) {
                    viewModel.checkAppVersionResponse = response.getData();
                    if (viewModel.checkAppVersionResponse.getUpdateRequired()) {
                        viewBinding.layoutDialogConfirm.lDialog.setVisibility(View.VISIBLE);
                    } else {
                        showMgs(ToastMessage.TYPE_NORMAL, getString(R.string.current_version_is_lasted));
                    }
                } else {
                    showMgs(ToastMessage.TYPE_ERROR, getString(R.string.an_error_occurred));
                }
            }

            @Override
            public void doFail() {
                hideLoading();
                showMgs(ToastMessage.TYPE_ERROR, getString(R.string.an_error_occurred));
            }
        }, request);
    }

    private void checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, REQUEST_STORAGE_PERMISSION);
            } else {
                startUpdate();
            }
        } else {
            startUpdate();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startUpdate();
            } else {
                new ToastMessage(ToastMessage.TYPE_ERROR, getString(R.string.no_memory_permission)).showMessage(this);
            }
        }
    }

    private void startUpdate() {
        if (viewModel.checkAppVersionResponse != null) {
            new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.app_will_update)).showMessage(getApplicationContext());
            new UpdateManager(this).downloadAndInstallApk(viewModel.checkAppVersionResponse.getLatestVersion().getUrl());
        }
    }
}
