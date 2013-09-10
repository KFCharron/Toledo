package com.mediacrossing.campaignbooks;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;

public class ExcelWriter {

    private static Workbook WORKBOOK;

    public void writeLineItemSheetToWorkbook(LineItem lineItem) {

        //Create new sheet TODO add name
        Sheet lineItemSheet = WORKBOOK.createSheet(lineItem.getLineItemName());

        //Add line item header row

        //style header

        //add line item data

        //add campaign header row

        //style header

        //repeat for each campaign

        //pattern the rows

    }

    public void writeWorkbookToFile() {

    }

}
