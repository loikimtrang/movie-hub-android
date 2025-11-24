package com.movie_hub.android.ui.main.search.result;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.movie_hub.android.R;
import com.movie_hub.android.databinding.FragmentSearchResultBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.movie.detail.fragment.CastFragment;
import com.movie_hub.android.ui.main.movie.detail.fragment.RecommendationFragment;
import com.movie_hub.android.ui.main.search.result.adpter.SearchResultTabAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.davidea.flexibleadapter.databinding.BR;

public class SearchResultFragment extends BaseFragment<FragmentSearchResultBinding, SearchResultViewModel> {

    private String keyword;
    private final List<Fragment> fragmentList = new ArrayList<>();

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        keyword = getArguments() != null ? getArguments().getString("keyword") : "";
        binding.setVm(viewModel);

        setUpTab();
    }
    public void setUpTab() {
        List<String> tabTitles = new ArrayList<>();
        fragmentList.clear();

        tabTitles.add(getString(R.string.movie));
        fragmentList.add(RecommendationFragment.newInstance(RecommendationFragment.TYPE_SEARCH, keyword, 0L));

        tabTitles.add(getString(R.string.cast));
        fragmentList.add(CastFragment.newInstance(CastFragment.TYPE_SEARCH, keyword));

        SearchResultTabAdapter tabAdapter = new SearchResultTabAdapter(requireActivity(), fragmentList);
        binding.viewPager.setAdapter(tabAdapter);

        binding.btnMovie.setOnClickListener(v -> {
            binding.viewPager.setCurrentItem(0, true);
            updateTabUI(true); // Movie selected
        });

        binding.btnCast.setOnClickListener(v -> {
            binding.viewPager.setCurrentItem(1, true);
            updateTabUI(false); // Cast selected
        });

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTabUI(position == 0);
            }
        });
    }
    private void updateTabUI(boolean isMovieSelected) {
        // Movie tab
        binding.btnMovie.setBackground(ContextCompat.getDrawable(requireContext(),
                isMovieSelected ? R.drawable.bg_tab_search_result : R.drawable.bg_tab_search_result_un_select));
        binding.tvMovie.setTextColor(ContextCompat.getColor(requireContext(),
                isMovieSelected ? R.color.black : R.color.text));
        // Cast tab
        binding.btnCast.setBackground(ContextCompat.getDrawable(requireContext(),
                isMovieSelected ? R.drawable.bg_tab_search_result_un_select : R.drawable.bg_tab_search_result));
        binding.tvCast.setTextColor(ContextCompat.getColor(requireContext(),
                isMovieSelected ? R.color.text : R.color.black));
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search_result;
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }
}
