package com.mediacrossing.publisher_reporting;

import com.mediacrossing.campaignbooks.DataParse;
import com.mediacrossing.properties.ConfigurationProperties;
import com.mediacrossing.report_requests.AppNexusReportRequests;
import com.mediacrossing.segmenttargeting.HTTPConnection;
import com.mediacrossing.segmenttargeting.XlsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class RunPublisherReporting {

    private static final Logger LOG = LoggerFactory.getLogger(RunPublisherReporting.class);

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
        String rawJsonData;
        String outputPath = properties.getOutputPath();
        String appNexusUsername = properties.getAppNexusUsername();
        String appNexusPassword = properties.getAppNexusPassword();
        String mxUsername = properties.getMxUsername();
        String mxPassword = properties.getMxPassword();
        HTTPConnection httpConnection = new HTTPConnection(mxUsername, mxPassword);
        DataParse parser = new DataParse();

        httpConnection.authorizeAppNexusConnection(appNexusUsername, appNexusPassword);
        httpConnection.requestPublishersFromAN(appNexusUrl);
        rawJsonData = httpConnection.getJSONData();
        ArrayList<Publisher> pl = parser.parsePublisherIds(rawJsonData);
        ArrayList<Publisher> newPl = new ArrayList<Publisher>();
        for(Publisher pub : pl) {
            List<String[]> csvData =
                    AppNexusReportRequests.getPublisherReport(pub.getId(),appNexusUrl,httpConnection);

            //remove header
            csvData.remove(0);

            //for every row in the file
            for (String[] line : csvData) {
               newPl.add(new Publisher(line[0], pub.getPublisherName(), Float.parseFloat(line[1]), Integer.parseInt(line[2]),
                        Integer.parseInt(line[3]), Float.parseFloat(line[4]), Float.parseFloat(line[5]),
                        Float.parseFloat(line[6]), Float.parseFloat(line[7])));
            }
        }
        LOG.debug(newPl.toString());
        pl = newPl;
        XlsWriter.writePublisherReport(pl, outputPath);

    }
}
