package com.mediacrossing.campaignbooks;

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
    private int[] dailyDelivery;

    public Campaign(String campaignID, String campaignName, int lifetimeBudget,
                    String startDate, String endDate, int dailyBudget, int[] dailyDelivery) {
        this.campaignID = campaignID;
        this.campaignName = campaignName;
        this.lifetimeBudget = lifetimeBudget;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dailyBudget = dailyBudget;
        this.dailyDelivery = dailyDelivery;

        //TODO calculate daysRemaining, actualDailyBudget, totalDelivery
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

    public int[] getDailyDelivery() {
        return dailyDelivery;
    }

    public int getDaysActive() {
        return daysActive;
    }
}
