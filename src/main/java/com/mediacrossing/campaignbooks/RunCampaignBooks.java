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

public class RunCampaignBooks {

    private static final Logger LOG = LoggerFactory.getLogger(RunCampaignBooks.class);

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

        //for faster debugging
        boolean development = true;
        if (development) {
            try{
                FileInputStream door = new FileInputStream("/Users/charronkyle/Desktop/CampaignBookData.ser");
                ObjectInputStream reader = new ObjectInputStream(door);
                List<Advertiser> adList = (List<Advertiser>) reader.readObject();
                for (Advertiser advertiser : adList) {
                    ExcelWriter.writeAdvertiserSheetToWorkbook(advertiser);
                }
                ExcelWriter.writeWorkbookToFileWithOutputPath(outputPath);
                System.exit(0);

            }catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        }


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

        //Query MX for line items and campaigns of each advertiser, save them to advertiser list
        int count = 0;
        for (Advertiser advertiser : advertiserList) {
            httpConnection.setUrl(mxUrl + "/api/catalog/advertisers/" + advertiser.getAdvertiserID() + "/line-items");
            try {
                httpConnection.requestData(mxRequestProperties);
                rawJsonData = httpConnection.getJSONData();
                ArrayList<LineItem> lineItemList = parser.populateLineItemList(rawJsonData);
                for (LineItem lineItem : lineItemList) {
                    httpConnection.setUrl(mxUrl + "/api/catalog/advertisers/" + advertiser.getAdvertiserID() +
                            "/line-items/" + lineItem.getLineItemID() + "/campaigns");
                    httpConnection.requestData(mxRequestProperties);
                    rawJsonData = httpConnection.getJSONData();
                    ArrayList<Campaign> campaignList = parser.populateCampaignList(rawJsonData);
                    lineItem.setCampaignList(campaignList);
                }
                advertiser = new Advertiser(advertiser.getAdvertiserID(), advertiser.getAdvertiserName(), lineItemList);
                advertiserList.set(count, advertiser);
            } catch (FileNotFoundException e) {
                advertiserList.get(count).setLive(false);
                LOG.debug(advertiser.getAdvertiserID() + ": No line items found, live set to false.");
            }
            count++;
        }

        //Authorize AN Connection
        httpConnection.authorizeAppNexusConnection(appNexusUsername, appNexusPassword);

        //restrict requested reports to only advertisers w/ live campaigns
        for (Advertiser ad : advertiserList) {
            ad.setLive(false);
            for(LineItem li : ad.getLineItemList()) {
                if(li.getDaysRemaining() > 0 && li.getEndDate()!= null) {
                    ad.setLive(true);
                }
            }
        }

        //Create new list with only live advertisers
        ArrayList<Advertiser> liveAdvertiserList = new ArrayList<Advertiser>();
        for (Advertiser ad : advertiserList) {
            if (ad.isLive()) {
                liveAdvertiserList.add(ad);
            }
        }

        //For every advertiser, request report
        for (Advertiser advertiser : liveAdvertiserList) {

            //request daily deliveries
            List<String[]> csvData = AppNexusReportRequests.getAdvertiserAnalyticReport(advertiser.getAdvertiserID(),
                        appNexusUrl, httpConnection);

            //remove header string
            csvData.remove(0);

            //Creates new delivery, adds it to campaign if ids match
            for (String[] line : csvData) {
                Delivery delivery = new Delivery(line[0], line[1], line[2], line[3], line[4], line[5], line[6]);
                for(LineItem lineItem : advertiser.getLineItemList()) {
                    for(Campaign campaign : lineItem.getCampaignList()) {
                        if (campaign.getCampaignID().equals(delivery.getCampaignID())) {
                            campaign.addToDeliveries(delivery);
                            campaign.setTotalDelivery(campaign.getTotalDelivery() + delivery.getDelivery());
                        }
                    }
                }
            }

            //request lifetime stats
            csvData = AppNexusReportRequests.getLifetimeAdvertiserReport(advertiser.getAdvertiserID(),
                    appNexusUrl, httpConnection);

            //remove header string
            csvData.remove(0);

            //Creates new delivery, adds it to campaign if ids match
            for (String[] line : csvData) {
                for(LineItem li : advertiser.getLineItemList()) {
                    for(Campaign camp : li.getCampaignList()) {
                       if(camp.getCampaignID().equals(line[0])) {
                           camp.setLifetimeImps(Integer.parseInt(line[1]));
                           camp.setLifetimeClicks(Integer.parseInt(line[2]));
                           camp.setLifetimeCtr(Float.parseFloat(line[3]));
                           camp.setLifetimeConvs(Integer.parseInt(line[4]));
                       }
                    }
                }
            }
        }

        //Remove the first delivery from each campaign, incomplete data
        for (Advertiser ad : liveAdvertiserList) {
            for(LineItem li : ad.getLineItemList()) {
                for(Campaign camp : li.getCampaignList()) {
                    if(camp.getDeliveries().size() > 0)
                        camp.getDeliveries().remove(0);
                }
            }
        }

        // Serialize data object to a file
        try {
            ObjectOutputStream out = new ObjectOutputStream
                    (new FileOutputStream("/Users/charronkyle/Desktop/CampaignBookData.ser"));
            out.writeObject(liveAdvertiserList);
            out.close();
        } catch (IOException e) {
            LOG.error("Serialization Failed!");
            LOG.error(e.toString());
        }

        //Build and save excel book, each sheet being its own advertiser
        for (Advertiser advertiser : liveAdvertiserList) {
            ExcelWriter.writeAdvertiserSheetToWorkbook(advertiser);
        }
        ExcelWriter.writeWorkbookToFileWithOutputPath(outputPath);
    }
}
