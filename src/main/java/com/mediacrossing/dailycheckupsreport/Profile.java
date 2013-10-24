package com.mediacrossing.dailycheckupsreport;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Profile implements Serializable{
    private String id;
    private DateTime lastModified;
    private FrequencyTarget frequencyTarget;
    private GeographyTarget geographyTarget;
    private ArrayList<DaypartTarget> daypartTargetList = new ArrayList<DaypartTarget>();
    private List<SegmentGroupTarget> segmentGroupTargets = new ArrayList<SegmentGroupTarget>();

    public boolean modifiedYesterday() {
        if (lastModified.isAfter(new DateTime().toDateMidnight().minusDays(1))) return true;
        else return false;
    }

    public void setLastModified(DateTime lastModified) {
        this.lastModified = lastModified;
    }

    public List<SegmentGroupTarget> getSegmentGroupTargets() {
        return segmentGroupTargets;
    }

    public void setSegmentGroupTargets(ArrayList<SegmentGroupTarget> segmentGroupTargets) {
        this.segmentGroupTargets = segmentGroupTargets;
    }

    public FrequencyTarget getFrequencyTarget() {
        return frequencyTarget;
    }

    public void setFrequencyTarget(FrequencyTarget frequencyTarget) {
        this.frequencyTarget = frequencyTarget;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<DaypartTarget> getDaypartTargetList() {
        return daypartTargetList;
    }

    public void setDaypartTargetList(ArrayList<DaypartTarget> daypartTargetList) {
        this.daypartTargetList = daypartTargetList;
    }

    public GeographyTarget getGeographyTarget() {
        return geographyTarget;
    }

    public void setGeographyTarget(GeographyTarget geographyTarget) {
        this.geographyTarget = geographyTarget;
    }
}
