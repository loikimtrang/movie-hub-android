package com.movie_hub.android.ui.main.account;

import com.movie_hub.android.R;
import com.movie_hub.android.databinding.FragmentAccountBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;

import eu.davidea.flexibleadapter.databinding.BR;

public class AccountFragment extends BaseFragment<FragmentAccountBinding, AccountViewModel> {
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
        return R.layout.fragment_account;
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }
}
