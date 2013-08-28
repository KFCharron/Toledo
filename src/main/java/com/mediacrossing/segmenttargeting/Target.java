package com.mediacrossing.segmenttargeting;

import java.util.ArrayList;
//FIXME might not be using this class anymore.
public class Target {
    private String booleanOperator;
    ArrayList<Segment> segmentList = new ArrayList<Segment>();

    public Target (String booleanOperator, ArrayList<Segment> segmentList) {
        this.booleanOperator = booleanOperator;
        this.segmentList = segmentList;
    }

    public ArrayList<Segment> getSegmentList() {
        return segmentList;
    }

    public void setSegmentList(ArrayList<Segment> segmentList) {
        this.segmentList = segmentList;
    }

    public String getBooleanOperator() {
        return booleanOperator;
    }

    public void setBooleanOperator(String booleanOperator) {
        this.booleanOperator = booleanOperator;
    }
}
