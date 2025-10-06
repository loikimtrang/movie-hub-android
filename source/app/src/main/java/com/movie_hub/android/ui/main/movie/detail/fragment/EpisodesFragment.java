package com.movie_hub.android.ui.main.movie.detail.fragment;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.FragmentEpisodeBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;

import eu.davidea.flexibleadapter.databinding.BR;

public class EpisodesFragment extends BaseFragment<FragmentEpisodeBinding, EpisodesFragmentViewModel> {
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
        return R.layout.fragment_episode;
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }
}
