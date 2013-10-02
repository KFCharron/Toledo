package com.mediacrossing.campaignbooks;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Campaign {
    private String campaignID;
    private String campaignName;
    private String status;
    private float lifetimeBudget;
    private float dailyBudget;
    private float totalDelivery;
    private long daysActive;
    private List<Delivery> deliveries = new LinkedList<Delivery>();
    private ReportData dayReportData;
    private ReportData lifetimeReportData;
    private long daysRemaining;
    private int lifetimeImps;
    private int lifetimeClicks;
    private float lifetimeCtr;
    private DateTime startDate;
    private DateTime endDate;


    public Campaign(String campaignID, String campaignName, String status, float lifetimeBudget,
                    String startDate, String endDate, float dailyBudget) throws ParseException {
        this.campaignID = campaignID;
        this.campaignName = campaignName;
        this.lifetimeBudget = lifetimeBudget;
        this.dailyBudget = dailyBudget;
        this.status = status;

        //Converting parsed date strings to Date objects
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        if (!startDate.equals("null")) {
            Date start = sdf.parse(startDate);
            this.startDate = new DateTime(start);
        }
        else {
            this.startDate = null;
        }
        if (!endDate.equals("null")) {
            Date end = sdf.parse(endDate);
            this.endDate = new DateTime(end);
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

    public String getStatus() {
        return status;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public ReportData getDayReportData() {
        return dayReportData;
    }

    public void setDayReportData(ReportData dayReportData) {
        this.dayReportData = dayReportData;
    }

    public ReportData getLifetimeReportData() {
        return lifetimeReportData;
    }

    public void setLifetimeReportData(ReportData lifetimeReportData) {
        this.lifetimeReportData = lifetimeReportData;
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
        return (lifetimeBudget - totalDelivery) / daysRemaining;
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

    public long getFlightPercentage() {
        //TODO
        return 1L;
    }

    public long getDaysRemaining() {
        return daysRemaining;
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
