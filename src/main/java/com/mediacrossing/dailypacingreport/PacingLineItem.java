package com.mediacrossing.dailypacingreport;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import play.libs.F;

import java.util.ArrayList;

public class PacingLineItem {

    private String advertiserId;
    private String name;
    private DateTime startDate;
    private DateTime endDate;
    private int lifetimeBudget;
    private int daysActive;
    private ArrayList<F.Tuple<DateTime, Integer>> dailyData = new ArrayList<>();

    public PacingLineItem(String advertiserId, String name, DateTime startDate, DateTime endDate, int lifetimeBudget) {
        this.advertiserId = advertiserId;
        this.name = name;
        this.startDate = startDate.withTimeAtStartOfDay();
        this.endDate = endDate.withTimeAtStartOfDay();
        this.lifetimeBudget = lifetimeBudget;
        this.daysActive = (int)new Duration(startDate, endDate).getStandardDays();
    }

    public PacingLineItem() {
    }

    public String getAdvertiserId() {
        return advertiserId;
    }

    public String getName() {
        return name;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public int getLifetimeBudget() {
        return lifetimeBudget;
    }

    public int getDaysActive() {
        return daysActive;
    }

    public ArrayList<F.Tuple<DateTime, Integer>> getDailyData() {
        return dailyData;
    }
}
