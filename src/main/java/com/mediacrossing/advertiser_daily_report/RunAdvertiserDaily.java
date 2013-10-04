package com.mediacrossing.advertiser_daily_report;

import com.mediacrossing.campaignbooks.*;
import com.mediacrossing.connections.ConnectionRequestProperties;
import com.mediacrossing.properties.ConfigurationProperties;
import com.mediacrossing.report_requests.AppNexusReportRequests;
import com.mediacrossing.segmenttargeting.HTTPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RunAdvertiserDaily {

    private static final Logger LOG = LoggerFactory.getLogger(RunAdvertiserDaily.class);

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

    @SuppressWarnings("unchecked")
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
        DataParse parser = new DataParse();


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

        //Parse and save to list of advertisers
        final List<Advertiser> advertiserList = parser.populateAdvertiserList(rawJsonData);
        final List<Advertiser> liveAdvertiserList = new ArrayList<Advertiser>();
        for(Advertiser ad : advertiserList) {
            if(ad.isLive()) {
                liveAdvertiserList.add(ad);
            }
        }

        httpConnection.authorizeAppNexusConnection(appNexusUsername, appNexusPassword);

        //For every advertiser, request report
        for (Advertiser advertiser : liveAdvertiserList) {

            List<String[]> csvData = AppNexusReportRequests.getLineItemReport("yesterday", advertiser.getAdvertiserID(),
                    appNexusUrl, httpConnection);

            //remove header string
            csvData.remove(0);

            //add yesterday stats to line item
            ArrayList<DailyData> dailyLineItems = new ArrayList<DailyData>();
            for (String[] line : csvData) {
                DailyData data = new DailyData();
                data.setId(line[0]);
                data.setName(line[1]);
                data.setImps(line[2]);
                data.setClicks(line[3]);
                data.setTotalConv(line[4]);
                data.setMediaCost(line[5]);
                data.setCtr(line[6]);
                data.setConvRate(line[7]);
                data.setCpm(line[8]);
                data.setCpc(line[9]);
                dailyLineItems.add(data);
            }
            advertiser.setDailyLineItems(dailyLineItems);


            csvData = AppNexusReportRequests.getLineItemReport("lifetime", advertiser.getAdvertiserID(),
                    appNexusUrl, httpConnection);

            //remove header string
            csvData.remove(0);

            //add lifetime stats to line item
            ArrayList<DailyData> lifetimeLineItems = new ArrayList<DailyData>();
            for (String[] line : csvData) {
                DailyData data = new DailyData();
                data.setId(line[0]);
                data.setName(line[1]);
                data.setImps(line[2]);
                data.setClicks(line[3]);
                data.setTotalConv(line[4]);
                data.setMediaCost(line[5]);
                data.setCtr(line[6]);
                data.setConvRate(line[7]);
                data.setCpm(line[8]);
                data.setCpc(line[9]);
                lifetimeLineItems.add(data);
            }
            advertiser.setLifetimeLineItems(lifetimeLineItems);


            csvData = AppNexusReportRequests.getCampaignReport("yesterday", advertiser.getAdvertiserID(),
                    appNexusUrl, httpConnection);

            //remove header string
            csvData.remove(0);

            //add yesterday stats to campaign
            ArrayList<DailyData> dailyCampaigns = new ArrayList<DailyData>();
            for (String[] line : csvData) {
                DailyData data = new DailyData();
                data.setId(line[0]);
                data.setName(line[1]);
                data.setImps(line[2]);
                data.setClicks(line[3]);
                data.setTotalConv(line[4]);
                data.setMediaCost(line[5]);
                data.setCtr(line[6]);
                data.setConvRate(line[7]);
                data.setCpm(line[8]);
                data.setCpc(line[9]);
                dailyCampaigns.add(data);
            }
            advertiser.setDailyCampaigns(dailyCampaigns);


            csvData = AppNexusReportRequests.getCampaignReport("lifetime", advertiser.getAdvertiserID(),
                    appNexusUrl, httpConnection);

            //remove header string
            csvData.remove(0);

            //add yesterday stats to line item
            ArrayList<DailyData> lifetimeCampaigns = new ArrayList<DailyData>();
            for (String[] line : csvData) {
                DailyData data = new DailyData();
                data.setId(line[0]);
                data.setName(line[1]);
                data.setImps(line[2]);
                data.setClicks(line[3]);
                data.setTotalConv(line[4]);
                data.setMediaCost(line[5]);
                data.setCtr(line[6]);
                data.setConvRate(line[7]);
                data.setCpm(line[8]);
                data.setCpc(line[9]);
                lifetimeCampaigns.add(data);
            }
            advertiser.setLifetimeCampaigns(lifetimeCampaigns);

        }

        /*final ObjectOutputStream fos = new ObjectOutputStream(new FileOutputStream(outputPath+"/test.dat", true));
        try {
            fos.writeObject(advertiserList);
            fos.close();
        } catch (IOException e) {
            LOG.debug("Saving object failed.");
            e.printStackTrace();
        }*/

        ReportWriter.writeAdvertiserDailyReport(liveAdvertiserList, outputPath);
    }
}
