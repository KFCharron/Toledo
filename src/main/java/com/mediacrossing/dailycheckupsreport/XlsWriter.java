package com.mediacrossing.dailycheckupsreport;
import com.mediacrossing.publisherreporting.Placement;
import com.mediacrossing.publisherreporting.Publisher;
import com.mediacrossing.publisherreporting.TrendingData;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;

public class XlsWriter {

    private static final Logger LOG = LoggerFactory.getLogger(XlsWriter.class);

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

    public static void writeAllReports(ArrayList<Campaign> campaignArrayList, String outputPath) throws IOException {

        setCAMPAIGNARRAYLIST(campaignArrayList);
        setOUTPUTPATH(outputPath);
        WORKBOOK = new HSSFWorkbook();
        buildSummarySheet();
        buildSegmentSheet();
        buildFrequencySheet();
        buildDaypartSheet();
        buildGeographySheet();
        buildServingFeeSheet();
        //freeze top row for every sheet
        for (int x = 0; x < 5; x++) {
            WORKBOOK.getSheetAt(x).createFreezePane(0,1);
        }
        LocalDate today = new LocalDate(DateTimeZone.UTC);
        writeWorkbookToFileWithName("DailyCheckUps_"+today.toString()+".xls");

    }

    public static void buildSummarySheet() {
        Sheet s = WORKBOOK.createSheet("Campaigns Changed Yesterday");
        Row h = s.createRow(0);
        h.createCell(0).setCellValue("Campaigns Adjusted Yesterday");
        int count = 2;
        for (Campaign c : CAMPAIGNARRAYLIST) {
            if (c.getProfile().modifiedYesterday()) {
                s.createRow(count).createCell(0).setCellValue(c.getName() + " (" + c.getId() + ")");
                count++;
            }
        }
        s.autoSizeColumn(0);
    }

    public static void buildGeographySheet() {

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
        headerRow.createCell(7).setCellValue("Country");
        headerRow.createCell(8).setCellValue("Zip Targets");

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

        //Styles for patterning rows
        Font alert = WORKBOOK.createFont();
        alert.setBoldweight((short)1000);
        alert.setColor(IndexedColors.RED.getIndex());
        Font normal = WORKBOOK.createFont();
        normal.setColor(IndexedColors.BLACK.getIndex());

        CellStyle altRow = WORKBOOK.createCellStyle();
        altRow.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
        altRow.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        altRow.setWrapText(true);
        CellStyle whiteRow = WORKBOOK.createCellStyle();
        whiteRow.setWrapText(true);

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
            int cellNumber = 8;
            for(ZipTarget zip : camp.getGeographyTargets().getZipTargetList()) {
                stringBuilder.append(zip.getFromZip());
                stringBuilder.append("-");
                stringBuilder.append(zip.getToZip());
                stringBuilder.append("     ");
                if(index % 5 == 0) {
                    stringBuilder.append("\n");
                }
                index++;
                if (stringBuilder.toString().length() > 32000) {
                    campaignRow.createCell(cellNumber).setCellValue(stringBuilder.toString());
                    stringBuilder = new StringBuilder();
                    cellNumber++;
                }
            }

            campaignRow.createCell(cellNumber).setCellValue(stringBuilder.toString());

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
            campaignRow.createCell(7).setCellValue(stringBuilder.toString());

            altRow.setFont(normal);
            whiteRow.setFont(normal);

            if(camp.getProfile().modifiedYesterday()) {
                altRow.setFont(alert);
                whiteRow.setFont(alert);
                campaignRow.getCell(3).setCellValue(camp.getName() + " (Modified Yesterday)");
            }

            //Pattern rows
            if((rowCounter % 2) == 1) {
                for(Cell c: campaignRow) c.setCellStyle(whiteRow);
            }
            else {
                for(Cell c: campaignRow) c.setCellStyle(altRow);
            }

            rowCounter++;

        }
        for (int x = 3; x < 20; x++) {
            segmentSheet.autoSizeColumn(x);
        }
    }

    public static void buildDaypartSheet() {

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

        //Styles for patterning rows
        Font alert = WORKBOOK.createFont();
        alert.setBoldweight((short)1000);

        CellStyle altRow = WORKBOOK.createCellStyle();
        altRow.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
        altRow.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        altRow.setWrapText(true);
        CellStyle whiteRow = WORKBOOK.createCellStyle();
        whiteRow.setWrapText(true);

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

                if(campaign.getProfile().modifiedYesterday()) {
                    altRow.setFont(alert);
                    whiteRow.setFont(alert);
                    campaignRow.getCell(3).setCellValue(campaign.getName() + " (Modified Yesterday)");
                }

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

    public static void buildSegmentSheet() {

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

            //Styles for patterning rows
            Font alert = WORKBOOK.createFont();
            alert.setBoldweight((short)1000);
            CellStyle altRow = WORKBOOK.createCellStyle();
            altRow.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
            altRow.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            altRow.setWrapText(true);
            CellStyle whiteRow = WORKBOOK.createCellStyle();
            whiteRow.setWrapText(true);

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



                campaignRow.setHeightInPoints(linebreakCount * segmentSheet.getDefaultRowHeightInPoints());

                if(campaign.getProfile().modifiedYesterday()) {
                    altRow.setFont(alert);
                    whiteRow.setFont(alert);
                    campaignRow.getCell(3).setCellValue(campaign.getName() + " (Modified Yesterday)");
                }

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

    public static void buildServingFeeSheet() {

        //Create new sheet
        Sheet servingSheet = WORKBOOK.createSheet("Serving Fees");

        //Header row
        Row headerRow = servingSheet.createRow((short) 0);
        headerRow.createCell(0).setCellValue("Advertiser");
        headerRow.createCell(1).setCellValue("Line Item");
        headerRow.createCell(2).setCellValue("Campaign ID");
        headerRow.createCell(3).setCellValue("Campaign");
        headerRow.createCell(4).setCellValue("Serving Fees");

        //Setting ID columns to the width of 10 chars
        servingSheet.setColumnWidth(0, 3072);
        servingSheet.setColumnWidth(1, 2560);
        servingSheet.setColumnWidth(2, 3840);

        //Style header
        Font font = WORKBOOK.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBoldweight((short) 700);
        CellStyle bold = WORKBOOK.createCellStyle();
        bold.setFont(font);
        for(Cell c : headerRow)
            c.setCellStyle(bold);

        //Styles for patterning rows
        Font alert = WORKBOOK.createFont();
        alert.setBoldweight((short)1000);
        CellStyle altRow = WORKBOOK.createCellStyle();
        altRow.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
        altRow.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        altRow.setWrapText(true);
        CellStyle whiteRow = WORKBOOK.createCellStyle();
        whiteRow.setWrapText(true);

        //Repeat row for every campaign
        short rowCount = 1;
        for (Campaign c : CAMPAIGNARRAYLIST) {
            Row campRow = servingSheet.createRow(rowCount);
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



            if(c.getProfile().modifiedYesterday()) {
                altRow.setFont(alert);
                whiteRow.setFont(alert);
                campRow.getCell(3).setCellValue(c.getName() + " (Modified Yesterday)");
            }

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

        servingSheet.autoSizeColumn(3);
        servingSheet.autoSizeColumn(4);

    }

    public static void buildFrequencySheet() {

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

        //Styles for patterning rows
        Font alert = WORKBOOK.createFont();
        alert.setBoldweight((short)1000);
        CellStyle altRow = WORKBOOK.createCellStyle();
        altRow.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
        altRow.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        altRow.setWrapText(true);
        CellStyle whiteRow = WORKBOOK.createCellStyle();
        whiteRow.setWrapText(true);

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


            if(campaign.getProfile().modifiedYesterday()) {
                altRow.setFont(alert);
                whiteRow.setFont(alert);
                campaignRow.getCell(3).setCellValue(campaign.getName() + " (Modified Yesterday)");
            }

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
        LocalDate today = new LocalDate(DateTimeZone.UTC);
        writeWorkbookToFileWithName("SegmentLoadReport_"+today.toString()+".xls");
    }

    public static void writePublisherReport(ArrayList<Publisher> dayPubList,
                                            ArrayList<Publisher> lifetimePubList,
                                            ArrayList<Placement> dayPlaceList,
                                            ArrayList<Placement> lifetimePlaceList,
                                            String outputPath) throws IOException {

        WORKBOOK = new HSSFWorkbook();
        //Create new sheet
        Sheet publisherSheet = WORKBOOK.createSheet("24hr Publishers");

        //Create cell style for network revenue
        DataFormat df = WORKBOOK.createDataFormat();
        CellStyle fullCurrency = WORKBOOK.createCellStyle();

        fullCurrency.setDataFormat(df.getFormat("$#,##0.00"));
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
        headerRow.createCell(9).setCellValue("eCPM");
        headerRow.createCell(10).setCellValue("PnL");


        //Style header
        Font font = WORKBOOK.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBoldweight((short) 700);
        CellStyle bold = WORKBOOK.createCellStyle();
        bold.setFont(font);
        for(Cell c : headerRow)
            c.setCellStyle(bold);
        headerRow.setHeightInPoints(2 * publisherSheet.getDefaultRowHeightInPoints());


        int rowCounter = 1;
        for(Publisher pub : dayPubList) {
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
            dataRow.createCell(9).setCellValue(pub.getCpm());
            dataRow.getCell(9).setCellStyle(fullCurrency);
            dataRow.createCell(10).setCellValue(pub.getPnl());
            dataRow.getCell(10).setCellStyle(fullCurrency);
            rowCounter++;
        }

        for(int x = 0; x<= 10; x++) {
            publisherSheet.autoSizeColumn(x);
        }


        //build new sheet for lifetime publisher stats
        publisherSheet = WORKBOOK.createSheet("Lifetime Publishers");

        //Header row
        headerRow = publisherSheet.createRow((short) 0);
        headerRow.createCell(0).setCellValue("Publisher ID");
        headerRow.createCell(1).setCellValue("Publisher Name");
        headerRow.createCell(2).setCellValue("Total Imps");
        headerRow.createCell(3).setCellValue("Imps Sold");
        headerRow.createCell(4).setCellValue("Clicks");
        headerRow.createCell(5).setCellValue("RTB Imps");
        headerRow.createCell(6).setCellValue("Imps Kept");
        headerRow.createCell(7).setCellValue("Default Imps");
        headerRow.createCell(8).setCellValue("PSA Imps");
        headerRow.createCell(9).setCellValue("eCPM");
        headerRow.createCell(10).setCellValue("PnL");

        for(Cell c : headerRow)
            c.setCellStyle(bold);
        headerRow.setHeightInPoints(2 * publisherSheet.getDefaultRowHeightInPoints());


        rowCounter = 1;
        for(Publisher pub : lifetimePubList) {
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
            dataRow.createCell(9).setCellValue(pub.getCpm());
            dataRow.getCell(9).setCellStyle(fullCurrency);
            dataRow.createCell(10).setCellValue(pub.getPnl());
            dataRow.getCell(10).setCellStyle(fullCurrency);
            rowCounter++;
        }

        for(int x = 0; x<= 10; x++) {
            publisherSheet.autoSizeColumn(x);
        }



        //day placement sheet
        publisherSheet = WORKBOOK.createSheet("24hr Placements");

        //Header row
        headerRow = publisherSheet.createRow((short) 0);
        headerRow.createCell(0).setCellValue("Placement ID");
        headerRow.createCell(1).setCellValue("Placement Name");
        headerRow.createCell(2).setCellValue("Site ID");
        headerRow.createCell(3).setCellValue("Site Name");
        headerRow.createCell(4).setCellValue("Imps Total");
        headerRow.createCell(5).setCellValue("Imps Sold");
        headerRow.createCell(6).setCellValue("Clicks");
        headerRow.createCell(7).setCellValue("RTB");
        headerRow.createCell(8).setCellValue("Kept");
        headerRow.createCell(9).setCellValue("Default");
        headerRow.createCell(10).setCellValue("PSA");
        headerRow.createCell(11).setCellValue("Network Revenue");
        headerRow.createCell(12).setCellValue("eCPM");

        for(Cell c : headerRow)
            c.setCellStyle(bold);
        headerRow.setHeightInPoints(2 * publisherSheet.getDefaultRowHeightInPoints());


        rowCounter = 1;
        for(Placement p : dayPlaceList) {
            Row dataRow = publisherSheet.createRow(rowCounter);
            dataRow.createCell(0).setCellValue(p.getId());
            dataRow.createCell(1).setCellValue(p.getName());
            dataRow.createCell(2).setCellValue(p.getSiteId());
            dataRow.createCell(3).setCellValue(p.getSiteName());
            dataRow.createCell(4).setCellValue(p.getImpsTotal());
            dataRow.createCell(5).setCellValue(p.getImpsSold());
            dataRow.createCell(6).setCellValue(p.getClicks());
            dataRow.createCell(7).setCellValue(p.getRtbImps() + "(" + p.getRtbPercentage() + "%)");
            dataRow.createCell(8).setCellValue(p.getKeptImps() + "(" + p.getKeptPercentage() + "%)");
            dataRow.createCell(9).setCellValue(p.getDefaultImps() + "(" + p.getDefaultPercentage() + "%)");
            dataRow.createCell(10).setCellValue(p.getPsaImps() + "(" + p.getPsaPercentage() + "%)");
            dataRow.createCell(11).setCellValue(p.getNetworkRevenue());
            dataRow.createCell(12).setCellValue(p.getCpm());
            dataRow.getCell(11).setCellStyle(fullCurrency);
            dataRow.getCell(12).setCellStyle(fullCurrency);

            rowCounter++;
        }

        for(int x = 0; x<= 12; x++) {
            publisherSheet.autoSizeColumn(x);
        }

        //lifetime placement sheet
        publisherSheet = WORKBOOK.createSheet("Lifetime Placements");

        //Header row
        headerRow = publisherSheet.createRow((short) 0);
        headerRow.createCell(0).setCellValue("Placement ID");
        headerRow.createCell(1).setCellValue("Placement Name");
        headerRow.createCell(2).setCellValue("Site ID");
        headerRow.createCell(3).setCellValue("Site Name");
        headerRow.createCell(4).setCellValue("Imps Total");
        headerRow.createCell(5).setCellValue("Imps Sold");
        headerRow.createCell(6).setCellValue("Clicks");
        headerRow.createCell(7).setCellValue("RTB");
        headerRow.createCell(8).setCellValue("Kept");
        headerRow.createCell(9).setCellValue("Default");
        headerRow.createCell(10).setCellValue("PSA");
        headerRow.createCell(11).setCellValue("Network Revenue");
        headerRow.createCell(12).setCellValue("eCPM");

        for(Cell c : headerRow)
            c.setCellStyle(bold);
        headerRow.setHeightInPoints(2 * publisherSheet.getDefaultRowHeightInPoints());


        rowCounter = 1;
        for(Placement p : lifetimePlaceList) {
            Row dataRow = publisherSheet.createRow(rowCounter);
            dataRow.createCell(0).setCellValue(p.getId());
            dataRow.createCell(1).setCellValue(p.getName());
            dataRow.createCell(2).setCellValue(p.getSiteId());
            dataRow.createCell(3).setCellValue(p.getSiteName());
            dataRow.createCell(4).setCellValue(p.getImpsTotal());
            dataRow.createCell(5).setCellValue(p.getImpsSold());
            dataRow.createCell(6).setCellValue(p.getClicks());
            dataRow.createCell(7).setCellValue(p.getRtbImps() + "(" + p.getRtbPercentage() + "%)");
            dataRow.createCell(8).setCellValue(p.getKeptImps() + "(" + p.getKeptPercentage() + "%)");
            dataRow.createCell(9).setCellValue(p.getDefaultImps() + "(" + p.getDefaultPercentage() + "%)");
            dataRow.createCell(10).setCellValue(p.getPsaImps() + "(" + p.getPsaPercentage() + "%)");
            dataRow.createCell(11).setCellValue(p.getNetworkRevenue());
            dataRow.createCell(12).setCellValue(p.getCpm());
            dataRow.getCell(11).setCellStyle(fullCurrency);
            dataRow.getCell(12).setCellStyle(fullCurrency);
            rowCounter++;
        }

        for(int x = 0; x<= 12; x++) {
            publisherSheet.autoSizeColumn(x);
        }

        publisherSheet = WORKBOOK.createSheet("Trends");

        rowCounter = 1;

        for (Publisher p : dayPubList) {
            //Name
            Row nameRow = publisherSheet.createRow(rowCounter);
            nameRow.createCell(1).setCellValue(p.getPublisherName());
            //headers
            rowCounter = rowCounter + 2;
            Row headRow = publisherSheet.createRow(rowCounter);
            headRow.createCell(1).setCellValue("Date");
            headRow.createCell(2).setCellValue("Avails");
            headRow.createCell(3).setCellValue("Defaults");
            headRow.createCell(4).setCellValue("RPM");
            headRow.createCell(5).setCellValue("Revenue");

            for (Cell c : nameRow) c.setCellStyle(bold);
            for (Cell c : headRow) c.setCellStyle(bold);
            nameRow.setHeightInPoints(2 * publisherSheet.getDefaultRowHeightInPoints());
            headRow.setHeightInPoints(2 * publisherSheet.getDefaultRowHeightInPoints());
            //data
            TrendingData totals = new TrendingData();
            for (TrendingData d : p.getTrendList()) {
                Row dRow = publisherSheet.createRow(++rowCounter);
                dRow.createCell(1).setCellValue(d.getDate().getMonthOfYear() + "/" + d.getDate().getDayOfMonth());
                dRow.createCell(2).setCellValue(d.getImps());
                dRow.createCell(3).setCellValue(d.getDefaultPercentage());
                dRow.createCell(4).setCellValue(d.getRpm());
                dRow.createCell(5).setCellValue(d.getRevenue());

                totals.setImps(totals.getImps() + d.getImps());
                totals.setDefaults(totals.getDefaults() + d.getDefaults());
                totals.setRevenue(totals.getRevenue() + d.getRevenue());
                totals.setRpm(totals.getRpm() + d.getRpm());
            }
            rowCounter = rowCounter + 2;
            //averages
            Row averageRow = publisherSheet.createRow(rowCounter);
            averageRow.createCell(1).setCellValue("Averages:");
            averageRow.getCell(1).setCellStyle(bold);
            averageRow.setHeightInPoints(2 * publisherSheet.getDefaultRowHeightInPoints());

            if (p.getTrendList().size() != 0) {
                averageRow.createCell(2).setCellValue(totals.getImps()/p.getTrendList().size());
                averageRow.createCell(3).setCellValue(totals.getDefaultPercentage());
                averageRow.createCell(4).setCellValue(totals.getRpm()/p.getTrendList().size());
                averageRow.createCell(5).setCellValue(totals.getRevenue()/p.getTrendList().size());
            }


            //increment
            rowCounter = rowCounter + 4;
        }

        for(int x = 0; x<= 5; x++) {
            publisherSheet.autoSizeColumn(x);
        }

        OUTPUTPATH = outputPath;
        LocalDate today = new LocalDate(DateTimeZone.UTC);
        writeWorkbookToFileWithName("PublisherReport_"+today.toString()+".xls");


    }

}
