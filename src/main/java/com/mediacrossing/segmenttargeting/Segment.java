package com.mediacrossing.segmenttargeting;

public class Segment {
    private String id;
    private String name;
    private String action;
    private String boolOp;

    public Segment (String id, String name, String action, String boolOp) {
        this.id = id;
        this.name = name;
        this.action = action;
        this.boolOp = boolOp;
    }

    public String getBoolOp() {
        return boolOp;
    }

    public void setBoolOp(String boolOp) {
        this.boolOp = boolOp;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
