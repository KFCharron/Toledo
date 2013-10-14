package com.mediacrossing.advertiser_daily_report;

import com.mediacrossing.campaignbooks.Advertiser;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ReportWriter {

    public static void writeAdvertiserDailyReport(List<Advertiser> advertiserList, String outputPath)
            throws IOException {
        


        //init workbook, decimal formats
        Workbook wb = new HSSFWorkbook();

        //Create new sheet
        Sheet sheet = wb.createSheet("Advertiser Daily");

        //set up formatting styles
        DataFormat df = wb.createDataFormat();

        CellStyle fullCurrency = wb.createCellStyle();
        fullCurrency.setDataFormat(df.getFormat("$#,##0.00"));

        CellStyle halfCurrency = wb.createCellStyle();
        halfCurrency.setDataFormat(df.getFormat("$#,##0"));

        CellStyle percentage = wb.createCellStyle();
        percentage.setDataFormat(df.getFormat("0%"));

        CellStyle ctrPercentage = wb.createCellStyle();
        ctrPercentage.setDataFormat(df.getFormat("#.0000%"));

        CellStyle greenFullCurrency = wb.createCellStyle();
        greenFullCurrency.setDataFormat(df.getFormat("$#,##0.00"));
        greenFullCurrency.setFillForegroundColor(IndexedColors.LIGHT_GREEN.index);
        greenFullCurrency.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        CellStyle greenHalfCurrency = wb.createCellStyle();
        greenHalfCurrency.setDataFormat(df.getFormat("$#,##0"));
        greenHalfCurrency.setFillForegroundColor(IndexedColors.LIGHT_GREEN.index);
        greenHalfCurrency.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        CellStyle greenPercentage = wb.createCellStyle();
        greenPercentage.setDataFormat(df.getFormat("0%"));
        greenPercentage.setFillForegroundColor(IndexedColors.LIGHT_GREEN.index);
        greenPercentage.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        CellStyle greenCtrPercentage = wb.createCellStyle();
        greenCtrPercentage.setDataFormat(df.getFormat("0.0000%"));
        greenCtrPercentage.setFillForegroundColor(IndexedColors.LIGHT_GREEN.index);
        greenCtrPercentage.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        CellStyle greenStyle = wb.createCellStyle();
        greenStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.index);
        greenStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

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
                dataRow.createCell(5).setCellValue(data.getMediaCost());
                dataRow.createCell(6).setCellValue(data.getCtr());
                dataRow.createCell(7).setCellValue(data.getConvRate());
                dataRow.createCell(8).setCellValue(data.getCpm());
                dataRow.createCell(9).setCellValue(data.getCpc());
                if(data.getStartDay() != null && data.getEndDay() != null) {
                    dataRow.createCell(10).setCellValue(data.getStartDay().getMonthOfYear() + "/" +
                        data.getStartDay().getDayOfMonth());
                    dataRow.createCell(11).setCellValue(data.getEndDay().getMonthOfYear() + "/" +
                        data.getEndDay().getDayOfMonth());
                    dataRow.createCell(12).setCellValue(data.getPercentThroughFlight());
                    dataRow.createCell(13).setCellValue(data.getDailyBudget());
                    dataRow.createCell(14).setCellValue(data.getLifetimeBudget());
                } else {
                    dataRow.createCell(10);
                    dataRow.createCell(11);
                    dataRow.createCell(12);
                    dataRow.createCell(13);
                    dataRow.createCell(14);
                }

                dataRow.getCell(5).setCellStyle(fullCurrency);
                dataRow.getCell(6).setCellStyle(ctrPercentage);
                dataRow.getCell(7).setCellStyle(ctrPercentage);
                dataRow.getCell(8).setCellStyle(fullCurrency);
                dataRow.getCell(9).setCellStyle(fullCurrency);
                dataRow.getCell(12).setCellStyle(percentage);
                dataRow.getCell(13).setCellStyle(fullCurrency);
                dataRow.getCell(14).setCellStyle(halfCurrency);

                //Pattern rows
                if(rowCount % 2 == 1) {
                    dataRow.getCell(0).setCellStyle(greenStyle);
                    dataRow.getCell(1).setCellStyle(greenStyle);
                    dataRow.getCell(2).setCellStyle(greenStyle);
                    dataRow.getCell(3).setCellStyle(greenStyle);
                    dataRow.getCell(4).setCellStyle(greenStyle);
                    dataRow.getCell(5).setCellStyle(greenFullCurrency);
                    dataRow.getCell(6).setCellStyle(greenCtrPercentage);
                    dataRow.getCell(7).setCellStyle(greenCtrPercentage);
                    dataRow.getCell(8).setCellStyle(greenFullCurrency);
                    dataRow.getCell(9).setCellStyle(greenFullCurrency);
                    dataRow.getCell(10).setCellStyle(greenStyle);
                    dataRow.getCell(11).setCellStyle(greenStyle);
                    dataRow.getCell(12).setCellStyle(greenPercentage);
                    dataRow.getCell(13).setCellStyle(greenFullCurrency);
                    dataRow.getCell(14).setCellStyle(greenHalfCurrency);
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
                dataRow.createCell(5).setCellValue(data.getMediaCost());
                dataRow.createCell(6).setCellValue(data.getCtr());
                dataRow.createCell(7).setCellValue(data.getConvRate());
                dataRow.createCell(8).setCellValue(data.getCpm());
                dataRow.createCell(9).setCellValue(data.getCpc());
                if(data.getStartDay() != null && data.getEndDay() != null) {
                    dataRow.createCell(10).setCellValue(data.getStartDay().getMonthOfYear() + "/" +
                        data.getStartDay().getDayOfMonth());
                    dataRow.createCell(11).setCellValue(data.getEndDay().getMonthOfYear() + "/" +
                        data.getEndDay().getDayOfMonth());
                    dataRow.createCell(12).setCellValue(data.getPercentThroughFlight());
                    dataRow.createCell(13).setCellValue(data.getDailyBudget());
                    dataRow.createCell(14).setCellValue(data.getLifetimeBudget());
                } else {
                    dataRow.createCell(10);
                    dataRow.createCell(11);
                    dataRow.createCell(12);
                    dataRow.createCell(13);
                    dataRow.createCell(14);
                }

                dataRow.getCell(5).setCellStyle(fullCurrency);
                dataRow.getCell(6).setCellStyle(ctrPercentage);
                dataRow.getCell(7).setCellStyle(ctrPercentage);
                dataRow.getCell(8).setCellStyle(fullCurrency);
                dataRow.getCell(9).setCellStyle(fullCurrency);
                dataRow.getCell(12).setCellStyle(percentage);
                dataRow.getCell(13).setCellStyle(fullCurrency);
                dataRow.getCell(14).setCellStyle(halfCurrency);

                //Pattern rows
                if(rowCount % 2 == 1) {
                    dataRow.getCell(0).setCellStyle(greenStyle);
                    dataRow.getCell(1).setCellStyle(greenStyle);
                    dataRow.getCell(2).setCellStyle(greenStyle);
                    dataRow.getCell(3).setCellStyle(greenStyle);
                    dataRow.getCell(4).setCellStyle(greenStyle);
                    dataRow.getCell(5).setCellStyle(greenFullCurrency);
                    dataRow.getCell(6).setCellStyle(greenCtrPercentage);
                    dataRow.getCell(7).setCellStyle(greenCtrPercentage);
                    dataRow.getCell(8).setCellStyle(greenFullCurrency);
                    dataRow.getCell(9).setCellStyle(greenFullCurrency);
                    dataRow.getCell(10).setCellStyle(greenStyle);
                    dataRow.getCell(11).setCellStyle(greenStyle);
                    dataRow.getCell(12).setCellStyle(greenPercentage);
                    dataRow.getCell(13).setCellStyle(greenFullCurrency);
                    dataRow.getCell(14).setCellStyle(greenHalfCurrency);
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
                if (data.getStatus().equals("active")) {
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
                    if(data.getStartDay() != null && data.getEndDay() != null) {
                        dataRow.createCell(10).setCellValue(data.getStartDay().getMonthOfYear() + "/" +
                                data.getStartDay().getDayOfMonth());
                        dataRow.createCell(11).setCellValue(data.getEndDay().getMonthOfYear() + "/" +
                                data.getEndDay().getDayOfMonth());
                        dataRow.createCell(12).setCellValue(data.getPercentThroughFlight());
                        dataRow.createCell(13).setCellValue(data.getDailyBudget());
                        dataRow.createCell(14).setCellValue(data.getLifetimeBudget());
                        dataRow.createCell(15).setCellValue(data.getPercentThroughLifetimeBudget());
                        dataRow.createCell(16).setCellValue(data.getSuggestedDailyBudget());
                    } else {
                        dataRow.createCell(10);
                        dataRow.createCell(11);
                        dataRow.createCell(12);
                        dataRow.createCell(13);
                        dataRow.createCell(14);
                        dataRow.createCell(15);
                        dataRow.createCell(16);
                    }

                    dataRow.getCell(5).setCellStyle(fullCurrency);
                    dataRow.getCell(6).setCellStyle(ctrPercentage);
                    dataRow.getCell(7).setCellStyle(ctrPercentage);
                    dataRow.getCell(8).setCellStyle(fullCurrency);
                    dataRow.getCell(9).setCellStyle(fullCurrency);
                    dataRow.getCell(12).setCellStyle(percentage);
                    dataRow.getCell(13).setCellStyle(fullCurrency);
                    dataRow.getCell(14).setCellStyle(halfCurrency);
                    dataRow.getCell(15).setCellStyle(percentage);
                    dataRow.getCell(16).setCellStyle(fullCurrency);

                    //Pattern rows
                    if(rowCount % 2 == 1) {
                        dataRow.getCell(0).setCellStyle(greenStyle);
                        dataRow.getCell(1).setCellStyle(greenStyle);
                        dataRow.getCell(2).setCellStyle(greenStyle);
                        dataRow.getCell(3).setCellStyle(greenStyle);
                        dataRow.getCell(4).setCellStyle(greenStyle);
                        dataRow.getCell(5).setCellStyle(greenFullCurrency);
                        dataRow.getCell(6).setCellStyle(greenCtrPercentage);
                        dataRow.getCell(7).setCellStyle(greenCtrPercentage);
                        dataRow.getCell(8).setCellStyle(greenFullCurrency);
                        dataRow.getCell(9).setCellStyle(greenFullCurrency);
                        dataRow.getCell(10).setCellStyle(greenStyle);
                        dataRow.getCell(11).setCellStyle(greenStyle);
                        dataRow.getCell(12).setCellStyle(greenPercentage);
                        dataRow.getCell(13).setCellStyle(greenFullCurrency);
                        dataRow.getCell(14).setCellStyle(greenHalfCurrency);
                        dataRow.getCell(15).setCellStyle(greenPercentage);
                        dataRow.getCell(16).setCellStyle(greenFullCurrency);
                    }
                    rowCount++;
                }

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
                if (data.getStatus().equals("active")) {
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
                    if(data.getStartDay() != null && data.getEndDay() != null) {
                        dataRow.createCell(10).setCellValue(data.getStartDay().getMonthOfYear() + "/" +
                                data.getStartDay().getDayOfMonth());
                        dataRow.createCell(11).setCellValue(data.getEndDay().getMonthOfYear() + "/" +
                                data.getEndDay().getDayOfMonth());
                        dataRow.createCell(12).setCellValue(data.getPercentThroughFlight());
                        dataRow.createCell(13).setCellValue(data.getDailyBudget());
                        dataRow.createCell(14).setCellValue(data.getLifetimeBudget());
                        dataRow.createCell(15).setCellValue(data.getPercentThroughLifetimeBudget());
                        dataRow.createCell(16).setCellValue(data.getSuggestedDailyBudget());
                    } else {
                        dataRow.createCell(10);
                        dataRow.createCell(11);
                        dataRow.createCell(12);
                        dataRow.createCell(13);
                        dataRow.createCell(14);
                        dataRow.createCell(15);
                        dataRow.createCell(16);
                    }

                    dataRow.getCell(5).setCellStyle(fullCurrency);
                    dataRow.getCell(6).setCellStyle(ctrPercentage);
                    dataRow.getCell(7).setCellStyle(ctrPercentage);
                    dataRow.getCell(8).setCellStyle(fullCurrency);
                    dataRow.getCell(9).setCellStyle(fullCurrency);
                    dataRow.getCell(12).setCellStyle(percentage);
                    dataRow.getCell(13).setCellStyle(fullCurrency);
                    dataRow.getCell(14).setCellStyle(halfCurrency);
                    dataRow.getCell(15).setCellStyle(percentage);
                    dataRow.getCell(16).setCellStyle(fullCurrency);

                    //Pattern rows
                    if(rowCount % 2 == 1) {
                        dataRow.getCell(0).setCellStyle(greenStyle);
                        dataRow.getCell(1).setCellStyle(greenStyle);
                        dataRow.getCell(2).setCellStyle(greenStyle);
                        dataRow.getCell(3).setCellStyle(greenStyle);
                        dataRow.getCell(4).setCellStyle(greenStyle);
                        dataRow.getCell(5).setCellStyle(greenFullCurrency);
                        dataRow.getCell(6).setCellStyle(greenCtrPercentage);
                        dataRow.getCell(7).setCellStyle(greenCtrPercentage);
                        dataRow.getCell(8).setCellStyle(greenFullCurrency);
                        dataRow.getCell(9).setCellStyle(greenFullCurrency);
                        dataRow.getCell(10).setCellStyle(greenStyle);
                        dataRow.getCell(11).setCellStyle(greenStyle);
                        dataRow.getCell(12).setCellStyle(greenPercentage);
                        dataRow.getCell(13).setCellStyle(greenFullCurrency);
                        dataRow.getCell(14).setCellStyle(greenHalfCurrency);
                        dataRow.getCell(15).setCellStyle(greenPercentage);
                        dataRow.getCell(16).setCellStyle(greenFullCurrency);
                    }
                    rowCount++;
                }

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
