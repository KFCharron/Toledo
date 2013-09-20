package com.mediacrossing.campaignbooks;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Campaign {
    private String campaignID;
    private String campaignName;
    private Date startDate;
    private Date endDate;
    private float lifetimeBudget;
    private float dailyBudget;
    private float actualDailyBudget;
    private float totalDelivery;
    private int daysActive;
    private List<Delivery> deliveries = new LinkedList<Delivery>();

    public void addToDeliveries(Delivery delivery) {
        deliveries.add(delivery);
    }


    public Campaign(String campaignID, String campaignName, float lifetimeBudget,
                    String startDate, String endDate, float dailyBudget) throws ParseException {
        this.campaignID = campaignID;
        this.campaignName = campaignName;
        this.lifetimeBudget = lifetimeBudget;
        this.dailyBudget = dailyBudget;
        //Converting parsed date strings to Date objects
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        if (!startDate.equals("null"))
            this.startDate = sdf.parse(startDate);
        if (!endDate.equals("null"))
            this.endDate = sdf.parse(endDate);
        //Set actual daily budget, total delivery, days active


    }

    public String getCampaignID() {
        return campaignID;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public float getLifetimeBudget() {
        return lifetimeBudget;
    }

    public float getDailyBudget() {
        return dailyBudget;
    }

    public float getActualDailyBudget() {
        return actualDailyBudget;
    }

    public float getTotalDelivery() {
        return totalDelivery;
    }

    public int getDaysActive() {
        return daysActive;
    }

    public List<Delivery> getDeliveries() {
        return deliveries;
    }
}
