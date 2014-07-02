package com.mediacrossing.anupload;

import java.util.ArrayList;

public class DmaCampaign {

    private String campId;
    private String profileId;
    private ArrayList<Integer> dmas = new ArrayList<>();

    public DmaCampaign(String campId) {
        this.campId = campId;
    }

    public ArrayList<Integer> getDmas() {
        return dmas;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getCampId() {
        return campId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }
}
