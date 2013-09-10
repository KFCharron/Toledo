package com.mediacrossing.campaignbooks;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;

public class ExcelWriter {

    private static Workbook WORKBOOK;

    public void writeLineItemSheetToWorkbook(LineItem lineItem) {

        //Create new sheet
        Sheet lineItemSheet = WORKBOOK.createSheet(lineItem.getLineItemName());

        //Add line item header row
        Row lineItemHeader = lineItemSheet.createRow(0);
        lineItemHeader.createCell(0);
        lineItemHeader.createCell(1).setCellValue("Line Item");
        lineItemHeader.createCell(2).setCellValue("Lifetime Budget");
        lineItemHeader.createCell(3).setCellValue("Start Date");
        lineItemHeader.createCell(4).setCellValue("End Date");
        lineItemHeader.createCell(5).setCellValue("# of Days");
        lineItemHeader.createCell(6).setCellValue("Daily Budget");

        //style header
        Font font = WORKBOOK.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBoldweight((short) 700);
        CellStyle bold = WORKBOOK.createCellStyle();
        bold.setFont(font);
//        for(int x = 0; x < 7; x++)
//            lineItemHeader.getCell(x).setCellStyle(bold);
        //Try this, if fails, use commented method
        for(Cell cell : lineItemHeader) {
            cell.setCellStyle(bold);
        }

        //add line item data
        Row lineItemRow = lineItemSheet.createRow(1);
        lineItemRow.createCell(0);
        lineItemRow.createCell(1).setCellValue(lineItem.getLineItemName());
        lineItemRow.createCell(2).setCellValue(lineItem.getOverallBudget());
        lineItemRow.createCell(3).setCellValue(lineItem.getStartDate());
        lineItemRow.createCell(4).setCellValue(lineItem.getEndDate());
        lineItemRow.createCell(5).setCellValue(lineItem.getDaysActive());
        lineItemRow.createCell(6).setCellValue(lineItem.getDailyBudget());
        Row secondLineItemRow = lineItemSheet.createRow(2);
        //If fails, add blank cells before it.
        secondLineItemRow.createCell(5).setCellValue(lineItem.getDaysRemaining());

        //add campaign header row
        Row campaignHeaderRow = lineItemSheet.createRow(3);
        campaignHeaderRow.createCell(0).setCellValue("Campaign ID");

        //style header

        //repeat for each campaign

        //pattern the rows

        //size the columns appropriately

    }

    public void writeWorkbookToFile() {

    }

}
