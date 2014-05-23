package com.mediacrossing.campaignbooks;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.properties.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        String outputPath = properties.getOutputPath();
        String appNexusUsername = properties.getAppNexusUsername();
        String appNexusPassword = properties.getAppNexusPassword();
        String mxUsername = properties.getMxUsername();
        String mxPassword = properties.getMxPassword();
        MxService mxConn;
        if (mxUsername == null) {
            mxConn = new MxService(mxUrl);
        } else {
            mxConn = new MxService(mxUrl, mxUsername, mxPassword);
        }
        AppNexusService anConn = new AppNexusService(appNexusUrl, appNexusUsername,
                appNexusPassword, properties.getPartitionSize(), properties.getRequestDelayInSeconds());

        //Parse and save to list of advertisers
        final List<Advertiser> advertiserList = mxConn.requestAllAdvertisers();

        //Query MX for line items and campaigns of each advertiser, save them to advertiser list
        int count = 0;
        for (Advertiser ad : advertiserList) {
            try {
                ArrayList<LineItem> lineItemList = mxConn.requestLineItemsForAdvertiser(ad.getAdvertiserID());
                Collections.sort(lineItemList, new Comparator<LineItem>() {
                    @Override
                    public int compare(LineItem o1, LineItem o2) {
                        return o1.getLineItemID().compareTo(o2.getLineItemID());
                    }
                });
                for (LineItem lineItem : lineItemList) {

                    ArrayList<Campaign> campaignList =
                            mxConn.requestCampaignsForLineItem(ad.getAdvertiserID(), lineItem.getLineItemID());
                    lineItem.setCampaignList(campaignList);
                }
                ad = new Advertiser(ad.getAdvertiserID(), ad.getAdvertiserName(), lineItemList);
                advertiserList.set(count, ad);
            } catch (FileNotFoundException e) {
                advertiserList.get(count).setLive(false);
                LOG.debug(ad.getAdvertiserID() + ": No line items found, live set to false.");
            }
            count++;
        }

        //restrict requested reports to only advertisers w/ live campaigns
        for (Advertiser ad : advertiserList) {
            ad.setLive(false);
            for(LineItem li : ad.getLineItemList()) {
                if(li.getDaysRemaining() > 0 && li.getEndDateTime()!= null && !li.getLineItemName().contains("TEST")) {
                    ad.setLive(true);
                }
            }
        }

        //Create new list with only live advertisers
        final ArrayList<Advertiser> liveAdvertiserList = new ArrayList<>();
        for (Advertiser ad : advertiserList) {
            if (ad.isLive()) {
                liveAdvertiserList.add(ad);
            }
        }

        //For every advertiser, request report
        for (Advertiser ad : liveAdvertiserList) {

            //request daily deliveries
            List<String[]> csvData = anConn.getAdvertiserAnalyticReport(ad.getAdvertiserID());

            //remove header string
            csvData.remove(0);

            //Creates new delivery, adds it to campaign if ids match
            for (String[] line : csvData) {
                Delivery delivery = new Delivery(line[0], line[1], line[2], line[3], line[4], line[5], line[6]);
                for(LineItem lineItem : ad.getLineItemList()) {
                    for(Campaign campaign : lineItem.getCampaignList()) {
                        if (campaign.getCampaignID().equals(delivery.getCampaignID())) {
                            campaign.addToDeliveries(delivery);
                            campaign.setTotalDelivery(campaign.getTotalDelivery() + delivery.getDelivery());
                        }
                    }
                }
            }

            //request lifetime stats
            csvData = anConn.getLifetimeAdvertiserReport(ad.getAdvertiserID());

            //remove header string
            csvData.remove(0);

            //Creates new delivery, adds it to campaign if ids match
            for (String[] line : csvData) {
                for(LineItem li : ad.getLineItemList()) {
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

        //Build and save excel book, each sheet being its own advertiser
        for (Advertiser advertiser : liveAdvertiserList) {
            ExcelWriter.writeAdvertiserSheetToWorkbook(advertiser);
        }
        ExcelWriter.writeWorkbookToFileWithOutputPath(outputPath);
    }
}
