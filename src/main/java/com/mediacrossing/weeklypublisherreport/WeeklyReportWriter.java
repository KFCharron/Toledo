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

        //Write summary sheet
        Sheet summarySheet = wb.createSheet("Overview");

        //Create Universal Cell Styles
        DataFormat df = wb.createDataFormat();
        CellStyle fullCurrency = wb.createCellStyle();
        fullCurrency.setDataFormat(df.getFormat("$#,##0.00"));
        fullCurrency.setBorderTop(CellStyle.BORDER_THIN);
        fullCurrency.setBorderBottom(CellStyle.BORDER_THIN);
        fullCurrency.setBorderLeft(CellStyle.BORDER_THIN);
        fullCurrency.setBorderRight(CellStyle.BORDER_THIN);

        CellStyle fullCurrencyBottomRight = wb.createCellStyle();
        fullCurrencyBottomRight.cloneStyleFrom(fullCurrency);
        fullCurrencyBottomRight.setBorderBottom(CellStyle.BORDER_THICK);
        fullCurrencyBottomRight.setBorderRight(CellStyle.BORDER_THICK);
        fullCurrencyBottomRight.setBorderTop(CellStyle.BORDER_MEDIUM);

        CellStyle fullCurrencyBottom = wb.createCellStyle();
        fullCurrencyBottom.cloneStyleFrom(fullCurrencyBottomRight);
        fullCurrencyBottom.setBorderRight(CellStyle.BORDER_THIN);
        fullCurrencyBottom.setBorderTop(CellStyle.BORDER_MEDIUM);


        CellStyle normal = wb.createCellStyle();
        normal.setBorderBottom(CellStyle.BORDER_THIN);
        normal.setBorderTop(CellStyle.BORDER_THIN);
        normal.setBorderLeft(CellStyle.BORDER_THIN);
        normal.setBorderRight(CellStyle.BORDER_THIN);

        CellStyle top = wb.createCellStyle();
        top.setBorderTop(CellStyle.BORDER_THICK);
        top.setBorderLeft(CellStyle.BORDER_THIN);
        top.setBorderRight(CellStyle.BORDER_THIN);
        top.setBorderBottom(CellStyle.BORDER_MEDIUM);
        Font green = wb.createFont();
        green.setBoldweight((short)700);
        green.setFontHeightInPoints((short)14);
        green.setColor(IndexedColors.GREEN.getIndex());
        top.setFont(green);
        top.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        top.setFillPattern(CellStyle.SOLID_FOREGROUND);

        CellStyle topRight = wb.createCellStyle();
        topRight.setBorderTop(CellStyle.BORDER_THICK);
        topRight.setBorderRight(CellStyle.BORDER_THICK);
        topRight.setBorderLeft(CellStyle.BORDER_THIN);
        topRight.setBorderBottom(CellStyle.BORDER_MEDIUM);
        topRight.setFont(green);
        topRight.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        topRight.setFillPattern(CellStyle.SOLID_FOREGROUND);

        CellStyle right = wb.createCellStyle();
        right.setBorderRight(CellStyle.BORDER_THICK);
        right.setBorderLeft(CellStyle.BORDER_THIN);
        right.setBorderTop(CellStyle.BORDER_THIN);
        right.setBorderBottom(CellStyle.BORDER_THIN);
        right.setDataFormat(df.getFormat("$#,##0.00"));

        CellStyle bottomRight = wb.createCellStyle();
        bottomRight.setBorderBottom(CellStyle.BORDER_THICK);
        bottomRight.setBorderRight(CellStyle.BORDER_THICK);
        bottomRight.setBorderTop(CellStyle.BORDER_MEDIUM);
        bottomRight.setBorderLeft(CellStyle.BORDER_THIN);

        CellStyle bottom = wb.createCellStyle();
        bottom.setBorderBottom(CellStyle.BORDER_THICK);
        bottom.setBorderTop(CellStyle.BORDER_MEDIUM);
        bottom.setBorderLeft(CellStyle.BORDER_THIN);
        bottom.setBorderRight(CellStyle.BORDER_THIN);

        CellStyle bottomLeft = wb.createCellStyle();
        bottomLeft.setBorderBottom(CellStyle.BORDER_THICK);
        bottomLeft.setBorderLeft(CellStyle.BORDER_THICK);
        bottomLeft.setBorderTop(CellStyle.BORDER_MEDIUM);
        bottomLeft.setBorderRight(CellStyle.BORDER_THIN);
        Font bold = wb.createFont();
        bold.setBoldweight((short)1000);
        bottomLeft.setFont(bold);

        CellStyle left = wb.createCellStyle();
        left.setBorderLeft(CellStyle.BORDER_THICK);
        left.setBorderRight(CellStyle.BORDER_THIN);
        left.setBorderTop(CellStyle.BORDER_THIN);
        left.setBorderBottom(CellStyle.BORDER_THIN);

        CellStyle topLeft = wb.createCellStyle();
        topLeft.setBorderTop(CellStyle.BORDER_THICK);
        topLeft.setBorderLeft(CellStyle.BORDER_THICK);
        topLeft.setBorderBottom(CellStyle.BORDER_MEDIUM);
        topLeft.setBorderRight(CellStyle.BORDER_THIN);
        topLeft.setFont(green);
        topLeft.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        topLeft.setFillPattern(CellStyle.SOLID_FOREGROUND);

        CellStyle list = wb.createCellStyle();
        list.setBorderTop(CellStyle.BORDER_THIN);
        list.setBorderLeft(CellStyle.BORDER_THICK);
        list.setBorderRight(CellStyle.BORDER_THICK);
        list.setBorderBottom(CellStyle.BORDER_THIN);

        CellStyle lastList = wb.createCellStyle();
        lastList.setBorderTop(CellStyle.BORDER_THIN);
        lastList.setBorderBottom(CellStyle.BORDER_THICK);
        lastList.setBorderLeft(CellStyle.BORDER_THICK);
        lastList.setBorderRight(CellStyle.BORDER_THICK);

        CellStyle topList = wb.createCellStyle();
        topList.setBorderTop(CellStyle.BORDER_THICK);
        topList.setBorderLeft(CellStyle.BORDER_THICK);
        topList.setBorderRight(CellStyle.BORDER_THICK);
        topList.setBorderBottom(CellStyle.BORDER_MEDIUM);
        topList.setFont(green);
        topList.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        topList.setFillPattern(CellStyle.SOLID_FOREGROUND);

        //Build array list of totals for each day
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
                       daily.setPublisherRevenue(daily.getPublisherRevenue() + d.getPublisherRevenue());
                       grandTotal.setPublisherRevenue(grandTotal.getPublisherRevenue() + d.getPublisherRevenue());
                       daily.setUnfilled(daily.getUnfilled() + d.getUnfilled());
                       grandTotal.setUnfilled(grandTotal.getUnfilled() + d.getUnfilled());
                       daily.setNetworkRevenue(daily.getNetworkRevenue() + d.getNetworkRevenue());
                       grandTotal.setNetworkRevenue(daily.getNetworkRevenue() + d.getNetworkRevenue());
                       cpmCount++;
                       daily.seteCpm((daily.geteCpm() + d.geteCpm()) / cpmCount);
                   }
                }
            }
            dailyTotals.add(daily);
            grandTotal.seteCpm(grandTotal.geteCpm() + daily.geteCpm());
        }
        Row header = summarySheet.createRow(1);
        header.createCell(1).setCellValue("Day");
        header.createCell(2).setCellValue("Avails");
        header.createCell(3).setCellValue("Imps");
        header.createCell(4).setCellValue("Unfilled");
        header.createCell(5).setCellValue("Errors");
        header.createCell(6).setCellValue("Avg eCPM");
        header.createCell(7).setCellValue("Gross Rev.");
        header.createCell(8).setCellValue("Publisher Rev.");

        for (Cell c : header) c.setCellStyle(top);
        header.getCell(1).setCellStyle(topLeft);
        header.getCell(8).setCellStyle(topRight);

        int rowCount = 1;

        for (DailyPublisherData d : dailyTotals) {
            Row dataRow = summarySheet.createRow(++rowCount);
            dataRow.createCell(1).setCellValue(d.getDate().getMonthOfYear() +
                    "/" + d.getDate().getDayOfMonth());
            dataRow.createCell(2).setCellValue(d.getAvails());
            dataRow.createCell(3).setCellValue(d.getImps());
            dataRow.createCell(4).setCellValue(d.getUnfilled());
            dataRow.createCell(5).setCellValue(d.getErrors());
            dataRow.createCell(6).setCellValue(d.geteCpm());
            dataRow.createCell(7).setCellValue(d.getNetworkRevenue());
            dataRow.createCell(8).setCellValue(d.getPublisherRevenue());

            for (Cell c : dataRow) c.setCellStyle(normal);
            dataRow.getCell(1).setCellStyle(left);
            dataRow.getCell(6).setCellStyle(fullCurrency);
            dataRow.getCell(7).setCellStyle(fullCurrency);
            dataRow.getCell(8).setCellStyle(right);
        }
        rowCount++;
        Row blankRow = summarySheet.createRow(rowCount);
        for (int x = 1; x < 9; x++) blankRow.createCell(x).setCellStyle(normal);
        blankRow.getCell(1).setCellStyle(left);
        blankRow.getCell(8).setCellStyle(right);

        Row totalRow = summarySheet.createRow(++rowCount);
        totalRow.createCell(1).setCellValue("Grand Totals:");
        totalRow.createCell(2).setCellValue(grandTotal.getAvails());
        totalRow.createCell(3).setCellValue(grandTotal.getImps());
        totalRow.createCell(4).setCellValue(grandTotal.getUnfilled());
        totalRow.createCell(5).setCellValue(grandTotal.getErrors());
        totalRow.createCell(6).setCellValue(grandTotal.geteCpm() / 7);
        totalRow.createCell(7).setCellValue(grandTotal.getNetworkRevenue());
        totalRow.createCell(8).setCellValue(grandTotal.getPublisherRevenue());

        for (Cell c : totalRow) c.setCellStyle(bottom);
        totalRow.getCell(1).setCellStyle(bottomLeft);
        totalRow.getCell(6).setCellStyle(fullCurrencyBottom);
        totalRow.getCell(7).setCellStyle(fullCurrencyBottom);
        totalRow.getCell(8).setCellStyle(fullCurrencyBottomRight);

        //autosize each column
        for (int x = 0; x < 9; x++) summarySheet.autoSizeColumn(x);

        //Write sheet for each placement
        for (WeeklyPlacement place : p.getPlacements()) {
            String sheetName = WorkbookUtil.createSafeSheetName(place.getName()+"("+place.getId()+")");
            Sheet placementSheet;
            try {
                placementSheet = wb.createSheet(sheetName);
            }catch (Exception e) {
                //duplicate found, fix it.
                placementSheet =
                        wb.createSheet(WorkbookUtil.createSafeSheetName("(" + place.getId() + ")" + place.getName()));
            }
            Row headerRow = placementSheet.createRow(1);
            headerRow.createCell(1).setCellValue("Day");
            headerRow.createCell(2).setCellValue("Avails");
            headerRow.createCell(3).setCellValue("Imps");
            headerRow.createCell(4).setCellValue("Unfilled");
            headerRow.createCell(5).setCellValue("Errors");
            headerRow.createCell(6).setCellValue("eCPM");
            headerRow.createCell(7).setCellValue("Gross Rev.");
            headerRow.createCell(8).setCellValue("Publisher Rev.");

            for (Cell c : headerRow) c.setCellStyle(top);
            headerRow.getCell(1).setCellStyle(topLeft);
            headerRow.getCell(8).setCellStyle(topRight);


            rowCount = 1;
            int datacount = 0;
            for (DailyPublisherData data : place.getDailyDataList()) {
                Row dataRow = placementSheet.createRow(++rowCount);
                dataRow.createCell(1).setCellValue(data.getDate().getMonthOfYear() +
                        "/" + data.getDate().getDayOfMonth());
                dataRow.createCell(2).setCellValue(data.getAvails());
                dataRow.createCell(3).setCellValue(data.getImps());
                dataRow.createCell(4).setCellValue(data.getUnfilled());
                dataRow.createCell(5).setCellValue(data.getErrors());
                dataRow.createCell(6).setCellValue(data.geteCpm());
                dataRow.createCell(7).setCellValue(data.getNetworkRevenue());
                dataRow.createCell(8).setCellValue(data.getPublisherRevenue());

                for (Cell c : dataRow) c.setCellStyle(normal);
                dataRow.getCell(1).setCellStyle(left);
                dataRow.getCell(6).setCellStyle(fullCurrency);
                dataRow.getCell(7).setCellStyle(fullCurrency);
                dataRow.getCell(8).setCellStyle(right);

                datacount++;
                if (datacount == place.getDailyDataList().size()) {
                    for (Cell c : dataRow) c.setCellStyle(bottom);
                    dataRow.getCell(1).setCellStyle(bottomLeft);
                    dataRow.getCell(6).setCellStyle(fullCurrencyBottom);
                    dataRow.getCell(7).setCellStyle(fullCurrencyBottom);
                    dataRow.getCell(8).setCellStyle(fullCurrencyBottomRight);
                }
            }

            for (int x = 0; x < 9; x++) placementSheet.autoSizeColumn(x);

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
