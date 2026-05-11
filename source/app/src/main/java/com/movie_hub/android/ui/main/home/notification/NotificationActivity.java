package com.movie_hub.android.ui.main.home.notification;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.request.notification.NotificationRequest;
import com.movie_hub.android.data.model.api.request.notification.UpdateReadRequest;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.notification.NotificationResponse;
import com.movie_hub.android.data.model.onesignal.MessageCommentResponse;
import com.movie_hub.android.data.model.onesignal.MessageOneSignal;
import com.movie_hub.android.data.model.onesignal.OneSignalCommand;
import com.movie_hub.android.databinding.ActivityNotificationBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.home.filter.model.FilterTypeModel;
import com.movie_hub.android.ui.main.home.notification.adapter.NotificationAdapter;
import com.movie_hub.android.ui.main.home.notification.adapter.NotificationFilterAdapter;
import com.movie_hub.android.ui.main.home.notification.shimmer.NotificationShimmerAdapter;
import com.movie_hub.android.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends BaseActivity<ActivityNotificationBinding, NotificationModel> implements NotificationAdapter.OnNotificationClickListener, SystemBarColorProvider {

    private NotificationAdapter adapter;
    private NotificationFilterAdapter typeFilterAdapter;
    private int currentPage = 0;
    private final int pageSize = 20;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    private final Handler timeUpdateHandler = new Handler(Looper.getMainLooper());
    private final Runnable timeUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (adapter != null && adapter.getItemCount() > 0) {
                adapter.notifyItemRangeChanged(0, adapter.getItemCount(), NotificationAdapter.PAYLOAD_UPDATE_TIME);
            }
            timeUpdateHandler.postDelayed(this, 60000); // 60 seconds
        }
    };

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

    @Override
    protected void onResume() {
        super.onResume();
        timeUpdateHandler.post(timeUpdateRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timeUpdateHandler.removeCallbacks(timeUpdateRunnable);
    }

    private void initView() {
        adapter = new NotificationAdapter(this, this);
        viewBinding.rvNotification.setLayoutManager(new LinearLayoutManager(this));
        viewBinding.rvNotification.setAdapter(adapter);

        initTypeFilter();

        viewBinding.swipeRefreshLayout.setOnRefreshListener(() -> {
            getListNotification(true);
        });
        viewBinding.option.setOnClickListener(this::showFilterMenu);
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

    private List<FilterTypeModel> getTypeFilters() {
        List<FilterTypeModel> list = new ArrayList<>();
        list.add(new FilterTypeModel(getString(R.string.all), -1, viewModel.currentFilterType == null));
        list.add(new FilterTypeModel(getString(R.string.nav_community), Constants.NOTIFICATION_TYPE_SOCIAL,
                viewModel.currentFilterType != null && viewModel.currentFilterType.equals(Constants.NOTIFICATION_TYPE_SOCIAL)));
        list.add(new FilterTypeModel(getString(R.string.nav_movies), Constants.NOTIFICATION_TYPE_MOVIE,
                viewModel.currentFilterType != null && viewModel.currentFilterType.equals(Constants.NOTIFICATION_TYPE_MOVIE)));
        return list;
    }

    private void initTypeFilter() {
        typeFilterAdapter = new NotificationFilterAdapter(filterTypeModel -> {
            if (filterTypeModel.getType() == -1) {
                viewModel.currentFilterType = null;
            } else {
                viewModel.currentFilterType = filterTypeModel.getType();
            }
            typeFilterAdapter.setData(getTypeFilters());
            getListNotification(true);
        });

        viewBinding.rvType.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        viewBinding.rvType.setAdapter(typeFilterAdapter);
        typeFilterAdapter.setData(getTypeFilters());
    }

    public void showShimmerLoading() {
        NotificationShimmerAdapter shimmerAdapter = new NotificationShimmerAdapter(6);
        viewBinding.rvNotification.setAdapter(shimmerAdapter);
    }

    public void getListNotification(boolean isRefresh) {
        if (isLoading) return;
        isLoading = true;

        if (isRefresh) {
            currentPage = 0;
            isLastPage = false;
            if (!viewBinding.swipeRefreshLayout.isRefreshing()) {
                showShimmerLoading();
            }
        } else {
            viewBinding.bottomLoadingBar.setVisibility(View.VISIBLE);
        }

        NotificationRequest request = new NotificationRequest();
        request.setPage(currentPage);
        request.setSize(pageSize);
        request.setIsRead(viewModel.currentFilterReadStatus);
        request.setType(viewModel.currentFilterType);

        viewModel.getListMovieHistory(new MainCallback<ResponseListObj<NotificationResponse>>() {
            @Override
            public void doSuccess(ResponseListObj<NotificationResponse> response) {
                isLoading = false;
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
                isLoading = false;
                finishLoading();
            }

            @Override public void doSuccess() {}
            @Override public void doFail() {
                isLoading = false;
                finishLoading();
            }
        }, request);
    }
    
    public void updateRead(NotificationResponse item, int position) {
        UpdateReadRequest updateReadRequest = new UpdateReadRequest();
        updateReadRequest.getIds().add(item.getId());
        showLoading();
        viewModel.updateRead(new MainCallback<Void>() {
            @Override
            public void doError(Throwable error) {
                hideLoading();
                handleNotificationAction(item);
            }

            @Override
            public void doSuccess() {
                hideLoading();
                item.setRead(true);
                adapter.notifyItemChanged(position);
                handleNotificationAction(item);
            }

            @Override
            public void doFail() {
                hideLoading();
                handleNotificationAction(item);
            }
        }, updateReadRequest);
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
        if (item.isRead()) {
            handleNotificationAction(item);
        } else {
            updateRead(item, position);
        }
    }

    private void handleNotificationAction(NotificationResponse item) {
        if (item.getCmd() == null) return;

        MessageOneSignal messageOneSignal = new MessageOneSignal();
        messageOneSignal.setCmd(item.getCmd());
        String jsonData = item.getData() != null ? item.getData() : item.getBody();
        messageOneSignal.setData(jsonData);
        messageOneSignal.setTitle(item.getTitle());
        messageOneSignal.setContent(item.getBody());

        switch (item.getCmd()) {
            case OneSignalCommand.CMD_REPLY_COMMENT:
                MessageCommentResponse messageCommentResponse = GsonUtils.fromJson(messageOneSignal.getData(), MessageCommentResponse.class);
                if (messageCommentResponse != null && messageCommentResponse.getMovieId() != null) {
                    getMovieDetailByNotification(Long.valueOf(messageCommentResponse.getMovieId()), messageOneSignal);
                }
                break;
            case OneSignalCommand.CMD_NEW_MOVIE:
                MovieResponse movieResponse = GsonUtils.fromJson(messageOneSignal.getData(), MovieResponse.class);
                if (movieResponse != null && movieResponse.getId() != null) {
                    getMovieDetailByNotification(movieResponse.getId(), messageOneSignal);
                }
                break;

            case OneSignalCommand.CMD_NEW_MOVIE_ITEM:
                MovieItemResponse movieItemResponse = GsonUtils.fromJson(messageOneSignal.getData(), MovieItemResponse.class);
                if (movieItemResponse != null && movieItemResponse.getId() != null) {
                    getMovieDetailByNotification(movieItemResponse.getMovie().getId(), messageOneSignal);
                }
                break;
            default:
                break;
        }
    }

    public void showFilterMenu(View v) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.layout_popup_filter, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(getDrawable(R.color.bg_dialog));
        popupWindow.setElevation(20); // Đổ bóng cho pop-up

        ImageView checkAll = popupView.findViewById(R.id.check_all);
        ImageView checkUnread = popupView.findViewById(R.id.check_un_read);
        ImageView checkRead = popupView.findViewById(R.id.check_read);

        // Reset ẩn tất cả trước
        checkAll.setVisibility(View.GONE);
        checkUnread.setVisibility(View.GONE);
        checkRead.setVisibility(View.GONE);

        if (viewModel.currentFilterReadStatus == null) {
            checkAll.setVisibility(View.VISIBLE);
        } else if (viewModel.currentFilterReadStatus == false) {
            checkUnread.setVisibility(View.VISIBLE);
        } else {
            checkRead.setVisibility(View.VISIBLE);
        }

        popupView.findViewById(R.id.btn_all).setOnClickListener(view -> {
            viewModel.currentFilterReadStatus = null;
            getListNotification(true);
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.btn_un_read).setOnClickListener(view -> {
            viewModel.currentFilterReadStatus = false;
            getListNotification(true);
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.btn_read).setOnClickListener(view -> {
            viewModel.currentFilterReadStatus = true;
            getListNotification(true);
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(v, 0, 0);
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
