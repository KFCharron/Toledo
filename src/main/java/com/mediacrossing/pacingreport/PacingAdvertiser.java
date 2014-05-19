package com.mediacrossing.pacingreport;

import org.joda.time.Duration;

import java.util.ArrayList;

public class PacingAdvertiser {

    private String name;
    private String id;
    private int lifetimeBudget = 0;
    private ArrayList<PacingLI> lineItems = new ArrayList<>();
    private Duration earliestToLatest;
    private int normalDailyBudget;
    private int lastDayBudget;

    public PacingAdvertiser(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public PacingAdvertiser(String name) {
        this.name = name;
        this.id = "0";
    }

    public int getLifetimeBudget() {
        return lifetimeBudget;
    }

    public void setLifetimeBudget(int lifetimeBudget) {
        this.lifetimeBudget = lifetimeBudget;
    }

    public ArrayList<PacingLI> getLineItems() {
        return lineItems;
    }

    public Duration getEarliestToLatest() {
        return earliestToLatest;
    }

    public void setEarliestToLatest(Duration earliestToLatest) {
        this.earliestToLatest = earliestToLatest;
    }

    public int getNormalDailyBudget() {
        return normalDailyBudget;
    }

    public void setNormalDailyBudget(int normalDailyBudget) {
        this.normalDailyBudget = normalDailyBudget;
    }

    public int getLastDayBudget() {
        return lastDayBudget;
    }

    public void setLastDayBudget(int lastDayBudget) {
        this.lastDayBudget = lastDayBudget;
    }
}
