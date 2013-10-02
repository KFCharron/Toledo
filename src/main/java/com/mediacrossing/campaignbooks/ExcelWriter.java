package com.mediacrossing.campaignbooks;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.joda.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class ExcelWriter {

    private static Workbook WORKBOOK = new HSSFWorkbook();

    private static final Logger LOG = LoggerFactory.getLogger(ExcelWriter.class);

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

    public void writeLineItemSheetToWorkbook(LineItem lineItem) {

        registerLoggerWithUncaughtExceptions();


        DecimalFormat df = new DecimalFormat("#.00");

        //Create new sheet
        String sheetName = WorkbookUtil.createSafeSheetName(lineItem.getLineItemName());
        Sheet lineItemSheet = WORKBOOK.createSheet(sheetName);

        //Conditional Formatting Logic
        SheetConditionalFormatting sheetCF = lineItemSheet.getSheetConditionalFormatting();
        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(ComparisonOperator.LT, "0");
        FontFormatting fontFmt = rule1.createFontFormatting();
        fontFmt.setFontStyle(true, false);
        fontFmt.setFontColorIndex(IndexedColors.DARK_RED.index);

        ConditionalFormattingRule [] cfRules =
                {
                        rule1
                };

        CellRangeAddress[] regions = {
                CellRangeAddress.valueOf("H3:H100")
        };
        sheetCF.addConditionalFormatting(regions, cfRules);

        //Add line item header row
        Row lineItemHeader = lineItemSheet.createRow(0);
        lineItemHeader.createCell(0);
        lineItemHeader.createCell(1).setCellValue("Line Item");
        lineItemHeader.createCell(2).setCellValue("Lifetime Budget");
        lineItemHeader.createCell(3).setCellValue("Start Date");
        lineItemHeader.createCell(4).setCellValue("End Date");
        lineItemHeader.createCell(5).setCellValue("# of Days");
        lineItemHeader.createCell(6).setCellValue("Daily Budget");
        lineItemHeader.createCell(7).setCellValue("Days Remaining");

        //style header
        Font font = WORKBOOK.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBoldweight((short) 700);
        CellStyle bold = WORKBOOK.createCellStyle();
        bold.setFont(font);

        for(Cell cell : lineItemHeader) {
            cell.setCellStyle(bold);
        }

        //add line item data
        Row lineItemRow = lineItemSheet.createRow(1);
        lineItemRow.createCell(0);
        lineItemRow.createCell(1).setCellValue(lineItem.getLineItemName());
        lineItemRow.createCell(2).setCellValue(lineItem.getLifetimeBudget());
        lineItemRow.createCell(3).setCellValue(lineItem.getStartDateString());
        lineItemRow.createCell(4).setCellValue(lineItem.getEndDateString());
        lineItemRow.createCell(5).setCellValue(lineItem.getDaysActive());
        lineItemRow.createCell(6).setCellValue(lineItem.getDailyBudget());
        lineItemRow.createCell(7).setCellValue(lineItem.getDaysRemaining());

        //add campaign header row
        Row campaignHeaderRow = lineItemSheet.createRow(3);
        campaignHeaderRow.createCell(0).setCellValue("Campaign ID");
        campaignHeaderRow.createCell(1).setCellValue("Campaign Name");
        campaignHeaderRow.createCell(2).setCellValue("Lifetime Budget");
        campaignHeaderRow.createCell(3).setCellValue("Start Date");
        campaignHeaderRow.createCell(4).setCellValue("End Date");
        campaignHeaderRow.createCell(5).setCellValue("# of Days");
        campaignHeaderRow.createCell(6).setCellValue("Daily Budget");
        campaignHeaderRow.createCell(7).setCellValue("Actual Daily Budget");
        campaignHeaderRow.createCell(8).setCellValue("Total Delivery");

        //style header
        font.setFontHeightInPoints((short) 12);
        bold.setFont(font);

        for(Cell cell : campaignHeaderRow) {
            cell.setCellStyle(bold);
        }

        //repeat for each campaign
        Float totalLifetimeBudget = 0.0f;
        Float totalDailyBudget = 0.0f;
        Float totalActualDailyBudget = 0.0f;
        Float totalCumulativeDelivery = 0.0f;
        int rowCount = 4;
        for (Campaign campaign : lineItem.getCampaignList()) {
            Row campaignRow = lineItemSheet.createRow(rowCount);
            campaignRow.createCell(0).setCellValue(campaign.getCampaignID());
            campaignRow.createCell(1).setCellValue(campaign.getCampaignName());
            if (campaign.getStatus().equals("inactive")) {
                Font italics = WORKBOOK.createFont();
                italics.setItalic(true);
                CellStyle style = WORKBOOK.createCellStyle();
                style.setFont(italics);
                campaignRow.getCell(1).setCellStyle(style);
            }
            campaignRow.createCell(2).setCellValue(campaign.getLifetimeBudget());
            if (campaign.getStartDate() != null && campaign.getEndDate() != null) {
                campaignRow.createCell(3).setCellValue(campaign.getStartDate().getMonthOfYear() +
                        "/" + campaign.getStartDate().getDayOfMonth());
                campaignRow.createCell(4).setCellValue(campaign.getEndDate().getMonthOfYear() +
                        "/" + campaign.getEndDate().getDayOfMonth());
            }
            campaignRow.createCell(5).setCellValue(campaign.getDaysActive());
            campaignRow.createCell(6).setCellValue(campaign.getDailyBudget());
            campaignRow.createCell(7).setCellValue(df.format(campaign.getActualDailyBudget()));
            campaignRow.createCell(8).setCellValue(df.format(campaign.getTotalDelivery()));
            int cellCount = 9;

            //list daily deliveries
            DateTime now = new DateTime();
            Duration startToNow = new Duration(lineItem.getStartDateTime(), now);

            for (long x = startToNow.getStandardDays(); x > 0; x--) {

                campaignHeaderRow.createCell(cellCount)
                        .setCellValue(lineItem.getStartDateTime().plusDays((int)x).monthOfYear().getAsString() + "/" +
                                lineItem.getStartDateTime().plusDays((int)x).dayOfMonth().getAsString());

                for(Delivery del : campaign.getDeliveries()) {

                    if(Integer.parseInt(lineItem.getStartDateTime()
                            .plusDays((int)x).dayOfMonth().getAsString()) == del.getDate().getDayOfMonth() &&
                            Integer.parseInt(lineItem.getStartDateTime()
                                    .plusDays((int)x).monthOfYear().getAsString()) ==
                                    del.getDate().getMonthOfYear()) {

                        campaignRow.createCell(cellCount).setCellValue(df.format(del.getDelivery()));
                    }
                }
                cellCount++;
            }

            rowCount++;

            for (Cell cell : campaignRow) {
                lineItemSheet.autoSizeColumn(cell.getColumnIndex());
            }
            totalLifetimeBudget += campaign.getLifetimeBudget();
            totalDailyBudget += campaign.getDailyBudget();
            totalActualDailyBudget += campaign.getActualDailyBudget();
            totalCumulativeDelivery += campaign.getTotalDelivery();
        }
        rowCount++;
        Row totalRow = lineItemSheet.createRow(rowCount);
        totalRow.createCell(2).setCellValue(df.format(totalLifetimeBudget));
        totalRow.createCell(6).setCellValue(df.format(totalDailyBudget));
        totalRow.createCell(7).setCellValue(df.format(totalActualDailyBudget));
        totalRow.createCell(8).setCellValue(df.format(totalCumulativeDelivery));
        rowCount+=3;

        Row campHeaderRow = lineItemSheet.createRow(rowCount);
        campHeaderRow.createCell(0).setCellValue("Campaign ID");
        campHeaderRow.createCell(1).setCellValue("Campaign Name");
        campHeaderRow.createCell(2).setCellValue("Lifetime Imps");
        campHeaderRow.createCell(3).setCellValue("Lifetime Clicks");
        campHeaderRow.createCell(4).setCellValue("Lifetime CTR");
        campHeaderRow.createCell(5).setCellValue("Daily Stats:");

        for(Cell cell : campHeaderRow) {
            cell.setCellStyle(bold);
        }

        rowCount++;

        for(Campaign camp : lineItem.getCampaignList()) {
            Row campRow = lineItemSheet.createRow(rowCount);
            campRow.createCell(0).setCellValue(camp.getCampaignID());
            campRow.createCell(1).setCellValue(camp.getCampaignName());
            campRow.createCell(2).setCellValue(camp.getLifetimeImps());
            campRow.createCell(3).setCellValue(camp.getLifetimeClicks());
            campRow.createCell(4).setCellValue(camp.getLifetimeCtr());
            int cellCount = 6;

            DateTime now = new DateTime();
            Duration startToNow = new Duration(lineItem.getStartDateTime(), now);
            for (long x = startToNow.getStandardDays(); x > 0; x--) {

                campHeaderRow.createCell(cellCount)
                        .setCellValue(lineItem.getStartDateTime().plusDays((int)x).monthOfYear().getAsString() + "/" +
                                lineItem.getStartDateTime().plusDays((int)x).dayOfMonth().getAsString());

                Boolean blankCells = true;
                for(Delivery del : camp.getDeliveries()) {

                    if(Integer.parseInt(lineItem.getStartDateTime().plusDays((int)x).dayOfMonth().getAsString())
                            == del.getDate().getDayOfMonth() &&
                            Integer.parseInt(lineItem.getStartDateTime().plusDays((int)x).monthOfYear().getAsString()
                            ) == del.getDate().getMonthOfYear()) {

                        blankCells = false;

                        campRow.createCell(cellCount).setCellValue(del.getImps());
                        cellCount++;
                        campRow.createCell(cellCount).setCellValue(del.getClicks());
                        cellCount++;
                    }
                }
                if(blankCells) {
                    cellCount += 2;
                }
            }
            rowCount++;
        }
    }

    public void writeWorkbookToFileWithOutputPath(String outputPath) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(new File(outputPath, "CampaignBooks.xls"));
        WORKBOOK.write(fileOut);
        fileOut.close();
    }

    public static void writeDailyAdvertiserReport(List<Advertiser> advertiserList, String outputPath)
            throws IOException {

        registerLoggerWithUncaughtExceptions();

        WORKBOOK = new HSSFWorkbook();
        Sheet reportSheet = WORKBOOK.createSheet("Daily Advertiser Report");
        DecimalFormat df = new DecimalFormat("#.00");

        Row titleRow = reportSheet.createRow(0);
        titleRow.createCell(0).setCellValue("Overview Report (campaigns with active delivery)");

        Row subtitleRow = reportSheet.createRow(2);
        subtitleRow.createCell(0).setCellValue("Last 24 hours only:");

        Row headerRow = reportSheet.createRow(4);
        headerRow.createCell(0).setCellValue("Line Item ID");
        headerRow.createCell(1).setCellValue("Line Item Name");
        headerRow.createCell(2).setCellValue("Imps");
        headerRow.createCell(3).setCellValue("Clicks");
        headerRow.createCell(4).setCellValue("Total Convs");
        headerRow.createCell(5).setCellValue("Media Cost");
        headerRow.createCell(6).setCellValue("% Daily Spent");
        headerRow.createCell(7).setCellValue("CTR");
        headerRow.createCell(8).setCellValue("Conv Rate");
        headerRow.createCell(9).setCellValue("CPM");
        headerRow.createCell(10).setCellValue("CPC");
        headerRow.createCell(11).setCellValue("Start Day");
        headerRow.createCell(12).setCellValue("End Day");
        headerRow.createCell(13).setCellValue("% Through Flight");
        headerRow.createCell(14).setCellValue("% Through Total Budget");
        headerRow.createCell(15).setCellValue("Req'd Daily Del. To Fill");

        int rowCount = 5;
        for(Advertiser ad : advertiserList) {
            if (ad.isLive()) {
                for(LineItem li : ad.getLineItemList()) {
                    Row dataRow = reportSheet.createRow(rowCount);
                    dataRow.createCell(0).setCellValue(li.getLineItemID());
                    dataRow.createCell(1).setCellValue(li.getLineItemName());
                    dataRow.createCell(2).setCellValue(li.getDayReportData().getImps());
                    dataRow.createCell(3).setCellValue(li.getDayReportData().getClicks());
                    dataRow.createCell(4).setCellValue(li.getDayReportData().getTotalConversions());
                    dataRow.createCell(5).setCellValue(li.getDayReportData().getMediaCost());
                    dataRow.createCell(6).setCellValue(
                            df.format(li.getDayReportData().getMediaCost() / li.getDailyBudget() * 100) + "%");
                    dataRow.createCell(7).setCellValue(li.getDayReportData().getCtr());
                    dataRow.createCell(8).setCellValue(li.getDayReportData().getConversionRate());
                    dataRow.createCell(9).setCellValue(li.getDayReportData().getCpm());
                    dataRow.createCell(10).setCellValue(li.getDayReportData().getCpc());
                    dataRow.createCell(11).setCellValue(li.getStartDateString());
                    dataRow.createCell(12).setCellValue(li.getEndDateString());
                    dataRow.createCell(13).setCellValue(li.getFlightPercentage() + "%");
                    dataRow.createCell(14).setCellValue(
                            df.format(li.getLifetimeReportData().getMediaCost() / li.getLifetimeBudget() * 100) + "%");
                    dataRow.createCell(15).setCellValue(
                            (li.getLifetimeBudget() - li.getLifetimeReportData().getMediaCost())
                                    / li.getDaysRemaining());

                    rowCount++;
                }

            }
        }

        Row campHeaderRow = reportSheet.createRow(rowCount);
        campHeaderRow.createCell(0).setCellValue("Campaign ID");
        campHeaderRow.createCell(1).setCellValue("Campaign Name");
        campHeaderRow.createCell(2).setCellValue("Imps");
        campHeaderRow.createCell(3).setCellValue("Clicks");
        campHeaderRow.createCell(4).setCellValue("Total Convs");
        campHeaderRow.createCell(5).setCellValue("Media Cost");
        campHeaderRow.createCell(6).setCellValue("% Daily Spent");
        campHeaderRow.createCell(7).setCellValue("CTR");
        campHeaderRow.createCell(8).setCellValue("Conv Rate");
        campHeaderRow.createCell(9).setCellValue("CPM");
        campHeaderRow.createCell(10).setCellValue("CPC");
        campHeaderRow.createCell(11).setCellValue("Start Day");
        campHeaderRow.createCell(12).setCellValue("End Day");
        campHeaderRow.createCell(13).setCellValue("% Through Flight");
        campHeaderRow.createCell(14).setCellValue("% Through Total Budget");
        campHeaderRow.createCell(15).setCellValue("Req'd Daily Del. To Fill");
        
        rowCount++;

        for(Advertiser ad : advertiserList) {
            if (ad.isLive()) {
                for(LineItem li : ad.getLineItemList()) {
                    for(Campaign camp : li.getCampaignList()) {
                        Row dataRow = reportSheet.createRow(rowCount);
                        dataRow.createCell(0).setCellValue(camp.getCampaignID());
                        dataRow.createCell(1).setCellValue(camp.getCampaignName());
                        dataRow.createCell(2).setCellValue(camp.getDayReportData().getImps());
                        dataRow.createCell(3).setCellValue(camp.getDayReportData().getClicks());
                        dataRow.createCell(4).setCellValue(camp.getDayReportData().getTotalConversions());
                        dataRow.createCell(5).setCellValue(camp.getDayReportData().getMediaCost());
                        dataRow.createCell(6).setCellValue(
                                df.format(camp.getDayReportData().getMediaCost() / camp.getDailyBudget() * 100) + "%");
                        dataRow.createCell(7).setCellValue(camp.getDayReportData().getCtr());
                        dataRow.createCell(8).setCellValue(camp.getDayReportData().getConversionRate());
                        dataRow.createCell(9).setCellValue(camp.getDayReportData().getCpm());
                        dataRow.createCell(10).setCellValue(camp.getDayReportData().getCpc());
                        //FIXME
                        //dataRow.createCell(11).setCellValue(camp.getStartDateString());
                        //dataRow.createCell(12).setCellValue(camp.getEndDateString());
                        dataRow.createCell(13).setCellValue(camp.getFlightPercentage() + "%");
                        dataRow.createCell(14).setCellValue(
                                df.format(camp.getLifetimeReportData().getMediaCost() / camp.getLifetimeBudget() * 100)
                                        + "%");
                        dataRow.createCell(15).setCellValue(
                                (camp.getLifetimeBudget() - camp.getLifetimeReportData().getMediaCost())
                                        / camp.getDaysRemaining());

                        rowCount++;  
                    }
                }
            }
        }

        rowCount++;

        Row lifetimeSubtitle = reportSheet.createRow(rowCount);
        lifetimeSubtitle.createCell(0).setCellValue("Lifetime Total:");

        rowCount++;

        Row hRow = reportSheet.createRow(rowCount);
        hRow.createCell(0).setCellValue("Line Item ID");
        hRow.createCell(1).setCellValue("Line Item Name");
        hRow.createCell(2).setCellValue("Imps");
        hRow.createCell(3).setCellValue("Clicks");
        hRow.createCell(4).setCellValue("Total Convs");
        hRow.createCell(5).setCellValue("Media Cost");
        hRow.createCell(6).setCellValue("% Daily Spent");
        hRow.createCell(7).setCellValue("CTR");
        hRow.createCell(8).setCellValue("Conv Rate");
        hRow.createCell(9).setCellValue("CPM");
        hRow.createCell(10).setCellValue("CPC");
        hRow.createCell(11).setCellValue("Start Day");
        hRow.createCell(12).setCellValue("End Day");
        hRow.createCell(13).setCellValue("% Through Flight");
        hRow.createCell(14).setCellValue("% Through Total Budget");
        hRow.createCell(15).setCellValue("Req'd Daily Del. To Fill");
        
        rowCount++;

        for(Advertiser ad : advertiserList) {
            if (ad.isLive()) {
                for(LineItem li : ad.getLineItemList()) {
                    Row dataRow = reportSheet.createRow(rowCount);
                    dataRow.createCell(0).setCellValue(li.getLineItemID());
                    dataRow.createCell(1).setCellValue(li.getLineItemName());
                    dataRow.createCell(2).setCellValue(li.getLifetimeReportData().getImps());
                    dataRow.createCell(3).setCellValue(li.getLifetimeReportData().getClicks());
                    dataRow.createCell(4).setCellValue(li.getLifetimeReportData().getTotalConversions());
                    dataRow.createCell(5).setCellValue(li.getLifetimeReportData().getMediaCost());
                    dataRow.createCell(6).setCellValue(
                            df.format(li.getLifetimeReportData().getMediaCost() / li.getDailyBudget() * 100) + "%");
                    dataRow.createCell(7).setCellValue(li.getLifetimeReportData().getCtr());
                    dataRow.createCell(8).setCellValue(li.getLifetimeReportData().getConversionRate());
                    dataRow.createCell(9).setCellValue(li.getLifetimeReportData().getCpm());
                    dataRow.createCell(10).setCellValue(li.getLifetimeReportData().getCpc());
                    dataRow.createCell(11).setCellValue(li.getStartDateString());
                    dataRow.createCell(12).setCellValue(li.getEndDateString());
                    dataRow.createCell(13).setCellValue(li.getFlightPercentage() + "%");
                    dataRow.createCell(14).setCellValue(
                            df.format(li.getLifetimeReportData().getMediaCost() / li.getLifetimeBudget() * 100) + "%");
                    dataRow.createCell(15).setCellValue(
                            (li.getLifetimeBudget() - li.getLifetimeReportData().getMediaCost())
                                    / li.getDaysRemaining());

                    rowCount++;
                }

            }
        }

        Row campHeadRow = reportSheet.createRow(rowCount);
        campHeadRow.createCell(0).setCellValue("Campaign ID");
        campHeadRow.createCell(1).setCellValue("Campaign Name");
        campHeadRow.createCell(2).setCellValue("Imps");
        campHeadRow.createCell(3).setCellValue("Clicks");
        campHeadRow.createCell(4).setCellValue("Total Convs");
        campHeadRow.createCell(5).setCellValue("Media Cost");
        campHeadRow.createCell(6).setCellValue("% Daily Spent");
        campHeadRow.createCell(7).setCellValue("CTR");
        campHeadRow.createCell(8).setCellValue("Conv Rate");
        campHeadRow.createCell(9).setCellValue("CPM");
        campHeadRow.createCell(10).setCellValue("CPC");
        campHeadRow.createCell(11).setCellValue("Start Day");
        campHeadRow.createCell(12).setCellValue("End Day");
        campHeadRow.createCell(13).setCellValue("% Through Flight");
        campHeadRow.createCell(14).setCellValue("% Through Total Budget");
        campHeadRow.createCell(15).setCellValue("Req'd Daily Del. To Fill");

        rowCount++;

        for(Advertiser ad : advertiserList) {
            if (ad.isLive()) {
                for(LineItem li : ad.getLineItemList()) {
                    for(Campaign camp : li.getCampaignList()) {
                        LOG.debug(li.toString());
                        Row dataRow = reportSheet.createRow(rowCount);
                        dataRow.createCell(0).setCellValue(camp.getCampaignID());
                        dataRow.createCell(1).setCellValue(camp.getCampaignName());
                        dataRow.createCell(2).setCellValue(camp.getLifetimeReportData().getImps());
                        dataRow.createCell(3).setCellValue(camp.getLifetimeReportData().getClicks());
                        dataRow.createCell(4).setCellValue(camp.getLifetimeReportData().getTotalConversions());
                        dataRow.createCell(5).setCellValue(camp.getLifetimeReportData().getMediaCost());
                        dataRow.createCell(6).setCellValue(
                                df.format(camp.getLifetimeReportData().getMediaCost() / camp.getDailyBudget() * 100)
                                        + "%");
                        dataRow.createCell(7).setCellValue(camp.getLifetimeReportData().getCtr());
                        dataRow.createCell(8).setCellValue(camp.getLifetimeReportData().getConversionRate());
                        dataRow.createCell(9).setCellValue(camp.getLifetimeReportData().getCpm());
                        dataRow.createCell(10).setCellValue(camp.getLifetimeReportData().getCpc());
                        //FIXME
                        //dataRow.createCell(11).setCellValue(camp.getStartDateString());
                        //dataRow.createCell(12).setCellValue(camp.getEndDateString());
                        dataRow.createCell(13).setCellValue(camp.getFlightPercentage() + "%");
                        dataRow.createCell(14).setCellValue(
                                df.format(camp.getLifetimeReportData().getMediaCost() / camp.getLifetimeBudget() * 100)
                                        + "%");
                        dataRow.createCell(15).setCellValue(
                                (camp.getLifetimeBudget() - camp.getLifetimeReportData().getMediaCost())
                                        / camp.getDaysRemaining());

                        rowCount++;
                    }
                }
            }
        }
        FileOutputStream fileOut = new FileOutputStream(new File(outputPath, "DailyCampaignReport.xls"));
        WORKBOOK.write(fileOut);
        fileOut.close();
    }
}
