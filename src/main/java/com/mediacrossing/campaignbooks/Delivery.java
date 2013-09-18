package com.mediacrossing.campaignbooks;

public class Delivery {
    private String date;
    private String campaignID;
    private float delivery;

    public Delivery(String date, String campaignID, String delivery) {
        this.date = date;
        this.campaignID = campaignID;
        this.delivery = Float.parseFloat(delivery);
    }

    public String getDate() {
        return date;
    }

    public String getCampaignID() {
        return campaignID;
    }

    public float getDelivery() {
        return delivery;
    }
}
