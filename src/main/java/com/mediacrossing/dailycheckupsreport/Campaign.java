package com.mediacrossing.dailycheckupsreport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Campaign implements Serializable {
    private String id;
    private String name;
    private String profileID;
    private String advertiserID;
    private String lineItemID;
    private Profile profile = new Profile();
    private ArrayList<ServingFee> servingFeeList;
    private String endDate;
    private String startDate;
    private int dailyImps;
    private String advertiserName;
    private String lineItemName;
    private float maxBid;
    private float baseBid;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public float getBaseBid() {
        return baseBid;
    }

    public void setBaseBid(float baseBid) {
        this.baseBid = baseBid;
    }

    public String getAdvertiserName() {
        return advertiserName;
    }

    public void setAdvertiserName(String advertiserName) {
        this.advertiserName = advertiserName;
    }

    public String getLineItemName() {
        return lineItemName;
    }

    public void setLineItemName(String lineItemName) {
        this.lineItemName = lineItemName;
    }

    public int getDailyImps() {
        return dailyImps;
    }

    public void setDailyImps(int dailyImps) {
        this.dailyImps = dailyImps;
    }

    public Profile getProfile() {
        return profile;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

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

    public FrequencyTarget getFrequencyTargets() {
        return profile.getFrequencyTarget();
    }

    public GeographyTarget getGeographyTargets() {
        return profile.getGeographyTarget();
    }

    public List<DaypartTarget> getDaypartTargetArrayList() {
        return profile.getDaypartTargetList();
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

    public float getMaxBid() {
        return maxBid;
    }

    public void setMaxBid(float maxBid) {
        this.maxBid = maxBid;
    }
}
