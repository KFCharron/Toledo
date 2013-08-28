package com.mediacrossing.segmenttargeting;

import com.mediacrossing.segmenttargeting.profiles.PartitionedProfileRepository;
import com.mediacrossing.segmenttargeting.profiles.ProfileRepository;
import com.mediacrossing.segmenttargeting.profiles.TruncatedProfileRepository;
import scala.Tuple2;
import scala.concurrent.duration.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Run {

    private static final int APPNEXUS_PARTITION_SIZE = 10;
    private static final Duration APPNEXUS_REQUEST_DELAY = Duration.apply(1, TimeUnit.MINUTES);

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
        ArrayList<Campaign> campaignArrayList;
        ArrayList<String> advertiserIDList;
        HTTPRequest httpConnection = new HTTPRequest();
        CSVWriter csvWriter = new CSVWriter();
        DataStore dataStore = new DataStore();

        //Get Token
        httpConnection.authorizeAppNexusConnection("MC_report", "13MediaCrossing!");

        //Get All Campaigns from MX, save them into list
        httpConnection.requestAllCampaignsFromMX();
        dataStore.setCampaignArrayList(parser.populateCampaignArrayList(httpConnection.getJSONData()));

        //Get Profile data for each Campaign, save campaign
        final ProfileRepository profileRepository = development(httpConnection);

        final List<Tuple2<String, String>> advertiserIdAndProfileIds =
                new ArrayList<Tuple2<String, String>>();
        for (Campaign c : dataStore.getCampaignArrayList()) {
            advertiserIdAndProfileIds.add(
                    new Tuple2<String, String>(c.getAdvertiserID(), c.getProfileID()));
        }

        final List<Profile> profiles = profileRepository.findBy(advertiserIdAndProfileIds);
        for (int index = 0; index < dataStore.getCampaignArrayList().size(); index++) {
            Campaign c = dataStore.getCampaignArrayList().get(index);
            c.setProfile(profiles.get(index));
        }

        //Convert Data to CSV files
        csvWriter.writeFrequencyFile(dataStore.getCampaignArrayList());
        csvWriter.writeDaypartFile(dataStore.getCampaignArrayList());
        csvWriter.writeGeographyFile(dataStore.getCampaignArrayList());
        csvWriter.writeSegmentFIle(dataStore.getCampaignArrayList());

    }
}
