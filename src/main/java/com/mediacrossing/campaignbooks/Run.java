package com.mediacrossing.campaignbooks;

import com.mediacrossing.segmenttargeting.HTTPRequest;
import com.mediacrossing.segmenttargeting.JSONParse;

public class Run {

    public static void main(String[] args) throws Exception {
        //Declare variables
        String mxAdvertiserUrl = "http://ec2-50-17-18-117.compute-1.amazonaws.com:9000/api/catalog/advertisers";
        String rawJsonData;
        HTTPRequest httpConnection = new HTTPRequest();
        JSONParse parser = new JSONParse();

        //Query MX for all advertisers
        httpConnection.setUrl(mxAdvertiserUrl);
        httpConnection.requestData();
        rawJsonData = httpConnection.getJSONData();

        //Save to list of advertisers
        parser.populateAdvertiserList(rawJsonData);

        //Query MX for line items of each advertiser

        //Save line items to each advertiser

        //Query AN for campaign quick stats interval=yesterday

        //Save each campaign to the line item

        //Build and save excel book, each sheet being its own line item

    }
}
