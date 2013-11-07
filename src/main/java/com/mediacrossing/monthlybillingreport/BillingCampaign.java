package com.mediacrossing.monthlybillingreport;

import java.io.Serializable;

public class BillingCampaign implements Serializable {

    private String id;
    private String name;
    private int imps;
    private int clicks;
    private float convs;
    private float mediaCost;
    private float networkRevenue;
    private int adExImps;
    private int appNexusImps;
    private float briligFee;
    private float evidonFee;
    private float integralFee;
    private float briligTotal;
    private float evidonTotal;
    private float integralTotal;

    public BillingCampaign(String id, String name, int imps, int clicks, float convs, float mediaCost, float networkRevenue) {
        this.id = id;
        this.name = name;
        this.imps = imps;
        this.clicks = clicks;
        this.convs = convs;
        this.mediaCost = mediaCost;
        this.networkRevenue = networkRevenue;
        this.briligFee = 0;
        this.evidonFee = 0;
        this.integralFee = 0;
    }

    public BillingCampaign() {
        this.imps = 0;
        this.clicks = 0;
        this.convs = 0;
        this.mediaCost = 0;
        this.networkRevenue = 0;
        this.adExImps = 0;
        this.appNexusImps = 0;
        this.briligTotal = 0;
        this.evidonTotal = 0;
        this.integralTotal = 0;
    }

    public float getBriligTotal() {
        return briligTotal;
    }

    public void setBriligTotal(float briligTotal) {
        this.briligTotal = briligTotal;
    }

    public float getEvidonTotal() {
        return evidonTotal;
    }

    public void setEvidonTotal(float evidonTotal) {
        this.evidonTotal = evidonTotal;
    }

    public float getIntegralTotal() {
        return integralTotal;
    }

    public void setIntegralTotal(float integralTotal) {
        this.integralTotal = integralTotal;
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
        this.appNexusImps = imps-this.adExImps;
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

    public int getAppNexusImps() {
        return appNexusImps;
    }

    public float getBriligFee() {
        return briligFee;
    }

    public void setBriligFee(float briligFee) {
        this.briligFee = briligFee;
    }

    public float getEvidonFee() {
        return evidonFee;
    }

    public void setEvidonFee(float evidonFee) {
        this.evidonFee = evidonFee;
    }

    public float getIntegralFee() {
        return integralFee;
    }

    public void setIntegralFee(float integralFee) {
        this.integralFee = integralFee;
    }
}
