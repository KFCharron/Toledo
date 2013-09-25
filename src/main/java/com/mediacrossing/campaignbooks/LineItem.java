package com.mediacrossing.campaignbooks;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LineItem {

    private String lineItemID;
    private String lineItemName;
    private Date startDate;
    private Date endDate;
    private float lifetimeBudget;
    private float dailyBudget;
    private long daysActive;
    private long daysRemaining;
    private List<Campaign> campaignList;
    private ReportData dayReportData;
    private ReportData lifetimeReportData;

    public ReportData getDayReportData() {
        return dayReportData;
    }

    public void setDayReportData(ReportData dayReportData) {
        this.dayReportData = dayReportData;
    }

    public ReportData getLifetimeReportData() {
        return lifetimeReportData;
    }

    public void setLifetimeReportData(ReportData lifetimeReportData) {
        this.lifetimeReportData = lifetimeReportData;
    }

    public LineItem(String lineItemID, String lineItemName, String startDate,
                    String endDate, String lifetimeBudget, String dailyBudget) throws ParseException {
        this.lineItemID = lineItemID;
        this.lineItemName = lineItemName;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        if(!startDate.equals("null") && !endDate.equals("null")) {
            this.startDate = sdf.parse(startDate);
            this.endDate = sdf.parse(endDate);
            this.daysActive = TimeUnit.DAYS.convert(this.endDate.getTime() - this.startDate.getTime(),
                    TimeUnit.MILLISECONDS);
            Date now = new Date();
            this.daysRemaining = TimeUnit.DAYS.convert(this.endDate.getTime() - now.getTime(), TimeUnit.MILLISECONDS);
        } else {
            this.startDate = null;
            this.endDate = null;
        }
        if(!lifetimeBudget.equals("null"))
        this.lifetimeBudget = Float.parseFloat(lifetimeBudget);
        if(!dailyBudget.equals("null"))
        this.dailyBudget = Float.parseFloat(dailyBudget);
    }

    public String getLineItemID() {
        return lineItemID;
    }

    public String getLineItemName() {
        return lineItemName;
    }

    public String getStartDate() {
        if(startDate != null) {
           return new SimpleDateFormat("dd-MMM").format(startDate);
        } else {
           return "";
        }
    }

    public String getEndDate() {
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

    public void setCampaignList(List<Campaign> campaignList) {
        this.campaignList = campaignList;
    }

    public long getFlightPercentage() {
        long duration = this.endDate.getTime() - this.startDate.getTime();
        long timeSinceStart = new Date().getTime() - this.startDate.getTime();
        return(timeSinceStart / duration * 100);
    }
}
