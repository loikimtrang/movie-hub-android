package com.movie_hub.android.ui.main.home.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Build;
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

import com.bumptech.glide.Glide;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.databinding.LayoutMovieDetailBinding;
import com.movie_hub.android.ui.main.home.OnMovieClickCallback;
import com.movie_hub.android.ui.main.movie.detail.adapter.TagCategoryAdapter;
import com.movie_hub.android.ui.main.search.topTrending.FlexSpacingItemDecoration;
import com.movie_hub.android.utils.DisplayUtils;

public class MovieDetailDialogFragment extends DialogFragment {

    private LayoutMovieDetailBinding binding;
    private TagCategoryAdapter tagCategoryAdapter;
    private MovieResponse movieResponse;
    public OnMovieClickCallback callback;
    public MovieDetailDialogFragment(MovieResponse movieResponse, OnMovieClickCallback callback) {
        this.movieResponse = movieResponse;
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = LayoutMovieDetailBinding.inflate(inflater, container, false);
        
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.layoutMain.setOnClickListener(v -> dismiss());

        tagCategoryAdapter = new TagCategoryAdapter();
        FlexboxLayoutManager layout = new FlexboxLayoutManager(getContext());
        layout.setFlexDirection(FlexDirection.ROW);
        layout.setFlexWrap(FlexWrap.WRAP);
        layout.setJustifyContent(JustifyContent.FLEX_START);
        layout.setAlignItems(AlignItems.FLEX_START);

        binding.rvTag.setLayoutManager(layout);
        int a = getResources().getDimensionPixelSize(R.dimen._6sdp);
        binding.rvTag.addItemDecoration(new FlexSpacingItemDecoration(a));
        binding.rvTag.setAdapter(tagCategoryAdapter);

        tagCategoryAdapter.setData(movieResponse.getCategories());

        binding.tvName.setText(movieResponse.getTitle());
        binding.tvNameOr.setText(movieResponse.getOriginalTitle());
        binding.ageRating.setText(DisplayUtils.displayAgeRating(movieResponse.getAgeRating()));
        binding.dateRelease.setText(DisplayUtils.getYearFromReleaseDate(movieResponse.getReleaseDate()));
        binding.description.setText(movieResponse.getDescription());

        Glide.with(binding.getRoot().getContext())
                .load(movieResponse.getThumbnailUrl())
                .placeholder(R.drawable.place_holder_16_9)
                .error(R.drawable.place_holder_16_9)
                .into(binding.imgMovie);

        if (movieResponse.getImageTitleUrl() != null && !movieResponse.getImageTitleUrl().isEmpty()) {
            Glide.with(binding.getRoot().getContext())
                    .load(movieResponse.getImageTitleUrl())
                    .into(binding.imgTitle);
        }

        binding.btnBannerInf.setOnClickListener(v -> {
            if (callback == null) return;
            callback.onMovieClick(movieResponse);
            dismiss();
        });

        binding.btnBannerWatchNow.setOnClickListener(v -> {
            if (callback == null) return;
            callback.onWatchMovieClick(movieResponse);
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();

            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            window.setBackgroundDrawableResource(android.R.color.transparent);

            window.setWindowAnimations(R.style.DialogAnimationCenter);

            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }
}