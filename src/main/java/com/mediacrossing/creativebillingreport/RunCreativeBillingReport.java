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
        String appNexusUrl = properties.getAppNexusUrl();
        String outputPath = properties.getOutputPath();
        String appNexusUsername = properties.getAppNexusUsername();
        String appNexusPassword = properties.getAppNexusPassword();
        int anPartitionSize = properties.getPartitionSize();
        Duration requestDelayInSeconds = properties.getRequestDelayInSeconds();
        AppNexusService anConn = new AppNexusService(appNexusUrl, appNexusUsername,
                appNexusPassword, anPartitionSize, requestDelayInSeconds);
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
                            if (fee.getBrokerName().equals("Integral Ad Science")) {
                                bc.setIntegralFee(Float.parseFloat(fee.getValue()));
                                bc.setIntegralTotal((imps/(float)1000)*bc.getIntegralFee());
                            }
                            else if (fee.getBrokerName().equals("Evidon")) {
                                bc.setEvidonFee(Float.parseFloat(fee.getValue()));
                                bc.setEvidonTotal((imps/(float)1000)*bc.getEvidonFee());
                            }
                            else if (fee.getBrokerName().equals("Brilig")) {
                                bc.setBriligFee(Float.parseFloat(fee.getValue()));
                                bc.setBriligTotal((imps/(float)1000)*bc.getBriligFee());
                                bc.setBriligImps(bc.getImps());
                            }
                            else if (fee.getBrokerName().equals("BlueKai")) {
                                bc.setBlueKaiFee(Float.parseFloat(fee.getValue()));
                                bc.setBlueKaiTotal((imps/(float)1000)*bc.getBlueKaiFee());
                            }
                            else if (fee.getBrokerName().equals("Grapeshot")) {
                                bc.setGrapeshotFee(Float.parseFloat(fee.getValue()));
                                bc.setGrapeshotTotal((imps/(float)1000)*bc.getGrapeshotFee());
                            }
                            else if (fee.getBrokerName().equals("ALC")) {
                                bc.setAlcFee(Float.parseFloat(fee.getValue()));
                                bc.setAlcTotal((imps/(float)1000)*bc.getAlcFee());
                            }
                            else if (fee.getBrokerName().equals("Spongecell")) {
                                bc.setSpongecellFee(Float.parseFloat(fee.getValue()));
                                bc.setSpongecellTotal((imps/(float)1000)*bc.getSpongecellFee());
                            }
                            else if (fee.getBrokerName().equals("Vidible")) {
                                bc.setVidibleFee(Float.parseFloat(fee.getValue()));
                                bc.setVidibleTotal((imps/(float)1000)*bc.getVidibleFee());
                            }
                            else if (fee.getBrokerName().equals("Peer39")) {
                                bc.setPeer39Fee(Float.parseFloat(fee.getValue()));
                                bc.setPeer39Total((bc.getMediaCost() * 0.15f));
                            }
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
                        b.setImps(b.getImps() + c.getImps());
                        b.setClicks(b.getClicks() + c.getClicks());
                        b.setConvs(b.getConvs() + c.getConvs());
                        b.setMediaCost(b.getMediaCost() + c.getMediaCost());
                        b.setNetworkRevenue(b.getNetworkRevenue() + c.getNetworkRevenue());
                        b.setAdExImps(b.getAdExImps() + c.getAdExImps());
                        b.setMxImps(b.getMxImps() + c.getMxImps());
                        b.setAppNexusImps(b.getAppNexusImps() + c.getAppNexusImps());
                        b.setBriligImps(b.getBriligImps() + c.getBriligImps());
                        b.setBriligTotal(b.getBriligTotal() + c.getBriligTotal());
                        b.setEvidonTotal(b.getEvidonTotal() + c.getEvidonTotal());
                        b.setIntegralTotal(b.getIntegralTotal() + c.getIntegralTotal());
                        b.setBlueKaiTotal(b.getBlueKaiTotal() + c.getBlueKaiTotal());
                        b.setAlcTotal(b.getAlcTotal() + c.getAlcTotal());
                        b.setGrapeshotTotal(b.getGrapeshotTotal() + c.getGrapeshotTotal());
                        b.setSpongecellTotal(b.getSpongecellTotal() + c.getSpongecellTotal());
                        b.setVidibleTotal(b.getVidibleTotal() + c.getVidibleTotal());
                        b.setPeer39Total(b.getPeer39Total() + c.getPeer39Total());
                        saved = true;
                    }
                }
                if(!saved) bc.add(new BillingCreative(c));
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
                    }
                }
            }
            else if (l[0].equals(mxImpId)) {
                for (BillingCreative b : bc) {
                    if (b.getCreativeId().equals(l[2])) {
                        b.setMxImps(Integer.parseInt(l[3]));
                    }
                }
            }
        }
        MonthlyBillingReportWriter.writeCreativeReport(bc, outputPath);
    }
}
