package com.movie_hub.android.utils;

import android.content.Context;
import com.movie_hub.android.R;
import com.movie_hub.android.ui.main.schedule.adapter.ScheduleModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateUtils {

    public static String getDayOfMonth(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 00:00:00", Locale.getDefault());
            Date date = sdf.parse(dateStr);
            return new SimpleDateFormat("dd/MM", Locale.getDefault()).format(date);
        } catch (Exception e) {
            return "00/00";
        }
    }

    public static String getDayOfWeek(Context context, String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 00:00:00", Locale.getDefault());
            Date date = sdf.parse(dateStr);

            Calendar now = Calendar.getInstance();
            Calendar target = Calendar.getInstance();
            target.setTime(date);

            boolean isToday = (now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR));

            if (isToday) {
                return context.getString(R.string.today);
            }

            int dayOfWeek = target.get(Calendar.DAY_OF_WEEK);

            switch (dayOfWeek) {
                case Calendar.MONDAY: return context.getString(R.string.monday);
                case Calendar.TUESDAY: return context.getString(R.string.tuesday);
                case Calendar.WEDNESDAY: return context.getString(R.string.wednesday);
                case Calendar.THURSDAY: return context.getString(R.string.thursday);
                case Calendar.FRIDAY: return context.getString(R.string.friday);
                case Calendar.SATURDAY: return context.getString(R.string.saturday);
                case Calendar.SUNDAY: return context.getString(R.string.sunday);
                default: return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static List<ScheduleModel> generate30Days() {
        List<ScheduleModel> list = new ArrayList<>();
        SimpleDateFormat sdfFull = new SimpleDateFormat("dd/MM/yyyy 00:00:00", Locale.getDefault());
        SimpleDateFormat sdfLabel = new SimpleDateFormat("dd/MM", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 30; i++) {
            String fullDate = sdfFull.format(calendar.getTime());
            String dayLabel = sdfLabel.format(calendar.getTime());

            list.add(new ScheduleModel(fullDate, dayLabel, i == 0, i == 0));

            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return list;
    }
}