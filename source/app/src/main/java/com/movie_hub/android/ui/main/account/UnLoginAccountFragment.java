package com.movie_hub.android.ui.main.account;

import android.annotation.SuppressLint;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.FragmentUnLoginAccountBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.MainActivity;
import com.movie_hub.android.ui.main.account.adapter.AccountMenuAdapter;
import com.movie_hub.android.ui.main.account.login.LoginActivity;
import com.movie_hub.android.ui.main.account.model.MenuItemModel;
import com.movie_hub.android.utils.ClickUtils;

import java.util.Arrays;
import java.util.List;

import eu.davidea.flexibleadapter.databinding.BR;

public class UnLoginAccountFragment extends BaseFragment<FragmentUnLoginAccountBinding, UnLoginAccountViewModel> implements SystemBarColorProvider, AccountMenuAdapter.OnItemClickListener {
    private AccountMenuAdapter adapter;
    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
        setUpMenu();
    }

    public void setUpMenu() {
        List<MenuItemModel> menuItems = Arrays.asList(
                new MenuItemModel(R.drawable.ic_watching, R.string.menu_watch_now),
                new MenuItemModel(R.drawable.ic_plus, R.string.menu_my_movies),
                new MenuItemModel(R.drawable.ic_heart, R.string.menu_favorites),
                new MenuItemModel(R.drawable.ic_policy, R.string.menu_privacy_policy),
                new MenuItemModel(R.drawable.ic_question_contact, R.string.menu_contact),
                new MenuItemModel(R.drawable.ic_language, R.string.menu_language),
                new MenuItemModel(R.drawable.ic_update_app, R.string.check_for_update)
        );

        adapter = new AccountMenuAdapter(menuItems, this);
        binding.rvMenu.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMenu.setAdapter(adapter);
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_un_login_account;
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onItemClick(MenuItemModel item) {
        switch (item.title) {
            case R.string.menu_watch_now:
            case R.string.menu_my_movies:
            case R.string.menu_favorites:
                onLoginClick();
                break;
            case R.string.menu_privacy_policy:
                break;
            case R.string.menu_contact:
                break;
            case R.string.menu_language:
                ((MainActivity) requireActivity()).navigateToLanguage();
                break;
            case R.string.check_for_update:
                ((MainActivity) requireActivity()).navigateToCheckUpdate();
                break;
        }
    }
    public void onLoginClick() {
        ClickUtils.debounceClick(binding.login);

        ((MainActivity) requireActivity()).navigateToNewActivity(getContext(), LoginActivity.class);
    }
}
