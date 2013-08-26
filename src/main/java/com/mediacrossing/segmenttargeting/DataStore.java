package com.mediacrossing.segmenttargeting;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: charronkyle
 * Date: 8/26/13
 * Time: 9:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class DataStore {
    private ArrayList<Campaign> campaignArrayList;

    public ArrayList<Campaign> getCampaignArrayList() {
        return campaignArrayList;
    }

    public void setCampaignArrayList(ArrayList<Campaign> campaignArrayList) {
        this.campaignArrayList = campaignArrayList;
    }

    public void saveAllCampaigns(String rawJSON) {

    }
}
