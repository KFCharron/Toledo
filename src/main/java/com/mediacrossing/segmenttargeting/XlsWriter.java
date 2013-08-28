package com.mediacrossing.segmenttargeting;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class XlsWriter {
    public void writeSegmentFileInXls (ArrayList<Campaign> campaignArrayList) {
        try {
            Workbook wb = new HSSFWorkbook();
            Sheet sheet = wb.createSheet("Sheet1");
            CreationHelper createHelper = wb.getCreationHelper();

            //Header row
            Row headerRow = sheet.createRow((short)0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Campaign");
            headerRow.createCell(2).setCellValue("Segments");





            //creates file
            FileOutputStream fileOut = new FileOutputStream("TargetSegment.xls");
            wb.write(fileOut);
            fileOut.close();
        }
        catch (UnsupportedEncodingException e) {}
        catch (FileNotFoundException e){}
        catch (IOException e){}



    }
}
