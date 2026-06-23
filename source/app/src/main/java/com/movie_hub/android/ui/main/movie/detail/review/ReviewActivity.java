package com.movie_hub.android.ui.main.movie.detail.review;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.review.CreateReviewReactionRequest;
import com.movie_hub.android.data.model.api.request.review.CreateReviewRequest;
import com.movie_hub.android.data.model.api.request.review.ReviewRequest;
import com.movie_hub.android.data.model.api.response.comment.VoteListResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.report.CreateReportRequest;
import com.movie_hub.android.data.model.api.response.review.ReviewResponse;
import com.movie_hub.android.data.model.onesignal.MessageReviewResponse;
import com.movie_hub.android.data.model.onesignal.OneSignalCommand;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityReviewBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.login.LoginActivity;
import com.movie_hub.android.ui.main.movie.detail.comment.shimmer.CommentShimmerAdapter;
import com.movie_hub.android.ui.main.movie.detail.review.adapter.ReviewItemAdapter;
import com.movie_hub.android.ui.main.movie.detail.review.dialog.ReviewDialogFragment;
import com.movie_hub.android.utils.DialogUtils;
import com.movie_hub.android.utils.GsonUtils;
import com.movie_hub.android.utils.ReportDialogUtils;
import com.movie_hub.android.utils.ReportUtils;

import java.util.List;

public class ReviewActivity extends BaseActivity<ActivityReviewBinding, ReviewViewModel>
    implements SystemBarColorProvider, View.OnClickListener,
    ReviewItemAdapter.ReviewCallback {

    public void setUpStartActivity() {
        float translateY = dpToPx(SHEET_TRANSLATE_Y_DP);

        // Animation mở: lớp mờ fade in + sheet trượt lên
        viewBinding.dimBackground.setAlpha(0f);
        viewBinding.contentView.setTranslationY(translateY);
        viewBinding.contentView.setAlpha(0f);

        viewBinding.dimBackground.animate()
                .alpha(1f)
                .setDuration(200)
                .setStartDelay(0)
                .start();

        viewBinding.contentView.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .start();

    }
    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
    @Override
    public void finish() {
        float translateY = dpToPx(SHEET_TRANSLATE_Y_DP);

        viewBinding.dimBackground.animate()
                .alpha(0f)
                .setDuration(250)
                .start();

        viewBinding.contentView.animate()
                .translationY(translateY)
                .alpha(0f)
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    super.finish();
                    overridePendingTransition(0, 0); // tắt animation window mặc định
                })
                .start();

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_review;
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
        return R.color.transparent;
    }

    @Override
    public int getNavigationBarColor() {
        return R.color.bg_input_comment;
    }

    private static final float SHEET_TRANSLATE_Y_DP = 200f;
    public static final String MSG_REVIEW = "MSG_REVIEW";
    public static final String MSG_CMD = "MSG_CMD";

    private ReviewItemAdapter reviewItemAdapter;
    private CommentShimmerAdapter commentShimmerAdapter;
    private int currentPage = 0;
    private int pageSize = 20;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private ActivityResultLauncher<Intent> loginLauncher;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        setUpStartActivity();
        setUpAdapter();
        showShimmer();

        String json = getIntent().getStringExtra("movie_details");
        boolean isReviewed = getIntent().getBooleanExtra("is_review", false);
        MovieResponse movieResponse = GsonUtils.fromJson(json, MovieResponse.class);
        viewModel.isReview.postValue(isReviewed);
        if (movieResponse != null) {
            viewModel.movieDetails = movieResponse;
            if (viewModel.movieDetails.getReviewCount() != null && viewModel.movieDetails.getReviewCount() > 0L) {
                viewBinding.tvCountCmt.setText(getString(R.string.rating) + " (" + viewModel.movieDetails.getReviewCount().toString() + ")");
            }

            if (viewModel.isLogin()) {
                getVoteList();
            } else {
                getListReview();
            }
        }

        loginLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        boolean loginSuccess = data != null && data.getBooleanExtra("login_success", false);
                        if (loginSuccess) {
                            new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.login_successful)).showMessage(this);
                            getVoteListForLoginSuccess();
                            checkReview();
                        }
                    }
                }
        );

        viewBinding.rvReview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) return;

                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm == null || reviewItemAdapter == null) return;

                int totalItemCount = lm.getItemCount();
                int lastVisibleItemPosition = lm.findLastVisibleItemPosition();

                if (!isLoading && !isLastPage && lastVisibleItemPosition >= totalItemCount - 5) {
                    getListReview();
                }
            }
        });

        viewModel.isReview.observe(this, isReview -> {
            if (isReview) {
                viewBinding.bottomReview.setVisibility(View.GONE);
            } else {
                viewBinding.bottomReview.setVisibility(View.VISIBLE);
            }
        });
    }

    public void showShimmer() {
        viewBinding.rvReview.setAdapter(commentShimmerAdapter);
    }

    public void hideShimmer() {
        viewBinding.rvReview.setAdapter(reviewItemAdapter);
    }

    public void setUpAdapter() {
        reviewItemAdapter = new ReviewItemAdapter(this, this, viewModel.getUserId());
        commentShimmerAdapter = new CommentShimmerAdapter(6);
        viewBinding.rvReview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    public void getVoteList() {
        viewModel.getVoteList(new MainCallback<List<VoteListResponse>>() {

            @Override
            public void doSuccess(List<VoteListResponse> data) {
                if (data != null && !data.isEmpty()) {
                    viewModel.voteListResponses = data;
                }

                getListReview();
            }

            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, viewModel.movieDetails.getId());
    }

    public void getVoteListForLoginSuccess() {
        viewModel.getVoteList(new MainCallback<List<VoteListResponse>>() {

            @Override
            public void doSuccess(List<VoteListResponse> data) {
                if (data != null && !data.isEmpty()) {
                    viewModel.voteListResponses = data;
                    reviewItemAdapter.updateVotesAndRefresh(data);
                }
            }

            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, viewModel.movieDetails.getId());
    }

    public void getListReview() {
        isLoading = true;

        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setSize(pageSize);
        reviewRequest.setPage(currentPage);
        reviewRequest.setMovieId(viewModel.movieDetails.getId());

        viewModel.getListReview(new MainCallback<ResponseListObj<ReviewResponse>>() {
            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
                isLoading = false;
            }

            @Override
            public void doSuccess(ResponseListObj<ReviewResponse> data) {
                if (data.getContent() != null && !data.getContent().isEmpty()) {
                    viewBinding.layoutEmpty.setVisibility(View.GONE);
                    if (currentPage == 0) {
                        viewBinding.layoutEmpty.setVisibility(View.GONE);
                        hideShimmer();
                        reviewItemAdapter.setData(viewModel.setupVoteList(data.getContent()));
                        scrollToNotificationReviewIfNeeded();
                    } else {
                        reviewItemAdapter.addData(viewModel.setupVoteList(data.getContent()));
                    }

                    currentPage++;

                    if (data.getTotalPages() == currentPage) {
                        isLastPage = true;
                    }
                } else {
                    if (currentPage == 0) {
                        hideShimmer();
                        viewBinding.layoutEmpty.setVisibility(View.VISIBLE);
                    }

                    isLastPage = true;
                }

                isLoading = false;
            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
                isLoading = false;
            }
        }, reviewRequest);
    }

    public void createReview(CreateReviewRequest request) {
        viewModel.creteReview(new MainCallback<ReviewResponse>() {
            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void doSuccess(ReviewResponse data) {
                viewBinding.layoutEmpty.setVisibility(View.GONE);
                reviewItemAdapter.addItemToTop(data);
                if (data.getStatistics().getReviewCount() != null && data.getStatistics().getReviewCount() > 0L) {
                    viewBinding.tvCountCmt.setText(getString(R.string.rating) + " (" + data.getStatistics().getReviewCount().toString() + ")");
                }

                viewModel.isReview.postValue(true);
            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, request);
    }

    public void createVote(CreateReviewReactionRequest request, ReviewResponse reviewResponse, int position) {
        showLoading();
        viewModel.voteReview(new MainCallback<ResponseWrapper>() {
            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
            @Override
            public void doSuccess(ResponseWrapper responseWrapper) {
                viewModel.updateVoteList(request);
                reviewItemAdapter.updateItemDirectly(viewBinding.rvReview, position, viewModel.handleVoteStatus(reviewResponse));
                hideLoading();
            }
            @Override
            public void doSuccess() {

            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, request);
    }



    public void showLoginRequiredDialog() {
        DialogUtils.dialogConfirm(
                this,
                getString(R.string.not_login),
                getString(R.string.login),
                (dialog, which) -> {
                    Intent it = new Intent(this, LoginActivity.class);
                    it.putExtra("login_from_other", "login_from_other");
                    loginLauncher.launch(it);
                },
                getString(R.string.cancel),
                null
        );
    }

    public void checkReview() {
        viewModel.checkIsReview(new MainCallback<ReviewResponse>() {
            @Override
            public void doError(Throwable error) {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
            @Override
            public void doSuccess(ReviewResponse data) {
                if (data != null) {
                    viewModel.isReview.postValue(true);
                } else {
                    viewModel.isReview.postValue(false);
                }
            }
            @Override
            public void doSuccess() {

            }

            @Override
            public void doFail() {
                hideLoading();
                showError(getString(R.string.an_error_occurred));
            }
        }, viewModel.movieDetails.getId());
    }

    public void showDialogReview() {
        String movieJson = GsonUtils.toJson(viewModel.movieDetails);
        ReviewDialogFragment dialog = ReviewDialogFragment.newInstance(movieJson);

        dialog.setCallback(this::createReview);

        dialog.show(getSupportFragmentManager(), "ReviewDialog");
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_open_review:
                if (!viewModel.isLogin()) {
                    showLoginRequiredDialog();
                    return;
                }
                showDialogReview();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDislike(ReviewResponse reviewResponse, int position) {
        if (!viewModel.isLogin()) {
            showLoginRequiredDialog();
            return;
        }

        CreateReviewReactionRequest request = new CreateReviewReactionRequest();
        request.setId(reviewResponse.getId());
        request.setType(Constants.REACTION_TYPE_DISLIKE);

        createVote(request, reviewResponse, position);
    }

    @Override
    public void onLike(ReviewResponse reviewResponse, int position) {
        if (!viewModel.isLogin()) {
            showLoginRequiredDialog();
            return;
        }
        CreateReviewReactionRequest request = new CreateReviewReactionRequest();
        request.setId(reviewResponse.getId());
        request.setType(Constants.REACTION_TYPE_LIKE);

        createVote(request, reviewResponse, position);
    }

    @Override
    public void onReport(ReviewResponse reviewResponse, int position) {
        if (!viewModel.isLogin()) {
            showLoginRequiredDialog();
            return;
        }
        if (reviewResponse == null || reviewResponse.getId() == null) {
            return;
        }

        ReportDialogUtils.show(
                this,
                getString(R.string.report_review_title),
                content -> submitReport(reviewResponse.getId(), Constants.USER_REPORT_TYPE_REVIEW, content)
        );
    }

    private void submitReport(long objectId, int reportType, String content) {
        CreateReportRequest request = new CreateReportRequest();
        request.setObjectId(objectId);
        request.setType(reportType);
        request.setContent(content);

        viewModel.createReport(new MainCallback<ResponseWrapper>() {
            @Override
            public void doError(Throwable error) {
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred))
                        .showMessage(ReviewActivity.this);
            }

            @Override
            public void doSuccess() {
            }

            @Override
            public void doSuccess(ResponseWrapper response) {
                new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.report_success))
                        .showMessage(ReviewActivity.this);
            }

            @Override
            public void doErrorForm(ResponseWrapper response) {
                ReportUtils.showReportFailMessage(
                        ReviewActivity.this,
                        response,
                        Constants.USER_REPORT_TYPE_REVIEW
                );
            }

            @Override
            public void doFail() {
            }
        }, request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        reviewItemAdapter.stopTimeUpdater();
    }

    private void scrollToNotificationReviewIfNeeded() {
        String json = getIntent().getStringExtra(MSG_REVIEW);
        String cmd = getIntent().getStringExtra(MSG_CMD);
        if (json == null || json.isEmpty() || !OneSignalCommand.CMD_TOXIC_REVIEW_LOCKED.equals(cmd)) {
            return;
        }

        getIntent().removeExtra(MSG_REVIEW);
        getIntent().removeExtra(MSG_CMD);

        MessageReviewResponse messageReviewResponse = GsonUtils.fromJson(json, MessageReviewResponse.class);
        if (messageReviewResponse == null || messageReviewResponse.getId() == null) {
            return;
        }

        long reviewId = Long.parseLong(messageReviewResponse.getId());
        int targetPosition = reviewItemAdapter.findPositionById(reviewId);
        if (targetPosition == -1) {
            return;
        }

        int finalTargetPosition = targetPosition;
        viewBinding.rvReview.post(() -> {
            LinearLayoutManager lm = (LinearLayoutManager) viewBinding.rvReview.getLayoutManager();
            if (lm != null) {
                lm.scrollToPositionWithOffset(finalTargetPosition, 0);
            }
        });
    }
}
