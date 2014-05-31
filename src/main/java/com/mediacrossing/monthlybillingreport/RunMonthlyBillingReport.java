package com.mediacrossing.monthlybillingreport;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.dailycheckupsreport.Campaign;
import com.mediacrossing.dailycheckupsreport.ServingFee;
import com.mediacrossing.properties.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.util.*;

public class RunMonthlyBillingReport {

    private static final Logger LOG = LoggerFactory.getLogger(RunMonthlyBillingReport.class);

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
        String appNexusUsername = properties.getAppNexusUsername();
        String appNexusPassword = properties.getAppNexusPassword();
        int anPartitionSize = properties.getPartitionSize();
        Duration requestDelayInSeconds = properties.getRequestDelayInSeconds();
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
                        for (ServingFee fee : c.getServingFeeList()) {
                            fee.setTotalFee(bc.getImps() * (Float.parseFloat(fee.getValue()) / 1000));
                            if (fee.getBrokerName().equals("Peer39")) fee.setTotalFee(bc.getMediaCost() * 0.15f);
                            bc.getServingFees().add(fee);
                            if (!fee.getBrokerName().equals("MediaCrossing")) {
                                feeNames.add(fee.getBrokerName());
                            }
                            if (fee.getBrokerName().equals("Brilig")) {
                                bc.setBriligImps(bc.getImps());
                            }
                            if (fee.getBrokerName().equals("Lotame")) {
                                bc.setLotameImps(bc.getImps());
                            }
                        }
                    }
                }
            }
        }

        List<String> sortedFees = new ArrayList<>(feeNames);
        Collections.sort(sortedFees);
        sortedFees.remove("Brilig");

        MonthlyBillingReportWriter.writeReportToFile(adList, sortedFees, outputPath, "Monthly");
    }
}
