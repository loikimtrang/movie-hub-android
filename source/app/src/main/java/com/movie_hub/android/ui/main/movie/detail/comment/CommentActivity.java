package com.movie_hub.android.ui.main.movie.detail.comment;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.ActivityCommentBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.utils.GsonUtils;

public class CommentActivity extends BaseActivity<ActivityCommentBinding, CommentViewModel> implements SystemBarColorProvider {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        String json = getIntent().getStringExtra("movie_details");
        MovieResponse movieResponse = GsonUtils.fromJson(json, MovieResponse.class);

        if (movieResponse != null) {
            viewModel.movieDetails = movieResponse;
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_comment;
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
        return R.color.transparent;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_comment;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_bottom);
    }

}
