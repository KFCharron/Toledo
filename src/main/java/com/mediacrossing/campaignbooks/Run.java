package com.mediacrossing.campaignbooks;

import com.mediacrossing.campaignbooks.campaigns.CampaignRepository;
import com.mediacrossing.campaignbooks.campaigns.PartitionedCampaignRepository;
import com.mediacrossing.segmenttargeting.HTTPRequest;
import scala.concurrent.duration.Duration;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Run {

    private static int APPNEXUS_PARTITION_SIZE = 10;
    private static Duration APPNEXUS_REQUEST_DELAY = Duration.apply(60, TimeUnit.SECONDS);

    private static CampaignRepository production(HTTPRequest r) {
        return new PartitionedCampaignRepository(
                r,
                APPNEXUS_PARTITION_SIZE,
                APPNEXUS_REQUEST_DELAY);
    }

    public static void main(String[] args) throws Exception {
        //Declare variables
        String mxUrl = "http://ec2-50-17-18-117.compute-1.amazonaws.com:9000/api/catalog";
        String rawJsonData;
        String outputPath = "";
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
        final CampaignRepository campaignRepository = production(httpConnection);

        for(Advertiser advertiser : advertiserList) {
            if (advertiser.isLive()) {
                for(LineItem lineItem : advertiser.getLineItemList()) {
                    List<String> campaignIds = new ArrayList<String>();
                    for(Campaign campaign : lineItem.getCampaignList()) {
                        campaignIds.add(campaign.getCampaignID());
                    }
                    List<Float> dailyDelivery = campaignRepository.findBy(campaignIds);
                    //For each campaign, add corresponding float to daily delivery array
                    int index = 0;
                    for (Campaign campaign : lineItem.getCampaignList()) {
                        campaign.addToDailyDeliveryList(dailyDelivery.get(index));
                        index++;
                    }
                }
            }
        }

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
