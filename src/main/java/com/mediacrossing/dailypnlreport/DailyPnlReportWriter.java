package com.mediacrossing.dailypnlreport;

import com.mediacrossing.creativebillingreport.BillingCreative;
import com.mediacrossing.dailycheckupsreport.ServingFee;
import com.mediacrossing.monthlybillingreport.BillingAdvertiser;
import com.mediacrossing.monthlybillingreport.BillingCampaign;
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

public class DailyPnlReportWriter {

    public static void writeReportToFile(ArrayList<BillingAdvertiser> adList, List<String> feeNames, String outputPath)
            throws IOException {

        //Create wb
        Workbook wb = new HSSFWorkbook();
        DataFormat df = wb.createDataFormat();
        CellStyle fullCurrency = wb.createCellStyle();
        fullCurrency.setDataFormat(df.getFormat("$#,##0.00"));

        /*GLOBAL COST VARIABLES*/
        float amazonCost = 0.05f;
        float briligCost = 0.50f;
        float discoveryCommission = 0.20f;

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
        summarySheet.createRow(11).createCell(0).setCellValue("AdEx Media Cost:");
        summarySheet.createRow(12).createCell(0).setCellValue("MX Media Cost:");
        summarySheet.createRow(13).createCell(0).setCellValue("AppNexus Commission:");
        summarySheet.createRow(14).createCell(0).setCellValue("AdEx Commission:");
        summarySheet.createRow(15).createCell(0).setCellValue("MX Commission:");
        summarySheet.createRow(16).createCell(0).setCellValue("AppNexus Imps:");
        summarySheet.createRow(17).createCell(0).setCellValue("AdEx Imps:");
        summarySheet.createRow(18).createCell(0).setCellValue("MX Imps:");
        summarySheet.createRow(19).createCell(0).setCellValue("AppNexus eCPM:");
        summarySheet.createRow(20).createCell(0).setCellValue("AdEx eCPM:");
        summarySheet.createRow(21).createCell(0).setCellValue("MX eCPM:");
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
        summarySheet.getRow(rowCount).createCell(1).setCellValue((float)grandTotal.getImps()/1000 * amazonCost);
        summarySheet.getRow(rowCount).getCell(1).setCellStyle(fullCurrency);
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("AdEx Cost:");
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("Brilig Cost:");
        summarySheet.getRow(rowCount).createCell(1).setCellValue((float)grandTotal.getBriligImps()/1000 * briligCost);
        summarySheet.getRow(rowCount).getCell(1).setCellStyle(fullCurrency);
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("Total Cost:");
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("Gross Profit:");
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("GP Margin:");

        summarySheet.getRow(1).createCell(1).setCellValue(grandTotal.getImps());
        summarySheet.getRow(2).createCell(1).setCellValue(grandTotal.getClicks());
        summarySheet.getRow(3).createCell(1).setCellValue(grandTotal.getConvs());
        summarySheet.getRow(7).createCell(1).setCellValue(grandTotal.getNetworkRevenue());
        summarySheet.getRow(9).createCell(1).setCellValue(grandTotal.getMediaCost());
        summarySheet.getRow(10).createCell(1).setCellValue(grandTotal.getAppNexusMediaCost());
        summarySheet.getRow(11).createCell(1).setCellValue(grandTotal.getAdXMediaCost());
        summarySheet.getRow(12).createCell(1).setCellValue(grandTotal.getMxMediaCost());
        summarySheet.getRow(13).createCell(1).setCellValue(grandTotal.getAnCommission());
        summarySheet.getRow(14).createCell(1).setCellValue(grandTotal.getAdXCommission());
        summarySheet.getRow(15).createCell(1).setCellValue(grandTotal.getMxCommission());
        summarySheet.getRow(16).createCell(1).setCellValue(grandTotal.getAppNexusImps());
        summarySheet.getRow(17).createCell(1).setCellValue(grandTotal.getAdExImps());
        summarySheet.getRow(18).createCell(1).setCellValue(grandTotal.getMxImps());
        summarySheet.getRow(19).createCell(1).setCellValue(grandTotal.getAnCpm());
        summarySheet.getRow(20).createCell(1).setCellValue(grandTotal.getAdXCpm());
        summarySheet.getRow(21).createCell(1).setCellValue(grandTotal.getMxCpm());
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
        summarySheet.getRow(12).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(13).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(14).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(15).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(19).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(20).getCell(1).setCellStyle(fullCurrency);
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
            headerRow.createCell(11).setCellValue("AdEx Media Cost");
            headerRow.createCell(12).setCellValue("MX Media Cost");
            headerRow.createCell(13).setCellValue("AppNexus Commission");
            headerRow.createCell(14).setCellValue("AdEx Commission");
            headerRow.createCell(15).setCellValue("MX Commission");
            headerRow.createCell(16).setCellValue("AppNexus Imps");
            headerRow.createCell(17).setCellValue("AdEx Imps");
            headerRow.createCell(18).setCellValue("MX Imps");
            headerRow.createCell(19).setCellValue("AppNexus eCPM");
            headerRow.createCell(20).setCellValue("AdEx eCPM");
            headerRow.createCell(21).setCellValue("MX eCPM");
            headerRow.createCell(22).setCellValue("Brilig Imps");
            int cellCount = 22;
            for (String n : feeNames) {
                if (n.equals("Lotame")) headerRow.createCell(++cellCount).setCellValue("Lotame Imps");
                headerRow.createCell(++cellCount).setCellValue(n);
            }
            headerRow.createCell(++cellCount).setCellValue("Amazon Cost");
            headerRow.createCell(++cellCount).setCellValue("AdX Cost");
            headerRow.createCell(++cellCount).setCellValue("Brilig Cost");
            headerRow.createCell(++cellCount).setCellValue("Total Cost");
            headerRow.createCell(++cellCount).setCellValue("Gross Profit");
            headerRow.createCell(++cellCount).setCellValue("GP Margin");
            if (ad.getId().equals("242222")) {
                headerRow.createCell(++cellCount).setCellValue("Discovery Commission");
            }


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
                dataRow.createCell(11).setCellValue(c.getAdXMediaCost());
                dataRow.createCell(12).setCellValue(c.getMxMediaCost());
                dataRow.createCell(13).setCellValue(c.getAnCommission());
                dataRow.createCell(14).setCellValue(c.getAdXCommission());
                dataRow.createCell(15).setCellValue(c.getMxCommission());
                dataRow.createCell(16).setCellValue(c.getAppNexusImps());
                dataRow.createCell(17).setCellValue(c.getAdExImps());
                dataRow.createCell(18).setCellValue(c.getMxImps());
                dataRow.createCell(19).setCellValue(c.getAnCpm());
                dataRow.createCell(20).setCellValue(c.getAdXCpm());
                dataRow.createCell(21).setCellValue(c.getMxCpm());
                dataRow.createCell(22).setCellValue(c.getBriligImps());
                cellCount = 22;
                for (String n : feeNames) {
                    if (n.equals("Lotame")) {
                        dataRow.createCell(++cellCount).setCellValue(c.getLotameImps());
                    }
                    dataRow.createCell(++cellCount).setCellValue(0);
                    dataRow.getCell(cellCount).setCellStyle(fullCurrency);
                }
                dataRow.createCell(++cellCount).setCellValue((float)c.getImps()/1000 * amazonCost);
                dataRow.getCell(cellCount).setCellStyle(fullCurrency);
                dataRow.createCell(++cellCount);
                dataRow.createCell(++cellCount).setCellValue((float)c.getBriligImps()/1000 * briligCost);
                dataRow.getCell(cellCount).setCellStyle(fullCurrency);
                dataRow.createCell(++cellCount);
                dataRow.createCell(++cellCount);
                dataRow.createCell(++cellCount);
                if (ad.getId().equals("242222")) {
                    dataRow.createCell(++cellCount).setCellValue(discoveryCommission * c.getNetworkRevenue());
                    dataRow.getCell(cellCount).setCellStyle(fullCurrency);
                }

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
                dataRow.getCell(12).setCellStyle(fullCurrency);
                dataRow.getCell(13).setCellStyle(fullCurrency);
                dataRow.getCell(14).setCellStyle(fullCurrency);
                dataRow.getCell(15).setCellStyle(fullCurrency);
                dataRow.getCell(19).setCellStyle(fullCurrency);
                dataRow.getCell(20).setCellStyle(fullCurrency);
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
            totalRow.createCell(10).setCellValue(adTotal.getAdExImps());
            totalRow.createCell(11).setCellValue(adTotal.getAdXMediaCost());
            totalRow.createCell(12).setCellValue(adTotal.getMxMediaCost());
            totalRow.createCell(13).setCellValue(adTotal.getAnCommission());
            totalRow.createCell(14).setCellValue(adTotal.getAdXCommission());
            totalRow.createCell(15).setCellValue(adTotal.getMxCommission());
            totalRow.createCell(16).setCellValue(adTotal.getAppNexusImps());
            totalRow.createCell(17).setCellValue(adTotal.getAdExImps());
            totalRow.createCell(18).setCellValue(adTotal.getMxImps());
            totalRow.createCell(19).setCellValue(adTotal.getAnCpm());
            totalRow.createCell(20).setCellValue(adTotal.getAdXCpm());
            totalRow.createCell(21).setCellValue(adTotal.getMxCpm());
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
            totalRow.createCell(++cellCount).setCellValue((float)adTotal.getImps()/1000 * amazonCost);
            totalRow.getCell(cellCount).setCellStyle(fullCurrency);
            cellCount++;
            totalRow.createCell(++cellCount).setCellValue((float)adTotal.getBriligImps()/1000 * briligCost);
            totalRow.getCell(cellCount).setCellStyle(fullCurrency);
            cellCount++;
            cellCount++;
            cellCount++;
            if (ad.getId().equals("242222")) {
                totalRow.createCell(++cellCount).setCellValue(adTotal.getNetworkRevenue() * discoveryCommission);
                totalRow.getCell(cellCount).setCellStyle(fullCurrency);
            }

            totalRow.getCell(7).setCellStyle(fullCurrency);
            totalRow.getCell(9).setCellStyle(fullCurrency);
            totalRow.getCell(10).setCellStyle(fullCurrency);
            totalRow.getCell(11).setCellStyle(fullCurrency);
            totalRow.getCell(12).setCellStyle(fullCurrency);
            totalRow.getCell(13).setCellStyle(fullCurrency);
            totalRow.getCell(14).setCellStyle(fullCurrency);
            totalRow.getCell(15).setCellStyle(fullCurrency);
            totalRow.getCell(19).setCellStyle(fullCurrency);
            totalRow.getCell(20).setCellStyle(fullCurrency);
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
        summaryHeader.createCell(11).setCellValue("AdEx Media Cost");
        summaryHeader.createCell(12).setCellValue("MX Media Cost");
        summaryHeader.createCell(13).setCellValue("AppNexus Commission");
        summaryHeader.createCell(14).setCellValue("AdEx Commission");
        summaryHeader.createCell(15).setCellValue("MX Commission");
        summaryHeader.createCell(16).setCellValue("AppNexus Imps");
        summaryHeader.createCell(17).setCellValue("AdEx Imps");
        summaryHeader.createCell(18).setCellValue("MX Imps");
        summaryHeader.createCell(19).setCellValue("AppNexus eCPM");
        summaryHeader.createCell(20).setCellValue("AdEx eCPM");
        summaryHeader.createCell(21).setCellValue("MX eCPM");
        summaryHeader.createCell(22).setCellValue("Brilig Imps");
        int cellCount = 22;
        for (String n : feeNames) {
            if (n.equals("Lotame")) summaryHeader.createCell(++cellCount).setCellValue("Lotame Imps");
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
                adRow.createCell(11).setCellValue(c.getAdXMediaCost());
                adRow.createCell(12).setCellValue(c.getMxMediaCost());
                adRow.createCell(13).setCellValue(c.getAnCommission());
                adRow.createCell(14).setCellValue(c.getAdXCommission());
                adRow.createCell(15).setCellValue(c.getMxCommission());
                adRow.createCell(16).setCellValue(c.getAppNexusImps());
                adRow.createCell(17).setCellValue(c.getAdExImps());
                adRow.createCell(18).setCellValue(c.getMxImps());
                adRow.createCell(19).setCellValue(c.getAnCpm());
                adRow.createCell(20).setCellValue(c.getAdXCpm());
                adRow.createCell(21).setCellValue(c.getMxCpm());
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
                    if (cell.getStringCellValue().equals("Amazon Cost")) {
                        adRow.createCell(cell.getColumnIndex()).setCellValue((float)c.getImps()/1000 * amazonCost);
                        adRow.getCell(cell.getColumnIndex()).setCellStyle(fullCurrency);
                    }
                    if (cell.getStringCellValue().equals("Brilig Cost")) {
                        adRow.createCell(cell.getColumnIndex()).setCellValue((float)c.getBriligImps()/1000 * briligCost);
                        adRow.getCell(cell.getColumnIndex()).setCellStyle(fullCurrency);
                    }
                }

                adRow.getCell(7).setCellStyle(fullCurrency);
                adRow.getCell(9).setCellStyle(fullCurrency);
                adRow.getCell(10).setCellStyle(fullCurrency);
                adRow.getCell(11).setCellStyle(fullCurrency);
                adRow.getCell(12).setCellStyle(fullCurrency);
                adRow.getCell(13).setCellStyle(fullCurrency);
                adRow.getCell(14).setCellStyle(fullCurrency);
                adRow.getCell(15).setCellStyle(fullCurrency);
                adRow.getCell(19).setCellStyle(fullCurrency);
                adRow.getCell(20).setCellStyle(fullCurrency);
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
        totalAd.getCell(12).setCellStyle(fullCurrency);
        totalAd.getCell(13).setCellStyle(fullCurrency);
        totalAd.getCell(14).setCellStyle(fullCurrency);
        totalAd.getCell(15).setCellStyle(fullCurrency);
        totalAd.getCell(19).setCellStyle(fullCurrency);
        totalAd.getCell(20).setCellStyle(fullCurrency);
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
                new FileOutputStream(new File(outputPath, "Daily_PnL_"
                        +now.toString()+".xls"));
        wb.write(fileOut);
        fileOut.close();
    }
}
