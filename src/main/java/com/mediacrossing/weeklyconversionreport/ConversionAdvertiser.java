package com.mediacrossing.weeklyconversionreport;

import java.io.Serializable;
import java.util.ArrayList;

public class ConversionAdvertiser implements Serializable {

    private String advertiserName = "";
    private String advertiserId = "";
    private String status = "";
    private ArrayList<ConversionData> conversionDataList = new ArrayList<ConversionData>();

    public ConversionAdvertiser(String advertiserName, String advertiserId, String status) {
        this.advertiserName = advertiserName;
        this.advertiserId = advertiserId;
        this.status = status;
    }

    public String getAdvertiserName() {
        return advertiserName;
    }

    public String getAdvertiserId() {
        return advertiserId;
    }

    public String getStatus() {
        return status;
    }

    public ArrayList<ConversionData> getConversionDataList() {
        return conversionDataList;
    }

    public void setConversionDataList(ArrayList<ConversionData> conversionDataList) {
        this.conversionDataList = conversionDataList;
    }
}
