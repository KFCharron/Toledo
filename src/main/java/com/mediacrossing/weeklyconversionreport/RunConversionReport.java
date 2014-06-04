package com.mediacrossing.weeklyconversionreport;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.properties.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
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
        String appNexusUrl = properties.getPutneyUrl();
        String outputPath = properties.getOutputPath();

        String mxUsername = properties.getMxUsername();
        String mxPassword = properties.getMxPassword();
        MxService mxConn;
        if (mxUsername == null) {
            mxConn = new MxService(mxUrl);
        } else {
            mxConn = new MxService(mxUrl, mxUsername, mxPassword);
        }
        AppNexusService anConn = new AppNexusService(appNexusUrl
        );

        //for faster debugging
        boolean development = false;
        if (development) {
            try{
                FileInputStream door = new FileInputStream("/Users/charronkyle/Desktop/ReportData/ConvAdvertiserList.ser");
                ObjectInputStream reader = new ObjectInputStream(door);
                ArrayList<ConversionAdvertiser> adList = (ArrayList<ConversionAdvertiser>) reader.readObject();
                ConversionReportWriter.writeReportToFile(adList, outputPath);
                System.exit(0);

            }catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        }

        //Parse and save to list of advertisers
        final ArrayList<ConversionAdvertiser> adList = mxConn.requestAllConversionAdvertisers();

        //request report for each advertiser
        for (ConversionAdvertiser ad : adList) {

            //request yesterday line item report
            List<String[]> csvData = anConn.getConversionReport(ad.getAdvertiserId());

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

        //Write report
        ConversionReportWriter.writeReportToFile(adList, outputPath);
    }
}
