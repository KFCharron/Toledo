package com.mediacrossing.segmenttargeting;

import com.mediacrossing.segmenttargeting.profiles.PartitionedProfileRepository;
import com.mediacrossing.segmenttargeting.profiles.ProfileRepository;
import com.mediacrossing.segmenttargeting.profiles.TruncatedProfileRepository;
import scala.App;
import scala.Tuple2;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Run {

    private static int APPNEXUS_PARTITION_SIZE = 10;
    private static Duration APPNEXUS_REQUEST_DELAY = Duration.apply(60, TimeUnit.SECONDS);

    private static ProfileRepository development(HTTPRequest r) {
        return new TruncatedProfileRepository(r, 10);
    }

    private static ProfileRepository production(HTTPRequest r) {
        return new PartitionedProfileRepository(
                r,
                APPNEXUS_PARTITION_SIZE,
                APPNEXUS_REQUEST_DELAY);
    }

    public static void main(String[] args) throws Exception {

        //Declare Variables
        JSONParse parser = new JSONParse();
        HTTPRequest httpConnection = new HTTPRequest();
        CSVWriter csvWriter = new CSVWriter();
        DataStore dataStore = new DataStore();
        String appNexusUsername = "";
        String appNexusPassword = "";
        String fileOutputPath = "";
        String mxUrl = "";

        //load a properties file
        Properties prop = new Properties();
        try {
            if(args[0] != null) {
                File configFile = new File(args[0]);
                InputStream is = new FileInputStream(configFile);
                try {
                    prop.load(is);
                } finally {
                    is.close();
                }
            }
            else
                prop.load(App.class.getClassLoader().getResourceAsStream("config.properties"));

            //set the properties
            appNexusUsername = prop.getProperty("appNexusUsername");
            appNexusPassword = prop.getProperty("appNexusPassword");
            fileOutputPath = prop.getProperty("outputPath");
            mxUrl = prop.getProperty("mxUrl");
            APPNEXUS_PARTITION_SIZE = Integer.parseInt(prop.getProperty("partitionSize"));
            APPNEXUS_REQUEST_DELAY =
                    Duration.apply((Integer.parseInt(prop.getProperty("requestDelayInSeconds"))), TimeUnit.SECONDS);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //Get Token
        httpConnection.authorizeAppNexusConnection(appNexusUsername, appNexusPassword);

        //Get All Campaigns from MX, save them into list
        httpConnection.requestAllCampaignsFromMX(mxUrl);
        dataStore.setCampaignArrayList(parser.populateCampaignArrayList(httpConnection.getJSONData()));

        //Get Profile data for each Campaign, save campaign
        final ProfileRepository profileRepository = production(httpConnection);

        final List<Tuple2<String, String>> advertiserIdAndProfileIds =
                new ArrayList<Tuple2<String, String>>();
        for (Campaign c : dataStore.getCampaignArrayList()) {
            advertiserIdAndProfileIds.add(
                    new Tuple2<String, String>(c.getAdvertiserID(), c.getProfileID()));
        }

        final List<Profile> profiles = profileRepository.findBy(advertiserIdAndProfileIds);
        System.out.println(profiles.size() + " " + advertiserIdAndProfileIds.size()
                + " " + dataStore.getCampaignArrayList().size());
        for (int index = 0; index < profiles.size(); index++) {
            Campaign c = dataStore.getCampaignArrayList().get(index);
            c.setProfile(profiles.get(index));
        }

        //Get broker fees for each campaign, calls 1 URL
        httpConnection.requestAllCampaignsFromAppNexus(dataStore.getCampaignArrayList());

        //parse fees, set to campaign list
        dataStore.setCampaignArrayList
                (parser.populateBrokerFees(dataStore.getCampaignArrayList(), httpConnection.getJSONData()));

        //Convert Data to CSV files
        csvWriter.writeFrequencyFile(dataStore.getCampaignArrayList(), fileOutputPath);
        csvWriter.writeDaypartFile(dataStore.getCampaignArrayList(), fileOutputPath);
        csvWriter.writeGeographyFile(dataStore.getCampaignArrayList(), fileOutputPath);

        //Write xls file for segment report
        XlsWriter xlsWriter = new XlsWriter();
        xlsWriter.writeSegmentFileInXls(dataStore.getCampaignArrayList(), fileOutputPath);

    }
}
