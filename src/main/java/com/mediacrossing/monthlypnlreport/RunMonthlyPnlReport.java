package com.mediacrossing.monthlypnlreport;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.dailycheckupsreport.Campaign;
import com.mediacrossing.dailycheckupsreport.ServingFee;
import com.mediacrossing.monthlybillingreport.BillingAdvertiser;
import com.mediacrossing.monthlybillingreport.BillingCampaign;
import com.mediacrossing.monthlybillingreport.BillingPublisher;
import com.mediacrossing.properties.ConfigurationProperties;
import com.mediacrossing.publisherreporting.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.util.*;

public class RunMonthlyPnlReport {

    private static final Logger LOG = LoggerFactory.getLogger(RunMonthlyPnlReport.class);

    public static void registerLoggerWithUncaughtExceptions() {
        Thread.setDefaultUncaughtExceptionHandler(
                new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
        );
    }

    public static void main(String[] args) throws Exception {

        registerLoggerWithUncaughtExceptions();

        //Declare variables
        ConfigurationProperties properties = new ConfigurationProperties(args);
        String appNexusUrl = properties.getPutneyUrl();
        String outputPath = properties.getOutputPath();

        AppNexusService anConn = new AppNexusService(appNexusUrl
        );
        String mxUsername = properties.getMxUsername();
        String mxPass = properties.getMxPassword();
        String mxUrl = properties.getMxUrl();
        MxService mxConn = new MxService(mxUrl, mxUsername, mxPass);

        ArrayList<BillingAdvertiser> adList = anConn.requestBillingReport("last_month");
        List<String[]> adExData = anConn.requestSellerReport("last_month");
        for (String[] l : adExData) {
            String googleAdExchangeId = "181";
            if (l[0].equals(googleAdExchangeId)) {
                for (BillingAdvertiser ad : adList) {
                    for (BillingCampaign camp : ad.getCampaigns()) {
                        if (camp.getId().equals(l[2])) {
                            camp.setAdExImps(Integer.parseInt(l[3]));
                        }
                    }
                }
            }
            String mxImpId = "1770";
            if (l[0].equals(mxImpId)) {
                for (BillingAdvertiser ad : adList) {
                    for (BillingCampaign camp : ad.getCampaigns()) {
                        if (camp.getId().equals(l[2])) {
                            camp.setMxImps(Integer.parseInt(l[3]));
                        }
                    }
                }
            }
        }
        //call on campaigns from mx
        //parse broker fees
        ArrayList<Campaign> feeCampaigns = mxConn.requestAllCampaigns();
        Set<String> feeNames = new HashSet<>();
        for (Campaign c : feeCampaigns) {
            for (BillingAdvertiser a : adList) {
                for (BillingCampaign bc : a.getCampaigns()) {
                    if (bc.getId().equals(c.getId())) {
                        bc.setMaxBid(c.getMaxBid());
                        bc.setBaseBid(c.getBaseBid());
                    }
                    if (bc.getId().equals(c.getId())) {
                        int ind = -1;
                        for (ServingFee f : c.getServingFeeList()) {
                            if (f.getBrokerName().equals("MediaCrossing")) ind = c.getServingFeeList().indexOf(f);
                        }
                        if (ind != -1) c.getServingFeeList().remove(ind);
                        for (ServingFee fee : c.getServingFeeList()) {

                            fee.setTotalFee(bc.getImps() * (Float.parseFloat(fee.getValue()) / 1000));
                            if (fee.getBrokerName().equals("Peer39")) fee.setTotalFee(bc.getMediaCost() * 0.15f);
                            bc.getServingFees().add(fee);
                            feeNames.add(fee.getBrokerName());
                            if (fee.getBrokerName().equals("Brilig")) {
                                bc.setBriligImps(bc.getImps());
                            }
                            else if (fee.getBrokerName().equals("Lotame")) {
                                bc.setLotameImps(bc.getImps());
                            }
                            else if (fee.getBrokerName().equals("BlueKai")) {
                                bc.setBlueKaiImps(bc.getImps());
                            }
                        }
                    }
                }
            }
        }

        for (BillingAdvertiser a : adList) {
            if (a.getId().equals("186199")) {
                for (BillingCampaign c : a.getCampaigns()) {
                    c.setNetworkRevenue(c.getTotalCost() + (.05f * c.getImps()/1000));
                }
            }
        }

        List<String> sortedFees = new ArrayList<>(feeNames);
        Collections.sort(sortedFees);

        ArrayList<Publisher> pubs = mxConn.requestAllPublishers();
        ArrayList<BillingPublisher> pubList = new ArrayList<>();
        for (Publisher p : pubs) {
            if (p.getStatus().equals("active")) {
                BillingPublisher bp = new BillingPublisher(p.getPublisherName() + " (" + p.getId() + ")",
                        anConn.requestPublisherBillingReport(p.getId(), "last_month"));
                pubList.add(bp);
            }
        }


        MonthlyPnLWriter.writeReportToFile(adList, sortedFees, outputPath, "Monthly_Flash_PnL_", pubList);
    }
}
