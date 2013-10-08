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
    private int convs;
    private float ctr;

    public Delivery(String date, String campaignID, String delivery, String imps, String clicks, String convs, String ctr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        this.date = new DateTime(sdf.parse(date));
        this.campaignID = campaignID;
        this.delivery = Float.parseFloat(delivery);
        this.imps = Integer.parseInt(imps);
        this.clicks = Integer.parseInt(clicks);
        this.convs = Integer.parseInt(convs);
        this.ctr = Float.parseFloat(ctr);
    }

    public int getConvs() {
        return convs;
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

    public float getCtr() {
        return ctr;
    }
}
