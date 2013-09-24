package com.mediacrossing.campaignbooks;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Campaign {
    private String campaignID;
    private String campaignName;
    private Date startDate;
    private Date endDate;
    private float lifetimeBudget;
    private float dailyBudget;
    private float actualDailyBudget;
    private float totalDelivery;
    private long daysActive;
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
        else {
            this.startDate = null;
        }
        if (!endDate.equals("null"))
            this.endDate = sdf.parse(endDate);
        else {
            this.endDate = null;
        }
        if(!startDate.equals("null") && !endDate.equals("null")) {
            this.daysActive = TimeUnit.DAYS.convert(this.endDate.getTime() - this.startDate.getTime(),
                    TimeUnit.MILLISECONDS);
        } else {
            this.daysActive = 0;
        }





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

    public String getStartDate() {
        if(startDate != null) {
            return new SimpleDateFormat("dd-MMM").format(startDate);
        } else {
            return "";
        }
    }

    public String getEndDate() {
        if(endDate != null) {
            return new SimpleDateFormat("dd-MMM").format(endDate);
        } else {
            return "";
        }
    }

    public float getLifetimeBudget() {
        return lifetimeBudget;
    }

    public float getDailyBudget() {
        return dailyBudget;
    }

    public float getActualDailyBudget() {
        actualDailyBudget = getTotalDelivery()/deliveries.size();
        return actualDailyBudget;
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
}
