package com.mediacrossing.weeklydomainreport;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class WeeklyDomainReportWriter {

    public static void writeReportToFile (ArrayList<Domain> domainList, String outputPath) throws IOException {

        Workbook wb = new HSSFWorkbook();

        Sheet sheet = wb.createSheet("Domain Performance");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Domain");
        header.createCell(1).setCellValue("Revenue");
        header.createCell(2).setCellValue("Clicks");
        header.createCell(3).setCellValue("ClickThrough %");
        header.createCell(4).setCellValue("ConvsPerMM");
        header.createCell(5).setCellValue("ConvsRate");
        header.createCell(6).setCellValue("Cost eCPA");
        header.createCell(7).setCellValue("Cost eCPC");
        header.createCell(8).setCellValue("CPM");
        header.createCell(9).setCellValue("CTR");
        header.createCell(10).setCellValue("Imps");
        header.createCell(11).setCellValue("Media Cost");
        header.createCell(12).setCellValue("Post Click Convs");
        header.createCell(13).setCellValue("Post Click Conv Rate");
        header.createCell(14).setCellValue("Post View Convs");
        header.createCell(15).setCellValue("Post View Convs Rate");
        header.createCell(16).setCellValue("Profit");
        header.createCell(17).setCellValue("Profit eCPM");

        CellStyle headerStyle = wb.createCellStyle();
        Font headerFont = wb.createFont();
        headerFont.setBoldweight((short)1000);
        headerFont.setColor(IndexedColors.GREEN.getIndex());
        headerStyle.setFont(headerFont);

        for (Cell c : header) c.setCellStyle(headerStyle);

        DataFormat df = wb.createDataFormat();

        CellStyle currency = wb.createCellStyle();
        currency.setDataFormat(df.getFormat("$#,##0.00"));

        CellStyle percentage = wb.createCellStyle();
        percentage.setDataFormat(df.getFormat("##0.####%"));

        CellStyle decimal = wb.createCellStyle();
        decimal.setDataFormat(df.getFormat("##0.0##"));

        int rowCount = 1;

        for (Domain d : domainList) {
            Row data = sheet.createRow(++rowCount);
            data.createCell(0).setCellValue(d.getName());
            data.createCell(1).setCellValue(d.getBookedRevenue());
            data.createCell(2).setCellValue(d.getClicks());
            data.createCell(3).setCellValue(d.getClickThroughPct());
            data.createCell(4).setCellValue(d.getConvsPerMm());
            data.createCell(5).setCellValue(d.getConvsRate());
            data.createCell(6).setCellValue(d.getCostEcpa());
            data.createCell(7).setCellValue(d.getCostEcpc());
            data.createCell(8).setCellValue(d.getCpm());
            data.createCell(9).setCellValue(d.getCtr());
            data.createCell(10).setCellValue(d.getImps());
            data.createCell(11).setCellValue(d.getMediaCost());
            data.createCell(12).setCellValue(d.getPostClickConvs());
            data.createCell(13).setCellValue(d.getPostClickConvsRate());
            data.createCell(14).setCellValue(d.getPostViewConvs());
            data.createCell(15).setCellValue(d.getPostViewConvsRate());
            data.createCell(16).setCellValue(d.getProfit());
            data.createCell(17).setCellValue(d.getProfitEcpm());

            data.getCell(1).setCellStyle(currency);
            data.getCell(6).setCellStyle(currency);
            data.getCell(7).setCellStyle(currency);
            data.getCell(8).setCellStyle(currency);
            data.getCell(11).setCellStyle(currency);
            data.getCell(16).setCellStyle(currency);
            data.getCell(17).setCellStyle(currency);

            data.getCell(3).setCellStyle(percentage);
            data.getCell(5).setCellStyle(percentage);
            data.getCell(9).setCellStyle(percentage);
            data.getCell(13).setCellStyle(percentage);
            data.getCell(15).setCellStyle(percentage);

            data.getCell(4).setCellStyle(decimal);
        }

        for (int x = 0; x < 18; x++) sheet.autoSizeColumn(x);

        //Export file
        LocalDate now = new LocalDate(DateTimeZone.UTC);
        FileOutputStream fileOut =
                new FileOutputStream(new File(outputPath, "WeeklyDomainPerformanceReport_"
                        +now.toString()+".xls"));
        wb.write(fileOut);
        fileOut.close();
    }
}
