package com.mediacrossing.campaignbooks;

import au.com.bytecode.opencsv.CSVReader;
import com.mediacrossing.connections.ConnectionRequestProperties;
import com.mediacrossing.segmenttargeting.HTTPConnection;
import scala.Tuple2;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

public class Run {

    public static void main(String[] args) throws Exception {

        //Declare variables
        //TODO Externalize configuration
        String mxUrl = "https://rtui.mediacrossing.com";
        String appNexusUrl = "http://api.appnexus.com";
        String rawJsonData;
        String outputPath = "/Users/charronkyle/Desktop/CampaignBook.xls";
        String appNexusUsername = "MC_report";
        String appNexusPassword = "13MediaCrossing!";
        String mxUsername = "rtui";
        String mxPassword = "stats4all";
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

        ArrayList<String> reportIdList = new ArrayList<String>();
        //For every advertiser, request report
        for (Advertiser advertiser : advertiserList) {
            String reportId = httpConnection.requestAdvertiserReport(advertiser.getAdvertiserID());
            boolean ready = false;
            while (!ready) {
                //Check to see if report is ready
                String jsonResponse = httpConnection.fetchDownloadUrl(reportId);
                System.out.println(jsonResponse);
                ready = parser.parseReportStatus(jsonResponse);
                if (!ready)
                    Thread.sleep(20000);
            }
            String downloadUrl = appNexusUrl + parser.getReportUrl();
            httpConnection.requestDownload(downloadUrl);
            System.out.println(httpConnection.getJSONData());

            InputStream is = new ByteArrayInputStream(httpConnection.getJSONData().getBytes());

            CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(is)));
            String [] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                System.out.println(nextLine[0] + nextLine[1] + " etc...");
            }
            //parse the string into object
            //match data to advertisers??
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
