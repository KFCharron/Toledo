package com.mediacrossing.finalpacingreport;

import com.mediacrossing.dailypacingreport.PacingLineItem;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.*;
import play.libs.F;

import java.util.ArrayList;

public class PacingReportWriter {

    public static Workbook writeReport(ArrayList<PacingAdvertiser> ads) {

        // Create wb
        Workbook wb = new HSSFWorkbook();

        // Styles
        DataFormat df = wb.createDataFormat();
        // FIXME
        //Row yellowDayHeader = sheet.createRow(rowCount);
        CellStyle black = wb.createCellStyle();
        black.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        black.setFillPattern(CellStyle.SOLID_FOREGROUND);
        CellStyle yellow = wb.createCellStyle();
        yellow.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        yellow.setFillPattern(CellStyle.SOLID_FOREGROUND);
        Font boldFont = wb.createFont();
        boldFont.setBoldweight((short) 1000);
        yellow.setFont(boldFont);
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
        adHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
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
        header.createCell(12).setCellValue("% Needed / 7 Day Avg");

        int rowCount = 1;
        for (PacingAdvertiser a : ads) {

            // Header per advertiser
            Row advertHeader = summarySheet.createRow(rowCount++);
            advertHeader.createCell(0).setCellValue(a.getName() + " (" + a.getId() + ")");
            advertHeader.getCell(0).setCellStyle(adHeaderStyle);
            for (int x = 1; x < 11; x++) {
                advertHeader.createCell(x);
                advertHeader.getCell(x).setCellStyle(adHeaderStyle);
            }

            for (PacingLineItem l : a.getLineList()) {
                Row dataRow = summarySheet.createRow(rowCount++);
                dataRow.createCell(0).setCellValue(l.getName());
                dataRow.createCell(1).setCellValue(l.getStartDate().toString("MMM-dd"));
                dataRow.createCell(2).setCellValue(l.getEndDate().toString("MMM-dd"));

                // Through Flight
                int daysPassed = (int)new Duration(l.getStartDate(), new DateTime().withTimeAtStartOfDay()).getStandardDays();
                int totalDays = (int)new Duration(l.getStartDate(), l.getEndDate()).getStandardDays();
                dataRow.createCell(3).setCellValue((float)daysPassed / totalDays);
                dataRow.getCell(3).setCellStyle(percentage);

                dataRow.createCell(4).setCellValue(l.getLifetimeBudget());
                dataRow.getCell(4).setCellStyle(number);

                // Through Lifetime
                int totalDelivery = 0;
                int yesterdayDelivery = 0;
                for (F.Tuple<DateTime, Integer> r : l.getDailyData()) {
                    System.out.println(r._2.intValue());
                    totalDelivery = totalDelivery
                            + r._2.intValue();
                    if (r._1.isEqual(new DateTime().withTimeAtStartOfDay().minusDays(1))) {
                        yesterdayDelivery = r._2.intValue();
                    }
                }

                dataRow.createCell(5).setCellValue(totalDelivery);
                dataRow.getCell(5).setCellStyle(number);
                dataRow.createCell(6).setCellValue((float)totalDelivery/l.getLifetimeBudget());
                dataRow.getCell(6).setCellStyle(percentage);

                // Yesterday Expectation
                int pastDelivery = (totalDelivery - yesterdayDelivery);
                int impsLeft = l.getLifetimeBudget() - pastDelivery;
                int daysLeft = totalDays - daysPassed + 1;
                int dailyBudget = impsLeft / daysLeft;

                dataRow.createCell(7).setCellValue(dailyBudget);
                dataRow.getCell(7).setCellStyle(number);
                dataRow.createCell(8).setCellValue(yesterdayDelivery);
                dataRow.getCell(8).setCellStyle(number);
                float percentDeliveredYesterday;
                if (dailyBudget == 0) {
                    percentDeliveredYesterday = 0;
                } else percentDeliveredYesterday = (float)yesterdayDelivery/dailyBudget;
                dataRow.createCell(9).setCellValue(percentDeliveredYesterday);
                dataRow.getCell(9).setCellStyle(percentage);
                if (dataRow.getCell(9).getNumericCellValue() < .9f) dataRow.getCell(9).setCellStyle(redPercentage);

                int deliveryRemaining = (l.getLifetimeBudget() - pastDelivery + yesterdayDelivery) / daysLeft;
                dataRow.createCell(10).setCellValue(deliveryRemaining);
                dataRow.getCell(10).setCellStyle(number);

                // Bold entire row if about to end
                if (new Duration(new DateTime().withTimeAtStartOfDay(), l.getEndDate()).getStandardDays() < 4) {
                    for (Cell c : dataRow) {
                        if (c.getColumnIndex() == 3 || c.getColumnIndex() == 6 || c.getColumnIndex() == 9) {
                            c.setCellStyle(boldPercentage);
                        } else if (c.getColumnIndex() == 4 || c.getColumnIndex() == 5 || c.getColumnIndex() == 7 ||
                                c.getColumnIndex() == 8 || c.getColumnIndex() ==  10) {
                            c.setCellStyle(boldNumber);
                        } else c.setCellStyle(boldString);
                    }
                }

                // Yellow entire row if no delivery
                if (dataRow.getCell(8).getNumericCellValue() < 1) {
                    for (Cell c : dataRow) {
                        if (c.getColumnIndex() == 3 || c.getColumnIndex() == 6 || c.getColumnIndex() == 9) {
                            c.setCellStyle(yellowPercentage);
                        } else if (c.getColumnIndex() == 4 || c.getColumnIndex() == 5 || c.getColumnIndex() == 7 ||
                                c.getColumnIndex() == 8 || c.getColumnIndex() ==  10) {
                            c.setCellStyle(yellowNumber);
                        } else c.setCellStyle(yellow);
                    }
                }

                rowCount++;
            }
        }
        for (Cell c : header) summarySheet.autoSizeColumn(c.getColumnIndex());


//        for (PacingAdvertiser a : ads) {
//
//            Sheet sheet = wb.createSheet(a.getName());
//
//            //int rowCount = 0;
//
//
//              // FIXME
////            // By week header
////            yellowDayHeader.createCell(0).setCellValue("BY WEEK");
////            yellowDayHeader.getCell(0).setCellStyle(yellow);
////            for (int x = 1; x < 8; x++) {
////                yellowDayHeader.createCell(x).setCellStyle(yellow);
////            }
//            rowCount++;
//
//            // Row Titles
//            Row dateRow = sheet.createRow(rowCount++);
//            Row dailyImpGoalRow = sheet.createRow(rowCount++);
//            dailyImpGoalRow.createCell(0).setCellValue("IMPS GOAL By Week");
//            Row impGoalToDateRow = sheet.createRow(rowCount++);
//            impGoalToDateRow.createCell(0).setCellValue("IMPS GOAL to Date");
//            Row goalPercentageDeliveredRow = sheet.createRow(rowCount++);
//            goalPercentageDeliveredRow.createCell(0).setCellValue("GOAL Percentage Delivered");
//            rowCount++;
//            Row dailyImpActualRow = sheet.createRow(rowCount++);
//            dailyImpActualRow.createCell(0).setCellValue("IMPS ACTUAL By Week");
//            Row actualImpsToDateRow = sheet.createRow(rowCount++);
//            actualImpsToDateRow.createCell(0).setCellValue("IMPS ACTUAL To Date");
//            Row actualPercentageDeliveredRow = sheet.createRow(rowCount++);
//            actualPercentageDeliveredRow.createCell(0).setCellValue("ACTUAL Percentage Delivered");
//            Row blackRow = sheet.createRow(rowCount++);
//            Row neededImpGoal = null;
//
//            // List of Weekly Totals
//            ArrayList<DailyPacingData> weeklyTotals = new ArrayList<>();
//            DailyPacingData weeklyTotal = new DailyPacingData(a.getDailyPacingNumbers().get(0).getDate());
//            int totalCount = 0;
//            for (DailyPacingData d : a.getDailyPacingNumbers()) {
//                // Add to the weeklyTotal
//                weeklyTotal.setGoalToDate(weeklyTotal.getGoalToDate() + d.getGoalToDate());
//                weeklyTotal.setGoalToday(weeklyTotal.getGoalToday() + d.getGoalToday());
//                weeklyTotal.setImpsDelivered(weeklyTotal.getImpsDelivered() + d.getImpsDelivered());
//                weeklyTotal.setImpsToDate(weeklyTotal.getImpsToDate() + d.getImpsToDate());
//                // Increment count
//                totalCount++;
//
//                // if x % 7 == 0 add old, new weeklytotal
//                if (totalCount % 6 == 0) {
//                    weeklyTotals.add(weeklyTotal);
//                    weeklyTotal = new DailyPacingData(a.getDailyPacingNumbers().get(totalCount).getDate());
//                    totalCount--;
//                }
//            }
//
//            // For each weekly total, put into columns
//            int colCount = 1;
//            for (DailyPacingData w : weeklyTotals) {
//                dateRow.createCell(colCount).setCellValue(w.getDate().toString("MMM-dd"));
//                dailyImpGoalRow.createCell(colCount).setCellValue(w.getGoalToday());
//                impGoalToDateRow.createCell(colCount).setCellValue(w.getGoalToDate());
//                goalPercentageDeliveredRow.createCell(colCount).setCellValue(0); // TODO set this
//                dailyImpActualRow.createCell(colCount).setCellValue(w.getImpsDelivered());
//                actualImpsToDateRow.createCell(colCount).setCellValue(w.getImpsToDate());
//                actualPercentageDeliveredRow.createCell(colCount).setCellValue(0); // TODO set this
//                colCount ++;
//            }
//
//
//
//
//
//        }
        return wb;
    }
}

