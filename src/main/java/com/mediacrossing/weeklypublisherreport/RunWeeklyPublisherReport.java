package com.mediacrossing.weeklypublisherreport;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.properties.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.io.*;
import java.util.ArrayList;

public class RunWeeklyPublisherReport {

    private static final Logger LOG = LoggerFactory.getLogger(RunWeeklyPublisherReport.class);

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

        //for faster debugging
        boolean development = false;
        if (development) {
            try{
                FileInputStream door = new FileInputStream("/Users/charronkyle/Desktop/ReportData/WeeklyPubList.ser");
                ObjectInputStream reader = new ObjectInputStream(door);
                ArrayList<WeeklyPublisher> pubList = (ArrayList<WeeklyPublisher>) reader.readObject();
                for (WeeklyPublisher p : pubList) {
                    WeeklyReportWriter.writeReportToFile(p, outputPath);
                }
                System.exit(0);
            }catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        }

        //Request publishers
        ArrayList<WeeklyPublisher> largerPubList = anConn.requestWeeklyPublishers();
        ArrayList<WeeklyPublisher> pubList = new ArrayList<WeeklyPublisher>();

        //remove inactive publishers
        for (WeeklyPublisher p : largerPubList) {
            if (p.getStatus().equals("active")) pubList.add(p);
        }

        for (WeeklyPublisher p : pubList) {
            p.setPaymentRules(anConn.requestPaymentRules(p.getId()));
            p.setPlacements(anConn.requestWeeklyPublisherReport(p.getId()));
        }

        // Serialize data object to a file
        try {
            ObjectOutputStream out = new ObjectOutputStream
                    (new FileOutputStream("/Users/charronkyle/Desktop/ReportData/WeeklyPubList.ser"));
            out.writeObject(pubList);
            out.close();
        } catch (IOException e) {
            LOG.error("Serialization Failed!");
            LOG.error(e.toString());
        }

        //Create file for every publisher
        for (WeeklyPublisher p : pubList) {
           WeeklyReportWriter.writeReportToFile(p, outputPath);
        }

    }
}
