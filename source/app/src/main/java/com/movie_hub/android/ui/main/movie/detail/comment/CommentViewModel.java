package com.movie_hub.android.ui.main.movie.detail.comment;


import androidx.lifecycle.MutableLiveData;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.comment.CommentRequest;
import com.movie_hub.android.data.model.api.request.comment.CreateCommentReactionRequest;
import com.movie_hub.android.data.model.api.request.comment.CreateCommentRequest;
import com.movie_hub.android.data.model.api.response.comment.CommentResponse;
import com.movie_hub.android.data.model.api.response.comment.VoteListResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.ui.base.activity.BaseViewModel;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.utils.NetworkUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;


public class CommentViewModel extends BaseViewModel {
    MovieResponse movieDetails = new MovieResponse();
    public MutableLiveData<List<CommentResponse>> commentList = new MutableLiveData<>();
    public MutableLiveData<List<VoteListResponse>> voteList = new MutableLiveData<>();

    public CommentResponse replyTo = new CommentResponse();
    public MutableLiveData<Long> totalComment = new MutableLiveData<>(0L);
    public CommentViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void mergeOrUpdateComments(List<CommentResponse> newList) {
        if (commentList.getValue() == null) {
            commentList.setValue(new ArrayList<>(newList));
            return;
        }

        List<CommentResponse> currentList = new ArrayList<>(commentList.getValue());
        Map<Long, CommentResponse> map = new HashMap<>();

        for (CommentResponse oldItem : currentList) {
            map.put(oldItem.getId(), oldItem);
        }

        for (CommentResponse newItem : newList) {
            CommentResponse existing = map.get(newItem.getId());

            if (existing != null) {
                // Giữ trạng thái mở comment con nếu đang mở
                if (Boolean.TRUE.equals(existing.getIsOpenChildComment())) {
                    newItem.setIsOpenChildComment(true);
                }

                if (replyTo != null && Objects.equals(replyTo.getId(), existing.getId())) {
                    newItem.setIsOpenChildComment(true);
                }


                // Nếu totalChildren thay đổi → cần gọi lại getListChildComment
                if (newItem.getTotalChildren() == existing.getTotalChildren()) {
                    newItem.setChildComments(existing.getChildComments());
                }
            }

            map.put(newItem.getId(), newItem); // Override
        }

        List<CommentResponse> mergedList = new ArrayList<>(map.values());

        // Sắp xếp pinned + thời gian sửa gần nhất
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        Collections.sort(mergedList, (c1, c2) -> {
            if (c1.isPinned() != c2.isPinned()) {
                return Boolean.compare(c2.isPinned(), c1.isPinned());
            }
            try {
                Date d1 = sdf.parse(c1.getModifiedDate());
                Date d2 = sdf.parse(c2.getModifiedDate());
                return d2.compareTo(d1);
            } catch (ParseException e) {
                return 0;
            }
        });

        commentList.postValue(mergedList);

        if (replyTo.getId() != null) {
            CommentRequest request = new CommentRequest();
            request.setIsOpenChildComment(true);

            if (replyTo.getParent() != null && replyTo.getParent().getId() != null) {
                request.setParentId(replyTo.getParent().getId());
            } else if (replyTo.getId() != null) {
                request.setParentId(replyTo.getId());
            }

            getListChildComment(request);

        }

        replyTo.setId(null);
    }

    public void getListChildComment(CommentRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        request.setSize(1000);
        compositeDisposable.add(repository.getApiService().getCommentList(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable ->
                        throwable.flatMap((Function<Throwable, ObservableSource<?>>) throwable1 -> {
                            if (NetworkUtils.checkNetworkError(throwable1)) {
                                hideLoading();
                                return application.showDialogNoInternetAccess();
                            } else {
                                return Observable.error(throwable1);
                            }
                        })
                )
                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                addChildCommentToList(request, response.getData().getContent());
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                        }
                )
        );
    }
    public void addChildCommentToList(CommentRequest request, List<CommentResponse> data) {
        if (data == null || data.isEmpty()) return;

        List<CommentResponse> list = commentList.getValue();
        if (list == null) return;
        for (CommentResponse comment : list) {
            if (comment != null && request.getParentId().equals(comment.getId())) {
                if (comment.getChildComments() != null) {
                    comment.getChildComments().clear();
                }
                comment.setChildComments(data);
                if (Boolean.TRUE.equals(comment.getIsOpenChildComment())) {
                    comment.setIsOpenChildComment(true);
                }
                commentList.postValue(list);
            }
        }
    }

    public int getTotalComments() {
        if (commentList.getValue() == null) return 0;

        int total = 0;
        for (CommentResponse comment : commentList.getValue()) {
            total += 1; // tính comment cha
            total += comment.getTotalChildren(); // cộng thêm số lượng comment con
        }
        return total;
    }


    public void getVoteList(MainCallback<List<VoteListResponse>> callback) {
        compositeDisposable.add(repository.getApiService().getVoteList(movieDetails.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable ->
                        throwable.flatMap((Function<Throwable, ObservableSource<?>>) throwable1 -> {
                            if (NetworkUtils.checkNetworkError(throwable1)) {
                                hideLoading();
                                return application.showDialogNoInternetAccess();
                            } else {
                                return Observable.error(throwable1);
                            }
                        })
                )
                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                callback.doSuccess(response.getData());
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                        }
                )
        );
    }
    public void getListComment(MainCallback<ResponseListObj<CommentResponse>> callback, CommentRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        compositeDisposable.add(repository.getApiService().getCommentList(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable ->
                        throwable.flatMap((Function<Throwable, ObservableSource<?>>) throwable1 -> {
                            if (NetworkUtils.checkNetworkError(throwable1)) {
                                hideLoading();
                                return application.showDialogNoInternetAccess();
                            } else {
                                return Observable.error(throwable1);
                            }
                        })
                )
                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                callback.doSuccess(response.getData());
                            } else {
                                callback.doFail();
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                            callback.doError(throwable);
                        }
                )
        );
    }

    public void createComment(MainCallback<ResponseWrapper> callback, CreateCommentRequest request) {
        compositeDisposable.add(repository.getApiService().createComment(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable ->
                        throwable.flatMap((Function<Throwable, ObservableSource<?>>) throwable1 -> {
                            if (NetworkUtils.checkNetworkError(throwable1)) {
                                hideLoading();
                                return application.showDialogNoInternetAccess();
                            } else {
                                return Observable.error(throwable1);
                            }
                        })
                )
                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                callback.doSuccess(response);
                            } else {
                                callback.doFail();
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                            callback.doError(throwable);
                        }
                )
        );
    }

    public void voteComment(MainCallback<ResponseWrapper> callback, CreateCommentReactionRequest request) {
        compositeDisposable.add(repository.getApiService().voteComment(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(throwable ->
                        throwable.flatMap((Function<Throwable, ObservableSource<?>>) throwable1 -> {
                            if (NetworkUtils.checkNetworkError(throwable1)) {
                                hideLoading();
                                return application.showDialogNoInternetAccess();
                            } else {
                                return Observable.error(throwable1);
                            }
                        })
                )
                .subscribe(
                        response -> {
                            if (response.isResult()) {
                                callback.doSuccess(response);
                            } else {
                                callback.doFail();
                            }
                        }, throwable -> {
                            Timber.e(throwable);
                            callback.doError(throwable);
                        }
                )
        );
    }
}
