package com.mediacrossing.advertiserdailyreport;

import com.mediacrossing.campaignbooks.*;
import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.properties.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

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
        String outputPath = properties.getOutputPath();
        String mxUsername = properties.getMxUsername();
        String mxPassword = properties.getMxPassword();
        MxService mxConn;
        if (mxUsername == null) {
            mxConn = new MxService(mxUrl);
        } else {
            mxConn = new MxService(mxUrl, mxUsername, mxPassword);
        }
        AppNexusService anConn = new AppNexusService(appNexusUrl, properties.getAppNexusUsername(),
                properties.getAppNexusPassword());

        //for faster debugging
        boolean development = false;
        if (development) {
            try{
                FileInputStream door = new FileInputStream("/Users/charronkyle/Desktop/AdvertiserList.ser");
                ObjectInputStream reader = new ObjectInputStream(door);
                List<Advertiser> adList = (List<Advertiser>) reader.readObject();
                ReportWriter.writeAdvertiserDailyReport(adList, outputPath);
                System.exit(0);

            }catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        }

        //Parse and save to list of advertisers
        final List<Advertiser> advertiserList = mxConn.requestAllAdvertisers();
        final List<Advertiser> liveAdvertiserList = new ArrayList<Advertiser>();

        for(Advertiser ad : advertiserList) {
            if(ad.isLive()) {
                liveAdvertiserList.add(ad);
            }
        }

        //For every advertiser, request report
        for (Advertiser advertiser : liveAdvertiserList) {

            //request yesterday line item report
            List<String[]> csvData = anConn.getLineItemReport("yesterday", advertiser.getAdvertiserID());

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

            //request lifetime line item report
            csvData = anConn.getLineItemReport("lifetime", advertiser.getAdvertiserID());

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

            //request yesterday campaign report
            csvData = anConn.getCampaignReport("yesterday", advertiser.getAdvertiserID());

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

            //request lifetime campaign report
            csvData = anConn.getCampaignReport("lifetime", advertiser.getAdvertiserID());

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

            //populate each advertiser with matching data
            for (Advertiser ad : liveAdvertiserList) {
                //get line item data
                //save to a list of line items
                List<LineItem> lineItems = mxConn.requestLineItemsForAdvertiser(ad.getAdvertiserID());
                for(LineItem li : lineItems) {
                    for(DailyData data : lifetimeLineItems) {
                        if(li.getLineItemID().equals(data.getId())) {
                            data.setDailyBudget(li.getDailyBudget());
                            data.setStartDay(li.getStartDateTime());
                            data.setEndDay(li.getEndDateTime());
                            data.setLifetimeBudget(li.getLifetimeBudget());
                            data.setStatus(li.getStatus());
                        }
                    }
                    for(DailyData data : dailyLineItems) {
                        if(li.getLineItemID().equals(data.getId())) {
                            data.setDailyBudget(li.getDailyBudget());
                            data.setStartDay(li.getStartDateTime());
                            data.setEndDay(li.getEndDateTime());
                            data.setLifetimeBudget(li.getLifetimeBudget());
                            data.setStatus(li.getStatus());
                        }
                    }

                    //get campaign data
                    List<Campaign> campaigns = mxConn.requestCampaignsForLineItem(advertiser.getAdvertiserID(),
                            li.getLineItemID());
                    for(Campaign camp : campaigns) {
                        for(DailyData data : lifetimeCampaigns) {
                            if(camp.getCampaignID().equals(data.getId())) {
                                data.setDailyBudget(camp.getDailyBudget());
                                data.setStartDay(camp.getStartDate());
                                data.setEndDay(camp.getEndDate());
                                data.setLifetimeBudget(camp.getLifetimeBudget());
                                data.setStatus(camp.getStatus());
                            }
                        }
                        for(DailyData data : dailyCampaigns) {
                            if(camp.getCampaignID().equals(data.getId())) {
                                data.setDailyBudget(camp.getDailyBudget());
                                data.setStartDay(camp.getStartDate());
                                data.setEndDay(camp.getEndDate());
                                data.setLifetimeBudget(camp.getLifetimeBudget());
                                data.setStatus(camp.getStatus());
                            }
                        }
                    }
                }
            }
        }

        // Serialize data object to a file
        /*try {
            ObjectOutputStream out = new ObjectOutputStream
                    (new FileOutputStream("/Users/charronkyle/Desktop/AdvertiserList.ser"));
            out.writeObject(liveAdvertiserList);
            out.close();
        } catch (IOException e) {
            LOG.error("Serialization Failed!");
            LOG.error(e.toString());
        }*/




        //Write report
        ReportWriter.writeAdvertiserDailyReport(liveAdvertiserList, outputPath);
    }
}
