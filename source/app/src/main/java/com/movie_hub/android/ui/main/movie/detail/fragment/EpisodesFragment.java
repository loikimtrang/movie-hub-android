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

public class EpisodesFragment extends BaseFragment<FragmentEpisodeBinding, EpisodesFragmentViewModel>
        implements ChooseSeasonBottomSheetDialog.ChooseSeasonBottomSheetCallback,
        EpisodeItemListAdapter.OnEpisodeClickListener {

    private MovieDetailViewModel sharedViewModel;
    private MovieResponse movieDetail;
    private EpisodeItemListAdapter adapter;

    // Chỉ lưu index của season đang được chọn
    private int currentSeasonIndex = 0;

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(MovieDetailViewModel.class);
        movieDetail = sharedViewModel.movieDetails;

        // Mặc định là season cuối cùng
        if (movieDetail != null && movieDetail.getSeasons() != null && !movieDetail.getSeasons().isEmpty()) {
            currentSeasonIndex = movieDetail.getSeasons().size() - 1;
        }

        setupViews();
        observeTracking();
    }

    private void setupViews() {
        adapter = new EpisodeItemListAdapter(this, getContext());
        binding.rvEpisode.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvEpisode.setAdapter(adapter);

        updateSeasonDisplay();
    }

    private void observeTracking() {
        sharedViewModel.movieDetailsTracking.observe(getViewLifecycleOwner(), tracking -> {
            if (tracking == null || !viewModel.isLogin()) return;

            sharedViewModel.applyWatchHistory(tracking);
            updateSeasonDisplay(); // refresh lại UI sau khi apply history
        });
    }

    // Luôn lấy dữ liệu mới nhất từ season hiện tại → không bao giờ bị mất dữ liệu
    private void updateSeasonDisplay() {
        if (movieDetail == null || movieDetail.getSeasons() == null || movieDetail.getSeasons().isEmpty()) {
            adapter.setData(new ArrayList<>());
            binding.tvSeason.setText(getString(R.string.season) + " -");
            return;
        }

        List<SeasonResponse> seasons = movieDetail.getSeasons();
        SeasonResponse currentSeason = seasons.get(currentSeasonIndex);

        binding.tvSeason.setText(getString(R.string.season) + " " + (currentSeasonIndex + 1));
        adapter.setData(currentSeason.getEpisodes());

        // Đánh dấu season đang chọn (nếu cần cho UI bottom sheet)
        for (SeasonResponse s : seasons) {
            s.setSelect(s == currentSeason);
        }
    }

    @SuppressLint("SetTextI18n")
    public void showSeasonListBottomSheet() {
        ClickUtils.debounceClick(binding.btnChooseSeason);

        if (movieDetail == null) return;

        int type = movieDetail.getType();
        if (type == Constants.TYPE_MOVIE_SINGLE || type == Constants.TYPE_MOVIE_TRAILER) return;

        List<SeasonResponse> seasons = sharedViewModel.movieDetails != null
                ? sharedViewModel.movieDetails.getSeasons()
                : null;

        if (seasons == null || seasons.isEmpty()) {
            Toast.makeText(getContext(), "Không có season để hiển thị", Toast.LENGTH_SHORT).show();
            return;
        }

        ChooseSeasonBottomSheetDialog sheet = new ChooseSeasonBottomSheetDialog(
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
    public void onSeasonClicked(SeasonResponse selectedSeason) {
        if (movieDetail == null || movieDetail.getSeasons() == null) return;

        for (int i = 0; i < movieDetail.getSeasons().size(); i++) {
            if (movieDetail.getSeasons().get(i).getId().equals(selectedSeason.getId())) {
                currentSeasonIndex = i;
                updateSeasonDisplay();
                break;
            }
        }
    }

    @Override
    public void onEpisodeClick(MovieItemResponse episode) {
        ((MovieDetailActivity) requireActivity()).navigateToWatchMovieActivity(episode);
    }

    // ==================== BaseFragment override ====================
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
}