package com.mediacrossing.campaignbooks;

import com.mediacrossing.segmenttargeting.HTTPRequest;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Run {

    public static void main(String[] args) throws Exception {

        //Declare variables
        String mxUrl = "http://ec2-50-17-18-117.compute-1.amazonaws.com:9000/api/catalog";
        String rawJsonData;
        String outputPath = "";
        String appNexusUsername = "MC_report";
        String appNexusPassword = "13MediaCrossing!";
        HTTPRequest httpConnection = new HTTPRequest();
        DataParse parser = new DataParse();

        //Query MX for all advertisers
        httpConnection.setUrl(mxUrl + "/advertisers");
        httpConnection.requestData();
        rawJsonData = httpConnection.getJSONData();

        //Parse and save to list of advertisers
        final List<Advertiser> advertiserList = parser.populateAdvertiserList(rawJsonData);

        //Query MX for line items of each advertiser, save them to advertiser list
        int count = 0;
        for (Advertiser advertiser : advertiserList) {
            httpConnection.setUrl(mxUrl + "/advertisers/" + advertiser.getAdvertiserID() + "/line-items");
            try {
                httpConnection.requestData();
                rawJsonData = httpConnection.getJSONData();
                List<LineItem> lineItemList = parser.populateLineItemList(rawJsonData);
                for(LineItem lineItem : lineItemList) {
                    httpConnection.setUrl(mxUrl + "/advertisers/" + advertiser.getAdvertiserID() +
                            "/line-items/" + lineItem.getLineItemID() + "/campaigns");
                    httpConnection.requestData();
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
        for(Advertiser advertiser : advertiserList)
            reportIdList.add(httpConnection.requestAdvertiserReport(advertiser.getAdvertiserID()));

        //collect report ids
        //check report until status is ready
        //when ready, download report as input stream.
        //parse csv input stream
        //save csv vars into campaigns

        //Build and save excel book, each sheet being its own line item
        ExcelWriter excelWriter = new ExcelWriter();
        for(Advertiser advertiser : advertiserList) {
            if(advertiser.isLive()) {
                for(LineItem lineItem : advertiser.getLineItemList()) {
                    excelWriter.writeLineItemSheetToWorkbook(lineItem);
                }
            }
        }
        excelWriter.writeWorkbookToFileWithOutputPath(outputPath);

    }
}
