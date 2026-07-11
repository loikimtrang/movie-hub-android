package com.movie_hub.android.data.model.onesignal;

import androidx.annotation.Nullable;

public final class OneSignalCommand {

    private OneSignalCommand() {
    }

    public static final String CMD_REPLY_COMMENT = "CMD_REPLY_COMMENT";
    public static final String CMD_COMMENT_UNLOCKED = "CMD_COMMENT_UNLOCKED";
    public static final String CMD_NEW_MOVIE = "CMD_NEW_MOVIE";
    public static final String CMD_NEW_MOVIE_ITEM = "CMD_NEW_MOVIE_ITEM";
    public static final String CMD_TOXIC_COMMENT_LOCKED = "CMD_TOXIC_COMMENT_LOCKED";
    public static final String CMD_TOXIC_REVIEW_LOCKED = "CMD_TOXIC_REVIEW_LOCKED";
    public static final String CMD_REVIEW_UNLOCKED = "CMD_REVIEW_UNLOCKED";
    public static final String CMD_ROOM_INVITE = "CMD_ROOM_INVITE";
    public static final String CMD_SEND_NOTIFICATION = "CMD_SEND_NOTIFICATION";
    public static final String CMD_VOTE_COMMENT = "CMD_VOTE_COMMENT";
    public static final String CMD_VOTE_REVIEW = "CMD_VOTE_REVIEW";

    public static boolean isCommentNavigationCmd(@Nullable String cmd) {
        return CMD_REPLY_COMMENT.equals(cmd)
                || CMD_TOXIC_COMMENT_LOCKED.equals(cmd)
                || CMD_COMMENT_UNLOCKED.equals(cmd)
                || CMD_VOTE_COMMENT.equals(cmd);
    }

    public static boolean isReviewNavigationCmd(@Nullable String cmd) {
        return CMD_TOXIC_REVIEW_LOCKED.equals(cmd)
                || CMD_REVIEW_UNLOCKED.equals(cmd)
                || CMD_VOTE_REVIEW.equals(cmd);
    }

    public static boolean shouldRefreshCommentOnForeground(@Nullable String cmd) {
        return CMD_TOXIC_COMMENT_LOCKED.equals(cmd)
                || CMD_COMMENT_UNLOCKED.equals(cmd)
                || CMD_VOTE_COMMENT.equals(cmd);
    }

    public static boolean shouldRefreshReviewOnForeground(@Nullable String cmd) {
        return CMD_TOXIC_REVIEW_LOCKED.equals(cmd)
                || CMD_REVIEW_UNLOCKED.equals(cmd)
                || CMD_VOTE_REVIEW.equals(cmd);
    }

    public static boolean isCommentLockedCmd(@Nullable String cmd) {
        return CMD_TOXIC_COMMENT_LOCKED.equals(cmd);
    }

    public static boolean isReviewLockedCmd(@Nullable String cmd) {
        return CMD_TOXIC_REVIEW_LOCKED.equals(cmd);
    }

    /** Reply + vote notifications show the "from @author" subtitle. */
    public static boolean shouldShowAuthorInNotification(@Nullable String cmd) {
        return CMD_REPLY_COMMENT.equals(cmd)
                || CMD_VOTE_COMMENT.equals(cmd)
                || CMD_VOTE_REVIEW.equals(cmd);
    }

    /** Lock/unlock moderation notifications use the app logo as avatar. */
    public static boolean shouldUseAppLogoInNotification(@Nullable String cmd) {
        return CMD_TOXIC_COMMENT_LOCKED.equals(cmd)
                || CMD_COMMENT_UNLOCKED.equals(cmd)
                || CMD_TOXIC_REVIEW_LOCKED.equals(cmd)
                || CMD_REVIEW_UNLOCKED.equals(cmd);
    }
}
