package com.mediacrossing.audiencecomposition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class RunAudienceComposition {

    public static SXSSFWorkbook wb = new SXSSFWorkbook(100);
    public static Sheet sheet = wb.createSheet();
    public static Row row;
    public static int rowCount = 0;
    public static int cellCount = 0;

    public static void processOneSheet(String filename) throws Exception {
        OPCPackage pkg = OPCPackage.open(filename);
        XSSFReader r = new XSSFReader( pkg );
        SharedStringsTable sst = r.getSharedStringsTable();

        XMLReader parser = fetchSheetParser(sst);

        // rId2 found by processing the Workbook
        // Seems to either be rId# or rSheet#
        InputStream sheet2 = r.getSheet("rId2");
        InputSource sheetSource = new InputSource(sheet2);
        parser.parse(sheetSource);
        sheet2.close();
    }

    public static void processAllSheets(String filename) throws Exception {
        OPCPackage pkg = OPCPackage.open(filename);
        XSSFReader r = new XSSFReader( pkg );
        SharedStringsTable sst = r.getSharedStringsTable();

        XMLReader parser = fetchSheetParser(sst);

        Iterator<InputStream> sheets = r.getSheetsData();
        while(sheets.hasNext()) {
            System.out.println("Processing new sheet:\n");
            InputStream sheet = sheets.next();
            InputSource sheetSource = new InputSource(sheet);
            parser.parse(sheetSource);
            sheet.close();
            System.out.println("");
        }
    }

    public static XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException {
        XMLReader parser =
                XMLReaderFactory.createXMLReader(
                        "org.apache.xerces.parsers.SAXParser"
                );
        ContentHandler handler = new SheetHandler(sst);
        parser.setContentHandler(handler);
        return parser;
    }

    /**
     * See org.xml.sax.helpers.DefaultHandler javadocs
     */
    private static class SheetHandler extends DefaultHandler {
        private SharedStringsTable sst;
        private String lastContents;
        private boolean nextIsString;
        private boolean numberCell = false;

        private SheetHandler(SharedStringsTable sst) {
            this.sst = sst;
        }

        public void startElement(String uri, String localName, String name,
                                 Attributes attributes) throws SAXException {
            // c => cell
            if(name.equals("c")) {
                // Print the cell reference
                if(attributes.getValue("r").contains("A")) {
                    if (rowCount > 0) {
                        row.createCell(cellCount++).setCellFormula("D" + (rowCount +1) + "/J" + (rowCount + 1) + "*1000");
                        row.createCell(cellCount++);
                        row.createCell(cellCount++);
                    }
                    row = sheet.createRow(++rowCount);
                    cellCount = 0;
                } else if(attributes.getValue("r").contains("B") ||
                          attributes.getValue("r").contains("D") ||
                          attributes.getValue("r").contains("E") ||
                          attributes.getValue("r").contains("J") ||
                          attributes.getValue("r").contains("K")) numberCell = true;

                System.out.print(attributes.getValue("r") + " - ");
                // Figure out if the value is an index in the SST
                String cellType = attributes.getValue("t");
                if(cellType != null && cellType.equals("s")) {
                    nextIsString = true;
                } else {
                    nextIsString = false;
                }
            }
            // Clear contents cache
            lastContents = "";
        }

        public void endElement(String uri, String localName, String name)
                throws SAXException {
            // Process the last contents as required.
            // Do now, as characters() may be called more than once
            if(nextIsString) {
                int idx = Integer.parseInt(lastContents);
                lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
                nextIsString = false;
            }

            // v => contents of a cell
            // Output after we've seen the string contents
            if(name.equals("v")) {
                System.out.println(lastContents);
                row.createCell(cellCount++).setCellValue(Float.parseFloat(lastContents));
                if (numberCell) {
                    row.getCell(cellCount-1).setCellType(Cell.CELL_TYPE_NUMERIC);
                    numberCell = false;
                }

            }
        }

        public void characters(char[] ch, int start, int length)
                throws SAXException {
            lastContents += new String(ch, start, length);
        }
    }

    public static void main(String[] args) throws Exception {
        processAllSheets("/Users/charronkyle/Documents/Developer/toledo/src/main/resources/MX - Lotame 4.14.14.xlsx");
        //processOneSheet("/Users/charronkyle/Documents/Developer/toledo/src/main/resources/MX - Lotame 4.14.14.xlsx");
        System.out.println("DONE!");
        FileOutputStream fileOut =
                new FileOutputStream(new File("/Users/charronkyle/Desktop/Test_Audience_Comp.xls"));
        wb.write(fileOut);
        fileOut.close();
        wb.dispose();
    }
}
