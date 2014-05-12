package com.mediacrossing.dailypacingreport;

import com.mediacrossing.campaignbooks.*;
import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.properties.ConfigurationProperties;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class RunDailyPacingReport {

    private static final Logger LOG = LoggerFactory.getLogger(RunDailyPacingReport.class);

    public static void registerLoggerWithUncaughtExceptions() {
        Thread.setDefaultUncaughtExceptionHandler(
                new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
        );
    }

    public static void main(String[] args) throws Exception {

        registerLoggerWithUncaughtExceptions();

        //Declare variables
        ConfigurationProperties properties = new ConfigurationProperties(args);
        String appNexusUrl = properties.getAppNexusUrl();
        String outputPath = properties.getOutputPath();
        String appNexusUsername = properties.getAppNexusUsername();
        String appNexusPassword = properties.getAppNexusPassword();
        int anPartitionSize = properties.getPartitionSize();
        Duration requestDelayInSeconds = properties.getRequestDelayInSeconds();
        AppNexusService anConn = new AppNexusService(appNexusUrl, appNexusUsername,
                appNexusPassword, anPartitionSize, requestDelayInSeconds);
        String mxUsername = properties.getMxUsername();
        String mxPass = properties.getMxPassword();
        String mxUrl = properties.getMxUrl();
        MxService mxConn = new MxService(mxUrl, mxUsername, mxPass);

        DateTimeFormatter dtf = DateTimeFormat.forPattern("YYYY-MM-dd");

        // Get All Line-Items From MX
        ArrayList<PacingLineItem> lis = mxConn.requestAllLineItems();

        // Get All Advertisers
        List<Advertiser> adverts = mxConn.requestAllAdvertisers();
        ArrayList<PacingAdvertiser> ads = new ArrayList<>();
        for (Advertiser a : adverts) {
            if (a.isLive()) {
                if (a.getAdvertiserName().equals("Millenium Hotel")) {
                    PacingAdvertiser springBlooms = new PacingAdvertiser("Mill - Spring Blooms");
                    PacingAdvertiser advancePurchase = new PacingAdvertiser("Mill - Adv. Purch.");
                    PacingAdvertiser other = new PacingAdvertiser("Mill - Other");
                    ads.add(springBlooms);
                    ads.add(advancePurchase);
                    ads.add(other);
                    for (PacingLineItem l : lis) {
                        if (l.getName().contains("SpringBlooms")) springBlooms.getLineItems().add(l);
                        else if (l.getName().contains("Advance")) advancePurchase.getLineItems().add(l);
                        else other.getLineItems().add(l);
                    }
                } else if (a.getAdvertiserName().contains("FELD")) {
                    PacingAdvertiser data = new PacingAdvertiser(a.getAdvertiserName() + " - Data");
                    PacingAdvertiser retargeting = new PacingAdvertiser(a.getAdvertiserName() + " - Retargeting");
                    PacingAdvertiser supply = new PacingAdvertiser(a.getAdvertiserName() + " - Supply");
                    PacingAdvertiser remaining = new PacingAdvertiser(a.getAdvertiserName() + " - Others");
                    ads.add(data);
                    ads.add(retargeting);
                    ads.add(supply);
                    ads.add(remaining);
                    for (PacingLineItem l : lis) {
                        if (l.getName().contains("Data")) data.getLineItems().add(l);
                        else if (l.getName().contains("Retargeting")) retargeting.getLineItems().add(l);
                        else if (l.getName().contains("Supply")) supply.getLineItems().add(l);
                        else remaining.getLineItems().add(l);
                    }
                } else {
                    PacingAdvertiser pa = new PacingAdvertiser(a);
                    ads.add(pa);
                    for (PacingLineItem l : lis) {
                        if (l.getAdvertiserId().equals(a.getAdvertiserID())) {
                            pa.getLineItems().add(l);
                        }
                    }
                }

            }
        }

        // Grab Earliest Start
        DateTime earliestDate = new DateTime();
        for (PacingAdvertiser a : ads) {
            for (PacingLineItem l : a.getLineItems()) {
                if (l.getStartDate().isBefore(earliestDate)) earliestDate = l.getStartDate();
            }
        }

        // Query AN for Report Using Grabbed Start Date
        List<String[]> reportData = anConn.requestPacingReport(earliestDate);
        reportData.remove(0);
        for (String[] line : reportData) {
            for (PacingAdvertiser a : ads) {
                for (PacingLineItem l : a.getLineItems()) {
                    if (l.getName().equals(line[1])) {

                        int pacingBudget;
                        org.joda.time.Duration dur = new org.joda.time.Duration(l.getStartDate(), l.getEndDate());
                        long daysLive = dur.getStandardDays();
                        DateTime dataDate = dtf.parseDateTime(line[2]);
                        int normalBudget = (l.getLifetimeBudget() / (int)daysLive);
                        if (dataDate.dayOfYear().equals(l.getEndDate().dayOfYear())) {
                            pacingBudget = (int)(.75 * normalBudget);
                        } else {
                            pacingBudget = (int)(((.25 * normalBudget)/(daysLive-1)) + normalBudget);
                        }

                        l.getDailyData().add(new ImpressionDateBudget(
                                dataDate,
                                Integer.parseInt(line[3]),
                                pacingBudget
                            ));
                    }
                }

            }
        }

        // POI
        Workbook wb = PacingReportWriter.writeReport(ads);
        LocalDate now = new LocalDate(DateTimeZone.UTC);
        FileOutputStream fileOut =
                new FileOutputStream(new File(outputPath, "Daily_Pacing_Report_"
                        + now.toString() + ".xls"));
        wb.write(fileOut);
        fileOut.close();

    }

}
