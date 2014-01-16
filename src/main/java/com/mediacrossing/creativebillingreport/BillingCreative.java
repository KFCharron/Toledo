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
    private float briligTotal;
    private float evidonTotal;
    private float integralTotal;
    private float blueKaiTotal;
    private float alcTotal;
    private float grapeshotTotal;
    private float spongecellTotal;
    private float vidibleTotal;
    private float peer39Total;
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
        this.briligTotal = c.getBriligTotal();
        this.evidonTotal = c.getEvidonTotal();
        this.integralTotal = c.getIntegralTotal();
        this.blueKaiTotal = c.getBlueKaiTotal();
        this.alcTotal = c.getAlcTotal();
        this.grapeshotTotal = c.getGrapeshotTotal();
        this.spongecellTotal = c.getSpongecellTotal();
        this.vidibleTotal = c.getVidibleTotal();
        this.peer39Total = c.getPeer39Total();
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
        this.briligTotal = 0;
        this.evidonTotal = 0;
        this.integralTotal = 0;
        this.blueKaiTotal = 0;
        this.alcTotal = 0;
        this.grapeshotTotal = 0;
        this.spongecellTotal = 0;
        this.vidibleTotal = 0;
        this.peer39Total = 0;
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

    public float getPeer39Total() {
        return peer39Total;
    }

    public void setPeer39Total(float peer39Total) {
        this.peer39Total = peer39Total;
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

    public int getBriligImps() {
        return briligImps;
    }

    public void setBriligImps(int briligImps) {
        this.briligImps = briligImps;
    }

    public float getBlueKaiTotal() {
        return blueKaiTotal;
    }

    public void setBlueKaiTotal(float blueKaiTotal) {
        this.blueKaiTotal = blueKaiTotal;
    }

    public float getAlcTotal() {
        return alcTotal;
    }

    public void setAlcTotal(float alcTotal) {
        this.alcTotal = alcTotal;
    }

    public float getGrapeshotTotal() {
        return grapeshotTotal;
    }

    public void setGrapeshotTotal(float grapeshotTotal) {
        this.grapeshotTotal = grapeshotTotal;
    }

    public float getSpongecellTotal() {
        return spongecellTotal;
    }

    public void setSpongecellTotal(float spongecellTotal) {
        this.spongecellTotal = spongecellTotal;
    }

    public float getVidibleTotal() {
        return vidibleTotal;
    }

    public void setVidibleTotal(float vidibleTotal) {
        this.vidibleTotal = vidibleTotal;
    }
}
