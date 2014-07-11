package com.mediacrossing.anupload;

import au.com.bytecode.opencsv.CSVReader;
import com.mediacrossing.campaignbooks.Campaign;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.properties.ConfigurationProperties;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RunSegmentResearch {

    public static void main(String[] args) throws Exception {
        CSVReader reader = new CSVReader(new FileReader("/Users/charronkyle/Desktop/RegisTimestampAttribution.csv"));
        List<String[]> result = reader.readAll();
        result.remove(0);

        ArrayList<SegmentData> segList = new ArrayList<>();

        DateTimeFormatter dtf = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss.SSS'z'");

        for (String[] line : result) {
            boolean found = false;
            for (SegmentData s : segList) {
                if (s.getMxId().equals(line[0])) {
                    found = true;
                    DateTime date = dtf.parseDateTime(line[2]);
                    if (date.isBefore(s.getFirstTouch())) {
                        s.setFirstTouch(date);
                        s.setFirstTouchCampaignId(line[3]);
                    }
                    if (date.isAfter(s.getLastTouch())) {
                        s.setLastTouch(date);
                        s.setLastTouchCampaignId(line[3]);
                    }
                }
            }
            if (!found) {
                SegmentData newSeg = new SegmentData();
                newSeg.setMxId(line[0]);
                newSeg.setFirstTouch(dtf.parseDateTime(line[2]));
                newSeg.setLastTouch(dtf.parseDateTime(line[2]));
                newSeg.setFirstTouchCampaignId(line[3]);
                newSeg.setLastTouchCampaignId(line[3]);
                segList.add(newSeg);
            }

        }
        ConfigurationProperties properties = new ConfigurationProperties(args);
        MxService mxApi = new MxService(properties.getMxUrl(), properties.getMxUsername(), properties.getMxPassword());
        ArrayList<com.mediacrossing.dailycheckupsreport.Campaign> camps = mxApi.requestAllCampaigns();
        for (SegmentData d : segList) {
            for (com.mediacrossing.dailycheckupsreport.Campaign c : camps) {
                if (c.getId().equals(d.getFirstTouchCampaignId())) {
                    d.setFirstTouchCampaignName(c.getName());
                }
                if (c.getId().equals(d.getLastTouchCampaignId())) {
                    d.setLastTouchCampaignName(c.getName());
                }
            }
        }
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("MX User ID");
        header.createCell(1).setCellValue("First Touch");
        header.createCell(2).setCellValue("First Touch Campaign");
        header.createCell(3).setCellValue("Last Touch");
        header.createCell(4).setCellValue("Last Touch Campaign");

        int rowCount = 1;
        for (SegmentData d : segList) {
            Row dataRow = sheet.createRow(rowCount++);
            dataRow.createCell(0).setCellValue(d.getMxId());
            dataRow.createCell(1).setCellValue(d.getFirstTouch().toString("YYYY-MM-dd HH:mm:ss"));
            dataRow.createCell(2).setCellValue(d.getFirstTouchCampaignName() + " (" + d.getFirstTouchCampaignId() + ")");
            dataRow.createCell(3).setCellValue(d.getLastTouch().toString("YYYY-MM-dd HH:mm:ss"));
            dataRow.createCell(4).setCellValue(d.getLastTouchCampaignName() + " (" + d.getLastTouchCampaignId() + ")");
        }

        FileOutputStream fileOut =
                new FileOutputStream(new File(properties.getOutputPath(), "ConversionQuery.xls"));
        wb.write(fileOut);
        fileOut.close();
    }
}
