package com.movie_hub.android.ui.base.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.R;
import com.movie_hub.android.di.component.DaggerFragmentComponent;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.di.module.FragmentModule;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.utils.DialogUtils;

import javax.inject.Inject;
import javax.inject.Named;


public abstract class BaseFragment <B extends ViewDataBinding,V extends BaseFragmentViewModel> extends Fragment {

    protected B binding;
    @Inject
    protected V viewModel;

    private Dialog progressDialog;


    @Named("access_token")
    @Inject
    protected String token;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        performDependencyInjection(getBuildComponent());
        binding = DataBindingUtil.inflate(inflater,getLayoutId(),container,false);
        binding.setVariable(getBindingVariable(),viewModel);
        performDataBinding();
        viewModel.setToken(token);
        viewModel.mErrorMessage.observe(getViewLifecycleOwner(),toastMessage -> {
            if (toastMessage!=null){
                toastMessage.showMessage(requireContext());
            }
        });
        viewModel.mIsLoading.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback(){

            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if(((ObservableBoolean)sender).get()){
                    showProgressbar(getResources().getString(R.string.msg_loading));
                }else{
                    hideProgress();
                }
            }
        });
//        setupFocusListeners(binding.getRoot());
//        setupTouchListenerToHideKeyboard(binding.getRoot());
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    protected abstract void performDataBinding();

    public abstract int getBindingVariable();

    protected abstract int getLayoutId();

    protected abstract void performDependencyInjection(FragmentComponent buildComponent);

    private FragmentComponent getBuildComponent(){
        return DaggerFragmentComponent.builder()
                .appComponent(((MVVMApplication) requireActivity().getApplication()).getAppComponent())
                .fragmentModule(new FragmentModule(this))
                .build();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchListenerToHideKeyboard(View rootView) {
        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View focused = requireActivity().getCurrentFocus();
                if (focused instanceof EditText) {
                    Rect outRect = new Rect();
                    focused.getGlobalVisibleRect(outRect);
                    if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        focused.clearFocus();
                        ((BaseActivity<?, ?>) requireActivity()).hideKeyboard();
                    }
                }
            }
            return false;
        });
    }
    private void setupFocusListeners(View root) {
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                setupFocusListeners(group.getChildAt(i));
            }
        } else if (root instanceof android.widget.EditText) {
            root.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    ((BaseActivity<?, ?>) requireActivity()).hideKeyboard();
                }
            });
        }
    }

    public void showProgressbar(String msg){
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        progressDialog = DialogUtils.createDialogLoading(requireContext(), msg);
        progressDialog.show();
    }

    public void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this instanceof SystemBarColorProvider && getActivity() instanceof BaseActivity) {
            BaseActivity<?, ?> baseActivity = (BaseActivity<?, ?>) getActivity();
            SystemBarColorProvider provider = (SystemBarColorProvider) this;
            baseActivity.setSystemBarsColor(
                    provider.getStatusBarColor(),
                    provider.getNavigationBarColor()
            );
        }
    }
}
