package com.mediacrossing.campaignbooks;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.joda.time.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;


public class ExcelWriter {

    private static Workbook WORKBOOK = new HSSFWorkbook();

    //private static final Logger LOG = LoggerFactory.getLogger(ExcelWriter.class);

    public static void writeAdvertiserSheetToWorkbook(Advertiser ad) {

        //Create new sheet
        String sheetName = WorkbookUtil.createSafeSheetName(ad.getAdvertiserName() +
                " (" +  ad.getAdvertiserID() + ")");
        Sheet lineItemSheet = WORKBOOK.createSheet(sheetName);

        DataFormat df = WORKBOOK.createDataFormat();

        CellStyle fullCurrency = WORKBOOK.createCellStyle();
        fullCurrency.setDataFormat(df.getFormat("$#,##0.00"));

        CellStyle halfCurrency = WORKBOOK.createCellStyle();
        halfCurrency.setDataFormat(df.getFormat("$#,##0"));

        CellStyle percentage = WORKBOOK.createCellStyle();
        percentage.setDataFormat(df.getFormat("0%"));

        CellStyle ctrPercentage = WORKBOOK.createCellStyle();
        ctrPercentage.setDataFormat(df.getFormat("0.0000%"));


        int rowCount = 0;
        //setup totals for entire advertiser
        float ltBudgetGrandTotal = 0;
        float dailyBudgetGrandTotal = 0;
        float cumulativeDeliveryGrandTotal = 0;
        int maxDays = 0;
        DateTime today = new DateTime();
        for (LineItem l : ad.getLineItemList()) {
            Duration startToNow = new Duration(l.getStartDateTime(), today);
            if (startToNow.getStandardDays()-1 > maxDays) maxDays = (int)startToNow.getStandardDays()-1;
        }
        ArrayList<Float> grandTots =
                new ArrayList<Float>(Collections.nCopies(maxDays+10, 0f));

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
            lineItemHeader.createCell(10).setCellValue("Days Rem.");
            lineItemHeader.createCell(9).setCellValue("Flight");
            lineItemHeader.createCell(11).setCellValue("Pacing");

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

            //set styles
            lineItemRow.getCell(2).setCellStyle(halfCurrency);
            lineItemRow.getCell(6).setCellStyle(fullCurrency);
            lineItemRow.getCell(7).setCellStyle(fullCurrency);
            lineItemRow.getCell(8).setCellStyle(fullCurrency);
            lineItemRow.getCell(9).setCellStyle(percentage);

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
            ArrayList<Float> tots =
                    new ArrayList<Float>(Collections.nCopies((int)(startToNow.getStandardDays()+9), 0f));

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
                    italics.setColor(IndexedColors.GREY_25_PERCENT.getIndex());
                    CellStyle style = WORKBOOK.createCellStyle();
                    style.setFont(italics);
                    campaignRow.getCell(1).setCellStyle(style);
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
                campaignRow.createCell(6).setCellValue(campaign.getDailyBudget());
                campaignRow.createCell(7).setCellValue(campaign.getActualDailyBudget());
                campaignRow.createCell(8).setCellValue(campaign.getTotalDelivery());

                //set styles
                campaignRow.getCell(2).setCellStyle(halfCurrency);
                campaignRow.getCell(6).setCellStyle(fullCurrency);
                campaignRow.getCell(7).setCellStyle(fullCurrency);
                campaignRow.getCell(8).setCellStyle(fullCurrency);

                //add yellow if total delivery within 2 daily budgets of lifetime budget
                CellStyle yellowStyle = WORKBOOK.createCellStyle();
                yellowStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.index);
                yellowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                yellowStyle.setDataFormat(df.getFormat("$#,##0.00"));
                if (campaign.getTotalDelivery() >= campaign.getLifetimeBudget() - (campaign.getDailyBudget()*2)) {
                    campaignRow.getCell(8).setCellStyle(yellowStyle);
                }
                //add red if total delivery equals or exceeds lifetime budget
                CellStyle redStyle = WORKBOOK.createCellStyle();
                redStyle.setFillForegroundColor(IndexedColors.RED.index);
                redStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                redStyle.setDataFormat(df.getFormat("$#,##0.00"));
                if (campaign.getTotalDelivery() >= campaign.getLifetimeBudget()) {
                    campaignRow.getCell(8).setCellStyle(redStyle);
                }

                int cellCount = 9;

                //list daily deliveries
                for (long x = startToNow.getStandardDays()-1; x >= 0; x--) {
                    //add header cell with date
                    campaignHeaderRow.createCell(cellCount)
                            .setCellValue(lineItem.getStartDateTime().plusDays((int)x).monthOfYear().getAsString() +
                                    "/" + lineItem.getStartDateTime().plusDays((int)x).dayOfMonth().getAsString());
                    //step through every delivery
                    for(Delivery del : campaign.getDeliveries()) {
                        //if the dates match, add them
                        if(lineItem.getStartDateTime()
                                .plusDays((int)x).dayOfMonth().get() == del.getDate().getDayOfMonth() &&
                                lineItem.getStartDateTime()
                                        .plusDays((int)x).monthOfYear().get() ==
                                        del.getDate().getMonthOfYear()) {

                            campaignRow.createCell(cellCount).setCellValue(del.getDelivery());
                            //set style
                            campaignRow.getCell(cellCount).setCellStyle(fullCurrency);
                            //add to total count for the column
                            tots.set(cellCount, del.getDelivery()+ tots.get(cellCount));
                            grandTots.set(cellCount, del.getDelivery()+ grandTots.get(cellCount));
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

            //add to grand totals
            ltBudgetGrandTotal += totalLifetimeBudget;
            dailyBudgetGrandTotal += totalDailyBudget;
            cumulativeDeliveryGrandTotal += totalCumulativeDelivery;

            //set cell styles
            totalRow.getCell(2).setCellStyle(fullCurrency);
            totalRow.getCell(6).setCellStyle(fullCurrency);
            totalRow.getCell(7).setCellStyle(fullCurrency);
            totalRow.getCell(8).setCellStyle(fullCurrency);

            for(int x = cellTrack - 1; x > 8; x--) {
                totalRow.createCell(x).setCellValue(tots.get(x));
                //set style
                totalRow.getCell(x).setCellStyle(fullCurrency);
            }

            //set total pacing
            lineItemRow.getCell(7).setCellValue(((lineItem.getLifetimeBudget()-totalCumulativeDelivery)
                    /lineItem.getDaysRemaining()));
            //set style
            lineItemRow.getCell(7).setCellStyle(fullCurrency);

            //set % lt budget used
            double perLTBudget = totalCumulativeDelivery/lineItem.getLifetimeBudget();
            if (perLTBudget > 1) perLTBudget = 1;
            lineItemRow.getCell(8).setCellValue(perLTBudget);
            lineItemRow.getCell(8).setCellStyle(percentage);

            long daysPassed = lineItem.getDaysActive()-lineItem.getDaysRemaining();
            float pacing = totalCumulativeDelivery / (lineItem.getDailyBudget() * daysPassed);
            lineItemRow.createCell(11).setCellValue(pacing);
            lineItemRow.getCell(11).setCellStyle(percentage);

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

            CellStyle impStyle = WORKBOOK.createCellStyle();
            impStyle.setBorderBottom(CellStyle.BORDER_NONE);
            impStyle.setBorderTop(CellStyle.BORDER_THICK);

            CellStyle greenImpStyle = WORKBOOK.createCellStyle();
            greenImpStyle.setBorderBottom(CellStyle.BORDER_NONE);
            greenImpStyle.setBorderTop(CellStyle.BORDER_THICK);
            greenImpStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.index);
            greenImpStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

            CellStyle clickConvStyle = WORKBOOK.createCellStyle();
            clickConvStyle.setBorderTop(CellStyle.BORDER_NONE);
            clickConvStyle.setBorderBottom(CellStyle.BORDER_NONE);

            CellStyle greenClickConvStyle = WORKBOOK.createCellStyle();
            greenClickConvStyle.setBorderTop(CellStyle.BORDER_NONE);
            greenClickConvStyle.setBorderBottom(CellStyle.BORDER_NONE);
            greenClickConvStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.index);
            greenClickConvStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

            CellStyle ctrStyle = WORKBOOK.createCellStyle();
            ctrStyle.setBorderTop(CellStyle.BORDER_NONE);
            ctrStyle.setBorderBottom(CellStyle.BORDER_THICK);
            DataFormat decFor = WORKBOOK.createDataFormat();
            ctrStyle.setDataFormat(decFor.getFormat("#.0###%"));

            CellStyle greenCtrStyle = WORKBOOK.createCellStyle();
            greenCtrStyle.setBorderTop(CellStyle.BORDER_NONE);
            greenCtrStyle.setBorderBottom(CellStyle.BORDER_THICK);
            greenCtrStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.index);
            greenCtrStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            greenCtrStyle.setDataFormat(decFor.getFormat("#.0###%"));

            CellStyle bottomBorder = WORKBOOK.createCellStyle();
            bottomBorder.setBorderTop(CellStyle.BORDER_NONE);
            bottomBorder.setBorderBottom(CellStyle.BORDER_THICK);

            CellStyle bottomBorderCtr = WORKBOOK.createCellStyle();
            bottomBorderCtr.setBorderTop(CellStyle.BORDER_NONE);
            bottomBorderCtr.setBorderBottom(CellStyle.BORDER_THICK);
            bottomBorderCtr.setDataFormat(df.getFormat("0.0000%"));


            CellStyle topBorder = WORKBOOK.createCellStyle();
            topBorder.setBorderTop(CellStyle.BORDER_THICK);


            //populate data for every campaign
            for(Campaign camp : lineItem.getCampaignList()) {
                Row impRow = lineItemSheet.createRow(rowCount);
                impRow.createCell(8).setCellValue("Imps:");
                rowCount++;
                Row clickRow = lineItemSheet.createRow(rowCount);
                clickRow.createCell(8).setCellValue("Clicks:");
                rowCount++;
                Row convRow = lineItemSheet.createRow(rowCount);
                convRow.createCell(8).setCellValue("Convs:");
                rowCount++;

                Row campRow = lineItemSheet.createRow(rowCount);
                campRow.createCell(0).setCellValue(camp.getCampaignID());
                campRow.createCell(1).setCellValue(camp.getCampaignName());
                campRow.createCell(2).setCellValue(camp.getLifetimeImps());
                campRow.createCell(3).setCellValue(camp.getLifetimeClicks());
                campRow.createCell(4).setCellValue(camp.getLifetimeConvs());
                campRow.createCell(5).setCellValue(camp.getLifetimeCtr());
                campRow.createCell(6);
                campRow.createCell(7);
                campRow.createCell(8).setCellValue("CTR:");

                //if camp inactive, italic name
                int cellCount = 9;
                for (int x = 0; x < 8; x++)
                    impRow.createCell(x);
                for (Cell cell : campRow) {
                    cell.setCellStyle(bottomBorder);
                }
                for (Cell cell : impRow) {
                    cell.setCellStyle(topBorder);
                }

                if (camp.getStatus().equals("inactive")) {
                    Font italics = WORKBOOK.createFont();
                    italics.setItalic(true);
                    italics.setColor(IndexedColors.GREY_25_PERCENT.getIndex());
                    CellStyle style = WORKBOOK.createCellStyle();
                    style.setBorderTop(CellStyle.BORDER_NONE);
                    style.setBorderBottom(CellStyle.BORDER_THICK);
                    style.setFont(italics);
                    campRow.getCell(1).setCellStyle(style);
                }
                campRow.getCell(5).setCellStyle(bottomBorderCtr);

                //for every day between start date and now, create column
                for (long x = startToNow.getStandardDays()-1; x >= 0; x--) {

                    //add date header
                    campHeaderRow.createCell(cellCount)
                            .setCellValue(lineItem.getStartDateTime().plusDays((int)x).monthOfYear().getAsString() +
                                    "/" + lineItem.getStartDateTime().plusDays((int)x).dayOfMonth().getAsString());

                    //if there is no data, need to add blank cells to keep data in order with dates
                    Boolean blankCells = true;
                    for(Delivery del : camp.getDeliveries()) {

                        //if the dates match
                        if(lineItem.getStartDateTime().plusDays((int)x).dayOfMonth().get()
                                == del.getDate().getDayOfMonth() &&
                                lineItem.getStartDateTime()
                                        .plusDays((int)x).monthOfYear()
                                        .get() == del.getDate().getMonthOfYear()) {

                            //don't add blank cells
                            blankCells = false;

                            impRow.createCell(cellCount).setCellValue(del.getImps());
                            clickRow.createCell(cellCount).setCellValue(del.getClicks());
                            convRow.createCell(cellCount).setCellValue(del.getConvs());
                            campRow.createCell(cellCount).setCellValue(del.getCtr());

                            //set cell styles
                            impRow.getCell(cellCount).setCellStyle(impStyle);
                            clickRow.getCell(cellCount).setCellStyle(clickConvStyle);
                            convRow.getCell(cellCount).setCellStyle(clickConvStyle);
                            campRow.getCell(cellCount).setCellStyle(ctrStyle);

                            if(x % 2 == 1) {
                                impRow.getCell(cellCount).setCellStyle(greenImpStyle);
                                clickRow.getCell(cellCount).setCellStyle(greenClickConvStyle);
                                convRow.getCell(cellCount).setCellStyle(greenClickConvStyle);
                                campRow.getCell(cellCount).setCellStyle(greenCtrStyle);
                            }
                            cellCount++;
                        }
                    }
                    //add blank cells if there is no data
                    if(blankCells) {
                        impRow.createCell(cellCount);
                        clickRow.createCell(cellCount);
                        convRow.createCell(cellCount);
                        campRow.createCell(cellCount);

                        //set cell styles
                        impRow.getCell(cellCount).setCellStyle(impStyle);
                        clickRow.getCell(cellCount).setCellStyle(clickConvStyle);
                        convRow.getCell(cellCount).setCellStyle(clickConvStyle);
                        campRow.getCell(cellCount).setCellStyle(ctrStyle);

                        if(x % 2 == 1) {
                            impRow.getCell(cellCount).setCellStyle(greenImpStyle);
                            clickRow.getCell(cellCount).setCellStyle(greenClickConvStyle);
                            convRow.getCell(cellCount).setCellStyle(greenClickConvStyle);
                            campRow.getCell(cellCount).setCellStyle(greenCtrStyle);
                        }

                        cellCount++;
                    }

                }
                rowCount++;
            }
            rowCount+=3;

        }
        //Display totals
        Row grandTotal = lineItemSheet.createRow(rowCount);
        grandTotal.createCell(1).setCellValue("Grand");
        grandTotal.createCell(2).setCellValue(dailyBudgetGrandTotal);
        grandTotal.createCell(3).setCellValue(ltBudgetGrandTotal);
        grandTotal.createCell(4).setCellValue(cumulativeDeliveryGrandTotal);

        //set styles
        grandTotal.getCell(2).setCellStyle(fullCurrency);
        grandTotal.getCell(3).setCellStyle(fullCurrency);
        grandTotal.getCell(4).setCellStyle(fullCurrency);

        //Display daily grand totals
        int cellCount = 9;
        while(cellCount < grandTots.size()) {
            grandTotal.createCell(cellCount).setCellValue(grandTots.get(cellCount));
            grandTotal.getCell(cellCount).setCellStyle(fullCurrency);
            if(grandTots.get(cellCount) == 0) grandTotal.removeCell(grandTotal.getCell(cellCount));
            cellCount++;
        }

    }

    public static void writeWorkbookToFileWithOutputPath(String outputPath) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(new File(outputPath, "AutomatedCampaignBooks.xls"));
        WORKBOOK.write(fileOut);
        fileOut.close();
    }
}
