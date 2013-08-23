package com.mediacrossing.segment_targeting;

import java.io.*;
import java.util.ArrayList;
import com.google.gson.*;

/**
 * Created with IntelliJ IDEA.
 * User: charronkyle
 * Date: 8/20/13
 * Time: 12:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class FetchData {

    public static void main(String[] args) throws Exception {

        //Declare Variables
        JSONParse parser = new JSONParse();
        ArrayList<Campaign> campaignArrayList;
        ArrayList<String> advertiserIDList;
        HTTPRequest httpConnection = new HTTPRequest();
        CSVWriter csvWriter = new CSVWriter();

        //Get Token, Advertisers
        httpConnection.authorizeAppNexusConnection("MC_report", "13MediaCrossing!");
        httpConnection.requestAllAdvertisersFromAN();
        advertiserIDList = parser.populateAdvertiserIDList(httpConnection.getJSONData());

        //TODO setup mock api connection
        httpConnection.requestAllCampaignsFromMX();
        campaignArrayList = parser.getMockCampaignList(httpConnection.getJSONData());
/*
        //Get Campaigns for each Advertiser
        for(int x = 0; x < advertiserIDList.size(); x++) {

            String advertiserID = advertiserIDList.get(x);
            httpConnection.requestCampaignsByAdvertiserID(advertiserID);
            parser.populateCampaignList(httpConnection.getJSONData(), advertiserID);

        }
        campaignArrayList = parser.getCampaignArrayList();
*/

        //Get Profile data for each Campaign
        for(int y = 0; y < campaignArrayList.size(); y++) {

            String profileID = campaignArrayList.get(y).getProfileID();
            String advertiserID = campaignArrayList.get(y).getAdvertiserID();
            httpConnection.requestProfile(profileID, advertiserID);

            FrequencyTargets newFrequencyTarget = new FrequencyTargets();
            newFrequencyTarget = parser.populateFrequencyTarget(httpConnection.getJSONData());

            ArrayList<DaypartTarget> newDaypartTargetList = new ArrayList<DaypartTarget>();
            newDaypartTargetList = parser.populateDaypartTarget(httpConnection.getJSONData());

            GeographyTargets newGeographyTarget = new GeographyTargets();
            newGeographyTarget = parser.populateGeographyTarget(httpConnection.getJSONData());

            Campaign currentCampaign = campaignArrayList.get(y);
            currentCampaign.setFrequencyTargets(newFrequencyTarget);
            currentCampaign.setDaypartTargetArrayList(newDaypartTargetList);
            currentCampaign.setGeographyTargets(newGeographyTarget);

            campaignArrayList.set(y, currentCampaign);


        }

        //Convert Data to CSV files
        csvWriter.writeFrequencyFile(campaignArrayList);
        csvWriter.writeDaypartFile(campaignArrayList);
        csvWriter.writeGeographyFile(campaignArrayList);

    }
}
