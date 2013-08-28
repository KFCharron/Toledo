package com.mediacrossing.segmenttargeting;

/**
 * Created with IntelliJ IDEA.
 * User: charronkyle
 * Date: 8/22/13
 * Time: 11:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class FrequencyTarget {
    private String maxLifetimeImps;
    private String minSessionImps;
    private String maxSessionImps;
    private String maxDayImps;
    private String minMinutesPerImp;
    private String maxPageImps;

    public String getMaxPageImps() {
        return maxPageImps;
    }

    public void setMaxPageImps(String maxPageImps) {
        this.maxPageImps = maxPageImps;
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
}
