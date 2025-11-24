package com.movie_hub.android.ui.main.movie.detail.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;
import com.movie_hub.android.databinding.LayoutBottomSheetChooseSeasonBinding;
import com.movie_hub.android.databinding.LayoutBottomSheetInformationMovieBinding;
import com.movie_hub.android.ui.main.movie.detail.adapter.SeasonItemListAdapter;
import com.movie_hub.android.utils.DisplayUtils;
import com.movie_hub.android.utils.GsonUtils;
import com.movie_hub.android.utils.HtmlUtils;

import java.util.List;

public class InformationMovieBottomSheetDialog extends BottomSheetDialog  {

    private LayoutBottomSheetInformationMovieBinding binding;
    private MovieResponse movieDetails;
    public InformationMovieBottomSheetDialog(@NonNull Context context, MovieResponse movieDetails) {
        super(context);
        this.movieDetails = movieDetails;
        init();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        binding = LayoutBottomSheetInformationMovieBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());

        binding.description.setText(HtmlUtils.convertPtoStrong(movieDetails.getDescription()));
        binding.country.setText(movieDetails.getCountry());
        binding.ageRating.setText(DisplayUtils.displayAgeRatingDescription(getContext(), movieDetails.getAgeRating()));

        if (movieDetails.getType() == Constants.TYPE_MOVIE_SINGLE) {
            binding.yearRelease.setText(DisplayUtils.getYearFromReleaseDate(movieDetails.getReleaseDate()));
            binding.titleDuration.setText(getContext().getString(R.string.duration));
            binding.duration.setText(DisplayUtils.displayTimeFromSeconds(getContext(), movieDetails.getSeasons().get(0).getVideo().getDuration()));
        } if (movieDetails.getType() == Constants.TYPE_MOVIE_SERIES) {
            if (movieDetails.getSeasons() != null && !movieDetails.getSeasons().isEmpty()) {
                SeasonResponse lastSeason = movieDetails.getSeasons().get(movieDetails.getSeasons().size() - 1);
                binding.yearRelease.setText(DisplayUtils.getYearFromReleaseDate(lastSeason.getReleaseDate()));

                if (movieDetails.getSeasons().size() == 1) {
                    binding.titleDuration.setText(getContext().getString(R.string.episode_if));
                    binding.duration.setText(String.valueOf(
                            movieDetails.getSeasons().get(0).getEpisodes().size()
                    ));

                } else {
                    binding.titleDuration.setText(getContext().getString(R.string.season_if));
                    binding.duration.setText(String.valueOf(
                            movieDetails.getSeasons().size()
                    ));

                }
            }
        }
        

        binding.btnClose.setOnClickListener(v -> dismiss());
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @SuppressLint("NewApi")
    public void setupWindow() {
        Window window = getWindow();
        if (window == null) return;

        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(getContext().getColor(R.color.bg_season));


        View bottomSheet = findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            bottomSheet.setBackgroundColor(Color.TRANSPARENT);
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
        }
    }
}