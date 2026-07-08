package com.movie_hub.android.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.content.res.ColorStateList;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.core.content.ContextCompat;

import com.movie_hub.android.R;
import com.movie_hub.android.data.model.other.ToastMessage;

public class ReportDialogUtils {

    private ReportDialogUtils() {
    }

    public interface OnReportSubmitListener {
        void onSubmit(String content);
    }

    public static void show(Context context, String title, OnReportSubmitListener listener) {
        show(context, title, R.array.report_reasons, listener);
    }

    public static void show(Context context, String title, int reasonsArrayResId, OnReportSubmitListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_report);
        dialog.setCancelable(true);

        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int dialogHeight = (int) (screenHeight * 0.82f);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                            | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
            );
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dialogHeight
            );
        }

        TextView tvTitle = dialog.findViewById(R.id.tv_title);
        NestedScrollView scrollReasons = dialog.findViewById(R.id.scroll_reasons);
        RadioGroup radioGroup = dialog.findViewById(R.id.rg_reasons);
        EditText edtOtherReason = dialog.findViewById(R.id.edt_other_reason);
        tvTitle.setText(title);

        String otherReasonLabel = context.getString(R.string.report_reason_other);
        String[] reasons = context.getResources().getStringArray(reasonsArrayResId);
        int padding = context.getResources().getDimensionPixelSize(R.dimen._8sdp);
        for (String reason : reasons) {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setId(View.generateViewId());
            radioButton.setText(reason);
            radioButton.setTag(reason);
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

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selected = dialog.findViewById(checkedId);
            boolean isOther = selected != null && otherReasonLabel.equals(String.valueOf(selected.getTag()));
            edtOtherReason.setVisibility(isOther ? View.VISIBLE : View.GONE);
            if (!isOther) {
                edtOtherReason.setText("");
            } else {
                edtOtherReason.requestFocus();
                scrollReasons.post(() -> scrollReasons.fullScroll(View.FOCUS_DOWN));
            }
        });

        setupKeyboardVisibilityListener(dialog, dialogHeight);

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

            if (otherReasonLabel.equals(content)) {
                content = edtOtherReason.getText().toString().trim();
                if (content.isEmpty()) {
                    new ToastMessage(
                            ToastMessage.TYPE_WARNING,
                            context.getString(R.string.report_enter_other_reason)
                    ).showMessage(context);
                    return;
                }
            }

            dialog.dismiss();
            if (listener != null) {
                listener.onSubmit(content);
            }
        });

        dialog.show();
    }

    private static void setupKeyboardVisibilityListener(Dialog dialog, int defaultDialogHeight) {
        if (dialog.getWindow() == null) {
            return;
        }

        View decorView = dialog.getWindow().getDecorView();
        ViewTreeObserver.OnGlobalLayoutListener keyboardListener = () -> {
            if (dialog.getWindow() == null) {
                return;
            }

            Rect visibleFrame = new Rect();
            decorView.getWindowVisibleDisplayFrame(visibleFrame);
            int screenHeight = decorView.getRootView().getHeight();
            int keypadHeight = screenHeight - visibleFrame.bottom;

            int targetHeight = keypadHeight > screenHeight * 0.15
                    ? visibleFrame.height()
                    : defaultDialogHeight;

            ViewGroup.LayoutParams layoutParams = decorView.getLayoutParams();
            if (layoutParams != null && layoutParams.height != targetHeight) {
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, targetHeight);
            }
        };

        decorView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardListener);
        dialog.setOnDismissListener(d ->
                decorView.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardListener)
        );
    }
}
