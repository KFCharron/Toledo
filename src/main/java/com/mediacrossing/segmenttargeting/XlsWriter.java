package com.mediacrossing.segmenttargeting;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;

public class XlsWriter {

    private static final Logger LOG = LoggerFactory.getLogger(HTTPConnection.class);

    private static Workbook WORKBOOK;
    private static ArrayList<Campaign> CAMPAIGNARRAYLIST;
    private static String OUTPUTPATH;
    private static ArrayList<SegmentRow> SEGMENTROWLIST;

    public static void setSEGMENTROWLIST(ArrayList<SegmentRow> SEGMENTROWLIST) {
        XlsWriter.SEGMENTROWLIST = SEGMENTROWLIST;
    }

    public static void setCAMPAIGNARRAYLIST(ArrayList<Campaign> CAMPAIGNARRAYLIST) {
        XlsWriter.CAMPAIGNARRAYLIST = CAMPAIGNARRAYLIST;
    }

    public static void setOUTPUTPATH(String OUTPUTPATH) {
        XlsWriter.OUTPUTPATH = OUTPUTPATH;
    }

    public void writeWorkbookToFileWithName(String workbookName) throws IOException {
        //writes file
        FileOutputStream fileOut = new FileOutputStream(new File(OUTPUTPATH, workbookName));
        WORKBOOK.write(fileOut);
        fileOut.close();
        LOG.info(workbookName + " written to " + OUTPUTPATH);
    }

    public void writeAllReports(ArrayList<Campaign> campaignArrayList, String outputPath) throws IOException {

        setCAMPAIGNARRAYLIST(campaignArrayList);
        setOUTPUTPATH(outputPath);
        WORKBOOK = new HSSFWorkbook();
        buildSegmentSheet();
        buildFrequencySheet();
        buildDaypartSheet();
        buildGeographySheet();
        writeWorkbookToFileWithName("TargetSegmentReports.xls");

    }

    public void buildGeographySheet() {

        //Create new sheet
        Sheet segmentSheet = WORKBOOK.createSheet("Geography");

        //Header row
        Row headerRow = segmentSheet.createRow((short) 0);
        headerRow.createCell(0).setCellValue("Advertiser");
        headerRow.createCell(1).setCellValue("Line Item");
        headerRow.createCell(2).setCellValue("Campaign ID");
        headerRow.createCell(3).setCellValue("Campaign Name");
        headerRow.createCell(4).setCellValue("Action");
        headerRow.createCell(5).setCellValue("Countries");
        headerRow.createCell(6).setCellValue("Zips");
        headerRow.createCell(7).setCellValue("DMA Action");
        headerRow.createCell(8).setCellValue("Designated Market Areas");

        //Setting column widths
        segmentSheet.setColumnWidth(0, 3072);
        segmentSheet.setColumnWidth(1, 2560);
        segmentSheet.setColumnWidth(2, 3840);


        //Style header
        Font font = WORKBOOK.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBoldweight((short) 700);
        CellStyle bold = WORKBOOK.createCellStyle();
        bold.setFont(font);
        for(int x = 0; x < 9; x++)
            headerRow.getCell(x).setCellStyle(bold);


        //Repeat row for every campaign in list
        int rowCounter = 1;
        for (Campaign campaign : CAMPAIGNARRAYLIST) {
            Row campaignRow = segmentSheet.createRow(rowCounter);
            campaignRow.createCell(0).setCellValue(campaign.getAdvertiserID());
            campaignRow.createCell(1).setCellValue(campaign.getLineItemID());
            campaignRow.createCell(2).setCellValue(campaign.getId());
            campaignRow.createCell(3).setCellValue(campaign.getName());
            campaignRow.createCell(4).setCellValue(campaign.getGeographyTargets().getCountryAction());
            StringBuilder stringBuilder = new StringBuilder();
            int index = 0;
            for(CountryTarget country : campaign.getGeographyTargets().getCountryTargetList()) {
                if (index > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(country.getName());
                index++;
            }
            campaignRow.createCell(5).setCellValue(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            index = 0;
            for(ZipTarget zip : campaign.getGeographyTargets().getZipTargetList()) {
                if (index > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(zip.getFromZip());
                stringBuilder.append(" - ");
                stringBuilder.append(zip.getToZip());
            }
            campaignRow.createCell(6).setCellValue(stringBuilder.toString());
            campaignRow.createCell(7).setCellValue(campaign.getGeographyTargets().getDmaAction());
            stringBuilder = new StringBuilder();
            index = 0;
            for(DMATarget dma : campaign.getGeographyTargets().getDmaTargetList()) {
                if (index > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(dma.getName());
            }
            campaignRow.createCell(8).setCellValue(stringBuilder.toString());

            //Styles for patterning rows
            CellStyle altRow = WORKBOOK.createCellStyle();
            altRow.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
            altRow.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            altRow.setWrapText(true);
            CellStyle whiteRow = WORKBOOK.createCellStyle();
            whiteRow.setWrapText(true);

            //Pattern rows
            if((rowCounter % 2) == 1) {
                for (int x = 0; x < 9; x++)
                    campaignRow.getCell(x).setCellStyle(altRow);
            }
            else {
                for (int x = 0; x < 9; x++)
                    campaignRow.getCell(x).setCellStyle(whiteRow);
            }

            rowCounter++;

        }
        for (int x = 3; x < 9; x++) {
            segmentSheet.autoSizeColumn(x);
        }
    }

    public void buildDaypartSheet() {

        //Create new sheet
        Sheet segmentSheet = WORKBOOK.createSheet("Daypart");

        //Header row
        Row headerRow = segmentSheet.createRow((short) 0);
        headerRow.createCell(0).setCellValue("Advertiser");
        headerRow.createCell(1).setCellValue("Line Item");
        headerRow.createCell(2).setCellValue("Campaign ID");
        headerRow.createCell(3).setCellValue("Campaign Name");
        headerRow.createCell(4).setCellValue("Days");
        int cellCount = 5;
        for (int x = 0; x < 24; x++){
            headerRow.createCell(cellCount).setCellValue(x);
            segmentSheet.setColumnWidth(x, 1024);
            cellCount++;
        }



        //Style header
        Font font = WORKBOOK.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBoldweight((short) 700);
        CellStyle bold = WORKBOOK.createCellStyle();
        bold.setFont(font);
        for(int x = 0; x < cellCount; x++)
            headerRow.getCell(x).setCellStyle(bold);


        //Repeat row for every campaign in list
        int rowCounter = 1;
        for (Campaign campaign : CAMPAIGNARRAYLIST) {
            for(DaypartTarget daypart : campaign.getDaypartTargetArrayList()) {
                Row campaignRow = segmentSheet.createRow(rowCounter);
                campaignRow.createCell(0).setCellValue(campaign.getAdvertiserID());
                campaignRow.createCell(1).setCellValue(campaign.getLineItemID());
                campaignRow.createCell(2).setCellValue(campaign.getId());
                campaignRow.createCell(3).setCellValue(campaign.getName());
                campaignRow.createCell(4).setCellValue(daypart.getDay().toString());
                for (int x = 0; x < 24; x++) {
                    if(x >= daypart.getStartHour() && x <= daypart.getEndHour()) {
                        campaignRow.createCell(x+5).setCellValue("X");
                    } else {
                        campaignRow.createCell(x+5).setCellValue(" ");
                    }
                    segmentSheet.autoSizeColumn(x+5);
                }
                //Styles for patterning rows
                CellStyle altRow = WORKBOOK.createCellStyle();
                altRow.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
                altRow.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                altRow.setWrapText(true);
                CellStyle whiteRow = WORKBOOK.createCellStyle();
                whiteRow.setWrapText(true);

                //Pattern rows
                if((rowCounter % 2) == 1) {
                    for (int x = 0; x < 29; x++)
                        campaignRow.getCell(x).setCellStyle(altRow);
                }
                else {
                    for (int x = 0; x < 29; x++)
                        campaignRow.getCell(x).setCellStyle(whiteRow);
                }




                rowCounter++;
            }


        }
        //Setting column widths
        segmentSheet.setColumnWidth(0, 3072);
        segmentSheet.setColumnWidth(1, 2560);
        segmentSheet.setColumnWidth(2, 3840);
        segmentSheet.autoSizeColumn(3);
        segmentSheet.autoSizeColumn(4);

    }

    public void buildSegmentSheet() {

            //Create new sheet
            Sheet segmentSheet = WORKBOOK.createSheet("Target Segment");

            //Header row
            Row headerRow = segmentSheet.createRow((short) 0);
            headerRow.createCell(0).setCellValue("Advertiser");
            headerRow.createCell(1).setCellValue("Line Item");
            headerRow.createCell(2).setCellValue("Campaign ID");
            headerRow.createCell(3).setCellValue("Campaign");
            headerRow.createCell(4).setCellValue("Segments");
            headerRow.createCell(5).setCellValue("Insertion Fees");

            //Setting ID columns to the width of 10 chars
            segmentSheet.setColumnWidth(0, 3072);
            segmentSheet.setColumnWidth(1, 2560);
            segmentSheet.setColumnWidth(2, 3840);


            //Style header
            Font font = WORKBOOK.createFont();
            font.setFontHeightInPoints((short) 14);
            font.setBoldweight((short) 700);
            CellStyle bold = WORKBOOK.createCellStyle();
            bold.setFont(font);
            for(int x = 0; x < 6; x++)
                headerRow.getCell(x).setCellStyle(bold);


            //Repeat row for every campaign in list
            short rowCounter = 1;
            for (Campaign campaign : CAMPAIGNARRAYLIST) {
                Row campaignRow = segmentSheet.createRow(rowCounter);
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
                CellStyle altRow = WORKBOOK.createCellStyle();
                altRow.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
                altRow.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                altRow.setWrapText(true);
                CellStyle whiteRow = WORKBOOK.createCellStyle();
                whiteRow.setWrapText(true);

                campaignRow.setHeightInPoints(linebreakCount * segmentSheet.getDefaultRowHeightInPoints());

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
            segmentSheet.autoSizeColumn(3);
            segmentSheet.autoSizeColumn(4);
            segmentSheet.autoSizeColumn(5);



    }

    public void buildFrequencySheet() {

        //Create new sheet
        Sheet segmentSheet = WORKBOOK.createSheet("Frequency");

        //Header row
        Row headerRow = segmentSheet.createRow((short) 0);
        headerRow.createCell(0).setCellValue("Advertiser");
        headerRow.createCell(1).setCellValue("Line Item");
        headerRow.createCell(2).setCellValue("Campaign ID");
        headerRow.createCell(3).setCellValue("Campaign Name");
        headerRow.createCell(4).setCellValue("MaxImps/Person");
        headerRow.createCell(5).setCellValue("MaxImps/Person/Day");
        headerRow.createCell(6).setCellValue("MinMinutesBetweenImps");

        //Setting ID columns to the width of 10 chars
        segmentSheet.setColumnWidth(0, 3072);
        segmentSheet.setColumnWidth(1, 2560);
        segmentSheet.setColumnWidth(2, 3840);


        //Style header
        Font font = WORKBOOK.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBoldweight((short) 700);
        CellStyle bold = WORKBOOK.createCellStyle();
        bold.setFont(font);
        for(int x = 0; x < 7; x++)
            headerRow.getCell(x).setCellStyle(bold);


        //Repeat row for every campaign in list
        short rowCounter = 1;
        for (Campaign campaign : CAMPAIGNARRAYLIST) {
            Row campaignRow = segmentSheet.createRow(rowCounter);
            campaignRow.createCell(0).setCellValue(campaign.getAdvertiserID());
            campaignRow.createCell(1).setCellValue(campaign.getLineItemID());
            campaignRow.createCell(2).setCellValue(campaign.getId());
            campaignRow.createCell(3).setCellValue(campaign.getName());
            campaignRow.createCell(4).setCellValue(campaign.getFrequencyTargets().getMaxLifetimeImps());
            campaignRow.createCell(5).setCellValue(campaign.getFrequencyTargets().getMaxDayImps());
            campaignRow.createCell(6).setCellValue(campaign.getFrequencyTargets().getMinMinutesPerImp());

            //Styles for patterning rows
            CellStyle altRow = WORKBOOK.createCellStyle();
            altRow.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
            altRow.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            altRow.setWrapText(true);
            CellStyle whiteRow = WORKBOOK.createCellStyle();
            whiteRow.setWrapText(true);

            //Pattern rows
            if((rowCounter % 2) == 1) {
                for (int x = 0; x < 7; x++)
                    campaignRow.getCell(x).setCellStyle(altRow);
            }
            else {
                for (int x = 0; x < 7; x++)
                    campaignRow.getCell(x).setCellStyle(whiteRow);
            }

            rowCounter++;
        }

        //auto-size columns
        segmentSheet.autoSizeColumn(3);
        segmentSheet.autoSizeColumn(4);
        segmentSheet.autoSizeColumn(5);
        segmentSheet.autoSizeColumn(6);

    }

    public void buildSegmentLoadSheet() {

        //Create new sheet
        Sheet segmentSheet = WORKBOOK.createSheet("SegmentLoadReport");

        //Header row
        Row headerRow = segmentSheet.createRow((short) 0);
        headerRow.createCell(0).setCellValue("Segment ID");
        headerRow.createCell(1).setCellValue("Segment Name");
        headerRow.createCell(2).setCellValue("Total Loads");
        headerRow.createCell(3).setCellValue("Daily Uniques(Yesterday)");
        headerRow.createCell(4).setCellValue("Campaigns - Impressions(Yesterday)");


        //Style header
        Font font = WORKBOOK.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBoldweight((short) 700);
        CellStyle bold = WORKBOOK.createCellStyle();
        bold.setFont(font);
        for(int x = 0; x < 5; x++)
            headerRow.getCell(x).setCellStyle(bold);

        //Repeat row for every segment row
        short rowCounter = 1;
        for (SegmentRow segmentData : SEGMENTROWLIST) {
            Row segmentSheetRow = segmentSheet.createRow(rowCounter);
            segmentSheetRow.createCell(0).setCellValue(segmentData.getSegmentId());
            segmentSheetRow.createCell(1).setCellValue(segmentData.getName());
            segmentSheetRow.createCell(2).setCellValue(segmentData.getTotalLoads());
            segmentSheetRow.createCell(3).setCellValue(segmentData.getDailyUniques());
            StringBuilder stringBuilder = new StringBuilder();
            for(Campaign campaign : segmentData.getCampaigns()) {
                stringBuilder.append(campaign.getId());
                stringBuilder.append(" : ");
                stringBuilder.append(campaign.getDailyImps());
                stringBuilder.append("\n");
            }
            segmentSheetRow.createCell(4).setCellValue(stringBuilder.toString());


            rowCounter++;

        }

        //auto-size columns
        for(int x = 0; x < 5; x++) {
            segmentSheet.autoSizeColumn(x);
        }
    }

    public void writeSegmentFile(ArrayList<Campaign> campaignArrayList, String outputPath) throws IOException {
        setCAMPAIGNARRAYLIST(campaignArrayList);
        setOUTPUTPATH(outputPath);
        WORKBOOK = new HSSFWorkbook();
        buildSegmentSheet();
        writeWorkbookToFileWithName("SegmentReport.xls");
    }

    public void writeDaypartFile(ArrayList<Campaign> campaignArrayList, String outputPath) throws IOException {
        setCAMPAIGNARRAYLIST(campaignArrayList);
        setOUTPUTPATH(outputPath);
        WORKBOOK = new HSSFWorkbook();
        buildDaypartSheet();
        writeWorkbookToFileWithName("DaypartReport.xls");
    }

    public void writeGeographyFile(ArrayList<Campaign> campaignArrayList, String outputPath) throws IOException {
        setCAMPAIGNARRAYLIST(campaignArrayList);
        setOUTPUTPATH(outputPath);
        WORKBOOK = new HSSFWorkbook();
        buildGeographySheet();
        writeWorkbookToFileWithName("GeographyReport.xls");
    }

    public void writeFrequencyFile(ArrayList<Campaign> campaignArrayList, String outputPath) throws IOException {
        setCAMPAIGNARRAYLIST(campaignArrayList);
        setOUTPUTPATH(outputPath);
        WORKBOOK = new HSSFWorkbook();
        buildFrequencySheet();
        writeWorkbookToFileWithName("FrequencyReport.xls");
    }

    public void writeSegmentLoadFile(ArrayList<SegmentRow> segmentRowArrayList, String outputPath) throws IOException {
        setSEGMENTROWLIST(segmentRowArrayList);
        setOUTPUTPATH(outputPath);
        WORKBOOK = new HSSFWorkbook();
        buildSegmentLoadSheet();
        writeWorkbookToFileWithName("SegmentLoadReport.xls");
    }

}
