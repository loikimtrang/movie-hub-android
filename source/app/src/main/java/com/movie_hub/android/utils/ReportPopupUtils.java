package com.movie_hub.android.utils;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.movie_hub.android.R;

public class ReportPopupUtils {

    private ReportPopupUtils() {
    }

    public interface OnReportClickListener {
        void onReportClick();
    }

    public interface OnEditClickListener {
        void onEditClick();
    }

    public interface OnDeleteClickListener {
        void onDeleteClick();
    }

    public static void showCommentMorePopup(Context context,
                                            View anchor,
                                            boolean isOwnContent,
                                            OnReportClickListener reportListener,
                                            OnEditClickListener editListener,
                                            OnDeleteClickListener deleteListener) {
        View popupView = LayoutInflater.from(context).inflate(R.layout.layout_popup_report_option, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(20);

        View btnReport = popupView.findViewById(R.id.btn_report);
        View btnEdit = popupView.findViewById(R.id.btn_edit);
        View btnDelete = popupView.findViewById(R.id.btn_delete);

        if (isOwnContent) {
            btnReport.setVisibility(View.GONE);
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            btnEdit.setOnClickListener(v -> {
                popupWindow.dismiss();
                if (editListener != null) {
                    editListener.onEditClick();
                }
            });
            btnDelete.setOnClickListener(v -> {
                popupWindow.dismiss();
                if (deleteListener != null) {
                    deleteListener.onDeleteClick();
                }
            });
        } else {
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            btnReport.setVisibility(View.VISIBLE);
            btnReport.setOnClickListener(v -> {
                popupWindow.dismiss();
                if (reportListener != null) {
                    reportListener.onReportClick();
                }
            });
        }

        popupView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        int xOffset = anchor.getWidth() - popupView.getMeasuredWidth();
        popupWindow.showAsDropDown(anchor, xOffset, 0);
    }

    @Deprecated
    public static void showReportPopup(Context context, View anchor, OnReportClickListener listener) {
        showCommentMorePopup(context, anchor, false, listener, null, null);
    }
}
