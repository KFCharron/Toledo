package com.mediacrossing.dailypnlreport;

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

        CellStyle pattern = wb.createCellStyle();
        pattern.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        pattern.setFillPattern(CellStyle.SOLID_FOREGROUND);
        pattern.setDataFormat(df.getFormat("$#,##0.00"));

        CellStyle patternImp = wb.createCellStyle();
        patternImp.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        patternImp.setFillPattern(CellStyle.SOLID_FOREGROUND);
        
        CellStyle percentage = wb.createCellStyle();
        percentage.setDataFormat(df.getFormat("00.00%"));

        /*GLOBAL COST VARIABLES*/
        float amazonCost = 0.05f;
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
                grandTotal.setBlueKaiImps(grandTotal.getBlueKaiImps() + c.getBlueKaiImps());
            }
        }

        summarySheet.createRow(1).createCell(0).setCellValue("Imps:");
        summarySheet.createRow(2).createCell(0).setCellValue("Clicks:");
        summarySheet.createRow(3).createCell(0).setCellValue("Convs:");
        summarySheet.createRow(4).createCell(0).setCellValue("App Nexus Revenue:");
        summarySheet.createRow(5).createCell(0).setCellValue("Media Cost:");
        summarySheet.createRow(6).createCell(0).setCellValue("AppNexus Media Cost:");
        summarySheet.createRow(7).createCell(0).setCellValue("AppNexus Commission:");
        summarySheet.createRow(8).createCell(0).setCellValue("AppNexus Imps:");
        summarySheet.createRow(9).createCell(0).setCellValue("AppNexus eCPM:");
        summarySheet.createRow(10).createCell(0).setCellValue("MX Media Cost:");
        summarySheet.createRow(11).createCell(0).setCellValue("MX Commission:");
        summarySheet.createRow(12).createCell(0).setCellValue("MX Imps:");
        summarySheet.createRow(13).createCell(0).setCellValue("MX eCPM:");
        summarySheet.createRow(14).createCell(0).setCellValue("AdEx Media Cost:");
        summarySheet.createRow(15).createCell(0).setCellValue("AdEx Commission:");
        summarySheet.createRow(16).createCell(0).setCellValue("AdEx Imps:");
        summarySheet.createRow(17).createCell(0).setCellValue("AdEx eCPM:");
        int rowCount = 17;
        for (String n : feeNames) {
            if (n.equals("Lotame")) {
                summarySheet.createRow(++rowCount).createCell(0).setCellValue("Lotame Imps");
                summarySheet.getRow(rowCount).createCell(1).setCellValue(0);
            }else if (n.equals("Brilig")) {
                summarySheet.createRow(++rowCount).createCell(0).setCellValue("Brilig Imps");
                summarySheet.getRow(rowCount).createCell(1).setCellValue(0);
            }else if (n.equals("BlueKai")) {
                summarySheet.createRow(++rowCount).createCell(0).setCellValue("BlueKai Imps");
                summarySheet.getRow(rowCount).createCell(1).setCellValue(0);
            }
            summarySheet.createRow(++rowCount).createCell(0).setCellValue(n);
            summarySheet.getRow(rowCount).createCell(1).setCellValue(0);
            summarySheet.getRow(rowCount).getCell(1).setCellStyle(fullCurrency);
        }
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("Amazon Cost:");
        float amazonFee = (float) grandTotal.getImps() / 1000 * amazonCost;
        summarySheet.getRow(rowCount).createCell(1).setCellValue(amazonFee);
        summarySheet.getRow(rowCount).getCell(1).setCellStyle(fullCurrency);
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("Total Cost:");
        summarySheet.getRow(rowCount).createCell(1).setCellValue(grandTotal.getTotalCost() + amazonFee);
        summarySheet.getRow(rowCount).getCell(1).setCellStyle(fullCurrency);
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("Gross Profit:");
        summarySheet.getRow(rowCount).createCell(1).setCellValue(grandTotal.getNetworkRevenue()
                - grandTotal.getTotalCost() - amazonFee);
        summarySheet.getRow(rowCount).getCell(1).setCellStyle(fullCurrency);
        summarySheet.createRow(++rowCount).createCell(0).setCellValue("GP Margin:");
        summarySheet.getRow(rowCount).createCell(1)
                .setCellValue(summarySheet.getRow(rowCount - 1).getCell(1).getNumericCellValue()                        
                        / grandTotal.getNetworkRevenue());
        summarySheet.getRow(rowCount).getCell(1).setCellStyle(percentage);

        summarySheet.getRow(1).createCell(1).setCellValue(grandTotal.getImps());
        summarySheet.getRow(2).createCell(1).setCellValue(grandTotal.getClicks());
        summarySheet.getRow(3).createCell(1).setCellValue(grandTotal.getConvs());
        summarySheet.getRow(4).createCell(1).setCellValue(grandTotal.getNetworkRevenue());
        summarySheet.getRow(5).createCell(1).setCellValue(grandTotal.getMediaCost());
        summarySheet.getRow(6).createCell(1).setCellValue(grandTotal.getAppNexusMediaCost());
        summarySheet.getRow(7).createCell(1).setCellValue(grandTotal.getAnCommission());
        summarySheet.getRow(8).createCell(1).setCellValue(grandTotal.getAppNexusImps());
        summarySheet.getRow(9).createCell(1).setCellValue(grandTotal.getAnCpm());
        summarySheet.getRow(10).createCell(1).setCellValue(grandTotal.getMxMediaCost());
        summarySheet.getRow(11).createCell(1).setCellValue(grandTotal.getMxCommission());
        summarySheet.getRow(12).createCell(1).setCellValue(grandTotal.getMxImps());
        summarySheet.getRow(13).createCell(1).setCellValue(grandTotal.getMxCpm());
        summarySheet.getRow(14).createCell(1).setCellValue(grandTotal.getAdXMediaCost());
        summarySheet.getRow(15).createCell(1).setCellValue(grandTotal.getAdXCommission());
        summarySheet.getRow(16).createCell(1).setCellValue(grandTotal.getAdExImps());
        summarySheet.getRow(17).createCell(1).setCellValue(grandTotal.getAdXCpm());
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
            else if (r.getCell(0).getStringCellValue().equals("Brilig Imps")) {
                r.getCell(1).setCellValue(grandTotal.getBriligImps());
            }
            else if (r.getCell(0).getStringCellValue().equals("BlueKai Imps")) {
                r.getCell(1).setCellValue(grandTotal.getBlueKaiImps());
            }
        }

        summarySheet.getRow(4).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(5).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(6).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(7).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(9).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(10).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(11).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(13).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(14).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(15).getCell(1).setCellStyle(fullCurrency);
        summarySheet.getRow(17).getCell(1).setCellStyle(fullCurrency);

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
            headerRow.createCell(4).setCellValue("App Nexus Rev.");
            headerRow.createCell(5).setCellValue("Media Cost");
            headerRow.createCell(6).setCellValue("AppNexus Media Cost");
            headerRow.createCell(7).setCellValue("AppNexus Commission");
            headerRow.createCell(8).setCellValue("AppNexus Imps");
            headerRow.createCell(9).setCellValue("AppNexus eCPM");
            headerRow.createCell(10).setCellValue("MX Media Cost");
            headerRow.createCell(11).setCellValue("MX Commission");
            headerRow.createCell(12).setCellValue("MX Imps");
            headerRow.createCell(13).setCellValue("MX eCPM");
            headerRow.createCell(14).setCellValue("AdEx Media Cost");
            headerRow.createCell(15).setCellValue("AdEx Commission");
            headerRow.createCell(16).setCellValue("AdEx Imps");
            headerRow.createCell(17).setCellValue("AdEx eCPM");
            int cellCount = 17;
            for (String n : feeNames) {
                if (n.equals("Lotame")) headerRow.createCell(++cellCount).setCellValue("Lotame Imps");
                else if (n.equals("Brilig")) headerRow.createCell(++cellCount).setCellValue("Brilig Imps");
                else if (n.equals("BlueKai")) headerRow.createCell(++cellCount).setCellValue("BlueKai Imps");
                headerRow.createCell(++cellCount).setCellValue(n);
            }
            headerRow.createCell(++cellCount).setCellValue("Amazon Cost");
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
                dataRow.createCell(4).setCellValue(c.getNetworkRevenue());
                dataRow.createCell(5).setCellValue(c.getMediaCost());
                dataRow.createCell(6).setCellValue(c.getAppNexusMediaCost());
                dataRow.createCell(7).setCellValue(c.getAnCommission());
                dataRow.createCell(8).setCellValue(c.getAppNexusImps());
                dataRow.createCell(9).setCellValue(c.getAnCpm());
                dataRow.createCell(10).setCellValue(c.getMxMediaCost());
                dataRow.createCell(11).setCellValue(c.getMxCommission());
                dataRow.createCell(12).setCellValue(c.getMxImps());
                dataRow.createCell(13).setCellValue(c.getMxCpm());
                dataRow.createCell(14).setCellValue(c.getAdXMediaCost());
                dataRow.createCell(15).setCellValue(c.getAdXCommission());
                dataRow.createCell(16).setCellValue(c.getAdExImps());
                dataRow.createCell(17).setCellValue(c.getAdXCpm());
                cellCount = 17;
                for (String n : feeNames) {
                    if (n.equals("Lotame")) {
                        dataRow.createCell(++cellCount).setCellValue(c.getLotameImps());
                    } else if (n.equals("Brilig")) {
                        dataRow.createCell(++cellCount).setCellValue(c.getBriligImps());
                    } else if (n.equals("BlueKai")) {
                        dataRow.createCell(++cellCount).setCellValue(c.getBlueKaiImps());
                    }
                    dataRow.createCell(++cellCount).setCellValue(0);
                    dataRow.getCell(cellCount).setCellStyle(fullCurrency);
                }
                amazonFee = (float) c.getImps() / 1000 * amazonCost; 
                dataRow.createCell(++cellCount).setCellValue(amazonFee);
                dataRow.getCell(cellCount).setCellStyle(fullCurrency);
                dataRow.createCell(++cellCount).setCellValue(c.getTotalCost() + amazonFee);
                dataRow.getCell(cellCount).setCellStyle(fullCurrency);
                dataRow.createCell(++cellCount).setCellValue(c.getNetworkRevenue() - c.getTotalCost() - amazonFee);
                dataRow.getCell(cellCount).setCellStyle(fullCurrency);
                dataRow.createCell(++cellCount)
                        .setCellValue(dataRow.getCell(cellCount-1).getNumericCellValue()/c.getNetworkRevenue());
                dataRow.getCell(cellCount).setCellStyle(percentage);
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

                dataRow.getCell(4).setCellStyle(fullCurrency);
                dataRow.getCell(5).setCellStyle(fullCurrency);
                dataRow.getCell(6).setCellStyle(fullCurrency);
                dataRow.getCell(7).setCellStyle(fullCurrency);
                dataRow.getCell(9).setCellStyle(fullCurrency);
                dataRow.getCell(10).setCellStyle(fullCurrency);
                dataRow.getCell(11).setCellStyle(fullCurrency);
                dataRow.getCell(13).setCellStyle(fullCurrency);
                dataRow.getCell(14).setCellStyle(fullCurrency);
                dataRow.getCell(15).setCellStyle(fullCurrency);
                dataRow.getCell(18).setCellStyle(fullCurrency);

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
                adTotal.setBlueKaiImps(adTotal.getBlueKaiImps() + c.getBlueKaiImps());
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
            totalRow.createCell(4).setCellValue(adTotal.getNetworkRevenue());
            totalRow.createCell(5).setCellValue(adTotal.getMediaCost());
            totalRow.createCell(6).setCellValue(adTotal.getAppNexusMediaCost());
            totalRow.createCell(7).setCellValue(adTotal.getAnCommission());
            totalRow.createCell(8).setCellValue(adTotal.getAppNexusImps());
            totalRow.createCell(9).setCellValue(adTotal.getAnCpm());
            totalRow.createCell(10).setCellValue(adTotal.getMxMediaCost());
            totalRow.createCell(11).setCellValue(adTotal.getMxCommission());
            totalRow.createCell(12).setCellValue(adTotal.getMxImps());
            totalRow.createCell(13).setCellValue(adTotal.getMxCpm());
            totalRow.createCell(14).setCellValue(adTotal.getAdXMediaCost());
            totalRow.createCell(15).setCellValue(adTotal.getAdXCommission());
            totalRow.createCell(16).setCellValue(adTotal.getAdExImps());
            totalRow.createCell(17).setCellValue(adTotal.getAdXCpm());
            cellCount = 17;
            for (String n : feeNames) {
                if (n.equals("Lotame")) totalRow.createCell(++cellCount).setCellValue(adTotal.getLotameImps());
                else if (n.equals("Brilig")) totalRow.createCell(++cellCount).setCellValue(adTotal.getBriligImps());
                else if (n.equals("BlueKai")) totalRow.createCell(++cellCount).setCellValue(adTotal.getBriligImps());
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
            amazonFee = (float) adTotal.getImps() / 1000 * amazonCost; 
            totalRow.createCell(++cellCount).setCellValue(amazonFee);
            totalRow.getCell(cellCount).setCellStyle(fullCurrency);
            totalRow.createCell(++cellCount).setCellValue(adTotal.getTotalCost() + amazonFee);
            totalRow.getCell(cellCount).setCellStyle(fullCurrency);
            totalRow.createCell(++cellCount).setCellValue(adTotal.getNetworkRevenue() - adTotal.getTotalCost() - amazonFee);
            totalRow.getCell(cellCount).setCellStyle(fullCurrency);
            totalRow.createCell(++cellCount)
                    .setCellValue(totalRow.getCell(cellCount-1).getNumericCellValue()/adTotal.getNetworkRevenue());
            totalRow.getCell(cellCount).setCellStyle(percentage);
            if (ad.getId().equals("242222")) {
                totalRow.createCell(++cellCount).setCellValue(adTotal.getNetworkRevenue() * discoveryCommission);
                totalRow.getCell(cellCount).setCellStyle(fullCurrency);
            }

            totalRow.getCell(4).setCellStyle(fullCurrency);
            totalRow.getCell(5).setCellStyle(fullCurrency);
            totalRow.getCell(6).setCellStyle(fullCurrency);
            totalRow.getCell(7).setCellStyle(fullCurrency);
            totalRow.getCell(9).setCellStyle(fullCurrency);
            totalRow.getCell(10).setCellStyle(fullCurrency);
            totalRow.getCell(11).setCellStyle(fullCurrency);
            totalRow.getCell(13).setCellStyle(fullCurrency);
            totalRow.getCell(14).setCellStyle(fullCurrency);
            totalRow.getCell(15).setCellStyle(fullCurrency);
            totalRow.getCell(17).setCellStyle(fullCurrency);

            for (Row r : advertiserSheet) {
                for (Cell cell : r) {
                    int i = cell.getColumnIndex();
                    if ((i < 10 && i > 5) || (i < 18 && i > 13)) {
                        if (advertiserSheet.getRow(0).getCell(i).getStringCellValue().contains("Imp")) {
                            cell.setCellStyle(patternImp);
                        } else cell.setCellStyle(pattern);
                    }
                }
            }

            for (int x = 0; x <= cellCount; x++) advertiserSheet.autoSizeColumn(x);
        }

        //build advertiser summary page
        Row summaryHeader = advertiserSummary.createRow(0);
        summaryHeader.createCell(0).setCellValue("Advertiser");
        summaryHeader.createCell(1).setCellValue("Imps");
        summaryHeader.createCell(2).setCellValue("Clicks");
        summaryHeader.createCell(3).setCellValue("Convs");
        summaryHeader.createCell(4).setCellValue("App Nexus Rev.");
        summaryHeader.createCell(5).setCellValue("Media Cost");
        summaryHeader.createCell(6).setCellValue("AppNexus Media Cost");
        summaryHeader.createCell(7).setCellValue("AppNexus Commission");
        summaryHeader.createCell(8).setCellValue("AppNexus Imps");
        summaryHeader.createCell(9).setCellValue("AppNexus eCPM");
        summaryHeader.createCell(10).setCellValue("MX Media Cost");
        summaryHeader.createCell(11).setCellValue("MX Commission");
        summaryHeader.createCell(12).setCellValue("MX Imps");
        summaryHeader.createCell(13).setCellValue("MX eCPM");
        summaryHeader.createCell(14).setCellValue("AdEx Media Cost");
        summaryHeader.createCell(15).setCellValue("AdEx Commission");
        summaryHeader.createCell(16).setCellValue("AdEx Imps");
        summaryHeader.createCell(17).setCellValue("AdEx eCPM");
        int cellCount = 17;
        for (String n : feeNames) {
            if (n.equals("Lotame")) summaryHeader.createCell(++cellCount).setCellValue("Lotame Imps");
            else if (n.equals("Brilig")) summaryHeader.createCell(++cellCount).setCellValue("Brilig Imps");
            else if (n.equals("BlueKai")) summaryHeader.createCell(++cellCount).setCellValue("BlueKai Imps");
            summaryHeader.createCell(++cellCount).setCellValue(n);
        }
        summaryHeader.createCell(++cellCount).setCellValue("Amazon Cost");
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
                adRow.createCell(4).setCellValue(c.getNetworkRevenue());
                adRow.createCell(5).setCellValue(c.getMediaCost());
                adRow.createCell(6).setCellValue(c.getAppNexusMediaCost());
                adRow.createCell(7).setCellValue(c.getAnCommission());
                adRow.createCell(8).setCellValue(c.getAppNexusImps());
                adRow.createCell(9).setCellValue(c.getAnCpm());
                adRow.createCell(10).setCellValue(c.getMxMediaCost());
                adRow.createCell(11).setCellValue(c.getMxCommission());
                adRow.createCell(12).setCellValue(c.getMxImps());
                adRow.createCell(13).setCellValue(c.getMxCpm());
                adRow.createCell(14).setCellValue(c.getAdXMediaCost());
                adRow.createCell(15).setCellValue(c.getAdXCommission());
                adRow.createCell(16).setCellValue(c.getAdExImps());
                adRow.createCell(17).setCellValue(c.getAdXCpm());
                for (Cell cell : summaryHeader) {
                    if (cell.getStringCellValue().equals("Lotame Imps")) {
                        adRow.createCell(cell.getColumnIndex()).setCellValue(c.getLotameImps());
                    } else if (cell.getStringCellValue().equals("Brilig Imps")) {
                        adRow.createCell(cell.getColumnIndex()).setCellValue(c.getBriligImps());
                    } else if (cell.getStringCellValue().equals("BlueKai Imps")) {
                        adRow.createCell(cell.getColumnIndex()).setCellValue(c.getBlueKaiImps());
                    }
                    for (ServingFee f : c.getServingFees()) {
                        if (f.getBrokerName().equals(cell.getStringCellValue())) {
                            adRow.createCell(cell.getColumnIndex()).setCellValue(f.getTotalFee());
                            adRow.getCell(cell.getColumnIndex()).setCellStyle(fullCurrency);
                        }
                    }
                    amazonFee = (float)c.getImps()/1000 * amazonCost;
                    if (cell.getStringCellValue().equals("Amazon Cost")) {
                        adRow.createCell(cell.getColumnIndex()).setCellValue(amazonFee);
                        adRow.getCell(cell.getColumnIndex()).setCellStyle(fullCurrency);
                    } else if (cell.getStringCellValue().equals("Total Cost")) {
                        adRow.createCell(cell.getColumnIndex()).setCellValue(c.getTotalCost() + amazonFee);
                        adRow.getCell(cell.getColumnIndex()).setCellStyle(fullCurrency);
                    } else if (cell.getStringCellValue().equals("Gross Profit")) {
                        adRow.createCell(cell.getColumnIndex())
                                .setCellValue(c.getNetworkRevenue() - c.getTotalCost() - amazonFee);
                        adRow.getCell(cell.getColumnIndex()).setCellStyle(fullCurrency);
                    } else if (cell.getStringCellValue().equals("GP Margin")) {
                        adRow.createCell(cell.getColumnIndex())
                                .setCellValue(adRow.getCell(cell.getColumnIndex()-1).getNumericCellValue()
                                        /c.getNetworkRevenue());
                        adRow.getCell(cell.getColumnIndex()).setCellStyle(percentage);
                    }
                }

                adRow.getCell(4).setCellStyle(fullCurrency);
                adRow.getCell(5).setCellStyle(fullCurrency);
                adRow.getCell(6).setCellStyle(fullCurrency);
                adRow.getCell(7).setCellStyle(fullCurrency);
                adRow.getCell(9).setCellStyle(fullCurrency);
                adRow.getCell(10).setCellStyle(fullCurrency);
                adRow.getCell(11).setCellStyle(fullCurrency);
                adRow.getCell(13).setCellStyle(fullCurrency);
                adRow.getCell(14).setCellStyle(fullCurrency);
                adRow.getCell(15).setCellStyle(fullCurrency);
                adRow.getCell(17).setCellStyle(fullCurrency);

                rowCount++;
            }
        }
        Row totalAd = advertiserSummary.createRow(++rowCount);
        for (Cell c : summaryHeader) totalAd.createCell(c.getColumnIndex()).setCellValue(0);
        totalAd.getCell(4).setCellStyle(fullCurrency);
        totalAd.getCell(5).setCellStyle(fullCurrency);
        totalAd.getCell(6).setCellStyle(fullCurrency);
        totalAd.getCell(7).setCellStyle(fullCurrency);
        totalAd.getCell(9).setCellStyle(fullCurrency);
        totalAd.getCell(10).setCellStyle(fullCurrency);
        totalAd.getCell(11).setCellStyle(fullCurrency);
        totalAd.getCell(13).setCellStyle(fullCurrency);
        totalAd.getCell(14).setCellStyle(fullCurrency);
        totalAd.getCell(15).setCellStyle(fullCurrency);
        totalAd.getCell(17).setCellStyle(fullCurrency);
        for (Cell c : summaryHeader) {
            if (c.getColumnIndex() > 17 &&
                    !summaryHeader.getCell(c.getColumnIndex()).getStringCellValue().equals("Lotame Imps") &&
                    !summaryHeader.getCell(c.getColumnIndex()).getStringCellValue().equals("Brilig Imps") &&
                    !summaryHeader.getCell(c.getColumnIndex()).getStringCellValue().equals("BlueKai Imps"))
            {
                totalAd.getCell(c.getColumnIndex()).setCellStyle(fullCurrency);
            }
        }
        totalAd.getCell(0).setCellValue("Total:");
        for (Row r : advertiserSummary) {
            for (Cell c : r) {
                if (c.getColumnIndex() != 0 && c.getRowIndex() != totalAd.getRowNum() && c.getRowIndex() != 0 &&
                        !advertiserSummary.getRow(0).getCell(c.getColumnIndex()).getStringCellValue().contains("eCPM")
                        && !advertiserSummary.getRow(0).getCell(c.getColumnIndex()).getStringCellValue().contains("GP Margin")) {
                    totalAd.getCell(c.getColumnIndex())
                            .setCellValue(totalAd.getCell(c.getColumnIndex()).getNumericCellValue() +
                                    c.getNumericCellValue());
                }
            }
        }

        for (Row r : advertiserSummary) {
            for (Cell cell : r) {
                int i = cell.getColumnIndex();
                if ((i < 10 && i > 5) || (i < 18 && i > 13)) {
                    if (advertiserSummary.getRow(0).getCell(i).getStringCellValue().contains("Imps")) {
                        cell.setCellStyle(patternImp);
                    } else cell.setCellStyle(pattern);
                }
            }
        }

        for (int x = 0; x <= 50; x++) advertiserSummary.autoSizeColumn(x);


        //Export file
        LocalDate now = new LocalDate(DateTimeZone.UTC);
        FileOutputStream fileOut =
                new FileOutputStream(new File(outputPath, "Daily_PnL_"
                        + now.toString() + ".xls"));
        wb.write(fileOut);
        fileOut.close();
    }
}