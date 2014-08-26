package com.mediacrossing.campaignbooks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mediacrossing.publisherreporting.Publisher;
import com.mediacrossing.dailypacingreport.PacingLineItem;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class DataParse {

    private static String reportUrl;

    public static String getReportUrl() {
        return reportUrl;
    }

    public static boolean parsePutResponse(String response) {
        JsonElement jsonElement = new JsonParser().parse(response);
        JsonObject jsonObject = jsonElement.getAsJsonObject().getAsJsonObject("response");
        return true;
    }

    public static boolean parseSegment(String rawData) {
        JsonElement je = new JsonParser().parse(rawData);
        JsonObject jsonObject = je.getAsJsonObject().getAsJsonObject("response");
        if(!jsonObject.get("count").toString().equals("0")) return true; else return false;
    }

    public static List<Advertiser> populateAdvertiserList (String rawData) {
        List<Advertiser> advertiserList = new ArrayList<>();
        JsonElement jsonElement = new JsonParser().parse(rawData);
        JsonArray advertiserJsonArray = jsonElement.getAsJsonArray();
        for (JsonElement advertiser : advertiserJsonArray) {
            JsonObject jsonObject = advertiser.getAsJsonObject();
            advertiserList.add(new Advertiser(jsonObject.get("id").toString().replace("\"",""),
                    jsonObject.get("name").toString().replace("\"", ""),
                    jsonObject.get("status").toString().replace("\"", "")));
        }
        return advertiserList;

    }

    public static ArrayList<LineItem> populateLineItemList(String rawData) throws ParseException {
        ArrayList<LineItem> lineItemList = new ArrayList<>();
        JsonElement jsonElement = new JsonParser().parse(rawData);
        for (JsonElement lineItem : jsonElement.getAsJsonArray()) {
            JsonObject jsonObject = lineItem.getAsJsonObject();
            lineItemList.add(new LineItem(
                    jsonObject.get("id").toString().replace("\"",""),
                    jsonObject.get("name").toString().replace("\"",""),
                    jsonObject.get("startDate").toString().replace("\"",""),
                    jsonObject.get("endDate").toString().replace("\"",""),
                    jsonObject.get("lifetimeBudget").toString().replace("\"",""),
                    jsonObject.get("dailyBudget").toString().replace("\"",""),
                    jsonObject.get("status").toString().replace("\"", ""),
                    jsonObject.get("lifetimeBudgetImps").toString().replace("\"", ""),
                    jsonObject.get("dailyBudgetImps").toString().replace("\"", "")
            ));
        }
        return lineItemList;
    }

    public static ArrayList<Campaign> populateCampaignList(String rawData) throws ParseException {
        ArrayList<Campaign> campaignList = new ArrayList<>();
        JsonElement jsonElement = new JsonParser().parse(rawData);
        JsonArray campaignJsonArray = jsonElement.getAsJsonArray();
        for (JsonElement campaign : campaignJsonArray) {
            JsonObject jsonObject = campaign.getAsJsonObject();
            String id = jsonObject.get("id").toString().replace("\"", "");
            String name = jsonObject.get("name").toString().replace("\"", "");
            String status = jsonObject.get("status").toString().replace("\"", "");
            float lifetimeBudget = 0;
            if(!jsonObject.get("lifetimeBudget").isJsonNull()) {
                lifetimeBudget = jsonObject.get("lifetimeBudget").getAsFloat();
            }
            String startDate = jsonObject.get("startDate").toString().replace("\"","");
            String endDate = jsonObject.get("endDate").toString().replace("\"","");
            float dailyBudget = 0;
            if(!jsonObject.get("dailyBudget").isJsonNull()) {
                dailyBudget = jsonObject.get("dailyBudget").getAsFloat();
            }
            int lifetimeImp = 0;
            if(!jsonObject.get("lifetimeBudgetImps").isJsonNull()) {
                lifetimeImp = jsonObject.get("lifetimeBudgetImps").getAsInt();
            }
            int dailyImp = 0;
            if(!jsonObject.get("dailyBudgetImps").isJsonNull()) {
                dailyImp = jsonObject.get("dailyBudgetImps").getAsInt();
            }
            campaignList.add(new
                    Campaign(id, name, status, lifetimeBudget, startDate, endDate, dailyBudget, lifetimeImp, dailyImp));
        }
        return campaignList;
    }



    public static boolean parseReportStatus(String rawData) {
        JsonElement jsonElement = new JsonParser().parse(rawData);
        JsonObject jsonObject = jsonElement.getAsJsonObject().getAsJsonObject("response");
        if(jsonObject.get("execution_status").toString().equals("\"ready\"")) {
            jsonObject = jsonObject.getAsJsonObject("report");
            reportUrl = jsonObject.get("url").toString().replace("\"","");
            return true;
        }
        else
            return false;
    }

    public static ArrayList<Publisher> parsePublisherIdAndName(String rawData) {
        ArrayList<Publisher> pl = new ArrayList<>();
        JsonElement je = new JsonParser().parse(rawData);
        JsonObject jo = je.getAsJsonObject().getAsJsonObject("response");
        JsonArray ja = jo.getAsJsonArray("publishers");
        for(JsonElement jsonElement : ja) {
            jo = jsonElement.getAsJsonObject();
            pl.add(new Publisher(jo.get("id").toString().replace("\"", ""),
                    jo.get("name").toString().replace("\"", ""),
                    jo.get("last_modified").toString().replace("\"", ""),
                    jo.get("state").toString().replace("\"", "")));
        }
        return pl;
    }

    public static ArrayList<String> parseProfiles (String rawData) throws ParseException {
        ArrayList<String> pros = new ArrayList<>();
        JsonElement je = new JsonParser().parse(rawData);
        JsonObject jo = je.getAsJsonObject().getAsJsonObject("response");
        JsonArray ja = jo.getAsJsonArray("profiles");
        for(JsonElement jsonElement : ja) {
            jo = jsonElement.getAsJsonObject();
            pros.add(jo.get("id").toString().replace("\"", ""));
        }
        return pros;
    }

    public static ArrayList<LineItem> parseLineItems(String rawData) throws ParseException {
        ArrayList<LineItem> liList = new ArrayList<>();
        JsonElement je = new JsonParser().parse(rawData);
        JsonObject jo = je.getAsJsonObject().getAsJsonObject("response");
        JsonArray ja = jo.getAsJsonArray("line-items");
        for(JsonElement jsonElement : ja) {
            jo = jsonElement.getAsJsonObject();
            liList.add(new LineItem(
                    jo.get("id").toString().replace("\"",""),
                    jo.get("name").toString().replace("\"",""),
                    jo.get("start_date").toString().replace("\"",""),
                    jo.get("end_date").toString().replace("\"",""),
                    jo.get("lifetime_budget").toString().replace("\"",""),
                    jo.get("daily_budget").toString().replace("\"",""),
                    jo.get("state").toString().replace("\"", ""),
                    jo.get("lifetime_budget_imps").toString().replace("\"", ""),
                    jo.get("daily_budget_imps").toString().replace("\"", "")
            ));
        }
        return liList;
    }

    public static ArrayList<PacingLineItem> parsePacingLineItems(String rawData) throws ParseException {
        ArrayList<PacingLineItem> lis = new ArrayList<>();
        JsonElement je = new JsonParser().parse(rawData);
        JsonArray ja = je.getAsJsonArray();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss'Z'");
        for (JsonElement e : ja) {
            JsonObject li = e.getAsJsonObject();
            if (!li.get("advertiserId").isJsonNull() &&
                !li.get("name").isJsonNull() &&
                !li.get("startDate").isJsonNull() &&
                !li.get("endDate").isJsonNull()) {
                int lifetimeBudget;
                if(li.get("lifetimeBudgetImps").isJsonNull()) lifetimeBudget = 0;
                else lifetimeBudget = Integer.parseInt(li.get("lifetimeBudgetImps").getAsString().replace("\"",""));
                int dailyBudget;
                if(li.get("dailyBudgetImps").isJsonNull()) dailyBudget = 0;
                else dailyBudget = Integer.parseInt(li.get("dailyBudgetImps").getAsString().replace("\"",""));
                lis.add(new PacingLineItem(li.get("id").getAsString().replace("\"",""),
                                            li.get("advertiserId").getAsString().replace("\"",""),
                                            li.get("name").getAsString().replace("\"",""),
                                            dtf.parseDateTime(li.get("startDate").getAsString().replace("\"","")),
                                            dtf.parseDateTime(li.get("endDate").getAsString().replace("\"","")),
                                            lifetimeBudget,
                                            dailyBudget,
                                            li.get("status").getAsString().replace("\"","")
                                          )
                );
            }

        }
        return lis;
    }

    public static ArrayList<Campaign> parseCampaigns(String rawData, String liId) throws ParseException {
        ArrayList<Campaign> cList = new ArrayList<>();
        JsonElement je = new JsonParser().parse(rawData);
        JsonObject jo = je.getAsJsonObject().getAsJsonObject("response");
        JsonArray ja = jo.getAsJsonArray("campaigns");
        for(JsonElement jsonElement : ja) {
            jo = jsonElement.getAsJsonObject();
            float ltBudget = 0;
            if(!jo.get("lifetime_budget").isJsonNull()) {
                ltBudget = Float.parseFloat(jo.get("lifetime_budget").toString().replace("\"",""));
            }
            float dailyBudget = 0;
            if(!jo.get("daily_budget").isJsonNull()) {
                dailyBudget = Float.parseFloat(jo.get("daily_budget").toString().replace("\"",""));
            }
            int ltImpBudget = 0;
            if(!jo.get("lifetime_budget_imps").isJsonNull()) {
                dailyBudget = Integer.parseInt(jo.get("lifetime_budget_imps").toString().replace("\"",""));
            }
            int dailyImpBudget = 0;
            if(!jo.get("daily_budget_imps").isJsonNull()) {
                dailyBudget = Integer.parseInt(jo.get("daily_budget_imps").toString().replace("\"",""));
            }


            if (jo.get("line_item_id").toString().replace("\"","").equals(liId)) {
                cList.add(new Campaign(
                        jo.get("id").toString().replace("\"", ""),
                        jo.get("name").toString().replace("\"",""),
                        jo.get("state").toString().replace("\"", ""),
                        ltBudget,
                        jo.get("start_date").toString().replace("\"",""),
                        jo.get("end_date").toString().replace("\"",""),
                        dailyBudget,
                        ltImpBudget,
                        dailyImpBudget
                ));
            }
        }
        return cList;
    }
}
