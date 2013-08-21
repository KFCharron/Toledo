package com.mediacrossing.targetsegmenting;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: charronkyle
 * Date: 8/21/13
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class Target {
    private String booleanOperator;
    ArrayList<Segment> segmentList = new ArrayList<Segment>();

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
