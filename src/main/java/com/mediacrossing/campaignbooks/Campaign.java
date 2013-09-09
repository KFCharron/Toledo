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
}
