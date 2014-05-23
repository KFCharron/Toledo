//package com.mediacrossing.dailypacingreport;
//
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.ss.usermodel.*;
//import org.joda.time.*;
//import play.libs.F;
//
//import java.util.ArrayList;
//
//public class PacingReportWriter {
//
//    public static Workbook writeReport(ArrayList<PacingAdvertiser> ads) {
//
//        // Create wb
//        Workbook wb = new HSSFWorkbook();
//
//        for (PacingAdvertiser a : ads) {
//
//            Sheet sheet = wb.createSheet(a.getName());
//
//            int rowCount = 0;
//
//            DataFormat df = wb.createDataFormat();
//            Row yellowDayHeader = sheet.createRow(rowCount);
//            CellStyle black = wb.createCellStyle();
//            black.setFillForegroundColor(IndexedColors.BLACK.getIndex());
//            black.setFillPattern(CellStyle.SOLID_FOREGROUND);
//            CellStyle yellow = wb.createCellStyle();
//            yellow.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
//            yellow.setFillPattern(CellStyle.SOLID_FOREGROUND);
//            Font bold = wb.createFont();
//            bold.setBoldweight((short)1000);
//            yellow.setFont(bold);
//            CellStyle date = wb.createCellStyle();
//            date.setFont(bold);
//            CellStyle number = wb.createCellStyle();
//            number.setDataFormat(df.getFormat("#,###"));
//            CellStyle percentage = wb.createCellStyle();
//            percentage.setDataFormat(df.getFormat("##0%"));
//
//            yellowDayHeader.createCell(0).setCellValue("BY WEEK");
//            yellowDayHeader.getCell(0).setCellStyle(yellow);
//            for (int x = 1; x < 8; x++) {
//                yellowDayHeader.createCell(x).setCellStyle(yellow);
//            }
//            rowCount++;
//
//
//            DateTime earliest = a.getEarliest();
//            DateTime latest = a.getLatest();
//            int lifetimeDeliveryGoal = a.getLifetimeBudget();
//            Duration dur = new Duration(earliest, latest);
//
//            // Create List of weekly line items that are totals for each week
//            ArrayList<WeeklyTotals> weeklyNos = new ArrayList<>();
//            // Check if before earliest + 7 days until latest
//            DateTime current = earliest;
//            int goalToDate = 0;
//            int impToDate = 0;
//            while(current.isBefore(latest)) {
//                int impCount = 0;
//                int budgetCount = 0;
//                for (PacingLineItem l : a.getLineItems()) {
//                    for (F.Tuple i : l.getDailyData()) {
//                        if(i.getDate().isBefore(current.plusDays(7)) && i.getDate().isAfter(current)) {
//                            impCount = impCount + i.getImpressions();
//                            budgetCount = budgetCount + a.getNormalDailyBudget();
//                        }
//                    }
//                }
//                goalToDate = goalToDate + budgetCount;
//                impToDate = impToDate + impCount;
//                weeklyNos.add(new WeeklyTotals(current, budgetCount, goalToDate, impCount, impToDate));
//                current = current.plusDays(7);
//            }
//
//
//            if (lifetimeDeliveryGoal == 0) lifetimeDeliveryGoal = 1;
//
//            long daysLive = dur.getStandardDays();
//            if (daysLive == 0) daysLive = 1;
//            int normalBudget = (lifetimeDeliveryGoal / (int)daysLive);
//            int dailyBudget = (int)(((.25 * normalBudget)/(daysLive-1)) + normalBudget);
//            int lastDayBudget = (int)(.75 * normalBudget);
//
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
//            yellowDayHeader = sheet.createRow(rowCount);
//            for (int x = 0; x < 8; x++) yellowDayHeader.createCell(x).setCellStyle(yellow);
//            yellowDayHeader.getCell(0).setCellValue("BY DAY");
//
//            int cellCount = 1;
//            for (WeeklyTotals w : weeklyNos) {
//                dateRow.createCell(cellCount).setCellValue(w.getDate().toString("dd-MMM"));
//                dailyImpGoalRow.createCell(cellCount).setCellValue(w.getImpGoalByWeek());
//                impGoalToDateRow.createCell(cellCount).setCellValue(w.getImpGoalToDate());
//                goalPercentageDeliveredRow.createCell(cellCount).setCellValue((float)w.getImpGoalByWeek());
//                goalPercentageDeliveredRow.getCell(cellCount).setCellStyle(percentage);
//                dailyImpActualRow.createCell(cellCount).setCellValue(w.getImpActualByWeek());
//                actualImpsToDateRow.createCell(cellCount).setCellValue(w.getImpActualToDate());
//                actualPercentageDeliveredRow.createCell(cellCount).setCellValue((float)w.getImpActualByWeek()/w.getImpActualToDate());
//                actualPercentageDeliveredRow.getCell(cellCount).setCellStyle(percentage);
//                cellCount++;
//            }
//
//            cellCount = 1;
//            int impActualDate = 0;
//            rowCount = 11;
//
//            for(int x = 0; x <= dur.getStandardDays(); x++) {
//                if (x % 7 == 0) {
//                    cellCount = 1;
//                    dateRow = sheet.createRow(rowCount++);
//                    dailyImpGoalRow = sheet.createRow(rowCount++);
//                    impGoalToDateRow = sheet.createRow(rowCount++);
//                    goalPercentageDeliveredRow = sheet.createRow(rowCount++);
//                    rowCount++;
//                    dailyImpActualRow = sheet.createRow(rowCount++);
//                    actualImpsToDateRow = sheet.createRow(rowCount++);
//                    actualPercentageDeliveredRow = sheet.createRow(rowCount++);
//                    rowCount++;
//                    neededImpGoal = sheet.createRow(rowCount++);
//                    blackRow = sheet.createRow(rowCount++);
//                    for (int y = 0; y < 8; y++) blackRow.createCell(y).setCellStyle(black);
//                    dailyImpGoalRow.createCell(0).setCellValue("IMPS GOAL By Day");
//                    impGoalToDateRow.createCell(0).setCellValue("IMPS GOAL to Date");
//                    goalPercentageDeliveredRow.createCell(0).setCellValue("GOAL Percentage Delivered");
//                    dailyImpActualRow.createCell(0).setCellValue("IMPS ACTUAL By Day");
//                    actualImpsToDateRow.createCell(0).setCellValue("IMPS ACTUAL To Date");
//                    actualPercentageDeliveredRow.createCell(0).setCellValue("ACTUAL Percentage Delivered");
//                    neededImpGoal.createCell(0).setCellValue("DAILY PACING REQ'D TO HIT GOAL");
//                }
//                String currentDate = earliest.plusDays(x).toString("dd-MMM");
//                dateRow.createCell(cellCount).setCellValue(currentDate);
//                dateRow.getCell(cellCount).setCellStyle(date);
//
//                int impActualDay = 0;
//
//                dailyImpGoalRow.createCell(cellCount).setCellValue(dailyBudget);
//                impGoalToDateRow.createCell(cellCount).setCellValue(dailyBudget * (x+1));
//                goalPercentageDeliveredRow.createCell(cellCount)
//                        .setCellValue((float)(dailyBudget * x+1) / lifetimeDeliveryGoal);
//
//                // Last day of Data, set pacing lower
//                if (x+1 > dur.getStandardDays()) {
//                    dailyImpGoalRow.getCell(cellCount).setCellValue(lastDayBudget);
//                    impGoalToDateRow.getCell(cellCount)
//                            .setCellValue(dailyBudget * (x-1) + lastDayBudget);
//                    goalPercentageDeliveredRow.createCell(cellCount)
//                            .setCellValue((float) (dailyBudget * (x - 1) + lastDayBudget));
//                }
//
//                dailyImpGoalRow.getCell(cellCount).setCellStyle(number);
//                impGoalToDateRow.getCell(cellCount).setCellStyle(number);
//                goalPercentageDeliveredRow.getCell(cellCount).setCellStyle(percentage);
//
//                for (PacingLineItem l : a.getLineItems()) {
//                    for (ImpressionDateBudget d : l.getDailyData()) {
//                        if (d.getDate().toString("dd-MMM").equals(currentDate)) {
//
//                            impActualDay += d.getImpressions();
//                            impActualDate += d.getImpressions();
//
//                            dailyImpActualRow.createCell(cellCount).setCellValue(impActualDay);
//                            actualImpsToDateRow.createCell(cellCount).setCellValue(impActualDate);
//                            actualPercentageDeliveredRow.createCell(cellCount)
//                                    .setCellValue((float)impActualDate / lifetimeDeliveryGoal);
//                            neededImpGoal.createCell(cellCount).setCellValue(impActualDate / (dur.getStandardDays() - x));
//
//                            dailyImpActualRow.getCell(cellCount).setCellStyle(number);
//                            actualImpsToDateRow.getCell(cellCount).setCellStyle(number);
//                            actualPercentageDeliveredRow.getCell(cellCount).setCellStyle(percentage);
//                            neededImpGoal.getCell(cellCount).setCellStyle(number);
//                        }
//                    }
//                }
//                cellCount++;
//            }
//            for (Cell c : yellowDayHeader) sheet.autoSizeColumn(c.getColumnIndex());
//        }
//        return wb;
//    }
//}
