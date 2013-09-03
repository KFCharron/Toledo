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
            headerRow.createCell(5).setCellValue("Insertion Fees");

            //Setting ID columns to the width of 10 chars
            sheet.setColumnWidth(0, 3072);
            sheet.setColumnWidth(1, 2560);
            sheet.setColumnWidth(2, 3840);


            //Style header
            Font font = wb.createFont();
            font.setFontHeightInPoints((short)14);
            //font.setColor(HSSFColor.RED.index);
            font.setBoldweight((short)700);
            CellStyle bold = wb.createCellStyle();
            bold.setFont(font);
            for(int x = 0; x < 6; x++)
                headerRow.getCell(x).setCellStyle(bold);


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

                oneLine = new StringBuffer();
                if(campaign.getServingFeeList() != null) {
                    for(ServingFee fee : campaign.getServingFeeList()) {
                        oneLine.append(fee.getBrokerName() + " ");
                        oneLine.append(fee.getPaymentType() + " ");
                        oneLine.append("$" + fee.getValue() + " ");
                        oneLine.append("for " + fee.getDescription() + "\n");
                    }
                }
                campaignRow.createCell(5).setCellValue(oneLine.toString());



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
                    for (int x = 0; x < 6; x++)
                        campaignRow.getCell(x).setCellStyle(altRow);
                }
                else {
                    for (int x = 0; x < 6; x++)
                        campaignRow.getCell(x).setCellStyle(whiteRow);
                }

                rowCounter++;
            }

            //auto-size columns
            sheet.autoSizeColumn(3);
            sheet.autoSizeColumn(4);
            sheet.autoSizeColumn(5);

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
