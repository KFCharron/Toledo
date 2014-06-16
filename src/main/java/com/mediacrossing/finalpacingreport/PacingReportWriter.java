package com.mediacrossing.finalpacingreport;

import com.mediacrossing.dailypacingreport.PacingLineItem;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.*;
import play.libs.F;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class PacingReportWriter {

    public static Workbook writeReport(ArrayList<PacingAdvertiser> ads, Set<String> flightNames) {

        // Create wb
        Workbook wb = new HSSFWorkbook();

        // Styles
        DataFormat df = wb.createDataFormat();

        CellStyle black = wb.createCellStyle();
        black.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        black.setFillPattern(CellStyle.SOLID_FOREGROUND);

        CellStyle yellow = wb.createCellStyle();
        yellow.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        yellow.setFillPattern(CellStyle.SOLID_FOREGROUND);

        Font boldFont = wb.createFont();
        boldFont.setBoldweight((short) 1000);

        CellStyle date = wb.createCellStyle();
        date.setFont(boldFont);

        CellStyle number = wb.createCellStyle();
        number.setDataFormat(df.getFormat("#,###"));

        CellStyle percentage = wb.createCellStyle();
        percentage.setDataFormat(df.getFormat("##0%"));

        CellStyle adHeaderStyle = wb.createCellStyle();

        Font white = wb.createFont();
        white.setColor(IndexedColors.WHITE.getIndex());
        white.setFontHeightInPoints((short)14);
        adHeaderStyle.setFont(white);
        adHeaderStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        adHeaderStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

        CellStyle boldNumber = wb.createCellStyle();
        boldNumber.setDataFormat(df.getFormat("#,###"));
        boldNumber.setFont(boldFont);

        CellStyle boldPercentage = wb.createCellStyle();
        boldPercentage.setDataFormat(df.getFormat("##0%"));
        boldPercentage.setFont(boldFont);

        CellStyle boldString = wb.createCellStyle();
        boldString.setFont(boldFont);

        CellStyle redPercentage = wb.createCellStyle();
        redPercentage.setDataFormat(df.getFormat("##0%"));
        redPercentage.setFont(white);
        redPercentage.setFillForegroundColor(IndexedColors.RED.getIndex());
        redPercentage.setFillPattern(CellStyle.SOLID_FOREGROUND);

        CellStyle yellowString = wb.createCellStyle();
        yellowString.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        yellowString.setFillPattern(CellStyle.SOLID_FOREGROUND);

        CellStyle yellowPercentage = wb.createCellStyle();
        yellowPercentage.setDataFormat(df.getFormat("##0%"));
        yellowPercentage.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        yellowPercentage.setFillPattern(CellStyle.SOLID_FOREGROUND);

        CellStyle yellowNumber = wb.createCellStyle();
        yellowNumber.setDataFormat(df.getFormat("#,###"));
        yellowNumber.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        yellowNumber.setFillPattern(CellStyle.SOLID_FOREGROUND);


        Sheet summarySheet = wb.createSheet("Summary");
        Row header = summarySheet.createRow(0);
        header.createCell(0).setCellValue("Line Item");
        header.createCell(1).setCellValue("Start Date");
        header.createCell(2).setCellValue("End Date");
        header.createCell(3).setCellValue("% Time Through Flight");
        header.createCell(4).setCellValue("% Imps To LT");
        header.createCell(5).setCellValue("LT Goal");
        header.createCell(6).setCellValue("Imps Delivered To Date");
        header.createCell(7).setCellValue("Imps Needed Yest.");
        header.createCell(8).setCellValue("Delivered Yest.");
        header.createCell(9).setCellValue("% Needed Delivered Yest.");
        header.createCell(10).setCellValue("New Daily Imps Needed");
        header.createCell(11).setCellValue("Past 7 Day Imps Delivery Avg");
        header.createCell(12).setCellValue("% Change Needed / 7 Day Avg");

        int rowCount = 1;
        for (String flightName : flightNames) {

            int totalLifetimeGoal = 0;
            int totalDelivered = 0;
            int totalNeededYesterday = 0;
            int totalDeliveredYesterday = 0;
            int totalNewImpsNeeded = 0;
            int totalSevenDayAverage = 0;

            // Header per advertiser
            Row advertHeader = summarySheet.createRow(rowCount++);
            advertHeader.createCell(0).setCellValue(flightName);
            advertHeader.getCell(0).setCellStyle(adHeaderStyle);
            for (int x = 1; x < 13; x++) {
                advertHeader.createCell(x);
                advertHeader.getCell(x).setCellStyle(adHeaderStyle);
            }

            for (PacingAdvertiser a : ads) {
                for (PacingLineItem l : a.getLineList()) {

                    String lName = l.getName();
                    String[] parsed = lName.split("]");
                    String fName = parsed[1].substring(4, parsed[1].length());
                    String lineFlightName = a.getName() + " - " + fName;

                    if (lineFlightName.equals(flightName)) {
                        Row dataRow = summarySheet.createRow(rowCount++);
                        dataRow.createCell(0).setCellValue(l.getName());
                        dataRow.createCell(1).setCellValue(l.getStartDate().toString("MMM-dd"));
                        dataRow.createCell(2).setCellValue(l.getEndDate().toString("MMM-dd"));

                        // Through Flight
                        int daysPassed = (int)new Duration(l.getStartDate(), new DateTime().withTimeAtStartOfDay().plusDays(1)).getStandardDays();
                        int totalDays = (int)new Duration(l.getStartDate(), l.getEndDate().plusDays(1)).getStandardDays();
                        dataRow.createCell(3).setCellValue((float)daysPassed / totalDays);
                        dataRow.getCell(3).setCellStyle(percentage);

                        // Through Lifetime
                        int totalDelivery = 0;
                        int yesterdayDelivery = 0;
                        for (F.Tuple<DateTime, Integer> r : l.getDailyData()) {
                            totalDelivery = totalDelivery + r._2;
                            if (r._1.isEqual(new DateTime().withTimeAtStartOfDay().minusDays(1))) {
                                yesterdayDelivery = r._2;
                            }
                        }

                        // Percent Imps to lifetime
                        dataRow.createCell(4).setCellValue((float)totalDelivery/l.getLifetimeBudget());
                        dataRow.getCell(4).setCellStyle(percentage);

                        // Lifetime goal
                        dataRow.createCell(5).setCellValue(l.getLifetimeBudget());
                        dataRow.getCell(5).setCellStyle(number);

                        // Imps delivered to date
                        dataRow.createCell(6).setCellValue(totalDelivery);
                        dataRow.getCell(6).setCellStyle(number);

                        // Yesterday Expectation
                        int pastDelivery = (totalDelivery - yesterdayDelivery);

                        // Imps left before yesterday
                        int impsLeft = l.getLifetimeBudget() - pastDelivery;
                        int daysLeft = totalDays - daysPassed + 1;
                        int dailyBudget = impsLeft / daysLeft;

                        // Imps needed yesterday
                        dataRow.createCell(7).setCellValue(dailyBudget);
                        dataRow.getCell(7).setCellStyle(number);

                        //Imps delivered yesteday
                        dataRow.createCell(8).setCellValue(yesterdayDelivery);
                        dataRow.getCell(8).setCellStyle(number);

                        // Percentage needed delivered yesterday
                        float percentDeliveredYesterday;
                        if (dailyBudget == 0) {
                            percentDeliveredYesterday = 0;
                        } else percentDeliveredYesterday = (float)yesterdayDelivery/dailyBudget;
                        dataRow.createCell(9).setCellValue(percentDeliveredYesterday);
                        dataRow.getCell(9).setCellStyle(percentage);
                        if (dataRow.getCell(9).getNumericCellValue() < .9f) dataRow.getCell(9).setCellStyle(redPercentage);

                        int deliveryRemaining = (l.getLifetimeBudget() - totalDelivery) / (daysLeft - 1);

                        // New Daily Imps Needed
                        dataRow.createCell(10).setCellValue(deliveryRemaining);
                        dataRow.getCell(10).setCellStyle(number);

                        // Bold entire row if about to end
                        if (new Duration(new DateTime().withTimeAtStartOfDay(), l.getEndDate()).getStandardDays() < 4) {
                            for (Cell c : dataRow) {
                                if (c.getColumnIndex() == 3 || c.getColumnIndex() == 4 || c.getColumnIndex() == 9 || c.getColumnIndex() == 12) {
                                    c.setCellStyle(boldPercentage);
                                } else
                                    c.setCellStyle(boldNumber);
                            }
                        }

                        DateTime cutoffDate = new DateTime().withTimeAtStartOfDay().minusDays(7);
                        int sevenDayTotal = 0;
                        int dayCount = 0;
                        for (F.Tuple<DateTime, Integer> d : l.getDailyData()) {
                            if (d._1.isAfter(cutoffDate)) {
                                sevenDayTotal = sevenDayTotal + d._2;
                                dayCount++;
                            }
                        }
                        int sevenDayAverage;
                        if (dayCount == 0) sevenDayAverage = 0;
                        else sevenDayAverage = sevenDayTotal / dayCount;

                        dataRow.createCell(11).setCellValue(sevenDayAverage);
                        dataRow.getCell(11).setCellStyle(number);

                        float yestOverSevenAverage;
                        if (sevenDayAverage == 0) yestOverSevenAverage = 0;
                        else yestOverSevenAverage = (float)yesterdayDelivery / sevenDayAverage;
                        dataRow.createCell(12).setCellValue(yestOverSevenAverage);
                        dataRow.getCell(12).setCellStyle(percentage);

                        // Yellow entire row if no delivery
                        if (dataRow.getCell(8).getNumericCellValue() < 1) {
                            for (Cell c : dataRow) {
                                if (c.getColumnIndex() == 3 || c.getColumnIndex() == 4 || c.getColumnIndex() == 9 || c.getColumnIndex() == 12)
                                    c.setCellStyle(yellowPercentage);
                                else
                                    c.setCellStyle(yellowNumber);
                            }
                        }

                        rowCount++;

                        // Add To Running Totals
                        totalLifetimeGoal = totalLifetimeGoal + l.getLifetimeBudget();
                        totalDelivered = totalDelivered + totalDelivery;
                        totalNeededYesterday = totalNeededYesterday + dailyBudget;
                        totalDeliveredYesterday = totalDeliveredYesterday + yesterdayDelivery;
                        totalNewImpsNeeded = totalNewImpsNeeded + deliveryRemaining;
                        totalSevenDayAverage = totalSevenDayAverage + sevenDayAverage;
                    }
                }
            }

            float totalPercentToLifetime = (float)totalDelivered / totalLifetimeGoal;
            float totalPercentDeliveredYesterday = (float)totalDeliveredYesterday / totalNeededYesterday;
            float totalPercentNeededVsSevenDay = (float)totalNeededYesterday / totalSevenDayAverage;


            Row totalRow = summarySheet.createRow(rowCount++);
            totalRow.createCell(0).setCellValue("Flight Totals: ");
            totalRow.createCell(1);
            totalRow.createCell(2);
            totalRow.createCell(3);
            totalRow.createCell(4).setCellValue(totalPercentToLifetime);
            totalRow.createCell(5).setCellValue(totalLifetimeGoal);
            totalRow.createCell(6).setCellValue(totalDelivered);
            totalRow.createCell(7).setCellValue(totalNeededYesterday);
            totalRow.createCell(8).setCellValue(totalDeliveredYesterday);
            totalRow.createCell(9).setCellValue(totalPercentDeliveredYesterday);
            totalRow.createCell(10).setCellValue(totalNewImpsNeeded);
            totalRow.createCell(11).setCellValue(totalSevenDayAverage);
            totalRow.createCell(12).setCellValue(totalPercentNeededVsSevenDay);

            for (Cell c : totalRow) totalRow.getCell(c.getColumnIndex()).setCellStyle(number);
            totalRow.getCell(4).setCellStyle(percentage);
            totalRow.getCell(9).setCellStyle(percentage);
            totalRow.getCell(12).setCellStyle(percentage);

        }
        for (Cell c : header) summarySheet.autoSizeColumn(c.getColumnIndex());

        return wb;
    }
}

