package com.mediacrossing.pacingreport;

import java.util.ArrayList;

public class PacingLI {

    private String name;
    private String id;
    private ArrayList<DailyData> dailyDataList = new ArrayList<>();

    public PacingLI(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public ArrayList<DailyData> getDailyDataList() {
        return dailyDataList;
    }
}
