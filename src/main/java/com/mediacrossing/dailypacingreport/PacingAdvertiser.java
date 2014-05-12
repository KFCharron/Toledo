package com.mediacrossing.dailypacingreport;

import com.mediacrossing.campaignbooks.Advertiser;

import java.util.ArrayList;

public class PacingAdvertiser {

    private String name;
    private String id;
    private ArrayList<PacingLineItem> lineItems = new ArrayList<>();

    public PacingAdvertiser(Advertiser a) {
        this.name = a.getAdvertiserName();
        this.id = a.getAdvertiserID();
    }

    public PacingAdvertiser(String name) {
        this.name = name;
        this.id = "";
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
