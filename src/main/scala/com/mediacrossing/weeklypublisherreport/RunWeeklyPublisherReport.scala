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

  val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd")

  val pubs = for {p <- pubList} yield {
    val data = for {line <- anConn.requestPublisherWeekly(p.id).toList} yield {
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
      week1 = data.filter(d => d.day.getWeekOfWeekyear.equals(today.getWeekOfWeekyear)),
      week2 = data.filter(d => d.day.getWeekOfWeekyear.equals(today.getWeekOfWeekyear-1)),
      week3 = data.filter(d => d.day.getWeekOfWeekyear.equals(today.getWeekOfWeekyear-2)),
      week4 = data.filter(d => d.day.getWeekOfWeekyear.equals(today.getWeekOfWeekyear-3))
    )
    val wb = writeReport(pub)
//    val out = new FileOutputStream(new File(props.getOutputPath, pub.name + "_Weekly_Report_" + today.toString + ".xls"))

  }
  def writeReport(p: Publisher) = {
    val wb = new HSSFWorkbook()

    val df = wb.createDataFormat
    val currency = wb.createCellStyle
    currency.setDataFormat(df.getFormat("$#,##0.00"))

    val sheet = wb.createSheet("Weekly Report")
    val sumHeadRow = sheet.createRow(0)
    sumHeadRow.createCell(0).setCellValue("Summary")
    if (p.week1.length > 0) sumHeadRow.createCell(1).setCellValue("Week 1")
    if (p.week2.length > 0) sumHeadRow.createCell(2).setCellValue("Week 2")
    if (p.week3.length > 0) sumHeadRow.createCell(3).setCellValue("Week 3")
    if (p.week4.length > 0) sumHeadRow.createCell(4).setCellValue("Week 4")
    val fromRow = sheet.createRow(1).createCell(0).setCellValue("From")
    val toRow = sheet.createRow(2).createCell(0).setCellValue("To")
    val impRow = sheet.createRow(3).createCell(0).setCellValue("Imps")
    val soldRow = sheet.createRow(4).createCell(0).setCellValue("Sold")
    val defaultRow = sheet.createRow(5).createCell(0).setCellValue("Default")
    val netRevRow = sheet.createRow(6).createCell(0).setCellValue("Network Rev.")
    val pubRevRow = sheet.createRow(7).createCell(0).setCellValue("Publisher Rev.")
    //TODO
  }



}
case class PubJson(id: String, name: String, status: String, siteIds: List[String], placementIds: List[String])
case class Publisher(id: String, name: String, week1: List[DataRow], week2: List[DataRow], week3: List[DataRow],
                     week4: List[DataRow])
case class DataRow(day: DateTime, pubId: String, placeId: String, placeName: String, totalImps: Int, soldImps: Int,
                    defaultImps: Int, networkRevenue: Double, publisherRevenue: Double, eCPM: Double)
