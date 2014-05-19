package com.mediacrossing.dailypacingreport;

import org.joda.time.DateTime;

public class ImpressionDateBudget {

    private DateTime date;
    private int impressions;

    public ImpressionDateBudget(DateTime date, int impressions) {
        this.date = date;
        this.impressions = impressions;
    }

    public DateTime getDate() {
        return date;
    }

    public int getImpressions() {
        return impressions;
    }
}
