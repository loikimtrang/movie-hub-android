package com.movie_hub.android.ui.main.live.create;

import android.text.TextUtils;

import com.movie_hub.android.data.model.api.response.user.UserResponse;

public final class RoomMemberUiUtils {

    private RoomMemberUiUtils() {
    }

    public static String getPrimaryName(UserResponse user) {
        if (user == null) return "";
        if (!TextUtils.isEmpty(user.getFullName())) return user.getFullName().trim();
        if (!TextUtils.isEmpty(user.getUsername())) return user.getUsername().trim();
        if (!TextUtils.isEmpty(user.getEmail())) return user.getEmail().trim();
        return "";
    }

    /** Username first, then email. Null when neither is available. */
    public static String getSecondaryLabel(UserResponse user) {
        if (user == null) return null;
        if (!TextUtils.isEmpty(user.getUsername())) return "@" + user.getUsername().trim();
        if (!TextUtils.isEmpty(user.getEmail())) return user.getEmail().trim();
        return null;
    }

    public static String getChipLabel(UserResponse user) {
        return getPrimaryName(user);
    }
}
