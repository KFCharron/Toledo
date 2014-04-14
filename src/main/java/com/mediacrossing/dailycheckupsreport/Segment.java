package com.mediacrossing.dailycheckupsreport;

import java.io.Serializable;

public class Segment implements Serializable{
    private String id;
    private String name;
    private String action;
    private String boolOp;
    private String totalSegmentLoads;
    private String dailySegmentLoads;
    private String monthlySegmentLoads;
    private String code;

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

    public String getMonthlySegmentLoads() {
        return monthlySegmentLoads;
    }

    public void setMonthlySegmentLoads(String monthlySegmentLoads) {
        this.monthlySegmentLoads = monthlySegmentLoads;
    }

    public Segment (String id, String name, String action, String boolOp, String code) {
        this.id = id;
        this.name = name;
        this.action = action;
        this.boolOp = boolOp;
        this.code = code;
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

    public String getCode() {
        return code;
    }
}
