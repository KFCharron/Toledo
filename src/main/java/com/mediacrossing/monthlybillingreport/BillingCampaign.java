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
}
