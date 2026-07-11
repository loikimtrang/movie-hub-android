package com.movie_hub.android.ui.main.schedule;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.request.schedule.MovieScheduleRequest;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.FragmentScheduleBinding;
import com.movie_hub.android.di.component.FragmentComponent;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.schedule.adapter.MarginItemDecoration;
import com.movie_hub.android.ui.main.schedule.adapter.ScheduleModel;
import com.movie_hub.android.ui.main.schedule.adapter.ScheduleTimeAdapter;
import com.movie_hub.android.ui.main.schedule.adapter.movie.MovieScheduleAdapter;
import com.movie_hub.android.ui.main.schedule.adapter.movie.MovieScheduleShimmerAdapter;
import com.movie_hub.android.utils.DateUtils;
import java.util.List;
import com.movie_hub.android.BR;

public class ScheduleFragment extends BaseFragment<FragmentScheduleBinding, ScheduleViewModel>
        implements ScheduleTimeAdapter.OnScheduleClickListener, MovieScheduleAdapter.OnMovieClickListener {

    private ScheduleTimeAdapter scheduleAdapter;
    private MovieScheduleAdapter movieAdapter;
    private MovieScheduleShimmerAdapter shimmerAdapter;
    private ScheduleModel currentSelectedDate; // Lưu ngày đang chọn để refresh nếu cần

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAdapter();
        initSwipeRefresh();
    }

    private void initAdapter() {
        scheduleAdapter = new ScheduleTimeAdapter(getContext(), this);
        binding.rvTime.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        int marginSide = getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._3sdp);
        binding.rvTime.addItemDecoration(new MarginItemDecoration(marginSide));
        binding.rvTime.setAdapter(scheduleAdapter);

        movieAdapter = new MovieScheduleAdapter(getContext(), this);
        shimmerAdapter = new MovieScheduleShimmerAdapter(5);

        binding.rvMovie.setLayoutManager(new LinearLayoutManager(getContext()));

        List<ScheduleModel> days = DateUtils.generate30Days();
        scheduleAdapter.setData(days, binding.rvTime);

        if (!days.isEmpty()) {
            currentSelectedDate = days.get(0);
            getListMovie(currentSelectedDate);
        }
    }

    private void initSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            if (currentSelectedDate != null) {
                getListMovie(currentSelectedDate);
            } else {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onDateClick(ScheduleModel model, int position) {
        currentSelectedDate = model;
        getListMovie(model);
    }

    @Override
    public void onMovieClick(MovieItemResponse movieItemResponse) {
    }

    public void getListMovie(ScheduleModel scheduleModel) {
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.rvMovie.setAdapter(shimmerAdapter);

        MovieScheduleRequest request = new MovieScheduleRequest();
        request.setDate(scheduleModel.getFullDate());

        viewModel.getListMovie(new MainCallback<List<MovieItemResponse>>() {
            @Override
            public void doSuccess(List<MovieItemResponse> list) {
                binding.swipeRefreshLayout.setRefreshing(false);
                if (list != null && !list.isEmpty()) {
                    movieAdapter.setData(list);
                    binding.rvMovie.setAdapter(movieAdapter);
                } else {
                    binding.rvMovie.setAdapter(null);
                    String dateValue = scheduleModel.isToday()
                            ? " " + getString(R.string.today).toLowerCase()
                            : " " + scheduleModel.getDayLabel();

                    binding.tvEmpty.setText(getString(R.string.no_showtime_for_day, dateValue));
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void doError(Throwable error) {
                handleError();
            }

            @Override
            public void doFail() {
                handleError();
            }

            @Override
            public void doSuccess() {}
        }, request);
    }

    private void handleError() {
        binding.swipeRefreshLayout.setRefreshing(false);
        binding.rvMovie.setAdapter(null);
        new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred)).showMessage(getContext());
    }

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
    }

    @Override
    public int getBindingVariable() { return BR.vm; }

    @Override
    protected int getLayoutId() { return R.layout.fragment_schedule; }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }
}