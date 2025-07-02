package com.movie_hub.android.ui.main.account.manage_account;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.login.UserRegisterRequest;
import com.movie_hub.android.data.model.api.request.user.UserChangePasswordRequest;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityManageAccountBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.fragment.RegisterBottomSheetFragment;
import com.movie_hub.android.ui.main.account.manage_account.adapter.ManageAccountMenuAdapter;
import com.movie_hub.android.ui.main.account.manage_account.fragment.ChangePasswordBottomSheetFragment;
import com.movie_hub.android.ui.main.account.manage_account.model.ManageAccountItemModel;
import com.movie_hub.android.utils.ClickUtils;

import java.util.Arrays;
import java.util.List;

import eu.davidea.flexibleadapter.databinding.BR;

public class ManageAccountActivity extends BaseActivity<ActivityManageAccountBinding, ManageAccountViewModel> implements ManageAccountMenuAdapter.OnItemClickListener, SystemBarColorProvider {
    private ManageAccountMenuAdapter adapter;
    public static MutableLiveData<UserResponse> PROFILE = new MutableLiveData<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        setUpObserversProfile();
        setUpMenu();
    }
    public void setUpObserversProfile() {
        PROFILE.observe(this, profile -> {
            if (profile == null) return;
            Glide.with(this)
                    .load(profile.getAvatarPath())
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(viewBinding.avatar);
            if (profile.getFullName() == null) {
                viewBinding.name.setText(profile.getUsername());
            } else {
                viewBinding.name.setText(profile.getFullName());
            }
        });
    }
    public void setUpMenu() {
        List<ManageAccountItemModel> menuItems = Arrays.asList(
                new ManageAccountItemModel(R.drawable.ic_edit, R.string.update_information),
                new ManageAccountItemModel(R.drawable.ic_lock, R.string.change_password)
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onItemClick(ManageAccountItemModel item) {
        switch (item.idTitle) {
            case R.string.update_information:

                break;
            case R.string.change_password:
                ChangePasswordBottomSheetFragment changePasswordBottomSheetFragment = new ChangePasswordBottomSheetFragment();
                changePasswordBottomSheetFragment.show(getSupportFragmentManager(), changePasswordBottomSheetFragment.getTag());
                break;
        }
    }

    @Override
    public int getStatusBarColor() {
        return R.color.account_header;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_app;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PROFILE = new MutableLiveData<>();
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
}
