package com.mediacrossing.monthlybillingreport;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.creativebillingreport.BillingCreative;
import com.mediacrossing.dailycheckupsreport.Campaign;
import com.mediacrossing.dailycheckupsreport.ServingFee;
import com.mediacrossing.properties.ConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

        //for faster debugging
        boolean development = false;
        if (development) {
            try{
                FileInputStream door = new FileInputStream("/Users/charronkyle/Desktop/ReportData/BillingList.ser");
                ObjectInputStream reader = new ObjectInputStream(door);
                ArrayList<BillingAdvertiser> adList = (ArrayList<BillingAdvertiser>) reader.readObject();
                MonthlyBillingReportWriter.writeReportToFile(adList, outputPath);
                System.exit(0);
            }catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        }

        ArrayList<BillingAdvertiser> adList = anConn.requestBillingReport();
        List<String[]> adExData = anConn.requestSellerReport();
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

        MonthlyBillingReportWriter.writeReportToFile(adList, outputPath);
    }
}
