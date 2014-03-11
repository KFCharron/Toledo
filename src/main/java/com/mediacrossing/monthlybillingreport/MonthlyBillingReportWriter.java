package com.mediacrossing.monthlybillingreport;

import com.mediacrossing.creativebillingreport.BillingCreative;
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

    public static void writeReportToFile(ArrayList<BillingAdvertiser> adList,
                                         List<String> feeNames,
                                         String outputPath,
                                         String name) throws IOException {

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
                grandTotal.setMxMediaCost(grandTotal.getMxMediaCost() + c.getMxMediaCost());
                grandTotal.setAdXMediaCost(grandTotal.getAdXMediaCost() + c.getAdXMediaCost());
                grandTotal.setAppNexusMediaCost(grandTotal.getAppNexusMediaCost() + c.getAppNexusMediaCost());
                grandTotal.setLotameImps(grandTotal.getLotameImps() + c.getLotameImps());
            }
        }

        summarySheet.createRow(1).createCell(0).setCellValue("Imps:");
        summarySheet.createRow(2).createCell(0).setCellValue("Clicks:");
        summarySheet.createRow(3).createCell(0).setCellValue("Convs:");
        summarySheet.createRow(4).createCell(0).setCellValue("3rd Party Imps:");
        summarySheet.createRow(5).createCell(0).setCellValue("Billable Imps:");
        summarySheet.createRow(6).createCell(0).setCellValue("CPM:");
        summarySheet.createRow(7).createCell(0).setCellValue("App Nexus Revenue:");
        summarySheet.createRow(8).createCell(0).setCellValue("Billable To Client:");
        summarySheet.createRow(9).createCell(0).setCellValue("Media Cost:");
        summarySheet.createRow(10).createCell(0).setCellValue("AppNexus Media Cost:");
        summarySheet.createRow(11).createCell(0).setCellValue("AppNexus Commission:");
        summarySheet.createRow(12).createCell(0).setCellValue("AppNexus Imps:");
        summarySheet.createRow(13).createCell(0).setCellValue("AppNexus eCPM:");
        summarySheet.createRow(14).createCell(0).setCellValue("MX Media Cost:");
        summarySheet.createRow(15).createCell(0).setCellValue("MX Commission:");
        summarySheet.createRow(16).createCell(0).setCellValue("MX Imps:");
        summarySheet.createRow(17).createCell(0).setCellValue("MX eCPM:");
        summarySheet.createRow(18).createCell(0).setCellValue("AdEx Media Cost:");
        summarySheet.createRow(19).createCell(0).setCellValue("AdEx Commission:");
        summarySheet.createRow(20).createCell(0).setCellValue("AdEx Imps:");
        summarySheet.createRow(21).createCell(0).setCellValue("AdEx eCPM:");
        summarySheet.createRow(22).createCell(0).setCellValue("Brilig Imps:");
        int rowCount = 22;
        for (String n : feeNames) {
            if (n.equals("Lotame")) {
                summarySheet.createRow(++rowCount).createCell(0).setCellValue("Lotame Imps");
                summarySheet.getRow(rowCount).createCell(1).setCellValue(0);
            }
            summarySheet.createRow(++rowCount).createCell(0).setCellValue(n);
            summarySheet.getRow(rowCount).createCell(1).setCellValue(0);
            summarySheet.getRow(rowCount).getCell(1).setCellStyle(fullCurrency);
        }
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("Amazon Cost:");
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("Brilig Cost");
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("Total Cost");
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("Gross Profit");
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("GP Margin");

        summarySheet.getRow(1).createCell(1).setCellValue(grandTotal.getImps());
        summarySheet.getRow(2).createCell(1).setCellValue(grandTotal.getClicks());
        summarySheet.getRow(3).createCell(1).setCellValue(grandTotal.getConvs());
        summarySheet.getRow(7).createCell(1).setCellValue(grandTotal.getNetworkRevenue());
        summarySheet.getRow(9).createCell(1).setCellValue(grandTotal.getMediaCost());
        summarySheet.getRow(10).createCell(1).setCellValue(grandTotal.getAppNexusMediaCost());
        summarySheet.getRow(11).createCell(1).setCellValue(grandTotal.getAnCommission());
        summarySheet.getRow(12).createCell(1).setCellValue(grandTotal.getAppNexusImps());
        summarySheet.getRow(13).createCell(1).setCellValue(grandTotal.getAnCpm());
        summarySheet.getRow(14).createCell(1).setCellValue(grandTotal.getMxMediaCost());
        summarySheet.getRow(15).createCell(1).setCellValue(grandTotal.getMxCommission());
        summarySheet.getRow(16).createCell(1).setCellValue(grandTotal.getMxImps());
        summarySheet.getRow(17).createCell(1).setCellValue(grandTotal.getMxCpm());
        summarySheet.getRow(18).createCell(1).setCellValue(grandTotal.getAdXMediaCost());
        summarySheet.getRow(19).createCell(1).setCellValue(grandTotal.getAdXCommission());
        summarySheet.getRow(20).createCell(1).setCellValue(grandTotal.getAdExImps());
        summarySheet.getRow(21).createCell(1).setCellValue(grandTotal.getAdXCpm());
        summarySheet.getRow(22).createCell(1).setCellValue(grandTotal.getBriligImps());
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
        for (Row r : summarySheet) {
            if (r.getCell(0).getStringCellValue().equals("Lotame Imps")) {
                r.getCell(1).setCellValue(grandTotal.getLotameImps());
            }
        }

        summarySheet.getRow(7).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(9).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(10).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(11).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(13).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(14).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(15).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(17).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(18).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(19).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(21).getCell(1).setCellStyle(fullCurrency);


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
            headerRow.createCell(10).setCellValue("AppNexus Media Cost");
            headerRow.createCell(11).setCellValue("AppNexus Commission");
            headerRow.createCell(12).setCellValue("AppNexus Imps");
            headerRow.createCell(13).setCellValue("AppNexus eCPM");
            headerRow.createCell(14).setCellValue("MX Media Cost");
            headerRow.createCell(15).setCellValue("MX Commission");
            headerRow.createCell(16).setCellValue("MX Imps");
            headerRow.createCell(17).setCellValue("MX eCPM");
            headerRow.createCell(18).setCellValue("AdEx Media Cost");
            headerRow.createCell(19).setCellValue("AdEx Commission");
            headerRow.createCell(20).setCellValue("AdEx Imps");
            headerRow.createCell(21).setCellValue("AdEx eCPM");
            headerRow.createCell(22).setCellValue("Brilig Imps");
            int cellCount = 22;
            for (String n : feeNames) {
                if (n.equals("Lotame")) headerRow.createCell(++cellCount).setCellValue("Lotame Imps");
                headerRow.createCell(++cellCount).setCellValue(n);
            }
            headerRow.createCell(++cellCount).setCellValue("Amazon Cost");
            headerRow.createCell(++cellCount).setCellValue("Brilig Cost");
            headerRow.createCell(++cellCount).setCellValue("Total Cost");
            headerRow.createCell(++cellCount).setCellValue("Gross Profit");
            headerRow.createCell(++cellCount).setCellValue("GP Margin");

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
                dataRow.createCell(10).setCellValue(c.getAppNexusMediaCost()); 
                dataRow.createCell(11).setCellValue(c.getAnCommission());      
                dataRow.createCell(12).setCellValue(c.getAppNexusImps());      
                dataRow.createCell(13).setCellValue(c.getAnCpm());             
                dataRow.createCell(14).setCellValue(c.getMxMediaCost());       
                dataRow.createCell(15).setCellValue(c.getMxCommission());      
                dataRow.createCell(16).setCellValue(c.getMxImps());            
                dataRow.createCell(17).setCellValue(c.getMxCpm());             
                dataRow.createCell(18).setCellValue(c.getAdXMediaCost());      
                dataRow.createCell(19).setCellValue(c.getAdXCommission());     
                dataRow.createCell(20).setCellValue(c.getAdExImps());          
                dataRow.createCell(21).setCellValue(c.getAdXCpm());            
                dataRow.createCell(22).setCellValue(c.getBriligImps());        
                cellCount = 22;
                for (String n : feeNames) {
                    if (n.equals("Lotame")) {
                        dataRow.createCell(++cellCount).setCellValue(c.getLotameImps());
                    }
                    dataRow.createCell(++cellCount).setCellValue(0);
                    dataRow.getCell(cellCount).setCellStyle(fullCurrency);
                }
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
                    boolean saved = false;
                    for (ServingFee adSf : adTotal.getServingFees()) {
                        if (adSf.getBrokerName().equals(f.getBrokerName())) {
                            adSf.setTotalFee(adSf.getTotalFee() + f.getTotalFee());
                            saved = true;
                        }
                    }
                    if (!saved) adTotal.getServingFees().add(f);
                }

                dataRow.getCell(7).setCellStyle(fullCurrency);
                dataRow.getCell(9).setCellStyle(fullCurrency);
                dataRow.getCell(10).setCellStyle(fullCurrency);
                dataRow.getCell(11).setCellStyle(fullCurrency);
                dataRow.getCell(13).setCellStyle(fullCurrency);
                dataRow.getCell(14).setCellStyle(fullCurrency);
                dataRow.getCell(15).setCellStyle(fullCurrency);
                dataRow.getCell(16).setCellStyle(fullCurrency);
                dataRow.getCell(18).setCellStyle(fullCurrency);
                dataRow.getCell(19).setCellStyle(fullCurrency);
                dataRow.getCell(21).setCellStyle(fullCurrency);

                adTotal.setImps(adTotal.getImps() + c.getImps());
                adTotal.setClicks(adTotal.getClicks() + c.getClicks());
                adTotal.setConvs(adTotal.getConvs() + c.getConvs());
                adTotal.setMediaCost(adTotal.getMediaCost() + c.getMediaCost());
                adTotal.setNetworkRevenue(adTotal.getNetworkRevenue() + c.getNetworkRevenue());
                adTotal.setAdExImps(adTotal.getAdExImps() + c.getAdExImps());
                adTotal.setMxImps(adTotal.getMxImps() + c.getMxImps());
                adTotal.setAppNexusImps(adTotal.getAppNexusImps() + c.getAppNexusImps());
                adTotal.setBriligImps(adTotal.getBriligImps() + c.getBriligImps());
                adTotal.setAdXMediaCost(adTotal.getAdXMediaCost() + c.getAdXMediaCost());
                adTotal.setMxMediaCost(adTotal.getMxMediaCost() + c.getMxMediaCost());
                adTotal.setAppNexusMediaCost(adTotal.getAppNexusMediaCost() + c.getAppNexusMediaCost());
                adTotal.setLotameImps(adTotal.getLotameImps() + c.getLotameImps());
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
            totalRow.createCell(10).setCellValue(adTotal.getAppNexusMediaCost());
            totalRow.createCell(11).setCellValue(adTotal.getAnCommission());     
            totalRow.createCell(12).setCellValue(adTotal.getAppNexusImps());     
            totalRow.createCell(13).setCellValue(adTotal.getAnCpm());            
            totalRow.createCell(14).setCellValue(adTotal.getMxMediaCost());      
            totalRow.createCell(15).setCellValue(adTotal.getMxCommission());     
            totalRow.createCell(16).setCellValue(adTotal.getMxImps());           
            totalRow.createCell(17).setCellValue(adTotal.getMxCpm());            
            totalRow.createCell(18).setCellValue(adTotal.getAdXMediaCost());     
            totalRow.createCell(19).setCellValue(adTotal.getAdXCommission());    
            totalRow.createCell(20).setCellValue(adTotal.getAdExImps());         
            totalRow.createCell(21).setCellValue(adTotal.getAdXCpm());           
            totalRow.createCell(22).setCellValue(adTotal.getBriligImps());       
            cellCount = 22;
            for (String n : feeNames) {
                if (n.equals("Lotame")) totalRow.createCell(++cellCount).setCellValue(adTotal.getLotameImps());
                totalRow.createCell(++cellCount).setCellValue(0);
                for (Row r : advertiserSheet) {
                    for (Cell c : r) {
                        if (r.getRowNum() != totalRow.getRowNum()) {
                            if (c.getColumnIndex() == cellCount && c.getColumnIndex() != 0 && c.getRowIndex() != 0) {
                                totalRow.getCell(cellCount)
                                        .setCellValue(c.getNumericCellValue() +
                                                totalRow.getCell(cellCount).getNumericCellValue());
                            }
                        }
                    }
                }
                totalRow.getCell(cellCount).setCellStyle(fullCurrency);
            }

            totalRow.getCell(7).setCellStyle(fullCurrency);
            totalRow.getCell(9).setCellStyle(fullCurrency);
            totalRow.getCell(10).setCellStyle(fullCurrency);
            totalRow.getCell(11).setCellStyle(fullCurrency);
            totalRow.getCell(13).setCellStyle(fullCurrency);
            totalRow.getCell(14).setCellStyle(fullCurrency);
            totalRow.getCell(15).setCellStyle(fullCurrency);
            totalRow.getCell(17).setCellStyle(fullCurrency);
            totalRow.getCell(18).setCellStyle(fullCurrency);
            totalRow.getCell(19).setCellStyle(fullCurrency);
            totalRow.getCell(21).setCellStyle(fullCurrency);

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
        summaryHeader.createCell(10).setCellValue("AppNexus Media Cost");
        summaryHeader.createCell(11).setCellValue("AppNexus Commission");
        summaryHeader.createCell(12).setCellValue("AppNexus Imps");      
        summaryHeader.createCell(13).setCellValue("AppNexus eCPM");      
        summaryHeader.createCell(14).setCellValue("MX Media Cost");      
        summaryHeader.createCell(15).setCellValue("MX Commission");      
        summaryHeader.createCell(16).setCellValue("MX Imps");            
        summaryHeader.createCell(17).setCellValue("MX eCPM");            
        summaryHeader.createCell(18).setCellValue("AdEx Media Cost");    
        summaryHeader.createCell(19).setCellValue("AdEx Commission");    
        summaryHeader.createCell(20).setCellValue("AdEx Imps");          
        summaryHeader.createCell(21).setCellValue("AdEx eCPM");          
        summaryHeader.createCell(22).setCellValue("Brilig Imps");        
        int cellCount = 22;
        for (String n : feeNames) {
            if (n.equals("Lotame")) summaryHeader.createCell(++cellCount).setCellValue("Lotame Imps");
            summaryHeader.createCell(++cellCount).setCellValue(n);
        }
        summaryHeader.createCell(++cellCount).setCellValue("Amazon Cost");
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
                adRow.createCell(10).setCellValue(c.getAppNexusMediaCost()); 
                adRow.createCell(11).setCellValue(c.getAnCommission());      
                adRow.createCell(12).setCellValue(c.getAppNexusImps());      
                adRow.createCell(13).setCellValue(c.getAnCpm());             
                adRow.createCell(14).setCellValue(c.getMxMediaCost());       
                adRow.createCell(15).setCellValue(c.getMxCommission());      
                adRow.createCell(16).setCellValue(c.getMxImps());            
                adRow.createCell(17).setCellValue(c.getMxCpm());             
                adRow.createCell(18).setCellValue(c.getAdXMediaCost());      
                adRow.createCell(19).setCellValue(c.getAdXCommission());     
                adRow.createCell(20).setCellValue(c.getAdExImps());          
                adRow.createCell(21).setCellValue(c.getAdXCpm());            
                adRow.createCell(22).setCellValue(c.getBriligImps());        
                for (Cell cell : summaryHeader) {
                    if (cell.getStringCellValue().equals("Lotame Imps")) {
                        adRow.createCell(cell.getColumnIndex()).setCellValue(c.getLotameImps());
                    }
                    for (ServingFee f : c.getServingFees()) {
                        if (f.getBrokerName().equals(cell.getStringCellValue())) {
                            adRow.createCell(cell.getColumnIndex()).setCellValue(f.getTotalFee());
                            adRow.getCell(cell.getColumnIndex()).setCellStyle(fullCurrency);
                        }
                    }
                }

                adRow.getCell(7).setCellStyle(fullCurrency);
                adRow.getCell(9).setCellStyle(fullCurrency);
                adRow.getCell(10).setCellStyle(fullCurrency);
                adRow.getCell(11).setCellStyle(fullCurrency);
                adRow.getCell(13).setCellStyle(fullCurrency);
                adRow.getCell(14).setCellStyle(fullCurrency);
                adRow.getCell(15).setCellStyle(fullCurrency);
                adRow.getCell(17).setCellStyle(fullCurrency);
                adRow.getCell(18).setCellStyle(fullCurrency);
                adRow.getCell(19).setCellStyle(fullCurrency);
                adRow.getCell(21).setCellStyle(fullCurrency);

                rowCount++;
            }
        }
        Row totalAd = advertiserSummary.createRow(++rowCount);
        for (Cell c : summaryHeader) totalAd.createCell(c.getColumnIndex()).setCellValue(0);
        totalAd.getCell(7).setCellStyle(fullCurrency);
        totalAd.getCell(9).setCellStyle(fullCurrency);
        totalAd.getCell(10).setCellStyle(fullCurrency);
        totalAd.getCell(11).setCellStyle(fullCurrency);
        totalAd.getCell(13).setCellStyle(fullCurrency);
        totalAd.getCell(14).setCellStyle(fullCurrency);
        totalAd.getCell(15).setCellStyle(fullCurrency);
        totalAd.getCell(17).setCellStyle(fullCurrency);
        totalAd.getCell(18).setCellStyle(fullCurrency);
        totalAd.getCell(19).setCellStyle(fullCurrency);
        totalAd.getCell(21).setCellStyle(fullCurrency);
        for (Cell c : summaryHeader) {
            if (c.getColumnIndex() > 21 && !summaryHeader.getCell(c.getColumnIndex()).getStringCellValue().equals("Lotame Imps")) {
                totalAd.getCell(c.getColumnIndex()).setCellStyle(fullCurrency);
            }
        }
        totalAd.getCell(0).setCellValue("Total:");
        for (Row r : advertiserSummary) {
            for (Cell c : r) {
                if (c.getColumnIndex() != 0 && c.getRowIndex() != totalAd.getRowNum() && c.getRowIndex() != 0 &&
                        !advertiserSummary.getRow(0).getCell(c.getColumnIndex()).getStringCellValue().contains("eCPM")) {
                    totalAd.getCell(c.getColumnIndex())
                            .setCellValue(totalAd.getCell(c.getColumnIndex()).getNumericCellValue() +
                                    c.getNumericCellValue());
                }
            }
        }

        for (int x = 0; x <= 50; x++) advertiserSummary.autoSizeColumn(x);


        //Export file
        LocalDate now = new LocalDate(DateTimeZone.UTC);
        FileOutputStream fileOut =
                new FileOutputStream(new File(outputPath, name+"BillingReport_"
                        +now.toString()+".xls"));
        wb.write(fileOut);
        fileOut.close();
    }

    public static void writeCreativeReport(ArrayList<BillingCreative> bc,
                                            ArrayList<BillingAdvertiser> ads,
                                            String outPath) throws IOException
    {

        //Create wb
        Workbook wb = new HSSFWorkbook();
        DataFormat df = wb.createDataFormat();
        CellStyle fullCurrency = wb.createCellStyle();
        fullCurrency.setDataFormat(df.getFormat("$#,##0.00"));
        Sheet sheet = wb.createSheet("Creative Billing Report");
        CellStyle number = wb.createCellStyle();
        number.setDataFormat(df.getFormat("###,###,###"));

        Row head = sheet.createRow(0);
        head.createCell(0).setCellValue("ID");
        head.createCell(1).setCellValue("Creative");
        head.createCell(2).setCellValue("Imps");
        head.createCell(3).setCellValue("Clicks");
        head.createCell(4).setCellValue("Convs");
        head.createCell(5).setCellValue("3rd Party Imps");
        head.createCell(6).setCellValue("Billable Imps");
        head.createCell(7).setCellValue("CPM");
        head.createCell(8).setCellValue("App Nexus Rev.");
        head.createCell(9).setCellValue("Billable To Client");
        head.createCell(10).setCellValue("Media Cost");
        head.createCell(11).setCellValue("AdEx Imps");
        head.createCell(12).setCellValue("MX Imps");
        head.createCell(13).setCellValue("AppNexus Imps");
        head.createCell(14).setCellValue("Brilig Imps");
        head.createCell(15).setCellValue("ALC");
        head.createCell(16).setCellValue("Evidon");
        head.createCell(17).setCellValue("BlueKai");
        head.createCell(18).setCellValue("Grapeshot");
        head.createCell(19).setCellValue("Integral");
        head.createCell(20).setCellValue("Peer39");
        head.createCell(21).setCellValue("Spongecell");
        head.createCell(22).setCellValue("Vidible");
        head.createCell(23).setCellValue("Amazon Cost");
        head.createCell(24).setCellValue("AdX Cost");
        head.createCell(25).setCellValue("Brilig Cost");
        head.createCell(26).setCellValue("Total Cost");
        head.createCell(27).setCellValue("Gross Profit");
        head.createCell(28).setCellValue("GP Margin");

        BillingCreative total = new BillingCreative();
        int rowCount = 1;
        for(BillingCreative b: bc) {
            Row dataRow = sheet.createRow(rowCount);
            dataRow.createCell(0).setCellValue(b.getCreativeId());
            dataRow.createCell(1).setCellValue(b.getCreativeName());
            dataRow.createCell(2).setCellValue(b.getImps());
            dataRow.createCell(3).setCellValue(b.getClicks());
            dataRow.createCell(4).setCellValue(b.getConvs());
            dataRow.createCell(8).setCellValue(b.getNetworkRevenue());
            dataRow.createCell(10).setCellValue(b.getMediaCost());
            dataRow.createCell(11).setCellValue(b.getAdExImps());
            dataRow.createCell(12).setCellValue(b.getMxImps());
            dataRow.createCell(13).setCellValue(b.getAppNexusImps());
            dataRow.createCell(14).setCellValue(b.getBriligImps());
            //TODO

            dataRow.getCell(2).setCellStyle(number);
            dataRow.getCell(3).setCellStyle(number);
            dataRow.getCell(4).setCellStyle(number);
            dataRow.getCell(8).setCellStyle(fullCurrency);
            dataRow.getCell(10).setCellStyle(fullCurrency);
            dataRow.getCell(11).setCellStyle(number);
            dataRow.getCell(12).setCellStyle(number);
            dataRow.getCell(13).setCellStyle(number);
            dataRow.getCell(14).setCellStyle(number);
            //TODO

            total.setImps(total.getImps() + b.getImps());
            total.setClicks(total.getClicks() + b.getClicks());
            total.setConvs(total.getConvs() + b.getConvs());
            total.setMediaCost(total.getMediaCost() + b.getMediaCost());
            total.setNetworkRevenue(total.getNetworkRevenue() + b.getNetworkRevenue());
            total.setAdExImps(total.getAdExImps() + b.getAdExImps());
            total.setMxImps(total.getMxImps() + b.getMxImps());
            total.setAppNexusImps(total.getAppNexusImps() + b.getAppNexusImps());
            total.setBriligImps(total.getBriligImps() + b.getBriligImps());
            //TODO

            rowCount++;
        }
        Row totalRow = sheet.createRow(++rowCount);
        totalRow.createCell(0).setCellValue("Totals:");
        totalRow.createCell(2).setCellValue(total.getImps());
        totalRow.createCell(3).setCellValue(total.getClicks());
        totalRow.createCell(4).setCellValue(total.getConvs());
        totalRow.createCell(8).setCellValue(total.getNetworkRevenue());
        totalRow.createCell(10).setCellValue(total.getMediaCost());
        totalRow.createCell(11).setCellValue(total.getAdExImps());
        totalRow.createCell(12).setCellValue(total.getMxImps());
        totalRow.createCell(13).setCellValue(total.getAppNexusImps());
        totalRow.createCell(14).setCellValue(total.getBriligImps());
        //TODO

        totalRow.getCell(2).setCellStyle(number);
        totalRow.getCell(3).setCellStyle(number);
        totalRow.getCell(4).setCellStyle(number);
        totalRow.getCell(8).setCellStyle(fullCurrency);
        totalRow.getCell(10).setCellStyle(fullCurrency);
        totalRow.getCell(11).setCellStyle(number);
        totalRow.getCell(12).setCellStyle(number);
        totalRow.getCell(13).setCellStyle(number);
        totalRow.getCell(14).setCellStyle(number);
        //TODO

        for (Cell c : head) sheet.autoSizeColumn(c.getColumnIndex());

        for (BillingAdvertiser ad : ads) {
            Sheet adSheet = wb.createSheet(ad.getName());
            Row header = adSheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Creative");
            header.createCell(2).setCellValue("Imps");
            header.createCell(3).setCellValue("Clicks");
            header.createCell(4).setCellValue("Convs");
            header.createCell(5).setCellValue("3rd Party Imps");
            header.createCell(6).setCellValue("Billable Imps");
            header.createCell(7).setCellValue("CPM");
            header.createCell(8).setCellValue("App Nexus Rev.");
            header.createCell(9).setCellValue("Billable To Client");
            header.createCell(10).setCellValue("Media Cost");
            header.createCell(11).setCellValue("AdEx Imps");
            header.createCell(12).setCellValue("MX Imps");
            header.createCell(13).setCellValue("AppNexus Imps");
            header.createCell(14).setCellValue("Brilig Imps");
            //TODO
            header.createCell(23).setCellValue("Amazon Cost");
            header.createCell(24).setCellValue("AdX Cost");
            header.createCell(25).setCellValue("Brilig Cost");
            header.createCell(26).setCellValue("Total Cost");
            header.createCell(27).setCellValue("Gross Profit");
            header.createCell(28).setCellValue("GP Margin");

            BillingCreative adTotal = new BillingCreative();
            int count = 1;
            for (BillingCreative c : bc) {
                if (c.getAdId().equals(ad.getId())) {
                    Row d = adSheet.createRow(count);
                    d.createCell(0).setCellValue(c.getCreativeId());
                    d.createCell(1).setCellValue(c.getCreativeName());
                    d.createCell(2).setCellValue(c.getImps());
                    d.createCell(3).setCellValue(c.getClicks());
                    d.createCell(4).setCellValue(c.getConvs());
                    d.createCell(8).setCellValue(c.getNetworkRevenue());
                    d.createCell(10).setCellValue(c.getMediaCost());
                    d.createCell(11).setCellValue(c.getAdExImps());
                    d.createCell(12).setCellValue(c.getMxImps());
                    d.createCell(13).setCellValue(c.getAppNexusImps());
                    d.createCell(14).setCellValue(c.getBriligImps());
                    //TODO

                    d.getCell(2).setCellStyle(number);
                    d.getCell(3).setCellStyle(number);
                    d.getCell(4).setCellStyle(number);
                    d.getCell(8).setCellStyle(fullCurrency);
                    d.getCell(10).setCellStyle(fullCurrency);
                    d.getCell(11).setCellStyle(number);
                    d.getCell(12).setCellStyle(number);
                    d.getCell(13).setCellStyle(number);
                    d.getCell(14).setCellStyle(number);
                    //TODO

                    adTotal.setImps(adTotal.getImps() + c.getImps());
                    adTotal.setClicks(adTotal.getClicks() + c.getClicks());
                    adTotal.setConvs(adTotal.getConvs() + c.getConvs());
                    adTotal.setMediaCost(adTotal.getMediaCost() + c.getMediaCost());
                    adTotal.setNetworkRevenue(adTotal.getNetworkRevenue() + c.getNetworkRevenue());
                    adTotal.setAdExImps(adTotal.getAdExImps() + c.getAdExImps());
                    adTotal.setMxImps(adTotal.getMxImps() + c.getMxImps());
                    adTotal.setAppNexusImps(adTotal.getAppNexusImps() + c.getAppNexusImps());
                    adTotal.setBriligImps(adTotal.getBriligImps() + c.getBriligImps());
                    //TODO

                    count++;
                }
            }
            Row adTotalRow = adSheet.createRow(count+1);
            adTotalRow.createCell(0).setCellValue("Totals:");
            adTotalRow.createCell(2).setCellValue(adTotal.getImps());
            adTotalRow.createCell(3).setCellValue(adTotal.getClicks());
            adTotalRow.createCell(4).setCellValue(adTotal.getConvs());
            adTotalRow.createCell(8).setCellValue(adTotal.getNetworkRevenue());
            adTotalRow.createCell(10).setCellValue(adTotal.getMediaCost());
            adTotalRow.createCell(11).setCellValue(adTotal.getAdExImps());
            adTotalRow.createCell(12).setCellValue(adTotal.getMxImps());
            adTotalRow.createCell(13).setCellValue(adTotal.getAppNexusImps());
            adTotalRow.createCell(14).setCellValue(adTotal.getBriligImps());
            //TODO

            adTotalRow.getCell(2).setCellStyle(number);
            adTotalRow.getCell(3).setCellStyle(number);
            adTotalRow.getCell(4).setCellStyle(number);
            adTotalRow.getCell(8).setCellStyle(fullCurrency);
            adTotalRow.getCell(10).setCellStyle(fullCurrency);
            adTotalRow.getCell(11).setCellStyle(number);
            adTotalRow.getCell(12).setCellStyle(number);
            adTotalRow.getCell(13).setCellStyle(number);
            adTotalRow.getCell(14).setCellStyle(number);
            adTotalRow.getCell(15).setCellStyle(fullCurrency);
            adTotalRow.getCell(16).setCellStyle(fullCurrency);
            adTotalRow.getCell(17).setCellStyle(fullCurrency);
            adTotalRow.getCell(18).setCellStyle(fullCurrency);
            adTotalRow.getCell(19).setCellStyle(fullCurrency);
            adTotalRow.getCell(20).setCellStyle(fullCurrency);
            adTotalRow.getCell(21).setCellStyle(fullCurrency);
            adTotalRow.getCell(22).setCellStyle(fullCurrency);

            for (Cell c : header) adSheet.autoSizeColumn(c.getColumnIndex());
        }

        //Export file
        LocalDate now = new LocalDate(DateTimeZone.UTC);
        FileOutputStream fileOut =
                new FileOutputStream(new File(outPath, "CreativeBillingReport_"
                        +now.toString()+".xls"));
        wb.write(fileOut);
        fileOut.close();


    }
}
