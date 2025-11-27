package com.movie_hub.android.ui.main.movie.detail.comment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
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
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.comment.CommentRequest;
import com.movie_hub.android.data.model.api.request.comment.CreateCommentRequest;
import com.movie_hub.android.data.model.api.response.comment.CommentResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityCommentBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.login.LoginActivity;
import com.movie_hub.android.ui.main.movie.detail.comment.adapter.CommentParentAdapter;
import com.movie_hub.android.utils.ClickUtils;
import com.movie_hub.android.utils.DialogUtils;
import com.movie_hub.android.utils.GsonUtils;

public class CommentActivity extends BaseActivity<ActivityCommentBinding, CommentViewModel>
        implements SystemBarColorProvider, View.OnClickListener,
        CommentParentAdapter.OnCommentParentClickListener {

    private static final float SHEET_TRANSLATE_Y_DP = 200f; // bạn điều chỉnh độ trượt
    public int currentPage = 0;
    public int pageSize = 8;
    boolean isLastPage = false;
    private boolean isLoading = false;
    private CommentParentAdapter commentParentAdapter;
    private ActivityResultLauncher<Intent> loginLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);
        setUpStartActivity();
        setUpInput();

        String json = getIntent().getStringExtra("movie_details");
        MovieResponse movieResponse = GsonUtils.fromJson(json, MovieResponse.class);
        if (movieResponse != null) {
            viewModel.movieDetails = movieResponse;
            setUpAdapter();
            CommentRequest request = new CommentRequest();
            request.setPage(currentPage);
            request.setSize(pageSize);
            request.setMovieId(viewModel.movieDetails.getId());

            getListComment(request, false);
        }

        viewBinding.rvComment.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) return;

                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm == null || commentParentAdapter == null) return;

                int totalItemCount = lm.getItemCount();
                int lastVisibleItemPosition = lm.findLastVisibleItemPosition();

                if (!isLoading && !isLastPage && lastVisibleItemPosition >= totalItemCount - 5) {
                    isLoading = true;

                    CommentRequest request = new CommentRequest();
                    request.setPage(currentPage);
                    request.setSize(pageSize);
                    request.setMovieId(viewModel.movieDetails.getId());
                    getListComment(request, false);
                }
            }
        });

        loginLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        boolean loginSuccess = data != null && data.getBooleanExtra("login_success", false);
                        if (loginSuccess) {
                            new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.login_successful)).showMessage(this);
                        }
                    }
                }
        );

        observeCommentList();
    }

    public void observeCommentList() {
        viewBinding.rvComment.setItemAnimator(null);
        viewModel.commentList.observe(this, commentList -> {
            if (commentList.isEmpty()) return;
            commentParentAdapter.updateDataDiff(commentList);
        });
    }
    public void setUpAdapter() {
        commentParentAdapter = new CommentParentAdapter(this, this);
        viewBinding.rvComment.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        viewBinding.rvComment.setAdapter(commentParentAdapter);
    }

    public void createComment(CreateCommentRequest request) {
        viewModel.createComment(new MainCallback<ResponseWrapper>() {
            @Override
            public void doError(Throwable error) {

            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doSuccess(ResponseWrapper object) {
                if (object.isResult()) {
                    CommentRequest request = new CommentRequest();
                    request.setPage(0);
                    request.setSize(pageSize);
                    request.setMovieId(viewModel.movieDetails.getId());

                    getListComment(request, true);
                    viewBinding.edtComment.setText("");
                }
            }


            @Override
            public void doFail() {

            }
        }, request);
    }
    public void getListComment(CommentRequest request, Boolean isRefresh) {
        isLoading = true;
        showLoading();
        viewModel.getListComment(new MainCallback<ResponseListObj<CommentResponse>>() {
            @Override
            public void doSuccess(ResponseListObj<CommentResponse> data) {
                isLoading = false;
                hideLoading();
                if (data.getContent() != null && !data.getContent().isEmpty()) {
                    viewBinding.layoutEmpty.setVisibility(View.GONE);
                    viewBinding.rvComment.setVisibility(View.VISIBLE);

                    if (isRefresh) {
                        LinearLayoutManager lm = (LinearLayoutManager) viewBinding.rvComment.getLayoutManager();
                        int firstVisible = lm.findFirstVisibleItemPosition();
                        View firstVisibleView = lm.findViewByPosition(firstVisible);
                        int offset = firstVisibleView != null ? firstVisibleView.getTop() : 0;

                        viewModel.mergeOrUpdateComments(data.getContent());

                        int total = viewModel.commentList.getValue() != null ? viewModel.commentList.getValue().size() : 0;
                        currentPage = total / pageSize;
                        if (total % pageSize != 0) {
                            currentPage += 1;
                        }

                        viewBinding.rvComment.post(() -> {
                            lm.scrollToPositionWithOffset(firstVisible, offset);
                        });
                    }
                    else {
                        currentPage++;
                        viewModel.mergeOrUpdateComments(data.getContent());
                    }


                    if (currentPage == data.getTotalPages()) {
                        isLastPage = true;
                    }

                } else {
                    if (currentPage == 0) {
                        viewBinding.rvComment.setVisibility(View.GONE);
                        viewBinding.layoutEmpty.setVisibility(View.VISIBLE);
                    }
                    isLastPage = true;
                }
            }

            @Override
            public void doError(Throwable error) {
                hideLoading();
                isLoading = false;

            }

            @Override
            public void doSuccess() {
                hideLoading();
                isLoading = false;

            }

            @Override public void doFail() {
                isLoading = false;
                hideLoading();

            }
        }, request);
    }

    public void getListChildComment(CommentRequest request) {
        viewModel.getListComment(new MainCallback<ResponseListObj<CommentResponse>>() {
            @Override
            public void doError(Throwable error) {

            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doSuccess(ResponseListObj<CommentResponse> data) {

            }

            @Override
            public void doFail() {

            }
        }, request);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_create_comment:
                String content = viewBinding.edtComment.getText().toString().trim();
                if (!content.isEmpty()) {
                    CreateCommentRequest request = new CreateCommentRequest();
                    request.setContent(content);
                    request.setMovieId(viewModel.movieDetails.getId());
                    createComment(request);

                    viewBinding.edtComment.setText("");
                } else {

                }
                break;
            default:
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setUpInput() {
        viewBinding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            viewBinding.getRoot().getWindowVisibleDisplayFrame(r);
            int screenHeight = viewBinding.getRoot().getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > dpToPx(150)) {
                viewBinding.lCountChar.setVisibility(View.VISIBLE);
            } else {
                viewBinding.lCountChar.setVisibility(View.INVISIBLE);
            }
        });

        viewBinding.edtComment.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                viewBinding.tvCountChar.setText(String.valueOf(length));

                if (length > 0) {
                    viewBinding.btnCreateComment.setVisibility(View.VISIBLE);
                } else {
                    viewBinding.btnCreateComment.setVisibility(View.GONE);
                }
            }


            @Override public void afterTextChanged(Editable s) {}
        });

        viewBinding.edtComment.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && !viewModel.isLogin()) {
                if (!viewModel.isLogin()) {
                    ClickUtils.debounceClick(viewBinding.edtComment);
                    showLoginRequiredDialog();
                }
                return true;
            }
            return false;
        });

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (commentParentAdapter != null) {
            commentParentAdapter.stopTimeUpdater();
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_comment;
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
    public void onOpenChildClick(CommentResponse commentResponse) {

    }

    @Override
    public void onDisLikeClick(CommentResponse commentResponse) {

    }

    @Override
    public void onLikeClick(CommentResponse commentResponse) {

    }

    @Override
    public void onReplyClick(CommentResponse commentResponse) {

    }
}