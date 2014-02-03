package com.mediacrossing.weeklyconversionreport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;


public class ConversionParser {

    public static ArrayList<ConversionAdvertiser> populateLiveAdvertiserList(String rawData) {
        ArrayList<ConversionAdvertiser> advertiserList = new ArrayList<ConversionAdvertiser>();
        JsonElement jsonElement = new JsonParser().parse(rawData);
        JsonArray advertiserJsonArray = jsonElement.getAsJsonArray();
        for (JsonElement advertiser : advertiserJsonArray) {
            JsonObject jsonObject = advertiser.getAsJsonObject();
            JsonArray ja = jsonObject.get("lineItemIds").getAsJsonArray();
            if (ja.size() > 0) {
                advertiserList.add(new ConversionAdvertiser(jsonObject.get("name").toString().replace("\"",""),
                        jsonObject.get("id").toString().replace("\"", ""),
                        jsonObject.get("status").toString().replace("\"", "")));
            }
        }
        return advertiserList;
    }
}
