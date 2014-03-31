package com.mediacrossing.discrepancyreport;

import com.mediacrossing.publishercheckup.IdName;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;

public class DiscrepancyReportWriter {

    public static Workbook writeReport(ArrayList<Creative> creatives, ArrayList<IdName> idNames) {

        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("DFA vs AppNexus");

        CellStyle lineNames = wb.createCellStyle();
        Font bold = wb.createFont();
        bold.setBoldweight((short)1000);
        bold.setFontHeightInPoints((short)14);
        lineNames.setFont(bold);

        CellStyle solidBlack = wb.createCellStyle();
        solidBlack.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        solidBlack.setFillPattern(CellStyle.SOLID_FOREGROUND);

        CellStyle red = wb.createCellStyle();
        Font redFont = wb.createFont();
        redFont.setColor(IndexedColors.RED.getIndex());
        red.setFont(redFont);

        CellStyle percentage = wb.createCellStyle();
        DataFormat df = wb.createDataFormat();
        percentage.setDataFormat(df.getFormat("##0.00%"));

        CellStyle redPercentage = wb.createCellStyle();
        redPercentage.setDataFormat(df.getFormat("##0.00%"));
        redPercentage.setFont(redFont);

        int rowCount = 0;
        for (IdName tuple : idNames) {

            Row lineHeader = sheet.createRow(rowCount);
            lineHeader.setHeightInPoints((short) 15);
            lineHeader.createCell(0).setCellValue(tuple.getName());
            lineHeader.getCell(0).setCellStyle(lineNames);
            for (int x = 1; x < 10; x++) lineHeader.createCell(x).setCellStyle(solidBlack);


            rowCount++;
            rowCount++;
            Row header = sheet.createRow(rowCount);
            header.createCell(0).setCellValue("Creative");
            header.createCell(1).setCellValue("AN Imps");
            header.createCell(2).setCellValue("DFA Imps");
            header.createCell(3).setCellValue("Imps %");
            header.createCell(4).setCellValue("AN Clicks");
            header.createCell(5).setCellValue("DFA Clicks");
            header.createCell(6).setCellValue("Clicks %");
            header.createCell(7).setCellValue("AN Convs");
            header.createCell(8).setCellValue("DFA Convs");
            header.createCell(9).setCellValue("Convs %");

            rowCount++;
            rowCount++;

            Row totalRow = sheet.createRow(rowCount);
            totalRow.createCell(0).setCellValue("Totals:");

            rowCount++;
            rowCount++;

            Creative totals = new Creative();
            for (Creative c : creatives) {
                if (c.getLineItemId().
                        equals(tuple.getId())) {
                    Row data = sheet.createRow(rowCount);
                    data.createCell(0).setCellValue(c.getName() + "(" + c.getAppNexusId() + ")");
                    data.createCell(1).setCellValue(c.getAppNexusImps());
                    data.createCell(2).setCellValue(c.getDfaImps());
                    if (c.getDfaImps() == 0) data.createCell(3).setCellValue(0);
                    else data.createCell(3).setCellValue((float)(c.getDfaImps()-c.getAppNexusImps())
                            / c.getDfaImps());
                    data.getCell(3).setCellStyle(percentage);
                    data.createCell(4).setCellValue(c.getAppNexusClicks());
                    data.createCell(5).setCellValue(c.getDfaClicks());
                    if (c.getDfaClicks() == 0) data.createCell(6).setCellValue(0);
                    else data.createCell(6).setCellValue((float)(c.getDfaClicks() - c.getAppNexusClicks())
                            / c.getDfaClicks());
                    data.getCell(6).setCellStyle(percentage);
                    data.createCell(7).setCellValue(c.getAppNexusConvs());
                    data.createCell(8).setCellValue(c.getDfaConvs());
                    if (c.getDfaConvs() == 0) data.createCell(9).setCellValue(0);
                    else data.createCell(9).setCellValue((float)(c.getDfaConvs() - c.getAppNexusConvs())
                            / c.getDfaConvs());
                    data.getCell(9).setCellStyle(percentage);
                    totals.setAppNexusImps(totals.getAppNexusImps() + c.getAppNexusImps());
                    totals.setAppNexusClicks(totals.getAppNexusClicks() + c.getAppNexusClicks());
                    totals.setDfaImps(totals.getDfaImps() + c.getDfaImps());
                    totals.setDfaClicks(totals.getDfaClicks() + c.getDfaClicks());
                    totals.setAppNexusConvs(totals.getAppNexusConvs() + c.getAppNexusConvs());
                    totals.setDfaConvs(totals.getDfaConvs() + c.getDfaConvs());
                    rowCount++;
                }
            }
            rowCount++;
            totalRow.createCell(1).setCellValue(totals.getAppNexusImps());
            totalRow.createCell(2).setCellValue(totals.getDfaImps());
            if (totals.getDfaImps() == 0) totalRow.createCell(3).setCellValue(0);
            else totalRow.createCell(3).setCellValue((float)(totals.getDfaImps() - totals.getAppNexusImps())
                    / totals.getDfaImps());
            totalRow.createCell(4).setCellValue(totals.getAppNexusClicks());
            totalRow.createCell(5).setCellValue(totals.getDfaClicks());
            if (totals.getDfaClicks() == 0) totalRow.createCell(6).setCellValue(0);
            else totalRow.createCell(6).setCellValue((float)(totals.getDfaClicks() - totals.getAppNexusClicks())
                    / totals.getDfaClicks());
            totalRow.createCell(7).setCellValue(totals.getAppNexusConvs());
            totalRow.createCell(8).setCellValue(totals.getDfaConvs());
            if (totals.getDfaConvs() == 0) totalRow.createCell(9).setCellValue(0);
            else totalRow.createCell(9).setCellValue((float)(totals.getDfaConvs() - totals.getAppNexusConvs())
                    / totals.getDfaConvs());

            for (Cell c : totalRow) c.setCellStyle(red);
            totalRow.getCell(3).setCellStyle(redPercentage);
            totalRow.getCell(6).setCellStyle(redPercentage);
            totalRow.getCell(9).setCellStyle(redPercentage);

            for (Cell c : header) sheet.autoSizeColumn(c.getColumnIndex());
        }

        return wb;
    }
}
