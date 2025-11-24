package com.movie_hub.android.ui.main.person.fragment;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.databinding.FragmentMoviesInvolvedBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;

public class MoviesInvolvedFragment extends BaseFragment<FragmentMoviesInvolvedBinding, MoviesInvolvedFragmentViewModel> {
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
        return R.layout.fragment_movies_involved;
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }
}
