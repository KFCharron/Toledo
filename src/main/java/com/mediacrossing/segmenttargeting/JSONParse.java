package com.mediacrossing.segmenttargeting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: charronkyle
 * Date: 8/23/13
 * Time: 8:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class JSONParse {
    public ArrayList<Campaign> campaignArrayList = new ArrayList<Campaign>();

    public ArrayList<Campaign> getCampaignArrayList() {
        return campaignArrayList;
    }

    public void setCampaignArrayList(ArrayList<Campaign> campaignArrayList) {
        this.campaignArrayList = campaignArrayList;
    }

    public ArrayList<Campaign> getMockCampaignList(String rawData) {
        ArrayList<Campaign> newCampaignList = new ArrayList<Campaign>();
        //TODO
        return newCampaignList;
    }

    public FrequencyTargets populateFrequencyTarget(String rawData) {

        JsonElement jelement = new JsonParser().parse(rawData);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        jobject = jobject.getAsJsonObject("profile");
        FrequencyTargets newFrequencyTarget = new FrequencyTargets();
        newFrequencyTarget.setMaxDayImps(jobject.get("max_day_imps").toString());
        newFrequencyTarget.setMaxLifetimeImps(jobject.get("max_lifetime_imps").toString());
        newFrequencyTarget.setMaxPageImps(jobject.get("max_page_imps").toString());
        newFrequencyTarget.setMaxSessionImps(jobject.get("max_session_imps").toString());
        newFrequencyTarget.setMinMinutesPerImp(jobject.get("min_minutes_per_imp").toString());
        newFrequencyTarget.setMinSessionImps(jobject.get("min_session_imps").toString());

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

    public void populateCampaignList(String rawData, String advertiserID) {

        JsonElement jelement = new JsonParser().parse(rawData);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        JsonArray jarray = jobject.getAsJsonArray("campaigns");

        for (int x = 0; x < jarray.size(); x++) {
            jobject = jarray.get(x).getAsJsonObject();
            Campaign newCampaign = new Campaign();
            newCampaign.setId(jobject.get("id").toString());
            newCampaign.setState(jobject.get("state").toString());
            newCampaign.setAdvertiserID(advertiserID);
            newCampaign.setProfileID(jobject.get("profile_id").toString());
            newCampaign.setName(jobject.get("name").toString());
            newCampaign.setLineItemID(jobject.get("line_item_id").toString());
            campaignArrayList.add(newCampaign);
        }

    }

    public ArrayList<String> populateAdvertiserIDList (String rawData) {

        ArrayList<String> newAdvertiserList = new ArrayList<String>();

        JsonElement jelement = new JsonParser().parse(rawData);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        JsonArray jarray = jobject.getAsJsonArray("advertisers");
        String advertiserID;
        for (int x = 0; x < jarray.size(); x++) {
            jobject = jarray.get(x).getAsJsonObject();
            advertiserID = jobject.get("id").toString();
            newAdvertiserList.add(x, advertiserID);
        }

        return newAdvertiserList;
    }

}
