package com.mediacrossing.campaignbooks;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.joda.time.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class ExcelWriter {

    private static Workbook WORKBOOK = new HSSFWorkbook();

    //private static final Logger LOG = LoggerFactory.getLogger(ExcelWriter.class);

    public static void writeAdvertiserSheetToWorkbook(Advertiser ad) {

        //Create new sheet
        String sheetName = WorkbookUtil.createSafeSheetName(ad.getAdvertiserName() +
                " (" +  ad.getAdvertiserID() + ")");
        Sheet lineItemSheet = WORKBOOK.createSheet(sheetName);

        int rowCount = 0;

        for (int a = ad.getLineItemList().size()-1; a >= 0; a--) {

            LineItem lineItem = ad.getLineItemList().get(a);

            //Add line item header row
            Row lineItemHeader = lineItemSheet.createRow(rowCount);
            lineItemHeader.createCell(0).setCellValue("");
            lineItemHeader.createCell(1).setCellValue("Line Item");
            lineItemHeader.createCell(2).setCellValue("Lifetime Budget");
            lineItemHeader.createCell(3).setCellValue("Start Date");
            lineItemHeader.createCell(4).setCellValue("End Date");
            lineItemHeader.createCell(5).setCellValue("# of Days");
            lineItemHeader.createCell(6).setCellValue("Daily Budget");
            lineItemHeader.createCell(7).setCellValue("Total Pacing");
            lineItemHeader.createCell(8).setCellValue("LT Budget Used");
            lineItemHeader.createCell(10).setCellValue("Days Remaining");
            lineItemHeader.createCell(9).setCellValue("Duration Passed");

            //style header
            Font font = WORKBOOK.createFont();
            font.setFontHeightInPoints((short) 14);
            font.setBoldweight((short) 700);
            CellStyle bold = WORKBOOK.createCellStyle();
            bold.setFont(font);

            //bold each cell in header
            for(Cell cell : lineItemHeader) {
                cell.setCellStyle(bold);
            }

            //create solid black cell to distinguish new line items
            CellStyle solidBlack = WORKBOOK.createCellStyle();
            solidBlack.setFillForegroundColor(IndexedColors.BLACK.index);
            solidBlack.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            lineItemHeader.getCell(0).setCellStyle(solidBlack);

            rowCount++;

            //add line item data
            Row lineItemRow = lineItemSheet.createRow(rowCount);
            lineItemRow.createCell(0);
            lineItemRow.createCell(1).setCellValue(lineItem.getLineItemName());
            lineItemRow.createCell(2).setCellValue(lineItem.getLifetimeBudget());
            lineItemRow.createCell(3).setCellValue(lineItem.getStartDateString());
            lineItemRow.createCell(4).setCellValue(lineItem.getEndDateString());
            lineItemRow.createCell(5).setCellValue(lineItem.getDaysActive());
            lineItemRow.createCell(6).setCellValue(lineItem.getDailyBudget());
            lineItemRow.createCell(7); //set after obtaining all pacing numbers
            lineItemRow.createCell(8); //same here
            lineItemRow.createCell(10).setCellValue(lineItem.getDaysRemaining());


            //get duration passed
            float flightPer = 0;
            if(lineItem.getStartDateTime() != null && lineItem.getEndDateTime() != null) {
                DateTime now = new DateTime();
                float full = Days.daysBetween(lineItem.getStartDateTime(), lineItem.getEndDateTime()).getDays();
                float stn = Days.daysBetween(lineItem.getStartDateTime(), now.toDateMidnight()).getDays();

                if(stn / full > 1) flightPer = 1;
                else flightPer = stn / full;
            }
            lineItemRow.createCell(9).setCellValue(flightPer);


            rowCount+=2;

            //add campaign header row
            Row campaignHeaderRow = lineItemSheet.createRow(rowCount);
            campaignHeaderRow.createCell(0).setCellValue("Campaign ID");
            campaignHeaderRow.createCell(1).setCellValue("Campaign Name");
            campaignHeaderRow.createCell(2).setCellValue("Lifetime Budget");
            campaignHeaderRow.createCell(3).setCellValue("Start Date");
            campaignHeaderRow.createCell(4).setCellValue("End Date");
            campaignHeaderRow.createCell(5).setCellValue("# Days");
            campaignHeaderRow.createCell(6).setCellValue("Daily Budget");
            campaignHeaderRow.createCell(7).setCellValue("Daily Pacing");
            campaignHeaderRow.createCell(8).setCellValue("Total Delivery");

            //style header
            font.setFontHeightInPoints((short) 12);
            bold.setFont(font);

            for(Cell cell : campaignHeaderRow) {
                cell.setCellStyle(bold);
            }

            rowCount++;

            //init current date, and duration of start to now
            DateTime now = new DateTime();
            Duration startToNow = new Duration(lineItem.getStartDateTime(), now);
            //init total counts
            Float totalLifetimeBudget = 0.0f;
            Float totalDailyBudget = 0.0f;
            Float totalActualDailyBudget = 0.0f;
            Float totalCumulativeDelivery = 0.0f;
            double[] totalDailyDelivery = new double[100];
            for(double num : totalDailyDelivery) {
                //noinspection UnusedAssignment
                num = 0;
            }
            int cellTrack = 0;

            //add a row of stats for every campaign
            for (Campaign campaign : lineItem.getCampaignList()) {
                Row campaignRow = lineItemSheet.createRow(rowCount);
                campaignRow.createCell(0).setCellValue(campaign.getCampaignID());
                campaignRow.createCell(1).setCellValue(campaign.getCampaignName());

                //if inactive, italic camp name
                if (campaign.getStatus().equals("inactive")) {
                    Font italics = WORKBOOK.createFont();
                    italics.setItalic(true);
                    CellStyle style = WORKBOOK.createCellStyle();
                    style.setFont(italics);
                    campaignRow.getCell(1).setCellStyle(style);
                } else {
                    //if active, bold camp name
                    Font bolding = WORKBOOK.createFont();
                    bolding.setBoldweight((short)500);
                    CellStyle bolds = WORKBOOK.createCellStyle();
                    bolds.setFont(bolding);
                    campaignRow.getCell(1).setCellStyle(bolds);
                }

                campaignRow.createCell(2).setCellValue(campaign.getLifetimeBudget());
                //only add date if they're not null
                if (campaign.getStartDate() != null && campaign.getEndDate() != null) {
                    campaignRow.createCell(3).setCellValue(campaign.getStartDate().getMonthOfYear() +
                            "/" + campaign.getStartDate().getDayOfMonth());
                    campaignRow.createCell(4).setCellValue(campaign.getEndDate().getMonthOfYear() +
                            "/" + campaign.getEndDate().getDayOfMonth());
                }
                campaignRow.createCell(5).setCellValue(campaign.getDaysActive());
                campaignRow.createCell(6).setCellValue(campaign.getActualDailyBudget());
                campaignRow.createCell(8).setCellValue(campaign.getTotalDelivery());


                int cellCount = 9;

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


                //list daily deliveries
                for (long x = startToNow.getStandardDays(); x > 0; x--) {
                    //add header cell with date
                    campaignHeaderRow.createCell(cellCount)
                            .setCellValue(lineItem.getStartDateTime().plusDays((int)x).monthOfYear().getAsString() +
                                    "/" + lineItem.getStartDateTime().plusDays((int)x).dayOfMonth().getAsString());
                    //step through every delivery
                    for(Delivery del : campaign.getDeliveries()) {
                        //if the dates match, add them
                        if(Integer.parseInt(lineItem.getStartDateTime()
                                .plusDays((int)x).dayOfMonth().getAsString()) == del.getDate().getDayOfMonth() &&
                                Integer.parseInt(lineItem.getStartDateTime()
                                        .plusDays((int)x).monthOfYear().getAsString()) ==
                                        del.getDate().getMonthOfYear()) {

                            campaignRow.createCell(cellCount).setCellValue(del.getDelivery());
                            //add to total count for the column
                            totalDailyDelivery[cellCount] += del.getDelivery();
                        }
                    }
                    cellCount++;
                }

                rowCount++;
                //track max cell count
                if(cellCount > cellTrack) cellTrack = cellCount;
                //autosize every column
                for (Cell cell : campaignRow) {
                    lineItemSheet.autoSizeColumn(cell.getColumnIndex());
                }
                //add to column totals
                totalLifetimeBudget += campaign.getLifetimeBudget();
                totalDailyBudget += campaign.getDailyBudget();
                totalActualDailyBudget += campaign.getActualDailyBudget();
                totalCumulativeDelivery += campaign.getTotalDelivery();
            }
            rowCount++;

            //display totals
            Row totalRow = lineItemSheet.createRow(rowCount);
            totalRow.createCell(1).setCellValue("Column Totals:");
            totalRow.createCell(2).setCellValue(totalLifetimeBudget);
            totalRow.createCell(6).setCellValue(totalDailyBudget);
            totalRow.createCell(7).setCellValue(totalActualDailyBudget);
            totalRow.createCell(8).setCellValue(totalCumulativeDelivery);

            for(int x = cellTrack - 1; x > 8; x--) {
                totalRow.createCell(x).setCellValue(totalDailyDelivery[x]);
            }

            //set total pacing
            lineItemRow.getCell(7).setCellValue(((lineItem.getLifetimeBudget()-totalCumulativeDelivery)
                    /lineItem.getDaysRemaining()));

            //set % lt budget used
            double perLTBudget = totalCumulativeDelivery/lineItem.getLifetimeBudget();
            if (perLTBudget > 1) perLTBudget = 1;
            lineItemRow.getCell(8).setCellValue(perLTBudget);

            rowCount+=3;

            //create header row to imp, click, conv section
            Row campHeaderRow = lineItemSheet.createRow(rowCount);
            campHeaderRow.createCell(0).setCellValue("Campaign ID");
            campHeaderRow.createCell(1).setCellValue("Campaign Name");
            campHeaderRow.createCell(2).setCellValue("LT Imps");
            campHeaderRow.createCell(3).setCellValue("LT Clicks");
            campHeaderRow.createCell(4).setCellValue("LT Conv.");
            campHeaderRow.createCell(5).setCellValue("LT CTR");
            campHeaderRow.createCell(8).setCellValue("Daily Stats:");

            //bold the header
            for(Cell cell : campHeaderRow) {
                cell.setCellStyle(bold);
            }

            rowCount++;

            //populate data for every campaign
            for(Campaign camp : lineItem.getCampaignList()) {
                Row campRow = lineItemSheet.createRow(rowCount);
                campRow.createCell(0).setCellValue(camp.getCampaignID());
                campRow.createCell(1).setCellValue(camp.getCampaignName());
                campRow.createCell(2).setCellValue(camp.getLifetimeImps());
                campRow.createCell(3).setCellValue(camp.getLifetimeClicks());
                campRow.createCell(4).setCellValue(camp.getLifetimeConvs());
                campRow.createCell(5).setCellValue(camp.getLifetimeCtr());
                campRow.createCell(8).setCellValue("Imps:\nClicks:\nConvs:\nCTR:");

                //enable newlines
                CellStyle cs = WORKBOOK.createCellStyle();
                cs.setWrapText(true);
                campRow.getCell(8).setCellStyle(cs);
                //increase row height to accomodate 4 lines of text
                campRow.setHeightInPoints((4*lineItemSheet.getDefaultRowHeightInPoints()));

                //if camp inactive, italic name
                if (camp.getStatus().equals("inactive")) {
                    Font italics = WORKBOOK.createFont();
                    italics.setItalic(true);
                    CellStyle style = WORKBOOK.createCellStyle();
                    style.setFont(italics);
                    campRow.getCell(1).setCellStyle(style);
                } else {
                    //if camp active, bold name
                    Font bolding = WORKBOOK.createFont();
                    bolding.setBoldweight((short)500);
                    CellStyle bolds = WORKBOOK.createCellStyle();
                    bolds.setFont(bolding);
                    campRow.getCell(1).setCellStyle(bolds);
                }

                int cellCount = 9;

                //Style columns for easier readability
                CellStyle altRow = WORKBOOK.createCellStyle();
                altRow.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
                altRow.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                altRow.setWrapText(true);

                //for every day between start date and now, create column
                for (long x = startToNow.getStandardDays(); x > 0; x--) {

                    //add date header
                    campHeaderRow.createCell(cellCount)
                            .setCellValue(lineItem.getStartDateTime().plusDays((int)x).monthOfYear().getAsString() +
                                    "/" + lineItem.getStartDateTime().plusDays((int)x).dayOfMonth().getAsString());

                    //if there is no data, need to add blank cells to keep data in order with dates
                    Boolean blankCells = true;
                    for(Delivery del : camp.getDeliveries()) {

                        //if the dates match
                        if(Integer.parseInt(lineItem.getStartDateTime().plusDays((int)x).dayOfMonth().getAsString())
                                == del.getDate().getDayOfMonth() &&
                                Integer.parseInt(lineItem.getStartDateTime()
                                        .plusDays((int)x).monthOfYear()
                                        .getAsString()) == del.getDate().getMonthOfYear()) {

                            //don't add blank cells
                            blankCells = false;

                            campRow.createCell(cellCount).setCellValue(del.getImps() + "\n" +
                                                                        del.getClicks() + "\n" +
                                                                        del.getConvs() + "\n" +
                                                                        del.getCtr());
                            campRow.getCell(cellCount).setCellStyle(cs);

                            cellCount++;

                            if(x % 2 == 1) {
                                campRow.getCell(cellCount-1).setCellStyle(altRow);
                            }
                        }
                    }
                    //add blank cells if there is no data
                    if(blankCells) {
                        cellCount++;
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
