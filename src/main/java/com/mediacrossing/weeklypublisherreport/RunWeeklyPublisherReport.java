package com.mediacrossing.weeklypublisherreport;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.properties.ConfigurationProperties;
import com.mediacrossing.publishercheckup.PublisherConfig;
import com.mediacrossing.publisherreporting.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
        AppNexusService anConn = new AppNexusService(appNexusUrl, appNexusUsername,
                appNexusPassword);

        //for faster debugging
        boolean development = false;
        if (development) {
            try{
                FileInputStream door = new FileInputStream("/Users/charronkyle/Desktop/ReportData/WeeklyPubList.ser");
                ObjectInputStream reader = new ObjectInputStream(door);
                //ArrayList<PublisherConfig> adList = (ArrayList<ConversionAdvertiser>) reader.readObject();
                //ConversionReportWriter.writeReportToFile(adList, outputPath);
                System.exit(0);
            }catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        }

        //Request publishers
        ArrayList<WeeklyPublisher> pubList = anConn.requestWeeklyPublishers();

        //Request pub payment rule
        for (WeeklyPublisher p : pubList) {
            p.setPaymentRules(anConn.requestPaymentRules(p.getId()));
            //p.setPlacements(anConn.requestWeeklyPublisherReport(p.getId()));
        }

        //Request net pub report









        // Serialize data object to a file
        /*try {
            ObjectOutputStream out = new ObjectOutputStream
                    (new FileOutputStream("/Users/charronkyle/Desktop/ReportData/WeeklyPubList.ser"));
            out.writeObject(//TODO);
            out.close();
        } catch (IOException e) {
            LOG.error("Serialization Failed!");
            LOG.error(e.toString());
        }*/

    }
}
