package com.mediacrossing.weeklydomainreport;

import java.io.Serializable;

public class Domain implements Serializable {
    private String name;
    private double bookedRevenue;
    private int clicks;
    private String clickThroughPct;
    private double convsPerMm;
    private double convsRate;
    private double costEcpa;
    private double costEcpc;
    private double cpm;
    private double ctr;
    private int imps;
    private double mediaCost;
    private int postClickConvs;
    private double postClickConvsRate;
    private int postViewConvs;
    private double postViewConvsRate;
    private double profit;
    private double profitEcpm;

    public Domain(String name, double bookedRevenue, int clicks, String clickThroughPct, double convsPerMm,
                  double convsRate, double costEcpa, double costEcpc, double cpm, double ctr, int imps,
                  double mediaCost, int postClickConvs, double postClickConvsRate, int postViewConvs,
                  double postViewConvsRate, double profit, double profitEcpm) {
        this.name = name;
        this.bookedRevenue = bookedRevenue;
        this.clicks = clicks;
        this.clickThroughPct = clickThroughPct;
        this.convsPerMm = convsPerMm;
        this.convsRate = convsRate;
        this.costEcpa = costEcpa;
        this.costEcpc = costEcpc;
        this.cpm = cpm;
        this.ctr = ctr;
        this.imps = imps;
        this.mediaCost = mediaCost;
        this.postClickConvs = postClickConvs;
        this.postClickConvsRate = postClickConvsRate;
        this.postViewConvs = postViewConvs;
        this.postViewConvsRate = postViewConvsRate;
        this.profit = profit;
        this.profitEcpm = profitEcpm;
    }

    public String getName() {
        return name;
    }

    public double getBookedRevenue() {
        return bookedRevenue;
    }

    public int getClicks() {
        return clicks;
    }

    public String getClickThroughPct() {
        return clickThroughPct;
    }

    public double getConvsPerMm() {
        return convsPerMm;
    }

    public double getConvsRate() {
        return convsRate;
    }

    public double getCostEcpa() {
        return costEcpa;
    }

    public double getCostEcpc() {
        return costEcpc;
    }

    public double getCpm() {
        return cpm;
    }

    public double getCtr() {
        return ctr;
    }

    public int getImps() {
        return imps;
    }

    public double getMediaCost() {
        return mediaCost;
    }

    public int getPostClickConvs() {
        return postClickConvs;
    }

    public double getPostClickConvsRate() {
        return postClickConvsRate;
    }

    public int getPostViewConvs() {
        return postViewConvs;
    }

    public double getPostViewConvsRate() {
        return postViewConvsRate;
    }

    public double getProfit() {
        return profit;
    }

    public double getProfitEcpm() {
        return profitEcpm;
    }
}
