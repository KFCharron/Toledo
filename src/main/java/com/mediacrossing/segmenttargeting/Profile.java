package com.mediacrossing.segmenttargeting;

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
    private FrequencyTargets frequencyTargets = new FrequencyTargets();
    private GeographyTargets geographyTargets = new GeographyTargets();
    private ArrayList<DaypartTarget> daypartTargetList = new ArrayList<DaypartTarget>();

    public FrequencyTargets getFrequencyTargets() {
        return frequencyTargets;
    }

    public void setFrequencyTargets(FrequencyTargets frequencyTargets) {
        this.frequencyTargets = frequencyTargets;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<DaypartTarget> getDaypartTargetList() {
        return daypartTargetList;
    }

    public void setDaypartTargetList(ArrayList<DaypartTarget> daypartTargetList) {
        this.daypartTargetList = daypartTargetList;
    }

    public GeographyTargets getGeographyTargets() {
        return geographyTargets;
    }

    public void setGeographyTargets(GeographyTargets geographyTargets) {
        this.geographyTargets = geographyTargets;
    }
}
