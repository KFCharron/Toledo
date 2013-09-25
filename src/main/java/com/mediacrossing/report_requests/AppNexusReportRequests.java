package com.mediacrossing.report_requests;

import com.mediacrossing.campaignbooks.DataParse;
import com.mediacrossing.connections.ConnectionRequestProperties;
import com.mediacrossing.segmenttargeting.HTTPConnection;
import java.util.HashSet;
import java.util.List;

public class AppNexusReportRequests {

    static DataParse dataParse = new DataParse();

    public static List<String[]> getSegmentLoadReport(HashSet segmentIdSet,
                                                      String appNexusUrl,
                                                      HTTPConnection httpConnection) throws Exception {

        //Build the report filter argument string
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        for(Object segmentId : segmentIdSet) {
            stringBuilder.append("\""+ segmentId.toString() + "\"");
            count++;
            if(count < segmentIdSet.size())
                stringBuilder.append(",");
        }

        //request report
        String reportId = httpConnection.requestSegmentLoadReport(stringBuilder.toString());

        //Keep checking if report is ready
        boolean ready = false;
        while (!ready) {
            String jsonResponse = httpConnection.fetchDownloadUrl(reportId);

            ready = dataParse.parseReportStatus(jsonResponse);
            if (!ready)
                Thread.sleep(20000);
        }

        //Report is ready, download it
        String downloadUrl = appNexusUrl + "/" + dataParse.getReportUrl();
        httpConnection.requestDownload(downloadUrl);

        //return the report
        return httpConnection.getCsvData();
    }

    public static List<String[]> getAdvertiserAnalyticReport(String advertiserId,
                                                             String appNexusUrl,
                                                             HTTPConnection httpConnection) throws Exception {
        String reportId = httpConnection.requestAdvertiserReport(advertiserId);
        boolean ready = false;
        while (!ready) {
            //Check to see if report is ready
            String jsonResponse = httpConnection.fetchDownloadUrl(reportId);
            System.out.println(jsonResponse);
            ready = dataParse.parseReportStatus(jsonResponse);
            if (!ready)
                Thread.sleep(20000);
        }
        //Report is ready, download it
        String downloadUrl = appNexusUrl + "/" + dataParse.getReportUrl();
        httpConnection.requestDownload(downloadUrl);

        return httpConnection.getCsvData();
    }

    public static List<String[]> getCampaignImpsReport(String advertiserId,
                                                 String appNexusUrl,
                                                 HTTPConnection httpConnection) throws Exception {
        String reportId = httpConnection.requestCampaignImpsReport(advertiserId);
        boolean ready = false;
        while (!ready) {
            //Check to see if report is ready
            String jsonResponse = httpConnection.fetchDownloadUrl(reportId);
            System.out.println(jsonResponse);
            ready = dataParse.parseReportStatus(jsonResponse);
            if (!ready)
                Thread.sleep(20000);
        }
        //Report is ready, download it
        String downloadUrl = appNexusUrl + "/" + dataParse.getReportUrl();
        httpConnection.requestDownload(downloadUrl);

        return httpConnection.getCsvData();

    }

    public static List<String[]> getPublisherReport(String publisherId,
                                                       String appNexusUrl,
                                                       HTTPConnection httpConnection) throws Exception {
        String reportId = httpConnection.requestPublisherReport(publisherId);
        boolean ready = false;
        while (!ready) {
            //Check to see if report is ready
            String jsonResponse = httpConnection.fetchDownloadUrl(reportId);
            System.out.println(jsonResponse);
            ready = dataParse.parseReportStatus(jsonResponse);
            if (!ready)
                Thread.sleep(20000);
        }
        //Report is ready, download it
        String downloadUrl = appNexusUrl + "/" + dataParse.getReportUrl();
        httpConnection.requestDownload(downloadUrl);

        return httpConnection.getCsvData();

    }

    public static List<String[]> getLineItemReport(String interval, String advertiserId,
                                                             String appNexusUrl,
                                                             HTTPConnection httpConnection) throws Exception {
        String jsonPostData = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"advertiser_analytics\",\n" +
                "        \"columns\": [\n" +
                "            \"line_item_id\"\n" +
                "            \"imps\",\n" +
                "            \"clicks\",\n" +
                "            \"total-convs\",\n" +
                "            \"media_cost\",\n" +
                "            \"ctr\",\n" +
                "            \"conv_rate\",\n" +
                "            \"cpm\",\n" +
                "            \"cpc\",\n" +
                "        ],\n" +
                "        \"row_per\" :[\n" +
                "            \"line_item_id\"\n" +
                "        ],\n" +
                "        \"report_interval\": \"" + interval + "\",\n" +
                "        \"format\": \"csv\"\n" +
                "    }\n" +
                "}";
        String reportId = httpConnection.requestReport(advertiserId, jsonPostData);
        boolean ready = false;
        while (!ready) {
            //Check to see if report is ready
            String jsonResponse = httpConnection.fetchDownloadUrl(reportId);
            System.out.println(jsonResponse);
            ready = dataParse.parseReportStatus(jsonResponse);
            if (!ready)
                Thread.sleep(20000);
        }
        //Report is ready, download it
        String downloadUrl = appNexusUrl + "/" + dataParse.getReportUrl();
        httpConnection.requestDownload(downloadUrl);

        return httpConnection.getCsvData();
    }

    public static List<String[]> getCampaignReport(String interval, String advertiserId,
                                                            String appNexusUrl,
                                                            HTTPConnection httpConnection) throws Exception {
        String jsonPostData = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"advertiser_analytics\",\n" +
                "        \"columns\": [\n" +
                "            \"campaign_id\"\n" +
                "            \"imps\",\n" +
                "            \"clicks\",\n" +
                "            \"total-convs\",\n" +
                "            \"media_cost\",\n" +
                "            \"ctr\",\n" +
                "            \"conv_rate\",\n" +
                "            \"cpm\",\n" +
                "            \"cpc\",\n" +
                "        ],\n" +
                "        \"row_per\" :[\n" +
                "            \"campaign_id\"\n" +
                "        ],\n" +
                "        \"report_interval\": \"" + interval + "\",\n" +
                "        \"format\": \"csv\"\n" +
                "    }\n" +
                "}";
        String reportId = httpConnection.requestReport(advertiserId, jsonPostData);
        boolean ready = false;
        while (!ready) {
            //Check to see if report is ready
            String jsonResponse = httpConnection.fetchDownloadUrl(reportId);
            System.out.println(jsonResponse);
            ready = dataParse.parseReportStatus(jsonResponse);
            if (!ready)
                Thread.sleep(20000);
        }
        //Report is ready, download it
        String downloadUrl = appNexusUrl + "/" + dataParse.getReportUrl();
        httpConnection.requestDownload(downloadUrl);

        return httpConnection.getCsvData();
    }
}

