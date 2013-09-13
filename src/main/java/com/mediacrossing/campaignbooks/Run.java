package com.mediacrossing.campaignbooks;

import com.mediacrossing.connections.ConnectionRequestProperties;
import com.mediacrossing.segmenttargeting.HTTPRequest;
import scala.Tuple2;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

public class Run {

    public static void main(String[] args) throws Exception {

        //Declare variables
        // FIXME Externalize configuration
        String mxUrl = "https://rtui.mediacrossing.com/api/catalog";
        String rawJsonData;
        String outputPath = "";
        String appNexusUsername = "MC_report";
        String appNexusPassword = "13MediaCrossing!";
        String mxUsername = "rtui";
        String mxPassword = "stats4all";
        HTTPRequest httpConnection = new HTTPRequest(mxUsername, mxPassword);
        DataParse parser = new DataParse();


        final List<Tuple2<String, String>> mxRequestProperties =
                Collections.unmodifiableList(
                        Arrays.asList(
                                ConnectionRequestProperties.authorization(
                                        mxUsername,
                                        mxPassword)));

        //Query MX for all advertisers
        httpConnection.setUrl(mxUrl + "/advertisers");
        httpConnection.requestData(mxRequestProperties);
        rawJsonData = httpConnection.getJSONData();

        //Parse and save to list of advertisers
        final List<Advertiser> advertiserList = parser.populateAdvertiserList(rawJsonData);

        //Query MX for line items of each advertiser, save them to advertiser list
        int count = 0;
        for (Advertiser advertiser : advertiserList) {
            httpConnection.setUrl(mxUrl + "/advertisers/" + advertiser.getAdvertiserID() + "/line-items");
            try {
                httpConnection.requestData(mxRequestProperties);
                rawJsonData = httpConnection.getJSONData();
                List<LineItem> lineItemList = parser.populateLineItemList(rawJsonData);
                for (LineItem lineItem : lineItemList) {
                    httpConnection.setUrl(mxUrl + "/advertisers/" + advertiser.getAdvertiserID() +
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

        ArrayList<String> reportIdList = new ArrayList<String>();
        //For every advertiser, request report
        for (Advertiser advertiser : advertiserList)
            reportIdList.add(httpConnection.requestAdvertiserReport(advertiser.getAdvertiserID()));

        ArrayList<String> downloadUrlList = new ArrayList<String>();
        for (String reportId : reportIdList) {
            boolean ready = false;
            while (!ready) {
                //Check to see if report is ready
                String jsonResponse = httpConnection.fetchDownloadUrl(reportId);
                ready = parser.parseReportStatus(jsonResponse);
                if (!ready)
                    Thread.sleep(10000);
            }
            downloadUrlList.add(parser.getReportUrl());
        }

        for (String downloadUrl : downloadUrlList) {
            //input stream to take in csv
            //parse the string into object
            //match data to advetisers??
        }
        //when ready, download report as input stream.
        //parse csv input stream
        //save csv vars into campaigns

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
