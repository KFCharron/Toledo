package com.mediacrossing.advertiser_daily_report;

import com.mediacrossing.campaignbooks.Advertiser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ReportWriter {

    public static void writeAdvertiserDailyReport(List<Advertiser> advertiserList, String outputPath) throws IOException {

        Workbook wb = new HSSFWorkbook();

        //Create new sheet
        Sheet sheet = wb.createSheet("Advertiser Daily");

        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("Overview Report (campaigns with active delivery)");

        Row subtitleRow = sheet.createRow(2);
        subtitleRow.createCell(0).setCellValue("Last 24 hours only:");

        Row lineItemHeader = sheet.createRow(4);
        lineItemHeader.createCell(0).setCellValue("Line Item ID");
        lineItemHeader.createCell(1).setCellValue("Line Item Name");
        lineItemHeader.createCell(2).setCellValue("Imps");
        lineItemHeader.createCell(3).setCellValue("Clicks");
        lineItemHeader.createCell(4).setCellValue("Total Conv");
        lineItemHeader.createCell(5).setCellValue("Media Cost");
        lineItemHeader.createCell(6).setCellValue("CTR");
        lineItemHeader.createCell(7).setCellValue("Conv Rate");
        lineItemHeader.createCell(8).setCellValue("CPM");
        lineItemHeader.createCell(9).setCellValue("CPC");
        
        int rowCount = 5;
        for(Advertiser ad : advertiserList) {
            for(DailyData data : ad.getDailyLineItems()) {
                Row dataRow = sheet.createRow(rowCount);
                dataRow.createCell(0).setCellValue(data.getId());
                dataRow.createCell(1).setCellValue(data.getName());
                dataRow.createCell(2).setCellValue(data.getImps());
                dataRow.createCell(3).setCellValue(data.getClicks());
                dataRow.createCell(4).setCellValue(data.getTotalConv());
                dataRow.createCell(5).setCellValue(data.getMediaCost());
                dataRow.createCell(6).setCellValue(data.getCtr());
                dataRow.createCell(7).setCellValue(data.getConvRate());
                dataRow.createCell(8).setCellValue(data.getCpm());
                dataRow.createCell(9).setCellValue(data.getCpc());
                rowCount++;
            }
        }

        rowCount+=2;
        Row campaignHeader = sheet.createRow(rowCount);
        campaignHeader.createCell(0).setCellValue("Campaign ID");
        campaignHeader.createCell(1).setCellValue("Campaign Name");
        campaignHeader.createCell(2).setCellValue("Imps");
        campaignHeader.createCell(3).setCellValue("Clicks");
        campaignHeader.createCell(4).setCellValue("Total Conv");
        campaignHeader.createCell(5).setCellValue("Media Cost");
        campaignHeader.createCell(6).setCellValue("CTR");
        campaignHeader.createCell(7).setCellValue("Conv Rate");
        campaignHeader.createCell(8).setCellValue("CPM");
        campaignHeader.createCell(9).setCellValue("CPC");

        rowCount++;

        for(Advertiser ad : advertiserList) {
            for(DailyData data : ad.getDailyCampaigns()) {
                Row dataRow = sheet.createRow(rowCount);
                dataRow.createCell(0).setCellValue(data.getId());
                dataRow.createCell(1).setCellValue(data.getName());
                dataRow.createCell(2).setCellValue(data.getImps());
                dataRow.createCell(3).setCellValue(data.getClicks());
                dataRow.createCell(4).setCellValue(data.getTotalConv());
                dataRow.createCell(5).setCellValue(data.getMediaCost());
                dataRow.createCell(6).setCellValue(data.getCtr());
                dataRow.createCell(7).setCellValue(data.getConvRate());
                dataRow.createCell(8).setCellValue(data.getCpm());
                dataRow.createCell(9).setCellValue(data.getCpc());
                rowCount++;
            }
        }

        rowCount++;

        Row nextSubtitleRow = sheet.createRow(rowCount);
        nextSubtitleRow.createCell(0).setCellValue("Lifetime Stats:");

        rowCount++;

        Row nextLineItemHeader = sheet.createRow(rowCount);
        nextLineItemHeader = sheet.createRow(rowCount);
        nextLineItemHeader.createCell(0).setCellValue("Line Item ID");
        nextLineItemHeader.createCell(1).setCellValue("Line Item Name");
        nextLineItemHeader.createCell(2).setCellValue("Imps");
        nextLineItemHeader.createCell(3).setCellValue("Clicks");
        nextLineItemHeader.createCell(4).setCellValue("Total Conv");
        nextLineItemHeader.createCell(5).setCellValue("Media Cost");
        nextLineItemHeader.createCell(6).setCellValue("CTR");
        nextLineItemHeader.createCell(7).setCellValue("Conv Rate");
        nextLineItemHeader.createCell(8).setCellValue("CPM");
        nextLineItemHeader.createCell(9).setCellValue("CPC");

        rowCount++;
        for(Advertiser ad : advertiserList) {
            for(DailyData data : ad.getLifetimeLineItems()) {
                Row dataRow = sheet.createRow(rowCount);
                dataRow.createCell(0).setCellValue(data.getId());
                dataRow.createCell(1).setCellValue(data.getName());
                dataRow.createCell(2).setCellValue(data.getImps());
                dataRow.createCell(3).setCellValue(data.getClicks());
                dataRow.createCell(4).setCellValue(data.getTotalConv());
                dataRow.createCell(5).setCellValue(data.getMediaCost());
                dataRow.createCell(6).setCellValue(data.getCtr());
                dataRow.createCell(7).setCellValue(data.getConvRate());
                dataRow.createCell(8).setCellValue(data.getCpm());
                dataRow.createCell(9).setCellValue(data.getCpc());
                rowCount++;
            }
        }

        rowCount+=2;
        Row nextCampaignHeader = sheet.createRow(rowCount);
        nextCampaignHeader.createCell(0).setCellValue("Campaign ID");
        nextCampaignHeader.createCell(1).setCellValue("Campaign Name");
        nextCampaignHeader.createCell(2).setCellValue("Imps");
        nextCampaignHeader.createCell(3).setCellValue("Clicks");
        nextCampaignHeader.createCell(4).setCellValue("Total Conv");
        nextCampaignHeader.createCell(5).setCellValue("Media Cost");
        nextCampaignHeader.createCell(6).setCellValue("CTR");
        nextCampaignHeader.createCell(7).setCellValue("Conv Rate");
        nextCampaignHeader.createCell(8).setCellValue("CPM");
        nextCampaignHeader.createCell(9).setCellValue("CPC");

        rowCount++;

        for(Advertiser ad : advertiserList) {
            for(DailyData data : ad.getLifetimeCampaigns()) {
                Row dataRow = sheet.createRow(rowCount);
                dataRow.createCell(0).setCellValue(data.getId());
                dataRow.createCell(1).setCellValue(data.getName());
                dataRow.createCell(2).setCellValue(data.getImps());
                dataRow.createCell(3).setCellValue(data.getClicks());
                dataRow.createCell(4).setCellValue(data.getTotalConv());
                dataRow.createCell(5).setCellValue(data.getMediaCost());
                dataRow.createCell(6).setCellValue(data.getCtr());
                dataRow.createCell(7).setCellValue(data.getConvRate());
                dataRow.createCell(8).setCellValue(data.getCpm());
                dataRow.createCell(9).setCellValue(data.getCpc());
                rowCount++;
            }
        }

        FileOutputStream fileOut = new FileOutputStream(new File(outputPath, "DailyReport.xls"));
        wb.write(fileOut);
        fileOut.close();
    }
}
