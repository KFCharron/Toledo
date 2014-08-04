package com.mediacrossing.lotamereport;

import com.mediacrossing.connections.HTTPRequest;
import com.mediacrossing.dailycheckupsreport.*;
import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.dailycheckupsreport.profiles.ProfileRepository;
import com.mediacrossing.properties.ConfigurationProperties;
import com.mediacrossing.segmenttargeting.profiles.PutneyProfileRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class RunLotameReport {

    private static final Logger LOG = LoggerFactory.getLogger(RunLotameReport.class);

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

    private static ProfileRepository production(HTTPRequest r) {
        return new PutneyProfileRepository(r);
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

        ArrayList<Campaign> campList = mxConn.requestAllCampaigns();

        ArrayList<Campaign> lotameCamps = new ArrayList<>();

        for (Campaign c : campList) {
            boolean lotameCamp = false;
            for (ServingFee f : c.getServingFeeList()) {
                if (f.getBrokerName().equals("Lotame")) {
                    lotameCamp = true;
                }
            }
            if (lotameCamp) lotameCamps.add(c);
        }

        final ProfileRepository profileRepository = production(anConn.requests);

        final List<Tuple2<String, String>> advertiserIdAndProfileIds =
                new ArrayList<Tuple2<String, String>>();
        for (Campaign c : lotameCamps) {
            advertiserIdAndProfileIds.add(
                    new Tuple2<String, String>(c.getAdvertiserID(), c.getProfileID()));
        }

        final List<Profile> profiles = profileRepository.findBy(advertiserIdAndProfileIds, properties.getPutneyUrl());

        for (Campaign c : lotameCamps) {
            for (Profile p : profiles) {
                if (p.getId().equals(
                        c.getProfileID())) {
                    c.setProfile(p);
                }
            }
        }

        List<String[]> data = anConn.requestImpReport();
        data.remove(0);

        for (String[] l : data) {
            for (Campaign c : lotameCamps) {
                if (c.getId().equals(l[0])) {
                    c.setDailyImps(Integer.parseInt(l[1]));
                }
            }
        }

        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Campaign");
        header.createCell(1).setCellValue("Segment Name");
        header.createCell(2).setCellValue("Segment ID");
        header.createCell(3).setCellValue("Segment Code");
        header.createCell(4).setCellValue("Imps");
        int rowCount = 1;
        for (Campaign c : lotameCamps) {
            if (c.getDailyImps() > 0) {
                boolean usedImp = false;
                for (SegmentGroupTarget g : c.getProfile().getSegmentGroupTargets()) {
                    for (Segment s : g.getSegmentArrayList()) {
                        if (s.getAction().equals("include") && (s.getCode().contains("LME")
                                || s.getName().contains("Lotame")
                                || c.getName().contains("Lotame")) && !s.getCode().contains("VTOP")) {
                            Row dataRow = sheet.createRow(rowCount);
                            dataRow.createCell(0).setCellValue(c.getName());
                            dataRow.createCell(1).setCellValue(s.getName());
                            dataRow.createCell(2).setCellValue(s.getId());
                            dataRow.createCell(3).setCellValue(s.getCode());
                            if (!usedImp) {
                                dataRow.createCell(4).setCellValue(c.getDailyImps());
                                usedImp = true;
                            }
                            rowCount++;
                        }
                    }
                }

            }

        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
        sheet.autoSizeColumn(4);

        LocalDate now = new LocalDate(DateTimeZone.UTC);
        FileOutputStream fileOut =
                new FileOutputStream(new File(outputPath, "Monthly_Lotame_Report_" + now.toString() + ".xls"));
        wb.write(fileOut);
        fileOut.close();
    }

}
