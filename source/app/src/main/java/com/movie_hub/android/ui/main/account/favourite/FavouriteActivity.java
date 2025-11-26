package com.movie_hub.android.ui.main.account.favourite;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.databinding.ActivityFavouriteBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.account.favourite.fragment.MovieFavoriteFragment;
import com.movie_hub.android.ui.main.movie.detail.fragment.CastFragment;
import com.movie_hub.android.ui.main.search.result.adpter.SearchResultTabAdapter;

import java.util.ArrayList;
import java.util.List;

public class FavouriteActivity extends BaseActivity<ActivityFavouriteBinding, FavouriteViewModel> implements SystemBarColorProvider, View.OnClickListener {
    private final List<Fragment> fragmentList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        setUpTab();
    }

    public void setUpTab() {
        fragmentList.clear();
        fragmentList.add(MovieFavoriteFragment.newInstance());
        fragmentList.add(CastFragment.newInstance(CastFragment.TYPE_FAVORITE, null));
        SearchResultTabAdapter tabAdapter = new SearchResultTabAdapter(this, fragmentList);
        viewBinding.viewPager.setAdapter(tabAdapter);
        viewBinding.viewPager.setOffscreenPageLimit(fragmentList.size());

        viewBinding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTabUI(position == 0);
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_favourite;
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_movie:
                viewBinding.viewPager.setCurrentItem(0, true);
                updateTabUI(true);
                break;
            case R.id.btn_person:
                viewBinding.viewPager.setCurrentItem(1, true);
                updateTabUI(false);
                break;
            default:
                break;
        }
    }
    private void updateTabUI(boolean isMovieSelected) {
        // Movie tab
        viewBinding.btnMovie.setBackground(ContextCompat.getDrawable(this,
                isMovieSelected ? R.drawable.bg_tab_search_result : R.drawable.bg_tab_search_result_un_select));
        viewBinding.tvMovie.setTextColor(ContextCompat.getColor(this,
                isMovieSelected ? R.color.black : R.color.text));
        // Cast tab
        viewBinding.btnPerson.setBackground(ContextCompat.getDrawable(this,
                isMovieSelected ? R.drawable.bg_tab_search_result_un_select : R.drawable.bg_tab_search_result));
        viewBinding.tvPerson.setTextColor(ContextCompat.getColor(this,
                isMovieSelected ? R.color.text : R.color.black));
    }
}
