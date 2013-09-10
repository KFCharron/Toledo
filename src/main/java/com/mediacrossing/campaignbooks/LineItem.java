package com.mediacrossing.campaignbooks;

import java.util.List;

public class LineItem {
    private String lineItemID;
    private String lineItemName;
    private String startDate;
    private String endDate;
    private String overallBudget;
    private String dailyBudget;
    private List<Campaign> campaignList;
    private int daysActive;
    private int daysRemaining;

    public LineItem(String lineItemID, String lineItemName, String startDate,
                    String endDate, String overallBudget, String dailyBudget, List<Campaign> campaignList) {
        this.lineItemID = lineItemID;
        this.lineItemName = lineItemName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.overallBudget = overallBudget;
        this.dailyBudget = dailyBudget;
        this.campaignList = campaignList;
    }

    public LineItem(String lineItemID) {
        this.lineItemID = lineItemID;
    }

    public String getLineItemName() {
        return lineItemName;
    }

    public String getLineItemID() {
        return lineItemID;
    }

    public String getStartDate() {
        //TODO convert to human readable
        return startDate;
    }

    public String getEndDate() {
        //TODO convert to human readable
        return endDate;
    }

    public String getOverallBudget() {
        return overallBudget;
    }

    public String getDailyBudget() {
        return dailyBudget;
    }

    public List<Campaign> getCampaignList() {
        return campaignList;
    }

    public int getDaysActive() {
        //TODO calculate days active
        return daysActive;
    }

    public int getDaysRemaining() {
        //TODO calculate days remaining
        return daysRemaining;
    }
}