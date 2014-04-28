package com.mediacrossing.dailyclientpublisherreport

import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.AppNexusService
import scala.collection.JavaConversions._
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.joda.time.{DateTimeZone, LocalDate}
import java.io.{File, FileOutputStream}
import org.apache.poi.ss.usermodel._

object RunDailyClientPublisherReport extends App {

  val props = new ConfigurationProperties(args)
  val anConn = new AppNexusService(props.getAppNexusUrl,
                                   props.getAppNexusUsername,
                                   props.getAppNexusPassword)

  
  final val publisherId = "338866"
  val retrievedData = anConn.requestClientPublisherReport(publisherId)
                            .toList // Java Conversion
                            .tail   // Exclude Header Row
                            .map(r =>
                              DataRow(placementId = r(0),
                                      placementName = r(1).replace("_", " ").replace("Comcast", ""),
                                      avails = r(2).toInt,
                                      sold = r(3).toInt,
                                      publisherRevenue = r(4).toFloat))

  val wb = new HSSFWorkbook()
  val sheet = wb.createSheet("Comcast Daily")

  val headerRow = sheet.createRow(1)
  val headers = List("Placement", "Avails", "Sold", "Fill Rate (%)", "Revenue")
  val headerStyle = wb.createCellStyle()
  headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex)
  headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND)
  val headerFont = wb.createFont()
  headerFont.setBoldweight(1000)
  headerFont.setColor(IndexedColors.WHITE.getIndex)
  headerFont.setFontHeightInPoints(13)
  headerStyle.setFont(headerFont)


  headerRow.createCell(0).setCellValue(headers(0))
  headerRow.createCell(1).setCellValue(headers(1))
  headerRow.createCell(2).setCellValue(headers(2))
  headerRow.createCell(3).setCellValue(headers(3))
  headerRow.createCell(4).setCellValue(headers(4))
  headerRow.setHeightInPoints(16)

  for(c: Cell <- headerRow) c.setCellStyle(headerStyle)

  val df = wb.createDataFormat()
  val currency = wb.createCellStyle()
  currency.setDataFormat(df.getFormat("$#,##0.00"))
  val percentage = wb.createCellStyle()
  percentage.setDataFormat(df.getFormat("0.00%"))

  // Add row to excel sheet for every placement under the publisher
  var rowCount = 2
  retrievedData.foreach(r => {
    val row = sheet.createRow(rowCount)
    row.createCell(0).setCellValue(r.placementName)
    row.createCell(1).setCellValue(r.avails)
    row.createCell(2).setCellValue(r.sold)
    row.createCell(3).setCellValue(r.sold.toFloat / r.avails)
    row.createCell(4).setCellValue(r.publisherRevenue)

    row.getCell(1).setCellType(Cell.CELL_TYPE_NUMERIC)
    row.getCell(2).setCellType(Cell.CELL_TYPE_NUMERIC)
    row.getCell(3).setCellStyle(percentage)
    row.getCell(4).setCellStyle(currency)

    rowCount += 1

  })
  rowCount += 1

  val totalRow = sheet.createRow(rowCount)
  totalRow.createCell(0).setCellValue("Totals:")
  totalRow.createCell(1).setCellFormula("SUM(B3:B" + (rowCount-1) +  ")")
  totalRow.createCell(2).setCellFormula("SUM(C3:C" + (rowCount-1) +  ")")
  totalRow.createCell(3).setCellFormula("SUM(C3:C" + (rowCount-1) + ")/SUM(B3:B" + (rowCount-1) + ")")
  totalRow.createCell(4).setCellFormula("SUM(E3:E" + (rowCount-1) + ")")

  // Style and Autosize Columns
  totalRow.getCell(3).setCellStyle(percentage)
  totalRow.getCell(4).setCellStyle(currency)

  for(c: Cell <- totalRow) sheet.autoSizeColumn(c.getColumnIndex)

  // Workbook Output
  val today = new LocalDate(DateTimeZone.UTC)
  val out = new FileOutputStream(new File(props.getOutputPath, "Daily_Comcast_" + today.toString+ ".xls"))
  wb.write(out)
  out.close()



}
case class DataRow(placementName: String, placementId: String, avails: Int, sold: Int, publisherRevenue: Float)
