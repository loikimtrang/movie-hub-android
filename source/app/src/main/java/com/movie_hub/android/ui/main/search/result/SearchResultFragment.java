package com.movie_hub.android.ui.main.search.result;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.FragmentSearchResultBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;

import eu.davidea.flexibleadapter.databinding.BR;

public class SearchResultFragment extends BaseFragment<FragmentSearchResultBinding, SearchResultViewModel> {

    private String keyword;
    @Override
    protected void performDataBinding() {
        binding.setF(this);
        keyword = getArguments() != null ? getArguments().getString("keyword") : "";
        binding.setVm(viewModel);
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
