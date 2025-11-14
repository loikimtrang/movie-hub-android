package com.movie_hub.android.ui.main.movie.detail.fragment;

import android.annotation.SuppressLint;

import androidx.lifecycle.ViewModelProvider;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;
import com.movie_hub.android.databinding.FragmentEpisodeBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailViewModel;
import com.movie_hub.android.ui.main.movie.detail.dialog.ChooseSeasonBottomSheetDialog;
import com.movie_hub.android.utils.ClickUtils;
import com.movie_hub.android.utils.GsonUtils;

import java.util.Objects;

import eu.davidea.flexibleadapter.databinding.BR;

public class EpisodesFragment extends BaseFragment<FragmentEpisodeBinding, EpisodesFragmentViewModel> implements ChooseSeasonBottomSheetDialog.ChooseSeasonBottomSheetCallback {
    private MovieDetailViewModel sharedViewModel;
    private MovieResponse movieDetail;

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(MovieDetailViewModel.class);
        movieDetail = sharedViewModel.getMovieDetails().getValue();
        setUpView();
    }

    @SuppressLint("SetTextI18n")
    public void setUpView() {
        binding.tvSeason.setText(getContext().getString(R.string.season) + " " + (movieDetail.getSeasons().size()));
    }
    public void showSeasonListBottomSheet() {
        ClickUtils.debounceClick(binding.btnChooseSeason);
        if (movieDetail.getType() == Constants.TYPE_MOVIE_SINGLE || movieDetail.getType() == Constants.TYPE_MOVIE_TRAILER) return;
        ChooseSeasonBottomSheetDialog sheet = new ChooseSeasonBottomSheetDialog(Objects.requireNonNull(getContext()), this, GsonUtils.toJson(Objects.requireNonNull(sharedViewModel.getMovieDetails().getValue()).getSeasons()));
        sheet.show();
        Objects.requireNonNull(sheet.getWindow()).getDecorView().post(sheet::setupWindow);
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_episode;
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSeasonClicked(SeasonResponse selectedSeason) {
        MovieResponse currentMovie = sharedViewModel.getMovieDetails().getValue();
        if (currentMovie == null) return;

        MovieResponse updatedMovie = GsonUtils.fromJson(
                GsonUtils.toJson(currentMovie),
                MovieResponse.class
        );

        for (SeasonResponse s : updatedMovie.getSeasons()) {
            s.setSelect(s.getId().equals(selectedSeason.getId()));
        }

        sharedViewModel.setMovieDetails(updatedMovie);
    }
}
