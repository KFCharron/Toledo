package com.mediacrossing.weeklypublisherreport

import org.slf4j.LoggerFactory
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.collection.JavaConversions._
import java.io.{File, FileOutputStream}
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.IndexedColors


object RunWeeklyPubReport extends App {

  val LOG = LoggerFactory.getLogger(RunWeeklyPubReport.getClass)
  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable) {
      LOG.error(e.getMessage, e)
    }
  })

  val props = new ConfigurationProperties(args)
  val anConn = new AppNexusService(
    props.getAppNexusUrl,
    props.getAppNexusUsername,
    props.getAppNexusPassword,
    props.getPartitionSize,
    props.getRequestDelayInSeconds)
  val mxConn = {
    if (props.getMxUsername == null) new MxService(props.getMxUrl)
    else new MxService(props.getMxUrl, props.getMxUsername, props.getMxPassword)
  }

  val pubR = (
    (__ \ "id").read[String] ~
      (__ \ "name").read[String] ~
      (__ \ "status").read[String] ~
      (__ \ "siteIds").read(list[String]) ~
      (__ \ "placementIds").read(list[String])
    ).apply(PubJson.apply _)

  val pubList = Json.parse(mxConn.requestAllPublisherJson)
    .validate(list(pubR))
    //check for jsSuccess
    .get.filter(p => p.status == "active")

  val dateFormat = DateTimeFormat.forPattern("YYYY-MM-dd")

  val pubs = for {p <- pubList} yield {
    val data = for {line <- anConn.requestPublisherWeekly(p.id).tail.toList} yield {
      DataRow(
        day = dateFormat.parseDateTime(line(0)),
        pubId = line(1),
        placeId = line(2),
        placeName = line(3),
        totalImps = line(4).toInt,
        soldImps = line(5).toInt,
        defaultImps = line(6).toInt,
        networkRevenue = line(7).toDouble,
        publisherRevenue = line(8).toDouble,
        eCPM = line(9).toDouble
      )
    }
    val today = DateTime.now()
    val pub = Publisher(
      id = p.id,
      name = p.name,
      //FIXME
      week = data.filter(d => d.day.getDayOfMonth <= 23 && d.day.getDayOfMonth >= 17)
    )
    val wb = writeReport(pub)
    val out = new FileOutputStream(new File(props.getOutputPath, pub.name + "_Weekly_Report.xls"))
    wb.write(out)
    out.close()
  }
  def writeReport(p: Publisher) = {

    val wb = new HSSFWorkbook()

    val font = wb.createFont()
    font.setColor(IndexedColors.WHITE.getIndex)
    val headerStyle = wb.createCellStyle()
    headerStyle.setFont(font)
    headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex)

    val df = wb.createDataFormat
    val currency = wb.createCellStyle
    currency.setDataFormat(df.getFormat("$#,##0.00"))

    val sheet = wb.createSheet("Weekly Report")
    val sumHeadRow = sheet.createRow(0)
    sumHeadRow.createCell(0).setCellValue("Summary")

    val fromRow = sheet.createRow(1);
    fromRow.createCell(0).setCellValue("From")
    val toRow = sheet.createRow(2)
    toRow.createCell(0).setCellValue("To")
    val impRow = sheet.createRow(3)
    impRow.createCell(0).setCellValue("Imps")
    val soldRow = sheet.createRow(4)
    soldRow.createCell(0).setCellValue("Sold")
    val defaultRow = sheet.createRow(5)
    defaultRow.createCell(0).setCellValue("Default")
    val netRevRow = sheet.createRow(6)
    netRevRow.createCell(0).setCellValue("Network Rev.")
    val pubRevRow = sheet.createRow(7)
    pubRevRow.createCell(0).setCellValue("Publisher Rev.")

    var rowCount = 10
    if (p.week.length > 0) {
      sumHeadRow.createCell(1).setCellValue("Week 1")
      fromRow.createCell(1).setCellValue(p.week.last.day.getMonthOfYear + "/" + p.week.last.day.getDayOfMonth)
      toRow.createCell(1).setCellValue(p.week.head.day.getMonthOfYear + "/" + p.week.head.day.getDayOfMonth)
      impRow.createCell(1).setCellValue(p.week.foldLeft(0)(_ + _.totalImps))
      soldRow.createCell(1).setCellValue(p.week.foldLeft(0)(_ + _.soldImps))
      defaultRow.createCell(1).setCellValue(p.week.foldLeft(0)(_ + _.defaultImps))
      netRevRow.createCell(1).setCellValue(p.week.foldLeft(0.0)(_ + _.networkRevenue))
      pubRevRow.createCell(1).setCellValue(p.week.foldLeft(0.0)(_ + _.publisherRevenue))

      //Create a section for list of it's dataRows
      val week1Title = sheet.createRow(rowCount)
      week1Title.createCell(0).setCellValue("Week 1")
      rowCount+=1
      val headers = sheet.createRow(rowCount)
      headers.createCell(0).setCellValue("Date")
      headers.createCell(1).setCellValue("ID")
      headers.createCell(2).setCellValue("Placement")
      headers.createCell(3).setCellValue("Total Imps")
      headers.createCell(4).setCellValue("Sold")
      headers.createCell(5).setCellValue("Default")
      headers.createCell(6).setCellValue("Network Rev.")
      headers.createCell(7).setCellValue("Pub. Rev.")
      headers.createCell(8).setCellValue("eCPM")
      rowCount+=1
      p.week.foreach(d => {
        val dataRow = sheet.createRow(rowCount)
        dataRow.createCell(0).setCellValue(d.day.getMonthOfYear + "/" + d.day.getDayOfMonth)
        dataRow.createCell(1).setCellValue(d.placeId)
        dataRow.createCell(2).setCellValue(d.placeName)
        dataRow.createCell(3).setCellValue(d.totalImps)
        dataRow.createCell(4).setCellValue(d.soldImps)
        dataRow.createCell(5).setCellValue(d.defaultImps)
        dataRow.createCell(6).setCellValue(d.networkRevenue)
        dataRow.createCell(7).setCellValue(d.publisherRevenue)
        dataRow.createCell(8).setCellValue(d.eCPM)
        rowCount+=1
      })
    }

    for (x <- 0 to 9) sheet.autoSizeColumn(x)
    wb
  }
}
case class PubJson(id: String, name: String, status: String, siteIds: List[String], placementIds: List[String])
case class Publisher(id: String, name: String, week: List[DataRow])
case class DataRow(day: DateTime, pubId: String, placeId: String, placeName: String, totalImps: Int, soldImps: Int,
                    defaultImps: Int, networkRevenue: Double, publisherRevenue: Double, eCPM: Double)
