package com.mediacrossing.monthlybillingreport;

import java.io.Serializable;
import java.util.ArrayList;

public class BillingAdvertiser implements Serializable {

    private String name;
    private String id;
    private ArrayList<BillingCampaign> campaigns = new ArrayList<BillingCampaign>();

    public BillingAdvertiser(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public ArrayList<BillingCampaign> getCampaigns() {
        return campaigns;
    }
}
