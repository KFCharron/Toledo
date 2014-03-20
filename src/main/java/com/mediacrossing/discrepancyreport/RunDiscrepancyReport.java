package com.mediacrossing.discrepancyreport;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.dfareporting.Dfareporting;
import com.google.api.services.dfareporting.DfareportingScopes;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class RunDiscrepancyReport {

    private static final String APPLICATION_NAME = "DiscrepancyReport";

    /** Directory to store user credentials. */
    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(System.getProperty("user.home"), ".store/dfareporting_sample");

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
     * globally shared instance across your application.
     */
    private static FileDataStoreFactory dataStoreFactory;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    /** Global instance of the HTTP transport. */
    private static HttpTransport httpTransport;

    private static Dfareporting client;

    /** Authorizes the installed application to access user's protected data. */
    private static Credential authorize() throws Exception {
        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(RunDiscrepancyReport.class.getResourceAsStream("/client_secrets.json")));

        Set<String> scopes = new HashSet<>();
        scopes.add(DfareportingScopes.DFAREPORTING);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, scopes)
                .setDataStoreFactory(dataStoreFactory)
                .build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public static void main(String[] args) {
        try {
            // initialize the transport
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            // initialize the data store factory
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);

            // authorization
            Credential credential = authorize();

            // set up global Dfareporting instance
            client = new Dfareporting.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();

            //Add Code Here


        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

