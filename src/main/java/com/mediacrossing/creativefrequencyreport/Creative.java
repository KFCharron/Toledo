package com.mediacrossing.creativefrequencyreport;

public class Creative {

    private int lifetimeImps = 0;
    private int yesterdayImps = 0;
    private int lifetimeClicks = 0;
    private int yesterdayClicks = 0;
    private String name;

    public int getLifetimeImps() {
        return lifetimeImps;
    }

    public void setLifetimeImps(int lifetimeImps) {
        this.lifetimeImps = lifetimeImps;
    }

    public int getYesterdayImps() {
        return yesterdayImps;
    }

    public void setYesterdayImps(int yesterdayImps) {
        this.yesterdayImps = yesterdayImps;
    }

    public int getLifetimeClicks() {
        return lifetimeClicks;
    }

    public void setLifetimeClicks(int lifetimeClicks) {
        this.lifetimeClicks = lifetimeClicks;
    }

    public int getYesterdayClicks() {
        return yesterdayClicks;
    }

    public void setYesterdayClicks(int yesterdayClicks) {
        this.yesterdayClicks = yesterdayClicks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Creative(String name, int lifetimeImps, int lifetimeClicks) {
        this.lifetimeImps = lifetimeImps;
        this.lifetimeClicks = lifetimeClicks;
        this.name = name;
    }
}
