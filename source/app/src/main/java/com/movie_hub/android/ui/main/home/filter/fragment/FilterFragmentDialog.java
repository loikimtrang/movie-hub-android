package com.movie_hub.android.ui.main.home.filter.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.request.movie.MovieRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.AgeRatingRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.CountryRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.LanguageRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.TypeMovieRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.YearReleaseRequest;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.databinding.LayoutDialogFilterBinding;
import com.movie_hub.android.ui.main.home.filter.adapter.FilterAgeRatingAdapter;
import com.movie_hub.android.ui.main.home.filter.adapter.FilterCategoryItemAdapter;
import com.movie_hub.android.ui.main.home.filter.adapter.FilterCountryItemAdapter;
import com.movie_hub.android.ui.main.home.filter.adapter.FilterLanguageItemAdapter;
import com.movie_hub.android.ui.main.home.filter.adapter.FilterTypeItemAdapter;
import com.movie_hub.android.ui.main.home.filter.adapter.FilterYearReleaseAdapter;
import com.movie_hub.android.ui.main.home.filter.adapter.OnFilterClick;
import com.movie_hub.android.ui.main.home.filter.model.FilterTypeModel;
import com.movie_hub.android.ui.main.search.topTrending.FlexSpacingItemDecoration;
import com.movie_hub.android.utils.FileUtils;
import com.movie_hub.android.utils.GsonUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterFragmentDialog extends DialogFragment implements OnFilterClick {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutDialogFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            window.setWindowAnimations(R.style.DialogAnimation);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
    private LayoutDialogFilterBinding binding;
    private FilterDialogCallback filterDialogCallback;
    private MovieRequest movieRequest;

    private FilterTypeModel filterTypeModel;
    private List<CategoryResponse> categoryResponseList;
    private List<CountryRequest> countryRequestList;
    private List<LanguageRequest> languageRequestList;
    private List<TypeMovieRequest> typeMovieRequestList;
    private List<AgeRatingRequest> ageRatingRequestList;
    private List<YearReleaseRequest> yearReleaseRequestList;
    private FilterAgeRatingAdapter filterAgeRatingAdapter;
    private FilterCategoryItemAdapter filterCategoryItemAdapter;
    private FilterCountryItemAdapter filterCountryItemAdapter;
    private FilterLanguageItemAdapter filterLanguageItemAdapter;
    private FilterTypeItemAdapter filterTypeItemAdapter;
    private FilterYearReleaseAdapter filterYearReleaseAdapter;

    public interface FilterDialogCallback {
        void onFilterClick(MovieRequest request);
    }
    public FilterFragmentDialog(FilterDialogCallback callback,
                                MovieRequest request,
                                String categoryResponseJsonList,
                                String countryRequestJsonList,
                                String languageRequestJsonList,
                                String typeMovieRequestJsonList,
                                String ageRatingRequestJsonList,
                                String yearReleaseRequestJsonList,
                                FilterTypeModel filterTypeModel) {
        this.filterDialogCallback = callback;
        this.movieRequest = request;

        this.categoryResponseList = GsonUtils.fromJsonToList(categoryResponseJsonList, CategoryResponse.class);
        this.countryRequestList = GsonUtils.fromJsonToList(countryRequestJsonList, CountryRequest.class);
        this.languageRequestList = GsonUtils.fromJsonToList(languageRequestJsonList, LanguageRequest.class);
        this.typeMovieRequestList = GsonUtils.fromJsonToList(typeMovieRequestJsonList, TypeMovieRequest.class);
        this.ageRatingRequestList = GsonUtils.fromJsonToList(ageRatingRequestJsonList, AgeRatingRequest.class);
        this.yearReleaseRequestList = GsonUtils.fromJsonToList(yearReleaseRequestJsonList, YearReleaseRequest.class);
        this.filterTypeModel = filterTypeModel;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpAdapter();
        setupFilterToggleLogic();
        updateFilterUI();
        binding.btnFilter.setOnClickListener(v -> {
            filterDialogCallback.onFilterClick(movieRequest);
            dismiss();
        });

        binding.btnClose.setOnClickListener(v -> {
            dismiss();
        });

        binding.layoutSetting.setOnClickListener(v -> {
            dismiss();
        });

        if (filterTypeModel.getType() != Constants.TYPE_GENRE) {
            binding.layoutType.setVisibility(View.GONE);
        }
    }

    public void setUpAdapter() {
        int a = getResources().getDimensionPixelSize(R.dimen._6sdp);

        filterCategoryItemAdapter = new FilterCategoryItemAdapter(this, getContext());
        filterAgeRatingAdapter = new FilterAgeRatingAdapter(this, getContext());
        filterCountryItemAdapter = new FilterCountryItemAdapter(this, getContext());
        filterLanguageItemAdapter = new FilterLanguageItemAdapter(this, getContext());
        filterTypeItemAdapter = new FilterTypeItemAdapter(this, getContext());
        filterYearReleaseAdapter = new FilterYearReleaseAdapter(this, getContext());

        binding.rvFilterCategory.setLayoutManager(createFlexLayout(getContext()));
        binding.rvFilterCategory.addItemDecoration(new FlexSpacingItemDecoration(a));
        binding.rvFilterCategory.setAdapter(filterCategoryItemAdapter);

        binding.rvFilterAgeRating.setLayoutManager(createFlexLayout(getContext()));
        binding.rvFilterAgeRating.addItemDecoration(new FlexSpacingItemDecoration(a));
        binding.rvFilterAgeRating.setAdapter(filterAgeRatingAdapter);

        binding.rvFilterCountry.setLayoutManager(createFlexLayout(getContext()));
        binding.rvFilterCountry.addItemDecoration(new FlexSpacingItemDecoration(a));
        binding.rvFilterCountry.setAdapter(filterCountryItemAdapter);

        binding.rvFilterLanguage.setLayoutManager(createFlexLayout(getContext()));
        binding.rvFilterLanguage.addItemDecoration(new FlexSpacingItemDecoration(a));
        binding.rvFilterLanguage.setAdapter(filterLanguageItemAdapter);

        binding.rvFilterType.setLayoutManager(createFlexLayout(getContext()));
        binding.rvFilterType.addItemDecoration(new FlexSpacingItemDecoration(a));
        binding.rvFilterType.setAdapter(filterTypeItemAdapter);

        binding.rvFilterYearRelease.setLayoutManager(createFlexLayout(getContext()));
        binding.rvFilterYearRelease.addItemDecoration(new FlexSpacingItemDecoration(a));
        binding.rvFilterYearRelease.setAdapter(filterYearReleaseAdapter);

        bindFilterDataToAdapters();
    }
    private FlexboxLayoutManager createFlexLayout(Context context) {
        FlexboxLayoutManager layout = new FlexboxLayoutManager(context);
        layout.setFlexDirection(FlexDirection.ROW);
        layout.setFlexWrap(FlexWrap.WRAP);
        layout.setJustifyContent(JustifyContent.FLEX_START);
        layout.setAlignItems(AlignItems.FLEX_START);
        return layout;
    }

    public void bindFilterDataToAdapters() {
        filterCategoryItemAdapter.setData(categoryResponseList);
        filterCountryItemAdapter.setData(countryRequestList);
        filterLanguageItemAdapter.setData(languageRequestList);
        filterTypeItemAdapter.setData(typeMovieRequestList);
        filterAgeRatingAdapter.setData(ageRatingRequestList);
        filterYearReleaseAdapter.setData(yearReleaseRequestList);
    }

    private void setupFilterToggleLogic() {
        Map<View, Pair<View, ImageView>> toggleMap = new HashMap<>();

        toggleMap.put(binding.btnFilterCountry, new Pair<>(binding.rvFilterCountry, binding.arrowCountry));
        toggleMap.put(binding.btnFilterType, new Pair<>(binding.rvFilterType, binding.arrowType));
        toggleMap.put(binding.btnFilterCategory, new Pair<>(binding.rvFilterCategory, binding.arrowCate));
        toggleMap.put(binding.btnFilterYearRelease, new Pair<>(binding.rvFilterYearRelease, binding.arrowYearRelease));
        toggleMap.put(binding.btnFilterAgeRating, new Pair<>(binding.rvFilterAgeRating, binding.arrowAgeRating));
        toggleMap.put(binding.btnFilterLanguage, new Pair<>(binding.rvFilterLanguage, binding.arrowLanguage));

        for (Map.Entry<View, Pair<View, ImageView>> entry : toggleMap.entrySet()) {
            View btn = entry.getKey();
            View targetRv = entry.getValue().first;
            ImageView arrow = entry.getValue().second;

            btn.setOnClickListener(v -> {
                boolean isVisible = targetRv.getVisibility() == View.VISIBLE;

                // Hide all RVs and reset arrows
                for (Pair<View, ImageView> pair : toggleMap.values()) {
                    pair.first.setVisibility(View.GONE);
                    pair.second.setRotation(90); // mặc định mũi tên quay ngang
                }

                // Toggle clicked item
                if (!isVisible) {
                    targetRv.setVisibility(View.VISIBLE);
                    arrow.setRotation(270); // xoay xuống
                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateFilterUI() {

        // ==== CATEGORY ====
        if (movieRequest.getCategoryIds() != null && !movieRequest.getCategoryIds().isEmpty()) {
            binding.tvCate.setText(getString(R.string.selected) + " " + movieRequest.getCategoryIds().size());
        } else {
            binding.tvCate.setText(getString(R.string.all));
        }

        // ==== COUNTRY ====
        if (movieRequest.getCountryRequest() != null) {
            binding.tvCountry.setText(movieRequest.getCountryRequest().getLabel());
        } else {
            binding.tvCountry.setText(getString(R.string.all));
        }

        // ==== LANGUAGE ====
        if (movieRequest.getLanguageRequest() != null) {
            binding.tvLanguage.setText(movieRequest.getLanguageRequest().getLabel());
        } else {
            binding.tvLanguage.setText(getString(R.string.all));
        }

        // ==== AGE RATING ====
        if (movieRequest.getAgeRatingRequest() != null) {
            binding.tvAgeRating.setText(movieRequest.getAgeRatingRequest().getLabel());
        } else {
            binding.tvAgeRating.setText(getString(R.string.all));
        }

        // ==== TYPE ====
        if (movieRequest.getTypeMovieRequest() != null) {
            binding.tvType.setText(movieRequest.getTypeMovieRequest().getLabel());
        } else {
            binding.tvType.setText(getString(R.string.all));
        }

        // ==== YEAR RELEASE ====
        if (movieRequest.getYearReleaseRequest() != null) {
            binding.tvYearRelease.setText(String.valueOf(movieRequest.getYearReleaseRequest().getReleaseYear()));
        } else {
            binding.tvYearRelease.setText(getString(R.string.all));
        }
    }

    @Override
    public void onCategoryFilterClick(CategoryResponse categoryResponse) {
        if (categoryResponseList == null || categoryResponse == null) return;

        // Bảo đảm list luôn tồn tại
        if (movieRequest.getCategoryIds() == null) {
            movieRequest.setCategoryIds(new ArrayList<>());
        }

        if (movieRequest.getCategoryRequest() == null) {
            movieRequest.setCategoryRequest(new ArrayList<>());
        }

        if (categoryResponse.isSelect()) {
            // Nếu chưa có thì thêm vào
            if (!movieRequest.getCategoryIds().contains(categoryResponse.getId())) {
                movieRequest.getCategoryIds().add(categoryResponse.getId());
                movieRequest.getCategoryRequest().add(categoryResponse);
            }
        } else {
            // Nếu đã có mà user bỏ chọn thì xóa ra
            movieRequest.getCategoryIds().remove(categoryResponse.getId());
            movieRequest.getCategoryRequest().remove(categoryResponse);
        }

        // Optional: sync list object
        updateFilterUI();
    }


    @Override
    public void onCountryFilterClick(CountryRequest request) {
        if (countryRequestList == null || request == null) return;

        boolean wasSelected = request.isSelect();

        for (CountryRequest r : countryRequestList) {
            r.setSelect(false);
        }

        if (wasSelected) {
            request.setSelect(true);
            movieRequest.setCountry(request.getValue());
            movieRequest.setCountryRequest(request);
        } else {
            // Không chọn gì cả
            movieRequest.setCountry(null);
            movieRequest.setCountryRequest(null);
        }

        updateFilterUI();
    }

    @Override
    public void onLanguageFilterClick(LanguageRequest request) {
        if (languageRequestList == null || request == null) return;

        boolean wasSelected = request.isSelect();

        for (LanguageRequest r : languageRequestList) {
            r.setSelect(false);
        }

        if (wasSelected) {
            request.setSelect(true);
            movieRequest.setLanguage(request.getValue());
            movieRequest.setLanguageRequest(request);
        } else {
            movieRequest.setLanguage(null);
            movieRequest.setLanguageRequest(null);
        }

        updateFilterUI();
    }


    @Override
    public void onAgeRatingFilterClick(AgeRatingRequest request) {
        if (ageRatingRequestList == null || request == null) return;

        boolean wasSelected = request.isSelect();

        for (AgeRatingRequest r : ageRatingRequestList) {
            r.setSelect(false);
        }

        if (wasSelected) {
            request.setSelect(true);
            movieRequest.setAgeRating(request.getType());
            movieRequest.setAgeRatingRequest(request);
        } else {
            movieRequest.setAgeRating(null);
            movieRequest.setAgeRatingRequest(null);
        }

        updateFilterUI();
    }


    @Override
    public void onTypeFilterClick(TypeMovieRequest request) {
        if (typeMovieRequestList == null || request == null) return;

        boolean wasSelected = request.isSelect();

        for (TypeMovieRequest r : typeMovieRequestList) {
            r.setSelect(false);
        }

        if (wasSelected) {
            request.setSelect(true);
            movieRequest.setType(request.getType());
            movieRequest.setTypeMovieRequest(request);
        } else {
            movieRequest.setType(null);
            movieRequest.setTypeMovieRequest(null);
        }

        updateFilterUI();
    }

    @Override
    public void onYearFilterClick(YearReleaseRequest request) {
        if (yearReleaseRequestList == null || request == null) return;

        boolean wasSelected = request.isSelect();

        for (YearReleaseRequest r : yearReleaseRequestList) {
            r.setSelect(false);
        }

        if (wasSelected) {
            request.setSelect(true);
            movieRequest.setReleaseYear(request.getReleaseYear());
            movieRequest.setYearReleaseRequest(request);
        } else {
            movieRequest.setReleaseYear(null);
            movieRequest.setYearReleaseRequest(null);
        }

        updateFilterUI();
    }

}
