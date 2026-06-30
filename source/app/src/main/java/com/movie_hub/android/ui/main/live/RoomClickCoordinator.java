package com.movie_hub.android.ui.main.live;

import android.app.Activity;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.history.ListWatchHistoryResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.room.RoomResponse;
import com.movie_hub.android.data.model.other.ToastMessage;
import com.movie_hub.android.ui.main.MainCallback;
import com.movie_hub.android.utils.DialogUtils;
import com.movie_hub.android.utils.DisplayUtils;

/**
 * Handles room item click flow on any {@link RoomClickHost} screen.
 */
public class RoomClickCoordinator {

    private final RoomClickHost host;

    public RoomClickCoordinator(RoomClickHost host) {
        this.host = host;
    }

    private Activity activity() {
        return host.getHostActivity();
    }

    public void handleRoomClick(RoomResponse model) {
        if (!host.isUserLoggedIn()) {
            host.showLoginRequiredDialog();
            return;
        }
        if (model == null || model.getHost() == null) {
            return;
        }

        if (model.getHost().getId() == host.getUserId()) {
            handleRoomClickByHost(model);
        } else {
            handleRoomClickByClient(model);
        }
    }

    private void handleRoomClickByHost(RoomResponse model) {
        switch (model.getState()) {
            case Constants.ROOM_STATE_PENDING:
                handlePendingPremiereRoomClick(model, true);
                break;
            case Constants.ROOM_STATE_RUNNING:
                startRoom(model);
                break;
            case Constants.ROOM_STATE_ENDING:
                DialogUtils.dialogConfirm(
                        activity(),
                        activity().getString(R.string.msg_room_ended),
                        activity().getString(R.string.title_information),
                        (dialog, which) -> {
                            if (model.getMovieItem() != null && model.getMovieItem().getMovie() != null) {
                                getMovie(model.getMovieItem().getMovie().getId(), model, false, true);
                            }
                        },
                        activity().getString(R.string.back),
                        (dialog, which) -> dialog.dismiss()
                );
                break;
            default:
                break;
        }
    }

    private void handleRoomClickByClient(RoomResponse model) {
        switch (model.getState()) {
            case Constants.ROOM_STATE_PENDING:
                handlePendingPremiereRoomClick(model, false);
                break;
            case Constants.ROOM_STATE_RUNNING:
                getMovie(model.getMovieItem().getMovie().getId(), model, true, false);
                break;
            case Constants.ROOM_STATE_ENDING:
                DialogUtils.dialogConfirm(
                        activity(),
                        activity().getString(R.string.msg_room_ended),
                        activity().getString(R.string.title_information),
                        (dialog, which) -> {
                            getMovie(model.getMovieItem().getMovie().getId(), model, false, false);
                        },
                        activity().getString(R.string.back),
                        (dialog, which) -> dialog.dismiss()
                );
                break;
            default:
                break;
        }
    }

    private boolean isPremiereNotStarted(RoomResponse model) {
        long startTimeMillis = DisplayUtils.parseTimestamp(model.getStartTime());
        return startTimeMillis > 0 && System.currentTimeMillis() < startTimeMillis;
    }

    private boolean isPremiereExpired(RoomResponse model) {
        long endTimeMillis = DisplayUtils.parseTimestamp(model.getEndTime());
        return endTimeMillis > 0 && System.currentTimeMillis() > endTimeMillis;
    }

    private void showPremiereExpiredDialog(RoomResponse model, boolean isHost) {
        DialogUtils.dialogConfirm(
                activity(),
                activity().getString(R.string.msg_premier_expired),
                activity().getString(R.string.title_information),
                (dialog, which) -> {
                    if (model.getMovieItem() != null && model.getMovieItem().getMovie() != null) {
                        getMovie(model.getMovieItem().getMovie().getId(), model, false, isHost);
                    }
                },
                activity().getString(R.string.back),
                (dialog, which) -> dialog.dismiss()
        );
    }

    private void handlePendingPremiereRoomClick(RoomResponse model, boolean isHost) {
        if (isPremiereExpired(model)) {
            showPremiereExpiredDialog(model, isHost);
            return;
        }

        if (isPremiereNotStarted(model)) {
            DialogUtils.dialogConfirm(
                    activity(),
                    activity().getString(R.string.msg_premier_not_started),
                    activity().getString(R.string.title_information),
                    (dialog, which) -> {
                        if (model.getMovieItem() != null && model.getMovieItem().getMovie() != null) {
                            getMovie(model.getMovieItem().getMovie().getId(), model, false, isHost);
                        }
                    },
                    activity().getString(R.string.back),
                    (dialog, which) -> dialog.dismiss()
            );
            return;
        }

        if (isHost) {
            DialogUtils.dialogConfirm(
                    activity(),
                    activity().getString(R.string.msg_premier_ready_ask),
                    activity().getString(R.string.action_start),
                    (dialog, which) -> startRoom(model),
                    activity().getString(R.string.back),
                    (dialog, which) -> dialog.dismiss()
            );
        } else {
            DialogUtils.dialogConfirmSingleButton(
                    activity(),
                    activity().getString(R.string.msg_waiting_for_host),
                    activity().getString(R.string.back),
                    (dialog, which) -> dialog.dismiss()
            );
        }
    }

    private void getMovie(Long id, RoomResponse model, boolean isRoomRunning, boolean isHost) {
        host.fetchMovie(id, new MainCallback<MovieResponse>() {
            @Override
            public void doError(Throwable error) {
                host.hideHostLoading();
                new ToastMessage(ToastMessage.TYPE_WARNING, activity().getString(R.string.an_error_occurred))
                        .showMessage(activity());
            }

            @Override
            public void doSuccess() {
                host.hideHostLoading();
            }

            @Override
            public void doFail() {
                new ToastMessage(ToastMessage.TYPE_WARNING, activity().getString(R.string.an_error_occurred))
                        .showMessage(activity());
                host.hideHostLoading();
            }

            @Override
            public void doSuccess(MovieResponse object) {
                if (isRoomRunning) {
                    joinRoom(object, model, isHost);
                } else if (host.isUserLoggedIn()) {
                    getListMovieTracking(object);
                }
            }
        });
    }

    private void getListMovieTracking(MovieResponse movieResponse) {
        host.showHostLoading();
        host.fetchListMovieTracking(movieResponse.getId(), new MainCallback<ListWatchHistoryResponse>() {
            @Override
            public void doSuccess(ListWatchHistoryResponse data) {
                host.hideHostLoading();
                host.navigateToMovieDetail(movieResponse, data);
            }

            @Override
            public void doError(Throwable error) {
                host.hideHostLoading();
                host.showHostError(activity().getString(R.string.an_error_occurred));
            }

            @Override
            public void doSuccess() {
                host.hideHostLoading();
            }

            @Override
            public void doFail() {
                host.hideHostLoading();
                host.showHostError(activity().getString(R.string.an_error_occurred));
            }
        });
    }

    private void joinRoom(MovieResponse movieResponse, RoomResponse model, boolean isHost) {
        host.fetchJoinRoom(model.getId(), new MainCallback<RoomResponse>() {
            @Override
            public void doError(Throwable error) {
                new ToastMessage(ToastMessage.TYPE_WARNING, activity().getString(R.string.an_error_occurred))
                        .showMessage(activity());
                host.hideHostLoading();
            }

            @Override
            public void doSuccess() {
            }

            @Override
            public void doFail() {
                new ToastMessage(ToastMessage.TYPE_WARNING, activity().getString(R.string.an_error_occurred))
                        .showMessage(activity());
                host.hideHostLoading();
            }

            @Override
            public void doSuccess(RoomResponse object) {
                host.createMqttAndWatch(movieResponse, object, isHost);
            }
        });
    }

    private void startRoom(RoomResponse model) {
        host.showHostLoading();
        host.fetchStartRoom(model.getId(), new MainCallback<RoomResponse>() {
            @Override
            public void doError(Throwable error) {
                host.hideHostLoading();
            }

            @Override
            public void doSuccess() {
                host.hideHostLoading();
            }

            @Override
            public void doFail() {
                host.hideHostLoading();
            }

            @Override
            public void doSuccess(RoomResponse object) {
                getMovie(model.getMovieItem().getMovie().getId(), model, true, true);
            }
        });
    }
}
