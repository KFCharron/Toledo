package com.mediacrossing.dailycheckupsreport;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.HTTPRequest;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.dailycheckupsreport.profiles.ProfileRepository;
import com.mediacrossing.properties.ConfigurationProperties;
import com.mediacrossing.segmenttargeting.profiles.PartitionedProfileRepository;
import com.mediacrossing.segmenttargeting.profiles.TruncatedProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import scala.concurrent.duration.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RunDailyCheckUps {

    private static int APPNEXUS_PARTITION_SIZE;
    private static Duration APPNEXUS_REQUEST_DELAY;
    private static final Logger LOG = LoggerFactory.getLogger(RunDailyCheckUps.class);

    private static ProfileRepository development(HTTPRequest r) {
        return new TruncatedProfileRepository(r, 10);
    }

    private static ProfileRepository production(HTTPRequest r) {
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
        ConfigurationProperties properties = new ConfigurationProperties(args);
        String mxUsername = properties.getMxUsername();
        String mxPassword = properties.getMxPassword();
        String mxUrl = properties.getMxUrl();
        MxService mxConn;
        if (mxUsername == null) {
            mxConn = new MxService(mxUrl);
        } else {
            mxConn = new MxService(mxUrl, mxUsername, mxPassword);
        }
        String appNexusUrl = properties.getAppNexusUrl();
        AppNexusService anConn = new AppNexusService(appNexusUrl, properties.getAppNexusUsername(),
                                                            properties.getAppNexusPassword());
        DataStore dataStore = new DataStore();
        String fileOutputPath = properties.getOutputPath();

        APPNEXUS_PARTITION_SIZE = properties.getPartitionSize();
        APPNEXUS_REQUEST_DELAY = properties.getRequestDelayInSeconds();

        //for faster debugging
        boolean development = false;
        if (development) {
            try{
                FileInputStream door = new FileInputStream("/Users/charronkyle/Desktop/ReportData/TargetSegmentingData.ser");
                ObjectInputStream reader = new ObjectInputStream(door);
                dataStore.setLiveCampaignArrayList((ArrayList<Campaign>) reader.readObject());
                //Write xls file for all target segment reports
                XlsWriter.writeAllReports(dataStore.getLiveCampaignArrayList(), fileOutputPath);
                System.exit(0);

            }catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        }

        //Get All Campaigns from MX, save them into list
        dataStore.setCampaignArrayList(mxConn.requestAllCampaigns());
        LOG.info(dataStore.getLiveCampaignArrayList().size() + " campaigns are live.");


        //Get Profile data for each Campaign, save campaign
        final ProfileRepository profileRepository = production(anConn.requests);

        final List<Tuple2<String, String>> advertiserIdAndProfileIds =
                new ArrayList<Tuple2<String, String>>();
        for (Campaign c : dataStore.getLiveCampaignArrayList()) {
            advertiserIdAndProfileIds.add(
                    new Tuple2<String, String>(c.getAdvertiserID(), c.getProfileID()));
        }

        final List<Profile> profiles = profileRepository.findBy(advertiserIdAndProfileIds);

        for (int index = 0; index < profiles.size(); index++) {
            Campaign c = dataStore.getLiveCampaignArrayList().get(index);
            c.setProfile(profiles.get(index));
        }

        //Write xls file for all target segment reports
        XlsWriter.writeAllReports(dataStore.getLiveCampaignArrayList(), fileOutputPath);

       // Serialize data object to a file
        /*try {
            ObjectOutputStream out = new ObjectOutputStream
                    (new FileOutputStream("/Users/charronkyle/Desktop/ReportData/TargetSegmentingData.ser"));
            out.writeObject(dataStore.getLiveCampaignArrayList());
            out.close();
        } catch (IOException e) {
            LOG.error("Serialization Failed!");
            LOG.error(e.toString());
        }*/
    }
}
