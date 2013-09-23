package com.mediacrossing.campaignbooks;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelWriter {

    private static Workbook WORKBOOK = new HSSFWorkbook();

    public void writeLineItemSheetToWorkbook(LineItem lineItem) {

        //Create new sheet
        String sheetName = WorkbookUtil.createSafeSheetName(lineItem.getLineItemName());
        Sheet lineItemSheet = WORKBOOK.createSheet(sheetName);

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
        lineItemRow.createCell(2).setCellValue(lineItem.getLifetimeBudget());
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
        campaignHeaderRow.createCell(1).setCellValue("Campaign Name");
        campaignHeaderRow.createCell(2).setCellValue("Lifetime Budget");
        campaignHeaderRow.createCell(3).setCellValue("Start Date");
        campaignHeaderRow.createCell(4).setCellValue("End Date");
        campaignHeaderRow.createCell(5).setCellValue("# of Days");
        campaignHeaderRow.createCell(6).setCellValue("Daily Budget");
        campaignHeaderRow.createCell(7).setCellValue("Actual Daily Budget");
        campaignHeaderRow.createCell(8).setCellValue("Total Delivery");

        //style header
        font.setFontHeightInPoints((short) 12);
        bold.setFont(font);
//        for(int x = 0; x < 7; x++)
//            lineItemHeader.getCell(x).setCellStyle(bold);
        //Try this, if fails, use commented method
        for(Cell cell : campaignHeaderRow) {
            cell.setCellStyle(bold);
        }

        //repeat for each campaign
        int rowCount = 4;
        for (Campaign campaign : lineItem.getCampaignList()) {
            Row campaignRow = lineItemSheet.createRow(rowCount);
            campaignRow.createCell(0).setCellValue(campaign.getCampaignID());
            campaignRow.createCell(1).setCellValue(campaign.getCampaignName());
            campaignRow.createCell(2).setCellValue(campaign.getLifetimeBudget());
            campaignRow.createCell(3).setCellValue(campaign.getStartDate());
            campaignRow.createCell(4).setCellValue(campaign.getEndDate());
            campaignRow.createCell(5).setCellValue(campaign.getDaysActive());
            campaignRow.createCell(6).setCellValue(campaign.getDailyBudget());
            campaignRow.createCell(7).setCellValue(campaign.getActualDailyBudget());
            campaignRow.createCell(8).setCellValue(campaign.getTotalDelivery());
            int cellCount = 9;
            //list daily deliveries
            for(Delivery dailyDelivery : campaign.getDeliveries()) {
                campaignRow.createCell(cellCount).setCellValue(dailyDelivery.getDelivery());
                if(dailyDelivery.getDate() != null) {
                    campaignHeaderRow.createCell(cellCount).setCellValue(dailyDelivery.getDate());
                }
                cellCount++;
            }
            rowCount++;

            for (Cell cell : campaignRow) {
                lineItemSheet.autoSizeColumn(cell.getColumnIndex());
            }
        }
        rowCount++;
        Row totalRow = lineItemSheet.createRow(rowCount);
        totalRow.createCell(2).setCellFormula("=SUM(C4:C" + (rowCount-1) + ")");
        totalRow.createCell(6).setCellFormula("=SUM(G4:G" + (rowCount-1) + ")");
        totalRow.createCell(7).setCellFormula("=SUM(H4:H" + (rowCount-1) + ")");
        totalRow.createCell(8).setCellFormula("=SUM(I4:I" + (rowCount-1) + ")");
        totalRow.createCell(9).setCellFormula("=SUM(J4:J" + (rowCount-1) + ")");

    }

    public void writeWorkbookToFileWithOutputPath(String outputPath) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(new File(outputPath, "CampaignBooks.xls"));
        WORKBOOK.write(fileOut);
        fileOut.close();
    }

}
