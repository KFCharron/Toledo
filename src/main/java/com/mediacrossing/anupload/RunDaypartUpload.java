package com.mediacrossing.anupload;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.properties.ConfigurationProperties;

import java.util.ArrayList;

public class RunDaypartUpload {

    public static String anUrl = "http://localhost:8080/an/profile?advertiser_id=283120";
    public static String anUsername = "mediacrossing_api_user";
    public static String anPassword = "Z3^at0Fbr";
    public static String advertiserId = "283120";

    public static void main(String[] args) throws Exception {
        ConfigurationProperties properties = new ConfigurationProperties(args);

        //Get all profile IDs for homeserve
        //For each profile ID, upload target
        AppNexusService anConn = new AppNexusService(properties.getAppNexusUrl(), anUsername, anPassword, properties.getPartitionSize(), properties.getRequestDelayInSeconds());
        ArrayList<String> profileIds = anConn.requestAllProfilesForAdvertiser(advertiserId);
        String json = "{" +
                        "\"profile\":" +
                        "{" +
                            "\"daypart_targets\":[" +
                                "{" +
                                    "\"day\":\"all\"," +
                                    "\"start_hour\":\"6\"," +
                                    "\"end_hour\":\"22\"" +
                                "}" +
                            "]" +
                        "}}";
        for (String pro : profileIds) {
            anConn.putRequest("/profile?id=" + pro + "&advertiser_id=" + advertiserId, json);
        }
    }
}
