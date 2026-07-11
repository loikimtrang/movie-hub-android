package com.movie_hub.android.ui.main.person;

import static com.movie_hub.android.ui.main.account.login.LoginActivity.REQUEST_CODE_LOGIN;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.favourite.CreateFavouriteRequest;
import com.movie_hub.android.data.model.api.response.person.PersonResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityPersonDetailBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.login.LoginActivity;
import com.movie_hub.android.ui.main.person.fragment.InformationFragment;
import com.movie_hub.android.ui.main.person.fragment.MoviesInvolvedFragment;
import com.movie_hub.android.ui.main.search.result.adpter.SearchResultTabAdapter;
import com.movie_hub.android.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;

public class PersonDetailActivity extends BaseActivity<ActivityPersonDetailBinding, PersonDetailViewModel> implements SystemBarColorProvider, View.OnClickListener {
    private final List<Fragment> fragmentList = new ArrayList<>();
    private ActivityResultLauncher<Intent> loginLauncher;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        String json = getIntent().getStringExtra("person");
        boolean isDirection = getIntent().getBooleanExtra("isDirection", false);
        PersonResponse person = GsonUtils.fromJson(json, PersonResponse.class);
        viewModel.isDirection = isDirection;
        if (viewModel.isDirection) {
            viewBinding.layoutBtn.setVisibility(View.GONE);
            viewBinding.btnFavourite.setVisibility(View.GONE);
        } else {
            viewBinding.layoutBtn.setVisibility(View.VISIBLE);
            viewBinding.btnFavourite.setVisibility(View.VISIBLE);
        }

        if (person!=null) {
            viewModel.person = person;
            if (viewModel.isLogin()) {
                CreateFavouriteRequest request = new CreateFavouriteRequest();
                request.setTargetId(viewModel.person.getId());
                request.setType(Constants.FAVOURITE_TYPE_PERSON);
                viewModel.getFavourite(request);
            }

            setUpView();
        }

        viewModel.favouriteResponseFirst.observe(this, response -> {
            if (response != null) {
                updateIconFavourite(true);
                viewModel.isFavourite = true;
            } else {
                viewModel.isFavourite = false;
                updateIconFavourite(false);
            }
        });

        loginLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        boolean loginSuccess = data != null && data.getBooleanExtra("login_success", false);
                        if (loginSuccess) {
                            new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.login_successful)).showMessage(this);
                            CreateFavouriteRequest request = new CreateFavouriteRequest();
                            request.setTargetId(viewModel.person.getId());
                            request.setType(Constants.FAVOURITE_TYPE_PERSON);
                            viewModel.getFavourite(request);
                        }
                    }
                }
        );
    }

    public void setUpView() {
        viewBinding.name.setText(viewModel.person.getName());

        String imageUrl = Constants.MEDIA_URL + viewModel.person.getAvatarPath();

        Glide.with(this)
                .load(imageUrl)
                .transform(new jp.wasabeef.glide.transformations.BlurTransformation(25, 3)) // radius=25, sampling=3
                .into(viewBinding.bgBlur);

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.place_holder_2_3)
                .error(R.drawable.place_holder_2_3)
                .into(viewBinding.image);

        
        setUpTab();
    }
    
    public void setUpTab() {
        List<String> tabTitles = new ArrayList<>();
        fragmentList.clear();

        tabTitles.add(getString(R.string.info_display));
        fragmentList.add(InformationFragment.newInstance());

        tabTitles.add(getString(R.string.acted_display));
        fragmentList.add(MoviesInvolvedFragment.newInstance(viewModel.person.getId()));

        SearchResultTabAdapter tabAdapter = new SearchResultTabAdapter(this, fragmentList);
        viewBinding.viewPager.setAdapter(tabAdapter);


        viewBinding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTabUI(position == 0);
            }
        });
    }

    private void updateTabUI(boolean isMovieSelected) {
        // Movie tab
        viewBinding.btnInf.setBackground(ContextCompat.getDrawable(this,
                isMovieSelected ? R.drawable.bg_tab_search_result : R.drawable.bg_tab_search_result_un_select));
        viewBinding.tvInf.setTextColor(ContextCompat.getColor(this,
                isMovieSelected ? R.color.black : R.color.text));
        // Cast tab
        viewBinding.btnMovie.setBackground(ContextCompat.getDrawable(this,
                isMovieSelected ? R.drawable.bg_tab_search_result_un_select : R.drawable.bg_tab_search_result));
        viewBinding.tvMovie.setTextColor(ContextCompat.getColor(this,
                isMovieSelected ? R.color.text : R.color.black));
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
        return R.color.bg_app;
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
                viewBinding.viewPager.setCurrentItem(0, true);
                updateTabUI(true); // Movie selected
                break;
            case R.id.btn_movie:
                viewBinding.viewPager.setCurrentItem(1, true);
                updateTabUI(false); // Movie selected
                break;
            case R.id.btn_favourite:
                if (viewModel.isLogin()) {
                    if (viewModel.isFavourite) {
                        viewModel.deleteFavorite(viewModel.favourite.getId());
                    } else {
                        addFavoritePerson();
                    }
                    updateIconFavourite(!viewModel.isFavourite);
                } else {
                    Intent it = new Intent(this, LoginActivity.class);
                    it.putExtra("login_from_other", "login_from_other");
                    loginLauncher.launch(it);
                }
            default:
                break;
        }
    }

    public void addFavoritePerson() {
        CreateFavouriteRequest request = new CreateFavouriteRequest();
        request.setType(Constants.FAVOURITE_TYPE_PERSON);
        request.setTargetId(viewModel.person.getId());
        viewModel.createFavorite(request);
    }

    public void updateIconFavourite(Boolean isFavorite) {
        viewModel.isFavourite = isFavorite;
        if (isFavorite) {
            viewBinding.icFavourite.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_heart_select)
            );
            viewBinding.tvFavourite.setTextColor(
                    ContextCompat.getColor(this, R.color.bg_select_icon)
            );
        } else {
            viewBinding.icFavourite.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_heart)
            );
            viewBinding.tvFavourite.setTextColor(
                    ContextCompat.getColor(this, R.color.text)
            );
        }
    }
}
