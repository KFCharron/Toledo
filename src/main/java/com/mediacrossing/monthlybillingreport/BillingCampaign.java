package com.mediacrossing.monthlybillingreport;

import java.io.Serializable;

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
    private float briligFee;
    private float evidonFee;
    private float integralFee;
    private float briligTotal;
    private float evidonTotal;
    private float integralTotal;
    private int briligImps = 0;
    private float blueKaiFee;
    private float blueKaiTotal;
    private float alcFee;
    private float alcTotal;
    private float grapeshotFee;
    private float grapeshotTotal;
    private float spongecellFee;
    private float spongecellTotal;
    private float vidibleFee;
    private float vidibleTotal;
    private float peer39Fee;
    private float peer39Total;
    private float cpm;

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
        this.briligFee = 0;
        this.evidonFee = 0;
        this.integralFee = 0;
        this.alcFee = 0;
        this.blueKaiFee = 0;
        this.grapeshotFee = 0;
        this.spongecellFee = 0;
        this.vidibleFee = 0;
        this.peer39Fee = 0;
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
        this.briligFee = 0;
        this.evidonFee = 0;
        this.integralFee = 0;
        this.alcFee = 0;
        this.blueKaiFee = 0;
        this.grapeshotFee = 0;
        this.spongecellFee = 0;
        this.vidibleFee = 0;
        this.peer39Fee = 0;
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
        this.briligTotal = 0;
        this.evidonTotal = 0;
        this.integralTotal = 0;
        this.briligImps = 0;
        this.briligTotal = 0;
        this.alcTotal = 0;
        this.grapeshotTotal = 0;
        this.spongecellTotal = 0;
        this.vidibleTotal = 0;
        this.peer39Total = 0;
    }

    public float getPeer39Fee() {
        return peer39Fee;
    }

    public void setPeer39Fee(float peer39Fee) {
        this.peer39Fee = peer39Fee;
    }

    public float getPeer39Total() {
        return this.peer39Total;
    }

    public void setPeer39Total(float peer39Total) {
        this.peer39Total = peer39Total;
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

    public float getBlueKaiFee() {
        return blueKaiFee;
    }

    public void setBlueKaiFee(float blueKaiFee) {
        this.blueKaiFee = blueKaiFee;
    }

    public float getBlueKaiTotal() {
        return blueKaiTotal;
    }

    public void setBlueKaiTotal(float blueKaiTotal) {
        this.blueKaiTotal = blueKaiTotal;
    }

    public float getAlcFee() {
        return alcFee;
    }

    public void setAlcFee(float alcFee) {
        this.alcFee = alcFee;
    }

    public float getAlcTotal() {
        return alcTotal;
    }

    public void setAlcTotal(float alcTotal) {
        this.alcTotal = alcTotal;
    }

    public float getGrapeshotFee() {
        return grapeshotFee;
    }

    public void setGrapeshotFee(float grapeshotFee) {
        this.grapeshotFee = grapeshotFee;
    }

    public float getGrapeshotTotal() {
        return grapeshotTotal;
    }

    public void setGrapeshotTotal(float grapeshotTotal) {
        this.grapeshotTotal = grapeshotTotal;
    }

    public float getCpm() {
        return cpm;
    }

    public float getSpongecellFee() {
        return spongecellFee;
    }

    public void setSpongecellFee(float spongecellFee) {
        this.spongecellFee = spongecellFee;
    }

    public float getSpongecellTotal() {
        return spongecellTotal;
    }

    public void setSpongecellTotal(float spongecellTotal) {
        this.spongecellTotal = spongecellTotal;
    }

    public float getVidibleFee() {
        return vidibleFee;
    }

    public void setVidibleFee(float vidibleFee) {
        this.vidibleFee = vidibleFee;
    }

    public float getVidibleTotal() {
        return vidibleTotal;
    }

    public void setVidibleTotal(float vidibleTotal) {
        this.vidibleTotal = vidibleTotal;
    }
}
