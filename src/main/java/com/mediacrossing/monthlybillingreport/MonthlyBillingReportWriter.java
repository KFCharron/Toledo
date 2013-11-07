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
                grandTotal.setEvidonTotal(grandTotal.getEvidonTotal() + c.getEvidonTotal());
                grandTotal.setIntegralTotal(grandTotal.getIntegralTotal() + c.getIntegralTotal());
            }
        }

        summarySheet.createRow(1).createCell(0).setCellValue("Imps:");
        summarySheet.createRow(2).createCell(0).setCellValue("Clicks:");
        summarySheet.createRow(3).createCell(0).setCellValue("Convs:");
        summarySheet.createRow(4).createCell(0).setCellValue("Media Cost:");
        summarySheet.createRow(5).createCell(0).setCellValue("Network Rev.");
        summarySheet.createRow(6).createCell(0).setCellValue("AdEx Imps:");
        summarySheet.createRow(7).createCell(0).setCellValue("AppNexus Imps:");
        summarySheet.createRow(8).createCell(0).setCellValue("Brilig:");
        summarySheet.createRow(9).createCell(0).setCellValue("Evidon:");
        summarySheet.createRow(10).createCell(0).setCellValue("Integral:");

        summarySheet.getRow(1).createCell(1).setCellValue(grandTotal.getImps());
        summarySheet.getRow(2).createCell(1).setCellValue(grandTotal.getClicks());
        summarySheet.getRow(3).createCell(1).setCellValue(grandTotal.getConvs());
        summarySheet.getRow(4).createCell(1).setCellValue(grandTotal.getMediaCost());
        summarySheet.getRow(5).createCell(1).setCellValue(grandTotal.getNetworkRevenue());
        summarySheet.getRow(6).createCell(1).setCellValue(grandTotal.getAdExImps());
        summarySheet.getRow(7).createCell(1).setCellValue(grandTotal.getAppNexusImps());
        summarySheet.getRow(8).createCell(1).setCellValue(grandTotal.getBriligTotal());
        summarySheet.getRow(9).createCell(1).setCellValue(grandTotal.getEvidonTotal());
        summarySheet.getRow(10).createCell(1).setCellValue(grandTotal.getIntegralTotal());

        summarySheet.getRow(4).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(5).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(8).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(9).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(10).getCell(1).setCellStyle(fullCurrency);

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
            headerRow.createCell(4).setCellValue("Media Cost");
            headerRow.createCell(5).setCellValue("Network Rev.");
            headerRow.createCell(6).setCellValue("AdEx Imps");
            headerRow.createCell(7).setCellValue("AppNexus Imps");
            headerRow.createCell(8).setCellValue("Brilig");
            headerRow.createCell(9).setCellValue("Evidon");
            headerRow.createCell(10).setCellValue("Integral");

            int rowCount = 0;
            BillingCampaign adTotal = new BillingCampaign();

            for (BillingCampaign c : ad.getCampaigns()) {
                Row dataRow = advertiserSheet.createRow(++rowCount);
                dataRow.createCell(0).setCellValue(c.getName() + "(" + c.getId() + ")");
                dataRow.createCell(1).setCellValue(c.getImps());
                dataRow.createCell(2).setCellValue(c.getClicks());
                dataRow.createCell(3).setCellValue(c.getConvs());
                dataRow.createCell(4).setCellValue(c.getMediaCost());
                dataRow.createCell(5).setCellValue(c.getNetworkRevenue());
                dataRow.createCell(6).setCellValue(c.getAdExImps());
                dataRow.createCell(7).setCellValue(c.getAppNexusImps());
                dataRow.createCell(8).setCellValue(c.getBriligTotal());
                dataRow.createCell(9).setCellValue(c.getEvidonTotal());
                dataRow.createCell(10).setCellValue(c.getIntegralTotal());
                
                dataRow.getCell(4).setCellStyle(fullCurrency);
                dataRow.getCell(5).setCellStyle(fullCurrency);
                dataRow.getCell(8).setCellStyle(fullCurrency);
                dataRow.getCell(9).setCellStyle(fullCurrency);
                dataRow.getCell(10).setCellStyle(fullCurrency);
                
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
                
            }
            rowCount++;
            Row totalRow = advertiserSheet.createRow(++rowCount);
            totalRow.createCell(0).setCellValue("Totals:");
            totalRow.createCell(1).setCellValue(adTotal.getImps());
            totalRow.createCell(2).setCellValue(adTotal.getClicks());
            totalRow.createCell(3).setCellValue(adTotal.getConvs());
            totalRow.createCell(4).setCellValue(adTotal.getMediaCost());
            totalRow.createCell(5).setCellValue(adTotal.getNetworkRevenue());
            totalRow.createCell(6).setCellValue(adTotal.getAdExImps());
            totalRow.createCell(7).setCellValue(adTotal.getAppNexusImps());
            totalRow.createCell(8).setCellValue(adTotal.getBriligTotal());
            totalRow.createCell(9).setCellValue(adTotal.getEvidonTotal());
            totalRow.createCell(10).setCellValue(adTotal.getIntegralTotal());

            totalRow.getCell(4).setCellStyle(fullCurrency);
            totalRow.getCell(5).setCellStyle(fullCurrency);
            totalRow.getCell(8).setCellStyle(fullCurrency);
            totalRow.getCell(9).setCellStyle(fullCurrency);
            totalRow.getCell(10).setCellStyle(fullCurrency);
            
            for (int x = 0; x < 11; x++) advertiserSheet.autoSizeColumn(x);
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
