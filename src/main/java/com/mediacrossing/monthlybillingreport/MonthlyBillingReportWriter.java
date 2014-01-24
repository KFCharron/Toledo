package com.mediacrossing.monthlybillingreport;

//FIXME
//import com.mediacrossing.creativebillingreport.BillingCreative;
import com.mediacrossing.dailycheckupsreport.ServingFee;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MonthlyBillingReportWriter {

    public static void writeReportToFile(ArrayList<BillingAdvertiser> adList, List<String> feeNames, String outputPath) throws IOException {

        //Create wb
        Workbook wb = new HSSFWorkbook();
        DataFormat df = wb.createDataFormat();
        CellStyle fullCurrency = wb.createCellStyle();
        fullCurrency.setDataFormat(df.getFormat("$#,##0.00"));

        //Write summary sheet
        Sheet summarySheet = wb.createSheet("Overview");

        //Create grand total billing campaign
        BillingCampaign grandTotal = new BillingCampaign();
        for (BillingAdvertiser a : adList) {
            for (BillingCampaign c : a.getCampaigns()) {
                grandTotal.setImps(grandTotal.getImps() + c.getImps());
                grandTotal.setClicks(grandTotal.getClicks() + c.getClicks());
                grandTotal.setConvs(grandTotal.getConvs() + c.getConvs());
                grandTotal.setMediaCost(grandTotal.getMediaCost() + c.getMediaCost());
                grandTotal.setNetworkRevenue(grandTotal.getNetworkRevenue() + c.getNetworkRevenue());
                grandTotal.setAdExImps(grandTotal.getAdExImps() + c.getAdExImps());
                grandTotal.setMxImps(grandTotal.getMxImps() + c.getMxImps());
                grandTotal.setAppNexusImps(grandTotal.getAppNexusImps() + c.getAppNexusImps());
                grandTotal.setBriligImps(grandTotal.getBriligImps() + c.getBriligImps());
            }
        }

        summarySheet.createRow(1).createCell(0).setCellValue("Imps:");
        summarySheet.createRow(2).createCell(0).setCellValue("Clicks:");
        summarySheet.createRow(3).createCell(0).setCellValue("Convs:");
        summarySheet.createRow(4).createCell(0).setCellValue("3rd Party Imps:");
        summarySheet.createRow(5).createCell(0).setCellValue("Billable Imps:");
        summarySheet.createRow(6).createCell(0).setCellValue("CPM");
        summarySheet.createRow(7).createCell(0).setCellValue("App Nexus Revenue");
        summarySheet.createRow(8).createCell(0).setCellValue("Billable To Client");
        summarySheet.createRow(9).createCell(0).setCellValue("Media Cost");
        summarySheet.createRow(10).createCell(0).setCellValue("AdEx Imps:");
        summarySheet.createRow(11).createCell(0).setCellValue("AppNexus Imps:");
        summarySheet.createRow(12).createCell(0).setCellValue("Mx Imps:");
        summarySheet.createRow(13).createCell(0).setCellValue("Brilig Imps:");
        int rowCount = 13;
        for (String n : feeNames) {
            summarySheet.createRow(++rowCount).createCell(0).setCellValue(n);
            summarySheet.getRow(rowCount).createCell(1).setCellValue(0);
            summarySheet.getRow(rowCount).getCell(1).setCellStyle(fullCurrency);
        }
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("Amazon Cost:");
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("AdX Cost");
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("Brilig Cost");
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("Total Cost");
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("Gross Profit");
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("GP Margin");

        summarySheet.getRow(1).createCell(1).setCellValue(grandTotal.getImps());
        summarySheet.getRow(2).createCell(1).setCellValue(grandTotal.getClicks());
        summarySheet.getRow(3).createCell(1).setCellValue(grandTotal.getConvs());
        summarySheet.getRow(7).createCell(1).setCellValue(grandTotal.getNetworkRevenue());
        summarySheet.getRow(9).createCell(1).setCellValue(grandTotal.getMediaCost());
        summarySheet.getRow(10).createCell(1).setCellValue(grandTotal.getAdExImps());
        summarySheet.getRow(11).createCell(1).setCellValue(grandTotal.getAppNexusImps());
        summarySheet.getRow(12).createCell(1).setCellValue(grandTotal.getMxImps());
        summarySheet.getRow(13).createCell(1).setCellValue(grandTotal.getBriligImps());
        for (BillingAdvertiser a : adList) {
            for (BillingCampaign c : a.getCampaigns()) {
                for (ServingFee f : c.getServingFees()) {
                    for (Row r : summarySheet) {
                        if (r.getCell(0).getStringCellValue().equals(f.getBrokerName())) {
                            r.getCell(1).setCellValue(r.getCell(1).getNumericCellValue() + f.getTotalFee());
                        }
                    }
                }
            }
        }

        summarySheet.getRow(7).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(9).getCell(1).setCellStyle(fullCurrency);

        summarySheet.autoSizeColumn(0);
        summarySheet.autoSizeColumn(1);

        //Create sheet summing up by every advert.
        Sheet advertiserSummary = wb.createSheet("Advertiser Summary");
        ArrayList<BillingAdvertiser> adSummaryList = new ArrayList<>();

        //Create sheet for every advertiser
        for (BillingAdvertiser ad : adList) {

            String sheetName = WorkbookUtil.createSafeSheetName(ad.getName() + "(" + ad.getId() + ")");
            Sheet advertiserSheet = wb.createSheet(sheetName);

            Row headerRow = advertiserSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Campaign");
            headerRow.createCell(1).setCellValue("Imps");
            headerRow.createCell(2).setCellValue("Clicks");
            headerRow.createCell(3).setCellValue("Convs");
            headerRow.createCell(4).setCellValue("3rd Party Imps.");
            headerRow.createCell(5).setCellValue("Billable Imps");
            headerRow.createCell(6).setCellValue("CPM");
            headerRow.createCell(7).setCellValue("App Nexus Rev.");
            headerRow.createCell(8).setCellValue("Billable To Client");
            headerRow.createCell(9).setCellValue("Media Cost");
            headerRow.createCell(10).setCellValue("AdEx Imps");
            headerRow.createCell(11).setCellValue("Mx Imps");
            headerRow.createCell(12).setCellValue("AppNexus Imps");
            headerRow.createCell(13).setCellValue("Brilig Imps");
            int cellCount = 13;
            for (String n : feeNames) {
                headerRow.createCell(++cellCount).setCellValue(n);
            }
            headerRow.createCell(22).setCellValue("Amazon Cost");
            headerRow.createCell(23).setCellValue("AdX Cost");
            headerRow.createCell(24).setCellValue("Brilig Cost");
            headerRow.createCell(25).setCellValue("Total Cost");
            headerRow.createCell(26).setCellValue("Gross Profit");
            headerRow.createCell(27).setCellValue("GP Margin");

            rowCount = 0;

            BillingAdvertiser totalAdvertiser = new BillingAdvertiser(ad.getName(), ad.getId());
            BillingCampaign adTotal = new BillingCampaign();

            for (BillingCampaign c : ad.getCampaigns()) {
                Row dataRow = advertiserSheet.createRow(++rowCount);
                dataRow.createCell(0).setCellValue(c.getName() + "(" + c.getId() + ")");
                dataRow.createCell(1).setCellValue(c.getImps());
                dataRow.createCell(2).setCellValue(c.getClicks());
                dataRow.createCell(3).setCellValue(c.getConvs());
                dataRow.createCell(4);
                dataRow.createCell(5);
                dataRow.createCell(6);
                dataRow.createCell(7).setCellValue(c.getNetworkRevenue());
                dataRow.createCell(8);
                dataRow.createCell(9).setCellValue(c.getMediaCost());
                dataRow.createCell(10).setCellValue(c.getAdExImps());
                dataRow.createCell(11).setCellValue(c.getMxImps());
                dataRow.createCell(12).setCellValue(c.getAppNexusImps());
                dataRow.createCell(13).setCellValue(c.getBriligImps());
                cellCount = 13;
                for (String n : feeNames) {
                    dataRow.createCell(++cellCount).setCellValue(0);
                    dataRow.getCell(cellCount).setCellStyle(fullCurrency);
                }
                dataRow.createCell(++cellCount);
                dataRow.createCell(++cellCount);
                dataRow.createCell(++cellCount);
                dataRow.createCell(++cellCount);
                dataRow.createCell(++cellCount);
                dataRow.createCell(++cellCount);
                for (ServingFee f : c.getServingFees()) {
                    for (Cell cell: headerRow) {
                        if (cell.getStringCellValue().equals(f.getBrokerName())) {
                            dataRow.getCell(cell.getColumnIndex()).setCellValue(
                                    dataRow.getCell(cell.getColumnIndex()).getNumericCellValue() + f.getTotalFee());
                        }
                    }
                }

                dataRow.getCell(7).setCellStyle(fullCurrency);
                dataRow.getCell(9).setCellStyle(fullCurrency);

                adTotal.setImps(adTotal.getImps() + c.getImps());
                adTotal.setClicks(adTotal.getClicks() + c.getClicks());
                adTotal.setConvs(adTotal.getConvs() + c.getConvs());
                adTotal.setMediaCost(adTotal.getMediaCost() + c.getMediaCost());
                adTotal.setNetworkRevenue(adTotal.getNetworkRevenue() + c.getNetworkRevenue());
                adTotal.setAdExImps(adTotal.getAdExImps() + c.getAdExImps());
                adTotal.setMxImps(adTotal.getMxImps() + c.getMxImps());
                adTotal.setAppNexusImps(adTotal.getAppNexusImps() + c.getAppNexusImps());
                //FIXME setting adTotals?
                adTotal.setBriligImps(adTotal.getBriligImps() + c.getBriligImps());

                
            }

            //add totals to advertiser summary sheet
            totalAdvertiser.getCampaigns().add(adTotal);
            adSummaryList.add(totalAdvertiser);

            rowCount++;
            Row totalRow = advertiserSheet.createRow(++rowCount);
            totalRow.createCell(0).setCellValue("Totals:");
            totalRow.createCell(1).setCellValue(adTotal.getImps());
            totalRow.createCell(2).setCellValue(adTotal.getClicks());
            totalRow.createCell(3).setCellValue(adTotal.getConvs());
            totalRow.createCell(4);
            totalRow.createCell(5);
            totalRow.createCell(6);
            totalRow.createCell(7).setCellValue(adTotal.getNetworkRevenue());
            totalRow.createCell(8);
            totalRow.createCell(9).setCellValue(adTotal.getMediaCost());
            totalRow.createCell(10).setCellValue(adTotal.getAdExImps());
            totalRow.createCell(11).setCellValue(adTotal.getMxImps());
            totalRow.createCell(12).setCellValue(adTotal.getAppNexusImps());
            totalRow.createCell(13).setCellValue(adTotal.getBriligImps());
            cellCount = 13;
            for (String n : feeNames) {
                totalRow.createCell(++cellCount).setCellValue(0);
                totalRow.getCell(cellCount).setCellStyle(fullCurrency);
            }

            totalRow.getCell(7).setCellStyle(fullCurrency);
            totalRow.getCell(9).setCellStyle(fullCurrency);

            for (int x = 0; x <= cellCount; x++) advertiserSheet.autoSizeColumn(x);
        }

        //build advertiser summary page
        Row summaryHeader = advertiserSummary.createRow(0);
        summaryHeader.createCell(0).setCellValue("Advertiser");
        summaryHeader.createCell(1).setCellValue("Imps");
        summaryHeader.createCell(2).setCellValue("Clicks");
        summaryHeader.createCell(3).setCellValue("Convs");
        summaryHeader.createCell(4).setCellValue("3rd Party Imps.");
        summaryHeader.createCell(5).setCellValue("Billable Imps");
        summaryHeader.createCell(6).setCellValue("CPM");
        summaryHeader.createCell(7).setCellValue("App Nexus Rev.");
        summaryHeader.createCell(8).setCellValue("Billable To Client");
        summaryHeader.createCell(9).setCellValue("Media Cost");
        summaryHeader.createCell(10).setCellValue("AdEx Imps");
        summaryHeader.createCell(11).setCellValue("MX Imps");
        summaryHeader.createCell(12).setCellValue("AppNexus Imps");
        int cellCount = 12;
        for (String n : feeNames) {
            summaryHeader.createCell(++cellCount).setCellValue(n);
        }
        summaryHeader.createCell(++cellCount).setCellValue("Amazon Cost");
        summaryHeader.createCell(++cellCount).setCellValue("AdX Cost");
        summaryHeader.createCell(++cellCount).setCellValue("Brilig Cost");
        summaryHeader.createCell(++cellCount).setCellValue("Total Cost");
        summaryHeader.createCell(++cellCount).setCellValue("Gross Profit");
        summaryHeader.createCell(++cellCount).setCellValue("GP Margin");
        
        rowCount = 1;
        for (BillingAdvertiser ad : adSummaryList) {
            for (BillingCampaign c : ad.getCampaigns()) {
                Row adRow = advertiserSummary.createRow(rowCount);
                adRow.createCell(0).setCellValue(ad.getName());
                adRow.createCell(1).setCellValue(c.getImps());
                adRow.createCell(2).setCellValue(c.getClicks());
                adRow.createCell(3).setCellValue(c.getConvs());
                adRow.createCell(4);
                adRow.createCell(5);
                adRow.createCell(6);
                adRow.createCell(7).setCellValue(c.getNetworkRevenue());
                adRow.createCell(8);
                adRow.createCell(9).setCellValue(c.getMediaCost());
                adRow.createCell(10).setCellValue(c.getAdExImps());
                adRow.createCell(11).setCellValue(c.getMxImps());
                adRow.createCell(12).setCellValue(c.getAppNexusImps());
                cellCount = 12;
                for (String n : feeNames) {
                    adRow.createCell(++cellCount).setCellValue(0);
                    adRow.getCell(cellCount).setCellStyle(fullCurrency);

                }
                for (ServingFee f : c.getServingFees()) {
                    for (Cell cell: summaryHeader) {
                        if (cell.getStringCellValue().equals(f.getBrokerName())) {
                            adRow.getCell(cell.getColumnIndex()).setCellValue(f.getTotalFee());
                        }
                    }
                }

                adRow.getCell(7).setCellStyle(fullCurrency);
                adRow.getCell(9).setCellStyle(fullCurrency);

                rowCount++;
            }
        }
        for (int x = 0; x <= 35; x++) advertiserSummary.autoSizeColumn(x);


        //Export file
        LocalDate now = new LocalDate(DateTimeZone.UTC);
        FileOutputStream fileOut =
                new FileOutputStream(new File(outputPath, "MonthlyBillingReport_"
                        +now.toString()+".xls"));
        wb.write(fileOut);
        fileOut.close();
    }

//    public static void writeCreativeReport(ArrayList<BillingCreative> bc, ArrayList<BillingAdvertiser> ads, String outPath) throws IOException {
//
//        //Create wb
//        Workbook wb = new HSSFWorkbook();
//        DataFormat df = wb.createDataFormat();
//        CellStyle fullCurrency = wb.createCellStyle();
//        fullCurrency.setDataFormat(df.getFormat("$#,##0.00"));
//        Sheet sheet = wb.createSheet("Creative Billing Report");
//        CellStyle number = wb.createCellStyle();
//        number.setDataFormat(df.getFormat("###,###,###"));
//
//        Row head = sheet.createRow(0);
//        head.createCell(0).setCellValue("ID");
//        head.createCell(1).setCellValue("Creative");
//        head.createCell(2).setCellValue("Imps");
//        head.createCell(3).setCellValue("Clicks");
//        head.createCell(4).setCellValue("Convs");
//        head.createCell(5).setCellValue("3rd Party Imps");
//        head.createCell(6).setCellValue("Billable Imps");
//        head.createCell(7).setCellValue("CPM");
//        head.createCell(8).setCellValue("App Nexus Rev.");
//        head.createCell(9).setCellValue("Billable To Client");
//        head.createCell(10).setCellValue("Media Cost");
//        head.createCell(11).setCellValue("AdEx Imps");
//        head.createCell(12).setCellValue("MX Imps");
//        head.createCell(13).setCellValue("AppNexus Imps");
//        head.createCell(14).setCellValue("Brilig Imps");
//        head.createCell(15).setCellValue("ALC");
//        head.createCell(16).setCellValue("Evidon");
//        head.createCell(17).setCellValue("BlueKai");
//        head.createCell(18).setCellValue("Grapeshot");
//        head.createCell(19).setCellValue("Integral");
//        head.createCell(20).setCellValue("Peer39");
//        head.createCell(21).setCellValue("Spongecell");
//        head.createCell(22).setCellValue("Vidible");
//        head.createCell(23).setCellValue("Amazon Cost");
//        head.createCell(24).setCellValue("AdX Cost");
//        head.createCell(25).setCellValue("Brilig Cost");
//        head.createCell(26).setCellValue("Total Cost");
//        head.createCell(27).setCellValue("Gross Profit");
//        head.createCell(28).setCellValue("GP Margin");
//
//        BillingCreative total = new BillingCreative();
//        int rowCount = 1;
//        for(BillingCreative b: bc) {
//            Row dataRow = sheet.createRow(rowCount);
//            dataRow.createCell(0).setCellValue(b.getCreativeId());
//            dataRow.createCell(1).setCellValue(b.getCreativeName());
//            dataRow.createCell(2).setCellValue(b.getImps());
//            dataRow.createCell(3).setCellValue(b.getClicks());
//            dataRow.createCell(4).setCellValue(b.getConvs());
//            dataRow.createCell(8).setCellValue(b.getNetworkRevenue());
//            dataRow.createCell(10).setCellValue(b.getMediaCost());
//            dataRow.createCell(11).setCellValue(b.getAdExImps());
//            dataRow.createCell(12).setCellValue(b.getMxImps());
//            dataRow.createCell(13).setCellValue(b.getAppNexusImps());
//            dataRow.createCell(14).setCellValue(b.getBriligImps());
//            dataRow.createCell(15).setCellValue(b.getAlcTotal());
//            dataRow.createCell(16).setCellValue(b.getEvidonTotal());
//            dataRow.createCell(17).setCellValue(b.getBlueKaiTotal());
//            dataRow.createCell(18).setCellValue(b.getGrapeshotTotal());
//            dataRow.createCell(19).setCellValue(b.getIntegralTotal());
//            dataRow.createCell(20).setCellValue(b.getPeer39Total());
//            dataRow.createCell(21).setCellValue(b.getSpongecellTotal());
//            dataRow.createCell(22).setCellValue(b.getVidibleTotal());
//
//            dataRow.getCell(2).setCellStyle(number);
//            dataRow.getCell(3).setCellStyle(number);
//            dataRow.getCell(4).setCellStyle(number);
//            dataRow.getCell(8).setCellStyle(fullCurrency);
//            dataRow.getCell(10).setCellStyle(fullCurrency);
//            dataRow.getCell(11).setCellStyle(number);
//            dataRow.getCell(12).setCellStyle(number);
//            dataRow.getCell(13).setCellStyle(number);
//            dataRow.getCell(14).setCellStyle(number);
//            dataRow.getCell(15).setCellStyle(fullCurrency);
//            dataRow.getCell(16).setCellStyle(fullCurrency);
//            dataRow.getCell(17).setCellStyle(fullCurrency);
//            dataRow.getCell(18).setCellStyle(fullCurrency);
//            dataRow.getCell(19).setCellStyle(fullCurrency);
//            dataRow.getCell(20).setCellStyle(fullCurrency);
//            dataRow.getCell(21).setCellStyle(fullCurrency);
//            dataRow.getCell(22).setCellStyle(fullCurrency);
//
//            total.setImps(total.getImps() + b.getImps());
//            total.setClicks(total.getClicks() + b.getClicks());
//            total.setConvs(total.getConvs() + b.getConvs());
//            total.setMediaCost(total.getMediaCost() + b.getMediaCost());
//            total.setNetworkRevenue(total.getNetworkRevenue() + b.getNetworkRevenue());
//            total.setAdExImps(total.getAdExImps() + b.getAdExImps());
//            total.setMxImps(total.getMxImps() + b.getMxImps());
//            total.setAppNexusImps(total.getAppNexusImps() + b.getAppNexusImps());
//            total.setBriligImps(total.getBriligImps() + b.getBriligImps());
//            total.setBriligTotal(total.getBriligTotal() + b.getBriligTotal());
//            total.setEvidonTotal(total.getEvidonTotal() + b.getEvidonTotal());
//            total.setIntegralTotal(total.getIntegralTotal() + b.getIntegralTotal());
//            total.setBlueKaiTotal(total.getBlueKaiTotal() + b.getBlueKaiTotal());
//            total.setAlcTotal(total.getAlcTotal() + b.getAlcTotal());
//            total.setGrapeshotTotal(total.getGrapeshotTotal() + b.getGrapeshotTotal());
//            total.setSpongecellTotal(total.getSpongecellTotal() + b.getSpongecellTotal());
//            total.setVidibleTotal(total.getVidibleTotal() + b.getVidibleTotal());
//            total.setPeer39Total(total.getPeer39Total() + b.getPeer39Total());
//
//            rowCount++;
//        }
//        Row totalRow = sheet.createRow(++rowCount);
//        totalRow.createCell(0).setCellValue("Totals:");
//        totalRow.createCell(2).setCellValue(total.getImps());
//        totalRow.createCell(3).setCellValue(total.getClicks());
//        totalRow.createCell(4).setCellValue(total.getConvs());
//        totalRow.createCell(8).setCellValue(total.getNetworkRevenue());
//        totalRow.createCell(10).setCellValue(total.getMediaCost());
//        totalRow.createCell(11).setCellValue(total.getAdExImps());
//        totalRow.createCell(12).setCellValue(total.getMxImps());
//        totalRow.createCell(13).setCellValue(total.getAppNexusImps());
//        totalRow.createCell(14).setCellValue(total.getBriligImps());
//        totalRow.createCell(15).setCellValue(total.getAlcTotal());
//        totalRow.createCell(16).setCellValue(total.getEvidonTotal());
//        totalRow.createCell(17).setCellValue(total.getBlueKaiTotal());
//        totalRow.createCell(18).setCellValue(total.getGrapeshotTotal());
//        totalRow.createCell(19).setCellValue(total.getIntegralTotal());
//        totalRow.createCell(20).setCellValue(total.getPeer39Total());
//        totalRow.createCell(21).setCellValue(total.getSpongecellTotal());
//        totalRow.createCell(22).setCellValue(total.getVidibleTotal());
//
//        totalRow.getCell(2).setCellStyle(number);
//        totalRow.getCell(3).setCellStyle(number);
//        totalRow.getCell(4).setCellStyle(number);
//        totalRow.getCell(8).setCellStyle(fullCurrency);
//        totalRow.getCell(10).setCellStyle(fullCurrency);
//        totalRow.getCell(11).setCellStyle(number);
//        totalRow.getCell(12).setCellStyle(number);
//        totalRow.getCell(13).setCellStyle(number);
//        totalRow.getCell(14).setCellStyle(number);
//        totalRow.getCell(15).setCellStyle(fullCurrency);
//        totalRow.getCell(16).setCellStyle(fullCurrency);
//        totalRow.getCell(17).setCellStyle(fullCurrency);
//        totalRow.getCell(18).setCellStyle(fullCurrency);
//        totalRow.getCell(19).setCellStyle(fullCurrency);
//        totalRow.getCell(20).setCellStyle(fullCurrency);
//        totalRow.getCell(21).setCellStyle(fullCurrency);
//        totalRow.getCell(22).setCellStyle(fullCurrency);
//
//        for (Cell c : head) sheet.autoSizeColumn(c.getColumnIndex());
//
//        for (BillingAdvertiser ad : ads) {
//            Sheet adSheet = wb.createSheet(ad.getName());
//            Row header = adSheet.createRow(0);
//            header.createCell(0).setCellValue("ID");
//            header.createCell(1).setCellValue("Creative");
//            header.createCell(2).setCellValue("Imps");
//            header.createCell(3).setCellValue("Clicks");
//            header.createCell(4).setCellValue("Convs");
//            header.createCell(5).setCellValue("3rd Party Imps");
//            header.createCell(6).setCellValue("Billable Imps");
//            header.createCell(7).setCellValue("CPM");
//            header.createCell(8).setCellValue("App Nexus Rev.");
//            header.createCell(9).setCellValue("Billable To Client");
//            header.createCell(10).setCellValue("Media Cost");
//            header.createCell(11).setCellValue("AdEx Imps");
//            header.createCell(12).setCellValue("MX Imps");
//            header.createCell(13).setCellValue("AppNexus Imps");
//            header.createCell(14).setCellValue("Brilig Imps");
//            header.createCell(15).setCellValue("ALC");
//            header.createCell(16).setCellValue("Evidon");
//            header.createCell(17).setCellValue("BlueKai");
//            header.createCell(18).setCellValue("Grapeshot");
//            header.createCell(19).setCellValue("Integral");
//            header.createCell(20).setCellValue("Peer39");
//            header.createCell(21).setCellValue("Spongecell");
//            header.createCell(22).setCellValue("Vidible");
//            header.createCell(23).setCellValue("Amazon Cost");
//            header.createCell(24).setCellValue("AdX Cost");
//            header.createCell(25).setCellValue("Brilig Cost");
//            header.createCell(26).setCellValue("Total Cost");
//            header.createCell(27).setCellValue("Gross Profit");
//            header.createCell(28).setCellValue("GP Margin");
//
//            BillingCreative adTotal = new BillingCreative();
//            int count = 1;
//            for (BillingCreative c : bc) {
//                if (c.getAdId().equals(ad.getId())) {
//                    Row d = adSheet.createRow(count);
//                    d.createCell(0).setCellValue(c.getCreativeId());
//                    d.createCell(1).setCellValue(c.getCreativeName());
//                    d.createCell(2).setCellValue(c.getImps());
//                    d.createCell(3).setCellValue(c.getClicks());
//                    d.createCell(4).setCellValue(c.getConvs());
//                    d.createCell(8).setCellValue(c.getNetworkRevenue());
//                    d.createCell(10).setCellValue(c.getMediaCost());
//                    d.createCell(11).setCellValue(c.getAdExImps());
//                    d.createCell(12).setCellValue(c.getMxImps());
//                    d.createCell(13).setCellValue(c.getAppNexusImps());
//                    d.createCell(14).setCellValue(c.getBriligImps());
//                    d.createCell(15).setCellValue(c.getAlcTotal());
//                    d.createCell(16).setCellValue(c.getEvidonTotal());
//                    d.createCell(17).setCellValue(c.getBlueKaiTotal());
//                    d.createCell(18).setCellValue(c.getGrapeshotTotal());
//                    d.createCell(19).setCellValue(c.getIntegralTotal());
//                    d.createCell(20).setCellValue(c.getPeer39Total());
//                    d.createCell(21).setCellValue(c.getSpongecellTotal());
//                    d.createCell(22).setCellValue(c.getVidibleTotal());
//
//                    d.getCell(2).setCellStyle(number);
//                    d.getCell(3).setCellStyle(number);
//                    d.getCell(4).setCellStyle(number);
//                    d.getCell(8).setCellStyle(fullCurrency);
//                    d.getCell(10).setCellStyle(fullCurrency);
//                    d.getCell(11).setCellStyle(number);
//                    d.getCell(12).setCellStyle(number);
//                    d.getCell(13).setCellStyle(number);
//                    d.getCell(14).setCellStyle(number);
//                    d.getCell(15).setCellStyle(fullCurrency);
//                    d.getCell(16).setCellStyle(fullCurrency);
//                    d.getCell(17).setCellStyle(fullCurrency);
//                    d.getCell(18).setCellStyle(fullCurrency);
//                    d.getCell(19).setCellStyle(fullCurrency);
//                    d.getCell(20).setCellStyle(fullCurrency);
//                    d.getCell(21).setCellStyle(fullCurrency);
//                    d.getCell(22).setCellStyle(fullCurrency);
//
//                    adTotal.setImps(adTotal.getImps() + c.getImps());
//                    adTotal.setClicks(adTotal.getClicks() + c.getClicks());
//                    adTotal.setConvs(adTotal.getConvs() + c.getConvs());
//                    adTotal.setMediaCost(adTotal.getMediaCost() + c.getMediaCost());
//                    adTotal.setNetworkRevenue(adTotal.getNetworkRevenue() + c.getNetworkRevenue());
//                    adTotal.setAdExImps(adTotal.getAdExImps() + c.getAdExImps());
//                    adTotal.setMxImps(adTotal.getMxImps() + c.getMxImps());
//                    adTotal.setAppNexusImps(adTotal.getAppNexusImps() + c.getAppNexusImps());
//                    adTotal.setBriligImps(adTotal.getBriligImps() + c.getBriligImps());
//                    adTotal.setBriligTotal(adTotal.getBriligTotal() + c.getBriligTotal());
//                    adTotal.setEvidonTotal(adTotal.getEvidonTotal() + c.getEvidonTotal());
//                    adTotal.setIntegralTotal(adTotal.getIntegralTotal() + c.getIntegralTotal());
//                    adTotal.setBlueKaiTotal(adTotal.getBlueKaiTotal() + c.getBlueKaiTotal());
//                    adTotal.setAlcTotal(adTotal.getAlcTotal() + c.getAlcTotal());
//                    adTotal.setGrapeshotTotal(adTotal.getGrapeshotTotal() + c.getGrapeshotTotal());
//                    adTotal.setSpongecellTotal(adTotal.getSpongecellTotal() + c.getSpongecellTotal());
//                    adTotal.setVidibleTotal(adTotal.getVidibleTotal() + c.getVidibleTotal());
//                    adTotal.setPeer39Total(adTotal.getPeer39Total() + c.getPeer39Total());
//
//                    count++;
//                }
//            }
//            Row adTotalRow = adSheet.createRow(count+1);
//            adTotalRow.createCell(0).setCellValue("Totals:");
//            adTotalRow.createCell(2).setCellValue(adTotal.getImps());
//            adTotalRow.createCell(3).setCellValue(adTotal.getClicks());
//            adTotalRow.createCell(4).setCellValue(adTotal.getConvs());
//            adTotalRow.createCell(8).setCellValue(adTotal.getNetworkRevenue());
//            adTotalRow.createCell(10).setCellValue(adTotal.getMediaCost());
//            adTotalRow.createCell(11).setCellValue(adTotal.getAdExImps());
//            adTotalRow.createCell(12).setCellValue(adTotal.getMxImps());
//            adTotalRow.createCell(13).setCellValue(adTotal.getAppNexusImps());
//            adTotalRow.createCell(14).setCellValue(adTotal.getBriligImps());
//            adTotalRow.createCell(15).setCellValue(adTotal.getAlcTotal());
//            adTotalRow.createCell(16).setCellValue(adTotal.getEvidonTotal());
//            adTotalRow.createCell(17).setCellValue(adTotal.getBlueKaiTotal());
//            adTotalRow.createCell(18).setCellValue(adTotal.getGrapeshotTotal());
//            adTotalRow.createCell(19).setCellValue(adTotal.getIntegralTotal());
//            adTotalRow.createCell(20).setCellValue(adTotal.getPeer39Total());
//            adTotalRow.createCell(21).setCellValue(adTotal.getSpongecellTotal());
//            adTotalRow.createCell(22).setCellValue(adTotal.getVidibleTotal());
//
//            adTotalRow.getCell(2).setCellStyle(number);
//            adTotalRow.getCell(3).setCellStyle(number);
//            adTotalRow.getCell(4).setCellStyle(number);
//            adTotalRow.getCell(8).setCellStyle(fullCurrency);
//            adTotalRow.getCell(10).setCellStyle(fullCurrency);
//            adTotalRow.getCell(11).setCellStyle(number);
//            adTotalRow.getCell(12).setCellStyle(number);
//            adTotalRow.getCell(13).setCellStyle(number);
//            adTotalRow.getCell(14).setCellStyle(number);
//            adTotalRow.getCell(15).setCellStyle(fullCurrency);
//            adTotalRow.getCell(16).setCellStyle(fullCurrency);
//            adTotalRow.getCell(17).setCellStyle(fullCurrency);
//            adTotalRow.getCell(18).setCellStyle(fullCurrency);
//            adTotalRow.getCell(19).setCellStyle(fullCurrency);
//            adTotalRow.getCell(20).setCellStyle(fullCurrency);
//            adTotalRow.getCell(21).setCellStyle(fullCurrency);
//            adTotalRow.getCell(22).setCellStyle(fullCurrency);
//
//            for (Cell c : header) adSheet.autoSizeColumn(c.getColumnIndex());
//        }
//
//        //Export file
//        LocalDate now = new LocalDate(DateTimeZone.UTC);
//        FileOutputStream fileOut =
//                new FileOutputStream(new File(outPath, "CreativeBillingReport_"
//                        +now.toString()+".xls"));
//        wb.write(fileOut);
//        fileOut.close();
//
//
//    }
}
