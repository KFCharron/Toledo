package com.mediacrossing.publisherreporting;

import java.io.Serializable;

public class Placement implements Serializable{

    private String id;
    private String name;
    private String siteId;
    private String siteName;
    private int impsTotal;
    private int impsSold;
    private int clicks;
    private int rtbImps;
    private int keptImps;
    private int defaultImps;
    private int psaImps;
    private float networkRevenue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public int getImpsTotal() {
        return impsTotal;
    }

    public void setImpsTotal(int impsTotal) {
        this.impsTotal = impsTotal;
    }

    public int getImpsSold() {
        return impsSold;
    }

    public void setImpsSold(int impsSold) {
        this.impsSold = impsSold;
    }

    public int getClicks() {
        return clicks;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public int getRtbImps() {
        return rtbImps;
    }

    public void setRtbImps(int rtbImps) {
        this.rtbImps = rtbImps;
    }

    public int getKeptImps() {
        return keptImps;
    }

    public void setKeptImps(int keptImps) {
        this.keptImps = keptImps;
    }

    public int getDefaultImps() {
        return defaultImps;
    }

    public void setDefaultImps(int defaultImps) {
        this.defaultImps = defaultImps;
    }

    public int getPsaImps() {
        return psaImps;
    }

    public void setPsaImps(int psaImps) {
        this.psaImps = psaImps;
    }

    public float getNetworkRevenue() {
        return networkRevenue;
    }

    public void setNetworkRevenue(float networkRevenue) {
        this.networkRevenue = networkRevenue;
    }
}
