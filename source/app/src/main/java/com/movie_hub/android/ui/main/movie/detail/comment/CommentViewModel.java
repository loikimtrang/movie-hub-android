package com.movie_hub.android.ui.main.movie.detail.comment;


import android.annotation.SuppressLint;

import androidx.lifecycle.MutableLiveData;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.comment.CommentRequest;
import com.movie_hub.android.data.model.api.request.comment.CreateCommentReactionRequest;
import com.movie_hub.android.data.model.api.request.comment.CreateCommentRequest;
import com.movie_hub.android.data.model.api.response.comment.CommentResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.ui.base.activity.BaseViewModel;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.utils.NetworkUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CommentViewModel extends BaseViewModel {
    MovieResponse movieDetails = new MovieResponse();
    public MutableLiveData<List<CommentResponse>> commentList = new MutableLiveData<>();
    public CommentViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

//    public void mergeOrUpdateComments(List<CommentResponse> newList) {
//        if (commentList.getValue() == null) {
//            commentList.setValue(new ArrayList<>(newList));
//            return;
//        }
//
//        List<CommentResponse> currentList = new ArrayList<>(commentList.getValue());
//        Map<Long, CommentResponse> map = new HashMap<>();
//
//        // Thêm dữ liệu cũ vào map
//        for (CommentResponse item : currentList) {
//            map.put(item.getId(), item);
//        }
//
//        // Ghi đè nếu id trùng
//        for (CommentResponse item : newList) {
//            map.put(item.getId(), item);
//        }
//
//        // Tạo list mới từ map và sort theo modifiedDate mới nhất lên đầu
//        List<CommentResponse> mergedList = new ArrayList<>(map.values());
//
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
//        Collections.sort(mergedList, (c1, c2) -> {
//            try {
//                Date d1 = sdf.parse(c1.getModifiedDate());
//                Date d2 = sdf.parse(c2.getModifiedDate());
//                return d2.compareTo(d1); // newest first
//            } catch (ParseException e) {
//                return 0;
//            }
//        });
//
//        commentList.setValue(mergedList);
//    }
@SuppressLint("SimpleDateFormat")
public void mergeOrUpdateComments(List<CommentResponse> newList) {
    if (commentList.getValue() == null) {
        commentList.setValue(new ArrayList<>(newList));
        return;
    }

    List<CommentResponse> currentList = new ArrayList<>(commentList.getValue());
    Map<Long, CommentResponse> map = new HashMap<>();

    // Bỏ vào map để tránh trùng
    for (CommentResponse item : currentList) {
        map.put(item.getId(), item);
    }
    for (CommentResponse item : newList) {
        map.put(item.getId(), item); // override nếu trùng ID
    }

    // Tạo list mới từ map
    List<CommentResponse> mergedList = new ArrayList<>(map.values());

    // Sort: isPinned true lên trước, rồi sort theo modifiedDate mới nhất
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

    Collections.sort(mergedList, (c1, c2) -> {
        // Ưu tiên pin
        if (c1.isPinned() != c2.isPinned()) {
            return Boolean.compare(c2.isPinned(), c1.isPinned()); // true trước
        }

        try {
            Date d1 = sdf.parse(c1.getModifiedDate());
            Date d2 = sdf.parse(c2.getModifiedDate());
            return d2.compareTo(d1); // newest first
        } catch (ParseException e) {
            return 0;
        }
    });

    commentList.setValue(mergedList);
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
