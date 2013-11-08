package com.mediacrossing.weeklypublisherreport;

import org.joda.time.DateTime;

import java.io.Serializable;

public class DailyPublisherData implements Serializable {

    private DateTime date;
    private int avails;
    private int imps;
    private int errors;
    private float eCpm;
    private int unfilled;
    private float publisherRevenue;
    private float networkRevenue;

    public DailyPublisherData() {
    }

    public DailyPublisherData(DateTime date) {
        this.date = date;
        this.avails = 0;
        this.imps = 0;
        this.errors = 0;
        this.eCpm = 0;
        this.unfilled = 0;
        this.publisherRevenue = 0;
        this.networkRevenue = 0;
    }

    public float getNetworkRevenue() {
        return networkRevenue;
    }

    public void setNetworkRevenue(float networkRevenue) {
        this.networkRevenue = networkRevenue;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public int getAvails() {
        return avails;
    }

    public void setAvails(int avails) {
        this.avails = avails;
    }

    public int getImps() {
        return imps;
    }

    public void setImps(int imps) {
        this.imps = imps;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public float geteCpm() {
        return eCpm;
    }

    public void seteCpm(float eCpm) {
        this.eCpm = eCpm;
    }

    public int getUnfilled() {
        return unfilled;
    }

    public void setUnfilled(int unfilled) {
        this.unfilled = unfilled;
    }

    public float getPublisherRevenue() {
        return publisherRevenue;
    }

    public void setPublisherRevenue(float publisherRevenue) {
        this.publisherRevenue = publisherRevenue;
    }
}
