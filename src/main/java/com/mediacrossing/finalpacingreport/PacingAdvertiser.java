package com.mediacrossing.finalpacingreport;

import com.mediacrossing.dailypacingreport.PacingLineItem;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PacingAdvertiser {

    private String name;
    private String id;
    private DateTime start = new DateTime();
    private DateTime end = new DateTime();
    private int duration;
    private ArrayList<DailyPacingData> dailyPacingNumbers = new ArrayList<>();
    private ArrayList<PacingLineItem> lineList = new ArrayList<>();
    private Set<String> flightNames = new HashSet<>();

    public PacingAdvertiser(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<PacingLineItem> getLineList() {
        return lineList;
    }

    public String getId() {
        return id;
    }

    public DateTime getStart() {
        return start;
    }

    public void setStart(DateTime start) {
        this.start = start;
    }

    public DateTime getEnd() {
        return end;
    }

    public void setEnd(DateTime end) {
        this.end = end;
    }

    public ArrayList<DailyPacingData> getDailyPacingNumbers() {
        return dailyPacingNumbers;
    }

    public void setDailyPacingNumbers(ArrayList<DailyPacingData> dailyPacingNumbers) {
        this.dailyPacingNumbers = dailyPacingNumbers;
    }

    public void setDuration() {
        this.duration = (int)new Duration(this.start, this.end).getStandardDays();
    }

    public int getDuration() {
        return duration;
    }

    public Set<String> getFlightNames() {
        return flightNames;
    }
}
