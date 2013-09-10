package com.mediacrossing.campaignbooks;

import com.mediacrossing.segmenttargeting.HTTPRequest;

import java.io.FileNotFoundException;
import java.util.List;

public class Run {

    public static void main(String[] args) throws Exception {
        //Declare variables
        String mxUrl = "http://ec2-50-17-18-117.compute-1.amazonaws.com:9000/api/catalog";
        String rawJsonData;
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
                advertiser = new Advertiser(advertiser.getAdvertiserID(), lineItemList);
                advertiserList.set(count, advertiser);
            } catch (FileNotFoundException e) {
                advertiserList.get(count).setLive(false);
                System.out.println(advertiser.getAdvertiserID() + ": No line items found, live set to false.");
            }


            count++;
        }
        System.out.println(advertiserList.toString());


        //Query AN for campaign quick stats interval=yesterday

        //Save each campaign to the line item

        //Build and save excel book, each sheet being its own line item
        ExcelWriter excelWriter = new ExcelWriter();
        for(Advertiser advertiser : advertiserList) {
            if(advertiser.isLive()) {
                for(LineItem lineItem : advertiser.getLineItemList()) {
                    excelWriter.writeLineItemSheetToWorkbook(lineItem);
                }
            }
        }
        excelWriter.writeWorkbookToFile();

    }
}
