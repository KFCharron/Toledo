package com.mediacrossing.advertiser_daily_report;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;

import java.io.Serializable;

public class DailyData implements Serializable{

    private String id;
    private String name;
    private String imps;
    private String clicks;
    private String totalConv;
    private String mediaCost;
    private String ctr;
    private String convRate;
    private String cpm;
    private String cpc;
    private DateTime startDay;
    private DateTime endDay;
    private float dailyBudget;
    private float lifetimeBudget;
    private String status = "";

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float getPercentThroughFlight() {
        if(startDay != null && endDay != null) {
            DateTime now = new DateTime();
            float full = Days.daysBetween(startDay.toDateMidnight(), endDay.toDateMidnight()).getDays();
            float startToNow = Days.daysBetween(startDay.toDateMidnight(), now.toDateMidnight()).getDays();

            if(startToNow/full > 1) return 1;
            else return startToNow/full;

        } else return 0;
    }

    public float getPercentThroughLifetimeBudget() {
        return Float.parseFloat(mediaCost)/lifetimeBudget;
    }

    public float getSuggestedDailyBudget() {
        if (endDay != null) {
            DateTime now = new DateTime();
            Duration nowToEnd = new Duration(now, endDay);

            return (lifetimeBudget-Float.parseFloat(mediaCost))/nowToEnd.getStandardDays();
        } else {
            return 0;
        }

    }

    public float getLifetimeBudget() {
        return lifetimeBudget;
    }

    public void setLifetimeBudget(float lifetimeBudget) {
        this.lifetimeBudget = lifetimeBudget;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImps(String imps) {
        this.imps = imps;
    }

    public void setClicks(String clicks) {
        this.clicks = clicks;
    }

    public void setTotalConv(String totalConv) {
        this.totalConv = totalConv;
    }

    public void setMediaCost(String mediaCost) {
        this.mediaCost = mediaCost;
    }

    public void setCtr(String ctr) {
        this.ctr = ctr;
    }

    public void setConvRate(String convRate) {
        this.convRate = convRate;
    }

    public void setCpm(String cpm) {
        this.cpm = cpm;
    }

    public void setCpc(String cpc) {
        this.cpc = cpc;
    }

    public void setStartDay(DateTime startDay) {
        this.startDay = startDay;
    }

    public void setDailyBudget(float dailyBudget) {
        this.dailyBudget = dailyBudget;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getImps() {
        return Integer.parseInt(imps);
    }

    public int getClicks() {
        return Integer.parseInt(clicks);
    }

    public Integer getTotalConv() {
        return Integer.parseInt(totalConv);
    }

    public Float getMediaCost() {
        return Float.parseFloat(mediaCost);
    }

    public Float getCtr() {
        return Float.parseFloat(ctr);
    }

    public Float getConvRate() {
        return Float.parseFloat(convRate);
    }

    public Float getCpm() {
        return Float.parseFloat(cpm);
    }

    public Float getCpc() {
        return Float.parseFloat(cpc);
    }

    public DateTime getStartDay() {
        return startDay;
    }

    public float getDailyBudget() {
        return dailyBudget;
    }

    public DateTime getEndDay() {
        return endDay;
    }

    public void setEndDay(DateTime endDay) {
        this.endDay = endDay;
    }
}
