package com.mediacrossing.publisherreporting;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;

public class TrendingData implements Serializable {
    private DateTime date;
    private int imps;
    private int defaults;
    private double rpm;
    private double revenue;

    public TrendingData(String date, String imps, String defaults, String rpm, String revenue) {
        DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
        this.date = new DateTime(f.parseDateTime(date));
        this.imps = Integer.parseInt(imps);
        this.defaults = Integer.parseInt(defaults);
        this.rpm = Double.parseDouble(rpm);
        this.revenue = Double.parseDouble(revenue);
    }

    public TrendingData() {
        this.imps = 0;
        this.defaults = 0;
        this.rpm = 0;
        this.revenue = 0;
    }

    public DateTime getDate() {
        return date;
    }

    public int getImps() {
        return imps;
    }

    public double getRpm() {
        return rpm;
    }

    public double getRevenue() {
        return revenue;
    }

    public double getDefaultPercentage() {
        if (imps != 0) return (double)defaults/(double)imps;
        else return 0;
    }

    public int getDefaults() {
        return defaults;
    }

    public void setImps(int imps) {
        this.imps = imps;
    }

    public void setDefaults(int defaults) {
        this.defaults = defaults;
    }

    public void setRpm(double rpm) {
        this.rpm = rpm;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
}
