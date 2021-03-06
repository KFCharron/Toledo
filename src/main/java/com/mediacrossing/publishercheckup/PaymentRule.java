package com.mediacrossing.publishercheckup;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;

public class PaymentRule implements Serializable {

    private String id;
    private String name;
    private String state;
    private String pricingType;
    private double revshare;
    private String priority;
    private DateTime lastModified;

    public PaymentRule(String id, String name, String state, String pricingType,
                       double revshare, String priority, String lastModified) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.pricingType = pricingType;
        this.revshare = revshare;
        this.priority = priority;
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        this.lastModified = new DateTime(dtf.parseDateTime(lastModified), DateTimeZone.UTC);
    }



    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public String getPricingType() {
        return pricingType;
    }

    public double getRevshare() {
        return revshare;
    }

    public String getPriority() {
        return priority;
    }

    public boolean modifiedYesterday() {
        if (lastModified.isAfter(new DateTime().toDateMidnight().minusDays(1))) return true;
        else return false;
    }
}
