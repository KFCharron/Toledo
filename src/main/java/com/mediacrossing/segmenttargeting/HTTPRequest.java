package com.mediacrossing.segmenttargeting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class HTTPRequest {
    private String authorizationToken;
    private String JSONData;
    private String url;
    private int count = 0;


    public void requestData() throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        //default connection is GET

        //add token header
        con.setRequestProperty("Authorization", this.getAuthorizationToken());

        //Send GET request
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

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
        System.out.println(rawJSON);
        this.setJSONData(rawJSON);
        count++;
        System.out.println(count);
    }

    public void requestProfile(String profileID, String advertiserID) throws Exception {
        this.setUrl("http://api.appnexus.com/profile?id=" + profileID + "&advertiser_id=" + advertiserID);
        this.requestData();
    }

    public void requestCampaignsByAdvertiserID(String advertiserID) throws Exception {
        this.setUrl("http://api.appnexus.com/campaign?advertiser_id=" + advertiserID);
        this.requestData();
    }

    public void requestAllCampaignsFromAppNexus(ArrayList<Campaign> campaignArrayList) throws Exception {
        StringBuffer oneLine = new StringBuffer();
        oneLine.append("http://api.appnexus.com/campaign?id=");
        for (Campaign c : campaignArrayList) {
            oneLine.append(c.getId());
            oneLine.append(",");
        }
        this.setUrl(oneLine.toString());
        this.requestData();
    }

    public void requestAllCampaignsFromMX() throws Exception {
        //this.setUrl("http://ec2-50-17-18-117.compute-1.amazonaws.com:9000/api/catalog/campaigns");
        //this.requestData();
        MockMXData mockMXData = new MockMXData();
        this.setJSONData(mockMXData.getMockData());
    }

    public void requestAllAdvertisersFromAN() throws Exception {
        this.setUrl("http://api.appnexus.com/advertiser");
        this.requestData();
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
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

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
        System.out.println(rawJSON);

        //Parse JSON, obtain token
        JsonElement jelement = new JsonParser().parse(rawJSON);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        String token = jobject.get("token").toString();
        token = token.replace("\"","");
        System.out.println(token);

        this.setAuthorizationToken(token);

    }

    // HTTP GET request
    private void requestProfiles(String token) throws Exception {

        String url = "http://api.appnexus.com/profile?advertiser_id=165002";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        //default connection is GET

        //add token header
        con.setRequestProperty("Authorization", token);

        //Send GET request
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

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
        System.out.println(rawJSON);

        //return rawJSON;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
