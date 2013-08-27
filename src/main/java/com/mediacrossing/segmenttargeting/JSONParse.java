package com.mediacrossing.segmenttargeting;
import com.google.gson.*;
import java.util.ArrayList;
import java.util.Map;

public class JSONParse {

    public ArrayList<Campaign> getMockCampaignList(String rawData) {
        ArrayList<Campaign> newCampaignList = new ArrayList<Campaign>();
        System.out.println("Mock Campaign List not setup yet.");
        return newCampaignList;
    }

    public FrequencyTargets populateFrequencyTarget(String rawData) {

        JsonElement jelement = new JsonParser().parse(rawData);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        jobject = jobject.getAsJsonObject("profile");
        FrequencyTargets newFrequencyTarget = new FrequencyTargets();
        try {
        newFrequencyTarget.setMaxDayImps(jobject.get("max_day_imps").toString());
        newFrequencyTarget.setMaxLifetimeImps(jobject.get("max_lifetime_imps").toString());
        newFrequencyTarget.setMaxPageImps(jobject.get("max_page_imps").toString());
        newFrequencyTarget.setMaxSessionImps(jobject.get("max_session_imps").toString());
        newFrequencyTarget.setMinMinutesPerImp(jobject.get("min_minutes_per_imp").toString());
        newFrequencyTarget.setMinSessionImps(jobject.get("min_session_imps").toString());
        } catch (NullPointerException exception) {
            System.out.println("Null Pointer Exception");
        }

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
                DaypartTarget newDaypart = new DaypartTarget();
                //add variables to DaypartTarget
                newDaypart.setDay(kobject.get("day").toString());
                newDaypart.setStartHour(kobject.get("start_hour").getAsInt());
                newDaypart.setEndHour(kobject.get("end_hour").getAsInt());
                //add DaypartTargat to daypartTargetList
                newDaypartTarget.add(y, newDaypart);
            }
        }
        return newDaypartTarget;
    }

    public GeographyTargets populateGeographyTarget(String rawData) {

        JsonElement jelement = new JsonParser().parse(rawData);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        jobject = jobject.getAsJsonObject("profile");
        GeographyTargets newGeographyTarget = new GeographyTargets();

        if (!jobject.get("country_targets").isJsonNull()) {
            JsonArray karray = jobject.getAsJsonArray("country_targets");
            //Create new country list
            ArrayList<CountryTarget> countryTargetList = new ArrayList<CountryTarget>();
            for (int z = 0; z < karray.size(); z++) {
                JsonObject lobject = karray.get(z).getAsJsonObject();
                CountryTarget newCountry = new CountryTarget();
                newCountry.setCountry(lobject.get("country").toString());
                newCountry.setName(lobject.get("name").toString());
                countryTargetList.add(z, newCountry);
            }
            //Add countryTargetList to geography targets
            newGeographyTarget.setCountryTargetList(countryTargetList);
        }

        //set country action
        newGeographyTarget.setCountryAction(jobject.get("country_action").toString());

        //Check for null value
        if (!jobject.get("dma_targets").isJsonNull()) {
            //Move to dma target array
            JsonArray karray = jobject.getAsJsonArray("dma_targets");
            //Create new dma list
            ArrayList<DMATarget> dmaTargetList = new ArrayList<DMATarget>();
            for (int i = 0; i < karray.size(); i++) {
                JsonObject pobject = karray.get(i).getAsJsonObject();
                DMATarget newDMATarget = new DMATarget();
                newDMATarget.setDma(pobject.get("dma").toString());
                newDMATarget.setName(pobject.get("name").toString());
                dmaTargetList.add(i, newDMATarget);
            }
            //Add dmaTargetList to geography targets
            newGeographyTarget.setDmaTargetList(dmaTargetList);
        }

        //set dma action
        newGeographyTarget.setDmaAction(jobject.get("dma_action").toString());

        return newGeographyTarget;
    }

    public ArrayList<Campaign> populateCampaignArrayList(String rawData) {

        ArrayList<Campaign> campaignArrayList1 = new ArrayList<Campaign>();
        Gson gson = new Gson();
        JsonElement jelement = new JsonParser().parse(rawData);
        for (Map.Entry<String, JsonElement> entry : jelement.getAsJsonObject().entrySet()) {
            Campaign newCampaign = new Campaign();
            newCampaign.setAdvertiserID(entry.getKey().replace("\"",""));
            for (JsonElement values : entry.getValue().getAsJsonArray()) {

                JsonObject jsonObject = values.getAsJsonObject();
                newCampaign.setId(jsonObject.get("id").toString().replace("\"",""));
                newCampaign.setState(jsonObject.get("status").toString().replace("\"",""));
                newCampaign.setName(jsonObject.get("name").toString().replace("\"",""));
                newCampaign.setProfileID(jsonObject.get("profileId").toString().replace("\"",""));
                newCampaign.setLineItemID(jsonObject.get("lineItemId").toString().replace("\"",""));
                //campaignArrayList1.add(newCampaign);
            }
            //FIXME remove this to get all campaigns, instead of one per advertiser, uncomment one above
            campaignArrayList1.add(newCampaign);
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

                SegmentGroupTarget newSegmentGroupTarget = new SegmentGroupTarget();
                JsonObject jsonObject = jarray.get(x).getAsJsonObject();
                newSegmentGroupTarget.setBoolOp(segmentGroupBoolOp);
                String segmentBoolOp = jsonObject.get("boolean_operator").toString().replace("\"","");

                if(!jsonObject.get("segments").isJsonNull()) {
                    JsonArray karray = jsonObject.getAsJsonArray("segments");
                    ArrayList<Segment> newSegmentArrayList = new ArrayList<Segment>();
                    for (int y = 0; y < karray.size(); y++) {
                        Segment newSegment = new Segment();
                        JsonObject kobject = karray.get(y).getAsJsonObject();
                        newSegment.setAction(kobject.get("action").toString().replace("\"",""));
                        newSegment.setId(kobject.get("id").toString().replace("\"",""));
                        newSegment.setName(kobject.get("name").toString().replace("\"","").replace(",","\",\""));
                        newSegment.setBoolOp(segmentBoolOp);
                        newSegmentArrayList.add(y, newSegment);
                    }
                    newSegmentGroupTarget.setSegmentArrayList(newSegmentArrayList);
                }
                newSegmentGroupTargetList.add(x, newSegmentGroupTarget);
            }

        }

        return  newSegmentGroupTargetList;
    }
}
