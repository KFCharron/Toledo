package com.mediacrossing.anupload;

import org.joda.time.DateTime;

public class SegmentData {

    private String mxId;
    private DateTime firstTouch;
    private String firstTouchCampaignId;
    private String firstTouchCampaignName;
    private DateTime lastTouch;
    private String lastTouchCampaignId;
    private String lastTouchCampaignName;

    public String getMxId() {
        return mxId;
    }

    public void setMxId(String mxId) {
        this.mxId = mxId;
    }

    public DateTime getFirstTouch() {
        return firstTouch;
    }

    public void setFirstTouch(DateTime firstTouch) {
        this.firstTouch = firstTouch;
    }

    public String getFirstTouchCampaignId() {
        return firstTouchCampaignId;
    }

    public void setFirstTouchCampaignId(String firstTouchCampaignId) {
        this.firstTouchCampaignId = firstTouchCampaignId;
    }

    public String getFirstTouchCampaignName() {
        return firstTouchCampaignName;
    }

    public void setFirstTouchCampaignName(String firstTouchCampaignName) {
        this.firstTouchCampaignName = firstTouchCampaignName;
    }

    public DateTime getLastTouch() {
        return lastTouch;
    }

    public void setLastTouch(DateTime lastTouch) {
        this.lastTouch = lastTouch;
    }

    public String getLastTouchCampaignId() {
        return lastTouchCampaignId;
    }

    public void setLastTouchCampaignId(String lastTouchCampaignId) {
        this.lastTouchCampaignId = lastTouchCampaignId;
    }

    public String getLastTouchCampaignName() {
        return lastTouchCampaignName;
    }

    public void setLastTouchCampaignName(String lastTouchCampaignName) {
        this.lastTouchCampaignName = lastTouchCampaignName;
    }
}
