package com.mediacrossing.anupload;

import com.mediacrossing.campaignbooks.Campaign;
import com.mediacrossing.campaignbooks.LineItem;
import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.properties.ConfigurationProperties;

import java.util.ArrayList;

public class RunCampaignUpload {
    public static String anUrl = "http://localhost:8080/an/campaign?advertiser_id=283120";
    public static String anUsername = "mediacrossing_api_user";
    public static String anPassword = "Z3^at0Fbr";
    public static String advertiserId = "283120";

    public static void main(String[] args) throws Exception {
        ConfigurationProperties properties = new ConfigurationProperties(args);

        //Get all campaign ids
        //For each campaign ID, upload pacing
        AppNexusService anConn = new AppNexusService(properties.getAppNexusUrl(), anUsername, anPassword, properties.getPartitionSize(), properties.getRequestDelayInSeconds());
        MxService mxConn = new MxService(properties.getMxUrl(), properties.getMxUsername(), properties.getMxPassword());
        ArrayList<LineItem> lines = mxConn.requestLineItemsForAdvertiser(advertiserId);
        for (LineItem l : lines) {
            l.setCampaignList(mxConn.requestCampaignsForLineItem(advertiserId, l.getLineItemID()));
        }
        String json = "{" +
                "\"campaign\":" +
                "{" +
                "\"enable_pacing\":true" +
                "}}";
        for (LineItem l : lines) {
            for (Campaign c : l.getCampaignList()) {
                anConn.putRequest("/campaign?id=" + c.getCampaignID() + "&advertiser_id=" + advertiserId, json);
            }
        }
    }
}
