package com.mediacrossing.segmenttargeting;

import com.mediacrossing.segmenttargeting.profiles.PartitionedProfileRepository;
import com.mediacrossing.segmenttargeting.profiles.ProfileRepository;
import com.mediacrossing.segmenttargeting.profiles.TruncatedProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOG = LoggerFactory.getLogger(Run.class);

    private static ProfileRepository development(HTTPConnection r) {
        return new TruncatedProfileRepository(r, 10);
    }

    private static ProfileRepository production(HTTPConnection r) {
        return new PartitionedProfileRepository(
                r,
                APPNEXUS_PARTITION_SIZE,
                APPNEXUS_REQUEST_DELAY);
    }

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

        //Declare Variables
        JSONParse parser = new JSONParse();
        // FIXME Externalize to configuration
        String mxUsername = "rtui";
        String mxPassword = "stats4all";
        HTTPConnection httpConnection = new HTTPConnection(mxUsername, mxPassword);
        DataStore dataStore = new DataStore();
        String appNexusUsername = "";
        String appNexusPassword = "";
        String fileOutputPath = "";
        String mxUrl = "";

        //load a properties file
        Properties prop = new Properties();
        try {
            File configFile = new File(args[0].substring("--properties-file=".length()));
            InputStream is = new FileInputStream(configFile);
            try {
                prop.load(is);
            } finally {
                is.close();
            }

            //set the properties
            if (prop.isEmpty()) {
                LOG.error("Properties File Failed To Load.");
            } else {
                LOG.info("Properties File successfully loaded.");
            }

            appNexusUsername = prop.getProperty("appNexusUsername");
            appNexusPassword = prop.getProperty("appNexusPassword");
            fileOutputPath = prop.getProperty("outputPath");
            mxUrl = prop.getProperty("mxUrl");
            APPNEXUS_PARTITION_SIZE = Integer.parseInt(prop.getProperty("partitionSize"));
            APPNEXUS_REQUEST_DELAY =
                    Duration.apply((Integer.parseInt(prop.getProperty("requestDelayInSeconds"))), TimeUnit.SECONDS);

        } catch (IOException ex) {
            LOG.error("Unable to extract properties", ex);
        }

        //Get Token
        httpConnection.authorizeAppNexusConnection(appNexusUsername, appNexusPassword);

        //Get All Campaigns from MX, save them into list
        httpConnection.requestAllCampaignsFromMX(mxUrl);
        LOG.debug(httpConnection.getJSONData());
        dataStore.setCampaignArrayList(parser.populateCampaignArrayList(httpConnection.getJSONData()));
        dataStore.setLiveCampaignArrayList();
        LOG.info(dataStore.getLiveCampaignArrayList().size() + " campaigns are live.");


        //Get Profile data for each Campaign, save campaign
        final ProfileRepository profileRepository = production(httpConnection);

        final List<Tuple2<String, String>> advertiserIdAndProfileIds =
                new ArrayList<Tuple2<String, String>>();
        for (Campaign c : dataStore.getLiveCampaignArrayList()) {
            advertiserIdAndProfileIds.add(
                    new Tuple2<String, String>(c.getAdvertiserID(), c.getProfileID()));
        }

        final List<Profile> profiles = profileRepository.findBy(advertiserIdAndProfileIds);
        LOG.debug(profiles.size() + " " + advertiserIdAndProfileIds.size()
                + " " + dataStore.getLiveCampaignArrayList().size());
        for (int index = 0; index < profiles.size(); index++) {
            Campaign c = dataStore.getLiveCampaignArrayList().get(index);
            c.setProfile(profiles.get(index));
        }

        //Write xls file for all reports
        XlsWriter xlsWriter = new XlsWriter();
        xlsWriter.writeAllReports(dataStore.getLiveCampaignArrayList(), fileOutputPath);

    }
}
