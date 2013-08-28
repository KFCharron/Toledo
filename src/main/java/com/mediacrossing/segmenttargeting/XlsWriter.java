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

            Row row = sheet.createRow((short)0);
            // Create a cell and put a value in it.
            Cell cell = row.createCell(0);
            cell.setCellValue(1);


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
