package com.mediacrossing.dailypacingreport;

import com.mediacrossing.campaignbooks.Advertiser;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;

public class PacingAdvertiser {

    private String name;
    private String id;
    private int lifetimeBudget = 0;
    private ArrayList<PacingLineItem> lineItems = new ArrayList<>();
    private DateTime earliest = new DateTime();
    private DateTime latest = new DateTime();

    public PacingAdvertiser(Advertiser a) {
        this.name = a.getAdvertiserName();
        this.id = a.getAdvertiserID();
    }

    public PacingAdvertiser(String name) {
        this.name = name;
        this.id = "";
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public ArrayList<PacingLineItem> getLineItems() {
        return lineItems;
    }

    public int getLifetimeBudget() {
        return lifetimeBudget;
    }

    public void setLifetimeBudget(int lifetimeBudget) {
        this.lifetimeBudget = lifetimeBudget;
    }

    public int getNormalDailyBudget() {
        return (int)((lifetimeBudget / getDaysActive()) +
                (((1/(getDaysActive() - 1)) *
                        (.25 * (lifetimeBudget/getDaysActive())))));
    }

    public int getLastDayBudget() {
        return (int)(.75 * (lifetimeBudget/getDaysActive()));
    }

    public int getDaysActive() {
        return (int)new Duration(this.earliest, this.latest).getStandardDays();
    }

    public DateTime getLatest() {
        return latest;
    }

    public DateTime getEarliest() {
        return earliest;
    }

    public void setEarliest(DateTime earliest) {
        this.earliest = earliest;
    }

    public void setLatest(DateTime latest) {
        this.latest = latest;
    }
}
