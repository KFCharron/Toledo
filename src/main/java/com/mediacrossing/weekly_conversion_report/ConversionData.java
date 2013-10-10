package com.mediacrossing.weekly_conversion_report;

import java.io.Serializable;

public class ConversionData implements Serializable{

    private String lineItem;
    private String campaign;
    private String orderId;
    private String userId;
    private String postClickOrPostViewConv;
    private String creative;
    private String auctionId;
    private String externalData;
    private String impTime;
    private String datetime;

    public String getLineItem() {
        return lineItem;
    }

    public void setLineItem(String lineItem) {
        this.lineItem = lineItem;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostClickOrPostViewConv() {
        return postClickOrPostViewConv;
    }

    public void setPostClickOrPostViewConv(String postClickOrPostViewConv) {
        this.postClickOrPostViewConv = postClickOrPostViewConv;
    }

    public String getCreative() {
        return creative;
    }

    public void setCreative(String creative) {
        this.creative = creative;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getExternalData() {
        return externalData;
    }

    public void setExternalData(String externalData) {
        this.externalData = externalData;
    }

    public String getImpTime() {
        return impTime;
    }

    public void setImpTime(String impTime) {
        this.impTime = impTime;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
}
