package com.movie_hub.android.custom;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.movie_hub.android.R;

public class CustomDialog {

    public interface DialogCallback {
        void onConfirm();
        void onCancel();
    }

    public static void show(Context context, int idStringMgs,DialogCallback callback) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog);
        dialog.setCancelable(false);

        TextView tvMessage = dialog.findViewById(R.id.dialog_message);
        TextView btnOk = dialog.findViewById(R.id.btn_ok);
        TextView btnCancel = dialog.findViewById(R.id.btn_cancel);

        tvMessage.setText(idStringMgs);
        btnOk.setText(R.string.confirm);
        btnCancel.setText(R.string.cancel);

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) callback.onConfirm();
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) callback.onCancel();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.show();
    }
}
