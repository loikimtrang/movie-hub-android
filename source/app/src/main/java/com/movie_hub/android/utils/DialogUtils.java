package com.movie_hub.android.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.movie_hub.android.R;

public class DialogUtils {

    private DialogUtils(){
        //do not init
    }

    public static Dialog dialogConfirm(Context context,
                                       String msg,
                                       String btnPositive,
                                       DialogInterface.OnClickListener positive,
                                       String btnNegative,
                                       DialogInterface.OnClickListener negative) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        TextView messageView = dialog.findViewById(R.id.dialog_message);
        messageView.setText(msg);

        TextView btnOk = dialog.findViewById(R.id.btn_ok);
        btnOk.setText(btnPositive);
        btnOk.setOnClickListener(v -> {
            if (positive != null) positive.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
            dialog.dismiss();
        });

        TextView btnCancel = dialog.findViewById(R.id.btn_cancel);
        btnCancel.setText(btnNegative);
        btnCancel.setOnClickListener(v -> {
            if (negative != null) negative.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
            dialog.dismiss();
        });

        dialog.show();
        return dialog;
    }

    public static Dialog dialogConfirmSingleButton(Context context,
                                                   String msg,
                                                   String btnPositive,
                                                   DialogInterface.OnClickListener positive) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView messageView = dialog.findViewById(R.id.dialog_message);
        messageView.setText(msg);

        TextView btnOk = dialog.findViewById(R.id.btn_ok);
        btnOk.setText(btnPositive);
        btnOk.setOnClickListener(v -> {
            if (positive != null) positive.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
            dialog.dismiss();
        });

        TextView btnCancel = dialog.findViewById(R.id.btn_cancel);
        btnCancel.setVisibility(View.GONE); // 👈 Ẩn luôn nút Cancel

        dialog.show();
        return dialog;
    }


    public static Dialog createDialogLoading(Context context, String msg) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);

        View layout = inflater.inflate(R.layout.layout_progressbar, null);
//        if(msg!=null) {
//            TextView progressbarMsg = (TextView) layout.findViewById(R.id.progressbar_msg);
//            progressbarMsg.setText(msg);
//        }

        builder.setCancelable(false); // if you want user to wait for some process to finish,
        builder.setView(layout);
        return builder.create();
    }
}
