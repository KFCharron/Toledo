package com.mediacrossing.segmenttargeting;

public class DaypartTarget {
    private String day;
    private int startHour;
    private int endHour;

    public DaypartTarget(String day, int start, int end) {
        this.day = day;
        this.startHour = start;
        this.endHour = end;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }
}
