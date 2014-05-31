package com.mediacrossing.dailyclientpublisherreport

import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import scala.collection.JavaConversions._
import org.apache.poi.hssf.usermodel.{HSSFSheet, HSSFWorkbook}
import org.joda.time.{DateTimeZone, LocalDate}
import java.io.{File, FileOutputStream}
import org.apache.poi.ss.usermodel._
import play.api.libs.json._
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.slf4j.LoggerFactory

object RunDailyClientPublisherReport extends App {

  val LOG = LoggerFactory.getLogger(RunDailyClientPublisherReport.getClass)
  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable) {
      LOG.error(e.getMessage, e)
    }
  })

  val props = new ConfigurationProperties(args)
  val anConn = new AppNexusService(props.getPutneyUrl,
                                   props.getAppNexusUsername,
                                   props.getAppNexusPassword)
  val mxConn = new MxService(props.getMxUrl, props.getMxUsername, props.getMxPassword)
  val wb = new HSSFWorkbook()

  val pubR = (
    (__ \ "id").read[String] ~
      (__ \ "name").read[String] ~
      (__ \ "status").read[String]
    ).apply(Publisher.apply _)

  val form = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

  val pubs = Json.parse(mxConn.requestAllPublisherJson())
    .validate(list(pubR)) match {
    case e @ JsError(_) => sys.error(JsError.toFlatJson(e).toString())
    case JsSuccess(v, _) => v.filter(p => {
      p.status.equals("active")
    })
  }

  val reportData = anConn.requestClientPublisherReport()
    .toList
    .tail
    .map(r =>
      DataRow(
        publisherId = r(0),
        placementId = r(1),
        placementName = r(2).replace("_", " "),
        avails = r(3).toInt,
        sold = r(4).toInt + r(5).toInt,
        publisherRevenue = r(6).toFloat))

  pubs.foreach(p => addSheetToWorkbook(p))

  def addSheetToWorkbook(pub: Publisher): HSSFSheet = {
    val pubName = pub.name
    val pubId = pub.id

    val sheet = wb.createSheet(pubName)

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
    val number = wb.createCellStyle()
    number.setDataFormat(df.getFormat("#,##0"))

    // Add row to excel sheet for every placement under the publisher
    var rowCount = 2

    val pubData = reportData.filter(r => r.publisherId == pubId)

    pubData.foreach(r => {
      val row = sheet.createRow(rowCount)
      row.createCell(0).setCellValue(r.placementName)
      row.createCell(1).setCellValue(r.avails)
      row.createCell(2).setCellValue(r.sold)
      row.createCell(3).setCellValue(r.sold.toFloat / r.avails)
      row.createCell(4).setCellValue(r.publisherRevenue)

      row.getCell(1).setCellStyle(number)
      row.getCell(2).setCellStyle(number)
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
    totalRow.getCell(1).setCellStyle(number)
    totalRow.getCell(2).setCellStyle(number)
    totalRow.getCell(3).setCellStyle(percentage)
    totalRow.getCell(4).setCellStyle(currency)

    for(c: Cell <- totalRow) sheet.autoSizeColumn(c.getColumnIndex)

    sheet
  }

  // Workbook Output
  val today = new LocalDate(DateTimeZone.UTC)
  val out = new FileOutputStream(new File(props.getOutputPath, "Daily_Pub_Client_Report" + today.toString+ ".xls"))
  wb.write(out)
  out.close()



}
case class DataRow(publisherId: String,
                   placementName: String,
                   placementId: String,
                   avails: Int,
                   sold: Int,
                   publisherRevenue: Float)
case class Publisher(id: String, name: String, status: String)
