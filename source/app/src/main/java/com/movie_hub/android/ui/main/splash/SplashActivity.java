package com.movie_hub.android.ui.main.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.ActivitySplashBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.main.MainActivity;

import eu.davidea.flexibleadapter.databinding.BR;

public class SplashActivity extends BaseActivity<ActivitySplashBinding, SplashViewModel> {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SplashTheme);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        startActivity(new Intent(this, MainActivity.class));
        finish();
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
}
