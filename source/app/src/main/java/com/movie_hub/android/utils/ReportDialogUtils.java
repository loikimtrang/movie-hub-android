package com.movie_hub.android.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.content.res.ColorStateList;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.core.content.ContextCompat;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.other.ToastMessage;

public class ReportDialogUtils {

    private static final int VISIBLE_REASON_COUNT = 6;

    private ReportDialogUtils() {
    }

    public interface OnReportSubmitListener {
        void onSubmit(String content);
    }

    public static void show(Context context, String title, OnReportSubmitListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_report);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        TextView tvTitle = dialog.findViewById(R.id.tv_title);
        NestedScrollView scrollReasons = dialog.findViewById(R.id.scroll_reasons);
        RadioGroup radioGroup = dialog.findViewById(R.id.rg_reasons);
        tvTitle.setText(title);

        String[] reasons = context.getResources().getStringArray(R.array.report_reasons);
        int padding = context.getResources().getDimensionPixelSize(R.dimen._8sdp);
        for (int i = 0; i < reasons.length; i++) {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setId(View.generateViewId());
            radioButton.setText(reasons[i]);
            radioButton.setTag(reasons[i]);
            radioButton.setTextColor(ContextCompat.getColor(context, R.color.text_comment));
            radioButton.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)));
            radioButton.setPadding(padding, padding, padding, padding);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            radioButton.setLayoutParams(params);
            radioGroup.addView(radioButton);
        }

        radioGroup.post(() -> limitReasonListHeight(scrollReasons, radioGroup, VISIBLE_REASON_COUNT));

        dialog.findViewById(R.id.btn_close).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btn_send).setOnClickListener(v -> {
            int checkedId = radioGroup.getCheckedRadioButtonId();
            if (checkedId == -1) {
                new ToastMessage(
                        ToastMessage.TYPE_WARNING,
                        context.getString(R.string.report_select_reason)
                ).showMessage(context);
                return;
            }

            RadioButton selected = dialog.findViewById(checkedId);
            String content = selected.getTag() != null
                    ? selected.getTag().toString()
                    : selected.getText().toString();

            dialog.dismiss();
            if (listener != null) {
                listener.onSubmit(content);
            }
        });

        dialog.show();
    }

    private static void limitReasonListHeight(NestedScrollView scrollView, RadioGroup radioGroup, int visibleCount) {
        int childCount = radioGroup.getChildCount();
        if (childCount == 0) {
            return;
        }

        int width = scrollView.getWidth();
        if (width <= 0) {
            width = scrollView.getResources().getDisplayMetrics().widthPixels
                    - scrollView.getResources().getDimensionPixelSize(R.dimen._40sdp) * 2;
        }

        int itemsToMeasure = Math.min(visibleCount, childCount);
        int totalHeight = 0;
        int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST);

        for (int i = 0; i < itemsToMeasure; i++) {
            View child = radioGroup.getChildAt(i);
            child.measure(widthSpec, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += child.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = scrollView.getLayoutParams();
        params.height = totalHeight;
        scrollView.setLayoutParams(params);
    }
}
