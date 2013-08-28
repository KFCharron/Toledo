package com.mediacrossing.segmenttargeting;

public class FrequencyTarget {
    private String maxLifetimeImps;
    private String minSessionImps;
    private String maxSessionImps;
    private String maxDayImps;
    private String minMinutesPerImp;
    private String maxPageImps;

    public FrequencyTarget(String maxLifetimeImps, String minSessionImps, String maxSessionImps,
                           String maxDayImps, String minMinutesPerImp, String maxPageImps) {
        this.maxLifetimeImps = maxLifetimeImps;
        this.minSessionImps = minSessionImps;
        this.maxSessionImps = maxSessionImps;
        this.maxDayImps = maxDayImps;
        this.minMinutesPerImp = minMinutesPerImp;
        this.maxPageImps = maxPageImps;
    }

    public String getMaxPageImps() {
        return maxPageImps;
    }

    public String getMaxLifetimeImps() {
        return maxLifetimeImps;
    }

    public String getMinSessionImps() {
        return minSessionImps;
    }

    public String getMaxSessionImps() {
        return maxSessionImps;
    }


    public String getMaxDayImps() {
        return maxDayImps;
    }


    public String getMinMinutesPerImp() {
        return minMinutesPerImp;
    }

}
