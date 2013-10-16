package com.mediacrossing.advertiserdailyreport;

import com.mediacrossing.campaignbooks.*;
import com.mediacrossing.connections.ConnectionRequestProperties;
import com.mediacrossing.properties.ConfigurationProperties;
import com.mediacrossing.reportrequests.AppNexusReportRequests;
import com.mediacrossing.segmenttargeting.HTTPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

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
        String rawJsonData;
        String outputPath = properties.getOutputPath();
        String appNexusUsername = properties.getAppNexusUsername();
        String appNexusPassword = properties.getAppNexusPassword();
        String mxUsername = properties.getMxUsername();
        String mxPassword = properties.getMxPassword();
        HTTPConnection httpConnection = new HTTPConnection(mxUsername, mxPassword);
        DataParse parser = new DataParse();

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
        final List<Advertiser> liveAdvertiserList = new ArrayList<Advertiser>();

        for(Advertiser ad : advertiserList) {
            if(ad.isLive()) {
                liveAdvertiserList.add(ad);
            }
        }

        httpConnection.authorizeAppNexusConnection(appNexusUsername, appNexusPassword);

        //For every advertiser, request report
        for (Advertiser advertiser : liveAdvertiserList) {

            //request yesterday line item report
            List<String[]> csvData = AppNexusReportRequests.getLineItemReport("yesterday", advertiser.getAdvertiserID(),
                    appNexusUrl, httpConnection);

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
            csvData = AppNexusReportRequests.getLineItemReport("lifetime", advertiser.getAdvertiserID(),
                    appNexusUrl, httpConnection);

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
            csvData = AppNexusReportRequests.getCampaignReport("yesterday", advertiser.getAdvertiserID(),
                    appNexusUrl, httpConnection);

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
            csvData = AppNexusReportRequests.getCampaignReport("lifetime", advertiser.getAdvertiserID(),
                    appNexusUrl, httpConnection);

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
                httpConnection.requestLineItemsFromMX(mxUrl, ad.getAdvertiserID());
                //save to a list of line items
                List<LineItem> lineItems = DataParse.populateLineItemList(httpConnection.getJSONData());
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
                    httpConnection.setUrl(mxUrl + "/api/catalog/advertisers/" + advertiser.getAdvertiserID() +
                            "/line-items/" + li.getLineItemID() + "/campaigns");
                    httpConnection.requestData(mxRequestProperties);
                    List<Campaign> campaigns = DataParse.populateCampaignList(httpConnection.getJSONData());
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
/*
        try {
            ObjectOutputStream out = new ObjectOutputStream
                    (new FileOutputStream("/Users/charronkyle/Desktop/AdvertiserList.ser"));
            out.writeObject(liveAdvertiserList);
            out.close();
        } catch (IOException e) {
            LOG.error("Serialization Failed!");
            LOG.error(e.toString());
        }
*/


        //Write report
        ReportWriter.writeAdvertiserDailyReport(liveAdvertiserList, outputPath);
    }
}