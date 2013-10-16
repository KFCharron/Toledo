package com.mediacrossing.segmenttargeting;
import com.mediacrossing.publisherreporting.Publisher;
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

    public static void setCAMPAIGNARRAYLIST(ArrayList<Campaign> CAMPAIGNARRAYLIST) {
        XlsWriter.CAMPAIGNARRAYLIST = CAMPAIGNARRAYLIST;
    }

    public static void setOUTPUTPATH(String OUTPUTPATH) {
        XlsWriter.OUTPUTPATH = OUTPUTPATH;
    }

    public static void writeWorkbookToFileWithName(String workbookName) throws IOException {
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
        buildInsertionFeeSheet();
        //freeze top row for every sheet
        for (int x = 0; x < 5; x++) {
            WORKBOOK.getSheetAt(x).createFreezePane(0,1);
        }
        writeWorkbookToFileWithName("DailyCheckUps.xls");

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
        headerRow.createCell(4).setCellValue("DMA Action");
        headerRow.createCell(5).setCellValue("Designated Market Areas");
        headerRow.createCell(6).setCellValue("Action");
        headerRow.createCell(7).setCellValue("Zip Targets");
        headerRow.createCell(8).setCellValue("Country");

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
        for (Campaign camp : CAMPAIGNARRAYLIST) {
            Row campaignRow = segmentSheet.createRow(rowCounter);
            campaignRow.createCell(0).setCellValue(camp.getAdvertiserID());
            campaignRow.createCell(1).setCellValue(camp.getLineItemID());
            campaignRow.createCell(2).setCellValue(camp.getId());
            campaignRow.createCell(3).setCellValue(camp.getName());

            StringBuilder stringBuilder = new StringBuilder();
            int index = 1;
            for(DMATarget dma : camp.getGeographyTargets().getDmaTargetList()) {
                stringBuilder.append(dma.getName());
                stringBuilder.append("     ");
                if(index % 5 == 0) {
                    stringBuilder.append("\n");
                }
                index++;
            }
            campaignRow.createCell(5).setCellValue(stringBuilder.toString());

            campaignRow.createCell(4);
            if (camp.getGeographyTargets().getDmaTargetList().size() != 0)
                campaignRow.getCell(4).setCellValue(camp.getGeographyTargets().getDmaAction());

            stringBuilder = new StringBuilder();
            index = 1;
            for(ZipTarget zip : camp.getGeographyTargets().getZipTargetList()) {
                stringBuilder.append(zip.getFromZip());
                stringBuilder.append("-");
                stringBuilder.append(zip.getToZip());
                stringBuilder.append("     ");
                if(index % 5 == 0) {
                    stringBuilder.append("\n");
                }
                index++;
            }
            campaignRow.createCell(7).setCellValue(stringBuilder.toString());

            campaignRow.createCell(6);
            if(camp.getGeographyTargets().getCountryTargetList().size() != 0)
                campaignRow.getCell(6).setCellValue(camp.getGeographyTargets().getCountryAction());


            stringBuilder = new StringBuilder();
            index = 0;
            for(CountryTarget country : camp.getGeographyTargets().getCountryTargetList()) {
                if (index > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(country.getName());
                index++;
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
            for(int x = 0; x < 5; x++)
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
                        oneLine.append("(" + currentSegmentArray.get(y).getId() + ")" + currentSegmentArray.get(y).getName());
                        oneLine.append("]");
                        if((y+1) < currentSegmentArray.size()) {
                            oneLine.append("\n" + currentSegmentArray.get(y).getBoolOp() + "\n");
                        }
                    }
                    oneLine.append("}\n ");

                    if ((x+1) < campaign.getSegmentGroupTargetList().size()) {
                        oneLine.append("\n -" + (campaign.getSegmentGroupTargetList().get(x).getBoolOp().toUpperCase()) + "- \n\n");
                        linebreakCount += 3;
                    }
                }
                campaignRow.createCell(4).setCellValue(oneLine.toString());

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
                    for (int x = 0; x < 5; x++)
                        campaignRow.getCell(x).setCellStyle(altRow);
                }
                else {
                    for (int x = 0; x < 5; x++)
                        campaignRow.getCell(x).setCellStyle(whiteRow);
                }

                rowCounter++;
            }

            //auto-size columns
            segmentSheet.autoSizeColumn(3);
            segmentSheet.autoSizeColumn(4);
    }

    public void buildInsertionFeeSheet() {

        //Create new sheet
        Sheet insertionSheet = WORKBOOK.createSheet("Insertion Fees");

        //Header row
        Row headerRow = insertionSheet.createRow((short) 0);
        headerRow.createCell(0).setCellValue("Advertiser");
        headerRow.createCell(1).setCellValue("Line Item");
        headerRow.createCell(2).setCellValue("Campaign ID");
        headerRow.createCell(3).setCellValue("Campaign");
        headerRow.createCell(4).setCellValue("Insertion Fees");

        //Setting ID columns to the width of 10 chars
        insertionSheet.setColumnWidth(0, 3072);
        insertionSheet.setColumnWidth(1, 2560);
        insertionSheet.setColumnWidth(2, 3840);

        //Style header
        Font font = WORKBOOK.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBoldweight((short) 700);
        CellStyle bold = WORKBOOK.createCellStyle();
        bold.setFont(font);
        for(Cell c : headerRow)
            c.setCellStyle(bold);

        //Repeat row for every campaign
        short rowCount = 1;
        for (Campaign c : CAMPAIGNARRAYLIST) {
            Row campRow = insertionSheet.createRow(rowCount);
            campRow.createCell(0).setCellValue(c.getAdvertiserID());
            campRow.createCell(1).setCellValue(c.getLineItemID());
            campRow.createCell(2).setCellValue(c.getId());
            campRow.createCell(3).setCellValue(c.getName());
            StringBuffer sb = new StringBuffer();
            if (c.getServingFeeList() != null) {
                for (ServingFee fee : c.getServingFeeList()) {
                    sb.append(fee.getBrokerName() + " " );
                    sb.append("$" + fee.getValue() + " ");
                    sb.append(fee.getPaymentType() + " ");
                    sb.append("for " + fee.getDescription() + "\n");
                }
            }
            campRow.createCell(4).setCellValue(sb.toString());

            //Styles for patterning rows
            CellStyle altRow = WORKBOOK.createCellStyle();
            altRow.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
            altRow.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            altRow.setWrapText(true);
            CellStyle whiteRow = WORKBOOK.createCellStyle();
            whiteRow.setWrapText(true);

            if ((rowCount % 2) == 1) {
                for (Cell cc : campRow) {
                    cc.setCellStyle(altRow);
                }
            } else {
                for ( Cell cc : campRow) {
                    cc.setCellStyle(whiteRow);
                }
            }

            rowCount++;
        }

        insertionSheet.autoSizeColumn(3);
        insertionSheet.autoSizeColumn(4);

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
        headerRow.createCell(0).setCellValue("Advertiser ID");
        headerRow.createCell(1).setCellValue("Line Item");
        headerRow.createCell(2).setCellValue("Campaign");
        headerRow.createCell(3).setCellValue("Segment ID");
        headerRow.createCell(4).setCellValue("Campaign Name");
        headerRow.createCell(5).setCellValue("Segment Name");
        headerRow.createCell(6).setCellValue("Total Segment Loads");
        headerRow.createCell(7).setCellValue("Daily Segment Loads");
        headerRow.createCell(8).setCellValue("Daily Campaign Impressions");


        //Style header
        Font font = WORKBOOK.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBoldweight((short) 700);
        CellStyle bold = WORKBOOK.createCellStyle();
        bold.setFont(font);
        for(int x = 0; x < 9; x++)
            headerRow.getCell(x).setCellStyle(bold);

        //Repeat row for every segment row
        short rowCounter = 1;
        for (Campaign campaign : CAMPAIGNARRAYLIST) {
            for(SegmentGroupTarget segmentGroupTarget : campaign.getSegmentGroupTargetList())
            {
                for (Segment segment : segmentGroupTarget.getSegmentArrayList()) {
                    Row segmentRow = segmentSheet.createRow(rowCounter);
                    segmentRow.createCell(0).setCellValue("(" + campaign.getAdvertiserID() + ")"
                            + campaign.getAdvertiserName());
                    segmentRow.createCell(1).setCellValue("(" + campaign.getLineItemID() + ")"
                            + campaign.getLineItemName());
                    segmentRow.createCell(2).setCellValue(campaign.getId());
                    segmentRow.createCell(3).setCellValue(segment.getId());
                    segmentRow.createCell(4).setCellValue(campaign.getName());
                    segmentRow.createCell(5).setCellValue(segment.getName());
                    segmentRow.createCell(6).setCellValue(segment.getTotalSegmentLoads());
                    segmentRow.createCell(7).setCellValue(segment.getDailySegmentLoads());
                    segmentRow.createCell(8).setCellValue(campaign.getDailyImps());

                    rowCounter++;
                }
            }

        }

        //auto-size columns
        for(int x = 0; x < 9; x++) {
            segmentSheet.autoSizeColumn(x);
        }
    }

    public void writeSegmentLoadFile(ArrayList<Campaign> campaignArrayList, String outputPath) throws IOException {
        setCAMPAIGNARRAYLIST(campaignArrayList);
        setOUTPUTPATH(outputPath);
        WORKBOOK = new HSSFWorkbook();
        buildSegmentLoadSheet();
        writeWorkbookToFileWithName("SegmentLoadReport.xls");
    }

    public static void writePublisherReport(ArrayList<Publisher> pubList, String outputPath) throws IOException {

        WORKBOOK = new HSSFWorkbook();
        //Create new sheet
        Sheet publisherSheet = WORKBOOK.createSheet("PublisherReport");

        //Header row
        Row headerRow = publisherSheet.createRow((short) 0);
        headerRow.createCell(0).setCellValue("Publisher ID");
        headerRow.createCell(1).setCellValue("Publisher Name");
        headerRow.createCell(2).setCellValue("Total Imps");
        headerRow.createCell(3).setCellValue("Imps Sold");
        headerRow.createCell(4).setCellValue("Clicks");
        headerRow.createCell(5).setCellValue("RTB Imps");
        headerRow.createCell(6).setCellValue("Imps Kept");
        headerRow.createCell(7).setCellValue("Default Imps");
        headerRow.createCell(8).setCellValue("PSA Imps");


        //Style header
        Font font = WORKBOOK.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBoldweight((short) 700);
        CellStyle bold = WORKBOOK.createCellStyle();
        bold.setFont(font);
        for(Cell c : headerRow)
            c.setCellStyle(bold);

        short rowCounter = 1;
        for(Publisher pub : pubList) {
            Row dataRow = publisherSheet.createRow(rowCounter);
            dataRow.createCell(0).setCellValue(pub.getId());
            dataRow.createCell(1).setCellValue(pub.getPublisherName());
            dataRow.createCell(2).setCellValue(pub.getImpsTotal());
            dataRow.createCell(3).setCellValue(pub.getImpsSold());
            dataRow.createCell(4).setCellValue(pub.getClicks());
            dataRow.createCell(5).setCellValue(pub.getRtbPercentage() + "% (" + pub.getImpsRtb() + ")");
            dataRow.createCell(6).setCellValue(pub.getKeptPercentage() + "% (" + pub.getImpsKept() + ")");
            dataRow.createCell(7).setCellValue(pub.getDefaultPercentage() + "% (" + pub.getImpsDefault() + ")");
            dataRow.createCell(8).setCellValue(pub.getPsaPercentage() + "% (" + pub.getImpsPsa() + ")");
            rowCounter++;
        }

        for(int x = 0; x<= 8; x++) {
            publisherSheet.autoSizeColumn(x);
        }

        OUTPUTPATH = outputPath;
        writeWorkbookToFileWithName("PublisherReport.xls");


    }

}
