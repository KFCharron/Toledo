package com.mediacrossing.report_requests;

import com.mediacrossing.campaignbooks.DataParse;
import com.mediacrossing.segmenttargeting.HTTPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;

public class AppNexusReportRequests {

    static DataParse dataParse = new DataParse();

    private static final Logger LOG = LoggerFactory.getLogger(AppNexusReportRequests.class);

    public static List<String[]> getCampaignImpsReport(String advertiserId,
                                                 String appNexusUrl,
                                                 HTTPConnection httpConnection) throws Exception {

        String reportId = httpConnection.requestCampaignImpsReport(advertiserId);
        boolean ready = false;
        while (!ready) {
            //Check to see if report is ready
            String jsonResponse = httpConnection.fetchDownloadUrl(reportId);
            LOG.debug(jsonResponse);
            ready = dataParse.parseReportStatus(jsonResponse);
            if (!ready)
                Thread.sleep(20000);
        }
        //Report is ready, download it
        String downloadUrl = appNexusUrl + dataParse.getReportUrl();
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
            LOG.debug(jsonResponse);
            ready = dataParse.parseReportStatus(jsonResponse);
            if (!ready)
                Thread.sleep(20000);
        }
        //Report is ready, download it
        String downloadUrl = appNexusUrl + dataParse.getReportUrl();
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
                "            \"line_item_id\",\n" +
                "            \"line_item_name\",\n" +
                "            \"imps\",\n" +
                "            \"clicks\",\n" +
                "            \"total_convs\",\n" +
                "            \"media_cost\",\n" +
                "            \"ctr\",\n" +
                "            \"conv_rate\",\n" +
                "            \"cpm\",\n" +
                "            \"cpc\"\n" +
                "        ],\n" +
                "        \"row_per\" :[\n" +
                "            \"line_item_id\"\n" +
                "        ],\n" +
                "        \"report_interval\": \"" + interval + "\",\n" +
                "        \"format\": \"csv\"\n" +
                "    }\n" +
                "}";
        String reportId = httpConnection.requestAdvertiserAnalyticReport(advertiserId, jsonPostData);
        boolean ready = false;
        while (!ready) {
            //Check to see if report is ready
            String jsonResponse = httpConnection.fetchDownloadUrl(reportId);
            LOG.debug(jsonResponse);
            ready = dataParse.parseReportStatus(jsonResponse);
            if (!ready)
                Thread.sleep(20000);
        }
        //Report is ready, download it
        String downloadUrl = appNexusUrl + dataParse.getReportUrl();
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
                "            \"campaign_id\",\n" +
                "            \"campaign_name\",\n" +
                "            \"imps\",\n" +
                "            \"clicks\",\n" +
                "            \"total_convs\",\n" +
                "            \"media_cost\",\n" +
                "            \"ctr\",\n" +
                "            \"conv_rate\",\n" +
                "            \"cpm\",\n" +
                "            \"cpc\"\n" +
                "        ],\n" +
                "        \"row_per\" :[\n" +
                "            \"campaign_id\"\n" +
                "        ],\n" +
                "        \"report_interval\": \"" + interval + "\",\n" +
                "        \"format\": \"csv\"\n" +
                "    }\n" +
                "}";
        String reportId = httpConnection.requestAdvertiserAnalyticReport(advertiserId, jsonPostData);
        boolean ready = false;
        while (!ready) {
            //Check to see if report is ready
            String jsonResponse = httpConnection.fetchDownloadUrl(reportId);
            LOG.debug(jsonResponse);
            ready = dataParse.parseReportStatus(jsonResponse);
            if (!ready)
                Thread.sleep(20000);
        }
        //Report is ready, download it
        String downloadUrl = appNexusUrl + dataParse.getReportUrl();
        httpConnection.requestDownload(downloadUrl);

        return httpConnection.getCsvData();
    }

    public static List<String[]> getAdvertiserAnalyticReport(String advertiserId,
                                                   String appNexusUrl,
                                                   HTTPConnection httpConnection) throws Exception {

        String jsonPostData = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\":\"network_advertiser_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"day\",\n" +
                "            \"campaign_id\",\n" +
                "            \"total_revenue\",\n" +
                "            \"imps\",\n" +
                "            \"clicks\",\n" +
                "            \"total_convs\"\n" +

                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"campaign_id\",\n" +
                "            \"day\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"lifetime\",\n" +
                "        \"format\":\"csv\",\n" +
                "        \"emails\":[\n" +
                "        ],\n" +
                "        \"orders\": [\n" +
                "                    {\n" +
                "                        \"order_by\":\"day\", \n" +
                "                        \"direction\":\"DESC\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"order_by\":\"campaign_id\",\n" +
                "                        \"direction\":\"DESC\"\n" +
                "                    }\n" +
                "                    ]\n" +
                "    }\n" +
                "}";
        String reportId = httpConnection.requestAdvertiserAnalyticReport(advertiserId, jsonPostData);
        boolean ready = false;
        while (!ready) {
            //Check to see if report is ready
            String jsonResponse = httpConnection.fetchDownloadUrl(reportId);
            LOG.debug(jsonResponse);
            ready = dataParse.parseReportStatus(jsonResponse);
            if (!ready)
                Thread.sleep(20000);
        }
        //Report is ready, download it
        String downloadUrl = appNexusUrl + dataParse.getReportUrl();
        httpConnection.requestDownload(downloadUrl);

        return httpConnection.getCsvData();
    }

    public static List<String[]> getSegmentLoadReport(HashSet segmentIdSet,
                                                      String appNexusUrl,
                                                      HTTPConnection httpConnection) throws Exception {


        //Build the report filter argument string
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        for(Object segmentId : segmentIdSet) {
            stringBuilder.append("\"").append(segmentId.toString()).append("\"");
            count++;
            if(count < segmentIdSet.size())
                stringBuilder.append(",");
        }
        String segmentJson = stringBuilder.toString();

        String jsonPostData = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"segment_load\",\n" +
                "        \"columns\": [\n" +
                "            \"segment_id\",\n" +
                "            \"segment_name\",\n" +
                "            \"day\",\n" +
                "            \"total_loads\",\n" +
                "            \"daily_uniques\"\n" +
                "        ],\n" +
                "        \"filters\": [\n" +
                "            {\n" +
                "               \"segment_id\": [" + segmentJson + "]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"groups\": [\n" +
                "            \"segment_id\",\n" +
                "            \"day\"\n" +
                "        ],\n" +
                "        \"orders\": [\n" +
                "            \"day\"\n" +
                "        ],\n" +
                "        \"emails\": [],\n" +
                "        \"format\": \"csv\"\n" +
                "    }\n" +
                "}";
        String reportId = httpConnection.requestSegmentLoadsReport(jsonPostData);
        boolean ready = false;
        while (!ready) {
            //Check to see if report is ready
            String jsonResponse = httpConnection.fetchDownloadUrl(reportId);
            LOG.debug(jsonResponse);
            ready = dataParse.parseReportStatus(jsonResponse);
            if (!ready)
                Thread.sleep(20000);
        }
        //Report is ready, download it
        String downloadUrl = appNexusUrl + dataParse.getReportUrl();
        httpConnection.requestDownload(downloadUrl);

        return httpConnection.getCsvData();
    }

    public static List<String[]> getLifetimeAdvertiserReport(String advertiserId,
                                                             String appNexusUrl,
                                                             HTTPConnection httpConnection) throws Exception {

        String jsonPostData = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\":\"advertiser_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"campaign_id\",\n" +
                "            \"imps\",\n" +
                "            \"clicks\",\n" +
                "            \"ctr\",\n" +
                "            \"total_convs\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"campaign_id\",\n" +
                "            \"day\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"lifetime\",\n" +
                "        \"format\":\"csv\",\n" +
                "        \"emails\":[\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        String reportId = httpConnection.requestAdvertiserAnalyticReport(advertiserId, jsonPostData);
        boolean ready = false;
        while (!ready) {
            //Check to see if report is ready
            String jsonResponse = httpConnection.fetchDownloadUrl(reportId);
            LOG.debug(jsonResponse);
            ready = dataParse.parseReportStatus(jsonResponse);
            if (!ready)
                Thread.sleep(20000);
        }
        //Report is ready, download it
        String downloadUrl = appNexusUrl + dataParse.getReportUrl();
        httpConnection.requestDownload(downloadUrl);

        return httpConnection.getCsvData();
    }

}

