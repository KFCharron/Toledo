package com.mediacrossing.discrepancyreport;

import java.util.List;

public class Creative {

    private String name;
    private String appNexusId;
    private String originalContent;
    private List<String> campaignIds;
    private String dfaPlacementId;
    private String lineItemId;
    private int appNexusImps;
    private int appNexusClicks;
    private int appNexusConvs;
    private int dfaImps;
    private int dfaClicks;
    private int dfaConvs;

    public Creative(String name, String appNexusId, String originalContent, List<String> campaignIds) {
        this.name = name;
        this.appNexusId = appNexusId;
        this.originalContent = originalContent;
        this.campaignIds = campaignIds;
        String[] magic = originalContent.split(";")[0].split("\\.");
        this.dfaPlacementId = magic[magic.length-1];
    }

    public Creative() {
        this.appNexusImps = 0;
        this.appNexusClicks = 0;
        this.appNexusConvs = 0;
        this.dfaImps = 0;
        this.dfaClicks = 0;
        this.dfaConvs = 0;
    }

    public String getName() {
        return name;
    }

    public String getAppNexusId() {
        return appNexusId;
    }

    public List<String> getCampaignIds() {
        return campaignIds;
    }

    public String getDfaPlacementId() {
        return dfaPlacementId;
    }

    public int getAppNexusImps() {
        return appNexusImps;
    }

    public void setAppNexusImps(int appNexusImps) {
        this.appNexusImps = appNexusImps;
    }

    public int getAppNexusClicks() {
        return appNexusClicks;
    }

    public void setAppNexusClicks(int appNexusClicks) {
        this.appNexusClicks = appNexusClicks;
    }

    public int getAppNexusConvs() {
        return appNexusConvs;
    }

    public void setAppNexusConvs(int appNexusConvs) {
        this.appNexusConvs = appNexusConvs;
    }

    public int getDfaImps() {
        return dfaImps;
    }

    public void setDfaImps(int dfaImps) {
        this.dfaImps = dfaImps;
    }

    public int getDfaClicks() {
        return dfaClicks;
    }

    public void setDfaClicks(int dfaClicks) {
        this.dfaClicks = dfaClicks;
    }

    public int getDfaConvs() {
        return dfaConvs;
    }

    public void setDfaConvs(int dfaConvs) {
        this.dfaConvs = dfaConvs;
    }

    public String getLineItemId() {
        return lineItemId;
    }

    public void setLineItemId(String lineItemId) {
        this.lineItemId = lineItemId;
    }
}
