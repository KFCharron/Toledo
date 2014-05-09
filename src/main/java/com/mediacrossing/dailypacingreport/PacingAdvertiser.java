package com.mediacrossing.dailypacingreport;

import com.mediacrossing.campaignbooks.Advertiser;

import java.util.ArrayList;

public class PacingAdvertiser {

    private String name;
    private String id;
    private ArrayList<PacingLineItem> lineItems = new ArrayList<>();

    public PacingAdvertiser(Advertiser a) {
        name = a.getAdvertiserName();
        id = a.getAdvertiserID();
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public ArrayList<PacingLineItem> getLineItems() {
        return lineItems;
    }
}
