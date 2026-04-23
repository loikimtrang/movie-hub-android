package com.movie_hub.android.ui.main.home.notification;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.request.notification.NotificationRequest;
import com.movie_hub.android.data.model.api.response.notification.NotificationResponse;
import com.movie_hub.android.databinding.ActivityNotificationBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.home.notification.adapter.NotificationAdapter;
import com.movie_hub.android.ui.main.home.notification.shimmer.NotificationShimmerAdapter;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends BaseActivity<ActivityNotificationBinding, NotificationModel> implements NotificationAdapter.OnNotificationClickListener, SystemBarColorProvider {

    private NotificationAdapter adapter;
    private int currentPage = 0;
    private final int pageSize = 20;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    public int getLayoutId() {
        return R.layout.activity_notification;
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        initView();
        getListNotification(true);
    }

    private void initView() {
        adapter = new NotificationAdapter(this, this);
        viewBinding.rvNotification.setLayoutManager(new LinearLayoutManager(this));
        viewBinding.rvNotification.setAdapter(adapter);

        viewBinding.swipeRefreshLayout.setOnRefreshListener(() -> {
            getListNotification(true);
        });

        viewBinding.rvNotification.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        getListNotification(false);
                    }
                }
            }
        });
    }

    public void showShimmerLoading() {
        NotificationShimmerAdapter shimmerAdapter = new NotificationShimmerAdapter(6);
        viewBinding.rvNotification.setAdapter(shimmerAdapter);
    }

    public void getListNotification(boolean isRefresh) {
        if (isRefresh) {
            currentPage = 0;
            isLastPage = false;
            if (!viewBinding.swipeRefreshLayout.isRefreshing()) {
                showShimmerLoading();
            }
        } else {
            isLoading = true;
            viewBinding.bottomLoadingBar.setVisibility(View.VISIBLE);
        }

        NotificationRequest request = new NotificationRequest();
        request.setPage(currentPage);
        request.setSize(pageSize);
        viewModel.getListMovieHistory(new MainCallback<ResponseListObj<NotificationResponse>>() {
            @Override
            public void doSuccess(ResponseListObj<NotificationResponse> response) {
                List<NotificationResponse> data = response.getContent();

                if (isRefresh) {
                    viewBinding.rvNotification.setAdapter(adapter);
                    viewBinding.swipeRefreshLayout.setRefreshing(false);

                    if (data == null || data.isEmpty()) {
                        viewBinding.layoutEmpty.setVisibility(View.VISIBLE);
                        adapter.setData(new ArrayList<>());
                    } else {
                        viewBinding.layoutEmpty.setVisibility(View.GONE);
                        adapter.setData(data);
                    }
                } else {
                    isLoading = false;
                    viewBinding.bottomLoadingBar.setVisibility(View.GONE);
                    if (data != null && !data.isEmpty()) {
                        adapter.addData(data);
                    }
                }

                if (data == null || currentPage >= response.getTotalPages() || data.size() < pageSize) {
                    isLastPage = true;
                } else {
                    currentPage++;
                }
            }

            @Override
            public void doError(Throwable error) {
                finishLoading();
            }

            @Override public void doSuccess() {}
            @Override public void doFail() { finishLoading(); }
        }, request);
    }

    private void finishLoading() {
        viewBinding.bottomLoadingBar.setVisibility(View.GONE);

        isLoading = false;
        viewBinding.swipeRefreshLayout.setRefreshing(false);
        if (viewBinding.rvNotification.getAdapter() instanceof NotificationShimmerAdapter) {
            viewBinding.rvNotification.setAdapter(adapter);
        }
    }

    @Override
    public void onItemClick(NotificationResponse item, int position) {
        if (!item.isRead()) {
            // Cập nhật UI ngay lập tức
            item.setRead(true);
            adapter.notifyItemChanged(position);

            // TODO: Gọi API thông báo cho Server rằng user đã đọc tin này
            // viewModel.markAsRead(item.getId());
        }

    }

    @Override
    public int getStatusBarColor() {
        return R.color.header_app;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_app;
    }
}