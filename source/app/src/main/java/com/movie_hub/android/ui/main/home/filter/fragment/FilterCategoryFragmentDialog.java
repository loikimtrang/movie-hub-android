package com.movie_hub.android.ui.main.home.filter.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.request.movie.MovieRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.AgeRatingRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.CountryRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.LanguageRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.TypeMovieRequest;
import com.movie_hub.android.data.model.api.request.movie.filter.YearReleaseRequest;
import com.movie_hub.android.data.model.api.response.category.CategoryResponse;
import com.movie_hub.android.databinding.LayoutDialogFilterCategoryBinding;
import com.movie_hub.android.ui.main.home.filter.adapter.FilterCategoryItemAdapter;
import com.movie_hub.android.ui.main.home.filter.adapter.OnFilterClick;
import com.movie_hub.android.ui.main.search.topTrending.FlexSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class FilterCategoryFragmentDialog extends DialogFragment implements OnFilterClick {
    private LayoutDialogFilterCategoryBinding binding;
    private FilterCategoryDialogCallback filterCategoryDialogCallback;
    private List<CategoryResponse> categoryResponseList;
    private FilterCategoryItemAdapter filterCategoryItemAdapter;
    private MovieRequest movieRequest = new MovieRequest();
    @SuppressLint("SetTextI18n")
    @Override
    public void onCategoryFilterClick(CategoryResponse categoryResponse) {
        List<Long> categoryIds;
        List<CategoryResponse> list;
        if (movieRequest.getCategoryIds() == null) {
            categoryIds = new ArrayList<>();
        } else {
            categoryIds = new ArrayList<>(movieRequest.getCategoryIds());
        }

        if (movieRequest.getCategoryRequest() == null) {
            list = new ArrayList<>();
        } else {
            list = new ArrayList<>(movieRequest.getCategoryRequest());
        }

        if (categoryResponse.isSelect()) {
            if (!categoryIds.contains(categoryResponse.getId())) {
                categoryIds.add(categoryResponse.getId());
                list.add(categoryResponse);
            }
        } else {
            categoryIds.remove(categoryResponse.getId());
            list.remove(categoryResponse);
        }

        if (categoryIds.isEmpty()) {
            binding.tvCate.setVisibility(View.INVISIBLE);
        } else {
            binding.tvCate.setText(getString(R.string.selected) + " " + categoryIds.size());
            binding.tvCate.setVisibility(View.VISIBLE);
        }
        movieRequest.setCategoryIds(categoryIds);
        movieRequest.setCategoryRequest(list);
    }
    @Override
    public void onCountryFilterClick(CountryRequest request) {

    }

    @Override
    public void onLanguageFilterClick(LanguageRequest request) {

    }

    @Override
    public void onAgeRatingFilterClick(AgeRatingRequest request) {

    }

    @Override
    public void onTypeFilterClick(TypeMovieRequest request) {

    }

    @Override
    public void onYearFilterClick(YearReleaseRequest request) {

    }

    public interface FilterCategoryDialogCallback {
        void onFilterCategoryClick(MovieRequest request);
        void onDialogDismiss();
    }
    public FilterCategoryFragmentDialog(FilterCategoryDialogCallback callback, List<CategoryResponse> categoryResponseList) {
        this.filterCategoryDialogCallback = callback;
        this.categoryResponseList = categoryResponseList;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutDialogFilterCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpAdapter();

        binding.btnFilter.setOnClickListener(v -> {
            if (movieRequest.getCategoryIds().isEmpty()) {
                dismiss();
            } else {
                filterCategoryDialogCallback.onFilterCategoryClick(movieRequest);
                dismiss();
            }
        });

        binding.btnClose.setOnClickListener(v -> {
            dismiss();
        });
    }
    
    public void setUpAdapter() {
        filterCategoryItemAdapter = new FilterCategoryItemAdapter(this, getContext());
        FlexboxLayoutManager layout = new FlexboxLayoutManager(getContext());
        layout.setFlexDirection(FlexDirection.ROW);
        layout.setFlexWrap(FlexWrap.WRAP);
        layout.setJustifyContent(JustifyContent.FLEX_START);
        layout.setAlignItems(AlignItems.FLEX_START);

        binding.rvFilterCategory.setLayoutManager(layout);
        int a = getResources().getDimensionPixelSize(R.dimen._6sdp);
        binding.rvFilterCategory.addItemDecoration(new FlexSpacingItemDecoration(a));
        binding.rvFilterCategory.setAdapter(filterCategoryItemAdapter);

        filterCategoryItemAdapter.setData(categoryResponseList);
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

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        filterCategoryDialogCallback.onDialogDismiss();
        super.onDismiss(dialog);
    }
}
