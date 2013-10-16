package com.mediacrossing.dailycheckupsreport;

import java.util.ArrayList;

public class SegmentRow {
    private String segmentId;
    private String name;
    private ArrayList<Campaign> campaigns;
    private String totalLoads;
    private String dailyUniques;

    public SegmentRow(String segmentId, String name, ArrayList<Campaign> campaigns, String totalLoads, String dailyUniques) {
        this.segmentId = segmentId;
        this.name = name;
        this.campaigns = campaigns;
        this.totalLoads = totalLoads;
        this.dailyUniques = dailyUniques;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Campaign> getCampaigns() {
        return campaigns;
    }

    public String getTotalLoads() {
        return totalLoads;
    }

    public String getDailyUniques() {
        return dailyUniques;
    }
}
