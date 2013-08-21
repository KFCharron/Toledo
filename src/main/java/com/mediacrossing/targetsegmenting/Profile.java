package com.mediacrossing.targetsegmenting;

import java.util.ArrayList;
/**
 * Created with IntelliJ IDEA.
 * User: charronkyle
 * Date: 8/21/13
 * Time: 12:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class Profile {
    private String id;
    private String maxLifetimeImps;
    private String minSessionImps;
    private String maxSessionImps;
    private String maxDayImps;
    private String minMinutesPerImp;
    private String maxPageImps;

    public String getSegementBool() {
        return segementBool;
    }

    public void setSegementBool(String segementBool) {
        this.segementBool = segementBool;
    }

    public ArrayList<Target> getSegmentGroupTargetList() {
        return segmentGroupTargetList;
    }

    public void setSegmentGroupTargetList(ArrayList<Target> segmentGroupTargetList) {
        this.segmentGroupTargetList = segmentGroupTargetList;
    }

    private String segementBool;
    private ArrayList<Target> segmentGroupTargetList = new ArrayList<Target>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaxLifetimeImps() {
        return maxLifetimeImps;
    }

    public void setMaxLifetimeImps(String maxLifetimeImps) {
        this.maxLifetimeImps = maxLifetimeImps;
    }

    public String getMinSessionImps() {
        return minSessionImps;
    }

    public void setMinSessionImps(String minSessionImps) {
        this.minSessionImps = minSessionImps;
    }

    public String getMaxSessionImps() {
        return maxSessionImps;
    }

    public void setMaxSessionImps(String maxSessionImps) {
        this.maxSessionImps = maxSessionImps;
    }

    public String getMaxDayImps() {
        return maxDayImps;
    }

    public void setMaxDayImps(String maxDayImps) {
        this.maxDayImps = maxDayImps;
    }

    public String getMinMinutesPerImp() {
        return minMinutesPerImp;
    }

    public void setMinMinutesPerImp(String minMinutesPerImp) {
        this.minMinutesPerImp = minMinutesPerImp;
    }

    public String getMaxPageImps() {
        return maxPageImps;
    }

    public void setMaxPageImps(String maxPageImps) {
        this.maxPageImps = maxPageImps;
    }
}
