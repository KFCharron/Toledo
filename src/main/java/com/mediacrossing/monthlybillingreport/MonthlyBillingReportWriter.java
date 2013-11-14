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
            }
        }

        summarySheet.createRow(1).createCell(0).setCellValue("Imps:");
        summarySheet.createRow(2).createCell(0).setCellValue("Clicks:");
        summarySheet.createRow(3).createCell(0).setCellValue("Convs:");
        summarySheet.createRow(4).createCell(0).setCellValue("Media Cost:");
        summarySheet.createRow(5).createCell(0).setCellValue("Network Rev.");
        summarySheet.createRow(6).createCell(0).setCellValue("AdEx Imps:");
        summarySheet.createRow(7).createCell(0).setCellValue("AppNexus Imps:");
        summarySheet.createRow(8).createCell(0).setCellValue("Brilig Imps:");
        summarySheet.createRow(9).createCell(0).setCellValue("Evidon:");
        summarySheet.createRow(10).createCell(0).setCellValue("Integral:");
        summarySheet.createRow(11).createCell(0).setCellValue("Grapeshot:");
        summarySheet.createRow(12).createCell(0).setCellValue("BlueKai:");
        summarySheet.createRow(13).createCell(0).setCellValue("ALC:");

        summarySheet.getRow(1).createCell(1).setCellValue(grandTotal.getImps());
        summarySheet.getRow(2).createCell(1).setCellValue(grandTotal.getClicks());
        summarySheet.getRow(3).createCell(1).setCellValue(grandTotal.getConvs());
        summarySheet.getRow(4).createCell(1).setCellValue(grandTotal.getMediaCost());
        summarySheet.getRow(5).createCell(1).setCellValue(grandTotal.getNetworkRevenue());
        summarySheet.getRow(6).createCell(1).setCellValue(grandTotal.getAdExImps());
        summarySheet.getRow(7).createCell(1).setCellValue(grandTotal.getAppNexusImps());
        summarySheet.getRow(8).createCell(1).setCellValue(grandTotal.getBriligImps());
        summarySheet.getRow(9).createCell(1).setCellValue(grandTotal.getEvidonTotal());
        summarySheet.getRow(10).createCell(1).setCellValue(grandTotal.getIntegralTotal());
        summarySheet.getRow(11).createCell(1).setCellValue(grandTotal.getGrapeshotTotal());
        summarySheet.getRow(12).createCell(1).setCellValue(grandTotal.getBlueKaiTotal());
        summarySheet.getRow(13).createCell(1).setCellValue(grandTotal.getAlcTotal());

        summarySheet.getRow(4).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(5).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(9).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(10).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(11).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(12).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(13).getCell(1).setCellStyle(fullCurrency);

        summarySheet.autoSizeColumn(0);
        summarySheet.autoSizeColumn(1);


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
            headerRow.createCell(7).setCellValue("Media Cost");
            headerRow.createCell(8).setCellValue("Network Rev.");
            headerRow.createCell(9).setCellValue("AdEx Imps");
            headerRow.createCell(10).setCellValue("AppNexus Imps");
            headerRow.createCell(11).setCellValue("Brilig Imps");
            headerRow.createCell(12).setCellValue("Evidon");
            headerRow.createCell(13).setCellValue("Integral");
            headerRow.createCell(14).setCellValue("Grapeshot");
            headerRow.createCell(15).setCellValue("BlueKai");
            headerRow.createCell(16).setCellValue("ALC");

            int rowCount = 0;
            BillingCampaign adTotal = new BillingCampaign();

            for (BillingCampaign c : ad.getCampaigns()) {
                Row dataRow = advertiserSheet.createRow(++rowCount);
                dataRow.createCell(0).setCellValue(c.getName() + "(" + c.getId() + ")");
                dataRow.createCell(1).setCellValue(c.getImps());
                dataRow.createCell(2).setCellValue(c.getClicks());
                dataRow.createCell(3).setCellValue(c.getConvs());
                dataRow.createCell(4);
                dataRow.createCell(5);
                dataRow.createCell(6).setCellValue(c.getCpm());
                dataRow.createCell(7).setCellValue(c.getMediaCost());
                dataRow.createCell(8).setCellValue(c.getNetworkRevenue());
                dataRow.createCell(9).setCellValue(c.getAdExImps());
                dataRow.createCell(10).setCellValue(c.getAppNexusImps());
                dataRow.createCell(11).setCellValue(c.getBriligImps());
                dataRow.createCell(12).setCellValue(c.getEvidonTotal());
                dataRow.createCell(13).setCellValue(c.getIntegralTotal());
                dataRow.createCell(14).setCellValue(c.getGrapeshotTotal());
                dataRow.createCell(15).setCellValue(c.getBlueKaiTotal());
                dataRow.createCell(16).setCellValue(c.getAlcTotal());

                dataRow.getCell(6).setCellStyle(fullCurrency);
                dataRow.getCell(7).setCellStyle(fullCurrency);
                dataRow.getCell(8).setCellStyle(fullCurrency);
                dataRow.getCell(12).setCellStyle(fullCurrency);
                dataRow.getCell(13).setCellStyle(fullCurrency);
                dataRow.getCell(14).setCellStyle(fullCurrency);
                dataRow.getCell(15).setCellStyle(fullCurrency);
                dataRow.getCell(16).setCellStyle(fullCurrency);

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
                
            }
            rowCount++;
            Row totalRow = advertiserSheet.createRow(++rowCount);
            totalRow.createCell(0).setCellValue("Totals:");
            totalRow.createCell(1).setCellValue(adTotal.getImps());
            totalRow.createCell(2).setCellValue(adTotal.getClicks());
            totalRow.createCell(3).setCellValue(adTotal.getConvs());
            totalRow.createCell(4);
            totalRow.createCell(5);
            totalRow.createCell(6);
            totalRow.createCell(7).setCellValue(adTotal.getMediaCost());
            totalRow.createCell(8).setCellValue(adTotal.getNetworkRevenue());
            totalRow.createCell(9).setCellValue(adTotal.getAdExImps());
            totalRow.createCell(10).setCellValue(adTotal.getAppNexusImps());
            totalRow.createCell(11).setCellValue(adTotal.getBriligImps());
            totalRow.createCell(12).setCellValue(adTotal.getEvidonTotal());
            totalRow.createCell(13).setCellValue(adTotal.getIntegralTotal());
            totalRow.createCell(14).setCellValue(adTotal.getGrapeshotTotal());
            totalRow.createCell(15).setCellValue(adTotal.getBlueKaiTotal());
            totalRow.createCell(16).setCellValue(adTotal.getAlcTotal());

            totalRow.getCell(7).setCellStyle(fullCurrency);
            totalRow.getCell(8).setCellStyle(fullCurrency);
            totalRow.getCell(12).setCellStyle(fullCurrency);
            totalRow.getCell(13).setCellStyle(fullCurrency);
            totalRow.getCell(14).setCellStyle(fullCurrency);
            totalRow.getCell(15).setCellStyle(fullCurrency);
            totalRow.getCell(16).setCellStyle(fullCurrency);

            for (int x = 0; x < 17; x++) advertiserSheet.autoSizeColumn(x);
        }

        //Export file
        LocalDate now = new LocalDate(DateTimeZone.UTC);
        FileOutputStream fileOut =
                new FileOutputStream(new File(outputPath, "MonthlyBillingReport_"
                        +now.toString()+".xls"));
        wb.write(fileOut);
        fileOut.close();
    }
}
