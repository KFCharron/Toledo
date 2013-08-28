package com.mediacrossing.segmenttargeting;

import java.util.ArrayList;
import java.util.List;

public class Profile {
    private String id;
    private FrequencyTarget frequencyTarget;
    private GeographyTarget geographyTarget;
    private ArrayList<DaypartTarget> daypartTargetList = new ArrayList<DaypartTarget>();
    private List<SegmentGroupTarget> segmentGroupTargets = new ArrayList<SegmentGroupTarget>();

    public Profile (String id, FrequencyTarget frequencyTarget, GeographyTarget geographyTarget,
                    ArrayList<DaypartTarget> daypartTargetList, List<SegmentGroupTarget> segmentGroupTargets) {
        this.id = id;
        this.frequencyTarget = frequencyTarget;
        this.geographyTarget = geographyTarget;
        this.daypartTargetList = daypartTargetList;
        this.segmentGroupTargets = segmentGroupTargets;
    }

    public List<SegmentGroupTarget> getSegmentGroupTargets() {
        return segmentGroupTargets;
    }

    public void setSegmentGroupTargets(ArrayList<SegmentGroupTarget> segmentGroupTargets) {
        this.segmentGroupTargets = segmentGroupTargets;
    }

    public FrequencyTarget getFrequencyTarget() {
        return frequencyTarget;
    }

    public void setFrequencyTarget(FrequencyTarget frequencyTarget) {
        this.frequencyTarget = frequencyTarget;
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

    public GeographyTarget getGeographyTarget() {
        return geographyTarget;
    }

    public void setGeographyTarget(GeographyTarget geographyTarget) {
        this.geographyTarget = geographyTarget;
    }
}
