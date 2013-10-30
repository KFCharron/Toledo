package com.mediacrossing.publishercheckup;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.properties.ConfigurationProperties;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.io.*;
import java.util.ArrayList;

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
        String anUser = configProps.getAppNexusUsername();
        String anPass = configProps.getAppNexusPassword();
        String anUrl = configProps.getAppNexusUrl();
        int anPartitionSize = configProps.getPartitionSize();
        Duration requestDelayInSeconds = configProps.getRequestDelayInSeconds();
        AppNexusService anService = new AppNexusService(anUrl, anUser, anPass, anPartitionSize, requestDelayInSeconds);
        String outPath = configProps.getOutputPath();
        Workbook wb = new HSSFWorkbook();

        //for faster debugging
        boolean development = false;
        if (development) {
            try{
                FileInputStream door =
                        new FileInputStream("/Users/charronkyle/Desktop/ReportData/PublisherCheckUpData.ser");
                ObjectInputStream reader = new ObjectInputStream(door);
                ArrayList<PublisherConfig> pubList = (ArrayList<PublisherConfig>) reader.readObject();
                wb = ReportGenerator.writePublisherCheckUpReport(pubList);
                FileOutputStream fileOut = new FileOutputStream(new File(outPath, "PublisherCheckUps.xls"));
                wb.write(fileOut);
                fileOut.close();
                System.exit(0);

            }catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        }

        //Request publishers from AN
        ArrayList<PublisherConfig> pubs = anService.requestPublisherConfigs();

        //for each publisher, request placements, payment rules, and profiles; parse and store.
        for (PublisherConfig p : pubs) {
            p.setPlacements(anService.requestPlacements(p.getId()));
            p.setPaymentRules(anService.requestPaymentRules(p.getId()));
            p.setYmProfiles(anService.requestYmProfiles(p.getId()));
        }

        // Serialize data object to a file
        /*try {
            ObjectOutputStream out = new ObjectOutputStream
                    (new FileOutputStream("/Users/charronkyle/Desktop/ReportData/PublisherCheckUpData.ser"));
            out.writeObject(pubs);
            out.close();
        } catch (IOException e) {
            LOG.error("Serialization Failed!");
            LOG.error(e.toString());
        }*/

        //build report with all publishers
        wb = ReportGenerator.writePublisherCheckUpReport(pubs);

        //write wb to file
        FileOutputStream fileOut = new FileOutputStream(new File(outPath, "PublisherCheckUps.xls"));
        wb.write(fileOut);
        fileOut.close();
    }
}
