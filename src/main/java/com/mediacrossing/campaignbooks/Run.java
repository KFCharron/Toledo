package com.mediacrossing.campaignbooks;

import com.mediacrossing.connections.ConnectionRequestProperties;
import com.mediacrossing.properties.ConfigurationProperties;
import com.mediacrossing.report_requests.AppNexusReportRequests;
import com.mediacrossing.segmenttargeting.HTTPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

public class Run {

    private static final Logger LOG = LoggerFactory.getLogger(Run.class);

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
                System.out.println(advertiser.getAdvertiserID() + ": No line items found, live set to false.");
            }
            count++;
        }

        httpConnection.authorizeAppNexusConnection(appNexusUsername, appNexusPassword);

        //For every advertiser, request report
        for (Advertiser advertiser : advertiserList) {

            List<String[]> csvData = AppNexusReportRequests.getAdvertiserAnalyticReport(advertiser.getAdvertiserID(),
                    httpConnection, appNexusUrl);

            //remove header string
            csvData.remove(0);

            //Creates new delivery, adds it to campaign if ids match
            for (String[] line : csvData) {
                Delivery delivery = new Delivery(line[0],line[1],line[2]);
                for(LineItem lineItem : advertiser.getLineItemList()) {
                    for(Campaign campaign : lineItem.getCampaignList()) {
                        if (campaign.getCampaignID().equals(delivery.getCampaignID()))
                            campaign.addToDeliveries(delivery);
                    }
                }
            }
        }

        //Build and save excel book, each sheet being its own line item
        ExcelWriter excelWriter = new ExcelWriter();
        for (Advertiser advertiser : advertiserList) {
            if (advertiser.isLive()) {
                for (LineItem lineItem : advertiser.getLineItemList()) {
                    excelWriter.writeLineItemSheetToWorkbook(lineItem);
                }
            }
        }
        excelWriter.writeWorkbookToFileWithOutputPath(outputPath);

    }


}
