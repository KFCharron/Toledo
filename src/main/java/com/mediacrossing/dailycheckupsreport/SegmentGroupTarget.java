package com.mediacrossing.dailycheckupsreport;

import java.io.Serializable;
import java.util.ArrayList;

public class SegmentGroupTarget implements Serializable{

    private String boolOp;
    private ArrayList<Segment> segmentArrayList = new ArrayList<Segment>();

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
