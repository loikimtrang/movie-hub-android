package com.movie_hub.android.ui.main.person;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.person.PersonResponse;
import com.movie_hub.android.databinding.ActivityPersonDetailBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.utils.GsonUtils;

public class PersonDetailActivity extends BaseActivity<ActivityPersonDetailBinding, PersonDetailViewModel> implements SystemBarColorProvider, View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        String json = getIntent().getStringExtra("person");
        PersonResponse person = GsonUtils.fromJson(json, PersonResponse.class);

        if (person!=null) {
            viewModel.person = person;
            setUpView();
        }
    }

    public void setUpView() {
        viewBinding.name.setText(viewModel.person.getOtherName());

        Glide.with(this)
                .load(Constants.MEDIA_URL + viewModel.person.getAvatarPath())
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(viewBinding.image);
    }
    @Override
    public int getLayoutId() {
        return R.layout.activity_person_detail;
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public int getStatusBarColor() {
        return R.color.account_header;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_app;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_inf:
                break;
            case R.id.btn_movie:
                break;
            default:
                break;
        }
    }
}
