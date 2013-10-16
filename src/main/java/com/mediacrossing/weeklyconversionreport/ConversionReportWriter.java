package com.mediacrossing.weeklyconversionreport;

import java.util.ArrayList;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ConversionReportWriter {

    public static void writeReportToFile (ArrayList<ConversionAdvertiser> adList, String outputPath) throws IOException {

        //create new workbook + sheet
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("Conversions Report");
        int rowCount = 0;

        DataFormat df = wb.createDataFormat();
        CellStyle fullCurrency = wb.createCellStyle();
        fullCurrency.setDataFormat(df.getFormat("$#,##0.00"));

        for (ConversionAdvertiser ad : adList) {
            if(ad.getConversionDataList().size() > 0) {
                //name row
                Row nameRow = sheet.createRow(rowCount);
                nameRow.createCell(0);
                nameRow.createCell(1).setCellValue(ad.getAdvertiserName() + " (" + ad.getAdvertiserId() + ")");

                //create solid black cell to distinguish new line items
                CellStyle solidBlack = wb.createCellStyle();
                solidBlack.setFillForegroundColor(IndexedColors.BLACK.index);
                solidBlack.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                nameRow.getCell(0).setCellStyle(solidBlack);

                //style name
                Font font = wb.createFont();
                font.setFontHeightInPoints((short) 14);
                font.setBoldweight((short) 700);
                CellStyle bold = wb.createCellStyle();
                bold.setFont(font);
                nameRow.getCell(1).setCellStyle(bold);

                rowCount++;

                //header row
                Row headerRow = sheet.createRow(rowCount);
                headerRow.createCell(0).setCellValue("LineItem");
                headerRow.createCell(1).setCellValue("Campaign");
                headerRow.createCell(2).setCellValue("Creative");
                headerRow.createCell(3).setCellValue("Pixel ID");
                headerRow.createCell(4).setCellValue("Pixel Name");
                headerRow.createCell(5).setCellValue("Imp Type");
                headerRow.createCell(6).setCellValue("Post Click/Post View Conv.");
                headerRow.createCell(7).setCellValue("Post Click/Post View Rev.");
                headerRow.createCell(8).setCellValue("Order ID");
                headerRow.createCell(9).setCellValue("User ID");
                headerRow.createCell(10).setCellValue("Auction ID");
                headerRow.createCell(11).setCellValue("External Data");
                headerRow.createCell(12).setCellValue("Imp Time");
                headerRow.createCell(13).setCellValue("Datetime");


                //style header
                Font font2 = wb.createFont();
                font2.setFontHeightInPoints((short) 12);
                font2.setBoldweight((short) 700);
                CellStyle bold2 = wb.createCellStyle();
                bold2.setFont(font2);

                //bold each cell in header
                for(Cell cell : headerRow) {
                    cell.setCellStyle(bold2);
                }

                rowCount++;

                //for every conversion, list the data
                for(ConversionData data : ad.getConversionDataList()) {
                    Row dataRow = sheet.createRow(rowCount);
                    dataRow.createCell(0).setCellValue(data.getLineItem());
                    dataRow.createCell(1).setCellValue(data.getCampaign());
                    dataRow.createCell(2).setCellValue(data.getCreative());
                    dataRow.createCell(3).setCellValue(data.getPixelId());
                    dataRow.createCell(4).setCellValue(data.getPixelName());
                    dataRow.createCell(5).setCellValue(data.getImpType());
                    dataRow.createCell(6).setCellValue(data.getPostClickOrPostViewConv());
                    dataRow.createCell(7).setCellValue(Float.parseFloat(data.getPostClickOrPoseViewRevenue()));
                    dataRow.createCell(8).setCellValue(data.getOrderId());
                    dataRow.createCell(9).setCellValue(data.getUserId());
                    dataRow.createCell(10).setCellValue(data.getAuctionId());
                    dataRow.createCell(11).setCellValue(data.getExternalData());
                    dataRow.createCell(12).setCellValue(data.getImpTime());
                    dataRow.createCell(13).setCellValue(data.getDatetime());

                    //style currency
                    dataRow.getCell(7).setCellStyle(fullCurrency);

                    rowCount++;
                }
                rowCount+=2;
            }
        }

        for(int x = 0; x<= 13; x++) {
            sheet.autoSizeColumn(x);
        }

        FileOutputStream fileOut = new FileOutputStream(new File(outputPath, "ConversionsReport.xls"));
        wb.write(fileOut);
        fileOut.close();
    }
}
