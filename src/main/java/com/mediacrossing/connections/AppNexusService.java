package com.mediacrossing.connections;

import com.mediacrossing.campaignbooks.DataParse;
import com.mediacrossing.dailycheckupsreport.JSONParse;
import com.mediacrossing.monthlybillingreport.BillingAdvertiser;
import com.mediacrossing.publishercheckup.*;
import com.mediacrossing.publisherreporting.Publisher;
import com.mediacrossing.weeklypublisherreport.WeeklyPlacement;
import com.mediacrossing.weeklypublisherreport.WeeklyPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import scala.concurrent.duration.Duration;

import java.util.*;

public class AppNexusService {

    private static final Logger LOG = LoggerFactory.getLogger(AppNexusService.class);

    public HTTPRequest requests;
    private String url;
    private int partitionSize;
    private Duration delay;
    private int queryCount;

    public AppNexusService(String url, String username, String password, int anPartitionSize, Duration dur) throws Exception {
        String authJson = "{\"auth\":{\"username\":\"" + username +
                "\",\"password\":\"" + password + "\"}}";
        String tokenJson = new HTTPSRequest().postRequest("https://api.appnexus.com/auth", authJson);
        String token = JSONParse.obtainToken(tokenJson);
        Iterable<Tuple2<String, String>> requestProperties = Collections.unmodifiableList(
                Arrays.asList(ConnectionRequestProperties.authorization(token)));
        this.requests = new HTTPRequest(requestProperties);
        this.url = url;
        this.partitionSize = anPartitionSize;
        this.delay = dur;
        this.queryCount = 0;
    }

    public AppNexusService(String url, String username, String password) throws Exception {
        String authJson = "{\"auth\":{\"username\":\"" + username +
                "\",\"password\":\"" + password + "\"}}";
        String tokenJson = new HTTPSRequest().postRequest("https://api.appnexus.com/auth", authJson);
        String token = JSONParse.obtainToken(tokenJson);
        Iterable<Tuple2<String, String>> requestProperties = Collections.unmodifiableList(
                Arrays.asList(ConnectionRequestProperties.authorization(token)));
        this.requests = new HTTPRequest(requestProperties);
        this.url = url;
    }

    private void throttleCheck() throws InterruptedException {
        this.queryCount++;
        if(this.queryCount >= this.partitionSize) {
            Thread.sleep(delay.toMillis());
            this.queryCount = 0;
        }
    }

    public ArrayList<Publisher> requestPublishers() throws Exception {
        String json = requests.getRequest(url + "/publisher");
        throttleCheck();
        return DataParse.parsePublisherIdAndName(json);
    }

    public ArrayList<PublisherConfig> requestPublisherConfigs() throws Exception {
        ArrayList<Publisher> temp = requestPublishers();
        ArrayList<PublisherConfig> pubConfigs = new ArrayList<PublisherConfig>();
        for (Publisher p : temp) pubConfigs.add(new PublisherConfig(p.getId(), p.getPublisherName(), p.getLastModified()));
        return pubConfigs;
    }

    public ArrayList<WeeklyPublisher> requestWeeklyPublishers() throws Exception {
        ArrayList<Publisher> temp = requestPublishers();
        ArrayList<WeeklyPublisher> weeklyPubs = new ArrayList<WeeklyPublisher>();
        for (Publisher p : temp) weeklyPubs.add(new WeeklyPublisher(p.getId(), p.getPublisherName(), p.getStatus()));
        return weeklyPubs;
    }

    public ArrayList<Placement> requestPlacements(String pubId) throws Exception {
        String json = requests.getRequest(url+"/placement?publisher_id="+pubId);
        throttleCheck();
        return ResponseParser.parsePlacements(json);
    }

    public ArrayList<PaymentRule> requestPaymentRules(String pubId) throws Exception {
        String json = requests.getRequest(url+"/payment-rule?publisher_id=" + pubId);
        throttleCheck();
        return ResponseParser.parsePaymentRules(json);
    }

    public ArrayList<YMProfile> requestYmProfiles (String pubId) throws Exception {
        String json = requests.getRequest(url+"/ym-profile?publisher_id="+pubId);
        throttleCheck();
        return ResponseParser.parseYmProfiles(json);
    }

    public ArrayList<BillingAdvertiser> requestBillingReport () throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_analytics\",\n" +
                "        \"columns\": [\n" +
                "            \"advertiser_id\",\n" +
                "            \"advertiser_name\",\n" +
                "            \"campaign_id\",\n" +
                "            \"campaign_name\",\n" +
                "            \"imps\",\n" +
                "            \"clicks\",\n" +
                "            \"total_convs\",\n" +
                "            \"cost\",\n" +
                "            \"revenue\"\n" +
                "        ],\n" +
                "        \"row_per\" :[\n" +
                "            \"campaign_id\"\n" +
                "        ],\n" +
                "        \"report_interval\": \"last_month\",\n" +
                "        \"format\": \"csv\",\n" +
                "        \"emails\":[],\n" +
                "        \"orders\": [\n" +
                "            {\n" +
                "            \"order_by\" : \"campaign_id\",\n" +
                "            \"direction\": \"DESC\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"timezone\": \"EST5EDT\"\n" +
                "    }\n" +
                "}";

        String json = requests.postRequest(url+"/report", jsonPost);
        return ResponseParser.parseBillingReport(downloadReportWhenReady(json));
    }

    public List<String[]> requestSellerReport () throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_analytics\",\n" +
                "        \"columns\": [\n" +
                "            \"seller_member_id\",\n" +
                "            \"seller_member_name\",\n" +
                "            \"campaign_id\",\n" +
                "            \"imps\"\n" +
                "        ],\n" +
                "        \"row_per\" :[\n" +
                "            \"campaign_id\",\n" +
                "            \"seller_member_id\"\n" +
                "        ],\n" +
                "        \"report_interval\": \"last_month\",\n" +
                "        \"format\": \"csv\",\n" +
                "        \"emails\":[],\n" +
                "        \"orders\": [\n" +
                "            {\n" +
                "            \"order_by\" : \"seller_member_id\",\n" +
                "            \"direction\": \"DESC\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"timezone\": \"EST5EDT\"\n" +
                "    }\n" +
                "}";

        String json = requests.postRequest(url+"/report", jsonPost);
        return downloadReportWhenReady(json);
    }

    public ArrayList<WeeklyPlacement> requestWeeklyPublisherReport (String pubId) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\":\"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"day\",\n" +
                "            \"placement_id\",\n" +
                "            \"placement_name\",\n" +
                "            \"imps_total\",\n" +
                "            \"imps_kept\",\n" +
                "            \"imps_resold\",\n" +
                "            \"imps_default\",\n" +
                "            \"imps_psa\",\n" +
                "            \"imps_blank\",\n" +
                "            \"imps_psa_error\",\n" +
                "            \"imps_default_error\",\n" +
                "            \"imps_default_bidder\",\n" +
                "            \"publisher_rpm\",\n" +
                "            \"publisher_revenue\",\n" +
                "            \"network_revenue\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"day\",\n" +
                "            \"placement_id\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"last_7_days\",\n" +
                "        \"format\":\"csv\",\n" +
                "        \"emails\":[\n" +
                "        ],\n" +
                "        \"orders\": [\n" +
                "                    {\n" +
                "                        \"order_by\":\"day\", \n" +
                "                        \"direction\":\"ASC\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"order_by\":\"placement_id\", \n" +
                "                        \"direction\":\"DESC\"\n" +
                "                    }\n" +
                "                    ],\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";

        String json = requests.postRequest(url+"/report?publisher_id="+pubId, jsonPost);
        return ResponseParser.parsePlacementReport(downloadReportWhenReady(json));
    }

    public ArrayList<WeeklyPlacement> requestMonthlyPublisherReport (String pubId) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\":\"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"day\",\n" +
                "            \"placement_id\",\n" +
                "            \"placement_name\",\n" +
                "            \"imps_total\",\n" +
                "            \"imps_kept\",\n" +
                "            \"imps_resold\",\n" +
                "            \"imps_default\",\n" +
                "            \"imps_psa\",\n" +
                "            \"imps_blank\",\n" +
                "            \"imps_psa_error\",\n" +
                "            \"imps_default_error\",\n" +
                "            \"imps_default_bidder\",\n" +
                "            \"publisher_rpm\",\n" +
                "            \"publisher_revenue\",\n" +
                "            \"network_revenue\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"day\",\n" +
                "            \"placement_id\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"last_month\",\n" +
                "        \"format\":\"csv\",\n" +
                "        \"emails\":[\n" +
                "        ],\n" +
                "        \"orders\": [\n" +
                "                    {\n" +
                "                        \"order_by\":\"day\", \n" +
                "                        \"direction\":\"ASC\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"order_by\":\"placement_id\", \n" +
                "                        \"direction\":\"DESC\"\n" +
                "                    }\n" +
                "                    ],\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";

        String json = requests.postRequest(url+"/report?publisher_id="+pubId, jsonPost);
        return ResponseParser.parsePlacementReport(downloadReportWhenReady(json));
    }

    public ArrayList<String> requestTopBuyerReport (String pubId) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"buyer_member_name\",\n" +
                "            \"network_revenue\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"buyer_member_name\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"last_month\",\n" +
                "        \"format\":\"csv\",\n" +
                "        \"emails\":[\n" +
                "        ],\n" +
                "        \"orders\": [\n" +
                "                    {\n" +
                "                        \"order_by\":\"network_revenue\", \n" +
                "                        \"direction\":\"DESC\"\n" +
                "                    }\n" +
                "                    ],\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";
        String json = requests.postRequest(url+"/report?publisher_id="+pubId, jsonPost);
        return ResponseParser.parseTopBrandsOrBuyers(downloadReportWhenReady(json));

    }

    public ArrayList<String> requestTopBrandReport (String pubId) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"brand_name\",\n" +
                "            \"network_revenue\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"brand_name\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"last_month\",\n" +
                "        \"format\":\"csv\",\n" +
                "        \"emails\":[\n" +
                "        ],\n" +
                "        \"orders\": [\n" +
                "                    {\n" +
                "                        \"order_by\":\"network_revenue\", \n" +
                "                        \"direction\":\"DESC\"\n" +
                "                    }\n" +
                "                    ],\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";
        String json = requests.postRequest(url+"/report?publisher_id="+pubId, jsonPost);
        return ResponseParser.parseTopBrandsOrBuyers(downloadReportWhenReady(json));

    }

    public List<String[]> getPublisherReport(String interval, String pubId) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\":\"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"publisher_id\",\n" +
                "            \"imps_total\",\n" +
                "            \"imps_sold\",\n" +
                "            \"clicks\",\n" +
                "            \"imps_rtb\",\n" +
                "            \"imps_kept\",\n" +
                "            \"imps_default\",\n" +
                "            \"imps_psa\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"publisher_id\"\n" +
                "        ],\n" +
                "        \"report_interval\":\""+ interval +"\",\n" +
                "        \"format\":\"csv\",\n" +
                "        \"emails\":[\n" +
                "        ],\n" +
                "        \"orders\": [\n" +
                "                    {\n" +
                "                        \"order_by\":\"publisher_id\", \n" +
                "                        \"direction\":\"DESC\"\n" +
                "                    }\n" +
                "                    ],\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";

        String json = requests.postRequest(url+"/report?publisher_id="+pubId, jsonPost);
        return downloadReportWhenReady(json);
    }

    public List<String[]> getPlacementReport(String interval, String pubId) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "\t\t\"placement_id\",\n" +
                "\t\t\"placement_name\",\n" +
                "\t\t\"site_id\",\n" +
                "\t\t\"site_name\",\n" +
                "\t\t\"imps_total\",\n" +
                "\t\t\"imps_sold\",\n" +
                "\t\t\"clicks\",\n" +
                "            \t \"imps_rtb\",\n" +
                "            \t \"imps_kept\",\n" +
                "            \t \"imps_default\",\n" +
                "            \t \"imps_psa\",\n" +
                "\t\t\"network_revenue\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"placement_id\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"" + interval + "\",\n" +
                "        \"orders\": [\n" +
                "                    {\n" +
                "                        \"order_by\":\"placement_id\", \n" +
                "                        \"direction\":\"DESC\"\n" +
                "                    }\n" +
                "                    ],\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";

        String json = requests.postRequest(url+"/report?publisher_id="+pubId, jsonPost);
        return downloadReportWhenReady(json);

    }

    public List<String[]> getConversionReport(String adId) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"attributed_conversions\",\n" +
                "        \"columns\": [\n" +
                "            \"line_item\",\n" +
                "            \"campaign\",\n" +
                "            \"order_id\",\n" +
                "            \"user_id\",\n" +
                "            \"post_click_or_post_view_conv\",\n" +
                "      \"creative\",\n" +
                "      \"auction_id\",\n" +
                "      \"external_data\",\n" +
                "      \"imp_time\",\n" +
                "      \"datetime\",\n" +
                "      \"pixel_id\",\n" +
                "      \"pixel_name\",\n" +
                "      \"imp_type\",\n" +
                "      \"post_click_or_post_view_revenue\"\n" +
                "        ],\n" +
                "        \"filters\": [\n" +
                "        ],\n" +
                "        \"groups\": [\n" +
                "        ],\n" +
                "        \"orders\": [\n" +
                "           {"+
                "            \"order_by\":\"datetime\", \"direction\":\"DESC\" } \n" +
                "        ],\n" +
                "       \"report_interval\": \"last_7_days\"," +
                "        \"emails\": [],\n" +
                "        \"format\": \"csv\",\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";;

        String json = requests.postRequest(url+"/report?advertiser_id="+adId,jsonPost);
        return downloadReportWhenReady(json);
    }

    public List<String[]> getAdvertiserAnalyticReport(String adId) throws Exception {

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
                "            \"total_convs\",\n" +
                "            \"ctr\"\n" +
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
                "                    ],\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";
        String json = requests.postRequest(url + "/report?advertiser_id=" + adId, jsonPostData);

        return downloadReportWhenReady(json);
    }

    public List<String[]> getLifetimeAdvertiserReport(String adId) throws Exception {

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
                "        ],\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";
        String json = requests.postRequest(url + "/report?advertiser_id=" + adId, jsonPostData);
        return downloadReportWhenReady(json);
    }

    public List<String[]> getCampaignImpsReport(String adId) throws Exception {

        String post = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\":\"advertiser_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"campaign_id\",\n" +
                "            \"imps\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"campaign_id\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"yesterday\",\n" +
                "        \"format\":\"csv\",\n" +
                "        \"emails\":[\n" +
                "        ],\n" +
                "        \"orders\": [\n" +
                "                    {\n" +
                "                        \"order_by\":\"campaign_id\",\n" +
                "                        \"direction\":\"DESC\"\n" +
                "                    }\n" +
                "                    ],\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";

        String json = requests.postRequest(url + "/report?advertiser_id=" + adId, post);

        return downloadReportWhenReady(json);

    }

    public List<String[]> getSegmentLoadReport(HashSet segIdSet) throws Exception {

        //Build the report filter argument string
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        for(Object segmentId : segIdSet) {
            stringBuilder.append("\"").append(segmentId.toString()).append("\"");
            count++;
            if(count < segIdSet.size())
                stringBuilder.append(",");
        }
        String segmentJson = stringBuilder.toString();

        String post = "{\n" +
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
                "        \"format\": \"csv\",\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";

        String json = requests.postRequest(url + "/report", post);

        return downloadReportWhenReady(json);
    }

    public List<String[]> getLineItemReport(String interval, String adId) throws Exception {

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
                "        \"format\": \"csv\",\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";
        String json = requests.postRequest(url + "/report?advertiser_id=" + adId, jsonPostData);

        return downloadReportWhenReady(json);
    }

    public List<String[]> getCampaignReport(String interval, String adId) throws Exception {

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
                "        \"format\": \"csv\",\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";

        String json = requests.postRequest(url + "/report?advertiser_id=" + adId, jsonPostData);
        return downloadReportWhenReady(json);
    }

    private List<String[]> downloadReportWhenReady(String json) throws Exception {

        String reportId = JSONParse.obtainReportId(json);
        boolean ready = false;
        while (!ready) {
            //Check to see if report is ready
            String jsonResponse = requests.getRequest(url + "/report?id=" + reportId);
            LOG.debug(jsonResponse);
            ready = DataParse.parseReportStatus(jsonResponse);
            if (!ready)
                Thread.sleep(20000);
        }
        //Report is ready, download it
        String downloadUrl = url + DataParse.getReportUrl();

        return requests.reportRequest(downloadUrl);
    }

}
