package com.mediacrossing.segmenttargeting;

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
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private static final Logger LOG = LoggerFactory.getLogger(HTTPConnection.class);

    // FIXME MX-specific data should not live in a generic HTTPConnection
    // This is a major code smell
    final List<Tuple2<String, String>> mxRequestProperties;

    public HTTPConnection(String mxUsername, String mxPassword) {
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
        }
    }

    static {
        acceptAllCertificates();
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
        this.requestData(mxRequestProperties);
//        LOG.debug("Fetching Mock Data");
//        MockMXData mockMXData = new MockMXData();
//        this.setJSONData(mockMXData.getMockProfileData());
    }

    public void requestAllCampaignsFromMX(String mxUrl) throws Exception {

        this.setUrl(mxUrl);
        this.requestData(mxRequestProperties);
//        MockMXData mockMXData = new MockMXData();
//        this.setJSONData(mockMXData.getMockCampaignData());
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

    public String requestAdvertiserReport(String advertiserId) throws IOException {

        this.setUrl("http://api.appnexus.com/report?advertiser_id=" + advertiserId);

        java.net.URL wsURL = new URL(null, url,new sun.net.www.protocol.https.Handler());
        HttpsURLConnection con = (HttpsURLConnection) wsURL.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        //Set Auth Token
        con.setRequestProperty("Authorization", this.getAuthorizationToken());
        //Authorization JSON data
        //TODO add json here
        String urlParameters = "{\n" +
                "    \"report\":\n" +
                "    {\n" +
                "        \"report_type\":\"advertiser_analytics\",\n" +
                "        \"columns\":[\n" +
                "            \"day\",\n" +
                "            \"campaign_id\",\n" +
                "            \"spend\"\n" +
                "        ],\n" +
                "        \"row_per\":[\n" +
                "            \"campaign_id\",\n" +
                "            \"day\"\n" +
                "        ],\n" +
                "        \"report_interval\":\"lifetime\",\n" +
                "        \"format\":\"csv\",\n" +
                "        \"emails\":[\n" +
                "            \"kyle.charron@mediacrossing.com\"\n" +
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
        return Collections.unmodifiableList(
                Arrays.asList(ConnectionRequestProperties.authorization(this.authorizationToken)));
    }

    public void requestDownload(String downloadUrl) throws Exception {
        setUrl(downloadUrl);
        requestData(appNexusRequestProperties());
    }
}
