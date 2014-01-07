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
import org.apache.poi.ss.usermodel.{CellStyle, IndexedColors}


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
        paymentType = line(2),
        placeName = line(3),
        totalImps = line(4).toInt,
        soldImps = line(5).toInt,
        defaultImps = line(6).toInt,
        networkRevenue = line(7).toDouble,
        publisherRevenue = line(8).toDouble,
        eCPM = line(9).toDouble,
        servingFees = line(10).toDouble,
        placementSize = line(11),
        country = line(12)
      )
    }
    val today = DateTime.now().withTimeAtStartOfDay()
    val pub = Publisher(
      id = p.id,
      name = p.name,
      week1 = data.filter(d => d.day.isAfter(today.minusDays(7))),
      week2 = data.filter(d => d.day.isAfter(today.minusDays(14)) && d.day.isBefore(today.minusDays(7))),
      week3 = data.filter(d => d.day.isAfter(today.minusDays(21)) && d.day.isBefore(today.minusDays(14))),
      week4 = data.filter(d => d.day.isAfter(today.minusDays(31)) && d.day.isBefore(today.minusDays(21)))
    )
    val wb = writeReport(pub)
    val out = new FileOutputStream(new File(props.getOutputPath, pub.name + "_Weekly_Report.xls"))
    wb.write(out)
    out.close()
  }
  def writeReport(p: Publisher) = {

    val wb = new HSSFWorkbook()

    val headFont = wb.createFont()
    headFont.setColor(IndexedColors.WHITE.getIndex)
    val headStyle = wb.createCellStyle()
    headStyle.setFont(headFont)
    headStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex)
    headStyle.setFillPattern(CellStyle.SOLID_FOREGROUND)

    val df = wb.createDataFormat()
    val numbers = wb.createCellStyle()
    numbers.setDataFormat(df.getFormat("#,###,###"))
    val currency = wb.createCellStyle()
    currency.setDataFormat(df.getFormat("$#,##0.00"))
    val percentage = wb.createCellStyle()
    percentage.setDataFormat(df.getFormat("0.00%"))



    val sheet = wb.createSheet("Weekly Report")
    val sumHeadRow = sheet.createRow(0)
    sumHeadRow.createCell(0).setCellValue("Summary")
    sumHeadRow.getCell(0).setCellStyle(headStyle)

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
    var latestWeek = p.week1

    if (p.week4.length > 0) {
      sumHeadRow.createCell(5).setCellValue("Week 4")
      fromRow.createCell(5).setCellValue(p.week4.last.day.getMonthOfYear + "/" + p.week4.last.day.getDayOfMonth)
      toRow.createCell(5).setCellValue(p.week4.head.day.getMonthOfYear + "/" + p.week4.head.day.getDayOfMonth)
      impRow.createCell(5).setCellValue(p.week4.foldLeft(0)(_ + _.totalImps))
      soldRow.createCell(5).setCellValue(p.week4.foldLeft(0)(_ + _.soldImps))
      defaultRow.createCell(5).setCellValue(p.week4.foldLeft(0)(_ + _.defaultImps))
      netRevRow.createCell(5).setCellValue(p.week4.foldLeft(0.0)(_ + _.networkRevenue))
      pubRevRow.createCell(5).setCellValue(p.week4.foldLeft(0.0)(_ + _.publisherRevenue))
      latestWeek = p.week4
    }

    if (p.week3.length > 0) {
      sumHeadRow.createCell(4).setCellValue("Week 3")
      fromRow.createCell(4).setCellValue(p.week3.last.day.getMonthOfYear + "/" + p.week3.last.day.getDayOfMonth)
      toRow.createCell(4).setCellValue(p.week3.head.day.getMonthOfYear + "/" + p.week3.head.day.getDayOfMonth)
      impRow.createCell(4).setCellValue(p.week3.foldLeft(0)(_ + _.totalImps))
      soldRow.createCell(4).setCellValue(p.week3.foldLeft(0)(_ + _.soldImps))
      defaultRow.createCell(4).setCellValue(p.week3.foldLeft(0)(_ + _.defaultImps))
      netRevRow.createCell(4).setCellValue(p.week3.foldLeft(0.0)(_ + _.networkRevenue))
      pubRevRow.createCell(4).setCellValue(p.week3.foldLeft(0.0)(_ + _.publisherRevenue))
      latestWeek = p.week3
    }

    if (p.week2.length > 0) {
      sumHeadRow.createCell(3).setCellValue("Week 2")
      fromRow.createCell(3).setCellValue(p.week2.last.day.getMonthOfYear + "/" + p.week2.last.day.getDayOfMonth)
      toRow.createCell(3).setCellValue(p.week2.head.day.getMonthOfYear + "/" + p.week2.head.day.getDayOfMonth)
      impRow.createCell(3).setCellValue(p.week2.foldLeft(0)(_ + _.totalImps))
      soldRow.createCell(3).setCellValue(p.week2.foldLeft(0)(_ + _.soldImps))
      defaultRow.createCell(3).setCellValue(p.week2.foldLeft(0)(_ + _.defaultImps))
      netRevRow.createCell(3).setCellValue(p.week2.foldLeft(0.0)(_ + _.networkRevenue))
      pubRevRow.createCell(3).setCellValue(p.week2.foldLeft(0.0)(_ + _.publisherRevenue))
      latestWeek = p.week2
    }

    if (p.week1.length > 0) {
      sumHeadRow.createCell(2).setCellValue("Week 1")
      fromRow.createCell(2).setCellValue(p.week1.last.day.getMonthOfYear + "/" + p.week1.last.day.getDayOfMonth)
      toRow.createCell(2).setCellValue(p.week1.head.day.getMonthOfYear + "/" + p.week1.head.day.getDayOfMonth)
      impRow.createCell(2).setCellValue(p.week1.foldLeft(0)(_ + _.totalImps))
      soldRow.createCell(2).setCellValue(p.week1.foldLeft(0)(_ + _.soldImps))
      defaultRow.createCell(2).setCellValue(p.week1.foldLeft(0)(_ + _.defaultImps))
      netRevRow.createCell(2).setCellValue(p.week1.foldLeft(0.0)(_ + _.networkRevenue))
      pubRevRow.createCell(2).setCellValue(p.week1.foldLeft(0.0)(_ + _.publisherRevenue))
    }

    //Create a section for list of it's dataRows
    val week1Title = sheet.createRow(rowCount)
    week1Title.createCell(0).setCellValue("Latest Week")
    week1Title.getCell(0).setCellStyle(headStyle)
    rowCount+=1
    val headers = sheet.createRow(rowCount)
    headers.createCell(0).setCellValue("Date")
    headers.createCell(1).setCellValue("Placement")
    headers.createCell(2).setCellValue("Country")
    headers.createCell(3).setCellValue("Size")
    headers.createCell(4).setCellValue("Total Imps")
    headers.createCell(5).setCellValue("Sold")
    headers.createCell(6).setCellValue("Default")
    headers.createCell(7).setCellValue("Fill Rate")
    headers.createCell(8).setCellValue("Network Rev.")
    headers.createCell(9).setCellValue("Pub. Rev.")
    headers.createCell(10).setCellValue("eCPM")
    for(c <- headers) c.setCellStyle(headStyle)
    rowCount+=1
    latestWeek.foreach(d => {
      val dataRow = sheet.createRow(rowCount)
      dataRow.createCell(0).setCellValue(d.day.getMonthOfYear + "/" + d.day.getDayOfMonth)
      dataRow.createCell(1).setCellValue(d.placeName)
      dataRow.createCell(2).setCellValue(d.country)
      dataRow.createCell(3).setCellValue(d.placementSize)
      dataRow.createCell(4).setCellValue(d.totalImps)
      dataRow.createCell(5).setCellValue(d.soldImps)
      dataRow.createCell(6).setCellValue(d.defaultImps)
      dataRow.createCell(7).setCellValue(d.fillRate)
      dataRow.createCell(8).setCellValue(d.networkRevenue)
      dataRow.createCell(9)
      if (d.paymentType.equals("revshare")) {
        headers.getCell(9).setCellValue("Gross Rev.")
        dataRow.getCell(9).setCellValue(d.grossRevenue)
      } else dataRow.getCell(9).setCellValue(d.publisherRevenue)
      dataRow.createCell(10).setCellValue(d.eCPM)

      dataRow.getCell(4).setCellStyle(numbers)
      dataRow.getCell(5).setCellStyle(numbers)
      dataRow.getCell(6).setCellStyle(numbers)
      dataRow.getCell(7).setCellStyle(percentage)
      dataRow.getCell(8).setCellStyle(currency)
      dataRow.getCell(9).setCellStyle(currency)
      dataRow.getCell(10).setCellStyle(currency)

      rowCount+=1
    }
    )

    for (x <- 0 to 11) sheet.autoSizeColumn(x)
    wb
  }
}
case class PubJson(id: String, name: String, status: String, siteIds: List[String], placementIds: List[String])
case class Publisher(id: String, name: String, week1: List[DataRow], week2: List[DataRow], week3: List[DataRow],
                     week4: List[DataRow])
case class DataRow(day: DateTime, pubId: String, paymentType: String, placeName: String, totalImps: Int,
                   soldImps: Int, defaultImps: Int, networkRevenue: Double, publisherRevenue: Double,
                   eCPM: Double, servingFees: Double, placementSize: String, country: String) {
  val fillRate = Int.int2float(soldImps) / Int.int2float(totalImps)
  val grossRevenue = servingFees + publisherRevenue
}
