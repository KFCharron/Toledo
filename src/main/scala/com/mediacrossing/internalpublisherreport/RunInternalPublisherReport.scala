package com.mediacrossing.internalpublisherreport

import org.slf4j.LoggerFactory
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.{DateTimeConstants, DateTime}
import org.joda.time.format.DateTimeFormat
import scala.collection.JavaConversions._
import java.io.{File, FileOutputStream}
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.{CellStyle, IndexedColors}

object RunInternalPublisherReport extends App {

  val LOG = LoggerFactory.getLogger(RunInternalPublisherReport.getClass)
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
    val data = for {line <- anConn.requestInternalPublisherReport(p.id).tail.toList.filterNot(l => l(2).equals("CPM"))} yield {
      DataRow(
        day = dateFormat.parseDateTime(line(0)),
        pubId = line(1),
        paymentType = line(2),
        placeName = line(3),
        totalImps = line(4).toInt,
        keptImps = line(5).toInt,
        resoldImps = line(6).toInt,
        defaultImps = line(7).toInt,
        networkRevenue = line(8).toDouble,
        publisherRevenue = line(9).toDouble,
        eCPM = line(10).toDouble,
        servingFees = line(11).toDouble,
        placementSize = line(12)
      )
    }

    val brands = anConn.requestTopBrandReport(p.id).toList

    val monday = DateTime.now().withTimeAtStartOfDay().withDayOfWeek(DateTimeConstants.MONDAY)
    val pub = Publisher(
      id = p.id,
      name = p.name,
      week4 = data.filter(d => d.day.isBefore(monday) && d.day.isAfter(monday.minusDays(8))),
      week3 = data.filter(d => d.day.isBefore(monday.minusDays(7)) && d.day.isAfter(monday.minusDays(15))),
      week2 = data.filter(d => d.day.isBefore(monday.minusDays(14)) && d.day.isAfter(monday.minusDays(22))),
      week1 = data.filter(d => d.day.isBefore(monday.minusDays(21)) && d.day.isAfter(monday.minusDays(29))),
      brands = brands
    )
    val wb = writeReport(pub)
    val out = new FileOutputStream(new File(props.getOutputPath, "Internal_" + pub.name + "_Weekly_Report.xls"))
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
    sumHeadRow.createCell(1).setCellStyle(headStyle)
    val fromRow = sheet.createRow(1)
    fromRow.createCell(0).setCellValue("From")
    val toRow = sheet.createRow(2)
    toRow.createCell(0).setCellValue("To")
    val impRow = sheet.createRow(3)
    impRow.createCell(0).setCellValue("Imps")
    val keptRow = sheet.createRow(4)
    keptRow.createCell(0).setCellValue("Kept")
    val resoldRow = sheet.createRow(5)
    resoldRow.createCell(0).setCellValue("Resold")
    val defaultRow = sheet.createRow(6)
    defaultRow.createCell(0).setCellValue("Default")
    val pubRevRow = sheet.createRow(7)
    pubRevRow.createCell(0).setCellValue("Publisher Rev.")
    val netRevRow = sheet.createRow(8)
    netRevRow.createCell(0).setCellValue("Network Rev.")
    val bRow1 = sheet.createRow(9)
    val bRow2 = sheet.createRow(10)

    var rowCount = 12
    val latestWeek = p.week4

    if (p.week4.length > 0) {
      sumHeadRow.createCell(5).setCellValue("Last Week")
      fromRow.createCell(5).setCellValue(p.week4.head.day.getMonthOfYear + "/" + p.week4.head.day.getDayOfMonth)
      toRow.createCell(5).setCellValue(p.week4.last.day.getMonthOfYear + "/" + p.week4.last.day.getDayOfMonth)
      impRow.createCell(5).setCellValue(p.week4.foldLeft(0)(_ + _.totalImps))
      keptRow.createCell(5).setCellValue(p.week4.foldLeft(0)(_ + _.keptImps))
      resoldRow.createCell(5).setCellValue(p.week4.foldLeft(0)(_ + _.resoldImps))
      defaultRow.createCell(5).setCellValue(p.week4.foldLeft(0)(_ + _.defaultImps))
      netRevRow.createCell(5).setCellValue(p.week4.foldLeft(0.0)(_ + _.networkRevenue))
      pubRevRow.createCell(5).setCellValue(p.week4.foldLeft(0.0)(_ + _.publisherRevenue))
      impRow.getCell(5).setCellStyle(numbers)
      keptRow.getCell(5).setCellStyle(numbers)
      resoldRow.getCell(5).setCellStyle(numbers)
      defaultRow.getCell(5).setCellStyle(numbers)
      netRevRow.getCell(5).setCellStyle(currency)
      pubRevRow.getCell(5).setCellStyle(currency)

      //Top Brands List
      sumHeadRow.createCell(7).setCellValue("Top Brands")
      sumHeadRow.getCell(7).setCellStyle(headStyle)
      sumHeadRow.createCell(8).setCellValue("Imps")
      sumHeadRow.getCell(8).setCellStyle(headStyle)
      fromRow.createCell(7).setCellValue(p.brands(0)._1); fromRow.createCell(8).setCellValue(p.brands(0)._2.toInt)
      toRow.createCell(7).setCellValue(p.brands(1)._1); toRow.createCell(8).setCellValue(p.brands(1)._2.toInt)
      impRow.createCell(7).setCellValue(p.brands(2)._1); impRow.createCell(8).setCellValue(p.brands(2)._2.toInt)
      keptRow.createCell(7).setCellValue(p.brands(3)._1); keptRow.createCell(8).setCellValue(p.brands(3)._2.toInt)
      resoldRow.createCell(7).setCellValue(p.brands(4)._1); resoldRow.createCell(8).setCellValue(p.brands(4)._2.toInt)
      defaultRow.createCell(7).setCellValue(p.brands(5)._1); defaultRow.createCell(8).setCellValue(p.brands(5)._2.toInt)
      pubRevRow.createCell(7).setCellValue(p.brands(6)._1); pubRevRow.createCell(8).setCellValue(p.brands(6)._2.toInt)
      netRevRow.createCell(7).setCellValue(p.brands(7)._1); netRevRow.createCell(8).setCellValue(p.brands(7)._2.toInt)
      bRow1.createCell(7).setCellValue(p.brands(8)._1); bRow1.createCell(8).setCellValue(p.brands(8)._2.toInt)
      bRow2.createCell(7).setCellValue(p.brands(9)._1); bRow2.createCell(8).setCellValue(p.brands(9)._2.toInt)
    }

    if (p.week3.length > 0) {
      sumHeadRow.createCell(4).setCellValue("2 Weeks Ago")
      fromRow.createCell(4).setCellValue(p.week3.head.day.getMonthOfYear + "/" + p.week3.head.day.getDayOfMonth)
      toRow.createCell(4).setCellValue(p.week3.last.day.getMonthOfYear + "/" + p.week3.last.day.getDayOfMonth)
      impRow.createCell(4).setCellValue(p.week3.foldLeft(0)(_ + _.totalImps))
      keptRow.createCell(4).setCellValue(p.week3.foldLeft(0)(_ + _.keptImps))
      resoldRow.createCell(4).setCellValue(p.week3.foldLeft(0)(_ + _.resoldImps))
      defaultRow.createCell(4).setCellValue(p.week3.foldLeft(0)(_ + _.defaultImps))
      netRevRow.createCell(4).setCellValue(p.week3.foldLeft(0.0)(_ + _.networkRevenue))
      pubRevRow.createCell(4).setCellValue(p.week3.foldLeft(0.0)(_ + _.publisherRevenue))
      impRow.getCell(4).setCellStyle(numbers)
      keptRow.getCell(4).setCellStyle(numbers)
      resoldRow.getCell(4).setCellStyle(numbers)
      defaultRow.getCell(4).setCellStyle(numbers)
      netRevRow.getCell(4).setCellStyle(currency)
      pubRevRow.getCell(4).setCellStyle(currency)
    }

    if (p.week2.length > 0) {
      sumHeadRow.createCell(3).setCellValue("3 Weeks Ago")
      fromRow.createCell(3).setCellValue(p.week2.head.day.getMonthOfYear + "/" + p.week2.head.day.getDayOfMonth)
      toRow.createCell(3).setCellValue(p.week2.last.day.getMonthOfYear + "/" + p.week2.last.day.getDayOfMonth)
      impRow.createCell(3).setCellValue(p.week2.foldLeft(0)(_ + _.totalImps))
      keptRow.createCell(3).setCellValue(p.week2.foldLeft(0)(_ + _.keptImps))
      resoldRow.createCell(3).setCellValue(p.week2.foldLeft(0)(_ + _.resoldImps))
      defaultRow.createCell(3).setCellValue(p.week2.foldLeft(0)(_ + _.defaultImps))
      netRevRow.createCell(3).setCellValue(p.week2.foldLeft(0.0)(_ + _.networkRevenue))
      pubRevRow.createCell(3).setCellValue(p.week2.foldLeft(0.0)(_ + _.publisherRevenue))
      impRow.getCell(3).setCellStyle(numbers)
      keptRow.getCell(3).setCellStyle(numbers)
      resoldRow.getCell(3).setCellStyle(numbers)
      defaultRow.getCell(3).setCellStyle(numbers)
      netRevRow.getCell(3).setCellStyle(currency)
      pubRevRow.getCell(3).setCellStyle(currency)
    }

    if (p.week1.length > 0) {
      sumHeadRow.createCell(2).setCellValue("4 Weeks Ago")
      fromRow.createCell(2).setCellValue(p.week1.head.day.getMonthOfYear + "/" + p.week1.head.day.getDayOfMonth)
      toRow.createCell(2).setCellValue(p.week1.last.day.getMonthOfYear + "/" + p.week1.last.day.getDayOfMonth)
      impRow.createCell(2).setCellValue(p.week1.foldLeft(0)(_ + _.totalImps))
      keptRow.createCell(2).setCellValue(p.week1.foldLeft(0)(_ + _.keptImps))
      resoldRow.createCell(2).setCellValue(p.week1.foldLeft(0)(_ + _.resoldImps))
      defaultRow.createCell(2).setCellValue(p.week1.foldLeft(0)(_ + _.defaultImps))
      netRevRow.createCell(2).setCellValue(p.week1.foldLeft(0.0)(_ + _.networkRevenue))
      pubRevRow.createCell(2).setCellValue(p.week1.foldLeft(0.0)(_ + _.publisherRevenue))
      impRow.getCell(2).setCellStyle(numbers)
      keptRow.getCell(2).setCellStyle(numbers)
      resoldRow.getCell(2).setCellStyle(numbers)
      defaultRow.getCell(2).setCellStyle(numbers)
      netRevRow.getCell(2).setCellStyle(currency)
      pubRevRow.getCell(2).setCellStyle(currency)
    }

    //Create a section for list of it's dataRows
    val week1Title = sheet.createRow(rowCount)
    week1Title.createCell(0).setCellValue("Latest Week")
    week1Title.getCell(0).setCellStyle(headStyle)
    rowCount+=1
    val headers = sheet.createRow(rowCount)
    headers.createCell(0).setCellValue("Date")
    headers.createCell(1).setCellValue("Placement")
    headers.createCell(2).setCellValue("Size")
    headers.createCell(3).setCellValue("Total Imps")
    headers.createCell(4).setCellValue("Kept")
    headers.createCell(5).setCellValue("Resold")
    headers.createCell(6).setCellValue("Default")
    headers.createCell(7).setCellValue("Network Rev.")
    headers.createCell(8).setCellValue("Pub. Rev.")
    headers.createCell(9).setCellValue("eCPM")
    headers.createCell(10).setCellValue("Fill Rate")
    headers.createCell(11).setCellValue("Serving Fees")

    for(c <- headers) c.setCellStyle(headStyle)
    rowCount+=1
    latestWeek.foreach(d => {
      val dataRow = sheet.createRow(rowCount)
      dataRow.createCell(0).setCellValue(d.day.getMonthOfYear + "/" + d.day.getDayOfMonth)
      dataRow.createCell(1).setCellValue(d.placeName)
      dataRow.createCell(2).setCellValue(d.placementSize)
      dataRow.createCell(3).setCellValue(d.totalImps)
      dataRow.createCell(4).setCellValue(d.keptImps)
      dataRow.createCell(5).setCellValue(d.resoldImps)
      dataRow.createCell(6).setCellValue(d.defaultImps)
      dataRow.createCell(7).setCellValue(d.networkRevenue)
      dataRow.createCell(8)
      if (d.paymentType.equals("Owner Revshare")) {
        headers.getCell(8).setCellValue("Gross Rev.")
        dataRow.getCell(8).setCellValue(d.grossRevenue)
      } else dataRow.getCell(8).setCellValue(d.publisherRevenue)
      dataRow.createCell(9).setCellValue(d.eCPM)
      dataRow.createCell(10).setCellValue(d.fillRate)
      dataRow.createCell(11).setCellValue(d.servingFees)

      dataRow.getCell(3).setCellStyle(numbers)
      dataRow.getCell(4).setCellStyle(numbers)
      dataRow.getCell(5).setCellStyle(numbers)
      dataRow.getCell(6).setCellStyle(numbers)
      dataRow.getCell(7).setCellStyle(currency)
      dataRow.getCell(8).setCellStyle(currency)
      dataRow.getCell(9).setCellStyle(currency)
      dataRow.getCell(10).setCellStyle(percentage)
      dataRow.getCell(11).setCellStyle(currency)

      rowCount+=1
    }
    )
    val totalRow = sheet.createRow(rowCount+1)
    totalRow.createCell(0).setCellValue("Totals:")
    totalRow.createCell(3).setCellValue(latestWeek.foldLeft(0)(_ + _.totalImps))
    totalRow.createCell(4).setCellValue(latestWeek.foldLeft(0)(_ + _.keptImps))
    totalRow.createCell(5).setCellValue(latestWeek.foldLeft(0)(_ + _.resoldImps))
    totalRow.createCell(6).setCellValue(latestWeek.foldLeft(0)(_ + _.defaultImps))
    totalRow.createCell(7).setCellValue(latestWeek.foldLeft(0.0)(_ + _.networkRevenue))
    totalRow.createCell(8).setCellValue(latestWeek.foldLeft(0.0)(_ + _.publisherRevenue))
    totalRow.createCell(10).setCellValue(
      (totalRow.getCell(4).getNumericCellValue + totalRow.getCell(5).getNumericCellValue) /
      totalRow.getCell(3).getNumericCellValue
    )
    totalRow.createCell(11).setCellValue(latestWeek.foldLeft(0.0)(_ + _.servingFees))

    totalRow.getCell(3).setCellStyle(numbers)
    totalRow.getCell(4).setCellStyle(numbers)
    totalRow.getCell(5).setCellStyle(numbers)
    totalRow.getCell(6).setCellStyle(numbers)
    totalRow.getCell(7).setCellStyle(currency)
    totalRow.getCell(8).setCellStyle(currency)
    totalRow.getCell(10).setCellStyle(percentage)
    totalRow.getCell(11).setCellStyle(currency)

    for (x <- 0 to 12) sheet.autoSizeColumn(x)
    wb
  }
}
case class PubJson(id: String, name: String, status: String, siteIds: List[String], placementIds: List[String])
case class Publisher(id: String, name: String, week1: List[DataRow], week2: List[DataRow], week3: List[DataRow],
                     week4: List[DataRow], brands: List[(String, String)])
case class DataRow(day: DateTime, pubId: String, paymentType: String, placeName: String, totalImps: Int,
                   keptImps: Int, resoldImps: Int, defaultImps: Int, networkRevenue: Double, publisherRevenue: Double,
                   eCPM: Double, servingFees: Double, placementSize: String) {
  val fillRate = Int.int2float(keptImps + resoldImps) / Int.int2float(totalImps)
  val grossRevenue = servingFees + publisherRevenue
}
