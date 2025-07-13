package com.movie_hub.android.ui.main.search;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.inputmethod.EditorInfo;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.FragmentSearchBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.search.result.SearchResultFragment;
import com.movie_hub.android.ui.main.search.suggestion.SearchSuggestionFragment;
import com.movie_hub.android.ui.main.search.topTrending.SearchTopTrendingFragment;

import eu.davidea.flexibleadapter.databinding.BR;

public class SearchFragment extends BaseFragment<FragmentSearchBinding, SearchViewModel> implements SystemBarColorProvider {

    private FragmentManager fragmentManager;

    private SearchTopTrendingFragment trendingFragment = new SearchTopTrendingFragment();
    private SearchSuggestionFragment suggestionFragment = new SearchSuggestionFragment();
    private SearchResultFragment resultFragment;

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
        fragmentManager = getChildFragmentManager();
        showTrending();
        setUpSearchBar();
        setupSearchEvents();
    }

    private void setupSearchEvents() {
        binding.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    showTrending();
                } else {
                    showSuggestion(keyword);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String keyword = binding.search.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    showResult(keyword);
                }
                return true;
            }
            return false;
        });
    }

    private void showTrending() {
        fragmentManager.beginTransaction()
                .replace(R.id.contentView, trendingFragment)
                .commit();
    }

    private void showSuggestion(String keyword) {
        suggestionFragment.setKeyWord(keyword);
        fragmentManager.beginTransaction()
                .replace(R.id.contentView, suggestionFragment)
                .commit();
    }

    private void showResult(String keyword) {
        resultFragment = new SearchResultFragment();
        Bundle bundle = new Bundle();
        bundle.putString("keyword", keyword);
        resultFragment.setArguments(bundle);

        fragmentManager.beginTransaction()
                .replace(R.id.contentView, resultFragment)
                .commit();
    }
    public void setUpSearchBar() {
        Drawable searchIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_search_bar);
        Drawable clearIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_clear);

        int sizeInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20, requireContext().getResources().getDisplayMetrics());

        if (searchIcon != null) {
            searchIcon.setBounds(0, 0, sizeInPx, sizeInPx);
        }
        if (clearIcon != null) {
            clearIcon.setBounds(0, 0, sizeInPx, sizeInPx);
        }

        binding.search.setCompoundDrawables(searchIcon, null, clearIcon, null);
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search;
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
}
