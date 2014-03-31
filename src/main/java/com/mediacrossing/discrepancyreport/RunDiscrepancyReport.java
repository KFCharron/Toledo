package com.mediacrossing.discrepancyreport;

import au.com.bytecode.opencsv.CSVReader;
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
import com.google.api.services.dfareporting.model.*;
import com.google.api.services.dfareporting.model.File;
import com.google.api.services.dfareporting.model.Report;
import com.mediacrossing.campaignbooks.LineItem;
import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.dailycheckupsreport.Campaign;
import com.mediacrossing.properties.ConfigurationProperties;
import com.mediacrossing.publishercheckup.IdName;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RunDiscrepancyReport {

    private static final String[] ADVERTISER_IDS = {"186354", "186355", "186356"};

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

    private static final Logger LOG = LoggerFactory.getLogger(RunDiscrepancyReport.class);

    public static void registerLoggerWithUncaughtExceptions() {
        Thread.setDefaultUncaughtExceptionHandler(
                new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
        );
    }

    public static void main(String[] args) throws Exception {

        registerLoggerWithUncaughtExceptions();

        ConfigurationProperties properties = new ConfigurationProperties(args);
        String appNexusUrl = properties.getAppNexusUrl();
        String outputPath = properties.getOutputPath();
        String appNexusUsername = properties.getAppNexusUsername();
        String appNexusPassword = properties.getAppNexusPassword();
        AppNexusService anConn = new AppNexusService(appNexusUrl, appNexusUsername,
                appNexusPassword);
        MxService mxConn = new MxService(properties.getMxUrl(), properties.getMxUsername(), properties.getMxPassword());

        try {
            // initialize the transport
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            // initialize the data store factory
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);

            // authorization
            Credential credential = authorize();

            // set up global Dfareporting instance
            client = new Dfareporting.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Fetch report
            long reportId = 7563880;
            long profileId = 1154616;

            Dfareporting reporting = client;

            long fileId = reporting.reports().run(profileId, reportId).setSynchronous(true).execute().getId();

            File reportFile = reporting.reports().files().get(profileId, reportId, fileId).execute();

            // CSV of DFA Placements
            List<String[]> dfaCsvData = DownloadReportFile.run(reporting, reportFile);
            dfaCsvData = dfaCsvData.subList(9, dfaCsvData.size()-1);

            // Request Creative from AN
            ArrayList<Creative> creatives = new ArrayList<>();
            for (String i : ADVERTISER_IDS) {
                ArrayList<Creative> c = anConn.getActiveDfaCreative(i);
                for (Creative x : c) creatives.add(x);
            }

            for (Creative c : creatives) {
                System.out.println(c.getDfaPlacementId());
                for (String[] l : dfaCsvData) {
                    if (l[0].equals(c.getDfaPlacementId())) {
                        c.setDfaImps(Integer.parseInt(l[1]));
                        c.setDfaClicks(Integer.parseInt(l[2]));
                        c.setDfaConvs(Integer.parseInt(l[3]) + Integer.parseInt(l[4]));
                    }
                }
            }

            // Creative Performance From AN
            List<String[]> anCsvData = anConn.requestDfaCreativeReport("yesterday");
            for (Creative c : creatives) {
                for (String[] l : anCsvData) {
                    if (l[0].equals(c.getAppNexusId())) {
                        c.setAppNexusImps(Integer.parseInt(l[1]));
                        c.setAppNexusClicks(Integer.parseInt(l[2]));
                    }
                }
            }

            //LT Conv Nos.
            List<String[]> ltCsvData = anConn.requestDfaCreativeReport("lifetime");
            for (Creative c : creatives) {
                for (String[] l : ltCsvData) {
                    if (l[0].equals(c.getAppNexusId())) {
                        c.setAppNexusConvs(Integer.parseInt(l[3]));
                    }
                }
            }

            ArrayList<Campaign> campData = mxConn.requestAllCampaigns();
            for (Creative c : creatives) {
                if (!c.getCampaignIds().isEmpty()) {
                    for (Campaign camp : campData) {
                        if (c.getCampaignIds().get(0).equals(camp.getId())) {
                            c.setLineItemId(camp.getLineItemID());
                        }
                    }
                } else c.setLineItemId("NONE");
            }

            ArrayList<Creative> finalList = new ArrayList<>();
            for (Creative c : creatives) {
                if (!(c.getDfaImps() == 0 && c.getAppNexusImps() == 0)) finalList.add(c);
            }

            Set<String> uniqueLiIds = new HashSet<>();
            for (Creative c : finalList) uniqueLiIds.add(c.getLineItemId());

            ArrayList<LineItem> lineData = new ArrayList<>();
            for (String a : ADVERTISER_IDS) {
                ArrayList<LineItem> l = mxConn.requestLineItemsForAdvertiser(a);
                for (LineItem i : l) lineData.add(i);
            }

            ArrayList<IdName> idNames = new ArrayList<>();
            for (String i : uniqueLiIds) {
                String name = "NO NAME";
                for (LineItem l : lineData) {
                    if (l.getLineItemID().equals(i)) {
                        name = l.getLineItemName();
                    }
                }
                idNames.add(new IdName(i,name));
            }


            // Write report
            Workbook wb = DiscrepancyReportWriter.writeReport(finalList, idNames);
            LocalDate now = new LocalDate(DateTimeZone.UTC);
            wb.write(new FileOutputStream(new java.io.File(outputPath, "DFA_Discrepancy_Report_"
                    + now.toString() + ".xls")));


        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

