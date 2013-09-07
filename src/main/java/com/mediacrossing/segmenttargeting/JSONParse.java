package com.mediacrossing.segmenttargeting;
import com.google.gson.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONParse {

    private static final Logger LOG = LoggerFactory.getLogger(HTTPRequest.class);


    public FrequencyTarget populateFrequencyTarget(String rawData) {

        JsonElement jelement = new JsonParser().parse(rawData);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        jobject = jobject.getAsJsonObject("profile");

        String maxDayImps = jobject.get("max_day_imps").toString();
        String maxLifetimeImps = jobject.get("max_lifetime_imps").toString();
        String maxPageImps = jobject.get("max_page_imps").toString();
        String maxSessionImps = jobject.get("max_session_imps").toString();
        String minMinPerImp = jobject.get("min_minutes_per_imp").toString();
        String minSessionImps = jobject.get("min_session_imps").toString();
        FrequencyTarget newFrequencyTarget = new FrequencyTarget(maxLifetimeImps, minSessionImps, maxSessionImps,
                maxDayImps, minMinPerImp, maxPageImps);

        return newFrequencyTarget;
    }

    public ArrayList<DaypartTarget> populateDaypartTarget(String rawData) {

        JsonElement jelement = new JsonParser().parse(rawData);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        jobject = jobject.getAsJsonObject("profile");
        ArrayList<DaypartTarget> newDaypartTarget = new ArrayList<DaypartTarget>();
        if(!jobject.get("daypart_targets").isJsonNull()) {
            JsonArray karray = jobject.getAsJsonArray("daypart_targets");
            for(int y = 0; y < karray.size(); y++) {
                JsonObject kobject = karray.get(y).getAsJsonObject();
                //add variables to DaypartTarget
                String day = kobject.get("day").toString().replace("\"","");
                int start = kobject.get("start_hour").getAsInt();
                int end = kobject.get("end_hour").getAsInt();
                DaypartTarget newDaypart = new DaypartTarget(day, start, end);
                //add DaypartTargat to daypartTargetList
                newDaypartTarget.add(y, newDaypart);
            }
        }
        return newDaypartTarget;
    }

    public GeographyTarget populateGeographyTarget(String rawData) {

        JsonElement jelement = new JsonParser().parse(rawData);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        jobject = jobject.getAsJsonObject("profile");
        //Create new country list
        ArrayList<CountryTarget> countryTargetList = new ArrayList<CountryTarget>();
        if (!jobject.get("country_targets").isJsonNull()) {
            JsonArray karray = jobject.getAsJsonArray("country_targets");

            for (int z = 0; z < karray.size(); z++) {
                JsonObject lobject = karray.get(z).getAsJsonObject();
                String country = lobject.get("country").toString().replace("\"","");
                String name = lobject.get("name").toString().replace("\"","");
                CountryTarget newCountry = new CountryTarget(country, name);
                countryTargetList.add(z, newCountry);
            }

        }

        //set country action
        String countryAction = jobject.get("country_action").toString().replace("\"","");

        //Create new dma list
        ArrayList<DMATarget> dmaTargetList = new ArrayList<DMATarget>();

        //Check for null value
        if (!jobject.get("dma_targets").isJsonNull()) {
            //Move to dma target array
            JsonArray karray = jobject.getAsJsonArray("dma_targets");

            for (int i = 0; i < karray.size(); i++) {
                JsonObject pobject = karray.get(i).getAsJsonObject();
                String dma = pobject.get("dma").toString().replace("\"","");
                String name = pobject.get("name").toString().replace("\"","");
                DMATarget newDMATarget = new DMATarget(dma, name);
                dmaTargetList.add(i, newDMATarget);
            }

        }

        //set dma action
        String dmaAction = jobject.get("dma_action").toString().replace("\"","");

        //Create new zip target list
        ArrayList<ZipTarget> zipTargetList = new ArrayList<ZipTarget>();

        if (!jobject.get("zip_targets").isJsonNull()) {
            //Move to zip target array
            JsonArray zarray = jobject.getAsJsonArray("zip_targets");

            for (int a = 0; a < zarray.size(); a++) {
                JsonObject zobject = zarray.get(a).getAsJsonObject();
                String fromZip = zobject.get("from_zip").toString().replace("\"","");
                String toZip = zobject.get("to_zip").toString().replace("\"","");
                ZipTarget newZipTarget = new ZipTarget(fromZip, toZip);
                zipTargetList.add(a, newZipTarget);

            }
        }

        GeographyTarget newGeographyTarget =
                new GeographyTarget(countryTargetList, dmaTargetList, countryAction, dmaAction, zipTargetList);
        return newGeographyTarget;
    }

    public ArrayList<Campaign> populateCampaignArrayList(String rawData) throws ParseException {

        ArrayList<Campaign> campaignArrayList1 = new ArrayList<Campaign>();
        JsonElement jelement = new JsonParser().parse(rawData);
        for (Map.Entry<String, JsonElement> entry : jelement.getAsJsonObject().entrySet()) {
            String currentAdvertiserID = entry.getKey().replace("\"","");
            for (JsonElement values : entry.getValue().getAsJsonArray()) {
                Campaign newCampaign = new Campaign();
                JsonObject jsonObject = values.getAsJsonObject();
                newCampaign.setAdvertiserID(currentAdvertiserID);
                newCampaign.setId(jsonObject.get("id").toString().replace("\"",""));
                newCampaign.setState(jsonObject.get("status").toString().replace("\"",""));
                newCampaign.setName(jsonObject.get("name").toString().replace("\"",""));
                newCampaign.setProfileID(jsonObject.get("profileId").toString().replace("\"",""));
                newCampaign.setLineItemID(jsonObject.get("lineItemId").toString().replace("\"",""));
                newCampaign.setEndDate(jsonObject.get("endDate").toString().replace("\"",""));
                if (!jsonObject.get("brokerFees").isJsonNull()) {
                    JsonArray jarray = jsonObject.getAsJsonArray("brokerFees");
                    ArrayList<ServingFee> newServingFeeList = new ArrayList<ServingFee>();
                    for (int x = 0; x < jarray.size(); x++) {
                        jsonObject = jarray.get(x).getAsJsonObject();
                        String brokerName = jsonObject.get("broker_name").toString().replace("\"","");
                        String paymentType = jsonObject.get("payment_type").toString().replace("\"","");
                        String value = jsonObject.get("value").toString().replace("\"","");
                        String description = jsonObject.get("description").toString().replace("\"","");
                        ServingFee newServingFee = new ServingFee(brokerName, paymentType, value, description);
                        newServingFeeList.add(x, newServingFee);
                    }
                    newCampaign.setServingFeeList(newServingFeeList);
                }
                campaignArrayList1.add(newCampaign);
            }
        }
        if (campaignArrayList1.isEmpty()) {
           LOG.error("Campaign List is empty after parse.");
        }
        else {
            LOG.info(campaignArrayList1.size() + "campaigns received.");
        }
        return  campaignArrayList1;

    }

    public ArrayList<SegmentGroupTarget> populateSegmentGroupTargetList(String rawData) {
        JsonElement jelement = new JsonParser().parse(rawData);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        jobject = jobject.getAsJsonObject("profile");
        ArrayList<SegmentGroupTarget> newSegmentGroupTargetList = new ArrayList<SegmentGroupTarget>();
        String segmentGroupBoolOp = jobject.get("segment_boolean_operator").toString().replace("\"","");
        if (!jobject.get("segment_group_targets").isJsonNull()) {
            //Move to segment target array
            JsonArray jarray = jobject.getAsJsonArray("segment_group_targets");
            for (int x = 0; x < jarray.size(); x++) {

                JsonObject jsonObject = jarray.get(x).getAsJsonObject();
                String segmentBoolOp = jsonObject.get("boolean_operator").toString().replace("\"","");
                ArrayList<Segment> newSegmentArrayList = new ArrayList<Segment>();
                if(!jsonObject.get("segments").isJsonNull()) {
                    JsonArray karray = jsonObject.getAsJsonArray("segments");
                    for (int y = 0; y < karray.size(); y++) {

                        JsonObject kobject = karray.get(y).getAsJsonObject();
                        String action = kobject.get("action").toString().replace("\"","");
                        String id = kobject.get("id").toString().replace("\"","");
                        String name = kobject.get("name").toString().replace("\"","");
                        Segment newSegment = new Segment(id, name, action, segmentBoolOp);
                        newSegmentArrayList.add(y, newSegment);
                    }
                }
                SegmentGroupTarget newSegmentGroupTarget =
                        new SegmentGroupTarget(segmentGroupBoolOp, newSegmentArrayList);
                newSegmentGroupTargetList.add(x, newSegmentGroupTarget);
            }

        }

        return  newSegmentGroupTargetList;
    }

}
