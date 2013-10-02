package com.mediacrossing.campaignbooks;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Delivery {
    private DateTime date;
    private String campaignID;
    private float delivery;
    private int imps;
    private int clicks;

    public Delivery(String date, String campaignID, String delivery, String imps, String clicks) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        this.date = new DateTime(sdf.parse(date));
        this.campaignID = campaignID;
        this.delivery = Float.parseFloat(delivery);
        this.imps = Integer.parseInt(imps);
        this.clicks = Integer.parseInt(clicks);
    }

    public DateTime getDate() {
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
