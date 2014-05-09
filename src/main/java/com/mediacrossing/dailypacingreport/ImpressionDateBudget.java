package com.mediacrossing.dailypacingreport;

import org.joda.time.DateTime;

public class ImpressionDateBudget {

    private DateTime date;
    private int impressions;
    private int pacingBudget;

    public ImpressionDateBudget(DateTime date, int impressions, int pacingBudget) {
        this.date = date;
        this.impressions = impressions;
        this.pacingBudget = pacingBudget;
    }

    public DateTime getDate() {
        return date;
    }

    public int getImpressions() {
        return impressions;
    }

    public int getPacingBudget() {
        return pacingBudget;
    }
}
