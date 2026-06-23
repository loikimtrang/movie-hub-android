package com.movie_hub.android.ui.main.movie.detail.comment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.movie_hub.android.BR;
import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.comment.CommentRequest;
import com.movie_hub.android.data.model.api.request.comment.CreateCommentReactionRequest;
import com.movie_hub.android.data.model.api.request.comment.CreateCommentRequest;
import com.movie_hub.android.data.model.api.response.comment.CommentResponse;
import com.movie_hub.android.data.model.api.response.comment.VoteListResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.report.CreateReportRequest;
import com.movie_hub.android.data.model.onesignal.MessageCommentResponse;
import com.movie_hub.android.data.model.onesignal.OneSignalCommand;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.databinding.ActivityCommentBinding;
import com.movie_hub.android.di.component.ActivityComponent;
import com.movie_hub.android.ui.base.activity.BaseActivity;
import com.movie_hub.android.ui.base.activity.SystemBarColorProvider;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.ui.main.account.login.LoginActivity;
import com.movie_hub.android.ui.main.movie.detail.comment.adapter.CommentChildAdapter;
import com.movie_hub.android.ui.main.movie.detail.comment.adapter.CommentParentAdapter;
import com.movie_hub.android.ui.main.movie.detail.comment.adapter.EpisodeCommentItemAdapter;
import com.movie_hub.android.ui.main.movie.detail.comment.model.TagComment;
import com.movie_hub.android.ui.main.movie.detail.comment.shimmer.CommentShimmerAdapter;
import com.movie_hub.android.ui.main.movie.detail.comment.shimmer.EpisodeCommentShimmerAdapter;
import com.movie_hub.android.utils.ClickUtils;
import com.movie_hub.android.utils.DialogUtils;
import com.movie_hub.android.utils.ReportDialogUtils;
import com.movie_hub.android.utils.ReportUtils;
import com.movie_hub.android.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;

public class CommentActivity extends BaseActivity<ActivityCommentBinding, CommentViewModel>
        implements SystemBarColorProvider, View.OnClickListener,
        CommentParentAdapter.OnCommentParentClickListener,
        CommentChildAdapter.OnCommentChildClickListener,
        EpisodeCommentItemAdapter.OnEpisodeCommentClickListener {

    private static final float SHEET_TRANSLATE_Y_DP = 200f;
    public int pageSize = 1000;
    private CommentParentAdapter commentParentAdapter;
    private CommentShimmerAdapter commentShimmerAdapter;

    private EpisodeCommentItemAdapter episodeCommentItemAdapter;
    private EpisodeCommentShimmerAdapter episodeCommentShimmerAdapter;
    private ActivityResultLauncher<Intent> loginLauncher;
    private boolean isStateReply = false;
    private boolean isShowShimmer = false;
    public static final String MSG_CMT = "MSG_CMT";
    public static final String MSG_CMD = "MSG_CMD";
    private boolean isShowShimmerComment = false;
    @SuppressLint("SetTextI18n")
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
            if (viewModel.movieDetails.getCommentCount() != null && viewModel.movieDetails.getCommentCount() > 0L) {
                viewModel.totalComment.postValue(viewModel.movieDetails.getCommentCount());
            }
            setUpAdapter();
            showShimmer();
            if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES) {
                viewModel.tagComments = viewModel.movieDetails.getListLabelEpisode(this);
                TagComment tagCommentAll = new TagComment();
                tagCommentAll.setLabel(getString(R.string.all));
                tagCommentAll.setMovieId(viewModel.movieDetails.getId());
                tagCommentAll.setMovieItemId(-1L);
                tagCommentAll.setSelect(true);

                viewModel.tagComments.add(0, tagCommentAll);
                viewModel.tagCommentSelect = viewModel.tagComments.get(0);

                viewBinding.rvEpisode.setVisibility(View.VISIBLE);
                episodeCommentItemAdapter.setData(viewModel.tagComments);
            }

            if (viewModel.isLogin()) {
                getVoteList();
            } else {
                getListComment(viewModel.getCommentRequest());
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
                            getVoteList();
                        }
                    }
                }
        );

        observeCommentList();

        viewModel.totalComment.observe(this, total -> {
            if (total == null || total == 0L) {
                viewBinding.tvCountCmt.setText(getString(R.string.comment));
            } else {
                viewBinding.tvCountCmt.setText(getString(R.string.comment) + " (" + total + ")");
            }
        });
    }

    @Override
    public void onEpisodeClick(TagComment tagComment) {
        for (TagComment comment: viewModel.tagComments) {
            comment.setSelect(false);
            if (tagComment.getMovieItemId().equals(comment.getMovieItemId())) {
                comment.setSelect(true);
                viewModel.tagCommentSelect = tagComment;
            }
        }
        reset();
        if (viewModel.isLogin()) {
            getVoteList();
        } else {
            getListComment(viewModel.getCommentRequest());
        }
    }
    public void reset() {
        viewModel.commentList.setValue(new ArrayList<>());
        viewModel.voteList.setValue(new ArrayList<>());
        viewModel.replyTo = new CommentResponse();
        isShowShimmerComment = false;
        commentParentAdapter.clearData();
        showShimmerComment();
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

    public void showShimmer() {
        showShimmerComment();
        if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES) {
            viewBinding.rvEpisode.setAdapter(episodeCommentShimmerAdapter);
        }
    }

    public void showShimmerComment() {
        viewBinding.rvComment.setAdapter(commentShimmerAdapter);
    }

    public void hideShimmer() {
        isShowShimmer = true;
        hideShimmerComment();
        viewBinding.rvEpisode.setAdapter(episodeCommentItemAdapter);
    }

    public void hideShimmerComment() {
        viewBinding.rvComment.setAdapter(commentParentAdapter);
    }

    @SuppressLint("SetTextI18n")
    public void observeCommentList() {
        viewBinding.rvComment.setItemAnimator(null);
        viewModel.commentList.observe(this, commentList -> {
            if (commentList == null || commentList.isEmpty()) {
                commentParentAdapter.clearData();
                return;
            }
            List<VoteListResponse> voteList = viewModel.voteList.getValue();
            commentParentAdapter.setData(commentList, viewModel.voteList.getValue(), viewModel.tagCommentSelect);

            if (!isShowShimmer) {
                hideShimmer();
                isShowShimmerComment = true;
            }

            if (!isShowShimmerComment) {
                hideShimmerComment();
            }
        });

    }
    public void setUpAdapter() {
        commentParentAdapter = new CommentParentAdapter(this, this, viewModel.getUserId());
        commentShimmerAdapter = new CommentShimmerAdapter(6);
        viewBinding.rvComment.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        if (viewModel.movieDetails.getType() == Constants.TYPE_MOVIE_SERIES) {
            episodeCommentItemAdapter = new EpisodeCommentItemAdapter(this, this);
            episodeCommentShimmerAdapter = new EpisodeCommentShimmerAdapter(6, this);
            viewBinding.rvEpisode.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }
    }

    public void createComment(CreateCommentRequest request) {
        hideStateReply();
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
                    getListComment(viewModel.getCommentRequest());
                    viewBinding.edtComment.setText("");

                    Long total = viewModel.totalComment.getValue();
                    if (total != null) {
                        total++;
                        viewModel.totalComment.postValue(total);
                    }
                }
            }


            @Override
            public void doFail() {

            }
        }, request);
    }
    public void getListComment(CommentRequest request) {
        showLoading();
        viewModel.getListComment(new MainCallback<ResponseListObj<CommentResponse>>() {
            @Override
            public void doSuccess(ResponseListObj<CommentResponse> data) {
                hideLoading();
                if (data.getContent() != null && !data.getContent().isEmpty()) {
                    viewBinding.layoutEmpty.setVisibility(View.GONE);
                    viewBinding.rvComment.setVisibility(View.VISIBLE);

                    LinearLayoutManager lm = (LinearLayoutManager) viewBinding.rvComment.getLayoutManager();
                    int firstVisible = lm.findFirstVisibleItemPosition();
                    View firstVisibleView = lm.findViewByPosition(firstVisible);
                    int offset = firstVisibleView != null ? firstVisibleView.getTop() : 0;

                    viewModel.mergeOrUpdateComments(data.getContent());
                    String json = getIntent().getStringExtra(MSG_CMT);
                    String cmd = getIntent().getStringExtra(MSG_CMD);

                    if (json != null && !json.isEmpty() && viewModel.isLogin()) {
                        getIntent().removeExtra(MSG_CMT);
                        getIntent().removeExtra(MSG_CMD);
                        MessageCommentResponse mCmtResponse = GsonUtils.fromJson(json, MessageCommentResponse.class);
                        int targetPosition = resolveNotificationCommentPosition(mCmtResponse, cmd);

                        if (targetPosition != -1) {
                            int finalTargetPosition = targetPosition;
                            viewBinding.rvComment.post(() -> lm.scrollToPositionWithOffset(finalTargetPosition, 0));
                        } else {
                            viewBinding.rvComment.post(() -> lm.scrollToPositionWithOffset(firstVisible, offset));
                        }
                    } else {
                        viewBinding.rvComment.post(() -> {
                            lm.scrollToPositionWithOffset(firstVisible, offset);
                        });
                    }

                } else {
                    if (!isShowShimmer) {
                        hideShimmer();
                        isShowShimmerComment = true;
                    }

                    if (!isShowShimmerComment) {
                        hideShimmerComment();
                    }
                    viewBinding.rvComment.setVisibility(View.GONE);
                    viewBinding.layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void doError(Throwable error) {
                hideLoading();

            }

            @Override
            public void doSuccess() {
                hideLoading();

            }

            @Override public void doFail() {
                hideLoading();

            }
        }, request);
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
            CharSequence beforeText;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                beforeText = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                viewBinding.tvCountChar.setText(String.valueOf(length));
                viewBinding.btnCreateComment.setVisibility(length > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                ForegroundColorSpan[] spans = s.getSpans(0, s.length(), ForegroundColorSpan.class);
                for (ForegroundColorSpan span : spans) {
                    int spanStart = s.getSpanStart(span);
                    int spanEnd = s.getSpanEnd(span);
                    int cursorPos = viewBinding.edtComment.getSelectionStart();

                    if (cursorPos == spanEnd && beforeText.length() > s.length()) {
                        s.delete(spanStart, spanEnd); // chỉ xoá mention
                        break;
                    }
                }
            }

        });

        viewBinding.edtComment.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && !viewModel.isLogin()) {
                ClickUtils.debounceClick(viewBinding.edtComment);
                showLoginRequiredDialog();
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

        setupKeyboardVisibilityListener();
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
    private void setupKeyboardVisibilityListener() {
        final View rootView = findViewById(android.R.id.content);
        final View contentView = findViewById(R.id.content_view);
        final int defaultMarginTop = getResources().getDimensionPixelSize(R.dimen._80sdp);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentView.getLayoutParams();

            if (keypadHeight > screenHeight * 0.15) {
                if (params.topMargin != 0) {
                    params.topMargin = 0;
                    contentView.setLayoutParams(params);
                }
            } else {
                if (params.topMargin != defaultMarginTop) {
                    params.topMargin = defaultMarginTop;
                    contentView.setLayoutParams(params);
                }
            }
        });
    }

    @Override
    public void onOpenChildClick(CommentResponse commentResponse, List<CommentResponse> items) {
        for (CommentResponse comment: items) {
            if (comment.getId().equals(commentResponse.getId())) {
                if (Boolean.TRUE.equals(commentResponse.getIsOpenChildComment())) {
                    CommentRequest request = new CommentRequest();
                    request.setParentId(commentResponse.getId());
                    request.setIsOpenChildComment(true);
                    viewModel.getListChildComment(request);

                } else {
                    comment.setIsOpenChildComment(false);
                    viewModel.commentList.postValue(items);
                }
                return;
            }
        }
    }

    @Override
    public void onDisLikeClick(CommentResponse commentResponse) {
        if (!viewModel.isLogin()) {
            showLoginRequiredDialog();
            return;
        }

        CreateCommentReactionRequest request = new CreateCommentReactionRequest();
        request.setId(commentResponse.getId());
        request.setType(Constants.REACTION_TYPE_DISLIKE);

        voteComment(request);
    }

    @Override
    public void onLikeClick(CommentResponse commentResponse) {
        if (!viewModel.isLogin()) {
            showLoginRequiredDialog();
            return;
        }

        CreateCommentReactionRequest request = new CreateCommentReactionRequest();
        request.setId(commentResponse.getId());
        request.setType(Constants.REACTION_TYPE_LIKE);

        voteComment(request);
    }
    public void getVoteList() {
        viewModel.getVoteList(new MainCallback<List<VoteListResponse>>() {
            @Override
            public void doError(Throwable error) {

            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doSuccess(List<VoteListResponse> response) {
                viewModel.voteList.postValue(response);
                getListComment(viewModel.getCommentRequest());
            }

            @Override
            public void doFail() {

            }
        });
    }
    public void voteComment(CreateCommentReactionRequest request) {
        viewModel.voteComment(new MainCallback<ResponseWrapper>() {
            @Override
            public void doError(Throwable error) {

            }

            @Override
            public void doSuccess() {

            }

            @Override
            public void doSuccess(ResponseWrapper response) {
                getVoteList();
            }

            @Override
            public void doFail() {

            }
        }, request);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onReplyClick(CommentResponse commentResponse) {
        if (!viewModel.isLogin()) {
            showLoginRequiredDialog();
            return;
        }
        isStateReply = true;
        viewModel.replyTo = commentResponse;
        viewBinding.tvReplyTo.setText(getString(R.string.replying_to) + " @" + commentResponse.getAuthor().getFullName());
        viewBinding.lReplyTo.setVisibility(View.VISIBLE);
        viewBinding.edtComment.requestFocus();
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(viewBinding.edtComment, InputMethodManager.SHOW_IMPLICIT);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_create_comment:
                String content = viewBinding.edtComment.getText().toString().trim();
                if (!content.isEmpty()) {
                    CreateCommentRequest request = viewModel.getCreateCommentRequest();
                    request.setContent(content);

                    if (isStateReply) {
                        if (viewModel.replyTo.getParent() != null && viewModel.replyTo.getParent().getId() != null) {
                            request.setParentId(viewModel.replyTo.getParent().getId());
                            request.setReplyToKind(viewModel.replyTo.getParent().getAuthor().getKind());
                            request.setReplyToId(viewModel.replyTo.getParent().getAuthor().getId());
                        } else if (viewModel.replyTo.getId() != null) {
                            request.setParentId(viewModel.replyTo.getId());
                            request.setReplyToKind(viewModel.replyTo.getAuthor().getKind());
                            request.setReplyToId(viewModel.replyTo.getAuthor().getId());
                        }
                    }

                    createComment(request);
                    viewBinding.edtComment.setText("");
                }
                break;
            case R.id.btn_cancel_cmt:
                hideStateReply();
                break;
            default:
                break;
        }
    }

    public void hideStateReply() {
        viewBinding.lReplyTo.setVisibility(View.GONE);
        isStateReply = false;
        viewBinding.edtComment.setText("");
        viewBinding.tvReplyTo.setText(getString(R.string.replying_to));
    }

    @Override
    public void onLikeChildClick(CommentResponse commentResponse) {
        if (!viewModel.isLogin()) {
            showLoginRequiredDialog();
            return;
        }

        CreateCommentReactionRequest request = new CreateCommentReactionRequest();
        request.setId(commentResponse.getId());
        request.setType(Constants.REACTION_TYPE_LIKE);

        voteComment(request);
    }

    @Override
    public void onDislikeChildClick(CommentResponse commentResponse) {
        if (!viewModel.isLogin()) {
            showLoginRequiredDialog();
            return;
        }

        CreateCommentReactionRequest request = new CreateCommentReactionRequest();
        request.setId(commentResponse.getId());
        request.setType(Constants.REACTION_TYPE_DISLIKE);

        voteComment(request);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onReplyChildClick(CommentResponse commentResponse) {
        if (!viewModel.isLogin()) {
            showLoginRequiredDialog();
            return;
        }
        isStateReply = true;
        viewModel.replyTo = commentResponse;
        viewBinding.tvReplyTo.setText(getString(R.string.replying_to) + " @" + commentResponse.getAuthor().getFullName());
        viewBinding.lReplyTo.setVisibility(View.VISIBLE);
        viewBinding.edtComment.requestFocus();
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(viewBinding.edtComment, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onReportClick(CommentResponse commentResponse) {
        showReportDialog(commentResponse);
    }

    @Override
    public void onReportChildClick(CommentResponse commentResponse) {
        showReportDialog(commentResponse);
    }

    @Override
    public void onDeleteClick(CommentResponse commentResponse) {
        showDeleteCommentConfirmDialog(commentResponse, false);
    }

    @Override
    public void onDeleteChildClick(CommentResponse commentResponse) {
        showDeleteCommentConfirmDialog(commentResponse, true);
    }

    private void showDeleteCommentConfirmDialog(CommentResponse commentResponse, boolean isChildComment) {
        if (!viewModel.isLogin()) {
            showLoginRequiredDialog();
            return;
        }
        if (commentResponse == null || commentResponse.getId() == null) {
            return;
        }

        DialogUtils.dialogConfirm(
                this,
                getString(R.string.delete_comment_confirm),
                getString(R.string.delete),
                (dialog, which) -> deleteComment(commentResponse, isChildComment),
                getString(R.string.cancel),
                null
        );
    }

    private void deleteComment(CommentResponse commentResponse, boolean isChildComment) {
        viewModel.deleteComment(new MainCallback<ResponseWrapper>() {
            @Override
            public void doError(Throwable error) {
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred))
                        .showMessage(CommentActivity.this);
            }

            @Override
            public void doSuccess() {
            }

            @Override
            public void doSuccess(ResponseWrapper response) {
                if (isChildComment) {
                    Long parentId = commentResponse.getParent() != null
                            ? commentResponse.getParent().getId()
                            : null;
                    if (parentId == null) {
                        parentId = viewModel.findParentIdForChild(commentResponse.getId());
                    }
                    if (parentId != null) {
                        viewModel.removeChildCommentFromList(parentId, commentResponse.getId());
                    }
                } else {
                    viewModel.removeCommentFromList(commentResponse.getId());
                }
                new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.delete_comment_success))
                        .showMessage(CommentActivity.this);
            }

            @Override
            public void doFail() {
                new ToastMessage(ToastMessage.TYPE_WARNING, getString(R.string.an_error_occurred))
                        .showMessage(CommentActivity.this);
            }
        }, commentResponse.getId());
    }

    private void showReportDialog(CommentResponse commentResponse) {
        if (!viewModel.isLogin()) {
            showLoginRequiredDialog();
            return;
        }
        if (commentResponse == null || commentResponse.getId() == null) {
            return;
        }

        ReportDialogUtils.show(
                this,
                getString(R.string.report_comment_title),
                content -> submitReport(commentResponse.getId(), Constants.USER_REPORT_TYPE_COMMENT, content)
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
                        .showMessage(CommentActivity.this);
            }

            @Override
            public void doSuccess() {
            }

            @Override
            public void doSuccess(ResponseWrapper response) {
                new ToastMessage(ToastMessage.TYPE_NORMAL, getString(R.string.report_success))
                        .showMessage(CommentActivity.this);
            }

            @Override
            public void doErrorForm(ResponseWrapper response) {
                ReportUtils.showReportFailMessage(
                        CommentActivity.this,
                        response,
                        Constants.USER_REPORT_TYPE_COMMENT
                );
            }

            @Override
            public void doFail() {
            }
        }, request);
    }

    private int resolveNotificationCommentPosition(MessageCommentResponse mCmtResponse, String cmd) {
        if (mCmtResponse == null) return -1;

        List<CommentResponse> currentList = viewModel.commentList.getValue();
        if (currentList == null) return -1;

        if (OneSignalCommand.CMD_TOXIC_COMMENT_LOCKED.equals(cmd)) {
            if (mCmtResponse.getId() == null) return -1;
            long commentId = Long.parseLong(mCmtResponse.getId());

            if (mCmtResponse.getParentId() != null && !mCmtResponse.getParentId().isEmpty()) {
                long parentId = Long.parseLong(mCmtResponse.getParentId());
                for (int i = 0; i < currentList.size(); i++) {
                    if (currentList.get(i).getId() != null && currentList.get(i).getId() == parentId) {
                        currentList.get(i).setIsOpenChildComment(true);
                        CommentRequest commentRequest = new CommentRequest();
                        commentRequest.setParentId(parentId);
                        commentRequest.setIsOpenChildComment(true);
                        viewModel.getListChildComment(commentRequest);
                        return i;
                    }
                }
            }

            for (int i = 0; i < currentList.size(); i++) {
                if (currentList.get(i).getId() != null && currentList.get(i).getId() == commentId) {
                    return i;
                }
            }
            return -1;
        }

        if (mCmtResponse.getParentId() == null || mCmtResponse.getParentId().isEmpty()) return -1;

        long targetId = Long.parseLong(mCmtResponse.getParentId());
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).getId() != null && currentList.get(i).getId() == targetId) {
                currentList.get(i).setIsOpenChildComment(true);
                CommentRequest commentRequest = new CommentRequest();
                commentRequest.setParentId(targetId);
                commentRequest.setIsOpenChildComment(true);
                viewModel.getListChildComment(commentRequest);
                return i;
            }
        }
        return -1;
    }
}