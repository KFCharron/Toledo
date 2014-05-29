package com.mediacrossing.finalpacingreport;

import com.mediacrossing.campaignbooks.Advertiser;
import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.dailypacingreport.PacingLineItem;
import com.mediacrossing.properties.ConfigurationProperties;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.F;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RunFinalPacingReport {

    private static final Logger LOG = LoggerFactory.getLogger(RunFinalPacingReport.class);

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
        DateTime today = new DateTime().withTimeAtStartOfDay();
        // Get all advertisers From MX
        List<Advertiser> bareAdvertisers = mxConn.requestAllAdvertisers();
        List<Advertiser> liveAdvertisers = new ArrayList<>();
        for (Advertiser a : bareAdvertisers) if (a.isLive()) liveAdvertisers.add(a);

        ArrayList<PacingLineItem> allLines = mxConn.requestAllLineItems();
        ArrayList<PacingLineItem> activeLines = new ArrayList<>();

        for (PacingLineItem l : allLines)
            if (l.getEndDate().isAfter(today) && l.getStartDate().isBefore(today))
                activeLines.add(l);

        // Grab Earliest Start
        DateTime earliestDate = new DateTime().withTimeAtStartOfDay();
        for (PacingLineItem l : activeLines) {
            if (l.getStartDate().isBefore(earliestDate)) earliestDate = l.getStartDate();
        }

        // Query AN for Report Using Grabbed Start Date
        System.out.println("STARTING DATE FOR REPORT REQUEST:  " + earliestDate.getMonthOfYear() + "/" + earliestDate.getDayOfMonth());
        List<String[]> reportData = anConn.requestPacingReport(earliestDate);
        reportData.remove(0);
        for (String[] line : reportData) {
            for (PacingLineItem l : activeLines) {
                if (l.getName().equals(line[1])) {
                    DateTime dataDate = dtf.parseDateTime(line[2]).withTimeAtStartOfDay();
                    l.getDailyData().add(new F.Tuple<>(dataDate, Integer.getInteger(line[3], Integer.parseInt(line[3]))));
                }
            }
        }

        Set<String> flightNames = new HashSet<>();

        ArrayList<PacingAdvertiser> finalAdvertisers = new ArrayList<>();
        for (Advertiser a : liveAdvertisers) {
            PacingAdvertiser pacingAdvertiser = new PacingAdvertiser(a.getAdvertiserName(), a.getAdvertiserID());
            for (PacingLineItem l : activeLines) {
                if (l.getAdvertiserId().equals(pacingAdvertiser.getId())) {
                    String lName = l.getName();
                    String[] parsed = lName.split("]");
                    String flightName = parsed[1].substring(4, parsed[1].length());
                    flightNames.add(a.getAdvertiserName() + " - " + flightName);
                    pacingAdvertiser.getLineList().add(l);
                    if (l.getStartDate().isBefore(pacingAdvertiser.getStart())) pacingAdvertiser.setStart(l.getStartDate());
                    if (l.getEndDate().isAfter(pacingAdvertiser.getEnd())) pacingAdvertiser.setEnd(l.getEndDate());
                }
            }
            pacingAdvertiser.setDuration();
            finalAdvertisers.add(pacingAdvertiser);
        }
        Workbook wb = PacingReportWriter.writeReport(finalAdvertisers, flightNames);
        LocalDate now = new LocalDate(DateTimeZone.UTC);
        FileOutputStream fileOut =
                new FileOutputStream(new File(outputPath, "Daily_Pacing_Report_"
                        + now.toString() + ".xls"));
        wb.write(fileOut);
        fileOut.close();
    }
}
