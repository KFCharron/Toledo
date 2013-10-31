package com.mediacrossing.segmentloadreport;

import com.mediacrossing.connections.*;
import com.mediacrossing.dailycheckupsreport.*;
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
import java.util.HashSet;
import java.util.List;

public class RunSegmentLoadReport {

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
        boolean development = true;
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

            csvData = anConn.getCampaignImpsReport(advertiserId);

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
                    if(segment.getAction().equals("include") && !segment.getCode().contains("DLTD")) {
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

        csvData = anConn.getSegmentLoadReport(segmentIdSet);

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
            String jsonData = mxConn.requestAdvertiser(adId);
            for (Campaign camp : newCampaignArrayList) {
                if (camp.getAdvertiserID().equals(adId)) {
                    camp.setAdvertiserName(JSONParse.obtainAdvertiserName(jsonData));
                    ArrayList<String> liArray = JSONParse.obtainLineItemArray(jsonData);
                    for(String li : liArray) {
                        if(camp.getLineItemID().equals(li)) {
                            camp.setLineItemName(JSONParse.obtainLineItemName(mxConn.requestLineItemNames(adId),li));
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

        // Serialize data object to a file
        /*try {
            ObjectOutputStream out = new ObjectOutputStream
                    (new FileOutputStream("/Users/charronkyle/Desktop/ReportData/SegmentLoadData.ser"));
            out.writeObject(newCampaignArrayList);
            out.close();
        } catch (IOException e) {
            LOG.error("Serialization Failed!");
            LOG.error(e.toString());
        }*/

        XlsWriter xlsWriter = new XlsWriter();
        xlsWriter.writeSegmentLoadFile(dataStore.getLiveCampaignArrayList(), fileOutputPath);
    }
}
