package com.mediacrossing.campaignbooks;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class LineItem implements Serializable {

    private String lineItemID;
    private String lineItemName;
    private float lifetimeBudget;
    private float dailyBudget;
    private long daysActive;
    private long daysRemaining;
    private ArrayList<Campaign> campaignList;
    private DateTime startDateTime;
    private DateTime endDateTime;
    private String status;
    private float lifetimeImpBudget;
    private float dailyImpBudget;



    public LineItem(String lineItemID, String lineItemName, String startDate,
                    String endDate, String lifetimeBudget, String dailyBudget, String status, String ltImpBudget,
                    String dImpBudget) throws ParseException {
        this.lineItemID = lineItemID;
        this.lineItemName = lineItemName;
        this.status = status;
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

        if(!startDate.equals("null") && !endDate.equals("null")) {
            this.startDateTime = new DateTime(formatter.parseDateTime(startDate), DateTimeZone.UTC);
            this.endDateTime = new DateTime(formatter.parseDateTime(endDate), DateTimeZone.UTC);
        }

        Duration startToEnd = new Duration(startDateTime, endDateTime);
        DateTime now = new DateTime();
        Duration nowToEnd = new Duration(now, endDateTime);

        this.daysActive = startToEnd.getStandardDays();

        if(!lifetimeBudget.equals("null")) {
            this.lifetimeBudget = Float.parseFloat(lifetimeBudget);
        }
        if(!dailyBudget.equals("null")) {
            this.dailyBudget = Float.parseFloat(dailyBudget);
        }
        if (!ltImpBudget.equals("null")) {
            this.lifetimeImpBudget = Float.parseFloat(ltImpBudget);
        }
        if (!dImpBudget.equals("null")) {
            this.dailyImpBudget = Float.parseFloat(dImpBudget);
        }
        this.daysRemaining = nowToEnd.getStandardDays() + 1L;
    }

    public float getLifetimeImpBudget() {
        return lifetimeImpBudget;
    }

    public float getDailyImpBudget() {
        return dailyImpBudget;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DateTime getStartDateTime() {
        return startDateTime;
    }

    public String getLineItemID() {
        return lineItemID;
    }

    public String getLineItemName() {
        return lineItemName;
    }

    public String getStartDateString() {
        if(startDateTime != null) {
           return (startDateTime.monthOfYear().getAsString() + "/" + startDateTime.dayOfMonth().getAsString());
        } else {
           return "";
        }
    }

    public String getEndDateString() {
        if(endDateTime != null) {
            return (endDateTime.monthOfYear().getAsString() + "/" + endDateTime.dayOfMonth().getAsString());
        } else {
            return "";
        }
    }

    public Float getLifetimeBudget() {
        return lifetimeBudget;
    }

    public Float getDailyBudget() {
        return dailyBudget;
    }

    public List<Campaign> getCampaignList() {
        return campaignList;
    }

    public long getDaysActive() {
        return daysActive;
    }

    public long getDaysRemaining() {
        return daysRemaining;
    }

    public void setCampaignList(ArrayList<Campaign> campaignList) {
        this.campaignList = campaignList;
    }

    public DateTime getEndDateTime() {
        return endDateTime;
    }
}
