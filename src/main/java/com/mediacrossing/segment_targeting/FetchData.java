package com.mediacrossing.segment_targeting;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import com.google.gson.*;

/**
 * Created with IntelliJ IDEA.
 * User: charronkyle
 * Date: 8/20/13
 * Time: 12:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class FetchData {

    public static void main(String[] args) throws Exception {

        FetchData http = new FetchData();
        String authToken = http.requestAuthorizationToken();
        String profileData = http.requestProfiles(authToken);
        http.parseData(profileData);
    }

    // HTTP POST request
    private String requestAuthorizationToken() throws Exception {

        String url = "https://api.appnexus.com/auth";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        //Authorization JSON data
        String urlParameters = "{\"auth\":{\"username\":\"MC_report\",\"password\":\"13MediaCrossing!\"}}";

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
        JsonObject  jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        String token = jobject.get("token").toString();
        token = token.replace("\"","");
        System.out.println(token);

        return token;
    }

    // HTTP GET request
    private String requestProfiles(String token) throws Exception {

        String url = "http://api.appnexus.com/profile?advertiser_id=184587";

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

        return rawJSON;
    }

    private void parseData(String data) {

        //Parse JSON, obtain data
        JsonElement jelement = new JsonParser().parse(data);
        JsonObject  jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        JsonArray jarray = jobject.getAsJsonArray("profiles");
        for(int x = 0; x < jarray.size(); x++) {
            jobject = jarray.get(x).getAsJsonObject();
            String result = jobject.get("id").toString();
            System.out.println(result);
        }

    }
}
