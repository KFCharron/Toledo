package com.mediacrossing.campaignbooks;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LineItem implements Serializable {

    private String lineItemID;
    private String lineItemName;
    private Date startDate;
    private Date endDate;
    private float lifetimeBudget;
    private float dailyBudget;
    private long daysActive;
    private long daysRemaining;
    private ArrayList<Campaign> campaignList;
    private DateTime startDateTime;
    private DateTime endDateTime;
    private String status;



    public LineItem(String lineItemID, String lineItemName, String startDate,
                    String endDate, String lifetimeBudget, String dailyBudget, String status) throws ParseException {
        this.lineItemID = lineItemID;
        this.lineItemName = lineItemName;
        this.status = status;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        if(!startDate.equals("null") && !endDate.equals("null")) {
            this.startDate = sdf.parse(startDate);
            this.startDateTime = new DateTime(startDate);
            this.endDate = sdf.parse(endDate);
            this.endDateTime = new DateTime(endDate);
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
        this.daysRemaining = nowToEnd.getStandardDays() + 1L;
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

    public Date getEndDate() {
        return endDate;
    }

    public String getLineItemID() {
        return lineItemID;
    }

    public String getLineItemName() {
        return lineItemName;
    }

    public String getStartDateString() {
        if(startDate != null) {
           return new SimpleDateFormat("dd-MMM").format(startDate);
        } else {
           return "";
        }
    }

    public String getEndDateString() {
        if(startDate != null) {
            return new SimpleDateFormat("dd-MMM").format(endDate);
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

    public long getFlightPercentage() {
        long duration = this.endDate.getTime() - this.startDate.getTime();
        long timeSinceStart = new Date().getTime() - this.startDate.getTime();
        return(timeSinceStart / duration * 100);
    }

    public DateTime getEndDateTime() {
        return endDateTime;
    }
}
