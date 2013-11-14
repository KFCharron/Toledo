package com.mediacrossing.monthlybillingreport;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
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
                            if (fee.getBrokerName().equals("Evidon")) {
                                bc.setEvidonFee(Float.parseFloat(fee.getValue()));
                                bc.setEvidonTotal((imps/(float)1000)*bc.getEvidonFee());
                            }
                            if (fee.getBrokerName().equals("Brilig")) {
                                bc.setBriligFee(Float.parseFloat(fee.getValue()));
                                bc.setBriligTotal((imps/(float)1000)*bc.getBriligFee());
                                bc.setBriligImps(bc.getImps());
                            }
                            if (fee.getBrokerName().equals("BlueKai")) {
                                bc.setBlueKaiFee(Float.parseFloat(fee.getValue()));
                                bc.setBlueKaiTotal((imps/(float)1000)*bc.getBlueKaiFee());
                            }
                            if (fee.getBrokerName().equals("Grapeshot")) {
                                bc.setGrapeshotFee(Float.parseFloat(fee.getValue()));
                                bc.setGrapeshotTotal((imps/(float)1000)*bc.getGrapeshotFee());
                            }
                            if (fee.getBrokerName().equals("ALC")) {
                                bc.setAlcFee(Float.parseFloat(fee.getValue()));
                                bc.setAlcTotal((imps/(float)1000)*bc.getAlcFee());
                            }
                        }
                    }
                }
            }
        }

        // Serialize data object to a file
        try {
            ObjectOutputStream out = new ObjectOutputStream
                    (new FileOutputStream("/Users/charronkyle/Desktop/ReportData/BillingList.ser"));
            out.writeObject(adList);
            out.close();
        } catch (IOException e) {
            LOG.error("Serialization Failed!");
            LOG.error(e.toString());
        }

        MonthlyBillingReportWriter.writeReportToFile(adList, outputPath);

    }
}
