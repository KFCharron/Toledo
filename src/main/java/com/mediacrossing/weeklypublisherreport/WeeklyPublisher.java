package com.mediacrossing.weeklypublisherreport;

import com.mediacrossing.publishercheckup.PaymentRule;

import java.io.Serializable;
import java.util.ArrayList;

public class WeeklyPublisher implements Serializable {

    private String id;
    private String name;
    private String status;
    private ArrayList<PaymentRule> paymentRules;
    private ArrayList<WeeklyPlacement> placements;
    private ArrayList<String> topBuyers;
    private ArrayList<String> topBrands;

    public WeeklyPublisher(String id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
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


    public String getStatus() {
        return status;
    }

    public ArrayList<String> getTopBuyers() {
        return topBuyers;
    }

    public void setTopBuyers(ArrayList<String> topBuyers) {
        this.topBuyers = topBuyers;
    }

    public ArrayList<String> getTopBrands() {
        return topBrands;
    }

    public void setTopBrands(ArrayList<String> topBrands) {
        this.topBrands = topBrands;
    }
}
