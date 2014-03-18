package com.mediacrossing.lotamereport;

import com.mediacrossing.dailycheckupsreport.Campaign;
import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.connections.MxService;
import com.mediacrossing.dailycheckupsreport.ServingFee;
import com.mediacrossing.properties.ConfigurationProperties;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class RunLotameReport {

    private static final Logger LOG = LoggerFactory.getLogger(RunLotameReport.class);

    public static void registerLoggerWithUncaughtExceptions() {
        Thread.setDefaultUncaughtExceptionHandler(
                new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
        );
    }

    public static void main(String[] args) throws Exception {

        registerLoggerWithUncaughtExceptions();

        //Declare variables
        ConfigurationProperties properties = new ConfigurationProperties(args);
        String appNexusUrl = properties.getAppNexusUrl();
        String outputPath = properties.getOutputPath();
        String appNexusUsername = properties.getAppNexusUsername();
        String appNexusPassword = properties.getAppNexusPassword();
        int anPartitionSize = properties.getPartitionSize();
        Duration requestDelayInSeconds = properties.getRequestDelayInSeconds();
        AppNexusService anConn = new AppNexusService(appNexusUrl, appNexusUsername,
                appNexusPassword, anPartitionSize, requestDelayInSeconds);
        String mxUsername = properties.getMxUsername();
        String mxPass = properties.getMxPassword();
        String mxUrl = properties.getMxUrl();
        MxService mxConn = new MxService(mxUrl, mxUsername, mxPass);

        ArrayList<Campaign> campList = mxConn.requestAllCampaigns();

        ArrayList<Campaign> lotameCamps = new ArrayList<>();

        for (Campaign c : campList) {
            boolean lotameCamp = false;
            for (ServingFee f : c.getServingFeeList()) {
                if (f.getBrokerName().equals("Lotame")) {
                    lotameCamp = true;
                }
            }
            if (lotameCamp) lotameCamps.add(c);
        }

        List<String[]> data = anConn.requestImpReport();
        data.remove(0);

        for (String[] l : data) {
            for (Campaign c : lotameCamps) {
                if (c.getId().equals(l[0])) {
                    c.setDailyImps(Integer.parseInt(l[1]));
                }
            }
        }

        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Campaign");
        header.createCell(1).setCellValue("Imps");
        int rowCount = 1;
        for (Campaign c : lotameCamps) {
            if (c.getDailyImps() > 0) {
                Row dataRow = sheet.createRow(rowCount);
                dataRow.createCell(0).setCellValue(c.getName());
                dataRow.createCell(1).setCellValue(c.getDailyImps());
                rowCount++;
            }

        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        FileOutputStream fileOut =
                new FileOutputStream(new File(outputPath, "Lotame_Imps_Feb12-28.xls"));
        wb.write(fileOut);
        fileOut.close();
    }

}
