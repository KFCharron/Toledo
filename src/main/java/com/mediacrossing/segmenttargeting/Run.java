package com.mediacrossing.segmenttargeting;

import java.util.ArrayList;

public class Run {

    public static void main(String[] args) throws Exception {

        //Declare Variables
        JSONParse parser = new JSONParse();
        ArrayList<Campaign> campaignArrayList;
        ArrayList<String> advertiserIDList;
        HTTPRequest httpConnection = new HTTPRequest();
        CSVWriter csvWriter = new CSVWriter();
        DataStore dataStore = new DataStore();

        //Get Token
        httpConnection.authorizeAppNexusConnection("MC_report", "13MediaCrossing!");

        //Get All Campaigns from MX, save them into list
        httpConnection.requestAllCampaignsFromMX();
        dataStore.setCampaignArrayList(parser.populateCampaignArrayList(httpConnection.getJSONData()));

        //Get Profile data for each Campaign, save campaign
        ArrayList<Campaign> newCampaignArrayList = dataStore.getCampaignArrayList();
        for(int y = 0; y < newCampaignArrayList.size(); y++) {

            String profileID = newCampaignArrayList.get(y).getProfileID();
            String advertiserID = newCampaignArrayList.get(y).getAdvertiserID();
            httpConnection.requestProfile(profileID, advertiserID);

            FrequencyTargets newFrequencyTarget = new FrequencyTargets();
            newFrequencyTarget = parser.populateFrequencyTarget(httpConnection.getJSONData());

            ArrayList<DaypartTarget> newDaypartTargetList = new ArrayList<DaypartTarget>();
            newDaypartTargetList = parser.populateDaypartTarget(httpConnection.getJSONData());

            GeographyTargets newGeographyTarget = new GeographyTargets();
            newGeographyTarget = parser.populateGeographyTarget(httpConnection.getJSONData());

            ArrayList<SegmentGroupTarget> newSegmentGroupTargetList = new ArrayList<SegmentGroupTarget>();
            newSegmentGroupTargetList = parser.populateSegmentGroupTargetList(httpConnection.getJSONData());

            Campaign currentCampaign = newCampaignArrayList.get(y);
            currentCampaign.setFrequencyTargets(newFrequencyTarget);
            currentCampaign.setDaypartTargetArrayList(newDaypartTargetList);
            currentCampaign.setGeographyTargets(newGeographyTarget);
            currentCampaign.setSegmentGroupTargetList(newSegmentGroupTargetList);

            newCampaignArrayList.set(y, currentCampaign);


        }
        dataStore.setCampaignArrayList(newCampaignArrayList);



        //Convert Data to CSV files
        csvWriter.writeFrequencyFile(dataStore.getCampaignArrayList());
        csvWriter.writeDaypartFile(dataStore.getCampaignArrayList());
        csvWriter.writeGeographyFile(dataStore.getCampaignArrayList());
        csvWriter.writeSegmentFIle(dataStore.getCampaignArrayList());

    }
}
