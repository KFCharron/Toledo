package com.mediacrossing.publishercheckup;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.util.ArrayList;

public class PublisherConfig implements Serializable {

    private String id;
    private String name;
    private DateTime lastModified;
    private ArrayList<Placement> placements;
    private ArrayList<PaymentRule> paymentRules;
    private ArrayList<YMProfile> ymProfiles;

    public PublisherConfig(String id, String name, String lastModified) {
        this.id = id;
        this.name = name;
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        this.lastModified = new DateTime(dtf.parseDateTime(lastModified), DateTimeZone.UTC);
    }

    public String getId() {
        return id;
    }

    public void setPlacements(ArrayList<Placement> placements) {
        this.placements = placements;
    }

    public void setPaymentRules(ArrayList<PaymentRule> paymentRules) {
        this.paymentRules = paymentRules;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Placement> getPlacements() {
        return placements;
    }

    public ArrayList<PaymentRule> getPaymentRules() {
        return paymentRules;
    }

    public ArrayList<YMProfile> getYmProfiles() {
        return ymProfiles;
    }

    public void setYmProfiles(ArrayList<YMProfile> ymProfiles) {
        this.ymProfiles = ymProfiles;
    }

    public boolean modifiedYesterday() {
        if (lastModified.isAfter(new DateTime().toDateMidnight().minusDays(1))) return true;
        else return false;
    }
}
