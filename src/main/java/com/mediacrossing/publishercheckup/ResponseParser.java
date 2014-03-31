package com.mediacrossing.publishercheckup;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mediacrossing.discrepancyreport.Creative;
import com.mediacrossing.monthlybillingreport.BillingAdvertiser;
import com.mediacrossing.monthlybillingreport.BillingCampaign;
import com.mediacrossing.weeklydomainreport.Domain;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

public class ResponseParser {

    public static ArrayList<Creative> parseCreatives(String json) {
        ArrayList<Creative> creatives = new ArrayList<>();
        JsonElement jelement = new JsonParser().parse(json);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        if(!jobject.get("creatives").isJsonNull()) {
            JsonArray jarray = jobject.getAsJsonArray("creatives");
            for (JsonElement j : jarray) {
                JsonObject jo = j.getAsJsonObject();
                String id = jo.get("id").toString().replace("\"", "");
                String name = jo.get("name").toString().replace("\"", "");
                String originalContent = jo.get("original_content").toString().replace("\"", "");
                List<String> campaigns = new ArrayList<>();
                if(!jo.get("campaigns").isJsonNull()) {
                    JsonArray ja = jo.getAsJsonArray("campaigns");
                    for (JsonElement el : ja) {
                        JsonObject job = el.getAsJsonObject();
                        campaigns.add(job.get("campaign_id").toString().replace("\"", ""));
                    }
                }
                creatives.add(new Creative(name, id, originalContent, campaigns));
            }
        }
        return creatives;
    }

    public static ArrayList<Placement> parsePlacements(String json) {
        ArrayList<Placement> placements = new ArrayList<>();
        JsonElement jelement = new JsonParser().parse(json);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        if(!jobject.get("placements").isJsonNull()) {
            JsonArray jarray = jobject.getAsJsonArray("placements");
            for (JsonElement j : jarray) {
                JsonObject jo = j.getAsJsonObject();
                if (jo.get("state").toString().replace("\"","").equals("active")) {
                    String id = jo.get("id").toString().replace("\"", "");
                    String name = jo.get("name").toString().replace("\"", "");
                    String state = jo.get("state").toString().replace("\"", "");
                    String lastModified = jo.get("last_modified").toString().replace("\"", "");
                    ArrayList<IdName> filteredAdvertisers = new ArrayList<>();
                    if(!jo.get("filtered_advertisers").isJsonNull()) {
                        JsonArray ja = jo.getAsJsonArray("filtered_advertisers");
                        for (JsonElement el : ja) {
                            JsonObject job = el.getAsJsonObject();
                            filteredAdvertisers.add(new IdName(job.get("id").toString().replace("\"", ""),
                                    job.get("name").toString().replace("\"", "")));
                        }
                    }
                    ArrayList<IdName> contentCategories = new ArrayList<>();
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
        }
        return placements;
    }

    public static ArrayList<Domain> parseDomainReport (List<String[]> csvData) {
        ArrayList<Domain> domains = new ArrayList<>();
        csvData.remove(0);
        for (String[] l : csvData) {
            domains.add(new Domain(l[0], Double.parseDouble(l[1]), Integer.parseInt(l[2]), l[3],
                    Double.parseDouble(l[4]), Double.parseDouble(l[5]), Double.parseDouble(l[6]),
                    Double.parseDouble(l[7]), Double.parseDouble(l[8]), Double.parseDouble(l[9]),
                    Integer.parseInt(l[10]), Double.parseDouble(l[11]), Integer.parseInt(l[12]),
                    Double.parseDouble(l[13]), Integer.parseInt(l[14]), Double.parseDouble(l[15]),
                    Double.parseDouble(l[16]), Double.parseDouble(l[17])));
        }
        return domains;
    }

    public static ArrayList<PaymentRule> parsePaymentRules(String json) {
        ArrayList<PaymentRule> paymentRules = new ArrayList<>();
        JsonElement jelement = new JsonParser().parse(json);
        JsonObject jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        if(!jobject.get("payment-rules").isJsonNull()) {
            JsonArray jarray = jobject.getAsJsonArray("payment-rules");
            for (JsonElement j : jarray) {
                JsonObject jo = j.getAsJsonObject();
                if (jo.get("state").toString().replace("\"","").equals("active")) {
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
        }
        return paymentRules;
    }

    public static ArrayList<YMProfile> parseYmProfiles(String json) {
        ArrayList<YMProfile> ymProfiles = new ArrayList<>();
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
                ArrayList<FloorRule> floorRules = new ArrayList<>();
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
                        ArrayList<IdName> audienceList = new ArrayList<>();
                        if (!job.get("members").isJsonNull()) {
                            JsonArray jArray1 = job.getAsJsonArray("members");
                            for (JsonElement j1 : jArray1) {
                                JsonObject object = j1.getAsJsonObject();
                                audienceList.add(new IdName(object.get("id").getAsString().replace("\"", ""),
                                        object.get("name").getAsString().replace("\"", "")));
                            }
                        }
                        ArrayList<IdName> supplyList = new ArrayList<>();
                        if (!job.get("brands").isJsonNull()) {
                            JsonArray jArray1 = job.getAsJsonArray("brands");
                            for (JsonElement j1 : jArray1) {
                                JsonObject object = j1.getAsJsonObject();
                                supplyList.add(new IdName(object.get("id").getAsString().replace("\"", ""),
                                        object.get("name").getAsString().replace("\"", "")));
                            }
                        }
                        ArrayList<IdName> demandList = new ArrayList<>();
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

    public static ArrayList<BillingAdvertiser> parseBillingReport (List<String[]> csvData) {
        csvData.remove(0);
        ArrayList<BillingAdvertiser> ads = new ArrayList<>();
        for (String[] l : csvData) {
            BillingCampaign camp = new BillingCampaign(l[2], l[3], Integer.parseInt(l[4]),
                    Integer.parseInt(l[5]),
                    Integer.parseInt(l[6]),
                    Float.parseFloat(l[7]),
                    Float.parseFloat(l[8]), Float.parseFloat(l[9]));
            boolean saved = false;
            for (BillingAdvertiser ad : ads) {
                //If ad Id is zero, it's sell side, don't save it.
                if (l[0].equals(ad.getId())) {
                    boolean campSaved = false;
                    for (BillingCampaign c : ad.getCampaigns()) {
                        if (c.getId().equals(camp.getId()) && !camp.getId().equals("0")) {
                            campSaved = true;
                            c.setImps(c.getImps() + camp.getImps());
                            c.setClicks(c.getClicks() + camp.getClicks());
                            c.setConvs(c.getConvs() + camp.getConvs());
                            c.setMediaCost(c.getMediaCost() + camp.getMediaCost());
                            c.setNetworkRevenue(c.getNetworkRevenue() + camp.getNetworkRevenue());
                            if (l[10].equals("181")) c.setAdXMediaCost(camp.getMediaCost());
                            else if (l[10].equals("1770")) c.setMxMediaCost(camp.getMediaCost());
                            else c.setAppNexusMediaCost(c.getAppNexusMediaCost() + camp.getMediaCost());
                        }
                    }
                    if (!campSaved && !camp.getId().equals("0")) {
                        if (l[10].equals("181")) camp.setAdXMediaCost(camp.getMediaCost());
                        else if (l[10].equals("1770")) camp.setMxMediaCost(camp.getMediaCost());
                        else camp.setAppNexusMediaCost(camp.getMediaCost());
                        ad.getCampaigns().add(camp);
                    }
                    saved = true;
                }
            }
            if (!saved) {
                if(!l[0].equals("0")){
                    BillingAdvertiser ad = new BillingAdvertiser(l[1], l[0]);
                    if (l[10].equals("181")) camp.setAdXMediaCost(camp.getMediaCost());
                    else if (l[10].equals("1770")) camp.setMxMediaCost(camp.getMediaCost());
                    else camp.setAppNexusMediaCost(camp.getMediaCost());
                    ad.getCampaigns().add(camp);
                    ads.add(ad);
                }
            }
        }
        return ads;
    }

    public static float parseResoldRevenue (List<String[]> csvData) {
        csvData.remove(0);
        for (String[] l : csvData) {
            if(l[0].equals("6")) {
                return Float.parseFloat(l[1]);
            }
        }
        return 0;
    }

    public static ArrayList<BillingAdvertiser> parseCreativeBillingReport (List<String[]> csvData) {
        csvData.remove(0);
        ArrayList<BillingAdvertiser> ads = new ArrayList<>();
        for (String[] l : csvData) {
            BillingCampaign camp = new BillingCampaign(l[2], l[3], Integer.parseInt(l[4]),
                    Integer.parseInt(l[5]),
                    Integer.parseInt(l[6]),
                    Float.parseFloat(l[7]),
                    Float.parseFloat(l[8]), Float.parseFloat(l[9]), l[10], l[11]);
            boolean saved = false;
            for (BillingAdvertiser ad : ads) {
                //If ad Id is zero, it's sell side, don't save it.
                if (l[0].equals(ad.getId())) {
                    if (l[2].equals("0")) saved = true;
                    else {
                        ad.getCampaigns().add(camp);
                        saved = true;
                    }
                }
            }
            if (!saved) {
                if(!l[0].equals("0")){
                    BillingAdvertiser ad = new BillingAdvertiser(l[1], l[0]);
                    ad.getCampaigns().add(camp);
                    ads.add(ad);
                }
            }
        }
        return ads;
    }

    public static ArrayList<Tuple2<String, String>> parseTopBrandsOrBuyers(List<String[]> csvData) {

        csvData.remove(0);
        ArrayList<Tuple2<String, String>> brands = new ArrayList<>();

        int count = 0;
        for (String[] l : csvData) {
           if (count < 10) {
               brands.add(new Tuple2(l[0], l[1]));
               count++;
           }
        }
        for (int x = count; x < 10; x++) brands.add(new Tuple2("","0"));

        return brands;
    }


}
