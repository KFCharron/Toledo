package com.mediacrossing.publisher_reporting;

import java.text.DecimalFormat;

public class Publisher {
    private String id;
    private String publisherName;
    private float impsTotal;
    private int impsSold;
    private int clicks;
    private float impsRtb;
    private float impsKept;
    private float impsDefault;
    private float impsPsa;
    private String rtbPercentage;
    private String keptPercentage;
    private String defaultPercentage;
    private String psaPercentage;

    public Publisher(String id, String publisherName) {
        this.id = id;
        this.publisherName = publisherName;
    }

    public Publisher(String id, String publisherName, float impsTotal, int impsSold,
                     int clicks, float impsRtb, float impsKept, float impsDefault, float impsPsa) {
        this.id = id;
        this.publisherName = publisherName;
        this.impsTotal = impsTotal;
        this.impsSold = impsSold;
        this.clicks = clicks;
        this.impsRtb = impsRtb;
        this.impsKept = impsKept;
        this.impsDefault = impsDefault;
        this.impsPsa = impsPsa;
        DecimalFormat df = new DecimalFormat("#.00");
        this.rtbPercentage = df.format(impsRtb / impsTotal * 100);
        this.keptPercentage = df.format(impsKept / impsTotal * 100);
        this.defaultPercentage = df.format(impsDefault / impsTotal * 100);
        this.psaPercentage = df.format(impsPsa / impsTotal * 100);
    }

    public String getId() {
        return id;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public float getImpsTotal() {
        return impsTotal;
    }

    public int getImpsSold() {
        return impsSold;
    }

    public int getClicks() {
        return clicks;
    }

    public float getImpsRtb() {
        return impsRtb;
    }

    public float getImpsKept() {
        return impsKept;
    }

    public float getImpsDefault() {
        return impsDefault;
    }

    public float getImpsPsa() {
        return impsPsa;
    }

    public String getRtbPercentage() {
        return rtbPercentage;
    }

    public String getKeptPercentage() {
        return keptPercentage;
    }

    public String getDefaultPercentage() {
        return defaultPercentage;
    }

    public String getPsaPercentage() {
        return psaPercentage;
    }
}
