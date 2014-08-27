package com.mediacrossing.connections;

import au.com.bytecode.opencsv.CSVReader;
import com.fasterxml.jackson.core.util.BufferRecycler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class HTTPRequest implements Request {

    private static final Logger LOG = LoggerFactory.getLogger(HTTPRequest.class);
    private Iterable<Tuple2<String, String>> requestProperties;

    public HTTPRequest(Iterable<Tuple2<String, String>> requestProperties) {
        this.requestProperties = requestProperties;
    }

    public HTTPRequest() {
        this.requestProperties = null;
    }

    public String getRequest(String url) throws Exception {

        //Create URL object
        URL obj = new URL(url);
        LOG.debug("\nSending 'GET' request to URL : " + url);

        //Open connection
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        if (requestProperties != null) {
            for (Tuple2<String, String> kv : requestProperties) {
                con.setRequestProperty(kv._1(), kv._2());
            }
        }

        //Obtain response code
        int responseCode = con.getResponseCode();
        LOG.debug("Response Code : " + responseCode);

        //Init Input Reader
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //Return response
        String rawJSON = response.toString();
        if (rawJSON.isEmpty()) {
            LOG.error("No JSON received.");
        }
        if (responseCode != 200) {
            LOG.error("Exiting Program Due to Non-200");
        }
        return rawJSON;
    }

    public String postRequest(String url, String jsonRequest) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        LOG.debug("\nSending 'POST' request to URL : " + url);
        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(jsonRequest);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            LOG.error("Received Response Code " + responseCode + " from " + url);
            LOG.error("Exiting Program");
        }

        LOG.debug("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));

        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine + "\n");
        }
        in.close();

        return response.toString();
    }

    public String putRequest(String url, String jsonRequest) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        if (requestProperties != null) {
            for (Tuple2<String, String> kv : requestProperties) {
                con.setRequestProperty(kv._1(), kv._2());
            }
        }

        //add request header
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(jsonRequest);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            LOG.error("Received Response Code " + responseCode + " from " + url);
            LOG.error("Exiting Program");
            System.exit(1);
        }
        LOG.debug("\nSending 'PUT' request to URL : " + url);
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
        LOG.debug(response.toString());
        return response.toString();
    }

    public List<String[]> reportRequest(String url) throws Exception {

        //Create URL object
        URL obj = new URL(url);

        //Open connection
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        if (requestProperties != null) {
            for (Tuple2<String, String> kv : requestProperties) {
                con.setRequestProperty(kv._1(), kv._2());
            }
        }

        //Obtain response code
        int responseCode = con.getResponseCode();
        LOG.debug("\nSending 'GET' request to URL : " + url);
        LOG.debug("Response Code : " + responseCode);

        //Input Reader
        InputStream is = con.getInputStream();

        CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(is)));
        List<String[]> csvData = reader.readAll();
        is.close();
        return csvData;
    }
}

