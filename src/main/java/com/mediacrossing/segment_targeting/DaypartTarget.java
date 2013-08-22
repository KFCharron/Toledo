package com.mediacrossing.segment_targeting;

/**
 * Created with IntelliJ IDEA.
 * User: charronkyle
 * Date: 8/22/13
 * Time: 10:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class DaypartTarget {
    private String day;
    private int startHour;
    private int endHour;

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
