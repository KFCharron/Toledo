package com.mediacrossing.publishercheckup;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.util.ArrayList;

public class YMProfile implements Serializable {

    private String id;
    private String name;
    private String description;
    private DateTime lastModified;
    private ArrayList<FloorRule> floorRules;

    public YMProfile(String id, String name, String description, ArrayList<FloorRule> floorRules, String lastModified) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.floorRules = floorRules;
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        this.lastModified = new DateTime(dtf.parseDateTime(lastModified), DateTimeZone.UTC);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<FloorRule> getFloorRules() {
        return floorRules;
    }

    public boolean modifiedYesterday() {
        if (lastModified.isAfter(new DateTime().toDateMidnight().minusDays(1))) return true;
        else return false;
    }
}
