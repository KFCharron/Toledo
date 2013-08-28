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


    public ArrayList<Segment> getSegmentArrayList() {
        return segmentArrayList;
    }

}
