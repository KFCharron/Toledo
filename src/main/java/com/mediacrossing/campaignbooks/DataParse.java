package com.mediacrossing.campaignbooks;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.ParseException;
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
            campaignList.add(new Campaign(
                    jsonObject.get("id").toString().replace("\"",""),
                    jsonObject.get("name").toString().replace("\"",""),
                    jsonObject.get("overallBudget").getAsFloat(),
                    jsonObject.get("startDate").toString().replace("\"",""),
                    jsonObject.get("endDate").toString().replace("\"",""),
                    jsonObject.get("dailyBudget").getAsFloat()
            ));
        }
        return campaignList;
    }
}
