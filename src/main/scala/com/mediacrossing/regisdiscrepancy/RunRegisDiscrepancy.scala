package com.mediacrossing.regisdiscrepancy

import java.io.{File, FileOutputStream}
import java.util.Date

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel._
import org.joda.time.DateTime
import scala.collection.JavaConversions._


import scala.collection.mutable.ListBuffer

object RunRegisDiscrepancy extends App {

  // Import MediaMind Report
  val mmSheet = WorkbookFactory.create(
    new File("/Users/charronkyle/Downloads/Regis_CPS_Regis_Enrollment_Marketing_2015_Custom_Report_230803.xlsx"))
    .getSheetAt(0)

  // Import AN Report
  val anSheet = WorkbookFactory.create(new File("/Users/charronkyle/Downloads/report.xlsx"))
    .getSheetAt(0)

  // Buffer To Map Real Names
  val realNameBuffer = new scala.collection.mutable.ListBuffer[(String,String)]()

  // Buffer For List of AN Report Data
  val anTupleBuffer = new scala.collection.mutable.ListBuffer[(String,Int,String)]()

  // Read In AN Report
  println("Reading AN Sheet...")
  for(i <- 1 until anSheet.getPhysicalNumberOfRows) {
    val row = anSheet.getRow(i)
    val realName = row.getCell(1).getStringCellValue
      .split("[ _]")
      .filterNot(w => w.equals("PL") || w.equals("V2") || w.equals("Updated"))
      .mkString(" ")
    val fixName = row.getCell(1).getStringCellValue
      .split("[ _]")
      .filterNot(w => w.equals("PL") || w.equals("V2") || w.equals("Updated"))
      .mkString("")
      .replace("x", "")
      .replace("tet", "text")
    val imps = row.getCell(2).getNumericCellValue.toInt
    val dateString = new DateTime(row.getCell(0).getDateCellValue).toString("M'/'d'/'yy")
    anTupleBuffer += ((fixName, imps, dateString))
    realNameBuffer += ((fixName, realName))
  }

  // Map For Real Names
  val realNameMap = realNameBuffer.toMap

  // Map Creative Name To List of Maps Date To Imp
  val anMap: Map[String, Map[String, Int]] = anTupleBuffer.groupBy(_._1)
    .mapValues(l => l.map(t3 => (t3._3, t3._2)).groupBy(_._1).mapValues(l => l.map(t => t._2).sum))
    .filterNot(kv => kv._1.contains("TEST"))

  // MM Data Buffer
  val mmTupleBuffer = new scala.collection.mutable.ListBuffer[(String,Int,String)]()
  // Date To Only Pull Last 7 Days Of Data
  val minDate = new DateTime().minusDays(8)
  // Read In MediaMind Report
  println("Reading Sizmek Sheet...")
  for(i <- 30 until mmSheet.getPhysicalNumberOfRows) {
    val rowDate = {
      try {
        val d = mmSheet.getRow(i).getCell(0).getDateCellValue
        new DateTime(d)
      } catch {
        case npe: NullPointerException => {
          new DateTime(new Date(100000))
        }
        case ise: IllegalStateException => {
          new DateTime(new Date(100000))
        }
      }
    }
    // If Date Is Valid, save data
    if(rowDate.isAfter(minDate)) {
      val row = mmSheet.getRow(i)
      val name = {
        try {
          row.getCell(1).getStringCellValue
            .split(" ")
            .filterNot(w => w.equals(""))
            .mkString("")
            .replace("x", "")
            .replace("tet", "text")
        } catch {
          case npe: NullPointerException => ""
        }
      }
      val imps = {
        try {
          row.getCell(12).getNumericCellValue.toInt
        } catch {
          case npe: NullPointerException => -1
        }
      }
      val mmDateString = rowDate.toString("M'/'d'/'yy")
      mmTupleBuffer += ((name, imps, mmDateString))
    }
  }


  // Map MediaMind Creative Names To Maps of Date To Imps
  val mmMap: Map[String, Map[String, Int]] = mmTupleBuffer.groupBy(_._1)
    .mapValues(l => l.map(t3 => (t3._3, t3._2)).groupBy(_._1).mapValues(l => l.map(t => t._2).sum))

  // Create Workbook
  val wb = new HSSFWorkbook()
  val found = wb.createSheet("AN Names found in MM sheet")
  var foundRowCount = 2
  val dateHeaderRow = found.createRow(0)
  val headerRow = found.createRow(1)
  headerRow.createCell(0).setCellValue("Creative Name")
  var colCount = 1
  val headerDateListBuffer = new ListBuffer[String]
  for (x <- 1 to 7) {
    val addedDateString = new DateTime().minusDays(x).toString("M'/'d'/'yy")
    headerDateListBuffer += addedDateString
  }
  for (s <- headerDateListBuffer.toList) {
    dateHeaderRow.createCell(colCount).setCellValue(s)
    headerRow.createCell(colCount).setCellValue("AppNexus")
    headerRow.createCell(colCount+1).setCellValue("Sizmek")
    headerRow.createCell(colCount+2).setCellValue("% Diff")
    colCount += 3
  }

  val percentageStyle = wb.createCellStyle()
  val df = wb.createDataFormat()
  percentageStyle.setDataFormat(df.getFormat("0%"))
  val numberStyle = wb.createCellStyle()
  numberStyle.setDataFormat(df.getFormat("#,##0"))
  val greenPercentageStyle = wb.createCellStyle()
  greenPercentageStyle.cloneStyleFrom(percentageStyle)
  greenPercentageStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex)
  greenPercentageStyle.setFillPattern(CellStyle.SOLID_FOREGROUND)
  val greenNumberStyle = wb.createCellStyle()
  greenNumberStyle.cloneStyleFrom(numberStyle)
  greenNumberStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex)
  greenNumberStyle.setFillPattern(CellStyle.SOLID_FOREGROUND)
  val greenStyle = wb.createCellStyle()
  greenStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex)
  greenStyle.setFillPattern(CellStyle.SOLID_FOREGROUND)
  val alertStyle = wb.createCellStyle()
  val alertFont = wb.createFont()
  alertFont.setColor(IndexedColors.RED.getIndex)
  alertFont.setBoldweight(1000)
  alertStyle.setFont(alertFont)


  anMap.foreach(name => {
    val row = found.createRow(foundRowCount)
    row.createCell(0).setCellValue(realNameMap.get(name._1).get)
    name._2.foreach(date => {
      for(h: Cell <- dateHeaderRow.cellIterator().toIterator) {
        println("Iterating...")
        if(h.getStringCellValue.equals(date._1)) {
          println("Date Match!")
          val index = h.getColumnIndex
          println("Name = " + name._1)
          println("Date = " + date._1)

          if(name._1.contains("SpongeCell")) {
            if(name._1.contains("Retargeting")) {
              row.createCell(index).setCellValue(date._2)
              row.createCell(index+1).setCellValue(mmMap.get("Autoplay,RetargetingGeneral").get.get(date._1).get)
              row.createCell(index+2).setCellValue(math.abs(1-(row.getCell(index).getNumericCellValue / row.getCell(index+1).getNumericCellValue)))
              row.getCell(index+2).setCellStyle(percentageStyle)
              if(row.getCell(index+2).getNumericCellValue >= .1) row.getCell(0).setCellStyle(alertStyle)
            }
            else {
              row.createCell(index).setCellValue(date._2)
              row.createCell(index+1).setCellValue(mmMap.get("Autoplay,General").get.get(date._1).get)
              row.createCell(index+2).setCellValue(math.abs(1-(row.getCell(index).getNumericCellValue / row.getCell(index+1).getNumericCellValue)))
              row.getCell(index+2).setCellStyle(percentageStyle)
              if(row.getCell(index+2).getNumericCellValue >= .1) row.getCell(0).setCellStyle(alertStyle)
            }
          }
          else {
            val mmImpCount = mmMap.get(name._1)
              .get
              .get(date._1)
              .getOrElse(-1)
            row.createCell(index).setCellValue(date._2)
            row.createCell(index+1).setCellValue(mmImpCount)
            row.createCell(index+2).setCellValue(math.abs(1-(row.getCell(index).getNumericCellValue / row.getCell(index+1).getNumericCellValue)))
            row.getCell(index+2).setCellStyle(percentageStyle)
            if(row.getCell(index+2).getNumericCellValue >= .1) row.getCell(0).setCellStyle(alertStyle)
          }
        }
      }
    })
    foundRowCount += 1
  })

  for(row:Row <- found.iterator().toIterator) {
    var color = true
    for (cell:Cell <- row.cellIterator().toIterator) {
      if(cell.getColumnIndex != 0) {
        if(color) {
          if(row.getRowNum > 1) {
            if(found.getRow(1).getCell(cell.getColumnIndex).getStringCellValue.contains("%")){
              cell.setCellStyle(greenPercentageStyle)
            } else {
              cell.setCellStyle(greenNumberStyle)
            }
          } else {
            cell.setCellStyle(greenStyle)
          }
        }
        if(cell.getColumnIndex % 3 == 0 && cell.getColumnIndex != 0) color = !color
      }
    }
  }

  for(x <- 0 to 20) found.autoSizeColumn(x)

  wb.write(new FileOutputStream(new File("/Users/charronkyle/Desktop", "Discrep_Test.xls")))
  println("Done.")
}
