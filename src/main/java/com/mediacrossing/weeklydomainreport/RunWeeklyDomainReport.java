package com.mediacrossing.weeklydomainreport;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.properties.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        String appNexusUrl = properties.getPutneyUrl();
        String outputPath = properties.getOutputPath();

        AppNexusService anConn = new AppNexusService(appNexusUrl
        );

        //Parse and save to list of advertisers
        final ArrayList<Domain> domains = anConn.requestDomainReport();

        //Write report
        WeeklyDomainReportWriter.writeReportToFile(domains, outputPath);
    }
}