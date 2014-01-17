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
import java.util.List;

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
            String name;
            try {
                name = tokens[1];
                boolean saved = false;
                for (Creative c : creatives) {
                    if (name.equals(c.getName())) {
                        c.setLifetimeImps(c.getLifetimeImps() + Integer.parseInt(l[1]));
                        c.setLifetimeClicks(c.getLifetimeClicks() + Integer.parseInt(l[2]));
                        saved = true;
                    }
                }
                if (!saved) {
                    creatives.add(new Creative(name, Integer.parseInt(l[1]), Integer.parseInt(l[2])));
                }
            } catch (Exception e) {
                LOG.debug("Caught " + l[0]);
            }
        }
        //request yesterday's, add to creatives from before
        data = anConn.requestCreativeReport("yesterday");
        data.remove(0);
        for (String[] l : data) {
            String[] tokens = l[0].split(delim);
            String name;
            try {
                name = tokens[1];
                for (Creative c : creatives) {
                    if (c.getName().equals(name)) {
                        c.setYesterdayImps(c.getYesterdayImps() + Integer.parseInt(l[1]));
                        c.setYesterdayClicks(c.getYesterdayClicks() + Integer.parseInt(l[2]));
                    }
                }
            } catch (Exception e) {
                LOG.debug("Caught " + l[0]);
            }
        }

        Workbook wb = new HSSFWorkbook();
        Sheet s = wb.createSheet("Tag Heuer");
        Row head = s.createRow(0);
        head.createCell(0).setCellValue("Creative");
        head.createCell(1).setCellValue("Rate");
        head.createCell(2).setCellValue("Imps Yesterday");
        head.createCell(3).setCellValue("Imps Lifetime");
        head.createCell(4).setCellValue("Imps V1");
        head.createCell(5).setCellValue("Imps V2");
        head.createCell(6).setCellValue("Clicks Yesterday");
        head.createCell(7).setCellValue("Clicks Lifetime");

        //Style header
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBoldweight((short) 700);
        CellStyle bold = wb.createCellStyle();
        bold.setFont(font);
        for(Cell c : head)
            c.setCellStyle(bold);
        head.setHeightInPoints(2 * s.getDefaultRowHeightInPoints());

        int rowCount = 1;
        for (Creative c : creatives) {
            Row row = s.createRow(rowCount);
            row.createCell(0).setCellValue(c.getName());
            row.createCell(2).setCellValue(c.getYesterdayImps());
            row.createCell(3).setCellValue(c.getLifetimeImps());
            row.createCell(6).setCellValue(c.getYesterdayClicks());
            row.createCell(7).setCellValue(c.getLifetimeClicks());
            if (c.getName().equals("Business Leaders")) {
                row.createCell(1).setCellValue(2.96);
                row.createCell(4).setCellValue(900000);
                row.createCell(5).setCellValue(1206000);
            } else if (c.getName().equals("Business Travelers")) {
                row.createCell(1).setCellValue(2.21);
                row.createCell(4).setCellValue(1900000);
                row.createCell(5).setCellValue(2546000);
            } else if (c.getName().equals("College Educated")) {
                row.createCell(1).setCellValue(2.21);
                row.createCell(4).setCellValue(1500000);
                row.createCell(5).setCellValue(2020000);
            } else if (c.getName().equals("Business Traveler Interest")) {
                row.createCell(1).setCellValue(3.12);
                row.createCell(4).setCellValue(1800000);
                row.createCell(5).setCellValue(2412000);
            } else if (c.getName().equals("Contextual/Keyword  Targeting")) {
                row.createCell(1).setCellValue(1.89);
                row.createCell(4).setCellValue(9200000);
                row.createCell(5).setCellValue(12328000);
            } else if (c.getName().equals("High Income / Net Worth 1")) {
                row.createCell(1).setCellValue(2.21);
                row.createCell(4).setCellValue(2500000);
                row.createCell(5).setCellValue(3400000);
            } else if (c.getName().equals("High Income / Net Worth 2")) {
                row.createCell(1).setCellValue(2.42);
                row.createCell(4).setCellValue(5800000);
                row.createCell(5).setCellValue(7772000);
            } else if (c.getName().equals("In-Market for Watches")) {
                row.createCell(1).setCellValue(2.91);
                row.createCell(4).setCellValue(3600000);
                row.createCell(5).setCellValue(4824000);
            } else if (c.getName().equals("Luxury Auto")) {
                row.createCell(1).setCellValue(3.39);
                row.createCell(4).setCellValue(4000000);
                row.createCell(5).setCellValue(5360000);
            } else if (c.getName().equals("Luxury Goods Interest")) {
                row.createCell(1).setCellValue(3.12);
                row.createCell(4).setCellValue(5800000);
                row.createCell(5).setCellValue(7772000);
            } else if (c.getName().equals("Men's Apparel 1")) {
                row.createCell(1).setCellValue(2.21);
                row.createCell(4).setCellValue(1700000);
                row.createCell(5).setCellValue(2278000);
            } else if (c.getName().equals("Re-targeting")) {
                row.createCell(1).setCellValue(3.1);
                row.createCell(4);
                row.createCell(5).setCellValue(549000);
            }
            rowCount++;
        }

        for (int x = 0; x < 8; x++) s.autoSizeColumn(x);

        LocalDate today = new LocalDate(DateTimeZone.UTC);
        FileOutputStream out = new FileOutputStream(new File(outputPath, "Tag_Heuer_Creative_Report_" +
                today.toString()+".xls"));
        wb.write(out);
        out.close();

    }
}
