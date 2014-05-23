package com.mediacrossing.finalpacingreport;

import org.joda.time.DateTime;

public class DailyPacingData {

    private DateTime date;
    private int impsDelivered;
    private int impsToDate;
    private int goalToday;
    private int goalToDate;

    public DailyPacingData(DateTime date) {

        this.date = date;
        this.impsDelivered = 0;
        this.impsToDate = 0;
        this.goalToday = 0;
        this.goalToDate = 0;
    }

    public int getImpsDelivered() {
        return impsDelivered;
    }

    public void setImpsDelivered(int impsDelivered) {
        this.impsDelivered = impsDelivered;
    }

    public int getImpsToDate() {
        return impsToDate;
    }

    public void setImpsToDate(int impsToDate) {
        this.impsToDate = impsToDate;
    }

    public int getGoalToday() {
        return goalToday;
    }

    public void setGoalToday(int goalToday) {
        this.goalToday = goalToday;
    }

    public int getGoalToDate() {
        return goalToDate;
    }

    public void setGoalToDate(int goalToDate) {
        this.goalToDate = goalToDate;
    }

    public DateTime getDate() {
        return date;
    }
}
