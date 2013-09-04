package com.mediacrossing.segmenttargeting;

import java.util.ArrayList;
import java.util.List;

public class Campaign {
    private String id;
    private String name;
    private String profileID;
    private String advertiserID;
    private String lineItemID;
    private String state;
    private Profile profile;
    private ArrayList<ServingFee> servingFeeList;

    public ArrayList<ServingFee> getServingFeeList() {
        return servingFeeList;
    }

    public void setServingFeeList(ArrayList<ServingFee> servingFeeList) {
        this.servingFeeList = servingFeeList;
    }

    public void setProfile(Profile p) {
        profile = p;
    }

    public List<SegmentGroupTarget> getSegmentGroupTargetList() {
        return profile.getSegmentGroupTargets();
    }

    public void setSegmentGroupTargetList(ArrayList<SegmentGroupTarget> segmentGroupTargetList) {
        profile.setSegmentGroupTargets(segmentGroupTargetList);
    }

    public FrequencyTarget getFrequencyTargets() {
        return profile.getFrequencyTarget();
    }

    public void setFrequencyTargets(FrequencyTarget frequencyTarget) {
        profile.setFrequencyTarget(frequencyTarget);
    }

    public GeographyTarget getGeographyTargets() {
        return profile.getGeographyTarget();
    }

    public void setGeographyTargets(GeographyTarget geographyTarget) {
        profile.setGeographyTarget(geographyTarget);
    }

    public List<DaypartTarget> getDaypartTargetArrayList() {
        return profile.getDaypartTargetList();
    }

    public void setDaypartTargetArrayList(ArrayList<DaypartTarget> daypartTargetArrayList) {
        profile.setDaypartTargetList(daypartTargetArrayList);
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
