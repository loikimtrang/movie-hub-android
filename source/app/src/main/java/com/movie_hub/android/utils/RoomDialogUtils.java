package com.movie_hub.android.utils;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.movie_hub.android.R;

public final class RoomDialogUtils {

    private RoomDialogUtils() {
    }

    /**
     * Shown after creating a scheduled room — room code only, with copy + close.
     */
    public static Dialog showRoomCodeDialog(Context context,
                                            @Nullable String roomCode,
                                            @Nullable Runnable onClose) {
        Dialog dialog = createDialog(context, R.layout.layout_dialog_room_code);
        bindCodeRow(dialog, roomCode);
        dialog.findViewById(R.id.btn_close).setOnClickListener(v -> {
            dialog.dismiss();
            if (onClose != null) onClose.run();
        });
        dialog.show();
        applyDialogWindowSize(dialog);
        return dialog;
    }

    /**
     * Room name + code while watching in a live room.
     */
    public static Dialog showRoomInfoDialog(Context context,
                                            @Nullable String roomName,
                                            @Nullable String roomCode) {
        Dialog dialog = createDialog(context, R.layout.layout_dialog_room_info);
        TextView nameView = dialog.findViewById(R.id.tv_room_name);
        String name = !TextUtils.isEmpty(roomName) ? roomName.trim() : context.getString(R.string.watch_together);
        nameView.setText(name);
        bindCodeRow(dialog, roomCode);
        dialog.findViewById(R.id.btn_close).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        applyDialogWindowSize(dialog);
        return dialog;
    }

    private static Dialog createDialog(Context context, int layoutRes) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layoutRes);
        dialog.setCancelable(true);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    /** Full-width window so {@code layout_marginHorizontal} on content defines dialog width (same as {@link DialogUtils}). */
    private static void applyDialogWindowSize(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window == null) return;
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private static void bindCodeRow(Dialog dialog, @Nullable String roomCode) {
        TextView codeView = dialog.findViewById(R.id.tv_room_code);
        ImageView copyBtn = dialog.findViewById(R.id.btn_copy_room_code);
        String code = !TextUtils.isEmpty(roomCode) ? roomCode.trim() : "-";
        codeView.setText(code);

        boolean canCopy = !TextUtils.isEmpty(roomCode);
        copyBtn.setVisibility(canCopy ? View.VISIBLE : View.GONE);
        copyBtn.setOnClickListener(v -> ClipboardUtils.copyRoomCode(dialog.getContext(), roomCode));
        codeView.setOnClickListener(canCopy
                ? v -> ClipboardUtils.copyRoomCode(dialog.getContext(), roomCode)
                : null);
    }
}
