package com.mediacrossing.segmenttargeting;

import java.util.ArrayList;

public class SegmentGroupTarget {

    private String boolOp;
    private ArrayList<Segment> segmentArrayList;

    public SegmentGroupTarget (String boolOp, ArrayList<Segment> segmentArrayList) {
        this.boolOp = boolOp;
        this.segmentArrayList = segmentArrayList;
    }

    public String getBoolOp() {
        return boolOp;
    }

    public void setBoolOp(String boolOp) {
        this.boolOp = boolOp;
    }

    public ArrayList<Segment> getSegmentArrayList() {
        return segmentArrayList;
    }

    public void setSegmentArrayList(ArrayList<Segment> segmentArrayList) {
        this.segmentArrayList = segmentArrayList;
    }
}
