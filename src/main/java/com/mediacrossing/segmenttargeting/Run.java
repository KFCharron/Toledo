package com.mediacrossing.segmenttargeting;

import com.mediacrossing.properties.ConfigurationProperties;
import com.mediacrossing.report_requests.AppNexusReportRequests;
import com.mediacrossing.segmenttargeting.profiles.PartitionedProfileRepository;
import com.mediacrossing.segmenttargeting.profiles.ProfileRepository;
import com.mediacrossing.segmenttargeting.profiles.TruncatedProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Run {

    private static int APPNEXUS_PARTITION_SIZE;
    private static Duration APPNEXUS_REQUEST_DELAY;
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
        ConfigurationProperties properties = new ConfigurationProperties(args);
        String mxUsername = properties.getMxUsername();
        String mxPassword = properties.getMxPassword();
        HTTPConnection httpConnection = new HTTPConnection(mxUsername, mxPassword);
        DataStore dataStore = new DataStore();
        String appNexusUsername = properties.getAppNexusUsername();
        String appNexusPassword = properties.getAppNexusPassword();
        String fileOutputPath = properties.getOutputPath();
        String mxUrl = properties.getMxUrl();
        String appNexusUrl = properties.getAppNexusUrl();
        APPNEXUS_PARTITION_SIZE = properties.getPartitionSize();
        APPNEXUS_REQUEST_DELAY = properties.getRequestDelayInSeconds();


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

        //Write xls file for all target segment reports
        XlsWriter xlsWriter = new XlsWriter();
        xlsWriter.writeAllReports(dataStore.getLiveCampaignArrayList(), fileOutputPath);

        /*Segment Load Report*/

        //Collect all segments into one list
        ArrayList<Segment> allSegments = new ArrayList<Segment>();
        for(Campaign campaign : dataStore.getCampaignArrayList()) {
            for(SegmentGroupTarget segmentGroupTarget : campaign.getProfile().getSegmentGroupTargets()) {
                for(Segment segment : segmentGroupTarget.getSegmentArrayList()) {
                    allSegments.add(segment);
                }

            }
        }
        //Create set of unique segmentIds
        HashSet segmentIdSet = new HashSet();
        for(Segment segment : allSegments) {
            segmentIdSet.add(segment.getId());
        }
        System.out.println(segmentIdSet.size());

        List<String[]> csvData = AppNexusReportRequests.getSegmentLoadReport(segmentIdSet, appNexusUrl, httpConnection);

        //remove header data
        csvData.remove(0);

        //for every line, parse the data into correct variables
        for (String[] line : csvData) {
            //add data to new list
        }

        //Request advertiser analytic reports to obtain daily campaign impressions



    }
}
