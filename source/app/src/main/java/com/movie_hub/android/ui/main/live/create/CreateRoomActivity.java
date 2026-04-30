package com.movie_hub.android.ui.main.live.create;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.request.room.CreateRoomRequest;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.season.SeasonResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityCreateRoomBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.live.create.adapter.RoomEpisodeAdapter;
import com.movie_hub.android.ui.main.movie.detail.dialog.ChooseSeasonBottomSheetDialog;
import com.movie_hub.android.utils.GsonUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateRoomActivity extends BaseActivity<ActivityCreateRoomBinding, CreateRoomViewModel> implements SystemBarColorProvider,
        ChooseSeasonBottomSheetDialog.ChooseSeasonBottomSheetCallback,
        RoomEpisodeAdapter.OnEpisodeClickListener {
    @Override
    public int getLayoutId() {
        return R.layout.activity_create_room;
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
        return R.color.header_app;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_app;
    }
    boolean[] isAutoStart = {false};
    private int currentSeasonIndex = 0;
    Calendar selectedCalendar = Calendar.getInstance();

    RoomEpisodeAdapter roomEpisodeAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setVm(viewModel);
        viewBinding.setA(this);
        String json = getIntent().getStringExtra("movie_details");

        if (json != null) {
            viewModel.movieDetails = GsonUtils.fromJson(json, MovieResponse.class);

            Glide.with(this)
                    .load(viewModel.movieDetails.getPosterUrl())
                    .placeholder(R.drawable.place_holder_2_3)
                    .error(R.drawable.place_holder_2_3)
                    .into(viewBinding.image);

            viewBinding.title.setText(viewModel.movieDetails.getTitle());
            viewBinding.subTitle.setText(viewModel.movieDetails.getOriginalTitle());

            if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES) {
                roomEpisodeAdapter = new RoomEpisodeAdapter(this, this);
                androidx.recyclerview.widget.GridLayoutManager layoutManager =
                        new androidx.recyclerview.widget.GridLayoutManager(this, 3);
                viewBinding.rvEpisode.setLayoutManager(layoutManager);
                viewBinding.rvEpisode.setAdapter(roomEpisodeAdapter);
                int spacing = getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._3sdp);
                viewBinding.rvEpisode.addItemDecoration(new androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view,
                                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                        outRect.left = spacing;
                        outRect.right = spacing;
                        outRect.bottom = spacing * 2;
                    }
                });

                updateSeasonDisplay();
                viewBinding.layoutEpisode.setVisibility(View.VISIBLE);
            }
        }


        viewBinding.btnStartSchedule.setOnClickListener(v -> {
            isAutoStart[0] = !isAutoStart[0];

            if (isAutoStart[0]) {
                viewBinding.btnStartSchedule.getChildAt(0)
                        .setBackgroundResource(R.drawable.ic_check_box_check);

                viewBinding.layoutCalender.setVisibility(View.VISIBLE);

                Calendar now = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                viewBinding.edtCalender.setText(sdf.format(now.getTime()));

                viewModel.isStartNow = false;
            } else {
                viewBinding.btnStartSchedule.getChildAt(0)
                        .setBackgroundResource(R.drawable.ic_check_box_un_check);
                viewBinding.layoutCalender.setVisibility(View.GONE);

                viewBinding.edtCalender.setText("");

                viewModel.isStartNow = true;
            }
        });

        viewBinding.edtCalender.setFocusable(false);
        viewBinding.edtCalender.setOnClickListener(v -> showDateTimePicker());

        viewBinding.btnCreate.setOnClickListener(v -> {
            onClickCreateRoom();
        });
    }
    private void showDateTimePicker() {
        final Calendar now = Calendar.getInstance();

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedCalendar.set(Calendar.YEAR, year);
            selectedCalendar.set(Calendar.MONTH, month);
            selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedCalendar.set(Calendar.MINUTE, minute);
                selectedCalendar.set(Calendar.SECOND, 0);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                viewBinding.edtCalender.setText(sdf.format(selectedCalendar.getTime()));

            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();

        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    @SuppressLint("SetTextI18n")
    public void showSeasonListBottomSheet() {

        if (viewModel.movieDetails == null) return;

        int type = viewModel.movieDetails.getType();
        if (type == Constants.TYPE_MOVIE_SINGLE || type == Constants.TYPE_MOVIE_TRAILER) return;

        List<SeasonResponse> seasons = viewModel.movieDetails != null
                ? viewModel.movieDetails.getSeasons()
                : null;

        if (seasons == null || seasons.isEmpty()) {
            Toast.makeText(this, "Không có season để hiển thị", Toast.LENGTH_SHORT).show();
            return;
        }

        ChooseSeasonBottomSheetDialog sheet = new ChooseSeasonBottomSheetDialog(
                this,
                this,
                GsonUtils.toJson(seasons)
        );
        sheet.show();

        if (sheet.getWindow() != null) {
            sheet.getWindow().getDecorView().post(sheet::setupWindow);
        }
    }
    private void updateSeasonDisplay() {
        if (viewModel.movieDetails == null || viewModel.movieDetails.getSeasons() == null || viewModel.movieDetails.getSeasons().isEmpty()) {
            roomEpisodeAdapter.setData(new ArrayList<>());
            viewBinding.tvSeason.setText(getString(R.string.season) + " -");
            return;
        }

        List<SeasonResponse> seasons = viewModel.movieDetails.getSeasons();
        SeasonResponse currentSeason = seasons.get(currentSeasonIndex);

        viewBinding.tvSeason.setText(getString(R.string.season) + " " + (currentSeasonIndex + 1));
        roomEpisodeAdapter.setData(currentSeason.getEpisodes());

        for (SeasonResponse s : seasons) {
            s.setSelect(s == currentSeason);
        }
    }
    @Override
    public void onSeasonClicked(SeasonResponse seasonResponse) {
        if (viewModel.movieDetails == null || viewModel.movieDetails.getSeasons() == null) return;

        for (int i = 0; i < viewModel.movieDetails.getSeasons().size(); i++) {
            if (viewModel.movieDetails.getSeasons().get(i).getId().equals(seasonResponse.getId())) {
                currentSeasonIndex = i;
                updateSeasonDisplay();
                break;
            }
        }
    }

    @Override
    public void onEpisodeClick(MovieItemResponse model, int position) {
        viewModel.currentEpisodeSelect = model;
    }

    private void onClickCreateRoom() {
        String roomName = viewBinding.tvNameRoom.getText().toString().trim();
        if (roomName.isEmpty()) {
            new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.error_input_room_name)).showMessage(this);
            return;
        }

        if (!viewModel.isStartNow) {
            Calendar now = Calendar.getInstance();
            if (selectedCalendar.before(now)) {
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.error_time_past)).showMessage(this);
                return;
            }
        }

        if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES && viewModel.currentEpisodeSelect == null) {
            new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.error_select_episode)).showMessage(this);
            return;
        }

        CreateRoomRequest request = new CreateRoomRequest();
        request.setName(roomName);
        request.setMovieItemId(viewModel.currentEpisodeSelect != null ?
                viewModel.currentEpisodeSelect.getId() :
                viewModel.movieDetails.getSeasons().get(0).getId());

        request.setStartNow(viewModel.isStartNow);
        request.setKind(Constants.ROOM_KIND_PUBLIC);

        if (!viewModel.isStartNow) {
            SimpleDateFormat apiSdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            request.setStartTime(apiSdf.format(selectedCalendar.getTime()));
        }

        viewModel.showLoading();
        viewModel.createRoom(new MainCallback<Void>() {
            @Override
            public void doError(Throwable error) {
                viewModel.hideLoading();
                new ToastMessage(ToastMessage.TYPE_ERROR, getString(R.string.an_error_occurred)).showMessage(CreateRoomActivity.this);
            }

            @Override
            public void doSuccess() {
                new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.create_room_success)).showMessage(CreateRoomActivity.this);
                new Handler().postDelayed(() -> {
                    finish();
                }, 1500);
            }

            @Override
            public void doFail() {
                new ToastMessage(ToastMessage.TYPE_ERROR, getString(R.string.an_error_occurred)).showMessage(CreateRoomActivity.this);
            }
        }, request);
    }
}
