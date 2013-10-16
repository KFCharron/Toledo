package com.mediacrossing.segmenttargeting;

import com.mediacrossing.properties.ConfigurationProperties;
import com.mediacrossing.reportrequests.AppNexusReportRequests;
import com.mediacrossing.segmenttargeting.profiles.PartitionedProfileRepository;
import com.mediacrossing.segmenttargeting.profiles.ProfileRepository;
import com.mediacrossing.segmenttargeting.profiles.TruncatedProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import scala.concurrent.duration.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RunSegmentTargeting {

    private static int APPNEXUS_PARTITION_SIZE;
    private static Duration APPNEXUS_REQUEST_DELAY;
    private static final Logger LOG = LoggerFactory.getLogger(RunSegmentTargeting.class);

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

        //for faster debugging
        boolean development = false;
        if (development) {
            try{
                FileInputStream door = new FileInputStream("/Users/charronkyle/Desktop/TargetSegmentingData.ser");
                ObjectInputStream reader = new ObjectInputStream(door);
                dataStore.setLiveCampaignArrayList((ArrayList<Campaign>) reader.readObject());
                //Write xls file for all target segment reports
                XlsWriter xlsWriter = new XlsWriter();
                xlsWriter.writeAllReports(dataStore.getLiveCampaignArrayList(), fileOutputPath);
                System.exit(0);

            }catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }
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

        //Write xls file for all target segment reports
        XlsWriter xlsWriter = new XlsWriter();
        xlsWriter.writeAllReports(dataStore.getLiveCampaignArrayList(), fileOutputPath);

        /*
        Segment Load Report
        */


        //Request advertiser analytic reports to obtain daily campaign impressions
        //collect set of unique advertiser ids
        HashSet<String> advertiserIdSet = new HashSet<String>();
        for (Campaign campaign : dataStore.getLiveCampaignArrayList()) {
            advertiserIdSet.add(campaign.getAdvertiserID());
        }

        //For every ad id, query MX
        //step through each campaign, add advertiser to it
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
                for(Campaign campaign : campaignList) {
                    if(campaign.getId().equals(line[0])) {
                        campaign.setDailyImps(Integer.parseInt(line[1]));
                    }
                }
            }
        }
        //update the live campaign list in the data store
        dataStore.setLiveCampaignArrayList(campaignList);

        //Collect all segments into one list
        ArrayList<Segment> allSegments = new ArrayList<Segment>();
        for(Campaign campaign : dataStore.getCampaignArrayList()) {
            for(SegmentGroupTarget segmentGroupTarget : campaign.getProfile().getSegmentGroupTargets()) {
                for(Segment segment : segmentGroupTarget.getSegmentArrayList()) {
                    if(segment.getAction().equals("include")) {
                        allSegments.add(segment);
                    }
                }
            }
        }
        //Create set of unique segmentIds
        HashSet segmentIdSet = new HashSet();
        for(Segment segment : allSegments) {
            segmentIdSet.add(segment.getId());
        }

        csvData = AppNexusReportRequests.getSegmentLoadReport(segmentIdSet,
                appNexusUrl, httpConnection);

        //remove header data
        csvData.remove(0);

        //save segment data to each segment
        ArrayList<Campaign> newCampaignArrayList = dataStore.getLiveCampaignArrayList();

        //collect ad ids
        HashSet<String> uniqueAdIds = new HashSet<String>();
        for (Campaign camp : newCampaignArrayList) {
            uniqueAdIds.add(camp.getAdvertiserID());
        }

        //step through all campaigns, compare both line IDs and ad ids
        for (String adId : uniqueAdIds) {
            httpConnection.requestAdvertiserFromMX(mxUrl, adId);
            String jsonData = httpConnection.getJSONData();
            for (Campaign camp : newCampaignArrayList) {
                if (camp.getAdvertiserID().equals(adId)) {
                    camp.setAdvertiserName(parser.obtainAdvertiserName(jsonData));
                    ArrayList<String> liArray = parser.obtainLineItemArray(jsonData);
                    for(String li : liArray) {
                        if(camp.getLineItemID().equals(li)) {
                            httpConnection.requestLineItemsFromMX(mxUrl, adId);
                            camp.setLineItemName(parser.obtainLineItemName(httpConnection.getJSONData(),li));
                        }
                    }
                }
            }
        }

        for (String[] line : csvData) {
            for(Campaign campaign : newCampaignArrayList) {
                for(SegmentGroupTarget segmentGroupTarget : campaign.getProfile().getSegmentGroupTargets()) {
                    for(Segment segment : segmentGroupTarget.getSegmentArrayList()) {
                        if(segment.getId().equals(line[0])) {
                            segment.setTotalSegmentLoads(line[3]);
                            segment.setDailySegmentLoads(line[4]);
                        }
                    }
                }
            }
        }
        //update live campaign list
        dataStore.setLiveCampaignArrayList(newCampaignArrayList);

        /*// Serialize data object to a file
        try {
            ObjectOutputStream out = new ObjectOutputStream
                    (new FileOutputStream("/Users/charronkyle/Desktop/TargetSegmentingData.ser"));
            out.writeObject(newCampaignArrayList);
            out.close();
        } catch (IOException e) {
            LOG.error("Serialization Failed!");
            LOG.error(e.toString());
        }*/

        xlsWriter.writeSegmentLoadFile(dataStore.getLiveCampaignArrayList(), fileOutputPath);

    }
}
