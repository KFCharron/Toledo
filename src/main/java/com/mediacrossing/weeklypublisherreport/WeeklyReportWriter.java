package com.mediacrossing.weeklypublisherreport;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class WeeklyReportWriter {

    public static void writeReportToFile (WeeklyPublisher p, String outputPath) throws IOException {

        //Create workbook
        Workbook wb = new HSSFWorkbook();
        DataFormat df = wb.createDataFormat();
        CellStyle fullCurrency = wb.createCellStyle();
        fullCurrency.setDataFormat(df.getFormat("$#,##0.00"));

        //Write summary sheet
        Sheet summarySheet = wb.createSheet("Overview");

        //Build arraylist of totals for each day
        DateTime today = new DateTime();
        ArrayList<DailyPublisherData> dailyTotals = new ArrayList<DailyPublisherData>();
        DailyPublisherData grandTotal = new DailyPublisherData(today);
        for (int x = 7; x > 0; x--) {
            DailyPublisherData daily = new DailyPublisherData(today.minusDays(x));
            int cpmCount = 0;
            for (WeeklyPlacement pl : p.getPlacements()) {
                for (DailyPublisherData d : pl.getDailyDataList()) {
                   if (today.minusDays(x).getDayOfMonth() == d.getDate().getDayOfMonth()) {
                       daily.setAvails(daily.getAvails() + d.getAvails());
                       grandTotal.setAvails(grandTotal.getAvails() + d.getAvails());
                       daily.setImps(daily.getImps() + d.getImps());
                       grandTotal.setImps(grandTotal.getImps() + d.getImps());
                       daily.setErrors(daily.getErrors() + d.getErrors());
                       grandTotal.setErrors(grandTotal.getErrors() + d.getErrors());
                       daily.setGross(daily.getGross() + d.getGross());
                       grandTotal.setGross(grandTotal.getGross() + d.getGross());
                       daily.setUnfilled(daily.getUnfilled() + d.getUnfilled());
                       grandTotal.setUnfilled(grandTotal.getUnfilled() + d.getUnfilled());
                       cpmCount++;
                       daily.seteCpm((daily.geteCpm() + d.geteCpm()) / cpmCount);
                   }
                }
            }
            dailyTotals.add(daily);
            grandTotal.seteCpm(grandTotal.geteCpm() + daily.geteCpm());
        }
        Row header = summarySheet.createRow(0);
        header.createCell(0).setCellValue("Day");
        header.createCell(1).setCellValue("Avails");
        header.createCell(2).setCellValue("Imps");
        header.createCell(3).setCellValue("Unfilled");
        header.createCell(4).setCellValue("Errors");
        header.createCell(5).setCellValue("Avg eCPM");
        header.createCell(6).setCellValue("Gross Rev.");

        int rowCount = 1;

        for (DailyPublisherData d : dailyTotals) {
            Row dataRow = summarySheet.createRow(++rowCount);
            dataRow.createCell(0).setCellValue(d.getDate().getMonthOfYear() +
                    "/" + d.getDate().getDayOfMonth());
            dataRow.createCell(1).setCellValue(d.getAvails());
            dataRow.createCell(2).setCellValue(d.getImps());
            dataRow.createCell(3).setCellValue(d.getUnfilled());
            dataRow.createCell(4).setCellValue(d.getErrors());
            dataRow.createCell(5).setCellValue(d.geteCpm());
            dataRow.createCell(6).setCellValue(d.getGross());

            dataRow.getCell(5).setCellStyle(fullCurrency);
            dataRow.getCell(6).setCellStyle(fullCurrency);
        }
        rowCount++;
        Row totalRow = summarySheet.createRow(++rowCount);
        totalRow.createCell(0).setCellValue("Grand Totals:");
        totalRow.createCell(1).setCellValue(grandTotal.getAvails());
        totalRow.createCell(2).setCellValue(grandTotal.getImps());
        totalRow.createCell(3).setCellValue(grandTotal.getUnfilled());
        totalRow.createCell(4).setCellValue(grandTotal.getErrors());
        totalRow.createCell(5).setCellValue(grandTotal.geteCpm() / 7);
        totalRow.createCell(6).setCellValue(grandTotal.getGross());

        totalRow.getCell(5).setCellStyle(fullCurrency);
        totalRow.getCell(6).setCellStyle(fullCurrency);

        //autosize each column
        for (int x = 0; x < 7; x++) summarySheet.autoSizeColumn(x);

        //Write sheet for each placement
        for (WeeklyPlacement place : p.getPlacements()) {
            String sheetName = WorkbookUtil.createSafeSheetName(place.getName()+"("+place.getId()+")");
            Sheet placementSheet = wb.createSheet(sheetName);
            Row headerRow = placementSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Day");
            headerRow.createCell(1).setCellValue("Avails");
            headerRow.createCell(2).setCellValue("Imps");
            headerRow.createCell(3).setCellValue("Unfilled");
            headerRow.createCell(4).setCellValue("Errors");
            headerRow.createCell(5).setCellValue("eCPM");
            headerRow.createCell(6).setCellValue("Gross Rev.");

            rowCount = 1;

            for (DailyPublisherData data : place.getDailyDataList()) {
                Row dataRow = placementSheet.createRow(++rowCount);
                dataRow.createCell(0).setCellValue(data.getDate().getMonthOfYear() +
                        "/" + data.getDate().getDayOfMonth());
                dataRow.createCell(1).setCellValue(data.getAvails());
                dataRow.createCell(2).setCellValue(data.getImps());
                dataRow.createCell(3).setCellValue(data.getUnfilled());
                dataRow.createCell(4).setCellValue(data.getErrors());
                dataRow.createCell(5).setCellValue(data.geteCpm());
                dataRow.createCell(6).setCellValue(data.getGross());

                dataRow.getCell(5).setCellStyle(fullCurrency);
                dataRow.getCell(6).setCellStyle(fullCurrency);
            }

            for (int x = 0; x < 7; x++) placementSheet.autoSizeColumn(x);

        }

        //Export file
        LocalDate now = new LocalDate(DateTimeZone.UTC);
        FileOutputStream fileOut =
                new FileOutputStream(new File(outputPath, p.getName() + "_WeeklyReport_"
                        +now.toString()+".xls"));
        wb.write(fileOut);
        fileOut.close();

    }
}
