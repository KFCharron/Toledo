package com.mediacrossing.dailypacingreport;

import org.joda.time.DateTime;

public class WeeklyTotals {

    private DateTime date;
    private int impGoalByWeek;
    private int impGoalToDate;
    private int impActualByWeek;
    private int impActualToDate;

    public WeeklyTotals(DateTime date, int impGoalByWeek, int impGoalToDate, int impActualByWeek, int impActualToDate) {
        this.date = date;
        this.impGoalByWeek = impGoalByWeek;
        this.impGoalToDate = impGoalToDate;
        this.impActualByWeek = impActualByWeek;
        this.impActualToDate = impActualToDate;
    }

    public DateTime getDate() {
        return date;
    }

    public int getImpGoalByWeek() {
        return impGoalByWeek;
    }

    public int getImpGoalToDate() {
        return impGoalToDate;
    }

    public int getImpActualByWeek() {
        return impActualByWeek;
    }

    public int getImpActualToDate() {
        return impActualToDate;
    }
}
