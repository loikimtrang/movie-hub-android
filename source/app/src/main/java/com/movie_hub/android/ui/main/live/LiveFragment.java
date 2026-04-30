package com.movie_hub.android.ui.main.live;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.databinding.FragmentLiveBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.live.adapter.RoomAdapter;

public class LiveFragment extends BaseFragment<FragmentLiveBinding, LiveViewModel> {
    @Override
    protected void performDataBinding() {
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_live;
    }

    public RoomAdapter roomAdapter;

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }
}
