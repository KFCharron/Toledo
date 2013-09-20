package com.mediacrossing.segmenttargeting;

public class Segment {
    private String id;
    private String name;
    private String action;
    private String boolOp;
    private String totalSegmentLoads;
    private String dailySegmentLoads;

    public String getTotalSegmentLoads() {
        return totalSegmentLoads;
    }

    public void setTotalSegmentLoads(String totalSegmentLoads) {
        this.totalSegmentLoads = totalSegmentLoads;
    }

    public String getDailySegmentLoads() {
        return dailySegmentLoads;
    }

    public void setDailySegmentLoads(String dailySegmentLoads) {
        this.dailySegmentLoads = dailySegmentLoads;
    }

    public Segment (String id, String name, String action, String boolOp) {
        this.id = id;
        this.name = name;
        this.action = action;
        this.boolOp = boolOp;
    }

    public String getBoolOp() {
        return boolOp;
    }

    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public String getAction() {
        return action;
    }

}
