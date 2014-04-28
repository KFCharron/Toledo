package com.mediacrossing.audiencecomposition

import org.apache.poi.ss.usermodel.{Sheet, Row}
import org.joda.time.{DateTimeZone, LocalDate}
import java.io.{File, FileOutputStream}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import scala.collection.JavaConversions._
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.xssf.eventusermodel.XSSFReader
import org.xml.sax.{InputSource, XMLReader}
import org.apache.poi.xssf.model.SharedStringsTable
import org.xml.sax.helpers.XMLReaderFactory

object RunAudienceComp extends App {

  // New wb
  val finalWb = new XSSFWorkbook(RunAudienceComp.getClass.getResourceAsStream("/Test_Audience_Template.xlsx"))
  finalWb.removeSheetAt(3)
  val dataSheet = finalWb.createSheet("Branded")
  val hRow = dataSheet.createRow(0)
  hRow.createCell(0).setCellValue("Audience Name")
  hRow.createCell(1).setCellValue("Audience ID")
  hRow.createCell(2).setCellValue("Audience Creation")
  hRow.createCell(3).setCellValue("Overlap Stats")
  hRow.createCell(4).setCellValue("30 Day Audience Stats")
  hRow.createCell(5).setCellValue("Branded Provider")
  hRow.createCell(6).setCellValue("Behavior")
  hRow.createCell(7).setCellValue("Hierarchy Path")
  hRow.createCell(8).setCellValue("Behavior ID")
  hRow.createCell(9).setCellValue("30 Day Behavior")
  hRow.createCell(10).setCellValue("CPM")
  hRow.createCell(11).setCellValue("Index")
  hRow.createCell(12).setCellValue("Norm")
  hRow.createCell(13).setCellValue("Score")
  for(x <- 0 to 13) dataSheet.autoSizeColumn(x)

  // Read in source file
  //val dataWb = new XSSFWorkbook(RunAudienceComposition.getClass.getResourceAsStream(""))
  val dataWb = new XSSFWorkbook(RunAudienceComp.getClass.getResourceAsStream("/MX - Lotame 4.14.14.xlsx"))
  //val dataWb: Workbook = WorkbookFactory.create(new File("/Users/charronkyle/Documents/Developer/toledo/src/main/resources/MX - Lotame 4.14.14.xlsx"))

  var rowCount = 1
  for (s: Sheet <- dataWb) {
    for (r: Row <- s) {
      if (r.getRowNum != 0) {
        val newRow = dataSheet.createRow(rowCount)
        newRow.createCell(0).setCellValue(r.getCell(0).getStringCellValue)
        newRow.createCell(1).setCellValue(r.getCell(1).getStringCellValue)
        newRow.createCell(2).setCellValue(r.getCell(2).getStringCellValue)
        newRow.createCell(3).setCellValue(r.getCell(3).getStringCellValue)
        newRow.createCell(4).setCellValue(r.getCell(4).getStringCellValue)
        newRow.createCell(5).setCellValue(r.getCell(5).getStringCellValue)
        newRow.createCell(6).setCellValue(r.getCell(6).getStringCellValue)
        newRow.createCell(7).setCellValue(r.getCell(7).getStringCellValue)
        newRow.createCell(8).setCellValue(r.getCell(8).getStringCellValue)
        newRow.createCell(9).setCellValue(r.getCell(9).getStringCellValue)
        newRow.createCell(10).setCellValue(r.getCell(10).getStringCellValue)
        rowCount += 1
      }
    }
  }

  // Write new file to same directory, new name
  val today = new LocalDate(DateTimeZone.UTC)
  val out = new FileOutputStream(new File("/Users/charronkyle/Desktop/Audience_Composition_" + today.toString+ ".xls"))
  finalWb.write(out)
  out.close()
}
