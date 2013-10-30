package com.mediacrossing.publishercheckup;

import java.io.Serializable;
import java.util.ArrayList;

public class FloorRule implements Serializable {

    private String id;
    private String name;
    private String description;
    private String code;
    private Float hardFloor;
    private Float softFloor;
    private String priority;
    private ArrayList<IdName> audienceList;
    private ArrayList<IdName> supplyList;
    private ArrayList<IdName> demandList;

    public FloorRule(String id, String name, String description, String code, Float hardFloor, Float softFloor,
                     String priority, ArrayList<IdName> audienceList, ArrayList<IdName> supplyList,
                     ArrayList<IdName> demandList) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.code = code;
        this.hardFloor = hardFloor;
        this.softFloor = softFloor;
        this.priority = priority;
        this.audienceList = audienceList;
        this.supplyList = supplyList;
        this.demandList = demandList;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public Float getHardFloor() {
        return hardFloor;
    }

    public Float getSoftFloor() {
        return softFloor;
    }

    public String getPriority() {
        return priority;
    }

    public ArrayList<IdName> getAudienceList() {
        return audienceList;
    }

    public ArrayList<IdName> getSupplyList() {
        return supplyList;
    }

    public ArrayList<IdName> getDemandList() {
        return demandList;
    }
}
