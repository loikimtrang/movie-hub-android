package com.movie_hub.android.ui.main.account;

import android.annotation.SuppressLint;

import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.databinding.FragmentAccountBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.account.adapter.AccountMenuAdapter;
import com.movie_hub.android.ui.main.account.manage_account.ManageAccountActivity;
import com.movie_hub.android.ui.main.account.model.MenuItemModel;

import java.util.Arrays;
import java.util.List;

import eu.davidea.flexibleadapter.databinding.BR;

public class AccountFragment extends BaseFragment<FragmentAccountBinding, AccountViewModel> implements AccountMenuAdapter.OnItemClickListener, SystemBarColorProvider {
    private AccountMenuAdapter adapter;
    public static MutableLiveData<UserResponse> PROFILE = new MutableLiveData<>();

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
        setUpObserversProfile();
        setUpMenu();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PROFILE = new MutableLiveData<>();
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_account;
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }
    @Override
    public int getStatusBarColor() {
        return R.color.account_header;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_tab_bar;
    }
    public void setUpObserversProfile() {
        PROFILE.observe(this, profile -> {
            if (profile == null) return;
            Glide.with(this)
                    .load(profile.getAvatarPath())
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.logo)
                    .into(binding.avatar);
            if (profile.getFullName() == null) {
                binding.name.setText(profile.getUsername());
            } else {
                binding.name.setText(profile.getFullName());
            }
            binding.email.setText(profile.getEmail());
        });
    }
    public void setUpMenu() {
        List<MenuItemModel> menuItems = Arrays.asList(
                new MenuItemModel(R.drawable.ic_watching, R.string.menu_watch_now),
                new MenuItemModel(R.drawable.ic_plus, R.string.menu_my_movies),
                new MenuItemModel(R.drawable.ic_heart, R.string.menu_favorites),
                new MenuItemModel(R.drawable.ic_policy, R.string.menu_privacy_policy),
                new MenuItemModel(R.drawable.ic_question_contact, R.string.menu_contact),
                new MenuItemModel(R.drawable.ic_language, R.string.menu_language)
        );

        adapter = new AccountMenuAdapter(menuItems, this);
        binding.rvMenu.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMenu.setAdapter(adapter);
    }
    public void onManageAccountClick() {
        ManageAccountActivity.PROFILE.setValue(PROFILE.getValue());
        ((MainActivity) requireActivity()).navigateToNewActivity(getContext(), ManageAccountActivity.class);
    }
    public void onSignOutClick() {
        ((MainActivity) requireActivity()).userSignOut();
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onItemClick(MenuItemModel item) {
        switch (item.title) {
            case R.string.menu_watch_now:
                break;
            case R.string.menu_my_movies:
                break;
            case R.string.menu_favorites:
                break;
            case R.string.menu_privacy_policy:
                break;
            case R.string.menu_contact:
                break;
            case R.string.menu_language:
                ((MainActivity) requireActivity()).navigateToLanguage();
                break;
        }
    }
}
