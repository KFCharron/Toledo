package com.mediacrossing.weeklypublisherreport;

import org.joda.time.DateTime;

public class DailyPublisherData {

    private DateTime date;
    private int avails;
    private int imps;
    private int errors;
    private float eCpm;
    private int unfilled;
    private float gross;

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public int getAvails() {
        return avails;
    }

    public void setAvails(int avails) {
        this.avails = avails;
    }

    public int getImps() {
        return imps;
    }

    public void setImps(int imps) {
        this.imps = imps;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public float geteCpm() {
        return eCpm;
    }

    public void seteCpm(float eCpm) {
        this.eCpm = eCpm;
    }

    public int getUnfilled() {
        return unfilled;
    }

    public void setUnfilled(int unfilled) {
        this.unfilled = unfilled;
    }

    public float getGross() {
        return gross;
    }

    public void setGross(float gross) {
        this.gross = gross;
    }
}
