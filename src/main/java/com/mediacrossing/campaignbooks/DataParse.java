package com.mediacrossing.campaignbooks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class DataParse {

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
        JsonArray lineItemJsonArray = jsonElement.getAsJsonArray();
        for (JsonElement lineItem : lineItemJsonArray) {
            JsonObject jsonObject = lineItem.getAsJsonObject();
            JsonArray campaignJsonArray = jsonObject.get("campaignIds").getAsJsonArray();
            List<Campaign> campaignList = new ArrayList<Campaign>();
            for (JsonElement campaign : campaignJsonArray) {
                Campaign newCampaign = new Campaign(campaign.getAsString());
                campaignList.add(newCampaign);
            }
            lineItemList.add(new LineItem(
                    jsonObject.get("id").toString().replace("\"",""),
                    jsonObject.get("name").toString().replace("\"",""),
                    jsonObject.get("startDate").toString().replace("\"",""),
                    jsonObject.get("endDate").toString().replace("\"",""),
                    jsonObject.get("overallBudget").toString().replace("\"",""),
                    jsonObject.get("dailyBudget").toString().replace("\"",""),
                    campaignList
            ));
        }
        return lineItemList;
    }
}
