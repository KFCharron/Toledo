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
import java.util.List;

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


        ArrayList<PacingAdvertiser> finalAdvertisers = new ArrayList<>();
        for (Advertiser a : liveAdvertisers) {
            PacingAdvertiser pacingAdvertiser = new PacingAdvertiser(a.getAdvertiserName(), a.getAdvertiserID());
            for (PacingLineItem l : activeLines) {
                if (l.getAdvertiserId().equals(pacingAdvertiser.getId())) {
                    pacingAdvertiser.getLineList().add(l);
                    if (l.getStartDate().isBefore(pacingAdvertiser.getStart())) pacingAdvertiser.setStart(l.getStartDate());
                    if (l.getEndDate().isAfter(pacingAdvertiser.getEnd())) pacingAdvertiser.setEnd(l.getEndDate());

                }
            }
            pacingAdvertiser.setDuration();
            int goalToDate = 0;
            int impsToDate = 0;
            // For every day the advertiser is live
//            for (int x = 0; x < pacingAdvertiser.getDuration(); x++) {
//                // Create the Date
//                DateTime currentDate = pacingAdvertiser.getStart().plusDays(x);
//                // Create the daily data object
//                DailyPacingData now = new DailyPacingData(currentDate);
//
//                // For every Line Item in the Advertiser
//                for (PacingLineItem l : pacingAdvertiser.getLineList()) {
//                    // If the lineItem is live during this time
//                    if (l.getStartDate().minusDays(1).isBefore(currentDate) &&
//                            l.getEndDate().plusDays(1).isAfter(currentDate)) {
//                        // Add to budget for this day
//                        now.setGoalToday(now.getGoalToday() + 0);// TODO day of budget
//                        goalToDate = goalToDate + 0; // TODO day of budget
//                        now.setGoalToDate(goalToDate);
//                    }
//                    for (F.Tuple<DateTime, Integer> d : l.getDailyData()) {
//                        if (d._1.getMillis() == currentDate.getMillis()) {
//                            // TODO Watch this to make sure matches are being made correctly
//                            now.setImpsDelivered(
//                                    now.getImpsDelivered()
//                                    + d._2.intValue());
//                            impsToDate = impsToDate + d._2.intValue();
//                            now.setImpsToDate(impsToDate);
//                        }
//                    }
//                }
//                pacingAdvertiser.getDailyPacingNumbers().add(now);
//            }
            finalAdvertisers.add(pacingAdvertiser);
        }
        Workbook wb = PacingReportWriter.writeReport(finalAdvertisers);
        LocalDate now = new LocalDate(DateTimeZone.UTC);
        FileOutputStream fileOut =
                new FileOutputStream(new File(outputPath, "Daily_Pacing_Report_"
                        + now.toString() + ".xls"));
        wb.write(fileOut);
        fileOut.close();
    }
}
