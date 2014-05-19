//package com.mediacrossing.pacingreport;
//
//import com.mediacrossing.campaignbooks.Advertiser;
//import com.mediacrossing.connections.AppNexusService;
//import com.mediacrossing.connections.MxService;
//import com.mediacrossing.dailypacingreport.PacingLineItem;
//import com.mediacrossing.properties.ConfigurationProperties;
//import org.joda.time.DateTime;
//import org.joda.time.format.DateTimeFormat;
//import org.joda.time.format.DateTimeFormatter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import scala.concurrent.duration.Duration;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class RunPacingReport {
//    private static final Logger LOG = LoggerFactory.getLogger(RunPacingReport.class);
//
//    public static void registerLoggerWithUncaughtExceptions() {
//        Thread.setDefaultUncaughtExceptionHandler(
//                new Thread.UncaughtExceptionHandler() {
//                    @Override
//                    public void uncaughtException(Thread t, Throwable e) {
//                        LOG.error(e.getMessage(), e);
//                    }
//                }
//        );
//    }
//
//    public static void main(String[] args) throws Exception {
//
//        registerLoggerWithUncaughtExceptions();
//
//        //Declare variables
//        ConfigurationProperties properties = new ConfigurationProperties(args);
//        String appNexusUrl = properties.getAppNexusUrl();
//        String outputPath = properties.getOutputPath();
//        String appNexusUsername = properties.getAppNexusUsername();
//        String appNexusPassword = properties.getAppNexusPassword();
//        int anPartitionSize = properties.getPartitionSize();
//        Duration requestDelayInSeconds = properties.getRequestDelayInSeconds();
//        AppNexusService anConn = new AppNexusService(appNexusUrl, appNexusUsername,
//                appNexusPassword, anPartitionSize, requestDelayInSeconds);
//        String mxUsername = properties.getMxUsername();
//        String mxPass = properties.getMxPassword();
//        String mxUrl = properties.getMxUrl();
//        MxService mxConn = new MxService(mxUrl, mxUsername, mxPass);
//
//        DateTimeFormatter dtf = DateTimeFormat.forPattern("YYYY-MM-dd");
//
//        List<Advertiser> basicAdvertisers = mxConn.requestAllAdvertisers();
//        ArrayList<PacingAdvertiser> advertisers = new ArrayList<>();
//
//        // Get All Line-Items From MX
//        ArrayList<PacingLineItem> allLines = mxConn.requestAllLineItems();
//        ArrayList<PacingLineItem> activeLines = new ArrayList<>();
//
//        for (PacingLineItem l : allLines) {
//            if (l.getEndDate().isAfter(new DateTime())) activeLines.add(l);
//        }
//
//        for (Advertiser a : basicAdvertisers) {
//            if (a.isLive() && !a.getAdvertiserID().equals("151391") && !a.getAdvertiserID().equals("186199")) {
//                // If advertiser is Millenium, split flights into different advertisers
//                if (a.getAdvertiserName().equals("Millenium Hotel")) {
//                    PacingAdvertiser springBlooms = new PacingAdvertiser("Mill - Spring Blooms");
//                    PacingAdvertiser advancePurchase = new PacingAdvertiser("Mill - Adv. Purch.");
//                    PacingAdvertiser other = new PacingAdvertiser("Mill - Other");
//                    advertisers.add(springBlooms);
//                    advertisers.add(advancePurchase);
//                    advertisers.add(other);
//                    // Save Line Items depending on their Flight name
//                    for (PacingLineItem l : activeLines) {
//                        if (l.getName().contains("SpringBlooms")) springBlooms.getLineItems().add(l);
//                        else if (l.getName().contains("Advance")) advancePurchase.getLineItems().add(l);
//                        else other.getLineItems().add(l);
//                    }
//                    // Separate FELD line items as well
//                } else if (a.getAdvertiserName().contains("FELD")) {
//                    PacingAdvertiser data = new PacingAdvertiser(a.getAdvertiserName() + " - Data");
//                    PacingAdvertiser retargeting = new PacingAdvertiser(a.getAdvertiserName() + " - Retargeting");
//                    PacingAdvertiser supply = new PacingAdvertiser(a.getAdvertiserName() + " - Supply");
//                    PacingAdvertiser remaining = new PacingAdvertiser(a.getAdvertiserName() + " - Others");
//                    advertisers.add(data);
//                    advertisers.add(retargeting);
//                    advertisers.add(supply);
//                    advertisers.add(remaining);
//                    for (PacingLineItem l : activeLines) {
//                        if (l.getName().contains("Data")) data.getLineItems().add(l);
//                        else if (l.getName().contains("Retargeting")) retargeting.getLineItems().add(l);
//                        else if (l.getName().contains("Supply")) supply.getLineItems().add(l);
//                        else remaining.getLineItems().add(l);
//                    }
//                    // Neither Millennium or FELD, save advertiser as normal
//                } else {
//                    com.mediacrossing.dailypacingreport.PacingAdvertiser pa = new com.mediacrossing.dailypacingreport.PacingAdvertiser(a);
//                    advertisers.add(pa);
//                    for (PacingLineItem l : activeLines) {
//                        if (l.getAdvertiserId().equals(a.getAdvertiserID())) {
//                            pa.getLineItems().add(l);
//                        }
//                    }
//                }
//
//            }
//        }
//
//
//    }
//}
