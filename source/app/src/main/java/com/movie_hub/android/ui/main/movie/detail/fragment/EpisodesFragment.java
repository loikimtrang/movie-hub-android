package com.movie_hub.android.ui.main.movie.detail.fragment;

import android.annotation.SuppressLint;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;
import com.movie_hub.android.databinding.FragmentEpisodeBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailActivity;
import com.movie_hub.android.ui.main.movie.detail.MovieDetailViewModel;
import com.movie_hub.android.ui.main.movie.detail.adapter.EpisodeItemListAdapter;
import com.movie_hub.android.ui.main.movie.detail.dialog.ChooseSeasonBottomSheetDialog;
import com.movie_hub.android.utils.ClickUtils;
import com.movie_hub.android.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.databinding.BR;

public class EpisodesFragment extends BaseFragment<FragmentEpisodeBinding, EpisodesFragmentViewModel> implements ChooseSeasonBottomSheetDialog.ChooseSeasonBottomSheetCallback,
    EpisodeItemListAdapter.OnEpisodeClickListener {
    private MovieDetailViewModel sharedViewModel;
    private MovieResponse movieDetail;
    private List<MovieItemResponse> episodes;
    private EpisodeItemListAdapter adapter;

    private int seasonIndexSelect = 0;

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(MovieDetailViewModel.class);
        movieDetail = sharedViewModel.movieDetails;
        episodes = movieDetail.getSeasons().get(movieDetail.getSeasons().size() - 1).getEpisodes();
        setUpView();

        sharedViewModel.movieDetailsTracking.observe(getViewLifecycleOwner(), tracking -> {
            if (tracking == null || !viewModel.isLogin()) return;

            sharedViewModel.applyWatchHistory(tracking);

            MovieResponse movie = sharedViewModel.movieDetails;
            List<SeasonResponse> seasons = movie.getSeasons();

            if (seasons != null && seasonIndexSelect >= 0 && seasonIndexSelect < seasons.size()) {
                List<MovieItemResponse> originalEpisodes = seasons.get(seasonIndexSelect).getEpisodes();

                List<MovieItemResponse> cloned = new ArrayList<>();
                for (MovieItemResponse item : originalEpisodes) {
                    cloned.add(new MovieItemResponse(item));
                }

                adapter.setData(cloned);
            }
        });

    }

    @SuppressLint("SetTextI18n")
    public void setUpView() {
        binding.tvSeason.setText(getContext().getString(R.string.season) + " " + (movieDetail.getSeasons().size()));

        adapter = new EpisodeItemListAdapter(this, getContext());
        binding.rvEpisode.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvEpisode.setAdapter(adapter);
        adapter.setData(episodes);
    }
    public void showSeasonListBottomSheet() {
        ClickUtils.debounceClick(binding.btnChooseSeason);

        if (movieDetail == null) return;
        int type = movieDetail.getType();
        if (type == Constants.TYPE_MOVIE_SINGLE || type == Constants.TYPE_MOVIE_TRAILER)
            return;

        // Lấy seasons, nếu null thì cho list rỗng
        List<SeasonResponse> seasons = null;
        if (sharedViewModel.movieDetails != null) {
            seasons = sharedViewModel.movieDetails.getSeasons();
        }

        if (seasons == null || seasons.isEmpty()) {
            Toast.makeText(getContext(), "Không có season để hiển thị", Toast.LENGTH_SHORT).show();
            return;
        }

        ChooseSeasonBottomSheetDialog sheet =
                new ChooseSeasonBottomSheetDialog(
                        requireContext(),
                        this,
                        GsonUtils.toJson(seasons)
                );

        sheet.show();

        if (sheet.getWindow() != null) {
            sheet.getWindow().getDecorView().post(sheet::setupWindow);
        }
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
        MovieResponse currentMovie = sharedViewModel.movieDetails;
        if (currentMovie == null) return;

        for (SeasonResponse s : currentMovie.getSeasons()) {
            s.setSelect(s.getId().equals(selectedSeason.getId()));
            if (s.isSelect()) {
                episodes.clear();
                episodes.addAll(s.getEpisodes());
                adapter.setData(episodes);
                binding.tvSeason.setText(getString(R.string.season) + " " +
                        (currentMovie.getSeasons().indexOf(s) + 1));

                seasonIndexSelect = currentMovie.getSeasons().indexOf(s);
            }
        }
        sharedViewModel.movieDetails = currentMovie;
    }

    @Override
    public void onEpisodeClick(MovieItemResponse episode) {
        ((MovieDetailActivity) requireActivity()).navigateToWatchMovieActivity(episode);
    }
}
