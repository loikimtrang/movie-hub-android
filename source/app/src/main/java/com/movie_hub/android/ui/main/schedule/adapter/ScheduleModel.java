package com.movie_hub.android.ui.main.schedule.adapter;

public class ScheduleModel {
    private String fullDate;
    private String dayLabel;
    private boolean isToday;
    private boolean isSelected;

    public ScheduleModel(String fullDate, String dayLabel, boolean isToday, boolean isSelected) {
        this.fullDate = fullDate;
        this.dayLabel = dayLabel;
        this.isToday = isToday;
        this.isSelected = isSelected;
    }

    public String getFullDate() { return fullDate; }
    public String getDayLabel() { return dayLabel; }
    public boolean isToday() { return isToday; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}
