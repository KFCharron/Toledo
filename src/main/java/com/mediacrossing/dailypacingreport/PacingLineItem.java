package com.mediacrossing.dailypacingreport;

import org.joda.time.DateTime;

import java.util.ArrayList;

public class PacingLineItem {

    private String advertiserId;
    private String name;
    private DateTime startDate;
    private DateTime endDate;
    private int lifetimeBudget;
    private ArrayList<ImpressionDateBudget> dailyData = new ArrayList<ImpressionDateBudget>();

    public PacingLineItem(String advertiserId, String name, DateTime startDate, DateTime endDate, int lifetimeBudget) {
        this.advertiserId = advertiserId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.lifetimeBudget = lifetimeBudget;
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

    public ArrayList<ImpressionDateBudget> getDailyData() {
        return dailyData;
    }
}
