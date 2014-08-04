package com.mediacrossing.dailycheckupsreport;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.HTTPRequest;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.dailycheckupsreport.profiles.ProfileRepository;
import com.mediacrossing.properties.ConfigurationProperties;
import com.mediacrossing.segmenttargeting.profiles.PutneyProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import java.util.ArrayList;
import java.util.List;

public class RunDailyCheckUps {

    private static final Logger LOG = LoggerFactory.getLogger(RunDailyCheckUps.class);

    private static ProfileRepository production(HTTPRequest r) {
        return new PutneyProfileRepository(r);
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
        String appNexusUrl = properties.getPutneyUrl();
        AppNexusService anConn = new AppNexusService(appNexusUrl
        );
        DataStore dataStore = new DataStore();
        String fileOutputPath = properties.getOutputPath();

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

        final List<Profile> profiles = profileRepository.findBy(advertiserIdAndProfileIds, properties.getPutneyUrl());

        for (int index = 0; index < profiles.size(); index++) {
            Campaign c = dataStore.getLiveCampaignArrayList().get(index);
            c.setProfile(profiles.get(index));
        }

        //Write xls file for all target segment reports
        XlsWriter.writeAllReports(dataStore.getLiveCampaignArrayList(), fileOutputPath);

    }
}
