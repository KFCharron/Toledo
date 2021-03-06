package com.mediacrossing.campaignbooks;

import com.mediacrossing.advertiserdailyreport.DailyData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Advertiser implements Serializable {

    private String advertiserID;
    private String advertiserName;
    private ArrayList<LineItem> lineItemList;
    private boolean live = true;
    private ArrayList<DailyData> dailyLineItems;
    private ArrayList<DailyData> dailyCampaigns;
    private ArrayList<DailyData> lifetimeLineItems;
    private ArrayList<DailyData> lifetimeCampaigns;

    public Advertiser(String advertiserID, String advertiserName, ArrayList<LineItem> lineItemList) {
        this.advertiserID = advertiserID;
        this.advertiserName = advertiserName;
        this.lineItemList = lineItemList;
    }

    public Advertiser(String advertiserID, String advertiserName, String status) {
        this.advertiserID = advertiserID;
        this.advertiserName = advertiserName;
        if(status.equals("inactive")) {
            this.live = false;
        }
    }

    public String getAdvertiserID() {
        return advertiserID;
    }

    public String getAdvertiserName() {
        return advertiserName;
    }

    public List<LineItem> getLineItemList() {
        return lineItemList;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public ArrayList<DailyData> getDailyLineItems() {
        return dailyLineItems;
    }

    public void setDailyLineItems(ArrayList<DailyData> dailyLineItems) {
        this.dailyLineItems = dailyLineItems;
    }

    public ArrayList<DailyData> getDailyCampaigns() {
        return dailyCampaigns;
    }

    public void setDailyCampaigns(ArrayList<DailyData> dailyCampaigns) {
        this.dailyCampaigns = dailyCampaigns;
    }

    public ArrayList<DailyData> getLifetimeLineItems() {
        return lifetimeLineItems;
    }

    public void setLifetimeLineItems(ArrayList<DailyData> lifetimeLineItems) {
        this.lifetimeLineItems = lifetimeLineItems;
    }

    public ArrayList<DailyData> getLifetimeCampaigns() {
        return lifetimeCampaigns;
    }

    public void setLifetimeCampaigns(ArrayList<DailyData> lifetimeCampaigns) {
        this.lifetimeCampaigns = lifetimeCampaigns;
    }
}
