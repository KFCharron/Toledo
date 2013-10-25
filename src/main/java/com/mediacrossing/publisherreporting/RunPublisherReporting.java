package com.mediacrossing.publisherreporting;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.properties.ConfigurationProperties;
import com.mediacrossing.dailycheckupsreport.XlsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RunPublisherReporting {

    private static final Logger LOG = LoggerFactory.getLogger(RunPublisherReporting.class);

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
        AppNexusService anConn = new AppNexusService(appNexusUrl, appNexusUsername,
                appNexusPassword);

        //for faster debugging
        boolean development = false;
        if (development) {
            try{
                FileInputStream door = new FileInputStream("/Users/charronkyle/Desktop/ReportData/PublisherLists.ser");
                ObjectInputStream reader = new ObjectInputStream(door);
                ArrayList<ArrayList> arrayLists = (ArrayList<ArrayList>) reader.readObject();
                XlsWriter.writePublisherReport(arrayLists.get(0), arrayLists.get(1),
                        arrayLists.get(2), arrayLists.get(3), outputPath);
                System.exit(0);

            }catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }
        }

        //get yesterday publisher report
        ArrayList<Publisher> commonData = anConn.requestPublishers();
        ArrayList<Publisher> dayPubList = commonData;
        ArrayList<Publisher> newPl = new ArrayList<Publisher>();

        for(Publisher pub : dayPubList) {
            List<String[]> csvData = anConn.getPublisherReport("yesterday", pub.getId());

            //remove header
            csvData.remove(0);

            //for every row in the file
            for (String[] line : csvData) {
                newPl.add(new Publisher(line[0], pub.getPublisherName(), Float.parseFloat(line[1]),
                        Integer.parseInt(line[2]), Integer.parseInt(line[3]), Float.parseFloat(line[4]),
                        Float.parseFloat(line[5]), Float.parseFloat(line[6]), Float.parseFloat(line[7])));
            }
        }
        dayPubList = newPl;

        //get lifetime pub report
        ArrayList<Publisher> lifetimePubList = commonData;
        ArrayList<Publisher> newLtPubList = new ArrayList<Publisher>();
        for(Publisher pub : lifetimePubList) {
            List<String[]> csvData = anConn.getPublisherReport("lifetime", pub.getId());
            //remove header
            csvData.remove(0);

            //for every row in the file
            for (String[] line : csvData) {
                newLtPubList.add(new Publisher(line[0], pub.getPublisherName(), Float.parseFloat(line[1]),
                        Integer.parseInt(line[2]), Integer.parseInt(line[3]), Float.parseFloat(line[4]),
                        Float.parseFloat(line[5]), Float.parseFloat(line[6]), Float.parseFloat(line[7])));
            }
        }
        lifetimePubList = newLtPubList;

        //get yesterday placement report
        ArrayList<Placement> dayPlacementList = new ArrayList<Placement>();
        for (Publisher pub : lifetimePubList) {
            List<String[]> csvData = anConn.getPlacementReport("yesterday", pub.getId());
            //remove header
            csvData.remove(0);

            //for every row in the file
            for (String[] line : csvData) {
                Placement p = new Placement();
                p.setId(line[0]);
                p.setName(line[1]);
                p.setSiteId(line[2]);
                p.setSiteName(line[3]);
                p.setImpsTotal(Integer.parseInt(line[4]));
                p.setImpsSold(Integer.parseInt(line[5]));
                p.setClicks(Integer.parseInt(line[6]));
                p.setRtbImps(Integer.parseInt(line[7]));
                p.setKeptImps(Integer.parseInt(line[8]));
                p.setDefaultImps(Integer.parseInt(line[9]));
                p.setPsaImps(Integer.parseInt(line[10]));
                p.setNetworkRevenue(Float.parseFloat(line[11]));
                dayPlacementList.add(p);
            }
        }

        //get lifetime placement report
        ArrayList<Placement> lifetimePlacementList = new ArrayList<Placement>();
        for (Publisher pub : lifetimePubList) {
            List<String[]> csvData = anConn.getPlacementReport("lifetime", pub.getId());
            //remove header
            csvData.remove(0);

            //for every row in the file
            for (String[] line : csvData) {
                Placement p = new Placement();
                p.setId(line[0]);
                p.setName(line[1]);
                p.setSiteId(line[2]);
                p.setSiteName(line[3]);
                p.setImpsTotal(Integer.parseInt(line[4]));
                p.setImpsSold(Integer.parseInt(line[5]));
                p.setClicks(Integer.parseInt(line[6]));
                p.setRtbImps(Integer.parseInt(line[7]));
                p.setKeptImps(Integer.parseInt(line[8]));
                p.setDefaultImps(Integer.parseInt(line[9]));
                p.setPsaImps(Integer.parseInt(line[10]));
                p.setNetworkRevenue(Float.parseFloat(line[11]));
                lifetimePlacementList.add(p);
            }
        }

        // Serialize data object to a file
        /*ArrayList<ArrayList> arrayLists = new ArrayList<ArrayList>();
        arrayLists.add(dayPubList);
        arrayLists.add(lifetimePubList);
        arrayLists.add(dayPlacementList);
        arrayLists.add(lifetimePlacementList);
        try {
            ObjectOutputStream out = new ObjectOutputStream
                    (new FileOutputStream("/Users/charronkyle/Desktop/ReportData/PublisherLists.ser"));
            out.writeObject(arrayLists);
            out.close();
        } catch (IOException e) {
            LOG.error("Serialization Failed!");
            LOG.error(e.toString());
        }*/

        XlsWriter.writePublisherReport(dayPubList, lifetimePubList, dayPlacementList, lifetimePlacementList, outputPath);
    }
}
