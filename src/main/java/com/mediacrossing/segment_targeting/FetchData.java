package com.mediacrossing.segment_targeting;
import scala.util.parsing.json.JSONArray;
import scala.util.parsing.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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

    private final String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws Exception {

        FetchData http = new FetchData();

        System.out.println("Testing 1 - Send Http GET request");
        http.sendGet();
        http.sendAuthorization();

    }

    // HTTP POST request
    private void sendAuthorization() throws Exception {

        String url = "https://api.appnexus.com/auth";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
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

        String rawJSON = response.toString();

        //print result
        System.out.println(rawJSON);

        JsonElement jelement = new JsonParser().parse(rawJSON);
        JsonObject  jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        String token = jobject.get("token").toString();
        token = token.replace("\"","");
        System.out.println(token);
    }

    // HTTP GET request
    private void sendGet() throws Exception {

        String url = "http://www.google.com/search?q=mkyong";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

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
        System.out.println(response.toString());

    }
}