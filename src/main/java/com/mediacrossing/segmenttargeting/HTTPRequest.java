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

    public void requestData() throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add token header
        con.setRequestProperty("Authorization", this.getAuthorizationToken());

        //Send GET request
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

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
        System.out.println(rawJSON);
        this.setJSONData(rawJSON);
    }

    public void requestProfile(String profileID, String advertiserID) throws Exception {
        //TODO change back to real data before deployment
//        this.setUrl("http://api.appnexus.com/profile?id=" + profileID + "&advertiser_id=" + advertiserID);
//        this.requestData();
        System.out.println("Fetching Mock Data");
        MockMXData mockMXData = new MockMXData();
        this.setJSONData(mockMXData.getMockProfileData());
    }

    public void requestAllCampaignsFromAppNexus(ArrayList<Campaign> campaignArrayList) throws Exception {
        StringBuilder oneLine = new StringBuilder();
        oneLine.append("http://api.appnexus.com/campaign?id=");
        for (Campaign c : campaignArrayList) {
            oneLine.append(c.getId());
            oneLine.append(",");
        }
        this.setUrl(oneLine.toString());
        this.requestData();
    }

    public void requestAllCampaignsFromMX(String mxUrl) throws Exception {

        this.setUrl(mxUrl);
        this.requestData();
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
}
