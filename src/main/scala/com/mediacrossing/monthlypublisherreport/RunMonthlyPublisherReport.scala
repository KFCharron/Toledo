package com.mediacrossing.monthlypublisherreport

import org.slf4j.LoggerFactory
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import play.api.libs.json._
import play.api.libs.json.Reads._
import com.mediacrossing.weeklypublisherreport.{DataRow, PubJson}
import play.api.libs.functional.syntax._
import org.joda.time.format.DateTimeFormat
import scala.collection.JavaConversions._
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.{CellStyle, IndexedColors}
import java.io.{File, FileOutputStream}
import org.joda.time.DateTime

object RunMonthlyPubReport extends App {

  val LOG = LoggerFactory.getLogger(RunMonthlyPubReport.getClass)
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
    val data = for {line <- anConn.requestPublisherReport(p.id).tail.toList.filterNot(l => l(2).equals("CPM"))} yield {
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
        placementSize = line(11)
      )
    }
    val firstOfMonth = DateTime.now().withDayOfMonth(1).withTimeAtStartOfDay()
    val pub = Publisher(
      id = p.id,
      name = p.name,
      daily = data.filter(d => d.day.isBefore(firstOfMonth) && d.day.isAfter(firstOfMonth
        .minusMonths(1)
        .minusDays(1))),
      pnl = getPublisherPnl(p.id)
    )

    val wb = writeReport(pub)
    val out = new FileOutputStream(new File(props.getOutputPath, pub.name + "_Monthly_Report.xls"))
    wb.write(out)
    out.close()
  }

  def getPublisherPnl(id: String) = {
     val csv = anConn.getPnlReport(id, "last_month")
     if (csv.size() > 1) {
       val l = csv.get(1)
       val netProfit = l(1).toFloat
       val impsTotal = l(2).toFloat
       val impsResold = l(3).toFloat
       val servingFees = l(4).toFloat
       val resold = anConn.getResoldRevenue(id, "last_month")
       netProfit - (resold * .135) - ((impsTotal - impsResold) * .001 * .017) - servingFees
     } else 0.0
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

    val sheet = wb.createSheet("Monthly Report")
    val sumHeadRow = sheet.createRow(0)
    sumHeadRow.createCell(0).setCellValue("Summary")
    sumHeadRow.getCell(0).setCellStyle(headStyle)
    val sumTotals = sheet.createRow(1)
    sumTotals.createCell(0)
    sumTotals.createCell(1)
    sumTotals.createCell(2)
    sumTotals.createCell(3).setCellValue("Total Imps")
    sumTotals.createCell(4).setCellValue("Sold")
    sumTotals.createCell(5).setCellValue("Default")
    sumTotals.createCell(6).setCellValue("Network Rev.")
    sumTotals.createCell(7).setCellValue("Publisher Rev.")
    sumTotals.createCell(8)
    sumTotals.createCell(9)
    sumTotals.createCell(10).setCellValue("PnL")
    for (c <- sumTotals) c.setCellStyle(headStyle)

    val sums = sheet.createRow(2)
    sums.createCell(3).setCellValue(p.daily.foldLeft(0)(_ + _.totalImps))
    sums.createCell(4).setCellValue(p.daily.foldLeft(0)(_ + _.soldImps))
    sums.createCell(5).setCellValue(p.daily.foldLeft(0)(_ + _.defaultImps))
    sums.createCell(6).setCellValue(p.daily.foldLeft(0.0)(_ + _.networkRevenue))
    sums.createCell(7).setCellValue(p.daily.foldLeft(0.0)(_ + _.publisherRevenue))
    sums.createCell(10).setCellValue(p.pnl)
    sums.getCell(3).setCellStyle(numbers)
    sums.getCell(4).setCellStyle(numbers)
    sums.getCell(5).setCellStyle(numbers)
    sums.getCell(6).setCellStyle(currency)
    sums.getCell(7).setCellStyle(currency)
    sums.getCell(10).setCellStyle(currency)


    var rowCount = 4


    //Create a section for list of it's dataRows
    val week1Title = sheet.createRow(rowCount)
    week1Title.createCell(0).setCellValue("Last Month")
    week1Title.getCell(0).setCellStyle(headStyle)
    rowCount+=1
    val headers = sheet.createRow(rowCount)
    headers.createCell(0).setCellValue("Date")
    headers.createCell(1).setCellValue("Placement")
    headers.createCell(2).setCellValue("Size")
    headers.createCell(3).setCellValue("Total Imps")
    headers.createCell(4).setCellValue("Sold")
    headers.createCell(5).setCellValue("Default")
    headers.createCell(6).setCellValue("Network Rev.")
    headers.createCell(7).setCellValue("Pub. Rev.")
    headers.createCell(8).setCellValue("eCPM")
    headers.createCell(9).setCellValue("Fill Rate")

    for(c <- headers) c.setCellStyle(headStyle)
    rowCount+=1
    p.daily.foreach(d => {
      val dataRow = sheet.createRow(rowCount)
      dataRow.createCell(0).setCellValue(d.day.getMonthOfYear + "/" + d.day.getDayOfMonth)
      dataRow.createCell(1).setCellValue(d.placeName)
      dataRow.createCell(2).setCellValue(d.placementSize)
      dataRow.createCell(3).setCellValue(d.totalImps)
      dataRow.createCell(4).setCellValue(d.soldImps)
      dataRow.createCell(5).setCellValue(d.defaultImps)
      dataRow.createCell(6).setCellValue(d.networkRevenue)
      dataRow.createCell(7)
      if (d.paymentType.equals("Owner Revshare")) {
        headers.getCell(7).setCellValue("Gross Rev.")
        dataRow.getCell(7).setCellValue(d.grossRevenue)
      } else dataRow.getCell(7).setCellValue(d.publisherRevenue)
      dataRow.createCell(8).setCellValue(d.eCPM)
      dataRow.createCell(9).setCellValue(d.fillRate)

      dataRow.getCell(3).setCellStyle(numbers)
      dataRow.getCell(4).setCellStyle(numbers)
      dataRow.getCell(5).setCellStyle(numbers)
      dataRow.getCell(6).setCellStyle(currency)
      dataRow.getCell(7).setCellStyle(currency)
      dataRow.getCell(8).setCellStyle(currency)
      dataRow.getCell(9).setCellStyle(percentage)

      rowCount+=1
    }
    )

    val totalRow = sheet.createRow(rowCount+1)
    totalRow.createCell(0).setCellValue("Totals:")
    totalRow.createCell(3).setCellValue(p.daily.foldLeft(0)(_ + _.totalImps))
    totalRow.createCell(4).setCellValue(p.daily.foldLeft(0)(_ + _.soldImps))
    totalRow.createCell(5).setCellValue(p.daily.foldLeft(0)(_ + _.defaultImps))
    totalRow.createCell(6).setCellValue(p.daily.foldLeft(0.0)(_ + _.networkRevenue))
    totalRow.createCell(7).setCellValue(p.daily.foldLeft(0.0)(_ + _.publisherRevenue))
    totalRow.createCell(9).setCellValue(totalRow.getCell(4).getNumericCellValue /
      totalRow.getCell(3).getNumericCellValue)

    totalRow.getCell(3).setCellStyle(numbers)
    totalRow.getCell(4).setCellStyle(numbers)
    totalRow.getCell(5).setCellStyle(numbers)
    totalRow.getCell(6).setCellStyle(currency)
    totalRow.getCell(7).setCellStyle(currency)
    totalRow.getCell(9).setCellStyle(percentage)

    for (x <- 0 to 11) sheet.autoSizeColumn(x)
    wb
  }
}
case class Publisher(id: String, name: String, daily: List[DataRow], pnl: Double)


