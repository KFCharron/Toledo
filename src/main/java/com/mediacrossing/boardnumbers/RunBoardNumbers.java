package com.mediacrossing.boardnumbers;

import com.mediacrossing.dailycheckupsreport.Campaign;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.properties.ConfigurationProperties;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.ArrayList;

public class RunBoardNumbers {

    public static void main(String[] args) throws Exception {
        ConfigurationProperties properties = new ConfigurationProperties(args);
        String mxUsername = properties.getMxUsername();
        String mxPass = properties.getMxPassword();
        String mxUrl = properties.getMxUrl();
        MxService mxConn = new MxService(mxUrl, mxUsername, mxPass);
        ArrayList<Campaign> camps = mxConn.requestAllCampaigns();
        ArrayList<Campaign> q1camps = new ArrayList<>();
        ArrayList<Campaign> q2camps = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss'Z'");
        DateTime startOfQ1 = new DateTime(2014, 4, 1, 0, 0);
        DateTime endOfQ1 = new DateTime(2014, 8, 1, 0, 0);
        for (Campaign c : camps) {
            System.out.println(c.getEndDate());
            if (!c.getEndDate().equals("null") && !c.getStartDate().equals("null")) {
                DateTime start = dtf.parseDateTime(c.getStartDate());
                DateTime end = dtf.parseDateTime(c.getEndDate());
                if ((start.isAfter(startOfQ1) && start.isBefore(endOfQ1)) || (end.isAfter(startOfQ1) && start.isBefore(endOfQ1))) {
                    q1camps.add(c);
                }
            }

        }
        System.out.println("CAMPAIGNS : " + q1camps.size());
    }
}
