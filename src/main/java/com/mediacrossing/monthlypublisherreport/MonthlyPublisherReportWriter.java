package com.mediacrossing.monthlypublisherreport;

import com.mediacrossing.weeklypublisherreport.DailyPublisherData;
import com.mediacrossing.weeklypublisherreport.WeeklyPlacement;
import com.mediacrossing.weeklypublisherreport.WeeklyPublisher;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MonthlyPublisherReportWriter {

    public static void writeReportToFile (WeeklyPublisher p, String outputPath) throws IOException {

        //Create workbook
        Workbook wb = new HSSFWorkbook();

        //Create Universal Cell Styles
        DataFormat df = wb.createDataFormat();
        CellStyle fullCurrency = wb.createCellStyle();
        fullCurrency.setDataFormat(df.getFormat("$#,##0.00"));

        CellStyle top = wb.createCellStyle();
        top.setBorderTop(CellStyle.BORDER_THICK);
        Font green = wb.createFont();
        green.setBoldweight((short)1000);
        green.setColor(IndexedColors.LIGHT_GREEN.getIndex());

        CellStyle topRight = wb.createCellStyle();
        topRight.setBorderTop(CellStyle.BORDER_THICK);
        topRight.setBorderRight(CellStyle.BORDER_THICK);

        CellStyle right = wb.createCellStyle();
        right.setBorderRight(CellStyle.BORDER_THICK);

        CellStyle bottomRight = wb.createCellStyle();
        bottomRight.setBorderBottom(CellStyle.BORDER_THICK);
        bottomRight.setBorderRight(CellStyle.BORDER_THICK);

        CellStyle bottom = wb.createCellStyle();
        bottom.setBorderBottom(CellStyle.BORDER_THICK);

        CellStyle bottomLeft = wb.createCellStyle();
        bottomLeft.setBorderBottom(CellStyle.BORDER_THICK);
        bottomLeft.setBorderLeft(CellStyle.BORDER_THICK);

        CellStyle left = wb.createCellStyle();
        left.setBorderLeft(CellStyle.BORDER_THICK);

        CellStyle topLeft = wb.createCellStyle();
        topLeft.setBorderTop(CellStyle.BORDER_THICK);
        topLeft.setBorderLeft(CellStyle.BORDER_THICK);

        //Write summary sheet
        Sheet summarySheet = wb.createSheet("Overview");

        //Build arraylist of totals for each day
        //get first of last month
        DateTime today = new DateTime();
        DateTime lastMonth = today.minusMonths(1).withDayOfMonth(1);
        ArrayList<DailyPublisherData> dailyTotals = new ArrayList<DailyPublisherData>();
        DailyPublisherData grandTotal = new DailyPublisherData(today);

        //step through and accumulate grand totals
        for (int x = 1; x <= lastMonth.dayOfMonth().getMaximumValue(); x++) {
            DailyPublisherData daily = new DailyPublisherData(lastMonth.plusDays(x-1));
            int cpmCount = 0;
            for (WeeklyPlacement pl : p.getPlacements()) {
                for (DailyPublisherData d : pl.getDailyDataList()) {
                    if (x == d.getDate().getDayOfMonth()) {
                        daily.setAvails(daily.getAvails() + d.getAvails());
                        grandTotal.setAvails(grandTotal.getAvails() + d.getAvails());
                        daily.setImps(daily.getImps() + d.getImps());
                        grandTotal.setImps(grandTotal.getImps() + d.getImps());
                        daily.setErrors(daily.getErrors() + d.getErrors());
                        grandTotal.setErrors(grandTotal.getErrors() + d.getErrors());
                        daily.setPublisherRevenue(daily.getPublisherRevenue() + d.getPublisherRevenue());
                        grandTotal.setPublisherRevenue(grandTotal.getPublisherRevenue() + d.getPublisherRevenue());
                        daily.setNetworkRevenue(daily.getNetworkRevenue() + d.getNetworkRevenue());
                        grandTotal.setNetworkRevenue(grandTotal.getNetworkRevenue() + d.getNetworkRevenue());
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

        //Headers for summary sheet
        Row header = summarySheet.createRow(0);
        header.createCell(0).setCellValue("Day");
        header.createCell(1).setCellValue("Avails");
        header.createCell(2).setCellValue("Imps");
        header.createCell(3).setCellValue("Unfilled");
        header.createCell(4).setCellValue("Errors");
        header.createCell(5).setCellValue("Avg eCPM");
        header.createCell(6).setCellValue("Network Rev.");
        header.createCell(7).setCellValue("Pub Rev.");
        header.createCell(11).setCellValue("Top Brands");
        header.createCell(12).setCellValue("Top Buyers");

        int rowCount = 1;
        //list every total
        for (DailyPublisherData d : dailyTotals) {
            Row dataRow = summarySheet.createRow(++rowCount);
            dataRow.createCell(0).setCellValue(d.getDate().getMonthOfYear() +
                    "/" + d.getDate().getDayOfMonth());
            dataRow.createCell(1).setCellValue(d.getAvails());
            dataRow.createCell(2).setCellValue(d.getImps());
            dataRow.createCell(3).setCellValue(d.getUnfilled());
            dataRow.createCell(4).setCellValue(d.getErrors());
            dataRow.createCell(5).setCellValue(d.geteCpm());
            dataRow.createCell(6).setCellValue(d.getNetworkRevenue());
            dataRow.createCell(7).setCellValue(d.getPublisherRevenue());

            dataRow.getCell(5).setCellStyle(fullCurrency);
            dataRow.getCell(6).setCellStyle(fullCurrency);
            dataRow.getCell(7).setCellStyle(fullCurrency);



            int topCount = rowCount - 2;
            if (topCount < p.getTopBrands().size()) {
                dataRow.createCell(11).setCellValue(p.getTopBrands().get(topCount));
            }
            if (topCount < p.getTopBuyers().size()) {
                dataRow.createCell(12).setCellValue(p.getTopBuyers().get(topCount));
            }
        }
        rowCount++;
        Row totalRow = summarySheet.createRow(++rowCount);
        totalRow.createCell(0).setCellValue("Grand Totals:");
        totalRow.createCell(1).setCellValue(grandTotal.getAvails());
        totalRow.createCell(2).setCellValue(grandTotal.getImps());
        totalRow.createCell(3).setCellValue(grandTotal.getUnfilled());
        totalRow.createCell(4).setCellValue(grandTotal.getErrors());
        totalRow.createCell(6).setCellValue(grandTotal.getNetworkRevenue());
        totalRow.createCell(7).setCellValue(grandTotal.getPublisherRevenue());

        totalRow.getCell(6).setCellStyle(fullCurrency);
        totalRow.getCell(7).setCellStyle(fullCurrency);




        //auto-size each column
        for (int x = 0; x < 13; x++) summarySheet.autoSizeColumn(x);

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
            Row headerRow = placementSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Day");
            headerRow.createCell(1).setCellValue("Avails");
            headerRow.createCell(2).setCellValue("Imps");
            headerRow.createCell(3).setCellValue("Unfilled");
            headerRow.createCell(4).setCellValue("Errors");
            headerRow.createCell(5).setCellValue("eCPM");
            headerRow.createCell(6).setCellValue("Publisher Rev.");
            headerRow.createCell(7).setCellValue("Network Rev.");

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
                dataRow.createCell(6).setCellValue(data.getPublisherRevenue());
                dataRow.createCell(7).setCellValue(data.getNetworkRevenue());

                dataRow.getCell(5).setCellStyle(fullCurrency);
                dataRow.getCell(6).setCellStyle(fullCurrency);
                dataRow.getCell(7).setCellStyle(fullCurrency);
            }

            for (int x = 0; x < 8; x++) placementSheet.autoSizeColumn(x);

        }

        //Export file
        LocalDate now = new LocalDate(DateTimeZone.UTC);
        FileOutputStream fileOut =
                new FileOutputStream(new File(outputPath, p.getName() + "_MonthlyReport_"
                        +now.toString()+".xls"));
        wb.write(fileOut);
        fileOut.close();

    }
}
