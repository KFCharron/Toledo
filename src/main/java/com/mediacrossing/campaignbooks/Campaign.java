package com.mediacrossing.campaignbooks;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Campaign implements Serializable {
    private String campaignID;
    private String campaignName;
    private String status;
    private float lifetimeBudget;
    private float dailyBudget;
    private float totalDelivery;
    private long daysActive;
    private ArrayList<Delivery> deliveries = new ArrayList<>();
    private long daysRemaining;
    private int lifetimeImps;
    private int lifetimeClicks;
    private int lifetimeConvs;
    private float lifetimeCtr;
    private DateTime startDate;
    private DateTime endDate;
    private int lifetimeImpBudget;
    private int dailyImpBudget;


    public Campaign(String campaignID, String campaignName, String status, float lifetimeBudget,
                    String startDate, String endDate, float dailyBudget, int lifetimeImpBudget,
                    int dailyImpBudget) throws ParseException {
        this.campaignID = campaignID;
        this.campaignName = campaignName;
        this.lifetimeBudget = lifetimeBudget;
        this.dailyBudget = dailyBudget;
        this.status = status;
        this.dailyImpBudget = dailyImpBudget;
        this.lifetimeImpBudget = lifetimeImpBudget;

        //Converting parsed date strings to Date objects
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'z'");
        if (!startDate.equals("null")) {
            this.startDate = new DateTime(formatter.parseDateTime(startDate));
        }
        else {
            this.startDate = null;
        }
        if (!endDate.equals("null")) {
            this.endDate = new DateTime(formatter.parseDateTime(endDate));
        }
        else {
            this.endDate = null;
        }

        Duration startToEndDuration = new Duration(this.startDate, this.endDate);
        DateTime now = new DateTime();
        Duration nowToEndPeriod = new Duration(now, this.endDate);

        this.daysActive = startToEndDuration.getStandardDays();
        this.daysRemaining = nowToEndPeriod.getStandardDays() + 1;
    }

    public int getLifetimeImpBudget() {
        return lifetimeImpBudget;
    }

    public int getDailyImpBudget() {
        return dailyImpBudget;
    }

    public int getLifetimeConvs() {
        return lifetimeConvs;
    }

    public void setLifetimeConvs(int lifetimeConvs) {
        this.lifetimeConvs = lifetimeConvs;
    }

    public String getStatus() {
        return status;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void addToDeliveries(Delivery delivery) {
        deliveries.add(delivery);
    }


    public void setTotalDelivery(float totalDelivery) {
        this.totalDelivery = totalDelivery;
    }

    public String getCampaignID() {
        return campaignID;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public float getLifetimeBudget() {
        return lifetimeBudget;
    }

    public float getDailyBudget() {
        return dailyBudget;
    }

    public float getActualDailyBudget() {
        if (daysRemaining != 0) {
            if (lifetimeBudget > 0) {
                return (lifetimeBudget - totalDelivery) / daysRemaining;
            } else return (lifetimeImpBudget - lifetimeImps)/daysRemaining;
        } else return 0;
    }

    public float getActualImpBudget() {
        return (lifetimeImpBudget- lifetimeImps) / daysRemaining;
    }

    public float getTotalDelivery() {
        return totalDelivery;
    }

    public long getDaysActive() {
        return daysActive;
    }

    public List<Delivery> getDeliveries() {
        return deliveries;
    }

    public int getLifetimeImps() {
        return lifetimeImps;
    }

    public void setLifetimeImps(int lifetimeImps) {
        this.lifetimeImps = lifetimeImps;
    }

    public int getLifetimeClicks() {
        return lifetimeClicks;
    }

    public void setLifetimeClicks(int lifetimeClicks) {
        this.lifetimeClicks = lifetimeClicks;
    }

    public float getLifetimeCtr() {
        return lifetimeCtr;
    }

    public void setLifetimeCtr(float lifetimeCtr) {
        this.lifetimeCtr = lifetimeCtr;
    }
}
