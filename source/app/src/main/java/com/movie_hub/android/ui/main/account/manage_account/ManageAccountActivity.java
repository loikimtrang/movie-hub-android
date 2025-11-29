package com.movie_hub.android.ui.main.account.manage_account;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.user.UserChangePasswordRequest;
import com.movie_hub.android.data.model.api.request.user.UserUpdateProfileRequest;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.api.response.user.UserUploadImageResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityManageAccountBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.manage_account.adapter.ManageAccountMenuAdapter;
import com.movie_hub.android.ui.main.account.manage_account.fragment.AvatarBottomSheetFragment;
import com.movie_hub.android.ui.main.account.manage_account.fragment.ChangeInformationBottomSheetFragment;
import com.movie_hub.android.ui.main.account.manage_account.fragment.ChangePasswordBottomSheetFragment;
import com.movie_hub.android.ui.main.account.manage_account.model.ManageAccountItemModel;
import com.movie_hub.android.utils.ClickUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import eu.davidea.flexibleadapter.databinding.BR;
import timber.log.Timber;

public class ManageAccountActivity extends BaseActivity<ActivityManageAccountBinding, ManageAccountViewModel> implements ManageAccountMenuAdapter.OnItemClickListener, SystemBarColorProvider {
    private ManageAccountMenuAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        viewModel.getUser();
        viewModel.getCurrentUserLiveData().observe(this, user -> {
            if (user != null) {
                Timber.d("👤 Hiển thị user: %s", user.getUsername());
                setUpUser(user);
            }
        });
        setUpMenu();
        viewBinding.avatar.setOnClickListener(v -> {
            ClickUtils.debounceClick(viewBinding.avatar);
            openAvatarMenu();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.getUser();
    }

    public void setUpUser(UserResponse profile) {
        if (profile == null) return;
        Glide.with(this)
                .load(Constants.MEDIA_URL + profile.getAvatarPath())
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(viewBinding.avatar);
        if (profile.getFullName() == null) {
            viewBinding.name.setText(profile.getUsername());
        } else {
            viewBinding.name.setText(profile.getFullName());
        }
    }
    public void setUpMenu() {
        List<ManageAccountItemModel> menuItems = Arrays.asList(
                new ManageAccountItemModel(R.drawable.ic_edit, R.string.update_information),
                new ManageAccountItemModel(R.drawable.ic_lock_screen, R.string.change_password)
        );

        adapter = new ManageAccountMenuAdapter(menuItems, this);
        viewBinding.rvMenu.setLayoutManager(new LinearLayoutManager(this));
        viewBinding.rvMenu.setAdapter(adapter);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_manage_account;
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    public void openAvatarMenu() {
        AvatarBottomSheetFragment avatarBottomSheetFragment = new AvatarBottomSheetFragment();
        avatarBottomSheetFragment.show(getSupportFragmentManager(), avatarBottomSheetFragment.getTag());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onItemClick(ManageAccountItemModel item) {
        switch (item.idTitle) {
            case R.string.update_information:
                ChangeInformationBottomSheetFragment.PROFILE = viewModel.getCurrentUserLiveData().getValue();
                ChangeInformationBottomSheetFragment changeInformationBottomSheetFragment = new ChangeInformationBottomSheetFragment();
                changeInformationBottomSheetFragment.show(getSupportFragmentManager(), changeInformationBottomSheetFragment.getTag());
                break;
            case R.string.change_password:
                ChangePasswordBottomSheetFragment changePasswordBottomSheetFragment = new ChangePasswordBottomSheetFragment();
                changePasswordBottomSheetFragment.show(getSupportFragmentManager(), changePasswordBottomSheetFragment.getTag());
                break;
        }
    }

    @Override
    public int getStatusBarColor() {
        return R.color.header_app;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_app;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    public void userChangePassword(UserChangePasswordRequest request, MainCallback<ResponseWrapper> callback) {
        viewModel.userChangePassWord(new MainCallback<ResponseWrapper>() {
            @Override
            public void doSuccess(ResponseWrapper object) {
                new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.change_password_success)).showMessage(ManageAccountActivity.this);
                callback.doSuccess(object);
            }
            @Override
            public void doErrorForm(ResponseWrapper response) {
                callback.doErrorForm(response);
            }
            @Override
            public void doFail() {
                callback.doFail();
            }

            @Override
            public void doError(Throwable throwable) {
                callback.doError(throwable);
            }

            @Override
            public void doSuccess() {}

        }, request);
    }

    public void userChangeInformation(UserUpdateProfileRequest request, MainCallback<ResponseWrapper> callback) {
        viewModel.userChangeInformation(new MainCallback<ResponseWrapper>() {
            @Override
            public void doSuccess(ResponseWrapper object) {
                new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.mgs_update_success)).showMessage(ManageAccountActivity.this);
                callback.doSuccess(object);
                viewModel.getUser();
                setUpUser(viewModel.getCurrentUserLiveData().getValue());
            }
            @Override
            public void doErrorForm(ResponseWrapper response) {
                callback.doErrorForm(response);
            }
            @Override
            public void doFail() {
                callback.doFail();
            }

            @Override
            public void doError(Throwable throwable) {
                callback.doError(throwable);
            }

            @Override
            public void doSuccess() {}

        }, request);
    }

    public void uploadAvatar(Uri croppedUri, MainCallback<UserUploadImageResponse> callback) {

        File file = new File(Objects.requireNonNull(croppedUri.getPath()));

        viewModel.uploadAvatar(file, new MainCallback<UserUploadImageResponse>() {
            @Override
            public void doSuccess(UserUploadImageResponse object) {
                callback.doSuccess(object);
            }

            @Override
            public void doFail() {
                callback.doFail();
            }

            @Override
            public void doError(Throwable error) {
                callback.doError(error);
            }

            @Override
            public void doSuccess() {}
        });
    }

    public void userChangeAvatar(String filePath, MainCallback<ResponseWrapper> callback) {
        UserUpdateProfileRequest request = getUserUpdateProfileRequest(filePath);

        viewModel.userChangeInformation(new MainCallback<ResponseWrapper>() {
            @Override
            public void doSuccess(ResponseWrapper object) {
                callback.doSuccess(object);
                viewModel.getUser();
                setUpUser(viewModel.getCurrentUserLiveData().getValue());
            }
            @Override
            public void doErrorForm(ResponseWrapper response) {
                callback.doErrorForm(response);
            }
            @Override
            public void doFail() {
                callback.doFail();
            }

            @Override
            public void doError(Throwable throwable) {
                callback.doError(throwable);
            }

            @Override
            public void doSuccess() {}

        }, request);
    }

    @NonNull
    private UserUpdateProfileRequest getUserUpdateProfileRequest(String filePath) {
        UserUpdateProfileRequest request = new UserUpdateProfileRequest();

        request.setAvatarPath(filePath);
        request.setGender(Objects.requireNonNull(viewModel.getCurrentUserLiveData().getValue()).getGender());
        request.setPhone(Objects.requireNonNull(viewModel.getCurrentUserLiveData().getValue()).getPhone());
        request.setUsername(Objects.requireNonNull(viewModel.getCurrentUserLiveData().getValue()).getUsername());
        request.setFullName(Objects.requireNonNull(viewModel.getCurrentUserLiveData().getValue()).getFullName());
        return request;
    }
}
