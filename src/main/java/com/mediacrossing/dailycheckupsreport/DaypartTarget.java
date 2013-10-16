package com.mediacrossing.dailycheckupsreport;

import java.io.Serializable;

public class DaypartTarget implements Serializable{
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

    public int getStartHour() {
        return startHour;
    }

    public int getEndHour() {
        return endHour;
    }

}
