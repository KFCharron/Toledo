package com.mediacrossing.dailycheckupsreport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DataStore {
    private ArrayList<Campaign> campaignArrayList;
    private ArrayList<Campaign> liveCampaignArrayList = new ArrayList<Campaign>();

    public ArrayList<Campaign> getCampaignArrayList() {
        return campaignArrayList;
    }

    public void setCampaignArrayList(ArrayList<Campaign> campaignArrayList) throws ParseException {
        this.campaignArrayList = campaignArrayList;

    }

    //returns a new list of live campaigns out of all campaigns
    public ArrayList<Campaign> getLiveCampaignArrayList() {
        return liveCampaignArrayList;
    }

    public void setLiveCampaignArrayList() throws ParseException {
        //builds live campaign list
        for (Campaign c : campaignArrayList) {
            if (!c.getEndDate().equals("null")) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Date campaignEndDate = sdf.parse(c.getEndDate());
                Date now = new Date();
                if(now.getTime() < campaignEndDate.getTime()) {
                    liveCampaignArrayList.add(c);
                }
            }
        }
    }

    public void setLiveCampaignArrayList(ArrayList<Campaign> campaignArrayList) {
        this.liveCampaignArrayList = campaignArrayList;
    }
}
