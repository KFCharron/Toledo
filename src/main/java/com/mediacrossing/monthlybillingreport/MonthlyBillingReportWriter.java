package com.mediacrossing.monthlybillingreport;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MonthlyBillingReportWriter {

    public static void writeReportToFile(ArrayList<BillingAdvertiser> adList, String outputPath) throws IOException {

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
                grandTotal.setAppNexusImps(grandTotal.getAppNexusImps() + c.getAppNexusImps());
                grandTotal.setBriligTotal(grandTotal.getBriligTotal() + c.getBriligTotal());
                grandTotal.setBriligImps(grandTotal.getBriligImps() + c.getBriligImps());
                grandTotal.setEvidonTotal(grandTotal.getEvidonTotal() + c.getEvidonTotal());
                grandTotal.setIntegralTotal(grandTotal.getIntegralTotal() + c.getIntegralTotal());
                grandTotal.setAlcTotal(grandTotal.getAlcTotal() + c.getAlcTotal());
                grandTotal.setBlueKaiTotal(grandTotal.getBlueKaiTotal() + c.getBlueKaiTotal());
                grandTotal.setGrapeshotTotal(grandTotal.getGrapeshotTotal() + c.getGrapeshotTotal());
                grandTotal.setSpongecellTotal(grandTotal.getSpongecellTotal() + c.getSpongecellTotal());
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
        summarySheet.createRow(12).createCell(0).setCellValue("Brilig Imps:");
        summarySheet.createRow(13).createCell(0).setCellValue("ALC:");
        summarySheet.createRow(14).createCell(0).setCellValue("Evidon:");
        summarySheet.createRow(15).createCell(0).setCellValue("BlueKai:");
        summarySheet.createRow(16).createCell(0).setCellValue("Grapeshot:");
        summarySheet.createRow(17).createCell(0).setCellValue("Integral:");
        summarySheet.createRow(18).createCell(0).setCellValue("Spongecell:");
        summarySheet.createRow(19).createCell(0).setCellValue("Amazon Cost:");
        summarySheet.createRow(20).createCell(0).setCellValue("AdX Cost");
        summarySheet.createRow(21).createCell(0).setCellValue("Brilig Cost");
        summarySheet.createRow(22).createCell(0).setCellValue("Total Cost");
        summarySheet.createRow(23).createCell(0).setCellValue("Gross Profit");
        summarySheet.createRow(24).createCell(0).setCellValue("GP Margin");

        summarySheet.getRow(1).createCell(1).setCellValue(grandTotal.getImps());
        summarySheet.getRow(2).createCell(1).setCellValue(grandTotal.getClicks());
        summarySheet.getRow(3).createCell(1).setCellValue(grandTotal.getConvs());
        summarySheet.getRow(7).createCell(1).setCellValue(grandTotal.getNetworkRevenue());
        summarySheet.getRow(9).createCell(1).setCellValue(grandTotal.getMediaCost());
        summarySheet.getRow(10).createCell(1).setCellValue(grandTotal.getAdExImps());
        summarySheet.getRow(11).createCell(1).setCellValue(grandTotal.getAppNexusImps());
        summarySheet.getRow(12).createCell(1).setCellValue(grandTotal.getBriligImps());
        summarySheet.getRow(13).createCell(1).setCellValue(grandTotal.getAlcTotal());
        summarySheet.getRow(14).createCell(1).setCellValue(grandTotal.getEvidonTotal());
        summarySheet.getRow(15).createCell(1).setCellValue(grandTotal.getBlueKaiTotal());
        summarySheet.getRow(16).createCell(1).setCellValue(grandTotal.getGrapeshotTotal());
        summarySheet.getRow(17).createCell(1).setCellValue(grandTotal.getIntegralTotal());
        summarySheet.getRow(18).createCell(1).setCellValue(grandTotal.getSpongecellTotal());

        summarySheet.getRow(7).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(9).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(13).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(14).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(15).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(16).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(17).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(18).getCell(1).setCellStyle(fullCurrency);

        summarySheet.autoSizeColumn(0);
        summarySheet.autoSizeColumn(1);

        //Create sheet summing up by every advert.
        Sheet advertiserSummary = wb.createSheet("Advertiser Summary");
        ArrayList<BillingAdvertiser> adSummaryList = new ArrayList<BillingAdvertiser>();

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
            headerRow.createCell(11).setCellValue("AppNexus Imps");
            headerRow.createCell(12).setCellValue("Brilig Imps");
            headerRow.createCell(13).setCellValue("ALC");
            headerRow.createCell(14).setCellValue("Evidon");
            headerRow.createCell(15).setCellValue("BlueKai");
            headerRow.createCell(16).setCellValue("Grapeshot");
            headerRow.createCell(17).setCellValue("Integral");
            headerRow.createCell(18).setCellValue("Spongecell");
            headerRow.createCell(19).setCellValue("Amazon Cost");
            headerRow.createCell(20).setCellValue("AdX Cost");
            headerRow.createCell(21).setCellValue("Brilig Cost");
            headerRow.createCell(22).setCellValue("Total Cost");
            headerRow.createCell(23).setCellValue("Gross Profit");
            headerRow.createCell(24).setCellValue("GP Margin");

            int rowCount = 0;

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
                dataRow.createCell(11).setCellValue(c.getAppNexusImps());
                dataRow.createCell(12).setCellValue(c.getBriligImps());
                dataRow.createCell(13).setCellValue(c.getAlcTotal());
                dataRow.createCell(14).setCellValue(c.getEvidonTotal());
                dataRow.createCell(15).setCellValue(c.getBlueKaiTotal());
                dataRow.createCell(16).setCellValue(c.getGrapeshotTotal());
                dataRow.createCell(17).setCellValue(c.getIntegralTotal());
                dataRow.createCell(18).setCellValue(c.getSpongecellTotal());
                dataRow.createCell(19);
                dataRow.createCell(20);
                dataRow.createCell(21);
                dataRow.createCell(22);
                dataRow.createCell(23);
                dataRow.createCell(24);

                dataRow.getCell(7).setCellStyle(fullCurrency);
                dataRow.getCell(9).setCellStyle(fullCurrency);
                dataRow.getCell(13).setCellStyle(fullCurrency);
                dataRow.getCell(14).setCellStyle(fullCurrency);
                dataRow.getCell(15).setCellStyle(fullCurrency);
                dataRow.getCell(16).setCellStyle(fullCurrency);
                dataRow.getCell(17).setCellStyle(fullCurrency);
                dataRow.getCell(18).setCellStyle(fullCurrency);

                adTotal.setImps(adTotal.getImps() + c.getImps());
                adTotal.setClicks(adTotal.getClicks() + c.getClicks());
                adTotal.setConvs(adTotal.getConvs() + c.getConvs());
                adTotal.setMediaCost(adTotal.getMediaCost() + c.getMediaCost());
                adTotal.setNetworkRevenue(adTotal.getNetworkRevenue() + c.getNetworkRevenue());
                adTotal.setAdExImps(adTotal.getAdExImps() + c.getAdExImps());
                adTotal.setAppNexusImps(adTotal.getAppNexusImps() + c.getAppNexusImps());
                adTotal.setBriligTotal(adTotal.getBriligTotal() + c.getBriligTotal());
                adTotal.setEvidonTotal(adTotal.getEvidonTotal() + c.getEvidonTotal());
                adTotal.setIntegralTotal(adTotal.getIntegralTotal() + c.getIntegralTotal());
                adTotal.setBriligImps(adTotal.getBriligImps() + c.getBriligImps());
                adTotal.setGrapeshotTotal(adTotal.getGrapeshotTotal() + c.getGrapeshotTotal());
                adTotal.setBlueKaiTotal(adTotal.getBlueKaiTotal() + c.getBlueKaiTotal());
                adTotal.setAlcTotal(adTotal.getAlcTotal() + c.getAlcTotal());
                adTotal.setSpongecellTotal(adTotal.getSpongecellTotal() + c.getSpongecellTotal());
                
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
            totalRow.createCell(11).setCellValue(adTotal.getAppNexusImps());
            totalRow.createCell(12).setCellValue(adTotal.getBriligImps());
            totalRow.createCell(13).setCellValue(adTotal.getAlcTotal());
            totalRow.createCell(14).setCellValue(adTotal.getEvidonTotal());
            totalRow.createCell(15).setCellValue(adTotal.getBlueKaiTotal());
            totalRow.createCell(16).setCellValue(adTotal.getGrapeshotTotal());
            totalRow.createCell(17).setCellValue(adTotal.getIntegralTotal());
            totalRow.createCell(18).setCellValue(adTotal.getSpongecellTotal());

            totalRow.getCell(7).setCellStyle(fullCurrency);
            totalRow.getCell(9).setCellStyle(fullCurrency);
            totalRow.getCell(13).setCellStyle(fullCurrency);
            totalRow.getCell(14).setCellStyle(fullCurrency);
            totalRow.getCell(15).setCellStyle(fullCurrency);
            totalRow.getCell(16).setCellStyle(fullCurrency);
            totalRow.getCell(17).setCellStyle(fullCurrency);
            totalRow.getCell(18).setCellStyle(fullCurrency);

            for (int x = 0; x < 25; x++) advertiserSheet.autoSizeColumn(x);
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
        summaryHeader.createCell(11).setCellValue("AppNexus Imps");
        summaryHeader.createCell(12).setCellValue("Brilig Imps");
        summaryHeader.createCell(13).setCellValue("ALC");
        summaryHeader.createCell(14).setCellValue("Evidon");
        summaryHeader.createCell(15).setCellValue("BlueKai");
        summaryHeader.createCell(16).setCellValue("Grapeshot");
        summaryHeader.createCell(17).setCellValue("Integral");
        summaryHeader.createCell(18).setCellValue("Spongecell");
        summaryHeader.createCell(19).setCellValue("Amazon Cost");
        summaryHeader.createCell(20).setCellValue("AdX Cost");
        summaryHeader.createCell(21).setCellValue("Brilig Cost");
        summaryHeader.createCell(22).setCellValue("Total Cost");
        summaryHeader.createCell(23).setCellValue("Gross Profit");
        summaryHeader.createCell(24).setCellValue("GP Margin");
        
        int rowCount = 1;
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
                adRow.createCell(11).setCellValue(c.getAppNexusImps());
                adRow.createCell(12).setCellValue(c.getBriligImps());
                adRow.createCell(13).setCellValue(c.getAlcTotal());
                adRow.createCell(14).setCellValue(c.getEvidonTotal());
                adRow.createCell(15).setCellValue(c.getBlueKaiTotal());
                adRow.createCell(16).setCellValue(c.getGrapeshotTotal());
                adRow.createCell(17).setCellValue(c.getIntegralTotal());
                adRow.createCell(18).setCellValue(c.getSpongecellTotal());

                adRow.getCell(7).setCellStyle(fullCurrency);
                adRow.getCell(9).setCellStyle(fullCurrency);
                adRow.getCell(13).setCellStyle(fullCurrency);
                adRow.getCell(14).setCellStyle(fullCurrency);
                adRow.getCell(15).setCellStyle(fullCurrency);
                adRow.getCell(16).setCellStyle(fullCurrency);
                adRow.getCell(17).setCellStyle(fullCurrency);
                adRow.getCell(18).setCellStyle(fullCurrency);

                rowCount++;
            }
        }
        for (int x = 0; x < 25; x++) advertiserSummary.autoSizeColumn(x);


        //Export file
        LocalDate now = new LocalDate(DateTimeZone.UTC);
        FileOutputStream fileOut =
                new FileOutputStream(new File(outputPath, "MonthlyBillingReport_"
                        +now.toString()+".xls"));
        wb.write(fileOut);
        fileOut.close();
    }
}
