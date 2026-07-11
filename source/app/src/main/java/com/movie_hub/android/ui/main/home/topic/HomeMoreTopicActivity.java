package com.movie_hub.android.ui.main.home.topic;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.request.collection.CollectionRequest;
import com.movie_hub.android.data.model.api.response.collection.CollectionResponse;
import com.movie_hub.android.databinding.ActivityHomeMoreTopicBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.home.topic.adapter.GridSpacingItemDecoration;
import com.movie_hub.android.ui.main.home.topic.adapter.TopicAdapter;
import com.movie_hub.android.ui.main.home.topic.shimmer.TopicShimmerAdapter;
import com.movie_hub.android.ui.main.home.topic.topic_detail.HomeTopicDetailActivity;
import com.movie_hub.android.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;


public class HomeMoreTopicActivity extends BaseActivity<ActivityHomeMoreTopicBinding, HomeMoreTopicViewModel> implements
        SystemBarColorProvider,
        TopicAdapter.OnTopicDetailClickCallback {
    @Override
    public int getLayoutId() {
        return R.layout.activity_home_more_topic;
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }
    @Override
    public int getStatusBarColor() {
        return R.color.header_app;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_app;
    }
    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    private TopicAdapter topicAdapter;
    private TopicShimmerAdapter topicShimmerAdapter;
    int currentPage = 0;
    int pageSize = 20;
    boolean isLastPage = false;
    private boolean isLoading = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        setUpAdapter();
        showShimmer();
        getListTopic();

        viewBinding.rvTopic.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) return;

                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm == null || topicAdapter == null) return;

                int totalItemCount = lm.getItemCount();
                int lastVisibleItemPosition = lm.findLastVisibleItemPosition();

                if (!isLoading && !isLastPage && lastVisibleItemPosition >= totalItemCount - 5) {
                    getListTopic();
                }
            }
        });

        viewBinding.tvTitle.setSelected(true);
    }

    public void setUpAdapter() {
        topicShimmerAdapter = new TopicShimmerAdapter(8);
        topicAdapter = new TopicAdapter(this, this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        viewBinding.rvTopic.setLayoutManager(layoutManager);

        RecyclerView.ItemDecoration decor = new GridSpacingItemDecoration(
                this,
                2,
                R.dimen._12sdp,
                true
        );
        viewBinding.rvTopic.addItemDecoration(decor);
        viewBinding.rvTopic.setAdapter(topicAdapter);
    }

    public void showShimmer() {
        viewBinding.rvTopic.setAdapter(topicShimmerAdapter);
    }

    public void hideShimmer() {
        viewBinding.rvTopic.setAdapter(topicAdapter);
    }
    public void getListTopic() {
        showLoading();
        isLoading = true;
        CollectionRequest request = new CollectionRequest();
        request.setSize(pageSize);
        request.setPage(currentPage);

        viewModel.getListTopic(new MainCallback<ResponseListObj<CollectionResponse>>() {
            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
                isLoading = false;
            }

            @Override
            public void doSuccess() {
                hideLoading();
                isLoading = false;
            }

            @Override
            public void doSuccess(ResponseListObj<CollectionResponse> data) {
                hideLoading();
                if (data.getContent() != null && !data.getContent().isEmpty()) {
                    if (currentPage == 0) {
                        hideShimmer();
                        viewModel.topicList.setValue(new ArrayList<>(data.getContent()));

                        topicAdapter.setData(data.getContent());
                    } else {
                        List<CollectionResponse> currentList = viewModel.topicList.getValue();
                        if (currentList == null) currentList = new ArrayList<>();

                        currentList.addAll(data.getContent());
                        viewModel.topicList.postValue(currentList);

                        topicAdapter.addData(data.getContent());
                    }

                    currentPage++;

                    if (currentPage >= data.getTotalPages()) {
                        isLastPage = true;
                    }
                } else {
                    isLastPage = true;
                }
                isLoading = false;
            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
                isLoading = false;
            }
        }, request);
    }
    @Override
    public void onTopicClick(CollectionResponse collectionResponse) {
        showLoading();
        Intent it = new Intent(this, HomeTopicDetailActivity.class);
        it.putExtra("collection", GsonUtils.toJson(collectionResponse));
        startActivity(it);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideLoading();
    }
}
