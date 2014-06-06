package com.mediacrossing.anupload;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.properties.ConfigurationProperties;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class RunDaypartUpload {

    public static String anUrl = "http://localhost:8888/an/";
    public static String anUsername = "mediacrossing_api_user";
    public static String anPassword = "Z3^at0Fbr";
    public static String advertiserId = "169351";

    public static void main(String[] args) throws Exception {
//        ConfigurationProperties properties = new ConfigurationProperties(args);

        //Get all profile IDs for homeserve
        //For each profile ID, upload target
        //AppNexusService anConn = new AppNexusService(properties.getPutneyUrl(), anUsername, anPassword, properties.getPartitionSize(), properties.getRequestDelayInSeconds());
        MxService mxConn = new MxService(anUrl);
        //ArrayList<String> profileIds = anConn.requestAllProfilesForAdvertiser(advertiserId);
        String[] proIds = new String[] {
                "9165571",
                "9165616",
                "9165741",
                "9165917",
                "9166116",
                "9166287",
                "9166428",
                "9166549",
                "9166979",
                "9167196",
                "9167559",
                "9168541",
                "9173844",
                "9173845",
                "9187264",
                "9190587",
                "9191248",
                "9442324",
                "9616557",
                "9723762",
                "9723763",
                "9746254",
                "9750241",
                "9926563",
                "9937633"
        };
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
                "    \"dma_action\" : \"exclude\" ,\n" +
                "    \"dma_targets\" : [\n";

//        BufferedReader in = new BufferedReader(new FileReader("/Users/charronkyle/Desktop/Chambers_GEOs_ToBeRemoved060514.csv"));
//
//        String inputLine;
//        StringBuilder response = new StringBuilder();
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine);
//        }
//        in.close();
        CSVReader reader = new CSVReader(new FileReader("/Users/charronkyle/Desktop/Chambers_GEOs_ToBeRemoved060514.csv"));
        List<String[]> result = reader.readAll();

//        List<String[]> result = new ArrayList<>();
//        String[] nextLine;
//        while ((nextLine = reader.readNext()) != null) {
//            result.add(nextLine);
//        }

        for (String[] line : result) {
            String number = line[0].substring(line[0].length()-4, line[0].length()-1);
            int id = Integer.parseInt(number);
            String addedDma = "{\"dma\" : " + id + "},";
            json = json + addedDma;
        }

        json = json.substring(0, json.length()-1) + "]}}";





//                String json = "{\n" +
//                "  \"profile\" : {\n" +
//                "    \"dma_action\" : \"include\" ,\n" +
//                "    \"dma_targets\" : [\n" +
//                "      {\"dma\" : 501},\n" +
//                "      {\"dma\" : 803},\n" +
//                "      {\"dma\" : 807},\n" +
//                "      {\"dma\" : 825},\n" +
//                "      {\"dma\" : 641},\n" +
//                "      {\"dma\" : 790},\n" +
//                "      {\"dma\" : 602},\n" +
//                "      {\"dma\" : 819},\n" +
//                "      {\"dma\" : 623},\n" +
//                "      {\"dma\" : 618},\n" +
//                "      {\"dma\" : 506},\n" +
//                "      {\"dma\" : 504},\n" +
//                "      {\"dma\" : 524},\n" +
//                "      {\"dma\" : 511},\n" +
//                "      {\"dma\" : 512},\n" +
//                "      {\"dma\" : 659},\n" +
//                "      {\"dma\" : 622},\n" +
//                "      {\"dma\" : 517},\n" +
//                "      {\"dma\" : 560},\n" +
//                "      {\"dma\" : 753},\n" +
//                "      {\"dma\" : 528},\n" +
//                "      {\"dma\" : 548},\n" +
//                "      {\"dma\" : 539},\n" +
//                "      {\"dma\" : 534},\n" +
//                "      {\"dma\" : 751},\n" +
//                "      {\"dma\" : 609}\n" +
//                "    ]\n" +
//                "  }}";

        //mxConn.putRequest("profile?id=9633086&advertiser_id="+advertiserId, json);

        // TODO Change Back when ready
        for (String pro : proIds) {
            mxConn.putRequest("profile?id=" + pro + "&advertiser_id=" + advertiserId, json);
        }
    }
}
