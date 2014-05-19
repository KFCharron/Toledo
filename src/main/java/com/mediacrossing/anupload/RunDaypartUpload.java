package com.mediacrossing.anupload;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.properties.ConfigurationProperties;

import java.util.ArrayList;

public class RunDaypartUpload {

    public static String anUrl = "http://localhost:8888/an/";
    public static String anUsername = "mediacrossing_api_user";
    public static String anPassword = "Z3^at0Fbr";
    public static String advertiserId = "206084";

    public static void main(String[] args) throws Exception {
//        ConfigurationProperties properties = new ConfigurationProperties(args);

        //Get all profile IDs for homeserve
        //For each profile ID, upload target
        //AppNexusService anConn = new AppNexusService(properties.getAppNexusUrl(), anUsername, anPassword, properties.getPartitionSize(), properties.getRequestDelayInSeconds());
        MxService mxConn = new MxService(anUrl);
        //ArrayList<String> profileIds = anConn.requestAllProfilesForAdvertiser(advertiserId);
        String[] proIds = new String[] {"9649324",
                "9649323",
                "9649321",
                "9649320",
                "9649319",
                "9649349",
                "9649347",
                "9649351",
                "9649353",
                "9649313"};
//        String json = "{" +
//                        "\"profile\":" +
//                        "{" +
//                            "\"daypart_targets\":[" +
//                                "{" +
//                                    "\"day\":\"all\"," +
//                                    "\"start_hour\":\"4\"," +
//                                    "\"end_hour\":\"23\"" +
//                                "}" +
//                            "]" +
//                        "}}";

//        String json = "{\n" +
//                "  \"profile\" : {\n" +
//                "    \"segment_group_targets\" : [\n" +
//                "      {\n" +
//                "        \"boolean_operator\" : \"or\",\n" +
//                "        \"segments\": [\n" +
//                "          {\n" +
//                "            \"id\": 402144,\n" +
//                "            \"action\": \"include\"\n" +
//                "          },\n" +
//                "          {\n" +
//                "            \"id\": 402143,\n" +
//                "            \"action\": \"include\"\n" +
//                "          }\n" +
//                "        ]\n" +
//                "      }\n" +
//                "    ]\n" +
//                "  }\n" +
//                "}";

        String json = "{\n" +
                "  \"profile\" : {\n" +
                "    \"dma_action\" : \"include\" ,\n" +
                "    \"dma_targets\" : [\n" +
                "      {\"dma\" : 501},\n" +
                "      {\"dma\" : 803},\n" +
                "      {\"dma\" : 807},\n" +
                "      {\"dma\" : 825},\n" +
                "      {\"dma\" : 641},\n" +
                "      {\"dma\" : 790},\n" +
                "      {\"dma\" : 602},\n" +
                "      {\"dma\" : 819},\n" +
                "      {\"dma\" : 623},\n" +
                "      {\"dma\" : 618},\n" +
                "      {\"dma\" : 506},\n" +
                "      {\"dma\" : 504},\n" +
                "      {\"dma\" : 524},\n" +
                "      {\"dma\" : 511},\n" +
                "      {\"dma\" : 512},\n" +
                "      {\"dma\" : 659},\n" +
                "      {\"dma\" : 622},\n" +
                "      {\"dma\" : 517},\n" +
                "      {\"dma\" : 560},\n" +
                "      {\"dma\" : 753},\n" +
                "      {\"dma\" : 528},\n" +
                "      {\"dma\" : 548},\n" +
                "      {\"dma\" : 539},\n" +
                "      {\"dma\" : 534},\n" +
                "      {\"dma\" : 751},\n" +
                "      {\"dma\" : 609}\n" +
                "    ]\n" +
                "  }}";

        //mxConn.putRequest("profile?id=9633086&advertiser_id="+advertiserId, json);

        // TODO Change Back when ready
        for (String pro : proIds) {
            mxConn.putRequest("profile?id=" + pro + "&advertiser_id=" + advertiserId, json);
        }
    }
}
