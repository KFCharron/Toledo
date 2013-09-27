package com.mediacrossing.campaignbooks;

public class Delivery {
    private String date;
    private String campaignID;
    private float delivery;
    private int imps;
    private int clicks;

    public Delivery(String date, String campaignID, String delivery, String imps, String clicks) {
        this.date = date;
        this.campaignID = campaignID;
        this.delivery = Float.parseFloat(delivery);
        this.imps = Integer.parseInt(imps);
        this.clicks = Integer.parseInt(clicks);
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

    public int getImps() {
        return imps;
    }

    public int getClicks() {
        return clicks;
    }
}
