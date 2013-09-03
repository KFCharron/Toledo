package com.mediacrossing.segmenttargeting;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class XlsWriter {
    public void writeSegmentFileInXls (ArrayList<Campaign> campaignArrayList) {
        try {
            //Create Workbook
            Workbook wb = new HSSFWorkbook();
            Sheet sheet = wb.createSheet("Sheet1");

            //Header row
            Row headerRow = sheet.createRow((short) 0);
            headerRow.createCell(0).setCellValue("Advertiser");
            headerRow.createCell(1).setCellValue("Line Item");
            headerRow.createCell(2).setCellValue("Campaign ID");
            headerRow.createCell(3).setCellValue("Campaign");
            headerRow.createCell(4).setCellValue("Segments");

            //Style header
            Font font = wb.createFont();
            font.setFontHeightInPoints((short)14);
            //font.setColor(HSSFColor.RED.index);
            font.setBoldweight((short)700);
            CellStyle bold = wb.createCellStyle();
            bold.setFont(font);
            headerRow.getCell(0).setCellStyle(bold);
            headerRow.getCell(1).setCellStyle(bold);
            headerRow.getCell(2).setCellStyle(bold);
            headerRow.getCell(3).setCellStyle(bold);
            headerRow.getCell(4).setCellStyle(bold);

            //Repeat row for every campaign in list
            short rowCounter = 1;
            for (Campaign campaign : campaignArrayList) {
                Row campaignRow = sheet.createRow(rowCounter);
                short linebreakCount = 1;
                campaignRow.createCell(0).setCellValue(campaign.getAdvertiserID());
                campaignRow.createCell(1).setCellValue(campaign.getLineItemID());
                campaignRow.createCell(2).setCellValue(campaign.getId());
                campaignRow.createCell(3).setCellValue(campaign.getName());

                StringBuffer oneLine = new StringBuffer();
                for(int x = 0; x < campaign.getSegmentGroupTargetList().size(); x++) {
                    oneLine.append("{");
                    ArrayList<Segment> currentSegmentArray =
                            campaign.getSegmentGroupTargetList().get(x).getSegmentArrayList();
                    for(int y = 0; y < currentSegmentArray.size(); y++) {
                        oneLine.append("[");
                        if(currentSegmentArray.get(y).getAction().equals("exclude")) {
                            oneLine.append("("+currentSegmentArray.get(y).getAction()+")");
                        }
                        oneLine.append(currentSegmentArray.get(y).getName());
                        oneLine.append("]");
                        if((y+1) < currentSegmentArray.size()) {
                            oneLine.append(" " + currentSegmentArray.get(y).getBoolOp() + " ");
                        }
                    }
                    oneLine.append("}");

                    if ((x+1) < campaign.getSegmentGroupTargetList().size()) {
                        oneLine.append("\n -" + (campaign.getSegmentGroupTargetList().get(x).getBoolOp()) + "- \n");
                        linebreakCount += 2;
                    }
                }
                campaignRow.createCell(4).setCellValue(oneLine.toString());

                //Styles for patterning rows
                CellStyle altRow = wb.createCellStyle();
                altRow.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
                altRow.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                altRow.setWrapText(true);
                CellStyle whiteRow = wb.createCellStyle();
                whiteRow.setWrapText(true);

                campaignRow.setHeightInPoints(linebreakCount * sheet.getDefaultRowHeightInPoints());

                //Pattern rows
                if((rowCounter % 2) == 1) {
                    campaignRow.getCell(0).setCellStyle(altRow);
                    campaignRow.getCell(1).setCellStyle(altRow);
                    campaignRow.getCell(2).setCellStyle(altRow);
                    campaignRow.getCell(3).setCellStyle(altRow);
                    campaignRow.getCell(4).setCellStyle(altRow);
                }
                else {
                    campaignRow.getCell(0).setCellStyle(whiteRow);
                    campaignRow.getCell(1).setCellStyle(whiteRow);
                    campaignRow.getCell(2).setCellStyle(whiteRow);
                    campaignRow.getCell(3).setCellStyle(whiteRow);
                    campaignRow.getCell(4).setCellStyle(whiteRow);
                }

                rowCounter++;
            }

            //auto-size columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            sheet.autoSizeColumn(3);
            sheet.autoSizeColumn(4);

            //writes file
            FileOutputStream fileOut = new FileOutputStream("TargetSegment.xls");
            wb.write(fileOut);
            fileOut.close();
        }
        catch (UnsupportedEncodingException e) {}
        catch (FileNotFoundException e){}
        catch (IOException e){}
    }
}
