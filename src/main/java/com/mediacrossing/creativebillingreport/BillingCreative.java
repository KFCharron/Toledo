package com.mediacrossing.creativebillingreport;

import com.mediacrossing.monthlybillingreport.BillingCampaign;

import java.io.Serializable;
import java.util.ArrayList;

public class BillingCreative implements Serializable {

    private String creativeId;
    private String creativeName;
    private String adId;
    private String adName;
    private int imps;
    private int clicks;
    private float convs;
    private float mediaCost;
    private float networkRevenue;
    private int adExImps;
    private int mxImps;
    private int appNexusImps;
    private int briligImps;
    private ArrayList<BillingCampaign> campaigns = new ArrayList<>();

    public BillingCreative(BillingCampaign c, String adId, String adName) {
        this.creativeId = c.getCreativeId();
        this.creativeName = c.getCreativeName();
        this.adId = adId;
        this.adName = adName;
        this.imps = c.getImps();
        this.clicks = c.getClicks();
        this.convs = c.getConvs();
        this.mediaCost = c.getMediaCost();
        this.networkRevenue = c.getNetworkRevenue();
        this.adExImps = c.getAdExImps();
        this.mxImps = c.getMxImps();
        this.appNexusImps = c.getAppNexusImps();
        this.briligImps = c.getBriligImps();
    }

    public BillingCreative() {
        this.imps = 0;
        this.clicks = 0;
        this.convs = 0;
        this.mediaCost = 0;
        this.networkRevenue = 0;
        this.adExImps = 0;
        this.mxImps = 0;
        this.appNexusImps = 0;
        this.briligImps = 0;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getAdName() {
        return adName;
    }

    public void setAdName(String adName) {
        this.adName = adName;
    }

    public ArrayList<BillingCampaign> getCampaigns() {
        return campaigns;
    }

    public String getCreativeId() {
        return creativeId;
    }

    public void setCreativeId(String creativeId) {
        this.creativeId = creativeId;
    }

    public String getCreativeName() {
        return creativeName;
    }

    public void setCreativeName(String creativeName) {
        this.creativeName = creativeName;
    }

    public int getImps() {
        return imps;
    }

    public void setImps(int imps) {
        this.imps = imps;
    }

    public int getClicks() {
        return clicks;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public float getConvs() {
        return convs;
    }

    public void setConvs(float convs) {
        this.convs = convs;
    }

    public float getMediaCost() {
        return mediaCost;
    }

    public void setMediaCost(float mediaCost) {
        this.mediaCost = mediaCost;
    }

    public float getNetworkRevenue() {
        return networkRevenue;
    }

    public void setNetworkRevenue(float networkRevenue) {
        this.networkRevenue = networkRevenue;
    }

    public int getAdExImps() {
        return adExImps;
    }

    public void setAdExImps(int adExImps) {
        this.adExImps = adExImps;
    }

    public int getMxImps() {
        return mxImps;
    }

    public void setMxImps(int mxImps) {
        this.mxImps = mxImps;
    }

    public int getAppNexusImps() {
        return appNexusImps;
    }

    public void setAppNexusImps(int appNexusImps) {
        this.appNexusImps = appNexusImps;
    }

    public int getBriligImps() {
        return briligImps;
    }

    public void setBriligImps(int briligImps) {
        this.briligImps = briligImps;
    }
}
