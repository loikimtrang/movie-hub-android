package com.movie_hub.android.ui.main.search.result;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.movie_hub.android.R;
import com.movie_hub.android.databinding.FragmentSearchResultBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.movie.detail.adapter.MovieDetailTabAdapter;
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

        RecommendationFragment.DISPLAY_FROM.setValue(CastFragment.TYPE_SEARCH);
        RecommendationFragment.KEY_WORD.setValue(keyword);
        tabTitles.add(getString(R.string.movie));
        fragmentList.add(new RecommendationFragment());

        tabTitles.add(getString(R.string.cast));
        CastFragment.DISPLAY_FROM.setValue(CastFragment.TYPE_SEARCH);
        CastFragment.KEY_WORD.setValue(keyword);
        fragmentList.add(new CastFragment());

        SearchResultTabAdapter tabAdapter = new SearchResultTabAdapter(requireActivity(), fragmentList);
        binding.viewPager.setAdapter(tabAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    View tabView = LayoutInflater.from(requireContext())
                            .inflate(R.layout.item_tab_search_result, binding.tabLayout, false);

                    TextView tabText = tabView.findViewById(R.id.tab_text);
                    tabText.setText(tabTitles.get(position));
                    tab.setCustomView(tabView);
                }
        ).attach();

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView tabText = Objects.requireNonNull(tab.getCustomView()).findViewById(R.id.tab_text);
                FrameLayout frameLayout = tab.getCustomView().findViewById(R.id.l_tab);
                tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                frameLayout.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_search_result));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
                FrameLayout frameLayout = tab.getCustomView().findViewById(R.id.l_tab);
                tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_cate));
                frameLayout.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_tab_search_result_un_select));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
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
