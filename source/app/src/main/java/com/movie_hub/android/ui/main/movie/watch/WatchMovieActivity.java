package com.movie_hub.android.ui.main.movie.watch;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.ActivityWatchMovieBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;

import eu.davidea.flexibleadapter.databinding.BR;

public class WatchMovieActivity extends BaseActivity<ActivityWatchMovieBinding, WatchMovieViewModel> {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_watch_movie;
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
