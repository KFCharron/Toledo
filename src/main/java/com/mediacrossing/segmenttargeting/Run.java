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

        /*
        Segment Load Report
        */


        //Request advertiser analytic reports to obtain daily campaign impressions
        HashSet<String> advertiserIdSet = new HashSet<String>();
        for (Campaign campaign : dataStore.getLiveCampaignArrayList()) {
            advertiserIdSet.add(campaign.getAdvertiserID());
        }
        //Request reports for each ad id
        ArrayList<Campaign> campaignList = dataStore.getLiveCampaignArrayList();
        List<String[]> csvData;
        for(String advertiserId : advertiserIdSet) {
            csvData = AppNexusReportRequests.getCampaignImpsReport(advertiserId,
                    appNexusUrl, httpConnection);

            //remove header
            csvData.remove(0);

            //for every row in the file
            for (String[] line : csvData) {

                //find the same campaign in the datastore
                int count = 0;
                while(!campaignList
                        .get(count)
                        .getId()
                        .equals(line[0])) {
                   count++;
                }
                if(campaignList.get(count).getId().equals(line[0])) {
                    //set that campaigns daily imps
                    campaignList.get(count).setDailyImps(Integer.parseInt(line[1]));
                }
                else {
                    LOG.info("Campaign " + campaignList.get(count).getId() + "is not live or not found.");
                }
            }
        }
        //update the live campaign list in the data store
        dataStore.setLiveCampaignArrayList(campaignList);

        //Collect all segments into one list
        ArrayList<Segment> allSegments = new ArrayList<Segment>();
        for(Campaign campaign : dataStore.getCampaignArrayList()) {
            for(SegmentGroupTarget segmentGroupTarget : campaign.getProfile().getSegmentGroupTargets()) {
                if(segmentGroupTarget.getBoolOp().equals("include")) {
                    for(Segment segment : segmentGroupTarget.getSegmentArrayList()) {
                        if(segment.getBoolOp().equals("include")) {
                            allSegments.add(segment);
                        }

                    }
                }


            }
        }
        //Create set of unique segmentIds
        HashSet segmentIdSet = new HashSet();
        for(Segment segment : allSegments) {
            segmentIdSet.add(segment.getId());
        }
        System.out.println(segmentIdSet.size());

        csvData = AppNexusReportRequests.getSegmentLoadReport(segmentIdSet,
                appNexusUrl, httpConnection);

        //remove header data
        csvData.remove(0);

        //for every line, parse the data into segment rows
        ArrayList<SegmentRow> segmentRowArrayList = new ArrayList<SegmentRow>();
        for (String[] line : csvData) {
            ArrayList<Campaign> segmentCampaigns = new ArrayList<Campaign>();
            for(Campaign campaign : dataStore.getLiveCampaignArrayList()) {
                for(SegmentGroupTarget segmentGroupTarget : campaign.getProfile().getSegmentGroupTargets()) {
                    if(segmentGroupTarget.getBoolOp().equals("include")) {
                        for(Segment segment : segmentGroupTarget.getSegmentArrayList()) {
                            if(segment.getBoolOp().equals("include")) {
                                if(segment.getId().equals(line[0])) {
                                    segmentCampaigns.add(campaign);
                                }
                            }
                        }
                    }
                }
            }
            SegmentRow segmentRow = new SegmentRow(line[0],line[1], segmentCampaigns);
            segmentRowArrayList.add(segmentRow);
        }
        xlsWriter.writeSegmentLoadFile(segmentRowArrayList, fileOutputPath);

    }
}
