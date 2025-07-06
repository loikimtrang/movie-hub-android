package com.movie_hub.android.ui.main.account.language;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.custom.CustomDialog;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityLanguageBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.account.language.adapter.LanguageMenuAdapter;
import com.movie_hub.android.ui.main.account.language.model.LanguageItemModel;
import com.movie_hub.android.ui.main.splash.SplashActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LanguageActivity extends BaseActivity<ActivityLanguageBinding, LanguageViewModel> implements SystemBarColorProvider, LanguageMenuAdapter.OnItemClickListener {
    private LanguageMenuAdapter adapter;
    List<LanguageItemModel> menuItems = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        setUpMenuLanguage();
    }

    public void setUpMenuLanguage() {
        menuItems.add(new LanguageItemModel(R.string.en,"en",  false));
        menuItems.add(new LanguageItemModel(R.string.vi,"vi",  false));

        String lang = viewModel.getLanguage();

        for (LanguageItemModel item: menuItems) {
            if (Objects.equals(lang, item.code)) item.isCheck = true;
        }

        adapter = new LanguageMenuAdapter(menuItems, this);
        viewBinding.rvMenu.setLayoutManager(new LinearLayoutManager(this));
        viewBinding.rvMenu.setAdapter(adapter);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_language;
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

    @Override
    public void onItemClick(LanguageItemModel item) {
        viewModel.showLoading();

        new Handler().postDelayed(() -> {
            viewModel.hideLoading();
            viewModel.setLanguage(item.code);

            Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finishAffinity();
        }, 1000);
    }

}
