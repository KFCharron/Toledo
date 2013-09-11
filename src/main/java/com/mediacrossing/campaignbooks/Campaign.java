package com.mediacrossing.campaignbooks;

import java.util.LinkedList;
import java.util.List;

public class Campaign {
    private String campaignID;
    private String campaignName;
    private String startDate;
    private String endDate;
    private int lifetimeBudget;
    private int daysRemaining;
    private int dailyBudget;
    private int actualDailyBudget;
    private int totalDelivery;
    private int daysActive;
    private List<Float> dailyDeliveryList = new LinkedList<Float>();


    public Campaign(String campaignID, String campaignName, int lifetimeBudget,
                    String startDate, String endDate, int dailyBudget, List<Float> dailyDeliveryList) {
        this.campaignID = campaignID;
        this.campaignName = campaignName;
        this.lifetimeBudget = lifetimeBudget;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dailyBudget = dailyBudget;
        this.dailyDeliveryList = dailyDeliveryList;

        //TODO calculate daysRemaining, actualDailyBudget, totalDelivery, covert startDate and endDate to Dates
    }

    public Campaign(String campaignID) {
        this.campaignID = campaignID;
    }

    public String getCampaignID() {
        return campaignID;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public String getStartDate() {
        //TODO convert to human readable
        return startDate;
    }

    public String getEndDate() {
        //TODO convert to human readable
        return endDate;
    }

    public int getLifetimeBudget() {
        return lifetimeBudget;
    }

    public int getDaysRemaining() {
        return daysRemaining;
    }

    public int getDailyBudget() {
        return dailyBudget;
    }

    public int getActualDailyBudget() {
        return actualDailyBudget;
    }

    public int getTotalDelivery() {
        return totalDelivery;
    }


    public int getDaysActive() {
        return daysActive;
    }

    public void addToDailyDeliveryList(float dailyDelivery) {
        this.dailyDeliveryList.add(dailyDelivery);
    }

    public List<Float> getDailyDeliveryList() {
        return dailyDeliveryList;
    }
}
