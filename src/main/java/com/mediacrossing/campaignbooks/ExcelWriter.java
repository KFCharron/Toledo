package com.mediacrossing.campaignbooks;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.joda.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

public class ExcelWriter {

    private static Workbook WORKBOOK = new HSSFWorkbook();

    private static final Logger LOG = LoggerFactory.getLogger(ExcelWriter.class);

    public static void writeAdvertiserSheetToWorkbook(Advertiser ad) {

        DecimalFormat df = new DecimalFormat("#.00");

        //Create new sheet
        String sheetName = WorkbookUtil.createSafeSheetName(ad.getAdvertiserID());
        Sheet lineItemSheet = WORKBOOK.createSheet(sheetName);
        int rowCount = 0;

        for (LineItem lineItem : ad.getLineItemList()) {

            //Add line item header row
            Row lineItemHeader = lineItemSheet.createRow(rowCount);
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

            rowCount++;

            //add line item data
            Row lineItemRow = lineItemSheet.createRow(rowCount);
            lineItemRow.createCell(0);
            lineItemRow.createCell(1).setCellValue(lineItem.getLineItemName());
            lineItemRow.createCell(2).setCellValue("$" + lineItem.getLifetimeBudget());
            lineItemRow.createCell(3).setCellValue(lineItem.getStartDateString());
            lineItemRow.createCell(4).setCellValue(lineItem.getEndDateString());
            lineItemRow.createCell(5).setCellValue(lineItem.getDaysActive());
            lineItemRow.createCell(6).setCellValue("$" + lineItem.getDailyBudget());
            lineItemRow.createCell(7).setCellValue(lineItem.getDaysRemaining());

            rowCount+=2;

            //add campaign header row
            Row campaignHeaderRow = lineItemSheet.createRow(rowCount);
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

            rowCount++;

            //repeat for each campaign
            Float totalLifetimeBudget = 0.0f;
            Float totalDailyBudget = 0.0f;
            Float totalActualDailyBudget = 0.0f;
            Float totalCumulativeDelivery = 0.0f;
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
                } else {
                    Font bolding = WORKBOOK.createFont();
                    bolding.setBoldweight((short)500);
                    CellStyle bolds = WORKBOOK.createCellStyle();
                    bolds.setFont(bolding);
                    campaignRow.getCell(1).setCellStyle(bolds);
                }
                campaignRow.createCell(2).setCellValue("$" + campaign.getLifetimeBudget());
                if (campaign.getStartDate() != null && campaign.getEndDate() != null) {
                    campaignRow.createCell(3).setCellValue(campaign.getStartDate().getMonthOfYear() +
                            "/" + campaign.getStartDate().getDayOfMonth());
                    campaignRow.createCell(4).setCellValue(campaign.getEndDate().getMonthOfYear() +
                            "/" + campaign.getEndDate().getDayOfMonth());
                }
                campaignRow.createCell(5).setCellValue(campaign.getDaysActive());
                campaignRow.createCell(6).setCellValue("$" + campaign.getDailyBudget());
                campaignRow.createCell(7).setCellValue("$" + df.format(campaign.getActualDailyBudget()));
                campaignRow.createCell(8).setCellValue("$" + df.format(campaign.getTotalDelivery()));

                //add yellow if total delivery within 2 daily budgets of lifetime budget
                CellStyle yellowStyle = WORKBOOK.createCellStyle();
                yellowStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.index);
                yellowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                if (campaign.getTotalDelivery() >= campaign.getLifetimeBudget() - (campaign.getDailyBudget()*2)) {
                    campaignRow.getCell(8).setCellStyle(yellowStyle);
                }
                //add red if total delivery equals or exceeds lifetime budget
                CellStyle redStyle = WORKBOOK.createCellStyle();
                redStyle.setFillForegroundColor(IndexedColors.RED.index);
                redStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                if (campaign.getTotalDelivery() >= campaign.getLifetimeBudget()) {
                    campaignRow.getCell(8).setCellStyle(redStyle);
                }

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

                            campaignRow.createCell(cellCount).setCellValue("$" + df.format(del.getDelivery()));
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
            totalRow.createCell(2).setCellValue("$" + df.format(totalLifetimeBudget));
            totalRow.createCell(6).setCellValue("$" + df.format(totalDailyBudget));
            totalRow.createCell(7).setCellValue("$" + df.format(totalActualDailyBudget));
            totalRow.createCell(8).setCellValue("$" + df.format(totalCumulativeDelivery));
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
                //TODO add % sign, df
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
            rowCount+=3;
        }
    }

    public static void writeWorkbookToFileWithOutputPath(String outputPath) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(new File(outputPath, "CampaignBooks.xls"));
        WORKBOOK.write(fileOut);
        fileOut.close();
    }
}
