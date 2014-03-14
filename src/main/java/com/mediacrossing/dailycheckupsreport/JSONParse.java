package com.mediacrossing.dailycheckupsreport;

import com.google.gson.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONParse {

    private static final Logger LOG = LoggerFactory.getLogger(JSONParse.class);

    public static String obtainReportId (String json) {
        JsonElement jelement = new JsonParser().parse(json);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        String reportId = jobject.get("report_id").toString().replace("\"", "");
        if (reportId == null) {
            LOG.error("ReportID not received.");
        }

        return reportId;
    }

    public static DateTime obtainLastModified(String rawData) {
        JsonElement jelement = new JsonParser().parse(rawData);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        jobject = jobject.getAsJsonObject("profile");
        String dateString = jobject.get("last_modified").toString().replace("\"", "");
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        return new DateTime(dtf.parseDateTime(dateString), DateTimeZone.UTC);

    }

    public static FrequencyTarget populateFrequencyTarget(String rawData) {

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

        return new FrequencyTarget(maxLifetimeImps, minSessionImps, maxSessionImps,
                maxDayImps, minMinPerImp, maxPageImps);
    }

    public static ArrayList<DaypartTarget> populateDaypartTarget(String rawData) {

        JsonElement jelement = new JsonParser().parse(rawData);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        jobject = jobject.getAsJsonObject("profile");
        ArrayList<DaypartTarget> newDaypartTarget = new ArrayList<DaypartTarget>();
        if (!jobject.get("daypart_targets").isJsonNull()) {
            JsonArray karray = jobject.getAsJsonArray("daypart_targets");
            for (int y = 0; y < karray.size(); y++) {
                JsonObject kobject = karray.get(y).getAsJsonObject();
                //add variables to DaypartTarget
                String day = kobject.get("day").toString().replace("\"", "");
                int start = kobject.get("start_hour").getAsInt();
                int end = kobject.get("end_hour").getAsInt();
                DaypartTarget newDaypart = new DaypartTarget(day, start, end);
                //add DaypartTargat to daypartTargetList
                newDaypartTarget.add(y, newDaypart);
            }
        }
        return newDaypartTarget;
    }

    public static GeographyTarget populateGeographyTarget(String rawData) {

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
                String country = lobject.get("country").toString().replace("\"", "");
                String name = lobject.get("name").toString().replace("\"", "");
                CountryTarget newCountry = new CountryTarget(country, name);
                countryTargetList.add(z, newCountry);
            }

        }

        //set country action
        String countryAction = jobject.get("country_action").toString().replace("\"", "");

        //Create new dma list
        ArrayList<DMATarget> dmaTargetList = new ArrayList<DMATarget>();

        //Check for null value
        if (!jobject.get("dma_targets").isJsonNull()) {
            //Move to dma target array
            JsonArray karray = jobject.getAsJsonArray("dma_targets");

            for (int i = 0; i < karray.size(); i++) {
                JsonObject pobject = karray.get(i).getAsJsonObject();
                String dma = pobject.get("dma").toString().replace("\"", "");
                String name = pobject.get("name").toString().replace("\"", "");
                DMATarget newDMATarget = new DMATarget(dma, name);
                dmaTargetList.add(i, newDMATarget);
            }

        }

        //set dma action
        String dmaAction = jobject.get("dma_action").toString().replace("\"", "");

        //Create new zip target list
        ArrayList<ZipTarget> zipTargetList = new ArrayList<ZipTarget>();

        if (!jobject.get("zip_targets").isJsonNull()) {
            //Move to zip target array
            JsonArray zarray = jobject.getAsJsonArray("zip_targets");

            for (int a = 0; a < zarray.size(); a++) {
                JsonObject zobject = zarray.get(a).getAsJsonObject();
                String fromZip = zobject.get("from_zip").toString().replace("\"", "");
                String toZip = zobject.get("to_zip").toString().replace("\"", "");
                ZipTarget newZipTarget = new ZipTarget(fromZip, toZip);
                zipTargetList.add(a, newZipTarget);

            }
        }

        return new GeographyTarget(countryTargetList, dmaTargetList, countryAction, dmaAction, zipTargetList);
    }

    public static ArrayList<Campaign> populateCampaignArrayList(String rawData) throws ParseException {

        ArrayList<Campaign> campaignArrayList1 = new ArrayList<Campaign>();
        JsonElement jelement = new JsonParser().parse(rawData);
        for (Map.Entry<String, JsonElement> entry : jelement.getAsJsonObject().entrySet()) {
            String currentAdvertiserID = entry.getKey().replace("\"", "");
            for (JsonElement values : entry.getValue().getAsJsonArray()) {
                Campaign newCampaign = new Campaign();
                JsonObject jsonObject = values.getAsJsonObject();
                newCampaign.setAdvertiserID(currentAdvertiserID);
                newCampaign.setId(jsonObject.get("id").toString().replace("\"", ""));
                newCampaign.setName(jsonObject.get("name").toString().replace("\"", ""));
                newCampaign.setProfileID(jsonObject.get("profileId").toString().replace("\"", ""));
                newCampaign.setLineItemID(jsonObject.get("lineItemId").toString().replace("\"", ""));
                newCampaign.setEndDate(jsonObject.get("endDate").toString().replace("\"", ""));
                if (!jsonObject.get("maxBid").isJsonNull())
                    newCampaign.setMaxBid(Float.parseFloat(jsonObject.get("maxBid").toString().replace("\"", "")));
                else newCampaign.setMaxBid(0);
                JsonArray jarray = jsonObject.getAsJsonArray("brokerFees");
                ArrayList<ServingFee> newServingFeeList = new ArrayList<ServingFee>();
                for (int x = 0; x < jarray.size(); x++) {
                    jsonObject = jarray.get(x).getAsJsonObject();
                    String brokerName = jsonObject.get("name").toString().replace("\"", "");
                    String paymentType = jsonObject.get("paymentType").toString().replace("\"", "");
                    String value = jsonObject.get("paymentValue").toString().replace("\"", "");
                    String description = jsonObject.get("description").toString().replace("\"", "");
                    ServingFee newServingFee = new ServingFee(brokerName, paymentType, value, description);
                    newServingFeeList.add(x, newServingFee);
                }
                newCampaign.setServingFeeList(newServingFeeList);
                campaignArrayList1.add(newCampaign);
            }
        }
        if (campaignArrayList1.isEmpty()) {
            LOG.error("Campaign List is empty after parse.");
        } else {
            LOG.info(campaignArrayList1.size() + " campaigns received.");
        }
        return campaignArrayList1;

    }

    public static ArrayList<SegmentGroupTarget> populateSegmentGroupTargetList(String rawData) {
        JsonElement jelement = new JsonParser().parse(rawData);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        jobject = jobject.getAsJsonObject("profile");
        ArrayList<SegmentGroupTarget> newSegmentGroupTargetList = new ArrayList<SegmentGroupTarget>();
        String segmentGroupBoolOp = jobject.get("segment_boolean_operator").toString().replace("\"", "");
        if (!jobject.get("segment_group_targets").isJsonNull()) {
            //Move to segment target array
            JsonArray jarray = jobject.getAsJsonArray("segment_group_targets");
            for (int x = 0; x < jarray.size(); x++) {

                JsonObject jsonObject = jarray.get(x).getAsJsonObject();
                String segmentBoolOp = jsonObject.get("boolean_operator").toString().replace("\"", "");
                ArrayList<Segment> newSegmentArrayList = new ArrayList<Segment>();
                if (!jsonObject.get("segments").isJsonNull()) {
                    JsonArray karray = jsonObject.getAsJsonArray("segments");
                    for (int y = 0; y < karray.size(); y++) {

                        JsonObject kobject = karray.get(y).getAsJsonObject();
                        String action = kobject.get("action").toString().replace("\"", "");
                        String id = kobject.get("id").toString().replace("\"", "");
                        String name = kobject.get("name").toString().replace("\"", "");
                        String code = kobject.get("code").toString().replace("\"", "");
                        Segment newSegment = new Segment(id, name, action, segmentBoolOp, code);
                        newSegmentArrayList.add(y, newSegment);
                    }
                }
                SegmentGroupTarget newSegmentGroupTarget =
                        new SegmentGroupTarget(segmentGroupBoolOp, newSegmentArrayList);
                newSegmentGroupTargetList.add(x, newSegmentGroupTarget);
            }

        }

        return newSegmentGroupTargetList;
    }

    public static String obtainLineItemName (String rawData, String lineItemId) {
       JsonElement je = new JsonParser().parse(rawData);
       JsonArray ja = je.getAsJsonArray();
       for (JsonElement jEl : ja) {
           JsonObject jo = jEl.getAsJsonObject();
           if(jo.get("id").toString().replace("\"", "").equals(lineItemId)) {
               return jo.get("name").toString().replace("\"", "");
           }
       }
       return "Not found";
    }

    public static String obtainAdvertiserName (String rawData) {
        JsonElement je = new JsonParser().parse(rawData);
        JsonObject jo = je.getAsJsonObject();
        return jo.get("name").toString().replace("\"", "");
    }

    public static ArrayList<String> obtainLineItemArray (String rawData) {
        ArrayList<String> liArray = new ArrayList<String>();
        JsonElement je = new JsonParser().parse(rawData);
        JsonObject jo = je.getAsJsonObject();
        JsonArray ja = jo.get("lineItemIds").getAsJsonArray();
        for(JsonElement jEl : ja) {
            liArray.add(jEl.getAsString().replace("\"", ""));
        }
        return liArray;
    }

    public static String obtainToken (String raw) {
        LOG.debug(raw);
        JsonElement jelement = new JsonParser().parse(raw);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        String token = "";
        try {
            token = jobject.get("token").toString();
        }catch (NullPointerException e) {
            LOG.error("NO TOKEN RECEIVED");
        }
        token = token.replace("\"", "");

        return token;
    }

}
