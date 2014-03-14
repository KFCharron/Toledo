package com.mediacrossing.monthlybillingreport;

import com.mediacrossing.dailycheckupsreport.ServingFee;

import java.io.Serializable;
import java.util.ArrayList;

public class BillingCampaign implements Serializable {

    private String id;
    private String name;
    private String creativeId;
    private String creativeName;
    private int imps;
    private int clicks;
    private float convs;
    private float mediaCost;
    private float networkRevenue;
    private int adExImps;
    private int mxImps;
    private int appNexusImps;
    private int briligImps = 0;
    private float cpm;
    private ArrayList<ServingFee> servingFees = new ArrayList<>();
    private float mxMediaCost;
    private float adXMediaCost;
    private float appNexusMediaCost;
    private int lotameImps = 0;
    private int blueKaiImps = 0;
    private float maxBid;

    public BillingCampaign(String id, String name, int imps, int clicks, float convs, float mediaCost,
                           float networkRevenue, float cpm, String creativeId, String creativeName) {
        this.id = id;
        this.name = name;
        this.creativeId = creativeId;
        this.creativeName = creativeName;
        this.imps = imps;
        this.clicks = clicks;
        this.convs = convs;
        this.mediaCost = mediaCost;
        this.networkRevenue = networkRevenue;
        this.cpm = cpm;
    }

    public BillingCampaign(String id, String name, int imps, int clicks, float convs, float mediaCost,
                           float networkRevenue, float cpm) {
        this.id = id;
        this.name = name;
        this.imps = imps;
        this.clicks = clicks;
        this.convs = convs;
        this.mediaCost = mediaCost;
        this.networkRevenue = networkRevenue;
        this.cpm = cpm;
    }

    public BillingCampaign() {
        this.imps = 0;
        this.clicks = 0;
        this.convs = 0;
        this.mediaCost = 0;
        this.networkRevenue = 0;
        this.adExImps = 0;
        this.mxImps = 0;
        this.appNexusImps = 0;
        this.briligImps = 0;
        this.mxMediaCost = 0;
        this.adXMediaCost = 0;
        this.appNexusMediaCost = 0;
        this.lotameImps = 0;
    }

    public float getMaxBid() {
        return maxBid;
    }

    public void setMaxBid(float maxBid) {
        this.maxBid = maxBid;
    }

    public float getAnCommission() {
        return .115f * this.appNexusMediaCost;
    }

    public float getAdXCommission() {
        return .1f * this.adXMediaCost;
    }

    public float getMxCommission() {
        return (float)this.mxImps / 1000f * .017f;
    }

    public float getAnCpm() {
        if (this.getAppNexusImps() != 0) {
            return (this.appNexusMediaCost + this.getAnCommission()) / (float)this.getAppNexusImps() * 1000f;
        } else {
            return 0;
        }
    }

    public float getAdXCpm() {
        if (this.adExImps != 0) {
            return (this.adXMediaCost + this.getAdXCommission()) / (float)this.adExImps * 1000f;
        } else return 0;
    }

    public float getMxCpm() {
        if (this.mxImps != 0) {
            return (this.mxMediaCost + this.getMxCommission()) / (float)this.mxImps * 1000f;
        } else return 0;
    }

    public int getLotameImps() {
        return lotameImps;
    }

    public void setLotameImps(int lotameImps) {
        this.lotameImps = lotameImps;
    }

    public ArrayList<ServingFee> getServingFees() {
        return servingFees;
    }

    public String getCreativeId() {
        return creativeId;
    }

    public String getCreativeName() {
        return creativeName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBriligImps() {
        return briligImps;
    }

    public void setBriligImps(int briligImps) {
        this.briligImps = briligImps;
    }

    public void setImps(int imps) {
        this.imps = imps;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public void setConvs(float convs) {
        this.convs = convs;
    }

    public void setMediaCost(float mediaCost) {
        this.mediaCost = mediaCost;
    }

    public void setNetworkRevenue(float networkRevenue) {
        this.networkRevenue = networkRevenue;
    }

    public void setAppNexusImps(int appNexusImps) {
        this.appNexusImps = appNexusImps;
    }

    public void setAdExImps(int adExImps) {
        this.adExImps = adExImps;
    }

    public void setMxImps(int mxImps) {
        this.mxImps = mxImps;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getImps() {
        return imps;
    }

    public int getClicks() {
        return clicks;
    }

    public float getConvs() {
        return convs;
    }

    public float getMediaCost() {
        return mediaCost;
    }

    public float getNetworkRevenue() {
        return networkRevenue;
    }

    public int getAdExImps() {
        return adExImps;
    }

    public int getMxImps() { return mxImps; }

    public int getAppNexusImps() {
        return imps - mxImps - adExImps;
    }

    public float getMxMediaCost() {
        return mxMediaCost;
    }

    public void setMxMediaCost(float mxMediaCost) {
        this.mxMediaCost = mxMediaCost;
    }

    public float getAdXMediaCost() {
        return adXMediaCost;
    }

    public void setAdXMediaCost(float adXMediaCost) {
        this.adXMediaCost = adXMediaCost;
    }

    public float getAppNexusMediaCost() {
        return appNexusMediaCost;
    }

    public void setAppNexusMediaCost(float appNexusMediaCost) {
        this.appNexusMediaCost = appNexusMediaCost;
    }

    public int getBlueKaiImps() {
        return blueKaiImps;
    }

    public void setBlueKaiImps(int blueKaiImps) {
        this.blueKaiImps = blueKaiImps;
    }

    public float getTotalCost() {
        float serveCost = 0;
        for (ServingFee f : this.getServingFees()) {
            serveCost = serveCost + f.getTotalFee();
        }
        return (this.mediaCost + this.getAnCommission() + this.getMxCommission() + this.getAdXCommission() + serveCost);
    }
}
