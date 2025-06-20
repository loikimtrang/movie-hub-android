package com.movie_hub.android.ui.main.schedule;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.FragmentScheduleBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;

import eu.davidea.flexibleadapter.databinding.BR;

public class ScheduleFragment extends BaseFragment<FragmentScheduleBinding, ScheduleViewModel> {
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
        return R.layout.fragment_schedule;
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }
}
