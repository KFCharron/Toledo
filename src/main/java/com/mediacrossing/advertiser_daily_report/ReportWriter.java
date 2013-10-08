package com.mediacrossing.advertiser_daily_report;

import com.mediacrossing.campaignbooks.Advertiser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class ReportWriter {

    public static void writeAdvertiserDailyReport(List<Advertiser> advertiserList, String outputPath)
            throws IOException {

        //init workbook, decimal formats
        Workbook wb = new HSSFWorkbook();
        DecimalFormat ctrDf = new DecimalFormat("#.0000");
        DecimalFormat df = new DecimalFormat("#.00");

        //Create new sheet
        Sheet sheet = wb.createSheet("Advertiser Daily");

        //title header
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(1).setCellValue("Overview Report (campaigns with active delivery)");

        //style header
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 16);
        font.setBoldweight((short) 700);
        CellStyle bold = wb.createCellStyle();
        bold.setFont(font);

        //bold the header
        titleRow.getCell(1).setCellStyle(bold);

        //create 24hr subheader
        Row subtitleRow = sheet.createRow(2);
        subtitleRow.createCell(0).setCellValue("24 hours:");

        //Pattern style
        CellStyle pattern = wb.createCellStyle();
        pattern.setFillForegroundColor(IndexedColors.LIGHT_GREEN.index);

        //create line item column names
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
        lineItemHeader.createCell(10).setCellValue("Start Date");
        lineItemHeader.createCell(11).setCellValue("End Date");
        lineItemHeader.createCell(12).setCellValue("% Flight");
        lineItemHeader.createCell(13).setCellValue("Daily Budget");
        lineItemHeader.createCell(14).setCellValue("Total Budget");

        //for every advertiser, add a data row for each line item
        int rowCount = 5;
        for(Advertiser ad : advertiserList) {
            for(DailyData data : ad.getDailyLineItems()) {
                Row dataRow = sheet.createRow(rowCount);
                dataRow.createCell(0).setCellValue(data.getId());
                dataRow.createCell(1).setCellValue(data.getName());
                dataRow.createCell(2).setCellValue(data.getImps());
                dataRow.createCell(3).setCellValue(data.getClicks());
                dataRow.createCell(4).setCellValue(data.getTotalConv());
                dataRow.createCell(5).setCellValue(df.format(data.getMediaCost()));
                dataRow.createCell(6).setCellValue(ctrDf.format(data.getCtr()));
                dataRow.createCell(7).setCellValue(ctrDf.format(data.getConvRate()));
                dataRow.createCell(8).setCellValue(df.format(data.getCpm()));
                dataRow.createCell(9).setCellValue(df.format(data.getCpc()));
                if(data.getStartDay() != null && data.getEndDay() != null) {
                    dataRow.createCell(10).setCellValue(data.getStartDay().getMonthOfYear() + "/" +
                        data.getStartDay().getDayOfMonth());
                    dataRow.createCell(11).setCellValue(data.getEndDay().getMonthOfYear() + "/" +
                        data.getEndDay().getMonthOfYear());
                    dataRow.createCell(12).setCellValue(ctrDf.format(data.getPercentThroughFlight()*100) + "%");
                    dataRow.createCell(13).setCellValue(data.getDailyBudget());
                    dataRow.createCell(14).setCellValue(data.getLifetimeBudget());
                } else {
                    dataRow.createCell(10);
                    dataRow.createCell(11);
                    dataRow.createCell(12);
                    dataRow.createCell(13);
                    dataRow.createCell(14);
                }
                for (int x = 0; x<15; x++) {
                    if(x%2 == 1)
                        dataRow.getCell(x).setCellStyle(pattern);
                }
                rowCount++;
            }
        }

        //add column names for campaign header
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
        campaignHeader.createCell(10).setCellValue("Start Date");
        campaignHeader.createCell(11).setCellValue("End Date");
        campaignHeader.createCell(12).setCellValue("% Flight");
        campaignHeader.createCell(13).setCellValue("Daily Budget");
        campaignHeader.createCell(14).setCellValue("Lifetime Budget");


        rowCount++;

        //add data row for each campaign
        for(Advertiser ad : advertiserList) {
            for(DailyData data : ad.getDailyCampaigns()) {
                Row dataRow = sheet.createRow(rowCount);
                dataRow.createCell(0).setCellValue(data.getId());
                dataRow.createCell(1).setCellValue(data.getName());
                dataRow.createCell(2).setCellValue(data.getImps());
                dataRow.createCell(3).setCellValue(data.getClicks());
                dataRow.createCell(4).setCellValue(data.getTotalConv());
                dataRow.createCell(5).setCellValue(df.format(data.getMediaCost()));
                dataRow.createCell(6).setCellValue(ctrDf.format(data.getCtr()));
                dataRow.createCell(7).setCellValue(ctrDf.format(data.getConvRate()));
                dataRow.createCell(8).setCellValue(df.format(data.getCpm()));
                dataRow.createCell(9).setCellValue(df.format(data.getCpc()));
                if(data.getStartDay() != null && data.getEndDay() != null) {
                    dataRow.createCell(10).setCellValue(data.getStartDay().getMonthOfYear() + "/" +
                        data.getStartDay().getDayOfMonth());
                    dataRow.createCell(11).setCellValue(data.getEndDay().getMonthOfYear() + "/" +
                        data.getEndDay().getMonthOfYear());
                    dataRow.createCell(12).setCellValue(ctrDf.format(data.getPercentThroughFlight()*100) + "%");
                    dataRow.createCell(13).setCellValue(data.getDailyBudget());
                    dataRow.createCell(14).setCellValue(data.getLifetimeBudget());
                } else {
                    dataRow.createCell(10);
                    dataRow.createCell(11);
                    dataRow.createCell(12);
                    dataRow.createCell(13);
                    dataRow.createCell(14);
                }
                for (int x = 0; x<15; x++) {
                    if(x%2 == 1)
                        dataRow.getCell(x).setCellStyle(pattern);
                }
                rowCount++;
            }
        }

        rowCount++;

        //repeat for lifetime stats
        Row nextSubtitleRow = sheet.createRow(rowCount);
        nextSubtitleRow.createCell(0).setCellValue("Lifetime:");

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
        nextLineItemHeader.createCell(10).setCellValue("Start Date");
        nextLineItemHeader.createCell(11).setCellValue("End Date");
        nextLineItemHeader.createCell(12).setCellValue("% Flight");
        nextLineItemHeader.createCell(13).setCellValue("Daily Budget");
        nextLineItemHeader.createCell(14).setCellValue("Lifetime Budget");
        nextLineItemHeader.createCell(15).setCellValue("% LT Budget Used");
        nextLineItemHeader.createCell(16).setCellValue("Suggested Daily");

        rowCount++;

        for(Advertiser ad : advertiserList) {
            for(DailyData data : ad.getLifetimeLineItems()) {
                Row dataRow = sheet.createRow(rowCount);
                dataRow.createCell(0).setCellValue(data.getId());
                dataRow.createCell(1).setCellValue(data.getName());
                dataRow.createCell(2).setCellValue(data.getImps());
                dataRow.createCell(3).setCellValue(data.getClicks());
                dataRow.createCell(4).setCellValue(data.getTotalConv());
                dataRow.createCell(5).setCellValue(df.format(data.getMediaCost()));
                dataRow.createCell(6).setCellValue(ctrDf.format(data.getCtr()));
                dataRow.createCell(7).setCellValue(ctrDf.format(data.getConvRate()));
                dataRow.createCell(8).setCellValue(df.format(data.getCpm()));
                dataRow.createCell(9).setCellValue(df.format(data.getCpc()));
                if(data.getStartDay() != null && data.getEndDay() != null) {
                    dataRow.createCell(10).setCellValue(data.getStartDay().getMonthOfYear() + "/" +
                            data.getStartDay().getDayOfMonth());
                    dataRow.createCell(11).setCellValue(data.getEndDay().getMonthOfYear() + "/" +
                            data.getEndDay().getMonthOfYear());
                    dataRow.createCell(12).setCellValue(ctrDf.format(data.getPercentThroughFlight()*100) + "%");
                    dataRow.createCell(13).setCellValue(data.getDailyBudget());
                    dataRow.createCell(14).setCellValue(data.getLifetimeBudget());
                    dataRow.createCell(15).setCellValue(ctrDf.format(data.getPercentThroughLifetimeBudget()*100) + "%");
                    dataRow.createCell(16).setCellValue(df.format(data.getSuggestedDailyBudget()));
                } else {
                    dataRow.createCell(10);
                    dataRow.createCell(11);
                    dataRow.createCell(12);
                    dataRow.createCell(13);
                    dataRow.createCell(14);
                    dataRow.createCell(15);
                    dataRow.createCell(16);
                }
                for (int x = 0; x<17; x++) {
                    if(x%2 == 1)
                        dataRow.getCell(x).setCellStyle(pattern);
                }
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
        nextCampaignHeader.createCell(10).setCellValue("Start Date");
        nextCampaignHeader.createCell(11).setCellValue("End Date");
        nextCampaignHeader.createCell(12).setCellValue("% Flight");
        nextCampaignHeader.createCell(13).setCellValue("Daily Budget");
        nextCampaignHeader.createCell(14).setCellValue("Lifetime Budget");
        nextCampaignHeader.createCell(15).setCellValue("% LT Budget Used");
        nextCampaignHeader.createCell(16).setCellValue("Suggested Daily");




        rowCount++;

        for(Advertiser ad : advertiserList) {
            for(DailyData data : ad.getLifetimeCampaigns()) {
                Row dataRow = sheet.createRow(rowCount);
                dataRow.createCell(0).setCellValue(data.getId());
                dataRow.createCell(1).setCellValue(data.getName());
                dataRow.createCell(2).setCellValue(data.getImps());
                dataRow.createCell(3).setCellValue(data.getClicks());
                dataRow.createCell(4).setCellValue(data.getTotalConv());
                dataRow.createCell(5).setCellValue(df.format(data.getMediaCost()));
                dataRow.createCell(6).setCellValue(ctrDf.format(data.getCtr()));
                dataRow.createCell(7).setCellValue(ctrDf.format(data.getConvRate()));
                dataRow.createCell(8).setCellValue(df.format(data.getCpm()));
                dataRow.createCell(9).setCellValue(df.format(data.getCpc()));
                if(data.getStartDay() != null && data.getEndDay() != null) {
                    dataRow.createCell(10).setCellValue(data.getStartDay().getMonthOfYear() + "/" +
                        data.getStartDay().getDayOfMonth());
                    dataRow.createCell(11).setCellValue(data.getEndDay().getMonthOfYear() + "/" +
                        data.getEndDay().getMonthOfYear());
                    dataRow.createCell(12).setCellValue(ctrDf.format(data.getPercentThroughFlight()*100) + "%");
                    dataRow.createCell(13).setCellValue(data.getDailyBudget());
                    dataRow.createCell(14).setCellValue(data.getLifetimeBudget());
                    dataRow.createCell(15).setCellValue(ctrDf.format(data.getPercentThroughLifetimeBudget()*100) + "%");
                    dataRow.createCell(16).setCellValue(df.format(data.getSuggestedDailyBudget()));
                } else {
                    dataRow.createCell(10);
                    dataRow.createCell(11);
                    dataRow.createCell(12);
                    dataRow.createCell(13);
                    dataRow.createCell(14);
                    dataRow.createCell(15);
                    dataRow.createCell(16);
                }
                for (int x = 0; x<17; x++) {
                    if(x%2 == 1)
                        dataRow.getCell(x).setCellStyle(pattern);
                }
                rowCount++;
            }
        }

        //style subheaders
        Font smallBold = wb.createFont();
        smallBold.setFontHeightInPoints((short) 12);
        CellStyle smallStyle = wb.createCellStyle();
        smallStyle.setFont(font);

        //apply styles
        for(Cell cell : lineItemHeader) cell.setCellStyle(smallStyle);
        for(Cell cell : campaignHeader) cell.setCellStyle(smallStyle);
        for(Cell cell : nextLineItemHeader) cell.setCellStyle(smallStyle);
        for (Cell cell : nextCampaignHeader) cell.setCellStyle(smallStyle);

        //style subtitle
        Font redFont = wb.createFont();
        redFont.setColor(IndexedColors.RED.index);
        redFont.setFontHeightInPoints((short) 14);
        CellStyle redStyle = wb.createCellStyle();
        redStyle.setFont(redFont);

        subtitleRow.getCell(0).setCellStyle(redStyle);
        nextSubtitleRow.getCell(0).setCellStyle(redStyle);

        //autosize columns
        for (int x = 0; x < 17; x++) {
            sheet.autoSizeColumn(x);
        }

        //output file
        FileOutputStream fileOut = new FileOutputStream(new File(outputPath, "DailyReport.xls"));
        wb.write(fileOut);
        fileOut.close();
    }
}
