package com.mediacrossing.connections;

import com.mediacrossing.campaignbooks.Campaign;
import com.mediacrossing.campaignbooks.LineItem;
import com.mediacrossing.campaignbooks.DataParse;
import com.mediacrossing.dailycheckupsreport.JSONParse;
import com.mediacrossing.discrepancyreport.Creative;
import com.mediacrossing.monthlybillingreport.BillingAdvertiser;
import com.mediacrossing.monthlybillingreport.ImpType;
import com.mediacrossing.publishercheckup.*;
import com.mediacrossing.publisherreporting.Publisher;
import com.mediacrossing.weeklydomainreport.Domain;
import org.joda.time.DateTime;
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

    public String putRequest(String url2, String json) throws Exception {
        throttleCheck();
        return requests.putRequest(url + url2, json);
    }

    public ArrayList<Publisher> requestPublishers() throws Exception {
        String json = requests.getRequest(url + "/publisher");
        throttleCheck();
        return DataParse.parsePublisherIdAndName(json);
    }

    public ArrayList<String> requestAllProfilesForAdvertiser(String adId) throws Exception {
        // FIXME This returns Line Item Profiles that you don't want to make changes to.
        return null;
    }

    public ArrayList<LineItem> requestLineItems(String adId) throws Exception {
        String json = requests.getRequest(url + "/line-item?advertiser_id=" + adId);
        throttleCheck();
        return DataParse.parseLineItems(json);
    }

    public ArrayList<Campaign> requestCampaigns(String adId, String liId) throws Exception {
        String json = requests.getRequest(url + "/campaign?advertiser_id=" + adId);
        throttleCheck();
        return DataParse.parseCampaigns(json, liId);
    }

    public boolean checkForSegment(String code) throws Exception {
        String json = requests.getRequest(url + "/segment?code=" + code);
        throttleCheck();
        return DataParse.parseSegment(json);
    }

    public ArrayList<PublisherConfig> requestPublisherConfigs() throws Exception {
        ArrayList<Publisher> temp = requestPublishers();
        ArrayList<PublisherConfig> pubConfigs = new ArrayList<>();
        for (Publisher p : temp)
            if (p.getStatus().equals("active"))
                pubConfigs.add(new PublisherConfig(p.getId(), p.getPublisherName(), p.getLastModified()));
        return pubConfigs;
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

    public ArrayList<Creative> getActiveDfaCreative (String adId) throws Exception {
        String adUrl = "?advertiser_id=" + adId;
//        for (int x = 0; x < adIds.length; x++) {
//            adUrl = adUrl.concat(adIds[x]);
//            if (!(x+1 >= adIds.length)) adUrl = adUrl.concat(",");
//        }
        String json = requests.getRequest(url + "/creative" + adUrl + "&state=active");
        return ResponseParser.parseCreatives(json);
    }

    public List<String[]> requestPacingReport(DateTime earliest) throws Exception {
        System.out.println(earliest.toString("YYYY-MM-dd"));
        String jsonPost = "{\n" +
                "  \"report\": {\n" +
                "    \"report_type\": \"network_analytics\",\n" +
                "    \"columns\" : [\n" +
                "      \"advertiser_name\",\n" +
                "      \"line_item_name\",\n" +
                "      \"day\",\n" +
                "      \"imps\"\n" +
                "    ],\n" +
                "    \"timezone\": \"EST\",\n" +
                "    \"start_date\": \"" + earliest.toString("YYYY-MM-dd") + "\"," +
                "    \"end_date\": \"" + new DateTime().toString("YYYY-MM-dd") + "\"" +
                "  }\n" +
                "}";
        String json = requests.postRequest(url+"/report", jsonPost);
        return downloadReportWhenReady(json);
    }

    public List<String[]> requestClientPublisherReport(String pubId) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"placement_id\",\n" +
                "            \"placement_name\",\n" +
                "            \"imps_total\",\n" +
                "            \"imps_sold\",\n" +
                "            \"publisher_revenue\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"placement_name\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"yesterday\",\n" +
                "        \"timezone\":\"EST\"\n" +
                "    }\n" +
                "}";
        String json = requests.postRequest(url+"/report?publisher_id=" + pubId, jsonPost);
        return downloadReportWhenReady(json);
    }

    public List<String[]> requestDfaCreativeReport(String interval) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_billing\",\n" +
                "        \"columns\":[\n" +
                "            \"creative_id\",\n" +
                "            \"imps\",\n" +
                "            \"clicks\",\n" +
                "            \"convs\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"creative_id\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"" + interval + "\",\n" +
                "        \"timezone\":\"EST\"\n" +
                "    }\n" +
                "}";
        String json = requests.postRequest(url+"/report", jsonPost);
        return downloadReportWhenReady(json);
    }

    public List<String[]> requestAcctReport() throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\":\"network_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"line_item_id\",\n" +
                "            \"month\",\n" +
                "            \"imps\"\n" +
                "        ],\n" +
                "        \"row_per\" : [\n" +
                "            \"month\",\n" +
                "            \"line_item_id\"\n" +
                "        ],\n" +
                "        \"orders\" : [{\"order_by\":\"month\", \"direction\":\"DESC\"}],\n" +
                "        \"report_interval\":\"lifetime\",\n" +
                "        \"timezone\":\"EST\"\n" +
                "    }\n" +
                "}";
        String json = requests.postRequest(url+"/report", jsonPost);
        return downloadReportWhenReady(json);
    }

    public ArrayList<BillingAdvertiser> requestBillingReport(String interval) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_billing\",\n" +
                "        \"columns\": [\n" +
                "            \"advertiser_id\",\n" +
                "            \"advertiser_name\",\n" +
                "            \"campaign_id\",\n" +
                "            \"campaign_name\",\n" +
                "            \"imps\",\n" +
                "            \"clicks\",\n" +
                "            \"convs\",\n" +
                "            \"media_cost\",\n" +
                "            \"network_revenue\",\n" +
                "            \"ecpm\",\n" +
                "            \"seller_member_id\"\n" +
                "        ],\n" +
                "        \"row_per\" :[\n" +
                "            \"campaign_id\"\n" +
                "        ],\n" +
                "        \"report_interval\": \"" + interval + "\",\n" +
                //"           \"start_date\": \"2014-04-15\", \"end_date\": \"2014-05-09\","+
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

    public ArrayList<ImpType> requestPublisherBillingReport(String publisherId, String interval) throws Exception {
    public ArrayList<ImpType> requestPublisherBillingReport(String publisherId, String interval) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"imp_type\",\n" +
                "            \"imps_total\",\n" +
                "            \"network_revenue\",\n" +
                "            \"publisher_revenue\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"imp_type\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"" + interval + "\",\n" +
                "        \"orders\": [\n" +
                "            {\n" +
                "            \"order_by\" : \"imp_type\",\n" +
                "            \"direction\": \"DESC\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"timezone\" : \"EST5EDT\"\n" +
                "    }\n" +
                "}";

        String json = requests.postRequest(url+"/report?publisher_id=" + publisherId, jsonPost);
        return ResponseParser.parsePublisherBillingReport(downloadReportWhenReady(json));
    }

    public List<String[]> requestImpReport() throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_billing\",\n" +
                "        \"columns\": [\n" +
                "            \"campaign_id\",\n" +
                "            \"imps\"\n" +
                "        ],\n" +
                "        \"row_per\" :[\n" +
                "            \"campaign_id\"\n" +
                "        ],\n" +
                "           \"report_interval\": \"last_month\"," +
                //"        \"start_date\": \"2014-02-12\", \"end_date\": \"2014-03-01\","+
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
        return downloadReportWhenReady(json);
    }

    public List<String[]> getPnlReport(String pubId, String interval) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\":\"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"publisher_id\",\n" +
                "            \"network_profit\",\n" +
                "            \"imps_total\",\n" +
                "            \"imps_resold\",\n" +
                "            \"serving_fees\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"publisher_id\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"" + interval + "\",\n" +
                "        \"format\":\"csv\",\n" +
                "        \"timezone\":\"EST5EDT\"\n" +
                "    }\n" +
                "}";

        String json = requests.postRequest(url+"/report?publisher_id="+pubId, jsonPost);
        return downloadReportWhenReady(json);
    }

    public float getResoldRevenue(String pubId, String interval) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\":\"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"imp_type_id\",\n" +
                "            \"network_revenue\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"imp_type_id\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"" + interval + "\",\n" +
                "        \"format\":\"csv\",\n" +
                "        \"timezone\":\"EST5EDT\"\n" +
                "    }\n" +
                "}";

        String json = requests.postRequest(url+"/report?publisher_id="+pubId, jsonPost);
        return ResponseParser.parseResoldRevenue(downloadReportWhenReady(json));
    }

    public ArrayList<BillingAdvertiser> requestCreativeBillingReport () throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_billing\",\n" +
                "        \"columns\": [\n" +
                "            \"advertiser_id\",\n" +
                "            \"advertiser_name\",\n" +
                "            \"campaign_id\",\n" +
                "            \"campaign_name\",\n" +
                "            \"imps\",\n" +
                "            \"clicks\",\n" +
                "            \"convs\",\n" +
                "            \"media_cost\",\n" +
                "            \"network_revenue\",\n" +
                "            \"ecpm\",\n" +
                "            \"creative_id\",\n" +
                "            \"creative_name\"\n" +
                "        ],\n" +
                "        \"row_per\" :[\n" +
                "            \"campaign_id\"\n" +
                "        ],\n" +
                "        \"report_interval\": \"last_month\",\n" +
                //"           \"start_date\": \"2013-11-01\", \"end_date\": \"2013-12-01\","+
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
        return ResponseParser.parseCreativeBillingReport(downloadReportWhenReady(json));
    }

    public List<String[]> requestSellerReport (String interval) throws Exception {
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
                "        \"report_interval\": \"" + interval + "\",\n" +
                //"           \"start_date\": \"2014-04-15\", \"end_date\": \"2014-05-0\","+
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

    public List<String[]> requestCreativeSellerReport () throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_billing\",\n" +
                "        \"columns\": [\n" +
                "            \"seller_member_id\",\n" +
                "            \"seller_member_name\",\n" +
                "            \"creative_id\",\n" +
                "            \"imps\"\n" +
                "        ],\n" +
                "        \"row_per\" :[\n" +
                "            \"creative_id\",\n" +
                "            \"seller_member_id\"\n" +
                "        ],\n" +
                "        \"report_interval\": \"last_month\",\n" +
                //"           \"start_date\": \"2013-11-01\", \"end_date\": \"2013-12-01\","+
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

    public ArrayList<Domain> requestDomainReport () throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_site_domain_performance\",\n" +
                "        \"columns\": [\n" +
                "            \"site_domain\",\n" +
                "            \"booked_revenue\",\n" +
                "            \"clicks\",\n" +
                "            \"click_thru_pct\",\n" +
                "            \"convs_per_mm\",\n" +
                "\t    \"convs_rate\",\n" +
                "\t    \"cost_ecpa\",\n" +
                "\t    \"cost_ecpc\",\n" +
                "\t    \"cpm\",\n" +
                "\t    \"ctr\",\n" +
                "        \"imps\",\n" +
                "        \"media_cost\",\n" +
                "        \"post_click_convs\",\n" +
                "        \"post_click_convs_rate\",\n" +
                "        \"post_view_convs\",\n" +
                "        \"post_view_convs_rate\",\n" +
                "        \"profit\",\n" +
                "        \"profit_ecpm\"\n" +
                "        ],\n" +
                "        \"orders\": [\n" +
                "            {\n" +
                "            \"order_by\":\"imps\",\n" +
                "            \"direction\":\"DESC\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"report_interval\":\"last_7_days\",\n" +
                "        \"emails\": [],\n" +
                "        \"format\": \"csv\"\n" +
                "    }\n" +
                "}";

        String json = requests.postRequest(url+"/report", jsonPost);
        return ResponseParser.parseDomainReport(downloadReportWhenReady(json));
    }

    public List<String[]> requestDailyDomainReport(String id) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"site_domain_performance\",\n" +
                "        \"columns\": [\n" +
                "            \"site_domain\",\n" +
                "            \"imps\",\n" +
                "            \"clicks\",\n" +
                "            \"click_thru_pct\",\n" +
                "            \"post_view_convs\",\n" +
                "            \"post_click_convs\",\n" +
                "            \"cpm\"\n" +
                "        ],\n" +
                "        \"report_interval\": \"yesterday\",\n" +
                "        \"orders\": [{\n" +
                "            \"order_by\":\"imps\", \"direction\":\"DESC\"\n" +
                "        }],\n" +
                "        \"timezone\": \"EST5EDT\"\n" +
                "    }\n" +
                "}";

        String json = requests.postRequest(url + "/report?advertiser_id=" + id, jsonPost);
        return downloadReportWhenReady(json);
    }

    public List<String[]> requestRollingDomainImpsReport() throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_site_domain_performance\",\n" +
                "        \"columns\": [\n" +
                "            \"advertiser_name\",\n" +
                "            \"site_domain\",\n" +
                "            \"day\",\n" +
                "            \"imps\"\n" +
                "        ],\n" +
                "        \"report_interval\": \"last_7_days\",\n" +
                "        \"orders\": [\n" +
                "            {\"order_by\":\"day\", \"direction\":\"DESC\"\n}," +
                "            {\"order_by\":\"imps\", \"direction\":\"DESC\"\n}" +
                "        ],\n" +
                "        \"timezone\": \"EST5EDT\"\n" +
                "    }\n" +
                "}";

        String json = requests.postRequest(url + "/report", jsonPost);
        return downloadReportWhenReady(json);
    }

    public List<String[]> requestPublisherReport(String id) throws Exception {
        String j = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"day\",\n" +
                "            \"publisher_id\",\n" +
                "            \"payment_type\",\n" +
                "            \"placement_name\",\n" +
                "            \"imps_total\",\n" +
                "            \"imps_sold\",\n" +
                "            \"imps_default\",\n" +
                "            \"network_revenue\",\n" +
                "            \"publisher_revenue\",\n" +
                "            \"network_rpm\",\n" +
                "            \"serving_fees\",\n" +
                "            \"size\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"placement_id\",\n" +
                "            \"day\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"lifetime\",\n" +
                "        \"emails\":[\n" +
                "        ],\n" +
                "        \"orders\": [\n" +
                "            {\n" +
                "            \"order_by\" : \"day\",\n" +
                "            \"direction\": \"ASC\"\n" +
                "            },\n" +
                "            {\n" +
                "            \"order_by\" : \"placement_name\",\n" +
                "            \"direction\": \"DESC\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";
        String json = requests.postRequest(url+"/report?publisher_id="+id, j);
        return downloadReportWhenReady(json);
    }

    public List<String[]> requestInternalPublisherReport(String id) throws Exception {
        String j = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"day\",\n" +
                "            \"publisher_id\",\n" +
                "            \"payment_type\",\n" +
                "            \"placement_name\",\n" +
                "            \"imps_total\",\n" +
                "            \"imps_kept\",\n" +
                "            \"imps_resold\",\n" +
                "            \"imps_default\",\n" +
                "            \"network_revenue\",\n" +
                "            \"publisher_revenue\",\n" +
                "            \"network_rpm\",\n" +
                "            \"serving_fees\",\n" +
                "            \"size\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"placement_id\",\n" +
                "            \"day\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"lifetime\",\n" +
                "        \"emails\":[\n" +
                "        ],\n" +
                "        \"orders\": [\n" +
                "            {\n" +
                "            \"order_by\" : \"day\",\n" +
                "            \"direction\": \"ASC\"\n" +
                "            },\n" +
                "            {\n" +
                "            \"order_by\" : \"placement_name\",\n" +
                "            \"direction\": \"DESC\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";
        String json = requests.postRequest(url+"/report?publisher_id="+id, j);
        return downloadReportWhenReady(json);
    }

    public ArrayList<Tuple2<String, String>> requestTopBrandReport (String pubId) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"brand_name\",\n" +
                "            \"imps_total\"\n" +
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
                "                        \"order_by\":\"imps_total\", \n" +
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
                "            \"imps_resold\",\n" +
                "            \"imps_kept\",\n" +
                "            \"imps_default\",\n" +
                "            \"imps_psa\",\n" +
                "            \"network_rpm\"\n" +
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

    public List<String[]> getPublisherSummary(String interval, String pubId) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\":\"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"publisher_name\",\n" +
                "            \"publisher_id\",\n" +
                "            \"imp_type\",\n" +
                "            \"imps_total\",\n" +
                "            \"network_revenue\",\n" +
                "            \"network_rpm\",\n" +
                "            \"network_profit\"\n" +

                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"imp_type\"\n" +
                "        ],\n" +
                "        \"report_interval\":\""+ interval +"\",\n" +
                "        \"emails\":[\n" +
                "        ],\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";

        String json = requests.postRequest(url+"/report?publisher_id="+pubId, jsonPost);
        return downloadReportWhenReady(json);
    }

    public List<String[]> getPublisherTrendReport(String pubId) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"day\",\n" +
                "            \"imps_total\",\n" +
                "            \"imps_default\",\n" +
                "            \"network_rpm\",\n" +
                "            \"network_revenue\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"day\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"last_7_days\",\n" +
                "        \"format\":\"csv\",\n" +
                "        \"timezone\":\"EST5EDT\",\n" +
                "        \"orders\": [\n" +
                "                    {\n" +
                "                        \"order_by\":\"day\", \n" +
                "                        \"direction\":\"DESC\"\n" +
                "                    }\n" +
                "                ]\n" +
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
                "            \t \"imps_resold\",\n" +
                "            \t \"imps_kept\",\n" +
                "            \t \"imps_default\",\n" +
                "            \t \"imps_psa\",\n" +
                "\t\t\"network_revenue\",\n" +
                "   \"network_rpm\"" +
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
                "}";

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
        String json = requests.postRequest(url + "report?advertiser_id=" + adId, jsonPostData);

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
                "            \"daily_uniques\",\n" +
                "            \"month\",\n" +
                "            \"monthly_uniques\"\n" +
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

    public List<String[]> requestBuyerReport(String pubId) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"day\",\n" +
                "            \"buyer_member_name\",\n" +
                "            \"buyer_member_id\",\n" +
                "            \"imps_kept\",\n" +
                "            \"imps_resold\",\n" +
                "            \"network_revenue\",\n" +
                "            \"network_rpm\",\n" +
                "            \"placement_id\",\n" +
                "            \"placement_name\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"placement_id\",\n" +
                "            \"brand_name\",\n" +
                "            \"day\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"last_7_days\",\n" +
                "        \"format\":\"csv\",\n" +
                "        \"emails\":[\n" +
                "        ],\n" +
                "        \"orders\": [\n" +
                "                    {\n" +
                "                        \"order_by\":\"buyer_member_name\", \n" +
                "                        \"direction\":\"DESC\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"order_by\":\"day\", \n" +
                "                        \"direction\":\"DESC\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"order_by\":\"network_revenue\", \n" +
                "                        \"direction\":\"DESC\"\n" +
                "                    },\n" +
                "                    {\n" +
                "                        \"order_by\":\"placement_id\",\n" +
                "                        \"direction\":\"DESC\"\n" +
                "                    }\n" +
                "                ],\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";
        return downloadReportWhenReady(requests.postRequest(url + "/report?publisher_id=" + pubId, jsonPost));
    }

    public List<String[]> requestBrandReport(String pubId) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"day\",\n" +
                "            \"brand_name\",\n" +
                "            \"brand_id\",\n" +
                "            \"imps_kept\",\n" +
                "            \"imps_resold\",\n" +
                "            \"network_revenue\",\n" +
                "            \"network_rpm\",\n" +
                "            \"placement_id\",\n" +
                "            \"placement_name\",\n" +
                "            \"imps_total\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"placement_id\",\n" +
                "            \"brand_name\",\n" +
                "            \"day\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"last_7_days\",\n" +
                "        \"format\":\"csv\",\n" +
                "        \"emails\":[\n" +
                "        ],\n" +
                "        \"orders\": [\n" +
                "                    {\n" +
                "                        \"order_by\":\"imps_total\", \n" +
                "                        \"direction\":\"DESC\"\n" +
                "                    }\n" +
                "                ],\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";
        return downloadReportWhenReady(requests.postRequest(url + "/report?publisher_id=" + pubId, jsonPost));
    }

    public List<String[]> requestPlacementReport(String pubId) throws Exception {
        String jsonPost = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_publisher_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"day\",\n" +
                "            \"imps_kept\",\n" +
                "            \"imps_resold\",\n" +
                "            \"network_revenue\",\n" +
                "            \"network_rpm\",\n" +
                "            \"placement_id\",\n" +
                "            \"placement_name\",\n" +
                "            \"imps_total\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"placement_id\",\n" +
                "            \"day\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"last_7_days\",\n" +
                "        \"format\":\"csv\",\n" +
                "        \"emails\":[\n" +
                "        ],\n" +
                "        \"orders\": [\n" +
                "                    {\n" +
                "                        \"order_by\":\"imps_total\", \n" +
                "                        \"direction\":\"DESC\"\n" +
                "                    }\n" +
                "                ],\n" +
                "        \"timezone\": \"EST5EDT\""+
                "    }\n" +
                "}";
        return downloadReportWhenReady(requests.postRequest(url + "/report?publisher_id=" + pubId, jsonPost));
    }

    public List<String[]> requestCreativeReport(String interval) throws Exception {
        String j = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\": \"network_advertiser_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"creative\",\n" +
                "            \"imps\",\n" +
                "            \"clicks\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"creative\"\n" +
                "        ],\n" +
                "        \"report_interval\":\""+interval+"\",\n" +
                "        \"timezone\":\"EST\"\n" +
                "    }\n" +
                "}";
        return downloadReportWhenReady(requests.postRequest(url + "/report?advertiser_id=271483", j));
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
