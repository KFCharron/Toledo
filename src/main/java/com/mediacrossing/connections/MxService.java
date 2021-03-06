package com.mediacrossing.connections;

import com.mediacrossing.campaignbooks.Advertiser;
import com.mediacrossing.campaignbooks.DataParse;
import com.mediacrossing.campaignbooks.LineItem;
import com.mediacrossing.dailycheckupsreport.Campaign;
import com.mediacrossing.dailycheckupsreport.JSONParse;
import com.mediacrossing.dailycheckupsreport.SegmentGroupTarget;
import com.mediacrossing.publisherreporting.Publisher;
import com.mediacrossing.weeklyconversionreport.ConversionAdvertiser;
import com.mediacrossing.weeklyconversionreport.ConversionParser;
import com.mediacrossing.dailypacingreport.PacingLineItem;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MxService {


    private Request requests;
    private String url;

    public MxService(String url, String username, String password) {
        this.url = url;
        //create properties
        Iterable<Tuple2<String, String>> requestProperties = Collections.unmodifiableList(
                Arrays.asList(
                        ConnectionRequestProperties.authorization(
                                username,
                                password)));
        //open https connection
        this.requests = new HTTPSRequest(requestProperties);
    }

    public MxService(String url) {
        this.url = url;
        this.requests = new HTTPRequest();
    }

    public String requestPacingMetricJson(String fullUrl) throws Exception {
        return requests.getRequest(fullUrl);
    }

    public String putRequest(String url2, String json) throws Exception {
        return requests.putRequest(url + url2, json);
    }

    public ArrayList<Campaign> requestAllCampaigns() throws Exception {
        String json = requests.getRequest(url + "/api/catalog/campaigns");
        return JSONParse.populateCampaignArrayList(json);
    }

    public String requestAllLiveRailLineItemJson() throws Exception {
        return requests.getRequest(url + "/api/catalog/line-items?channelType=preRoll");
    }

    public ArrayList<Publisher> requestAllPublishers() throws Exception {
        String json = requests.getRequest(url + "/api/catalog/publishers");
        return JSONParse.populatePublisherArrayList(json);
    }

    public String requestAdvertiser(String adId) throws Exception {
        return requests.getRequest(url + "/api/catalog/advertisers/" + adId);
    }

    public String requestLineItemNames(String adId) throws Exception {
        return requests.getRequest(url + "/api/catalog/advertisers/" + adId + "/line-items");
    }

    public List<Advertiser> requestAllAdvertisers() throws Exception {
        String json = requests.getRequest(url + "/api/catalog/advertisers");
        return DataParse.populateAdvertiserList(json);
    }

    public String requestAllAdvertiserJson() throws Exception {
        return requests.getRequest(url + "/ap/catalog/advertisers");
    }

    public ArrayList<LineItem> requestLineItemsForAdvertiser(String adId) throws Exception {
        String json = requests.getRequest(url + "/api/catalog/advertisers/" + adId + "/line-items");
        return DataParse.populateLineItemList(json);
    }

    public ArrayList<com.mediacrossing.campaignbooks.Campaign> requestCampaignsForLineItem(String adId, String lineId)
            throws Exception
    {
        String json = requests.getRequest(url + "/api/catalog/advertisers/" + adId
                + "/line-items/" + lineId + "/campaigns");
        return DataParse.populateCampaignList(json);
    }

    public ArrayList<ConversionAdvertiser> requestAllConversionAdvertisers() throws Exception {
        String json = requests.getRequest(url+"/api/catalog/advertisers");
        return ConversionParser.populateLiveAdvertiserList(json);
    }

    public String requestAllPublisherJson() throws Exception {
        return requests.getRequest(url+"/api/catalog/publishers");
    }

    public ArrayList<PacingLineItem> requestAllLineItems() throws Exception {
        return DataParse.parsePacingLineItems(requestAllLineItemJson());
    }

    public String requestAllLineItemJson() throws Exception {
        return requests.getRequest(url+"/api/catalog/line-items");
    }
}
