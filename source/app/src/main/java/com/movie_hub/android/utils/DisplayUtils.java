package com.movie_hub.android.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;

import java.text.ParseException;
import java.util.Date;

public class DisplayUtils {

    private DisplayUtils(){
        //do not initial me
    }

    public static int px2dp(float pxValue, Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    public static int dp2px(float dipValue, Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int px2sp(float pxValue, Context context) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int sp2px(float spValue, Context context) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static void showSoftInput(Context context) {
        InputMethodManager imm =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE); // 显示软键盘
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showSoftInput(Context context, View view) {
        InputMethodManager imm =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE); // 显示软键盘
        imm.showSoftInput(view, 0);
    }

    public static void hideSoftInput(Context context, View view) {
        InputMethodManager immHide =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE); // 隐藏软键盘
        immHide.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String displayAgeRating(int ageRating) {
        switch (ageRating) {
            case Constants.AGE_RATING_G:
                return "G";
            case Constants.AGE_RATING_PG:
                return "PG";
            case Constants.AGE_RATING_PG13:
                return "PG-13";
            case Constants.AGE_RATING_R:
                return "R";
            case Constants.AGE_RATING_NC17:
                return "NC-17";
            case Constants.AGE_RATING_18:
                return "18+";
            default:
                return "N/A";
        }
    }

    @SuppressLint("NewApi")
    public static String getYearFromReleaseDate(String releaseDate) {
        if (releaseDate == null || releaseDate.isEmpty()) return "";
        try {
            @SuppressLint({"NewApi", "LocalSuppress"}) SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            @SuppressLint({"NewApi", "LocalSuppress"}) Date date = inputFormat.parse(releaseDate);
            @SuppressLint({"NewApi", "LocalSuppress"}) SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy");
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String displayTimeFromSeconds(Context context, Long totalSeconds) {
        if (context == null) return "";

        Long hours = totalSeconds / 3600;
        Long minutes = (totalSeconds % 3600) / 60;

        String hourStr = context.getString(R.string.hour);
        String minuteStr = context.getString(R.string.minute);

        StringBuilder sb = new StringBuilder();

        if (hours > 0) {
            sb.append(hours).append(hourStr);
            if (minutes > 0) sb.append(" ").append(minutes).append(minuteStr);
        } else {
            sb.append(minutes).append(minuteStr);
        }

        return sb.toString().trim();
    }

}
