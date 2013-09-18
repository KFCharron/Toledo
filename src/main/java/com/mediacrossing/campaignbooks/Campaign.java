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
    private int daysRemaining;
    private float dailyBudget;
    private float actualDailyBudget;
    private float totalDelivery;
    private int daysActive;
    private List<Float> dailyDeliveryList = new LinkedList<Float>();
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
        //Calculating daysRemaining
        if (!startDate.equals("null") && !endDate.equals("null"))
            this.daysRemaining = (int)((this.endDate.getTime() - this.startDate.getTime())
                    / (1000 * 60 * 60 * 24));


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
        if (startDate != null) {
            DateFormat df = new SimpleDateFormat("dd-MMM");
            return df.format(startDate);
        }
        else
            return "";

    }

    public String getEndDate() {
        if (endDate != null) {
            DateFormat df = new SimpleDateFormat("dd-MMM");
            return df.format(endDate);
        }
        else
            return "";

    }

    public float getLifetimeBudget() {
        return lifetimeBudget;
    }

    public int getDaysRemaining() {
        return daysRemaining;
    }

    public float getDailyBudget() {
        return dailyBudget;
    }

    public float getActualDailyBudget() {
        float deliveryTotal = getTotalDelivery();
        actualDailyBudget = deliveryTotal / getDailyDeliveryList().size();
        return actualDailyBudget;
    }

    public float getTotalDelivery() {
        for(Float delivery : this.getDailyDeliveryList()) {

        }
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
