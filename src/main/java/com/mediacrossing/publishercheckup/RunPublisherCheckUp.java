package com.mediacrossing.publishercheckup;

import com.mediacrossing.campaignbooks.Advertiser;
import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.properties.ConfigurationProperties;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.io.*;
import java.net.IDN;
import java.util.ArrayList;
import java.util.List;

public class RunPublisherCheckUp {

    private static final Logger LOG = LoggerFactory.getLogger(RunPublisherCheckUp.class);

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

        //Declare Variables
        ConfigurationProperties configProps = new ConfigurationProperties(args);

        String putneyUrl = configProps.getPutneyUrl();
        String mxUsername = configProps.getMxUsername();
        String mxUrl = configProps.getMxUrl();
        String mxPassword = configProps.getMxPassword();
        AppNexusService anService = new AppNexusService(putneyUrl);
        MxService mxConn;
        if (mxUsername == null) {
            mxConn = new MxService(mxUrl);
        } else {
            mxConn = new MxService(mxUrl, mxUsername, mxPassword);
        }
        String outPath = configProps.getOutputPath();

        //Request publishers from AN
        ArrayList<PublisherConfig> pubs = anService.requestPublisherConfigs();

        List<Advertiser> aList = mxConn.requestAllAdvertisers();
        //for each publisher, request placements, payment rules, and profiles; parse and store.
        for (PublisherConfig p : pubs) {
            p.setPlacements(anService.requestPlacements(p.getId()));
            p.setPaymentRules(anService.requestPaymentRules(p.getId()));
            p.setYmProfiles(anService.requestYmProfiles(p.getId()));
            for (Placement pm : p.getPlacements()) {
                List<IdName> indList = new ArrayList<>();
                for (IdName a : pm.getFilteredAdvertisers()) {
                    for (Advertiser ad : aList) {
                        if (a.getId().equals(ad.getAdvertiserID())) {
                            if (!ad.isLive()) {
                               indList.add(a);
                            }
                        }
                    }
                }
                for (IdName x : indList) pm.getFilteredAdvertisers().remove(x);
            }
        }

        //build report with all publishers
        Workbook wb = ReportGenerator.writePublisherCheckUpReport(pubs);

        //write wb to file
        LocalDate today = new LocalDate(DateTimeZone.UTC);
        FileOutputStream fileOut = new FileOutputStream(new File(outPath, "PublisherCheckUps_"+today.toString()+".xls"));
        wb.write(fileOut);
        fileOut.close();
    }
}
