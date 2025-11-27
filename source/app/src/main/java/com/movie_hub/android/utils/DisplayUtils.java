package com.movie_hub.android.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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
    public static String displayAgeRatingDescription(Context context, int ageRating) {
        switch (ageRating) {
            case Constants.AGE_RATING_G:
                return context.getString(R.string.age_rating_g);
            case Constants.AGE_RATING_PG:
                return context.getString(R.string.age_rating_pg);
            case Constants.AGE_RATING_PG13:
                return context.getString(R.string.age_rating_pg13);
            case Constants.AGE_RATING_R:
                return context.getString(R.string.age_rating_r);
            case Constants.AGE_RATING_NC17:
                return context.getString(R.string.age_rating_nc17);
            case Constants.AGE_RATING_18:
                return context.getString(R.string.age_rating_age18);
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

    @SuppressLint("NewApi")
    public static String displayShortDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "";

        try {
            // Chuỗi đầu vào có giờ: "27/01/1969 00:00:00"
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(rawDate);

            // Định dạng cần xuất: "27/01/1969"
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String displayGender(Context context, int gender) {
        switch (gender) {
            case Constants.GENDER_MALE:
                return context.getString(R.string.male);
            case Constants.GENDER_FEMALE:
                return context.getString(R.string.female);
            case Constants.GENDER_UNSPECIFIED:
                return context.getString(R.string.unspecified);
            default:
                return context.getString(R.string.updating);
        }
    }

    public static String formatSecondsToHHMMSS(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        // Format với padding zero nếu cần
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }
    public static String getRemainingTimeText(Context context, long currentSeconds, long totalSeconds) {
        long remaining = totalSeconds - currentSeconds;

        if (remaining <= 0) {
            remaining = 0;
        } else if (remaining < 60) {
            remaining = 60;
        }

        long hours = remaining / 3600;
        long minutes = (remaining % 3600) / 60;

        String hourStr = context.getString(R.string.hour);       // "h"
        String minuteStr = context.getString(R.string.minute);   // "m"
        String remainingStr = context.getString(R.string.remaining); // "remaining"

        StringBuilder result = new StringBuilder(remainingStr + " ");

        if (hours > 0) {
            result.append(hours).append(hourStr);
        }

        if (minutes > 0 || hours == 0) {
            if (hours > 0) result.append(" ");
            result.append(minutes).append(minuteStr);
        }

        return result.toString().trim();
    }
    @SuppressLint("SimpleDateFormat")
    public static String getTimeAgo(Context context, String rawTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // input là UTC
            Date commentDate = sdf.parse(rawTime);

            long utcTime = commentDate.getTime(); // giữ nguyên
            long now = System.currentTimeMillis(); // local time của máy
            long diff = now - utcTime;

            long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            long weeks = days / 7;

            if (seconds < 60) return context.getString(R.string.just_now);
            if (minutes < 60) return minutes + " " + context.getString(R.string.minutes_ago);
            if (hours < 24) return hours + " " + context.getString(R.string.hours_ago);
            if (days < 7) return days + " " + context.getString(R.string.days_ago);
            return weeks + " " + context.getString(R.string.weeks_ago);

        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }



}
