package com.mediacrossing.publishercheckup;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mediacrossing.monthlybillingreport.BillingAdvertiser;
import com.mediacrossing.monthlybillingreport.BillingCampaign;
import com.mediacrossing.weeklypublisherreport.DailyPublisherData;
import com.mediacrossing.weeklypublisherreport.WeeklyPlacement;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class ResponseParser {

    public static ArrayList<Placement> parsePlacements(String json) {
        ArrayList<Placement> placements = new ArrayList<Placement>();
        JsonElement jelement = new JsonParser().parse(json);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        if(!jobject.get("placements").isJsonNull()) {
            JsonArray jarray = jobject.getAsJsonArray("placements");
            for (JsonElement j : jarray) {
                JsonObject jo = j.getAsJsonObject();
                String id = jo.get("id").toString().replace("\"", "");
                String name = jo.get("name").toString().replace("\"", "");
                String state = jo.get("state").toString().replace("\"", "");
                String lastModified = jo.get("last_modified").toString().replace("\"", "");
                ArrayList<IdName> filteredAdvertisers = new ArrayList<IdName>();
                if(!jo.get("filtered_advertisers").isJsonNull()) {
                    JsonArray ja = jo.getAsJsonArray("filtered_advertisers");
                    for (JsonElement el : ja) {
                        JsonObject job = el.getAsJsonObject();
                        filteredAdvertisers.add(new IdName(job.get("id").toString().replace("\"", ""),
                                job.get("name").toString().replace("\"", "")));
                    }
                }
                ArrayList<IdName> contentCategories = new ArrayList<IdName>();
                if(!jo.get("content_categories").isJsonNull()) {
                    JsonArray jarr = jo.getAsJsonArray("content_categories");
                    for (JsonElement el : jarr) {
                        JsonObject job = el.getAsJsonObject();
                        String ccId = job.get("id").toString().replace("\"", "");
                        String ccName = job.get("name").toString().replace("\"", "");
                        contentCategories.add(new IdName(ccId, ccName));
                    }
                }
                placements.add(new Placement(id, name, state, filteredAdvertisers, contentCategories, lastModified));
            }
        }
        return placements;
    }

    public static ArrayList<PaymentRule> parsePaymentRules(String json) {
        ArrayList<PaymentRule> paymentRules = new ArrayList<PaymentRule>();
        JsonElement jelement = new JsonParser().parse(json);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        if(!jobject.get("payment-rules").isJsonNull()) {
            JsonArray jarray = jobject.getAsJsonArray("payment-rules");
            for (JsonElement j : jarray) {
                JsonObject jo = j.getAsJsonObject();
                String id = jo.get("id").toString().replace("\"", "");
                String name = jo.get("name").toString().replace("\"", "");
                String state = jo.get("state").toString().replace("\"", "");
                String lastModified = jo.get("last_modified").toString().replace("\"", "");
                String pricingType = jo.get("pricing_type").toString().replace("\"", "");
                double revshare;
                if (jo.get("revshare").isJsonNull()) revshare = 0;
                else revshare = Double.parseDouble(jo.get("revshare").toString().replace("\"", ""));
                String priority = jo.get("priority").toString().replace("\"", "");

                paymentRules.add(new PaymentRule(id, name, state, pricingType, revshare, priority, lastModified));
            }
        }
        return paymentRules;
    }

    public static ArrayList<YMProfile> parseYmProfiles(String json) {
        ArrayList<YMProfile> ymProfiles = new ArrayList<YMProfile>();
        JsonElement jelement = new JsonParser().parse(json);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        if (!jobject.get("ym-profiles").isJsonNull()) {
            JsonArray jarray = jobject.getAsJsonArray("ym-profiles");
            for (JsonElement j : jarray) {
                JsonObject jo = j.getAsJsonObject();
                String id = jo.get("id").toString().replace("\"", "");
                String name = jo.get("name").toString().replace("\"", "");
                String description = jo.get("description").toString().replace("\"", "");
                String lastModified = jo.get("last_modified").toString().replace("\"", "");
                ArrayList<FloorRule> floorRules = new ArrayList<FloorRule>();
                if (!jo.get("floors").isJsonNull()) {
                    JsonArray jsArray = jo.getAsJsonArray("floors");
                    for (JsonElement je : jsArray) {
                        JsonObject job = je.getAsJsonObject();
                        String floorId = job.get("id").toString().replace("\"", "");
                        String floorName = job.get("name").toString().replace("\"", "");
                        String floorDescription = job.get("description").toString().replace("\"", "");
                        String floorCode = job.get("code").toString().replace("\"", "");
                        Float hardFloor;
                        if (job.get("hard_floor").isJsonNull()) hardFloor = 0f;
                        else hardFloor = Float.parseFloat(job.get("hard_floor").toString());
                        Float softFloor;
                        if (job.get("soft_floor").isJsonNull()) softFloor = 0f;
                        else softFloor = Float.parseFloat(job.get("soft_floor").toString());
                        String priority = job.get("priority").toString().replace("\"", "");
                        ArrayList<IdName> audienceList = new ArrayList<IdName>();
                        if (!job.get("members").isJsonNull()) {
                            JsonArray jArray1 = job.getAsJsonArray("members");
                            for (JsonElement j1 : jArray1) {
                                JsonObject object = j1.getAsJsonObject();
                                audienceList.add(new IdName(object.get("id").getAsString().replace("\"", ""),
                                        object.get("name").getAsString().replace("\"", "")));
                            }
                        }
                        ArrayList<IdName> supplyList = new ArrayList<IdName>();
                        if (!job.get("brands").isJsonNull()) {
                            JsonArray jArray1 = job.getAsJsonArray("brands");
                            for (JsonElement j1 : jArray1) {
                                JsonObject object = j1.getAsJsonObject();
                                supplyList.add(new IdName(object.get("id").getAsString().replace("\"", ""),
                                        object.get("name").getAsString().replace("\"", "")));
                            }
                        }
                        ArrayList<IdName> demandList = new ArrayList<IdName>();
                        if (!job.get("categories").isJsonNull()) {
                            JsonArray jArray1 = job.getAsJsonArray("categories");
                            for (JsonElement j1 : jArray1) {
                                JsonObject object = j1.getAsJsonObject();
                                demandList.add(new IdName(object.get("id").getAsString().replace("\"", ""),
                                        object.get("name").getAsString().replace("\"", "")));
                            }
                        }
                        floorRules.add(new FloorRule(floorId, floorName, floorDescription, floorCode,
                                hardFloor, softFloor, priority, audienceList, supplyList, demandList));
                    }
                }
                ymProfiles.add(new YMProfile(id, name, description, floorRules, lastModified));
            }
        }
        return ymProfiles;
    }

    public static ArrayList<WeeklyPlacement> parsePlacementReport (List<String[]> csvData) {

        csvData.remove(0);
        ArrayList<WeeklyPlacement> placements = new ArrayList<WeeklyPlacement>();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");

        for (String[] l : csvData) {
            DailyPublisherData data = new DailyPublisherData();
            data.setDate(new DateTime(dtf.parseDateTime(l[0])));
            data.setAvails(Integer.parseInt(l[3]));
            data.setImps(Integer.parseInt(l[4]) + Integer.parseInt(l[5]));
            data.setUnfilled(Integer.parseInt(l[6]) + Integer.parseInt(l[7]) + Integer.parseInt(l[8]));
            data.setErrors(Integer.parseInt(l[9]) + Integer.parseInt(l[10]) + Integer.parseInt(l[11]));
            data.seteCpm(Float.parseFloat(l[12]));
            data.setGross(Float.parseFloat(l[13]));
            Boolean found = false;
            for (WeeklyPlacement p : placements) {
                if (p.getId().equals(l[1])) {
                    p.getDailyDataList().add(data);
                    found = true;
                }
            }
            if (!found) {
                WeeklyPlacement p = new WeeklyPlacement(l[1], l[2]);
                p.getDailyDataList().add(data);
                placements.add(p);
            }
        }
        return placements;
    }

    public static ArrayList<BillingAdvertiser> parseBillingReport (List<String[]> csvData) {
        csvData.remove(0);
        ArrayList<BillingAdvertiser> ads = new ArrayList<BillingAdvertiser>();
        for (String[] l : csvData) {
            BillingCampaign camp = new BillingCampaign(l[2], l[3], Integer.parseInt(l[4]),
                    Integer.parseInt(l[5]),
                    Integer.parseInt(l[6]),
                    Float.parseFloat(l[7]),
                    Float.parseFloat(l[8]));
            boolean saved = false;
            //FIXME
            for (BillingAdvertiser ad : ads) {
               if (l[0].equals(ad.getId())) {
                   ad.getCampaigns().add(camp);
                   saved = true;
               }
            }
            if (!saved) {
                BillingAdvertiser ad = new BillingAdvertiser(l[1], l[0]);
                ad.getCampaigns().add(camp);
                ads.add(ad);
            }
        }
        return ads;
    }

    public static ArrayList<String> parseTopBrandsOrBuyers(List<String[]> csvData) {

        csvData.remove(0);
        ArrayList<String> brandNames = new ArrayList<String>();

        int count = 0;
        for (String[] l : csvData) {
           if (count < 10) {
               brandNames.add(l[0]);
               count++;
           }
        }

        return brandNames;
    }


}
