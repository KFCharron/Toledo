package com.mediacrossing.weeklypublisherreport;

import com.mediacrossing.publishercheckup.PaymentRule;

import java.util.ArrayList;

public class WeeklyPublisher {

    private String id;
    private String name;
    private ArrayList<PaymentRule> paymentRules;
    private ArrayList<WeeklyPlacement> placements;

    public WeeklyPublisher(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setPaymentRules(ArrayList<PaymentRule> paymentRules) {
        this.paymentRules = paymentRules;
    }

    public void setPlacements(ArrayList<WeeklyPlacement> placements) {
        this.placements = placements;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<PaymentRule> getPaymentRules() {
        return paymentRules;
    }

    public ArrayList<WeeklyPlacement> getPlacements() {
        return placements;
    }
}
