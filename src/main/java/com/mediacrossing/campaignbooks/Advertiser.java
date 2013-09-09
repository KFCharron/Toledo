package com.mediacrossing.campaignbooks;

import java.util.List;

public class Advertiser {

    private String advertiserID;
    private List<LineItem> lineItemList;

    public Advertiser(String advertiserID, List<LineItem> lineItemList) {
        this.advertiserID = advertiserID;
        this.lineItemList = lineItemList;
    }

    public Advertiser(String advertiserID) {
        this.advertiserID = advertiserID;
    }

    public String getAdvertiserID() {
        return advertiserID;
    }

    public List<LineItem> getLineItemList() {
        return lineItemList;
    }
}
