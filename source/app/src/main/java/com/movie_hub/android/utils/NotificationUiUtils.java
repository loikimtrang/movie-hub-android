package com.movie_hub.android.utils;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.response.MovieItem.MovieItemResponse;
import com.movie_hub.android.data.model.api.response.movie.MovieResponse;
import com.movie_hub.android.data.model.api.response.notification.NotificationResponse;
import com.movie_hub.android.data.model.onesignal.MessageCommentResponse;
import com.movie_hub.android.data.model.onesignal.MessageReviewResponse;
import com.movie_hub.android.data.model.onesignal.MessageRoomNotificationResponse;
import com.movie_hub.android.data.model.onesignal.OneSignalCommand;
import com.movie_hub.android.ui.main.home.notification.model.NotificationDisplayModel;

public final class NotificationUiUtils {

    private NotificationUiUtils() {
    }

    @NonNull
    public static NotificationDisplayModel build(@NonNull Context context, @NonNull NotificationResponse item) {
        NotificationDisplayModel model = new NotificationDisplayModel();
        model.setTitle(!TextUtils.isEmpty(item.getTitle()) ? item.getTitle() : context.getString(R.string.notification));

        String payloadJson = resolvePayloadJson(item);
        String cmd = item.getCmd();

        if (cmd == null) {
            fillFallback(model, item);
            return model;
        }

        switch (cmd) {
            case OneSignalCommand.CMD_REPLY_COMMENT:
            case OneSignalCommand.CMD_TOXIC_COMMENT_LOCKED:
            case OneSignalCommand.CMD_VOTE_COMMENT:
                fillComment(context, model, payloadJson, item, cmd);
                break;
            case OneSignalCommand.CMD_TOXIC_REVIEW_LOCKED:
            case OneSignalCommand.CMD_VOTE_REVIEW:
                fillReview(context, model, payloadJson, item, cmd);
                break;
            case OneSignalCommand.CMD_NEW_MOVIE:
                fillNewMovie(model, payloadJson, item);
                break;
            case OneSignalCommand.CMD_NEW_MOVIE_ITEM:
                fillNewMovieItem(context, model, payloadJson, item);
                break;
            case OneSignalCommand.CMD_ROOM_INVITE:
                fillRoomInvite(context, model, payloadJson, item);
                break;
            default:
                fillFallback(model, item);
                break;
        }

        return model;
    }

    @Nullable
    private static String resolvePayloadJson(NotificationResponse item) {
        if (!TextUtils.isEmpty(item.getData())) {
            return item.getData();
        }
        if (!TextUtils.isEmpty(item.getBody()) && item.getBody().trim().startsWith("{")) {
            return item.getBody();
        }
        return null;
    }

    private static void fillComment(Context context, NotificationDisplayModel model, String json,
                                    NotificationResponse item, String cmd) {
        MessageCommentResponse data = parseJson(json, MessageCommentResponse.class);
        if (data != null) {
            boolean isLocked = OneSignalCommand.CMD_TOXIC_COMMENT_LOCKED.equals(cmd);
            String authorName = resolveAuthorName(data.getAuthor());
            if (!isLocked && !TextUtils.isEmpty(authorName)) {
                model.setSubtitle(context.getString(R.string.notification_from, authorName));
            }
            if (!TextUtils.isEmpty(data.getContent())) {
                model.setPreview(plainText(data.getContent()));
            } else if (!TextUtils.isEmpty(data.getMovieTitle())) {
                model.setPreview(data.getMovieTitle());
            }
            String avatar = data.getAuthor() != null ? data.getAuthor().getAvatarPath() : null;
            setAvatar(model, avatar);
            applyPreviewMask(context, model, cmd, data.getToxicSpans());
        }
        fillFallbackTexts(model, item);
    }

    private static void fillReview(Context context, NotificationDisplayModel model, String json,
                                   NotificationResponse item, String cmd) {
        MessageReviewResponse data = parseJson(json, MessageReviewResponse.class);
        if (data != null) {
            boolean isLocked = OneSignalCommand.CMD_TOXIC_REVIEW_LOCKED.equals(cmd);
            String authorName = resolveAuthorName(data.getAuthor());
            if (!isLocked && !TextUtils.isEmpty(authorName)) {
                model.setSubtitle(context.getString(R.string.notification_from, authorName));
            }
            if (!TextUtils.isEmpty(data.getContent())) {
                model.setPreview(plainText(data.getContent()));
            } else if (!TextUtils.isEmpty(data.getMovieTitle())) {
                model.setPreview(data.getMovieTitle());
            }
            String avatar = data.getAuthor() != null ? data.getAuthor().getAvatarPath() : null;
            setAvatar(model, avatar);
            applyPreviewMask(context, model, cmd, data.getToxicSpans());
        }
        fillFallbackTexts(model, item);
    }

    private static void applyPreviewMask(Context context, NotificationDisplayModel model, String cmd, String toxicSpans) {
        boolean isLocked = OneSignalCommand.CMD_TOXIC_COMMENT_LOCKED.equals(cmd)
                || OneSignalCommand.CMD_TOXIC_REVIEW_LOCKED.equals(cmd);
        boolean hasToxicSpans = !ToxicTextUtils.parseToxicSpans(toxicSpans).isEmpty();
        if (!isLocked && !hasToxicSpans) {
            return;
        }
        model.setPreviewMasked(true);
        model.setToxicSpans(toxicSpans);
        if (isLocked) {
            model.setToxicStatus(ToxicTextUtils.STATUS_TOXIC_LOCKED);
        }
        if (TextUtils.isEmpty(model.getPreview())) {
            model.setPreview(context.getString(R.string.notification_content_hidden));
        }
    }

    private static void fillNewMovie(NotificationDisplayModel model, String json, NotificationResponse item) {
        MovieResponse data = parseJson(json, MovieResponse.class);
        if (data != null) {
            if (!TextUtils.isEmpty(data.getTitle())) {
                model.setPreview(data.getTitle());
            }
        }
        fillFallbackTexts(model, item);
    }

    private static void fillNewMovieItem(Context context, NotificationDisplayModel model, String json, NotificationResponse item) {
        MovieItemResponse data = parseJson(json, MovieItemResponse.class);
        if (data != null) {
            String episode = !TextUtils.isEmpty(data.getLabel())
                    ? context.getString(R.string.episode_char) + data.getLabel()
                    : null;
            String movieTitle = data.getMovie() != null ? data.getMovie().getTitle() : null;
            if (!TextUtils.isEmpty(episode) && !TextUtils.isEmpty(movieTitle)) {
                model.setPreview(episode + " · " + movieTitle);
            } else if (!TextUtils.isEmpty(movieTitle)) {
                model.setPreview(movieTitle);
            } else if (!TextUtils.isEmpty(episode)) {
                model.setPreview(episode);
            }
        }
        fillFallbackTexts(model, item);
    }

    private static void fillRoomInvite(Context context, NotificationDisplayModel model, String json, NotificationResponse item) {
        MessageRoomNotificationResponse data = parseJson(json, MessageRoomNotificationResponse.class);
        if (data != null) {
            MessageRoomNotificationResponse.AccountNotificationDto host = data.getHost();
            String hostName = host != null ? resolveAuthorName(host.getFullName(), host.getUsername(), host.getEmail()) : null;
            if (!TextUtils.isEmpty(hostName)) {
                model.setSubtitle(context.getString(R.string.notification_room_by_host, hostName));
            }
            if (!TextUtils.isEmpty(data.getName())) {
                model.setPreview(data.getName());
            } else if (!TextUtils.isEmpty(data.getMovieTitle())) {
                model.setPreview(data.getMovieTitle());
            }
            if (!TextUtils.isEmpty(data.getStartTime())) {
                String premiere = DisplayUtils.formatDateTime(data.getStartTime());
                if (!TextUtils.isEmpty(premiere)) {
                    String line = context.getString(R.string.notification_premiere_at, premiere);
                    model.setPreview(TextUtils.isEmpty(model.getPreview())
                            ? line
                            : model.getPreview() + "\n" + line);
                }
            }
            if (host != null) {
                setAvatar(model, host.getAvatarPath());
            }
        }
        fillFallbackTexts(model, item);
    }

    private static void fillFallback(NotificationDisplayModel model, NotificationResponse item) {
        fillFallbackTexts(model, item);
    }

    private static void fillFallbackTexts(NotificationDisplayModel model, NotificationResponse item) {
        if (TextUtils.isEmpty(model.getPreview()) && !TextUtils.isEmpty(item.getBody())
                && !item.getBody().trim().startsWith("{")) {
            model.setPreview(plainText(item.getBody()));
        }
        if (TextUtils.isEmpty(model.getTitle()) && !TextUtils.isEmpty(item.getTitle())) {
            model.setTitle(item.getTitle());
        }
    }

    @Nullable
    private static String resolveAuthorName(MessageCommentResponse.Author author) {
        if (author == null) return null;
        return resolveAuthorName(author.getFullName(), author.getUsername(), author.getEmail());
    }

    @Nullable
    private static String resolveAuthorName(MessageReviewResponse.Author author) {
        if (author == null) return null;
        return resolveAuthorName(author.getFullName(), author.getUsername(), author.getEmail());
    }

    @Nullable
    private static String resolveAuthorName(String fullName, String username, String email) {
        if (!TextUtils.isEmpty(fullName)) return fullName.trim();
        if (!TextUtils.isEmpty(username)) return username.trim();
        if (!TextUtils.isEmpty(email)) return email.trim();
        return null;
    }

    private static void setAvatar(NotificationDisplayModel model, @Nullable String path) {
        String url = resolveMediaUrl(path);
        if (!TextUtils.isEmpty(url)) {
            model.setAvatarUrl(url);
            model.setShowAvatar(true);
        }
    }

    @Nullable
    public static String resolveMediaUrl(@Nullable String path) {
        if (TextUtils.isEmpty(path)) return null;
        String trimmed = path.trim();
        if (trimmed.contains("http")) {
            return trimmed;
        }
        return Constants.MEDIA_URL + trimmed;
    }

    @Nullable
    private static String plainText(@Nullable String value) {
        if (TextUtils.isEmpty(value)) return null;
        if (value.contains("<")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY).toString().trim();
            }
            return Html.fromHtml(value).toString().trim();
        }
        return value.trim();
    }

    @Nullable
    private static <T> T parseJson(@Nullable String json, Class<T> clazz) {
        if (TextUtils.isEmpty(json)) return null;
        try {
            return GsonUtils.fromJson(json, clazz);
        } catch (Exception ignored) {
            return null;
        }
    }
}
