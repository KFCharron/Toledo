package com.mediacrossing.advertiser_daily_report;

import com.mediacrossing.campaignbooks.*;
import com.mediacrossing.connections.ConnectionRequestProperties;
import com.mediacrossing.properties.ConfigurationProperties;
import com.mediacrossing.report_requests.AppNexusReportRequests;
import com.mediacrossing.segmenttargeting.HTTPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.FileNotFoundException;
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

        //Query MX for line items of each advertiser, save them to advertiser list
        int count = 0;
        for (Advertiser advertiser : advertiserList) {
            httpConnection.setUrl(mxUrl + "/api/catalog/advertisers/" + advertiser.getAdvertiserID() + "/line-items");
            try {
                httpConnection.requestData(mxRequestProperties);
                rawJsonData = httpConnection.getJSONData();
                List<LineItem> lineItemList = parser.populateLineItemList(rawJsonData);
                for (LineItem lineItem : lineItemList) {
                    httpConnection.setUrl(mxUrl + "/api/catalog/advertisers/" + advertiser.getAdvertiserID() +
                            "/line-items/" + lineItem.getLineItemID() + "/campaigns");
                    httpConnection.requestData(mxRequestProperties);
                    rawJsonData = httpConnection.getJSONData();
                    List<Campaign> campaignList = parser.populateCampaignList(rawJsonData);
                    lineItem.setCampaignList(campaignList);
                }
                advertiser = new Advertiser(advertiser.getAdvertiserID(), lineItemList);
                advertiserList.set(count, advertiser);
            } catch (FileNotFoundException e) {
                advertiserList.get(count).setLive(false);
                LOG.debug(advertiser.getAdvertiserID() + ": No line items found, live set to false.");
            }
            count++;
        }

        httpConnection.authorizeAppNexusConnection(appNexusUsername, appNexusPassword);

        //For every advertiser, request report
        for (Advertiser advertiser : advertiserList) {
            if (advertiser.isLive()) {

                List<String[]> csvData = AppNexusReportRequests.getLineItemReport("yesterday", advertiser.getAdvertiserID(),
                        appNexusUrl, httpConnection);

                //remove header string
                csvData.remove(0);

                //add yesterday stats to line item
                for (String[] line : csvData) {
                    ReportData reportData = new ReportData(Integer.parseInt(line[1]), Integer.parseInt(line[2]),
                            Integer.parseInt(line[3]), Float.parseFloat(line[4]), Float.parseFloat(line[5]),
                            Float.parseFloat(line[6]), Float.parseFloat(line[7]), Float.parseFloat(line[8]), line[0]);
                    for (LineItem li : advertiser.getLineItemList()) {
                        if(li.getLineItemID().equals(reportData.getId()))
                            li.setDayReportData(reportData);
                    }
                }

                csvData = AppNexusReportRequests.getCampaignReport("yesterday", advertiser.getAdvertiserID(),
                        appNexusUrl, httpConnection);

                //remove header string
                csvData.remove(0);

                for (String[] line : csvData) {
                    ReportData reportData = new ReportData(Integer.parseInt(line[1]), Integer.parseInt(line[2]),
                            Integer.parseInt(line[3]), Float.parseFloat(line[4]), Float.parseFloat(line[5]),
                            Float.parseFloat(line[6]), Float.parseFloat(line[7]), Float.parseFloat(line[8]), line[0]);
                    for (LineItem li : advertiser.getLineItemList()) {
                        for (Campaign camp : li.getCampaignList()) {
                            if (camp.getCampaignID().equals(reportData.getId()))
                                camp.setDayReportData(reportData);
                        }
                    }
                }

                csvData = AppNexusReportRequests.getLineItemReport("lifetime", advertiser.getAdvertiserID(),
                        appNexusUrl, httpConnection);

                //remove header string
                csvData.remove(0);

                //add lifetime stats to lineitem
                for (String[] line : csvData) {
                    ReportData reportData = new ReportData(Integer.parseInt(line[1]), Integer.parseInt(line[2]),
                            Integer.parseInt(line[3]), Float.parseFloat(line[4]), Float.parseFloat(line[5]),
                            Float.parseFloat(line[6]), Float.parseFloat(line[7]), Float.parseFloat(line[8]), line[0]);
                    for (LineItem li : advertiser.getLineItemList()) {
                        if(li.getLineItemID().equals(reportData.getId()))
                            li.setLifetimeReportData(reportData);
                    }
                }

                csvData = AppNexusReportRequests.getCampaignReport("lifetime", advertiser.getAdvertiserID(),
                        appNexusUrl, httpConnection);

                //remove header string
                csvData.remove(0);

                //add lifetime stats to campaign
                for (String[] line : csvData) {

                    ReportData reportData = new ReportData(Integer.parseInt(line[1]), Integer.parseInt(line[2]),
                            Integer.parseInt(line[3]), Float.parseFloat(line[4]), Float.parseFloat(line[5]),
                            Float.parseFloat(line[6]), Float.parseFloat(line[7]), Float.parseFloat(line[8]), line[0]);

                    for (String str : line) {
                        LOG.debug(str);
                    }
                    LOG.debug(reportData.getImps() + " " + reportData.getClicks() +
                            " " + reportData.getTotalConversions() + " " + reportData.getMediaCost());

                    for (LineItem li : advertiser.getLineItemList()) {
                        for (Campaign camp : li.getCampaignList()) {
                            if (camp.getCampaignID().equals(reportData.getId()))
                                camp.setLifetimeReportData(reportData);
                        }
                    }
                }
            }
        }
        ExcelWriter.writeDailyAdvertiserReport(advertiserList, outputPath);
    }
}
