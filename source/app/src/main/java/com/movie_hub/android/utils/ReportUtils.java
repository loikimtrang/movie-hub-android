package com.movie_hub.android.utils;

import android.content.Context;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;
import com.movie_hub.android.data.model.api.ResponseWrapper;
import com.movie_hub.android.data.model.other.ToastMessage;

public class ReportUtils {

    private ReportUtils() {
    }

    public static void showReportFailMessage(Context context, ResponseWrapper response, int reportType) {
        String message;
        if (response != null
                && Constants.CODE_USER_REPORT_ALREADY_EXISTED.equals(response.getCode())) {
            if (reportType == Constants.USER_REPORT_TYPE_COMMENT) {
                message = context.getString(R.string.report_comment_already);
            } else if (reportType == Constants.USER_REPORT_TYPE_REVIEW) {
                message = context.getString(R.string.report_review_already);
            } else {
                message = context.getString(R.string.report_video_already);
            }
        } else {
            message = context.getString(R.string.an_error_occurred);
        }
        new ToastMessage(ToastMessage.TYPE_WARNING, message).showMessage(context);
    }
}
