package com.movie_hub.android.ui.main.movie.detail.review;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.RequestToMapConverter;
import com.movie_hub.android.data.model.api.ResponseListObj;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.api.request.review.CreateReviewReactionRequest;
import com.movie_hub.android.data.model.api.request.review.CreateReviewRequest;
import com.movie_hub.android.data.model.api.request.review.ReviewRequest;
import com.movie_hub.android.data.model.api.response.comment.VoteListResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.review.ReviewResponse;
import com.movie_hub.android.ui.base.activity.BaseViewModel;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class ReviewViewModel extends BaseViewModel {
    public MovieResponse movieDetails = new MovieResponse();
    public List<ReviewResponse> listReview = new ArrayList<>();
    public List<VoteListResponse> voteListResponses = new ArrayList<>();
    public ReviewViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public List<ReviewResponse> setupVoteList(List<ReviewResponse> reviewResponseList) {
        if (voteListResponses == null || voteListResponses.isEmpty()) return reviewResponseList;

        for (ReviewResponse review : reviewResponseList) {
            review.setLike(false);
            review.setDislike(false);

            for (VoteListResponse vote : voteListResponses) {
                if (vote.getId().equals(review.getId())) {
                    if (vote.getType() == Constants.REACTION_TYPE_LIKE) {
                        review.setLike(true);
                    } else if (vote.getType() == Constants.REACTION_TYPE_DISLIKE) {
                        review.setDislike(true);
                    }
                    break; // Đã match rồi thì khỏi check tiếp
                }
            }
        }

        return reviewResponseList;
    }

    public void updateVoteList(CreateReviewReactionRequest request) {
        if (voteListResponses == null) {
            voteListResponses = new ArrayList<>();
        }

        int index = -1;
        for (int i = 0; i < voteListResponses.size(); i++) {
            if (voteListResponses.get(i).getId().equals(request.getId())) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            VoteListResponse newVote = new VoteListResponse();
            newVote.setId(request.getId());
            newVote.setType(request.getType());
            voteListResponses.add(newVote);
        } else {
            VoteListResponse existingVote = voteListResponses.get(index);
            if (existingVote.getType() == request.getType()) {
                voteListResponses.remove(index);
            } else {
                existingVote.setType(request.getType());
            }
        }
    }

    public ReviewResponse handleVoteStatus(ReviewResponse review) {
        if (review == null) return null;

        // Reset trạng thái
        boolean wasLike = review.isLike();
        boolean wasDislike = review.isDislike();

        review.setLike(false);
        review.setDislike(false);

        if (voteListResponses == null || voteListResponses.isEmpty()) {
            // Nếu trước đó có like/dislike thì trừ đi
            if (wasLike) {
                review.setTotalLike(Math.max(0, (review.getTotalLike() == null ? 0 : review.getTotalLike()) - 1));
            }
            if (wasDislike) {
                review.setTotalDislike(Math.max(0, (review.getTotalDislike() == null ? 0 : review.getTotalDislike()) - 1));
            }
            return review;
        }

        boolean foundVote = false;

        for (VoteListResponse vote : voteListResponses) {
            if (vote.getId() != null && vote.getId().equals(review.getId())) {
                int type = vote.getType();
                foundVote = true;

                if (type == Constants.REACTION_TYPE_LIKE) {
                    if (!wasLike) {
                        review.setTotalLike((review.getTotalLike() == null ? 0 : review.getTotalLike()) + 1);
                    }
                    if (wasDislike) {
                        review.setTotalDislike(Math.max(0, (review.getTotalDislike() == null ? 0 : review.getTotalDislike()) - 1));
                    }
                    review.setLike(true);
                    review.setDislike(false);
                } else if (type == Constants.REACTION_TYPE_DISLIKE) {
                    if (!wasDislike) {
                        review.setTotalDislike((review.getTotalDislike() == null ? 0 : review.getTotalDislike()) + 1);
                    }
                    if (wasLike) {
                        review.setTotalLike(Math.max(0, (review.getTotalLike() == null ? 0 : review.getTotalLike()) - 1));
                    }
                    review.setDislike(true);
                    review.setLike(false);
                }
                break;
            }
        }

        if (!foundVote) {
            if (wasLike) {
                review.setTotalLike(Math.max(0, (review.getTotalLike() == null ? 0 : review.getTotalLike()) - 1));
            }
            if (wasDislike) {
                review.setTotalDislike(Math.max(0, (review.getTotalDislike() == null ? 0 : review.getTotalDislike()) - 1));
            }
        }

        return review;
    }



    public void getListReview(MainCallback<ResponseListObj<ReviewResponse>> callback, ReviewRequest request) {
        Map<String, Object> query = RequestToMapConverter.convert(request);
        request.setSize(1000);
        compositeDisposable.add(repository.getApiService().getReviewList(query)
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

    public void creteReview(MainCallback<ReviewResponse> callback, CreateReviewRequest request) {
        compositeDisposable.add(repository.getApiService().createReview(request)
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

    public void creteVote(MainCallback<ResponseWrapper> callback, CreateReviewReactionRequest request) {
        compositeDisposable.add(repository.getApiService().voteReview(request)
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

    public void checkIsReview(MainCallback<ReviewResponse> callback, Long movieId) {
        compositeDisposable.add(repository.getApiService().checkReview(movieId)
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

    public void getVoteList(MainCallback<List<VoteListResponse>> callback, Long movieId) {
        compositeDisposable.add(repository.getApiService().getVoteListReview(movieId)
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

    public void voteReview(MainCallback<ResponseWrapper> callback, CreateReviewReactionRequest request) {
        compositeDisposable.add(repository.getApiService().voteReview(request)
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
