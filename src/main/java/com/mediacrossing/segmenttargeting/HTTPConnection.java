package com.mediacrossing.segmenttargeting;

import au.com.bytecode.opencsv.CSVReader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mediacrossing.connections.ConnectionRequestProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class HTTPConnection {
    private String authorizationToken;
    private String JSONData;
    private String url;
    private List<String[]> csvData;


    public List<String[]> getCsvData() {
        return csvData;
    }

    private static final Logger LOG = LoggerFactory.getLogger(HTTPConnection.class);

    // This is a major code smell
    final List<Tuple2<String, String>> mxRequestProperties;

    public HTTPConnection(String mxUsername, String mxPassword) {
        //noinspection unchecked
        mxRequestProperties =
                Collections.unmodifiableList(
                        Arrays.asList(
                                ConnectionRequestProperties.authorization(
                                        mxUsername,
                                        mxPassword)));
    }

    static {
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
                new javax.net.ssl.HostnameVerifier() {

                    public boolean verify(String hostname,
                                          javax.net.ssl.SSLSession sslSession) {
                        return true;
                    }
                });
    }

    private static void acceptAllCertificates() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (GeneralSecurityException e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    static {
        acceptAllCertificates();
    }

    public void requestReportStatus(Iterable<Tuple2<String, String>> requestProperties) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        for (Tuple2<String, String> kv : requestProperties) {
            con.setRequestProperty(kv._1(), kv._2());
        }

        //Send GET request
        int responseCode = con.getResponseCode();
        LOG.debug("\nSending 'GET' request to URL : " + url);
        LOG.debug("Response Code : " + responseCode);

        //Input Reader
        InputStream is = con.getInputStream();

        CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(is)));
        csvData = reader.readAll();
        is.close();

    }

    public void requestData(Iterable<Tuple2<String, String>> requestProperties) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        for (Tuple2<String, String> kv : requestProperties) {
            con.setRequestProperty(kv._1(), kv._2());
        }

        //Send GET request
        int responseCode = con.getResponseCode();
        LOG.debug("\nSending 'GET' request to URL : " + url);
        LOG.debug("Response Code : " + responseCode);
        //Input Reader
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        String rawJSON = response.toString();
        if (rawJSON.isEmpty()) {
            LOG.error("No JSON received.");
        }
        this.setJSONData(rawJSON);
    }

    public void requestProfile(String profileID, String advertiserID) throws Exception {
        this.setUrl("http://api.appnexus.com/profile?id=" + profileID + "&advertiser_id=" + advertiserID);
        this.requestData(appNexusRequestProperties());
    }

    public void requestAllCampaignsFromMX(String mxUrl) throws Exception {

        this.setUrl(mxUrl + "/api/catalog/campaigns");
        this.requestData(mxRequestProperties);
    }

    public void requestLineItemsFromMX(String mxUrl, String advertiserId) throws Exception {

        this.setUrl(mxUrl + "/api/catalog/advertisers/" + advertiserId + "/line-items");
        this.requestData(mxRequestProperties);
    }

    public void requestAdvertiserFromMX(String mxUrl, String advertiserId) throws Exception {

        this.setUrl(mxUrl + "/api/catalog/advertisers/" + advertiserId);
        this.requestData(mxRequestProperties);
    }


    public void authorizeAppNexusConnection(String username, String password) throws Exception {
        this.setUrl("https://api.appnexus.com/auth");
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        //Authorization JSON data
        String urlParameters = "{\"auth\":{\"username\":\"" + username +
                "\",\"password\":\"" + password + "\"}}";

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        LOG.debug("\nSending 'POST' request to URL : " + url);
        LOG.debug("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //Received JSON data
        String rawJSON = response.toString();
        LOG.debug(rawJSON);

        //Parse JSON, obtain token
        JsonElement jelement = new JsonParser().parse(rawJSON);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        String token = jobject.get("token").toString();
        token = token.replace("\"", "");
        if (token.isEmpty()) {
            LOG.error("Token not received.");
        }

        this.setAuthorizationToken(token);

    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public void setAuthorizationToken(String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }

    public String getJSONData() {
        return JSONData;
    }

    public void setJSONData(String JSONData) {
        this.JSONData = JSONData;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String requestCampaignImpsReport(String advertiserId) throws IOException {

        this.setUrl("http://api.appnexus.com/report?advertiser_id=" + advertiserId);

        java.net.URL wsURL = new URL(null, url,new sun.net.www.protocol.https.Handler());
        HttpsURLConnection con = (HttpsURLConnection) wsURL.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        //Set Auth Token
        con.setRequestProperty("Authorization", this.getAuthorizationToken());
        //Authorization JSON data
        String urlParameters = "{\n" +
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
                "                    ]\n" +
                "    }\n" +
                "}";

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        LOG.debug("\nSending 'POST' request to URL : " + url);
        LOG.debug("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //Received JSON data
        String rawJSON = response.toString();
        LOG.debug(rawJSON);

        //Parse JSON, obtain token
        JsonElement jelement = new JsonParser().parse(rawJSON);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        String reportId = jobject.get("report_id").toString().replace("\"", "");
        if (reportId.isEmpty()) {
            LOG.error("ReportID not received.");
        }

        return reportId;


    }

    public String fetchDownloadUrl(String reportID) throws Exception {
        setUrl("http://api.appnexus.com/report?id=" + reportID);
        requestData(appNexusRequestProperties());
        return getJSONData();
    }

    private Iterable<Tuple2<String, String>> appNexusRequestProperties() {
        //noinspection unchecked
        return Collections.unmodifiableList(
                Arrays.asList(ConnectionRequestProperties.authorization(this.authorizationToken)));
    }

    public void requestDownload(String downloadUrl) throws Exception {
        setUrl(downloadUrl);
        requestReportStatus(appNexusRequestProperties());
    }

    public void requestPublishersFromAN(String mxUrl) throws Exception {
        this.setUrl(mxUrl + "/publisher");
        this.requestData(appNexusRequestProperties());
    }

    public String requestPublisherReport(String publisherId) throws IOException {

        this.setUrl("http://api.appnexus.com/report?publisher_id=" + publisherId);

        java.net.URL wsURL = new URL(null, url,new sun.net.www.protocol.https.Handler());
        HttpsURLConnection con = (HttpsURLConnection) wsURL.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        //Set Auth Token
        con.setRequestProperty("Authorization", this.getAuthorizationToken());
        //Authorization JSON data
        String urlParameters = "{\n" +
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
                "        \"report_interval\":\"yesterday\",\n" +
                "        \"format\":\"csv\",\n" +
                "        \"emails\":[\n" +
                "        ],\n" +
                "        \"orders\": [\n" +
                "                    {\n" +
                "                        \"order_by\":\"publisher_id\", \n" +
                "                        \"direction\":\"DESC\"\n" +
                "                    }\n" +
                "                    ]\n" +
                "    }\n" +
                "}";

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        LOG.debug("\nSending 'POST' request to URL : " + url);
        LOG.debug("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //Received JSON data
        String rawJSON = response.toString();
        LOG.debug(rawJSON);

        //Parse JSON, obtain token
        JsonElement jelement = new JsonParser().parse(rawJSON);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        String reportId = jobject.get("report_id").toString().replace("\"", "");
        if (reportId.isEmpty()) {
            LOG.error("ReportID not received.");
        }

        return reportId;


    }

    public String requestAdvertiserAnalyticReport(String advertiserId, String jsonPostData) throws IOException {

        this.setUrl("http://api.appnexus.com/report?advertiser_id=" + advertiserId);

        java.net.URL wsURL = new URL(null, url,new sun.net.www.protocol.https.Handler());
        HttpsURLConnection con = (HttpsURLConnection) wsURL.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        //Set Auth Token
        con.setRequestProperty("Authorization", this.getAuthorizationToken());
        //Authorization JSON data

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(jsonPostData);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        LOG.debug("\nSending 'POST' request to URL : " + url);
        LOG.debug("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //Received JSON data
        String rawJSON = response.toString();
        LOG.debug(rawJSON);

        //Parse JSON, obtain token
        JsonElement jelement = new JsonParser().parse(rawJSON);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        String reportId = jobject.get("report_id").toString().replace("\"", "");
        if (reportId.isEmpty()) {
            LOG.error("ReportID not received.");
        }

        return reportId;


    }

    public String requestSegmentLoadsReport(String jsonPostData) throws IOException {

        this.setUrl("http://api.appnexus.com/report");

        java.net.URL wsURL = new URL(null, url,new sun.net.www.protocol.https.Handler());
        HttpsURLConnection con = (HttpsURLConnection) wsURL.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        //Set Auth Token
        con.setRequestProperty("Authorization", this.getAuthorizationToken());
        //Authorization JSON data

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(jsonPostData);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        LOG.debug("\nSending 'POST' request to URL : " + url);
        LOG.debug("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //Received JSON data
        String rawJSON = response.toString();
        LOG.debug(rawJSON);

        //Parse JSON, obtain token
        JsonElement jelement = new JsonParser().parse(rawJSON);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        String reportId = jobject.get("report_id").toString().replace("\"", "");
        if (reportId.isEmpty()) {
            LOG.error("ReportID not received.");
        }

        return reportId;


    }

    public String requestConversionReport(String adId, String jsonPost) throws IOException {
        this.setUrl("http://api.appnexus.com/report?advertiser_id=" + adId);

        java.net.URL wsURL = new URL(null, url,new sun.net.www.protocol.https.Handler());
        HttpsURLConnection con = (HttpsURLConnection) wsURL.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        //Set Auth Token
        con.setRequestProperty("Authorization", this.getAuthorizationToken());
        //Authorization JSON data

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(jsonPost);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        LOG.debug("\nSending 'POST' request to URL : " + url);
        LOG.debug("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //Received JSON data
        String rawJSON = response.toString();
        LOG.debug(rawJSON);

        //Parse JSON, obtain token
        JsonElement jelement = new JsonParser().parse(rawJSON);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        String reportId = jobject.get("report_id").toString().replace("\"", "");
        if (reportId.isEmpty()) {
            LOG.error("ReportID not received.");
        }

        return reportId;
    }
}
