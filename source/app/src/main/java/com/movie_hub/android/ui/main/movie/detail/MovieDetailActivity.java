package com.movie_hub.android.ui.main.movie.detail;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.tabs.TabLayoutMediator;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.ActivityMovieDetailBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.movie.detail.adapter.MovieDetailTabAdapter;
import com.movie_hub.android.ui.main.movie.detail.adapter.TagCategoryAdapter;
import com.movie_hub.android.ui.main.movie.detail.fragment.CastFragment;
import com.movie_hub.android.ui.main.movie.detail.fragment.EpisodesFragment;
import com.movie_hub.android.ui.main.movie.detail.fragment.RecommendationFragment;
import com.movie_hub.android.ui.main.search.topTrending.FlexSpacingItemDecoration;
import com.movie_hub.android.utils.HtmlUtils;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.databinding.BR;

public class MovieDetailActivity extends BaseActivity<ActivityMovieDetailBinding, MovieDetailViewModel> implements SystemBarColorProvider {
    private TagCategoryAdapter tagCategoryAdapter;
    private final List<Fragment> fragmentList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        MovieResponse movie = getIntent().getParcelableExtra("movie_details");
        if (movie != null) {
            viewModel.setMovieDetails(movie);
        }

        observeViewModel();
        setUpTab(false);
    }


    private void observeViewModel() {
        viewModel.getMovieDetails().observe(this, movie -> {
            if (movie == null) return;

            viewBinding.includeMovieHeader.nameMovie.setText(movie.getTitle());
            viewBinding.includeMovieHeader.nameMovieOriginal.setText(movie.getOriginalTitle());
            viewBinding.includeMovieHeader.description.setText(HtmlUtils.convertPtoStrong(movie.getDescription()));

            tagCategoryAdapter = new TagCategoryAdapter();
            FlexboxLayoutManager layout = new FlexboxLayoutManager(this);
            layout.setFlexDirection(FlexDirection.ROW);
            layout.setFlexWrap(FlexWrap.WRAP);
            layout.setJustifyContent(JustifyContent.FLEX_START);
            layout.setAlignItems(AlignItems.FLEX_START);

            viewBinding.includeMovieHeader.cateList.setLayoutManager(layout);
            int a = getResources().getDimensionPixelSize(R.dimen._6sdp);
            viewBinding.includeMovieHeader.cateList.addItemDecoration(new FlexSpacingItemDecoration(a));
            viewBinding.includeMovieHeader.cateList.setAdapter(tagCategoryAdapter);

            tagCategoryAdapter.setData(movie.getCategories());
        });
    }



    public void setUpTab(boolean isSeries) {
        List<String> tabTitles = new ArrayList<>();
        fragmentList.clear();

        if (isSeries) {
            tabTitles.add("Tập phim");
            fragmentList.add(new EpisodesFragment());
        }

        tabTitles.add("Diễn viên");
        fragmentList.add(new CastFragment());

        tabTitles.add("Đề xuất");
        fragmentList.add(new RecommendationFragment());


        MovieDetailTabAdapter tabAdapter = new MovieDetailTabAdapter(this, fragmentList);
        viewBinding.subViewPager.setAdapter(tabAdapter);

        new TabLayoutMediator(viewBinding.subTabLayout, viewBinding.subViewPager,
                (tab, position) -> tab.setText(tabTitles.get(position))
        ).attach();

    }


    @Override
    public int getLayoutId() {
        return R.layout.activity_movie_detail;
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
    protected void onDestroy() {
        super.onDestroy();
    }
}
