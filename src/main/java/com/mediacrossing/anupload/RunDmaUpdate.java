package com.mediacrossing.anupload;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.HTTPRequest;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.dailycheckupsreport.Campaign;
import com.mediacrossing.dailycheckupsreport.DMATarget;
import com.mediacrossing.dailycheckupsreport.Profile;
import com.mediacrossing.dailycheckupsreport.SegmentGroupTarget;
import com.mediacrossing.dailycheckupsreport.profiles.ProfileRepository;
import com.mediacrossing.properties.ConfigurationProperties;
import com.mediacrossing.segmenttargeting.profiles.PutneyProfileRepository;
import scala.Tuple2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class RunDmaUpdate {

    public static String anUrl = "http://localhost:8888/an/";

    public static String advertiserId = "324264";

    private static ProfileRepository production(HTTPRequest r) {
        return new PutneyProfileRepository(r);
    }

    public static void main(String[] args) throws Exception {

        ConfigurationProperties properties = new ConfigurationProperties(args);
        AppNexusService anConn = new AppNexusService(anUrl);
        MxService mxConn = new MxService(anUrl);
        MxService mxApi = new MxService(properties.getMxUrl(), properties.getMxUsername(), properties.getMxPassword());
        ArrayList<Campaign> camps = mxApi.requestAllCampaigns();

        HashSet<String> campIds = new HashSet<>();
//        for (Campaign c : camps) {
//            if (c.getAdvertiserID().equals(advertiserId) && c.getName().contains("USA")) campIds.add(c.getId());
//        }


        CSVReader reader = new CSVReader(new FileReader("/Users/charronkyle/Desktop/Regis_Whitelist_Campaigns.csv"));
        List<String[]> result = reader.readAll();
        result.remove(0);
        for (String[] line : result) {
            campIds.add(line[0]);
        }

        reader = new CSVReader(new FileReader("/Users/charronkyle/Desktop/city_list.csv"));
        List<String[]> cities = reader.readAll();

        ArrayList<DmaCampaign> dmaCamps = new ArrayList<>();

        for (String id : campIds) {
            DmaCampaign d = new DmaCampaign(id);
            for (String[] line : cities) {
                d.getDmas().add(line[0]);
            }
            dmaCamps.add(d);
        }

        for (DmaCampaign c : dmaCamps) {
            for (Campaign campaign : camps) {
                if (campaign.getId().equals(c.getCampId())) c.setProfileId(campaign.getProfileID());
            }
        }

        final List<Tuple2<String, String>> advertiserIdAndProfileIds =
                new ArrayList<Tuple2<String, String>>();
        for (DmaCampaign c : dmaCamps) {
            advertiserIdAndProfileIds.add(
                    new Tuple2<String, String>(advertiserId, c.getProfileId()));
        }

//        final ProfileRepository profileRepository = production(anConn.requests);

//        final List<Profile> profiles = profileRepository.findBy(advertiserIdAndProfileIds);
//
//        for (Profile p : profiles) {
//            for (DmaCampaign c : dmaCamps) {
//                if (c.getProfileId().equals(p.getId())) {
//                    for (DMATarget d : p.getGeographyTarget().getDmaTargetList()) {
//                        c.getDmas().add(d.getDma());
//                    }
//                }
//            }
//        }

        for (DmaCampaign c : dmaCamps) {
            String json = "{\n" +
                    "  \"profile\" : {\n" +
                    "    \"city_action\" : \"include\" ,\n" +
                    "    \"city_targets\" : [\n";
            for (String dma : c.getDmas()) {
                json = json.concat("{\"id\" :\"" + dma + "\"},\n");
            }
            json = json.substring(0, json.length()-2);
            json = json.concat("    ]\n" +
                    "  }}");
            System.out.println(json);
            mxConn.putRequest("profile?id=" + c.getProfileId() + "&advertiser_id=" + advertiserId, json);
        }
    }
}
