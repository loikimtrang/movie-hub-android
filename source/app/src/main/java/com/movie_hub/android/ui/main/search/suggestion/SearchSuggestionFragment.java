package com.movie_hub.android.ui.main.search.suggestion;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.FragmentSearchSuggestionBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;

import eu.davidea.flexibleadapter.databinding.BR;
import lombok.Setter;

@Setter
public class SearchSuggestionFragment extends BaseFragment<FragmentSearchSuggestionBinding, SearchSuggestionViewModel> {

    public String keyWord;
    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search_suggestion;
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }
}
