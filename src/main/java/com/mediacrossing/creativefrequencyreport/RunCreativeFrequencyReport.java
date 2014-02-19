package com.mediacrossing.creativefrequencyreport;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.properties.ConfigurationProperties;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RunCreativeFrequencyReport {

    private static final Logger LOG = LoggerFactory.getLogger(RunCreativeFrequencyReport.class);

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

        //request lifetime numbers
        List<Creative> creatives = new ArrayList<>();
        List<String[]> data = anConn.requestCreativeReport("lifetime");
        data.remove(0);
        String delim = "[_]";
        for (String[] l : data) {
            String[] tokens = l[0].split(delim);
            String name = tokens[3];
            String size = tokens[4];
            creatives.add(new Creative(name, Integer.parseInt(l[1]), Integer.parseInt(l[2]), size));
        }

        //request yesterday's, add to creatives from before
        data = anConn.requestCreativeReport("yesterday");
        data.remove(0);
        for (String[] l : data) {
            String[] tokens = l[0].split(delim);
            String name = tokens[3];
            String size = tokens[4];
            for (Creative c : creatives) {
                if (c.getName().equals(name) && c.getSize().equals(size)) {
                    c.setYesterdayImps(c.getYesterdayImps() + Integer.parseInt(l[1]));
                    c.setYesterdayClicks(c.getYesterdayClicks() + Integer.parseInt(l[2]));
                }
            }
        }

        Set<String> cNames = new HashSet<>();
        for (Creative c : creatives) cNames.add(c.getName());

        Workbook wb = new HSSFWorkbook();
        Sheet s = wb.createSheet("Chase");
        Row head = s.createRow(0);
        head.createCell(0).setCellValue("Creative");
        head.createCell(1).setCellValue("Rate");
        head.createCell(2).setCellValue("Imps Yesterday");
        head.createCell(3).setCellValue("Imps Lifetime");
        head.createCell(4).setCellValue("Imp Goal");
        head.createCell(5).setCellValue("Clicks Yesterday");
        head.createCell(6).setCellValue("Clicks Lifetime");

        //Style header
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setBoldweight((short) 700);
        CellStyle bold = wb.createCellStyle();
        bold.setFont(font);
        for(Cell c : head)
            c.setCellStyle(bold);
        head.setHeightInPoints(2 * s.getDefaultRowHeightInPoints());

        int rowCount = 2;

        for (String n : cNames) {
            Creative total = new Creative();
            for (Creative c : creatives) {
                if (c.getName().equals(n)) {
                    Row r = s.createRow(rowCount);
                    r.createCell(0).setCellValue(c.getName() + " " + c.getSize());
                    r.createCell(2).setCellValue(c.getYesterdayImps());
                    r.createCell(3).setCellValue(c.getLifetimeImps());
                    r.createCell(5).setCellValue(c.getYesterdayClicks());
                    r.createCell(6).setCellValue(c.getLifetimeClicks());
                    rowCount++;
                    total.setName(n);
                    total.setYesterdayImps(total.getYesterdayImps() + c.getYesterdayImps());
                    total.setLifetimeImps(total.getLifetimeImps() + c.getLifetimeImps());
                    total.setYesterdayClicks(total.getYesterdayClicks() + c.getYesterdayClicks());
                    total.setLifetimeClicks(total.getLifetimeClicks() + c.getLifetimeClicks());
                }
            }
            rowCount++;

            Row r = s.createRow(rowCount);
            r.createCell(0).setCellValue(n + " Totals:");
            r.getCell(0).setCellStyle(bold);
            r.createCell(2).setCellValue(total.getYesterdayImps());
            r.createCell(3).setCellValue(total.getLifetimeImps());
            r.createCell(5).setCellValue(total.getYesterdayClicks());
            r.createCell(6).setCellValue(total.getLifetimeClicks());
            if (total.getName().equals("HHI75K+")) {
                r.createCell(1).setCellValue(3.07);
                r.createCell(4).setCellValue(1153420);
            } else if (total.getName().equals("FrequentHotelGuest")) {
                r.createCell(1).setCellValue(4.25);
                r.createCell(4).setCellValue(825647);
            } else if (total.getName().equals("Contextual")) {
                r.createCell(1).setCellValue(2.75);
                r.createCell(4).setCellValue(1238909);
            } else if (total.getName().equals("AudienceModelingProspecting")) {
                r.createCell(1).setCellValue(2.57);
                r.createCell(4).setCellValue(308171);
            } else if (total.getName().equals("Retargeting")) {
                r.createCell(1).setCellValue(6.05);
                r.createCell(4).setCellValue(124132);
            }
            rowCount+=3;
        }

        for (int x = 0; x < 7; x++) s.autoSizeColumn(x);

        LocalDate today = new LocalDate(DateTimeZone.UTC);
        FileOutputStream out = new FileOutputStream(new File(outputPath, "Chase_Hyatt_Creative_Report_" +
                today.toString()+".xls"));
        wb.write(out);
        out.close();

    }
}
