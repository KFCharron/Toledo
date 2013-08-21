package com.mediacrossing.segment_targeting;

/**
 * Created with IntelliJ IDEA.
 * User: charronkyle
 * Date: 8/21/13
 * Time: 12:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class Profile {
    private int id;
    private int maxLifetimeImps;
    private int minSessionImps;
    private int maxSessionImps;
    private int maxDayImps;
    private int minMinutesPerImp;
    private int maxPageImps;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMaxLifetimeImps() {
        return maxLifetimeImps;
    }

    public void setMaxLifetimeImps(int maxLifetimeImps) {
        this.maxLifetimeImps = maxLifetimeImps;
    }

    public int getMinSessionImps() {
        return minSessionImps;
    }

    public void setMinSessionImps(int minSessionImps) {
        this.minSessionImps = minSessionImps;
    }

    public int getMaxSessionImps() {
        return maxSessionImps;
    }

    public void setMaxSessionImps(int maxSessionImps) {
        this.maxSessionImps = maxSessionImps;
    }

    public int getMaxDayImps() {
        return maxDayImps;
    }

    public void setMaxDayImps(int maxDayImps) {
        this.maxDayImps = maxDayImps;
    }

    public int getMinMinutesPerImp() {
        return minMinutesPerImp;
    }

    public void setMinMinutesPerImp(int minMinutesPerImp) {
        this.minMinutesPerImp = minMinutesPerImp;
    }

    public int getMaxPageImps() {
        return maxPageImps;
    }

    public void setMaxPageImps(int maxPageImps) {
        this.maxPageImps = maxPageImps;
    }


}
