package com.mediacrossing.publishercheckup;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.util.ArrayList;

public class Placement implements Serializable {

    private String id;
    private String name;
    private String state;
    private DateTime lastModified;
    private ArrayList<IdName> filteredAdvertisers;
    private ArrayList<IdName> contentCategories;

    public Placement(String id, String name, String state, ArrayList<IdName> filteredAdvertisers,
                     ArrayList<IdName> contentCategories, String lastModified)
    {
        this.id = id;
        this.name = name;
        this.state = state;
        this.filteredAdvertisers = filteredAdvertisers;
        this.contentCategories = contentCategories;
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

    public ArrayList<IdName> getFilteredAdvertisers() {
        return filteredAdvertisers;
    }

    public ArrayList<IdName> getContentCategories() {
        return contentCategories;
    }

    public boolean modifiedYesterday() {
        if (lastModified.isAfter(new DateTime().toDateMidnight().minusDays(1))) return true;
        else return false;
    }
}
