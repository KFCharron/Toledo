package com.mediacrossing.weeklyconversionreport;

import com.mediacrossing.connections.ConnectionRequestProperties;
import com.mediacrossing.properties.ConfigurationProperties;
import com.mediacrossing.reportrequests.AppNexusReportRequests;
import com.mediacrossing.segmenttargeting.HTTPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RunConversionReport {

    private static final Logger LOG = LoggerFactory.getLogger(RunConversionReport.class);

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
        String mxUrl = properties.getMxUrl();
        String appNexusUrl = properties.getAppNexusUrl();
        String rawJsonData;
        String outputPath = properties.getOutputPath();
        String appNexusUsername = properties.getAppNexusUsername();
        String appNexusPassword = properties.getAppNexusPassword();
        String mxUsername = properties.getMxUsername();
        String mxPassword = properties.getMxPassword();
        HTTPConnection httpConnection = new HTTPConnection(mxUsername, mxPassword);

        //for faster debugging
        boolean development = false;
        if (development) {
            try{
                FileInputStream door = new FileInputStream("/Users/charronkyle/Desktop/ConvAdvertiserList.ser");
                ObjectInputStream reader = new ObjectInputStream(door);
                ArrayList<ConversionAdvertiser> adList = (ArrayList<ConversionAdvertiser>) reader.readObject();
                ConversionReportWriter.writeReportToFile(adList, outputPath);
                System.exit(0);

            }catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        }

        //request advertisers
        final List<Tuple2<String, String>> mxRequestProperties =
                Collections.unmodifiableList(
                        Arrays.asList(
                                ConnectionRequestProperties.authorization(
                                        mxUsername,
                                        mxPassword)));


        //Query MX for all advertisers
        httpConnection.setUrl(mxUrl + "/api/catalog/advertisers");
        httpConnection.requestData(mxRequestProperties);
        rawJsonData = httpConnection.getJSONData();

        //Authorize AN connection
        httpConnection.authorizeAppNexusConnection(appNexusUsername, appNexusPassword);

        //Parse and save to list of advertisers
        final ArrayList<ConversionAdvertiser> liveAdvertiserList = ConversionParser.populateLiveAdvertiserList(rawJsonData);

        //request report for each advertiser
        for (ConversionAdvertiser ad : liveAdvertiserList) {

            //request yesterday line item report
            List<String[]> csvData = AppNexusReportRequests.getConversionReport(ad.getAdvertiserId(),
                    appNexusUrl, httpConnection);

            //remove header string
            csvData.remove(0);

            //add all stats to advertiser
            ArrayList<ConversionData> cdList = new ArrayList<ConversionData>();
            for (String[] l : csvData) {
                ConversionData cd = new ConversionData();
                cd.setLineItem(l[0]);
                cd.setCampaign(l[1]);
                cd.setOrderId(l[2]);
                cd.setUserId(l[3]);
                cd.setPostClickOrPostViewConv(l[4]);
                cd.setCreative(l[5]);
                cd.setAuctionId(l[6]);
                cd.setExternalData(l[7]);
                cd.setImpTime(l[8]);
                cd.setDatetime(l[9]);
                cd.setPixelId(l[10]);
                cd.setPixelName(l[11]);
                cd.setImpType(l[12]);
                cd.setPostClickOrPostViewRevenue(l[13]);
                cdList.add(cd);
            }
            ad.setConversionDataList(cdList);

        }

        // Serialize data object to a file
        /*try {
            ObjectOutputStream out = new ObjectOutputStream
                    (new FileOutputStream("/Users/charronkyle/Desktop/ConvAdvertiserList.ser"));
            out.writeObject(liveAdvertiserList);
            out.close();
        } catch (IOException e) {
            LOG.error("Serialization Failed!");
            LOG.error(e.toString());
        }*/


        //Write report
        ConversionReportWriter.writeReportToFile(liveAdvertiserList, outputPath);

    }
}
