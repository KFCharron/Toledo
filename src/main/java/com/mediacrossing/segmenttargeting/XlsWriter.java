package com.mediacrossing.segmenttargeting;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class XlsWriter {
    public void writeSegmentFileInXls (ArrayList<Campaign> campaignArrayList) {
        try {
            Workbook wb = new HSSFWorkbook();
            Sheet sheet = wb.createSheet("Sheet1");

            CreationHelper createHelper = wb.getCreationHelper();

            //Header row
            Row headerRow = sheet.createRow((short) 0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Campaign");
            headerRow.createCell(2).setCellValue("Segments");

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

            //Repeat row for every campaign in list
//            short rowCount = 1;
//            for (Campaign campaign: campaignArrayList) {
//                Row campaignRow = sheet.createRow(rowCount);
//                campaignRow.createCell(0).setCellValue(campaign.getId());
//                campaignRow.createCell(1).setCellValue(campaign.getName());
//                List<SegmentGroupTarget> currentGroupArray = campaign.getSegmentGroupTargetList();
//                short cellCount = 1;
//                for (SegmentGroupTarget group: currentGroupArray) {
//                    ArrayList<Segment> currentSegmentArray = group.getSegmentArrayList();
//                    for(Segment segment: currentSegmentArray) {
////                        campaignRow.createCell(cellCount++)
////                                .setCellValue("[(" + segment.getAction() + ")" + segment.getName() + "]");
//
//                        System.out.println(cellCount);
//                        if (segment.getAction().equals("exclude")) {
//                            campaignRow.createCell(++cellCount).setCellValue("(" + segment.getAction() + ")");
//                        }
//
//                        else {
//                            campaignRow.createCell(++cellCount).setCellValue("");
//                        }
//                        //add left, top, bottom bold border
//                        Font actionFont = wb.createFont();
//                        font.setFontHeightInPoints((short)14);
//                        font.setColor(HSSFColor.RED.index);
//                        CellStyle topBottomLeft = wb.createCellStyle();
//                        topBottomLeft.setFont(font);
//                        topBottomLeft.setBorderLeft(CellStyle.BORDER_MEDIUM);
//                        topBottomLeft.setBorderRight(CellStyle.BORDER_NONE);
//                        topBottomLeft.setBorderTop(CellStyle.BORDER_MEDIUM);
//                        topBottomLeft.setBorderBottom(CellStyle.BORDER_MEDIUM);
//
//                        campaignRow.getCell(cellCount).setCellStyle(topBottomLeft);
//
//                        campaignRow.createCell(++cellCount).setCellValue(segment.getName());
//                        //add top, bottom, right bold border
//                        Font largerFont = wb.createFont();
//                        largerFont.setFontHeightInPoints((short)14);
//                        CellStyle topBottomRight = wb.createCellStyle();
//                        topBottomRight.setFont(largerFont);
//                        topBottomRight.setBorderLeft(CellStyle.BORDER_NONE);
//                        topBottomRight.setBorderRight(CellStyle.BORDER_MEDIUM);
//                        topBottomRight.setBorderTop(CellStyle.BORDER_MEDIUM);
//                        topBottomRight.setBorderBottom(CellStyle.BORDER_MEDIUM);
//                        campaignRow.getCell(cellCount).setCellStyle(topBottomRight);
//
//                        campaignRow.createCell(++cellCount).setCellValue(segment.getBoolOp());
//                        //no borders
//                        CellStyle noBorders = wb.createCellStyle();
//                        noBorders.setFont(largerFont);
//                        noBorders.setBorderLeft(CellStyle.BORDER_NONE);
//                        noBorders.setBorderRight(CellStyle.BORDER_NONE);
//                        noBorders.setBorderTop(CellStyle.BORDER_NONE);
//                        noBorders.setBorderBottom(CellStyle.BORDER_NONE);
//                        campaignRow.getCell(cellCount).setCellStyle(noBorders);
//
//                    }
//                    //add condition to only display when array size > 1
//                    campaignRow.createCell(++cellCount).setCellValue("-" + group.getBoolOp() + "-");
//                }
//                rowCount++;
//            }

            //Repeat row for every campaign in list
            short rowCounter = 1;
            for (Campaign campaign : campaignArrayList) {
                Row campaignRow = sheet.createRow(rowCounter);
                short linebreakCount = 1;
                campaignRow.createCell(0).setCellValue(campaign.getId());
                campaignRow.createCell(1).setCellValue(campaign.getName());

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
                campaignRow.createCell(2).setCellValue(oneLine.toString());

                CellStyle altRow = wb.createCellStyle();
                altRow.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
                altRow.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                altRow.setWrapText(true);

                CellStyle whiteRow = wb.createCellStyle();
                whiteRow.setWrapText(true);

                campaignRow.setHeightInPoints(linebreakCount * sheet.getDefaultRowHeightInPoints());

                if((rowCounter % 2) == 1) {
                    campaignRow.getCell(0).setCellStyle(altRow);
                    campaignRow.getCell(1).setCellStyle(altRow);
                    campaignRow.getCell(2).setCellStyle(altRow);
                }
                else {
                    campaignRow.getCell(0).setCellStyle(whiteRow);
                    campaignRow.getCell(1).setCellStyle(whiteRow);
                    campaignRow.getCell(2).setCellStyle(whiteRow);
                }

                rowCounter++;
            }

            //Merging regions
//            sheet.addMergedRegion(new CellRangeAddress(
//                    1, //first row (0-based)
//                    1, //last row  (0-based)
//                    2, //first column (0-based)
//                    3  //last column  (0-based)
//            ));


            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);
            //add for loop that auto sizes all columns
//            for(int x = 0; x < 20; x++) {
//                sheet.autoSizeColumn(x);
//            }

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
