package com.mediacrossing.test;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.properties.ConfigurationProperties;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class RunTest {

    public static void main(String[] args) throws Exception {
        ConfigurationProperties props = new ConfigurationProperties(args);
        AppNexusService anConn = new AppNexusService(props.getPutneyUrl());
        List<String[]> csvData = anConn.getAdvertiserAnalyticReport("206084");
        System.out.println(csvData.size());
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        int rowCount = 0;
        for (String[] row : csvData) {
            Row newRow = sheet.createRow(rowCount++);
            int cellCount = 0;
            for (String s : row) {
                newRow.createCell(cellCount++).setCellValue(s);
            }
        }

        FileOutputStream fileOut =
                new FileOutputStream(new File(props.getOutputPath(), "Test_Workbook.xls"));
        wb.write(fileOut);
        fileOut.close();
    }

}
