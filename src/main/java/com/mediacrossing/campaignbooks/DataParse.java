package com.mediacrossing.campaignbooks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class DataParse {

    private String reportUrl;

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }

    public List<Advertiser> populateAdvertiserList (String rawData) {
        List<Advertiser> advertiserList = new ArrayList<Advertiser>();
        JsonElement jsonElement = new JsonParser().parse(rawData);
        JsonArray advertiserJsonArray = jsonElement.getAsJsonArray();
        for (JsonElement advertiser : advertiserJsonArray) {
            JsonObject jsonObject = advertiser.getAsJsonObject();
            advertiserList.add(new Advertiser(jsonObject.get("id").toString().replace("\"","")));
        }
        return advertiserList;

    }

    public List<LineItem> populateLineItemList (String rawData) {
        List<LineItem> lineItemList = new ArrayList<LineItem>();
        JsonElement jsonElement = new JsonParser().parse(rawData);
        for (JsonElement lineItem : jsonElement.getAsJsonArray()) {
            JsonObject jsonObject = lineItem.getAsJsonObject();
            lineItemList.add(new LineItem(
                    jsonObject.get("id").toString().replace("\"",""),
                    jsonObject.get("name").toString().replace("\"",""),
                    jsonObject.get("startDate").toString().replace("\"",""),
                    jsonObject.get("endDate").toString().replace("\"",""),
                    jsonObject.get("overallBudget").toString().replace("\"",""),
                    jsonObject.get("dailyBudget").toString().replace("\"","")
            ));
        }
        return lineItemList;
    }

    public List<Campaign> populateCampaignList (String rawData) throws ParseException {
        List<Campaign> campaignList = new ArrayList<Campaign>();
        JsonElement jsonElement = new JsonParser().parse(rawData);
        JsonArray campaignJsonArray = jsonElement.getAsJsonArray();
        for (JsonElement campaign : campaignJsonArray) {
            JsonObject jsonObject = campaign.getAsJsonObject();
            String id = jsonObject.get("id").toString().replace("\"", "");
            String name = jsonObject.get("name").toString().replace("\"", "");
            float overallBudget = 0;
            if(!jsonObject.get("overallBudget").isJsonNull()) {
                overallBudget = jsonObject.get("overallBudget").getAsFloat();
            }
            String startDate = jsonObject.get("startDate").toString().replace("\"","");
            String endDate = jsonObject.get("endDate").toString().replace("\"","");
            float dailyBudget = 0;
            if(!jsonObject.get("dailyBudget").isJsonNull()) {
                dailyBudget = jsonObject.get("dailyBudget").getAsFloat();
            }
            campaignList.add(new Campaign(id, name, overallBudget, startDate, endDate, dailyBudget));
        }
        return campaignList;
    }

    public boolean parseReportStatus(String rawData) {
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
}
