package com.mediacrossing.weeklydomainreport;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.properties.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;

public class RunWeeklyDomainReport {

    private static final Logger LOG = LoggerFactory.getLogger(RunWeeklyDomainReport.class);

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

        //Parse and save to list of advertisers
        final ArrayList<Domain> domains = anConn.requestDomainReport();

        // Serialize data object to a file
        /*try {
            ObjectOutputStream out = new ObjectOutputStream
                    (new FileOutputStream("/Users/charronkyle/Desktop/ReportData/DomainList.ser"));
            out.writeObject(domains);
            out.close();
        } catch (IOException e) {
            LOG.error("Serialization Failed!");
            LOG.error(e.toString());
        }*/

        //Write report
        WeeklyDomainReportWriter.writeReportToFile(domains, outputPath);
    }
}