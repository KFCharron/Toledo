package com.mediacrossing.weeklypublisherreport;

import java.io.Serializable;
import java.util.ArrayList;

public class WeeklyPlacement implements Serializable {

    private String id;
    private String name;
    private ArrayList<DailyPublisherData> dailyDataList;

    public WeeklyPlacement(String id, String name) {
        this.id = id;
        this.name = name;
        this.dailyDataList = new ArrayList<DailyPublisherData>();
    }

    public ArrayList<DailyPublisherData> getDailyDataList() {
        return dailyDataList;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
