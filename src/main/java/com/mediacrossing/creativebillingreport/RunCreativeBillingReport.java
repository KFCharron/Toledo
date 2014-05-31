package com.mediacrossing.creativebillingreport;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.dailycheckupsreport.Campaign;
import com.mediacrossing.dailycheckupsreport.ServingFee;
import com.mediacrossing.monthlybillingreport.BillingAdvertiser;
import com.mediacrossing.monthlybillingreport.BillingCampaign;
import com.mediacrossing.monthlybillingreport.MonthlyBillingReportWriter;
import com.mediacrossing.properties.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;
import java.util.ArrayList;
import java.util.List;

public class RunCreativeBillingReport {

    private static final Logger LOG = LoggerFactory.getLogger(RunCreativeBillingReport.class);

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

        ArrayList<BillingAdvertiser> adList = anConn.requestCreativeBillingReport();

        //call on campaigns from mx
        //parse broker fees
        ArrayList<Campaign> feeCampaigns = mxConn.requestAllCampaigns();
        for (Campaign c : feeCampaigns) {
            for (BillingAdvertiser a : adList) {
                for (BillingCampaign bc : a.getCampaigns()) {
                    if (bc.getId().equals(c.getId())) {
                        for (ServingFee fee : c.getServingFeeList()) {
                            Float imps = (float)bc.getImps();

                        }
                    }
                }
            }
        }

        //Create empty creative list
        ArrayList<BillingCreative> bc = new ArrayList<>();
        boolean saved;
        for (BillingAdvertiser a : adList) {
            for (BillingCampaign c : a.getCampaigns()) {
                saved = false;
                for (BillingCreative b : bc) {
                    if (c.getCreativeId().equals(b.getCreativeId())) {
                        b.setAdId(a.getId());
                        b.setAdName(a.getName());
                        b.setImps(b.getImps() + c.getImps());
                        b.setClicks(b.getClicks() + c.getClicks());
                        b.setConvs(b.getConvs() + c.getConvs());
                        b.setMediaCost(b.getMediaCost() + c.getMediaCost());
                        b.setNetworkRevenue(b.getNetworkRevenue() + c.getNetworkRevenue());
                        b.setAdExImps(b.getAdExImps() + c.getAdExImps());
                        b.setMxImps(b.getMxImps() + c.getMxImps());
                        b.setAppNexusImps(b.getAppNexusImps() + c.getAppNexusImps());
                        b.setBriligImps(b.getBriligImps() + c.getBriligImps());
                        //TODO
                        b.getCampaigns().add(c);
                        saved = true;
                    }
                }
                if(!saved) bc.add(new BillingCreative(c, a.getId(), a.getName()));
            }
        }

        List<String[]> adExData = anConn.requestCreativeSellerReport();
        String mxImpId = "1770";
        String googleAdExchangeId = "181";
        for (String[] l : adExData) {
            if (l[0].equals(googleAdExchangeId)) {
                for (BillingCreative b : bc) {
                    if (b.getCreativeId().equals(l[2])) {
                        b.setAdExImps(Integer.parseInt(l[3]));
                        for (BillingCampaign c : b.getCampaigns()) {
                            c.setAdExImps(Integer.parseInt(l[3]));
                        }
                    }
                }
            }
            else if (l[0].equals(mxImpId)) {
                for (BillingCreative b : bc) {
                    if (b.getCreativeId().equals(l[2])) {
                        b.setMxImps(Integer.parseInt(l[3]));
                        for (BillingCampaign c : b.getCampaigns()) {
                            c.setAdExImps(Integer.parseInt(l[3]));
                        }
                    }
                }
            }
        }
        MonthlyBillingReportWriter.writeCreativeReport(bc, adList, outputPath);
    }
}
