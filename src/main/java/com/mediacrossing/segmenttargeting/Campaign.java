package com.mediacrossing.segmenttargeting;

import java.util.ArrayList;

public class Campaign {
    private String id;
    private String name;
    private String profileID;
    private String advertiserID;
    private String lineItemID;
    private String state;
    private FrequencyTargets frequencyTargets = new FrequencyTargets();
    private GeographyTargets geographyTargets = new GeographyTargets();
    private ArrayList<DaypartTarget> daypartTargetArrayList = new ArrayList<DaypartTarget>();
    private ArrayList<SegmentGroupTarget> segmentGroupTargetList = new ArrayList<SegmentGroupTarget>();

    public ArrayList<SegmentGroupTarget> getSegmentGroupTargetList() {
        return segmentGroupTargetList;
    }

    public void setSegmentGroupTargetList(ArrayList<SegmentGroupTarget> segmentGroupTargetList) {
        this.segmentGroupTargetList = segmentGroupTargetList;
    }

    public FrequencyTargets getFrequencyTargets() {
        return frequencyTargets;
    }

    public void setFrequencyTargets(FrequencyTargets frequencyTargets) {
        this.frequencyTargets = frequencyTargets;
    }

    public GeographyTargets getGeographyTargets() {
        return geographyTargets;
    }

    public void setGeographyTargets(GeographyTargets geographyTargets) {
        this.geographyTargets = geographyTargets;
    }

    public ArrayList<DaypartTarget> getDaypartTargetArrayList() {
        return daypartTargetArrayList;
    }

    public void setDaypartTargetArrayList(ArrayList<DaypartTarget> daypartTargetArrayList) {
        this.daypartTargetArrayList = daypartTargetArrayList;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileID() {
        return profileID;
    }

    public void setProfileID(String profileID) {
        this.profileID = profileID;
    }

    public String getAdvertiserID() {
        return advertiserID;
    }

    public void setAdvertiserID(String advertiserID) {
        this.advertiserID = advertiserID;
    }

    public String getLineItemID() {
        return lineItemID;
    }

    public void setLineItemID(String lineItemID) {
        this.lineItemID = lineItemID;
    }
}
