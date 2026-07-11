package com.movie_hub.android.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.annotation.Nullable;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.other.ToastMessage;

public final class ClipboardUtils {

    private ClipboardUtils() {
    }

    public static boolean copyToClipboard(Context context, @Nullable String label, @Nullable String text) {
        if (context == null || text == null || text.trim().isEmpty()) {
            return false;
        }
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return false;
        }
        String clipLabel = label != null && !label.isEmpty() ? label : "text";
        clipboard.setPrimaryClip(ClipData.newPlainText(clipLabel, text.trim()));
        return true;
    }

    public static void copyRoomCode(Context context, @Nullable String roomCode) {
        if (!copyToClipboard(context, "room_code", roomCode)) {
            return;
        }
        new ToastMessage(ToastMessage.TYPE_NORMAL, context.getString(R.string.room_code_copied))
                .showMessage(context);
    }
}
