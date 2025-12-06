package com.movie_hub.android.ui.main.account.privacy;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.databinding.ActivityPrivacyBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;

public class PrivacyActivity extends BaseActivity<ActivityPrivacyBinding, PrivacyViewModel> implements SystemBarColorProvider {
    @Override
    public int getLayoutId() {
        return R.layout.activity_privacy;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        WebView webView = viewBinding.webView;

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        webView.loadUrl("https://tenant-cms.vercel.app/privacy");
    }
}
