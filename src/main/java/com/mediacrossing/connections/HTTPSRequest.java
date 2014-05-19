package com.mediacrossing.connections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;

public class HTTPSRequest implements Request {

    private static final Logger LOG = LoggerFactory.getLogger(HTTPSRequest.class);
    private Iterable<Tuple2<String, String>> requestProperties;

    public HTTPSRequest(Iterable<Tuple2<String, String>> requestProperties) {
       this.requestProperties = requestProperties;
    }

    public HTTPSRequest() {
        this.requestProperties = null;
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

    public String getRequest(String url) throws Exception {

        //Create URL object
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        if (requestProperties != null) {
            for (Tuple2<String, String> kv : requestProperties) {
                con.setRequestProperty(kv._1(), kv._2());
            }
        }

        //Send GET request
        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            LOG.error("Received Response Code " + responseCode + " from " + url);
            LOG.error("Exiting Program");
            System.exit(1);
        }
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
        return rawJSON;
    }

    public String putRequest(String url, String json) throws Exception {
        // FIXME
        return ":)";
    }

    public String postRequest(String url, String jsonRequest) throws Exception {

        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        if (requestProperties != null) {
            for (Tuple2<String, String> kv : requestProperties) {
                con.setRequestProperty(kv._1(), kv._2());
            }
        }

        //add request header
        con.setRequestMethod("POST");
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
        return response.toString();
    }
}