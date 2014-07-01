package com.mediacrossing.lifetimesegmentreport;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.dailycheckupsreport.Campaign;
import com.mediacrossing.dailycheckupsreport.Segment;
import com.mediacrossing.dailycheckupsreport.SegmentGroupTarget;
import com.mediacrossing.properties.ConfigurationProperties;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import scala.Tuple3;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RunLTSegmentReport {

    private static final Logger LOG = LoggerFactory.getLogger(RunLTSegmentReport.class);

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

        ArrayList<Campaign> camps = mxConn.requestAllCampaigns();

        ArrayList<Tuple2<String, ArrayList<SegmentGroupTarget>>> segments = anConn.requestAllProfileSegments();

        List<String[]> data = anConn.getLifetimeCampaignImpsAndSpend();
        data.remove(0);

        for (Tuple2<String, ArrayList<SegmentGroupTarget>> t : segments) {
            for (Campaign c : camps) {
                if (c.getProfileID().equals(t._1())){
                    for (SegmentGroupTarget s : t._2()) c.getSegmentGroupTargetList().add(s);
                };
            }
        }

        for (String[] line : data) {
            for (Campaign c : camps) {
                if (c.getId().equals(line[0])) {
                    c.setImps(Integer.parseInt(line[1]));
                    c.setSpend(Float.parseFloat(line[2]));
                }
            }
        }

        HashSet<String> segmentNames = new HashSet<>();
        for (Campaign c : camps) {
            for (SegmentGroupTarget s : c.getSegmentGroupTargetList()) {
                for (Segment segment: s.getSegmentArrayList()) {
                    if (segment.getAction().equals("include")) {
                        segmentNames.add(segment.getName());
                    }
                }
            }
        }

        ArrayList<Tuple3<String, Integer, Float>> segmentTotals = new ArrayList<>();

        for (String s : segmentNames) {
            int impTotal = 0;
            float spendTotal = 0.0f;
            for (Campaign c : camps) {
                boolean count = false;
                for (SegmentGroupTarget g : c.getSegmentGroupTargetList()) {
                    for (Segment seg : g.getSegmentArrayList()) {
                        if (seg.getAction().equals("include") && seg.getName().equals(s)) {
                            count = true;
                        }
                    }
                }
                if (count) {
                    impTotal = impTotal + c.getImps();
                    spendTotal = spendTotal + c.getSpend();
                }
            }
            segmentTotals.add(new Tuple3<>(s, impTotal, spendTotal));
        }
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        int rowCount = 0;
        for (Tuple3<String, Integer, Float> t : segmentTotals) {
            Row row = sheet.createRow(rowCount++);
            row.createCell(0).setCellValue(t._1());
            row.createCell(1).setCellValue(t._2());
            row.createCell(2).setCellValue(t._3());
        }
        wb.write(new FileOutputStream(new File(outputPath, "Lifetime_Segments.xls")));
    }
}
