package com.mediacrossing.campaignbooks;

import java.io.Serializable;

public class ReportData implements Serializable {

    private int imps;
    private int clicks;
    private int totalConversions;
    private float mediaCost;
    private float ctr;
    private float conversionRate;
    private float cpm;
    private float cpc;
    private String id;

    public ReportData(int imps, int clicks, int totalConversions, float mediaCost,
                      float ctr, float conversionRate, float cpm, float cpc, String id) {
        this.imps = imps;
        this.clicks = clicks;
        this.totalConversions = totalConversions;
        this.mediaCost = mediaCost;
        this.ctr = ctr;
        this.conversionRate = conversionRate;
        this.cpm = cpm;
        this.cpc = cpc;
        this.id = id;
    }

    public int getImps() {
        return imps;
    }

    public int getClicks() {
        return clicks;
    }

    public int getTotalConversions() {
        return totalConversions;
    }

    public float getMediaCost() {
        return mediaCost;
    }

    public float getCtr() {
        return ctr;
    }

    public float getConversionRate() {
        return conversionRate;
    }

    public float getCpm() {
        return cpm;
    }

    public float getCpc() {
        return cpc;
    }

    public String getId() {
        return id;
    }
}
