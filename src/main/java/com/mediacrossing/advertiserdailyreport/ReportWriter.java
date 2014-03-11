package com.mediacrossing.advertiserdailyreport;

import com.mediacrossing.campaignbooks.Advertiser;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ReportWriter {

    private static final Logger LOG = LoggerFactory.getLogger(ReportWriter.class);


    public static void writeAdvertiserDailyReport(List<Advertiser> advertiserList, String outputPath)
            throws IOException {

        LOG.debug("ReportWriter Called");

        //init workbook, decimal formats
        Workbook wb = new HSSFWorkbook();

        //Create new sheet
        Sheet sheet = wb.createSheet("24hr Line Items");

        //set up formatting styles
        DataFormat df = wb.createDataFormat();

        CellStyle fullCurrency = wb.createCellStyle();
        fullCurrency.setDataFormat(df.getFormat("$#,##0.00"));

        CellStyle halfCurrency = wb.createCellStyle();
        halfCurrency.setDataFormat(df.getFormat("$#,##0"));

        CellStyle percentage = wb.createCellStyle();
        percentage.setDataFormat(df.getFormat("0%"));

        CellStyle ctrPercentage = wb.createCellStyle();
        ctrPercentage.setDataFormat(df.getFormat("#.##%"));

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
        font.setBoldweight((short) 700);
        CellStyle bold = wb.createCellStyle();
        bold.setFont(font);

        //bold the header
        titleRow.getCell(1).setCellStyle(bold);

        //create 24hr subheader
        Row subtitleRow = sheet.createRow(2);
        subtitleRow.createCell(0).setCellValue("24 hours:");

        //create line item column names
        LOG.debug("Starting Line Item Header");
        Row lineItemHeader = sheet.createRow(4);
        lineItemHeader.createCell(0).setCellValue("Line Item ID");
        lineItemHeader.createCell(1).setCellValue("Line Item Name");
        lineItemHeader.createCell(2).setCellValue("Imps");
        lineItemHeader.createCell(3).setCellValue("Clicks");
        lineItemHeader.createCell(4).setCellValue("Total Conv");
        lineItemHeader.createCell(5).setCellValue("Media Cost");
        lineItemHeader.createCell(6).setCellValue("CTR (bp)");
        lineItemHeader.createCell(7).setCellValue("Conv Rate (bp)");
        lineItemHeader.createCell(8).setCellValue("CPM");
        lineItemHeader.createCell(9).setCellValue("CPC");
        lineItemHeader.createCell(10).setCellValue("Start Date");
        lineItemHeader.createCell(11).setCellValue("End Date");
        lineItemHeader.createCell(12).setCellValue("% Flight");
        lineItemHeader.createCell(13).setCellValue("Daily Budget");
        lineItemHeader.createCell(14).setCellValue("Total Budget");
        LOG.debug("Finished Header.");

        //for every advertiser, add a data row for each line item
        int rowCount = 5;
        int impTotal = 0;
        int clickTotal = 0;
        int convTotal = 0;
        float mediaCostTotal = 0.0f;
        float cpmTotal = 0.0f;
        float cpcTotal = 0.0f;
        for(Advertiser ad : advertiserList) {
            LOG.debug("Starting Advertiser " + ad.getAdvertiserID());
            for(DailyData data : ad.getDailyLineItems()) {
                Row dataRow = sheet.createRow(rowCount);
                dataRow.createCell(0).setCellValue(data.getId());
                dataRow.createCell(1).setCellValue(data.getName());
                dataRow.createCell(2).setCellValue(data.getImps());
                dataRow.createCell(3).setCellValue(data.getClicks());
                dataRow.createCell(4).setCellValue(data.getTotalConv());
                dataRow.createCell(5).setCellValue(data.getMediaCost());
                dataRow.createCell(6).setCellValue(data.getCtr() * 10000);
                dataRow.createCell(7).setCellValue(data.getConvRate() * 10000);
                dataRow.createCell(8).setCellValue(data.getCpm());
                dataRow.createCell(9).setCellValue(data.getCpc());
                if(data.getStartDay() != null && data.getEndDay() != null) {
                    dataRow.createCell(10).setCellValue(data.getStartDay().getMonthOfYear() + "/" +
                        data.getStartDay().getDayOfMonth());
                    dataRow.createCell(11).setCellValue(data.getEndDay().getMonthOfYear() + "/" +
                        data.getEndDay().getDayOfMonth());
                    dataRow.createCell(12).setCellValue(data.getPercentThroughFlight());
                    if (data.getDailyBudget() > 0) {
                        dataRow.createCell(13).setCellValue(data.getDailyBudget());
                        dataRow.getCell(13).setCellStyle(fullCurrency);
                    } else {
                        dataRow.createCell(13).setCellValue(data.getDailyBudgetImps());
                    }
                    if (data.getLifetimeBudget() > 0) {
                        LOG.debug("Lifetime B > 0");
                        dataRow.createCell(14).setCellValue(data.getLifetimeBudget());
                        dataRow.getCell(14).setCellStyle(halfCurrency);
                    } else {
                        dataRow.createCell(14).setCellValue(data.getLifetimeBudgetImps());
                    }
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
                    if (data.getLifetimeBudget() > 0) {
                        dataRow.getCell(13).setCellStyle(greenFullCurrency);
                        dataRow.getCell(14).setCellStyle(greenHalfCurrency);
                    } else {
                        dataRow.getCell(13).setCellStyle(greenStyle);
                        dataRow.getCell(14).setCellStyle(greenStyle);
                    }

                }
                rowCount++;

                //add to totals
                impTotal += data.getImps();
                clickTotal += data.getClicks();
                convTotal += data.getTotalConv();
                mediaCostTotal += data.getMediaCost();
                cpmTotal += data.getCpm();
                cpcTotal += data.getCpc();
            }
            LOG.debug("Finished Advertiser " + ad.getAdvertiserID());

        }
        rowCount++;
        //populate total row
        LOG.debug("Starting Total Row");
        Row totalRow = sheet.createRow(rowCount);
        totalRow.createCell(1).setCellValue("Totals:");
        totalRow.createCell(2).setCellValue(impTotal);
        totalRow.createCell(3).setCellValue(clickTotal);
        totalRow.createCell(4).setCellValue(convTotal);
        totalRow.createCell(5).setCellValue(mediaCostTotal);
        totalRow.createCell(8).setCellValue(cpmTotal);
        totalRow.createCell(9).setCellValue(cpcTotal);

        //style total row
        totalRow.getCell(5).setCellStyle(fullCurrency);
        totalRow.getCell(8).setCellStyle(fullCurrency);
        totalRow.getCell(9).setCellStyle(fullCurrency);

        //autosize columns
        for (int x = 0; x < 15; x++) {
            sheet.autoSizeColumn(x);
        }

        //add column names for campaign header

        sheet = wb.createSheet("24hr Campaigns");
        rowCount = 0;
        LOG.debug("Finished Total Row.");
        LOG.debug("Starting Campaign Header");
        Row campaignHeader = sheet.createRow(rowCount);
        campaignHeader.createCell(0).setCellValue("Campaign ID");
        campaignHeader.createCell(1).setCellValue("Campaign Name");
        campaignHeader.createCell(2).setCellValue("Imps");
        campaignHeader.createCell(3).setCellValue("Clicks");
        campaignHeader.createCell(4).setCellValue("Total Conv");
        campaignHeader.createCell(5).setCellValue("Media Cost");
        campaignHeader.createCell(6).setCellValue("CTR (bp)");
        campaignHeader.createCell(7).setCellValue("Conv Rate (bp)");
        campaignHeader.createCell(8).setCellValue("CPM");
        campaignHeader.createCell(9).setCellValue("CPC");
        campaignHeader.createCell(10).setCellValue("Start Date");
        campaignHeader.createCell(11).setCellValue("End Date");
        campaignHeader.createCell(12).setCellValue("% Flight");
        campaignHeader.createCell(13).setCellValue("Daily Budget");
        campaignHeader.createCell(14).setCellValue("Lifetime Budget");

        LOG.debug("Finished Campaign Header");

        rowCount++;

        //add data row for each campaign
        impTotal = 0;
        clickTotal = 0;
        convTotal = 0;
        mediaCostTotal = 0.0f;
        cpmTotal = 0.0f;
        cpcTotal = 0.0f;
        for(Advertiser ad : advertiserList) {
            LOG.debug("Started Advertiser " + ad.getAdvertiserID());
            for(DailyData data : ad.getDailyCampaigns()) {
                Row dataRow = sheet.createRow(rowCount);
                dataRow.createCell(0).setCellValue(data.getId());
                dataRow.createCell(1).setCellValue(data.getName());
                dataRow.createCell(2).setCellValue(data.getImps());
                dataRow.createCell(3).setCellValue(data.getClicks());
                dataRow.createCell(4).setCellValue(data.getTotalConv());
                dataRow.createCell(5).setCellValue(data.getMediaCost());
                dataRow.createCell(6).setCellValue(data.getCtr() * 10000);
                dataRow.createCell(7).setCellValue(data.getConvRate() * 10000);
                dataRow.createCell(8).setCellValue(data.getCpm());
                dataRow.createCell(9).setCellValue(data.getCpc());
                if(data.getStartDay() != null && data.getEndDay() != null) {
                    dataRow.createCell(10).setCellValue(data.getStartDay().getMonthOfYear() + "/" +
                        data.getStartDay().getDayOfMonth());
                    dataRow.createCell(11).setCellValue(data.getEndDay().getMonthOfYear() + "/" +
                        data.getEndDay().getDayOfMonth());
                    dataRow.createCell(12).setCellValue(data.getPercentThroughFlight());
                    if (data.getDailyBudget() > 0) {
                        dataRow.createCell(13).setCellValue(data.getDailyBudget());
                        dataRow.getCell(13).setCellStyle(fullCurrency);
                    } else {
                        dataRow.createCell(13).setCellValue(data.getDailyBudgetImps());
                    }
                    if (data.getLifetimeBudget() > 0) {
                        dataRow.createCell(14).setCellValue(data.getLifetimeBudget());
                        dataRow.getCell(14).setCellStyle(halfCurrency);
                    } else {
                        dataRow.createCell(14).setCellValue(data.getLifetimeBudgetImps());
                    }
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
                    if (data.getDailyBudget() > 0) {
                        dataRow.getCell(13).setCellStyle(greenFullCurrency);
                    } else dataRow.getCell(13).setCellStyle(greenStyle);
                    if (data.getLifetimeBudget() > 0) {
                        dataRow.getCell(14).setCellStyle(greenHalfCurrency);
                    } else dataRow.getCell(14).setCellStyle(greenStyle);
                }

                rowCount++;

                //add to totals
                impTotal += data.getImps();
                clickTotal += data.getClicks();
                convTotal += data.getTotalConv();
                mediaCostTotal += data.getMediaCost();
                cpmTotal += data.getCpm();
                cpcTotal += data.getCpc();
            }
            LOG.debug("Finished Advertiser " + ad.getAdvertiserID());
        }
        rowCount++;

        LOG.debug("Starting Total Row");
        //populate total row
        totalRow = sheet.createRow(rowCount);
        totalRow.createCell(1).setCellValue("Totals:");
        totalRow.createCell(2).setCellValue(impTotal);
        totalRow.createCell(3).setCellValue(clickTotal);
        totalRow.createCell(4).setCellValue(convTotal);
        totalRow.createCell(5).setCellValue(mediaCostTotal);
        totalRow.createCell(8).setCellValue(cpmTotal);
        totalRow.createCell(9).setCellValue(cpcTotal);

        //style total row
        totalRow.getCell(5).setCellStyle(fullCurrency);
        totalRow.getCell(8).setCellStyle(fullCurrency);
        totalRow.getCell(9).setCellStyle(fullCurrency);

        //autosize columns
        for (int x = 0; x < 15; x++) {
            sheet.autoSizeColumn(x);
        }

        LOG.debug("Finished Total Row");

        sheet = wb.createSheet("Lifetime Line Items");
        rowCount = 0;
        //repeat for lifetime stats
        Row nextSubtitleRow = sheet.createRow(rowCount);
        nextSubtitleRow.createCell(0).setCellValue("Lifetime:");

        rowCount++;

        Row nextLineItemHeader = sheet.createRow(rowCount);
        nextLineItemHeader.createCell(0).setCellValue("Line Item ID");
        nextLineItemHeader.createCell(1).setCellValue("Line Item Name");
        nextLineItemHeader.createCell(2).setCellValue("Imps");
        nextLineItemHeader.createCell(3).setCellValue("Clicks");
        nextLineItemHeader.createCell(4).setCellValue("Total Conv");
        nextLineItemHeader.createCell(5).setCellValue("Media Cost");
        nextLineItemHeader.createCell(6).setCellValue("CTR (bp)");
        nextLineItemHeader.createCell(7).setCellValue("Conv Rate (bp)");
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

        //add data row for each campaign
        impTotal = 0;
        clickTotal = 0;
        convTotal = 0;
        mediaCostTotal = 0.0f;
        cpmTotal = 0.0f;
        cpcTotal = 0.0f;
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
                    dataRow.createCell(6).setCellValue(data.getCtr() * 10000);
                    dataRow.createCell(7).setCellValue(data.getConvRate() * 10000);
                    dataRow.createCell(8).setCellValue(data.getCpm());
                    dataRow.createCell(9).setCellValue(data.getCpc());
                    if(data.getStartDay() != null && data.getEndDay() != null) {
                        dataRow.createCell(10).setCellValue(data.getStartDay().getMonthOfYear() + "/" +
                                data.getStartDay().getDayOfMonth());
                        dataRow.createCell(11).setCellValue(data.getEndDay().getMonthOfYear() + "/" +
                                data.getEndDay().getDayOfMonth());
                        dataRow.createCell(12).setCellValue(data.getPercentThroughFlight());
                        if (data.getDailyBudget() > 0) {
                            dataRow.createCell(13).setCellValue(data.getDailyBudget());
                            dataRow.getCell(13).setCellStyle(fullCurrency);
                        } else dataRow.createCell(13).setCellValue(data.getDailyBudgetImps());
                        if (data.getLifetimeBudget() > 0) {
                            dataRow.createCell(14).setCellValue(data.getLifetimeBudget());
                            dataRow.getCell(14).setCellStyle(halfCurrency);
                        } else dataRow.createCell(14).setCellValue(data.getLifetimeBudgetImps());
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
                    dataRow.getCell(15).setCellStyle(percentage);
                    if (data.getDailyBudget() > 0) {
                        dataRow.getCell(16).setCellStyle(fullCurrency);
                    }
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
                        if (data.getLifetimeBudget() > 0) {
                            dataRow.getCell(13).setCellStyle(greenFullCurrency);
                            dataRow.getCell(14).setCellStyle(greenHalfCurrency);
                            dataRow.getCell(16).setCellStyle(greenFullCurrency);
                        } else {
                            dataRow.getCell(13).setCellStyle(greenStyle);
                            dataRow.getCell(14).setCellStyle(greenStyle);
                            dataRow.getCell(16).setCellStyle(greenStyle);
                        }
                        dataRow.getCell(15).setCellStyle(greenPercentage);
                    }
                    rowCount++;

                    //add to totals
                    impTotal += data.getImps();
                    clickTotal += data.getClicks();
                    convTotal += data.getTotalConv();
                    mediaCostTotal += data.getMediaCost();
                    cpmTotal += data.getCpm();
                    cpcTotal += data.getCpc();
                }

            }
        }
        rowCount++;
        //populate total row
        totalRow = sheet.createRow(rowCount);
        totalRow.createCell(1).setCellValue("Totals:");
        totalRow.createCell(2).setCellValue(impTotal);
        totalRow.createCell(3).setCellValue(clickTotal);
        totalRow.createCell(4).setCellValue(convTotal);
        totalRow.createCell(5).setCellValue(mediaCostTotal);
        totalRow.createCell(8).setCellValue(cpmTotal);
        totalRow.createCell(9).setCellValue(cpcTotal);


        //style total row
        totalRow.getCell(5).setCellStyle(fullCurrency);
        totalRow.getCell(8).setCellStyle(fullCurrency);
        totalRow.getCell(9).setCellStyle(fullCurrency);

        //autosize columns
        for (int x = 0; x < 17; x++) {
            sheet.autoSizeColumn(x);
        }

        sheet = wb.createSheet("Lifetime Campaigns");
        rowCount = 0;

        Row nextCampaignHeader = sheet.createRow(rowCount);
        nextCampaignHeader.createCell(0).setCellValue("Campaign ID");
        nextCampaignHeader.createCell(1).setCellValue("Campaign Name");
        nextCampaignHeader.createCell(2).setCellValue("Imps");
        nextCampaignHeader.createCell(3).setCellValue("Clicks");
        nextCampaignHeader.createCell(4).setCellValue("Total Conv");
        nextCampaignHeader.createCell(5).setCellValue("Media Cost");
        nextCampaignHeader.createCell(6).setCellValue("CTR (bp)");
        nextCampaignHeader.createCell(7).setCellValue("Conv Rate (bp)");
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

        //add data row for each campaign
        impTotal = 0;
        clickTotal = 0;
        convTotal = 0;
        mediaCostTotal = 0.0f;
        cpmTotal = 0.0f;
        cpcTotal = 0.0f;
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
                    dataRow.createCell(6).setCellValue(data.getCtr() * 10000);
                    dataRow.createCell(7).setCellValue(data.getConvRate() * 10000);
                    dataRow.createCell(8).setCellValue(data.getCpm());
                    dataRow.createCell(9).setCellValue(data.getCpc());
                    if(data.getStartDay() != null && data.getEndDay() != null) {
                        dataRow.createCell(10).setCellValue(data.getStartDay().getMonthOfYear() + "/" +
                                data.getStartDay().getDayOfMonth());
                        dataRow.createCell(11).setCellValue(data.getEndDay().getMonthOfYear() + "/" +
                                data.getEndDay().getDayOfMonth());
                        dataRow.createCell(12).setCellValue(data.getPercentThroughFlight());
                        if (data.getDailyBudget() > 0) {
                            dataRow.createCell(13).setCellValue(data.getDailyBudget());
                            dataRow.createCell(14).setCellValue(data.getLifetimeBudget());
                            dataRow.getCell(13).setCellStyle(fullCurrency);
                            dataRow.getCell(14).setCellStyle(halfCurrency);
                        } else {
                            dataRow.createCell(13).setCellValue(data.getDailyBudgetImps());
                            dataRow.createCell(14).setCellValue(data.getLifetimeBudgetImps());
                        }
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
                    dataRow.getCell(15).setCellStyle(percentage);
                    if (data.getDailyBudget() > 0) {
                        dataRow.getCell(16).setCellStyle(fullCurrency);
                    }

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
                        if (data.getDailyBudget() > 0) {
                            dataRow.getCell(13).setCellStyle(greenFullCurrency);
                            dataRow.getCell(14).setCellStyle(greenHalfCurrency);
                            dataRow.getCell(16).setCellStyle(greenFullCurrency);
                        } else {
                            dataRow.getCell(13).setCellStyle(greenStyle);
                            dataRow.getCell(14).setCellStyle(greenStyle);
                            dataRow.getCell(16).setCellStyle(greenStyle);
                        }
                        dataRow.getCell(15).setCellStyle(greenPercentage);
                    }
                    rowCount++;

                    //add to totals
                    impTotal += data.getImps();
                    clickTotal += data.getClicks();
                    convTotal += data.getTotalConv();
                    mediaCostTotal += data.getMediaCost();
                    cpmTotal += data.getCpm();
                    cpcTotal += data.getCpc();
                }

            }
        }
        rowCount++;
        //populate total row
        totalRow = sheet.createRow(rowCount);
        totalRow.createCell(1).setCellValue("Totals:");
        totalRow.createCell(2).setCellValue(impTotal);
        totalRow.createCell(3).setCellValue(clickTotal);
        totalRow.createCell(4).setCellValue(convTotal);
        totalRow.createCell(5).setCellValue(mediaCostTotal);
        totalRow.createCell(8).setCellValue(cpmTotal);
        totalRow.createCell(9).setCellValue(cpcTotal);

        //style total row
        totalRow.getCell(5).setCellStyle(fullCurrency);
        totalRow.getCell(8).setCellStyle(fullCurrency);
        totalRow.getCell(9).setCellStyle(fullCurrency);

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

        LOG.debug("About to Output File");
        //output file
        LocalDate today = new LocalDate(DateTimeZone.UTC);
        FileOutputStream fileOut = new FileOutputStream(new File(outputPath, "DailyReport_"+today.toString()+".xls"));
        LOG.debug("Writing " + "DailyReport_"+today.toString()+".xls" + " to " + outputPath);
        wb.write(fileOut);
        LOG.debug("Done");
        LOG.debug("Closing File OutputStream");
        fileOut.close();
        LOG.debug("Done");
        LOG.debug("End of Class.");
    }
}
