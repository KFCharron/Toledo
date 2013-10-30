package com.mediacrossing.publishercheckup;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ReportGenerator implements Serializable {

    public static Workbook writePublisherCheckUpReport(ArrayList<PublisherConfig> pubs) {
        Workbook wb = new HSSFWorkbook();
        wb = buildPlacementSheet(wb, pubs);
        wb = buildPaymentSheet(wb, pubs);
        wb = buildFloorRuleSheet(wb, pubs);

        return wb;
    }

    private static Workbook buildPlacementSheet(Workbook wb, ArrayList<PublisherConfig> pubs) {
        Sheet placementSheet = wb.createSheet("Placements");

        Row titleRow = placementSheet.createRow(0);
        titleRow.createCell(0).setCellValue("Placement Details");

        Row headerRow = placementSheet.createRow(2);
        headerRow.createCell(0).setCellValue("Publisher");
        headerRow.createCell(1).setCellValue("ID");
        headerRow.createCell(2).setCellValue("Placement Name");
        headerRow.createCell(3).setCellValue("Filtered Advertisers");
        headerRow.createCell(4).setCellValue("Content Categories");

        CellStyle boldHeader = wb.createCellStyle();
        Font boldFont = wb.createFont();
        boldFont.setFontHeightInPoints((short)14);
        boldFont.setBoldweight((short)1000);
        boldHeader.setFont(boldFont);

        for (Cell c : headerRow) c.setCellStyle(boldHeader);
        titleRow.getCell(0).setCellStyle(boldHeader);

        int rowCount = 3;
        for (PublisherConfig pub : pubs) {
            for (Placement p : pub.getPlacements()) {

                Font alert = wb.createFont();
                alert.setBoldweight((short)1000);
                CellStyle altGreen = wb.createCellStyle();
                altGreen.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                altGreen.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                altGreen.setWrapText(true);
                CellStyle normal = wb.createCellStyle();
                normal.setWrapText(true);

                Row dataRow = placementSheet.createRow(rowCount);
                dataRow.createCell(0).setCellValue(pub.getName() + "(" + pub.getId() + ")");
                dataRow.createCell(1).setCellValue(p.getId());
                dataRow.createCell(2).setCellValue(p.getName());
                StringBuilder sb = new StringBuilder();
                for (IdName t : p.getFilteredAdvertisers()) {
                    sb.append(t.getName()+"("+t.getId()+")\n");
                }
                dataRow.createCell(3).setCellValue(sb.toString());
                sb = new StringBuilder();
                for (IdName t : p.getContentCategories()) {
                    sb.append(t.getName()+"("+t.getId()+")\n");
                }
                dataRow.createCell(4).setCellValue(sb.toString());

                if(p.modifiedYesterday()) {
                    altGreen.setFont(alert);
                    normal.setFont(alert);
                    dataRow.getCell(2).setCellValue(p.getName() + " (Modified Yesterday)");
                }

                for (Cell c : dataRow) {
                    if (rowCount % 2 == 1) c.setCellStyle(altGreen);
                    else c.setCellStyle(normal);
                }

                rowCount++;
            }
        }
        for (int x = 0; x < 5; x++) placementSheet.autoSizeColumn(x);
        return wb;
    }

    private static Workbook buildPaymentSheet (Workbook wb, ArrayList<PublisherConfig> pubs) {
        Sheet paymentSheet = wb.createSheet("Payment Rules");

        Row titleRow = paymentSheet.createRow(0);
        titleRow.createCell(0).setCellValue("Payment Rules");

        Row headerRow = paymentSheet.createRow(2);
        headerRow.createCell(0).setCellValue("Publisher");
        headerRow.createCell(1).setCellValue("ID");
        headerRow.createCell(2).setCellValue("Payment Rule");
        headerRow.createCell(3).setCellValue("Type");
        headerRow.createCell(4).setCellValue("Rev. Share");
        headerRow.createCell(5).setCellValue("Priority");

        CellStyle boldHeader = wb.createCellStyle();
        Font boldFont = wb.createFont();
        boldFont.setFontHeightInPoints((short)14);
        boldFont.setBoldweight((short)1000);
        boldHeader.setFont(boldFont);

        for (Cell c : headerRow) c.setCellStyle(boldHeader);
        titleRow.getCell(0).setCellStyle(boldHeader);

        DecimalFormat percentage = new DecimalFormat("##%");

        int rowCount = 3;
        for (PublisherConfig pub : pubs) {
            for (PaymentRule p : pub.getPaymentRules()) {

                Font alert = wb.createFont();
                alert.setBoldweight((short)1000);
                CellStyle altGreen = wb.createCellStyle();
                altGreen.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                altGreen.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                altGreen.setWrapText(true);
                CellStyle normal = wb.createCellStyle();
                normal.setWrapText(true);

                Row dataRow = paymentSheet.createRow(rowCount);
                dataRow.createCell(0).setCellValue(pub.getName()+"("+pub.getId()+")");
                dataRow.createCell(1).setCellValue(p.getId());
                dataRow.createCell(2).setCellValue(p.getName());
                dataRow.createCell(3).setCellValue(p.getPricingType());
                dataRow.createCell(4).setCellValue(percentage.format(p.getRevshare()));
                dataRow.createCell(5).setCellValue(p.getPriority());

                if(p.modifiedYesterday()) {
                    altGreen.setFont(alert);
                    normal.setFont(alert);
                    dataRow.getCell(2).setCellValue(p.getName() + " (Modified Yesterday)");
                }

                for (Cell c : dataRow) {
                    if (rowCount % 2 == 1) c.setCellStyle(altGreen);
                    else c.setCellStyle(normal);
                }

                rowCount++;
            }
        }

        for (int x = 0; x < 6; x++) paymentSheet.autoSizeColumn(x);
        return wb;
    }

    private static Workbook buildFloorRuleSheet (Workbook wb, ArrayList<PublisherConfig> pubs) {

        Sheet floorSheet = wb.createSheet("Floor Rules");

        Row titleRow = floorSheet.createRow(0);
        titleRow.createCell(0).setCellValue("Floor Rules");

        int rowCount = 1;

        Row headerRow = floorSheet.createRow(rowCount);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Floor Name");
        headerRow.createCell(2).setCellValue("Hard");
        headerRow.createCell(3).setCellValue("Soft");
        headerRow.createCell(4).setCellValue("Priority");
        headerRow.createCell(5).setCellValue("Audience Targets");
        headerRow.createCell(6).setCellValue("Supply Targets");
        headerRow.createCell(7).setCellValue("Demand Filters");

        CellStyle boldHeader = wb.createCellStyle();
        Font boldFont = wb.createFont();
        boldFont.setFontHeightInPoints((short)16);
        boldFont.setBoldweight((short)1000);
        boldHeader.setFont(boldFont);

        CellStyle smallBoldHeader = wb.createCellStyle();
        Font smallBoldFont = wb.createFont();
        smallBoldFont.setFontHeightInPoints((short)12);
        smallBoldFont.setBoldweight((short)1000);
        smallBoldHeader.setFont(smallBoldFont);

        for (Cell c : headerRow) c.setCellStyle(boldHeader);
        titleRow.getCell(0).setCellStyle(boldHeader);

        rowCount+=2;

        for (PublisherConfig pub : pubs) {
            Row pubRow = floorSheet.createRow(rowCount);
            pubRow.createCell(0).setCellValue("Publisher:");
            pubRow.createCell(1).setCellValue(pub.getName()+"("+pub.getId()+")");

            pubRow.getCell(1).setCellStyle(smallBoldHeader);
            pubRow.getCell(0).setCellStyle(smallBoldHeader);

            rowCount++;

            DecimalFormat currency = new DecimalFormat("$##.00");

            for (YMProfile pro : pub.getYmProfiles()) {
                for (FloorRule r : pro.getFloorRules()) {

                    Font alert = wb.createFont();
                    alert.setBoldweight((short)1000);
                    CellStyle altGreen = wb.createCellStyle();
                    altGreen.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                    altGreen.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                    altGreen.setWrapText(true);
                    CellStyle normal = wb.createCellStyle();
                    normal.setWrapText(true);

                    Row dataRow = floorSheet.createRow(rowCount);
                    dataRow.createCell(0).setCellValue(r.getId());
                    dataRow.createCell(1).setCellValue(r.getName());
                    dataRow.createCell(2).setCellValue(currency.format(r.getHardFloor()));
                    dataRow.createCell(3).setCellValue(currency.format(r.getSoftFloor()));
                    dataRow.createCell(4).setCellValue(r.getPriority());
                    StringBuilder sb = new StringBuilder();
                    for (int x = 0; x < r.getAudienceList().size(); x++) {
                        sb.append(r.getAudienceList().get(x).getName());
                        sb.append("(");
                        sb.append(r.getAudienceList().get(x).getId());
                        sb.append(")");
                        if (!(x+1 >= r.getAudienceList().size())) sb.append("\n");
                    }
                    dataRow.createCell(5).setCellValue(sb.toString());
                    sb = new StringBuilder();
                    for (int x = 0; x < r.getSupplyList().size(); x++) {
                        sb.append(r.getSupplyList().get(x).getName());
                        sb.append("(");
                        sb.append(r.getSupplyList().get(x).getId());
                        sb.append(")");
                        if (!(x+1 >= r.getSupplyList().size())) sb.append("\n");
                    }
                    dataRow.createCell(6).setCellValue(sb.toString());
                    sb = new StringBuilder();
                    for (int x = 0; x < r.getDemandList().size(); x++) {
                        sb.append(r.getDemandList().get(x).getName());
                        sb.append("(");
                        sb.append(r.getDemandList().get(x).getId());
                        sb.append(")");
                        if (!(x+1 >= r.getDemandList().size())) sb.append("\n");
                    }
                    dataRow.createCell(7).setCellValue(sb.toString());

                    if(pro.modifiedYesterday()) {
                        altGreen.setFont(alert);
                        normal.setFont(alert);
                        dataRow.getCell(1).setCellValue(r.getName() + " (Modified Yesterday)");
                    }

                    for (Cell c : dataRow) {
                        if (rowCount % 2 == 1) c.setCellStyle(altGreen);
                        else c.setCellStyle(normal);
                    }

                    rowCount++;
                }
            }
            rowCount+=2;
        }
        for (int x = 0; x < 8; x++) floorSheet.autoSizeColumn(x);
        return wb;
    }
}